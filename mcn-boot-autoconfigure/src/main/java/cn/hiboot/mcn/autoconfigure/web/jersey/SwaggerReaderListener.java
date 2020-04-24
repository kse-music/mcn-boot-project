package cn.hiboot.mcn.autoconfigure.web.jersey;

import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.ReaderListener;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

/**
 * global generate header parameter
 *
 * @author DingHao
 * @since 2019/2/14 18:06
 */
public class SwaggerReaderListener implements ReaderListener {

    static final MultiValueMap<String,String> ignoreMethod = new LinkedMultiValueMap<>();

    @Override
    public void beforeScan(Reader reader, Swagger swagger) {

    }

    @Override
    public void afterScan(Reader reader, Swagger swagger) {
        HeaderParameter headerParameter = new HeaderParameter();
        headerParameter.setName("Authorization");
        headerParameter.setType("string");
        headerParameter.setRequired(true);
        Map<String, Path> paths = swagger.getPaths();
        if(paths != null){
            paths.forEach((k,v) -> {
                List<String> method = ignoreMethod.get(k);
                Map<HttpMethod, Operation> operationMap = v.getOperationMap();
                for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                    if(method == null || !method.contains(entry.getKey().toString())){
                        entry.getValue().addParameter(headerParameter);
                    }
                }
            });
        }
    }

}
