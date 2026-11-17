package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.common.NameValueProcessorJacksonConfig;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProcessor;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProperties;
import cn.hiboot.mcn.autoconfigure.web.security.WebSecurityProperties;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.util.SpringBeanUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.boot.context.properties.bind.Binder;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JacksonFilterContractTest {

    private GenericApplicationContext context;
    private Field contextField;
    private Object previousContext;
    private String previousRule;

    @BeforeEach
    void createIsolatedContext() throws ReflectiveOperationException {
        context = new GenericApplicationContext();
        context.registerBean(WebSecurityProperties.class, () -> {
            WebSecurityProperties properties = new WebSecurityProperties();
            properties.setDefaultExcludeUrls(new String[0]);
            return properties;
        });
        // SpringBeanUtils has no reset API; restore its static state after each contract check.
        contextField = SpringBeanUtils.class.getDeclaredField("context");
        contextField.setAccessible(true);
        previousContext = contextField.get(null);
        previousRule = ParamProcessorProperties.globalRule;
        contextField.set(null, context);
    }

    @AfterEach
    void restoreContext() throws IllegalAccessException {
        NameValueProcessorJacksonConfig.removeFeignRequest();
        ParamProcessorProperties.globalRule = previousRule;
        contextField.set(null, previousContext);
        context.close();
    }

    @Test
    void jsonStringsAreEscapedWithoutRawPayloadProcessingAndResponseEscapingIsOptIn() {
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("xss",
                Map.of("mcn.xss.exclude-fields", "raw", "mcn.xss.process-payload", "false")));
        XssProperties properties = Binder.get(context.getEnvironment()).bind("mcn.xss", XssProperties.class).get();
        context.registerBean(XssProperties.class, () -> properties);
        context.registerBean(XssProcessor.class, () -> new XssAutoConfiguration().defaultXssProcessor(properties));
        context.refresh();
        JsonMapper mapper = mapper();

        assertFalse(properties.isProcessPayload());
        Payload incoming = mapper.readValue("{\"text\":\"<b>&\",\"raw\":\"<b>&\"}", Payload.class);
        assertEquals("&lt;b&gt;&amp;", incoming.text());
        assertEquals("<b>&", incoming.raw());
        Payload response = new Payload("<b>&", "<b>&");
        assertEquals("<b>&", mapper.readTree(mapper.writeValueAsString(response)).get("text").asString());

        properties.setEscapeResponse(true);
        JsonMapper responseMapper = mapper();
        var output = responseMapper.readTree(responseMapper.writeValueAsString(response));
        assertEquals("&lt;b&gt;&amp;", output.get("text").asString());
        assertEquals("&lt;b&gt;&amp;", output.get("raw").asString());
        NameValueProcessorJacksonConfig.setFeignRequest();
        assertEquals("<b>&", responseMapper.readValue("{\"text\":\"<b>&\"}", Payload.class).text());
    }

    @Test
    void disablingTheParameterFilterDoesNotDisableJacksonInputRejection() {
        ParamProcessorProperties properties = new ParamProcessorProperties();
        properties.setUseFilter(false);
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("rule",
                Map.of("global.rule.pattern", "(?s).*[<>].*")));
        properties.setEnvironment(context.getEnvironment());
        context.registerBean(ParamProcessorProperties.class, () -> properties);
        context.registerBean(ParamProcessor.class, () -> new ParamProcessorAutoConfiguration().defaultParamProcessor());
        context.refresh();
        JsonMapper mapper = mapper();

        assertEquals("plain", mapper.readValue("{\"text\":\"plain\"}", Payload.class).text());
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> mapper.readValue("{\"text\":\"<script>\"}", Payload.class));
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        assertInstanceOf(ServiceException.class, cause);
        assertEquals("<script>", mapper.readTree(mapper.writeValueAsString(new Payload("<script>", null)))
                .get("text").asString());
    }

    private JsonMapper mapper() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new NameValueProcessorJacksonConfig(context.getBeanProvider(NameValueProcessor.class)).customize(builder);
        return builder.build();
    }

    public record Payload(String text, String raw) { }
}
