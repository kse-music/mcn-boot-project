package cn.hiboot.mcn.autoconfigure.jwt;

import cn.hiboot.mcn.autoconfigure.web.filter.JwtAuthenticationTokenFilter;
import cn.hiboot.mcn.autoconfigure.web.jersey.JerseySwaggerProperties;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ AbstractSecurityWebApplicationInitializer.class, SessionCreationPolicy.class,JWT.class })
@ConditionalOnProperty(prefix = "jwt.security", name = "login", havingValue = "true")
@EnableConfigurationProperties(JwtProperties.class)
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class WebSecurityAutoConfiguration extends WebSecurityConfigurerAdapter {

    private Environment environment;
    private JwtProperties jwtProperties;
    private JerseySwaggerProperties jerseySwaggerProperties;

    public WebSecurityAutoConfiguration(Environment environment, JwtProperties jwtProperties,JerseySwaggerProperties jerseySwaggerProperties) {
        this.environment = environment;
        this.jwtProperties = jwtProperties;
        this.jerseySwaggerProperties = jerseySwaggerProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public JerseySwaggerProperties jerseySwaggerProperties(){
        return new JerseySwaggerProperties();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        boolean filterCross = environment.getProperty("filter.cross", Boolean.class, true);
        if(filterCross){
            http.cors();
        }
        http
            .csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint())
        .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .anyRequest().authenticated()
        .and()
            .addFilterBefore(new JwtAuthenticationTokenFilter(authenticationEntryPoint()),UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) {
        String basePath = parsePath(jerseySwaggerProperties.getBasePath());
        List<String> defaultIgnoreUrls = new ArrayList<>(Arrays.asList(basePath+"swagger.json",basePath+"Swagger.html",basePath+"Swagger/**","/actuator/**"));
        List<String> ignoreUrls = jwtProperties.getSecurity().getIgnoreUrls();
        if(Objects.nonNull(ignoreUrls) && !ignoreUrls.isEmpty()){
            for (String ignoreUrl : ignoreUrls) {
                defaultIgnoreUrls.add(basePath+ignoreUrl);
            }
        }
        web.ignoring().antMatchers(defaultIgnoreUrls.toArray(new String[defaultIgnoreUrls.size()]));
    }

    private String parsePath(String path) {
        path = path.startsWith("/") ? path : "/" + path;
        path = path.endsWith("/") ? path : path + "/";
        return path;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return new JwtAuthenticationEntryPoint(new ObjectMapper());
    }

}