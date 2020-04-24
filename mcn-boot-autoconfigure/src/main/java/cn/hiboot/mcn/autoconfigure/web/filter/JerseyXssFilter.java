package cn.hiboot.mcn.autoconfigure.web.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@PreMatching
public class JerseyXssFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) {
        cleanQueryParams(request);
        cleanHeaders(request.getHeaders());
    }

    private void cleanQueryParams(ContainerRequestContext request) {
        UriBuilder builder = request.getUriInfo().getRequestUriBuilder();
        MultivaluedMap<String, String> queries = request.getUriInfo().getQueryParameters();

        for (Map.Entry<String, List<String>> query : queries.entrySet()) {
            String key = query.getKey();
            List<String> values = query.getValue();

            List<String> xssValues = new ArrayList<>();
            for (String value : values) {
                xssValues.add(JsoupUtil.clean(value));
            }

            Integer size = xssValues.size();
            builder.replaceQueryParam(key);

            if (size == 1) {
                builder.replaceQueryParam(key, xssValues.get(0));
            } else if (size > 1) {
                builder.replaceQueryParam(key, xssValues.toArray());
            }
        }

        request.setRequestUri(builder.build());
    }
    private void cleanHeaders(MultivaluedMap<String, String> headers) {
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            String key = header.getKey();
            List<String> values = header.getValue();

            List<String> cleanValues = new ArrayList<>();
            for (String value : values) {
                cleanValues.add(JsoupUtil.clean(value));
            }
            headers.put(key, cleanValues);
        }
    }

}