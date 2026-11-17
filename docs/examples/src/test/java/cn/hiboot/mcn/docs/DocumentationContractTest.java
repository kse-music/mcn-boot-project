package cn.hiboot.mcn.docs;

import cn.hiboot.mcn.autoconfigure.context.McnEnvironmentPostProcessor;
import cn.hiboot.mcn.autoconfigure.jdbc.DataSourceHolder;
import cn.hiboot.mcn.autoconfigure.jdbc.MultipleDataSourceConfig;
import cn.hiboot.mcn.autoconfigure.redis.DistributedLocker;
import cn.hiboot.mcn.autoconfigure.sql.SqlInitAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.sql.SqlInitProperties;
import cn.hiboot.mcn.autoconfigure.validator.ValidatorProperties;
import cn.hiboot.mcn.autoconfigure.validator.annotation.ValidatorForPhone;
import cn.hiboot.mcn.autoconfigure.web.exception.ExceptionProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.cors.CorsProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.integrity.DataIntegrityUtils;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorAutoConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessorProperties;
import cn.hiboot.mcn.autoconfigure.web.filter.special.servlet.ServletParamProcessorConfiguration;
import cn.hiboot.mcn.autoconfigure.web.filter.xss.XssProperties;
import cn.hiboot.mcn.cloud.encryptor.sm2.SM2Encryptor;
import cn.hiboot.mcn.cloud.encryptor.sm4.EncryptorProperties;
import cn.hiboot.mcn.core.exception.ServiceException;
import cn.hiboot.mcn.core.util.JacksonUtils;
import cn.hutool.crypto.SmUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DocumentationContractTest {

    @Test
    void responseExampleUsesTheRealSerializedContract() {
        Map<String, Object> response = JacksonUtils.toMap(ResponseExamples.writeResponse());
        assertEquals("OK", response.get("ActionStatus"));
        assertEquals(0, ((Number) response.get("ErrorCode")).intValue());
        assertEquals("", response.get("ErrorInfo"));
        assertEquals(List.of("demo"), response.get("data"));
        assertEquals(1, ((Number) response.get("count")).longValue());
        assertFalse(response.containsKey("Duration"));
        assertFalse(response.containsKey("success"));
        assertEquals(List.of("a", "b"), ResponseExamples.readNames("[\"a\",\"b\"]"));
        ServiceException error = assertThrows(ServiceException.class, () -> ErrorConfiguration.requireName(" "));
        assertEquals(400001, error.getCode());
    }

    @Test
    void phoneConstraintAllowsEmptyValuesAndOnlyChecksElevenDigits() {
        ValidatorForPhone validator = new ValidatorForPhone();
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("00000000000", null));
        assertFalse(validator.isValid("123", null));
        assertFalse(validator.isValid("abcdefghijk", null));
    }

    @Test
    void publishedCorsExampleBindsMultipleOriginsAndMethods() throws IOException {
        CorsProperties cors = bind("cors.properties", "mcn.cors", CorsProperties.class);
        assertEquals(List.of("https://console.example.com", "https://admin.example.com"), cors.getAllowedOrigins());
        assertEquals(List.of("GET", "POST"), cors.getAllowedMethods());
        assertEquals(List.of("Content-Type", "Authorization"), cors.getAllowedHeaders());
        assertEquals("/api/**", cors.getPattern());
        assertEquals(Boolean.TRUE, cors.getAllowCredentials());
        assertEquals(3600L, cors.getMaxAge());
    }

    @Test
    void documentedDefaultsDistinguishPropertyFieldsFromConditionalActivation() throws Exception {
        assertTrue(new ValidatorProperties().isFailFast());
        assertTrue(new ExceptionProperties().isReturnOriginExMsg());
        assertFalse(new XssProperties().isFailedFast());
        assertFalse(new XssProperties().isProcessPayload());
        assertEquals(Integer.MAX_VALUE, new XssProperties().getOrder());
        assertFalse(new DataIntegrityProperties().isCheckReplay());
        assertEquals(Duration.ofMinutes(1), new DataIntegrityProperties().getTimeout());
        assertFalse(new SqlInitProperties().isEnabled());
        assertTrue(SqlInitAutoConfiguration.class.getAnnotation(ConditionalOnProperty.class).matchIfMissing());
        assertFalse(new ParamProcessorProperties().isUseFilter());
        assertTrue(ServletParamProcessorConfiguration.class
                .getMethod("paramProcessorFilterRegistration", cn.hiboot.mcn.autoconfigure.web.filter.special.ParamProcessor.class,
                        ParamProcessorProperties.class).getAnnotation(ConditionalOnProperty.class).matchIfMissing());
        SqlInitProperties sql = bind("sql-init.properties", "spring.sql.init.additional", SqlInitProperties.class);
        assertTrue(sql.isEnabled());
        assertEquals("db", sql.getDir());
        assertEquals("//", sql.getSeparator());
    }

    @Test
    void publishedFilterExampleRejectsMatchingValuesRatherThanAcceptingThem() throws IOException {
        XssProperties xss = bind("filters.properties", "mcn.xss", XssProperties.class);
        assertFalse(xss.isProcessPayload());
        assertEquals(List.of("/api/**"), xss.getIncludeUrls());
        StandardEnvironment environment = environment(properties("filters.properties"));
        ParamProcessorProperties properties = new ParamProcessorProperties();
        String previous = ParamProcessorProperties.globalRule;
        try {
            properties.setEnvironment(environment);
            var processor = new ParamProcessorAutoConfiguration().defaultParamProcessor();
            assertEquals("plain", processor.process("", "name", "plain"));
            assertThrows(ServiceException.class, () -> processor.process("", "name", "<script>"));
            assertThrows(ServiceException.class, () -> processor.process("[a-z]+", "name", "abc"));
        } finally {
            ParamProcessorProperties.globalRule = previous;
        }
    }

    @Test
    void routingExampleRestoresThePreviousDataSourceEvenWhenTheOperationFails() {
        DataSourceHolder.clearDataSource();
        try {
            assertEquals("hello", RoutingExamples.withDataSource("hello", DataSourceHolder::getDataSource));
            assertNull(DataSourceHolder.getDataSource());
            DataSourceHolder.setDataSource("outer");
            assertThrows(IllegalStateException.class, () -> RoutingExamples.withDataSource("world", () -> {
                assertEquals("world", DataSourceHolder.getDataSource());
                throw new IllegalStateException("example failure");
            }));
            assertEquals("outer", DataSourceHolder.getDataSource());
            assertThrows(IllegalArgumentException.class, () -> new MultipleDataSourceConfig(Map.of(), environment(Map.of(
                    "dynamic.datasource.enabled", "true", "jpa.multiple.datasource.enabled", "true"))));
        } finally {
            DataSourceHolder.clearDataSource();
        }
    }

    @Test
    void lockExampleReleasesOnFailureAndDoesNotExecuteWhenAcquisitionFails() {
        class InMemoryLocker implements DistributedLocker {
            boolean acquired = true;
            int releases;
            @Override public boolean tryLock(String key, int wait, int lease, TimeUnit unit) { return acquired; }
            @Override public void unlock(String key) { releases++; }
        }
        InMemoryLocker locker = new InMemoryLocker();
        LockExamples example = new LockExamples(locker);
        assertThrows(IllegalStateException.class, () -> example.execute("test", () -> { throw new IllegalStateException(); }));
        assertEquals(1, locker.releases);
        locker.acquired = false;
        assertThrows(ServiceException.class, () -> example.execute("test", () -> fail("must not execute")));
        assertEquals(1, locker.releases);
        assertNull(locker.tryExecute("test", () -> "not run"));
    }

    @Test
    void integrityExampleUsesSortedParametersAndTheExactRawPayload() {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("b", "2");
        query.put("a", "1");
        query.put("signature", "ignored");
        String expected = SmUtil.sm3("a=1&b=2&timestamp=1000&nonceStr=nonce");
        assertEquals(expected, DataIntegrityUtils.signature("1000", "nonce", query, null, null));
        Map<String, String> headers = IntegrityExample.headers(query, "{\"name\":\"demo\"}");
        assertEquals(DataIntegrityUtils.signature(headers.get("TSM"), headers.get("nonceStr"), query, null,
                "{\"name\":\"demo\"}"), headers.get("signature"));
        assertNotEquals(headers.get("signature"), DataIntegrityUtils.signature(headers.get("TSM"), headers.get("nonceStr"), query,
                null, "{ \"name\": \"demo\" }"));
    }

    @Test
    void sm2ExampleRoundTripsWithGeneratedKeysAndFailsForInvalidCiphertext() {
        var keys = SmUtil.sm2();
        EncryptorProperties.SM2 sm2 = new EncryptorProperties.SM2();
        sm2.setPrivateKey(keys.getPrivateKeyBase64());
        sm2.setPublicKey(keys.getPublicKeyBase64());
        EncryptorProperties properties = new EncryptorProperties();
        properties.setSm2(sm2);
        SM2Encryptor encryptor = new SM2Encryptor(properties);
        EncryptionExample example = new EncryptionExample(encryptor);
        String ciphertext = example.encrypt("demo");
        assertNotEquals("demo", ciphertext);
        assertEquals("demo", encryptor.decrypt(ciphertext));
        assertThrows(RuntimeException.class, () -> encryptor.decrypt("invalid-ciphertext"));
    }

    @Test
    void mcnDefaultsAreLoadedAtLowPriorityAndUseCurrentOpenApiPaths() {
        StandardEnvironment environment = environment(Map.of("logging.level.root", "debug"));
        new McnEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication(ResponseExamples.class));
        assertTrue(environment.getPropertySources().contains("mcn-default"));
        assertTrue(environment.getPropertySources().contains("mcn-map"));
        assertEquals("debug", environment.getProperty("logging.level.root"));
        String paths = environment.getProperty("web.security.default-exclude-urls");
        assertTrue(paths.contains("/v3/api-docs/**"));
        assertFalse(paths.contains("/v2/api-docs"));
        assertEquals("60000", environment.getProperty("spring.cloud.openfeign.client.config.default.read-timeout"));
    }

    private static <T> T bind(String resource, String prefix, Class<T> type) throws IOException {
        return Binder.get(environment(properties(resource))).bind(prefix, Bindable.of(type))
                .orElseThrow(() -> new IllegalStateException("Cannot bind documented example: " + resource));
    }

    private static Map<String, Object> properties(String name) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = DocumentationContractTest.class.getResourceAsStream("/" + name)) {
            assertNotNull(input, name);
            properties.load(input);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        properties.forEach((key, value) -> result.put(key.toString(), value));
        return result;
    }

    private static StandardEnvironment environment(Map<String, ?> values) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("documented-example", new LinkedHashMap<>(values)));
        return environment;
    }
}
