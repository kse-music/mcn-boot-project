package cn.hiboot.mcn.autoconfigure.web.jersey;

import cn.hiboot.mcn.core.model.result.RestResp;
import cn.hiboot.mcn.core.util.McnUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.mvc.Template;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Singleton
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class SwaggerView {

    @Autowired
    private JerseySwaggerProperties jerseySwaggerProperties;

    @Path("swagger-ui.html")
    @GET
    @Template(name = "/index")
    public Map<String, Object> indexView() {
        Map<String, Object> map = new HashMap();
        String host = this.jerseySwaggerProperties.getHost();
        map.put("host", Objects.nonNull(host) ? host : this.jerseySwaggerProperties.getIp() + ":" + this.jerseySwaggerProperties.getPort());
        String path = McnUtils.dealUrlPath(jerseySwaggerProperties.getBasePath());
        map.put("path", path);
        String cdn = this.jerseySwaggerProperties.getCdn();
        map.put("cdn", Objects.nonNull(cdn) ? cdn : "/Swagger/");
        return map;
    }

    @POST
    @Path("h/lic")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public RestResp upload(@FormDataParam("file") InputStream fileIn,
                           @FormDataParam("file") FormDataContentDisposition fileInfo) {
        Properties prop = McnUtils.loadProperties("lic-verify.properties","kgLicence");
        File f = new File(prop.getProperty("licPath"));
        if(f.exists()){
            f.delete();
        }
        try {
            McnUtils.copyFile(fileIn,prop.getProperty("licPath"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RestResp<>();
    }


}
