package cn.hiboot.mcn.autoconfigure.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class JwtToken {

    private static JwtProperties jwtProperties;

    private HttpServletRequest request;
    private HttpServletResponse response;

    public JwtToken(JwtProperties jwtProperties){
        JwtToken.jwtProperties = jwtProperties;
    }

    public JwtToken(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public String createToken(Object identifier) {
        Map<String,Object> data = new HashMap<>();
        data.put("userId",identifier);
        return createToken(data);
    }

    public String createToken(Map<String,Object> data) {
        if(Objects.isNull(data) || data.isEmpty())return null;
        //签发时间
        Date iaDate = new Date();

        //过期时间
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.SECOND, jwtProperties.getExpireDate());
        Date expireDate = nowTime.getTime();

        Map<String,Object> map = new HashMap<>();
        map.put("alg","HS256");
        map.put("typ","JWT");

        JWTCreator.Builder builder = JWT.create()
                .withHeader(map)
                .withExpiresAt(expireDate)
                .withIssuedAt(iaDate)
                .withIssuer(jwtProperties.getIssuer());
        convertDataToActualType(data,builder);
        String token = builder.sign(getAlgorithm());
        returnToken(token);
        return token;
    }

    private void convertDataToActualType(Map<String,Object> data,JWTCreator.Builder builder){
        //Integer Double Date Boolean String
        data.forEach((k,v) -> {
            if(v instanceof Integer){
                builder.withClaim(k,((Integer) v).intValue());
            }else if(v instanceof Double){
                builder.withClaim(k,((Double) v).doubleValue());
            }else if(v instanceof Date){
                builder.withClaim(k,(Date)v);
            }else if(v instanceof Boolean){
                builder.withClaim(k,((Boolean) v).booleanValue());
            }else {
                builder.withClaim(k,v.toString());
            }
        });
    }

    private String checkIsCreateNewToken(DecodedJWT jwt) {
        Date issuedAt = jwt.getIssuedAt();
        if(System.currentTimeMillis() - issuedAt.getTime() > jwtProperties.getRefreshInterval()*jwtProperties.getUnit()){
            Map<String, Claim> claims = jwt.getClaims();
            Map<String,Object> data = new HashMap<>();
            claims.forEach((k,v) -> {
                if(!"iat".equals(k) && !"exp".equals(k) && !"iss".equals(k)){
                    data.put(k,v.as(Object.class));
                }
            });
            return createToken(data);
        }
        return null;
    }

    private Algorithm getAlgorithm(){
        try {
            return Algorithm.HMAC256(jwtProperties.getSecretKey());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("The Secret Character Encoding is not supported");
        }
    }


    public DecodedJWT checkToken(String token){
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        DecodedJWT jwt = verifier.verify(token);

        //通过之后，检查是否要返回新token
        checkIsCreateNewToken(jwt);
        return jwt;
    }

    public Integer getUserIdAsInt() {
        return getValueAsInt("userId");
    }
    public String getUserIdAsString() {
        return getValueByKey("userId");
    }
    public Integer getValueAsInt(String key) {
        return getValueByKey(key,Integer.class);
    }
    public Long getValueAsLong(String key) {
        return getValueByKey(key,Long.class);
    }
    public Double getValueAsDouble(String key) {
        return getValueByKey(key,Double.class);
    }
    public Boolean getValueAsBoolean(String key) {
        return getValueByKey(key,Boolean.class);
    }

    public String getValueByKey(String key) {
        return getValueByKey(key,String.class);
    }

    private <T> T getValueByKey(String key,Class<T> cls) {
        return getClaim(key).as(cls);
    }

    private Claim getClaim(String key) {
        return checkToken(getToken()).getClaim(key);
    }

    public String getToken(){
        HttpServletRequest request = getRequest();
        if(request == null){
            return null;
        }
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isBlank(authorization)){
            return null;
        }
        String[] str = authorization.split(" ");
        return str.length == 2?str[1]:null;
    }

    private HttpServletRequest getRequest(){
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if(Objects.nonNull(attributes)){
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            if(Objects.nonNull(request)){
                this.request = request;
            }
        }
        return this.request;
    }

    private HttpServletResponse getResponse(){
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if(Objects.nonNull(attributes)){
            HttpServletResponse response = ((ServletRequestAttributes) attributes).getResponse();
            if(Objects.nonNull(response)){
                this.response = response;
            }
        }
        return this.response;
    }

    private void returnToken(String token){
        HttpServletResponse response = getResponse();
        if(response != null){
            response.setHeader("Authorization",token);
        }
    }

}
