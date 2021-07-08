package cn.hiboot.mcn.autoconfigure.web.jersey;

import cn.hiboot.mcn.autoconfigure.web.exception.handler.AbstractExceptionHandler;
import cn.hiboot.mcn.core.exception.BaseException;
import cn.hiboot.mcn.core.model.ValidationErrorBean;
import cn.hiboot.mcn.core.model.result.RestResp;
import org.glassfish.jersey.server.ParamException;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationErrorData;
import org.glassfish.jersey.server.validation.internal.ValidationHelper;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.List;

public class GlobalExceptionHandler extends AbstractExceptionHandler implements ExceptionMapper<Exception> {

    @Context
    private Configuration config;

    @Override
    public Response toResponse(Exception e) {
        Response.Status statusCode = Response.Status.OK;
        RestResp<Object> rs;
        if(e instanceof BaseException){
            rs = buildErrorMessage(((BaseException)e).getCode(),((BaseException)e).getMsg());
        }else if(e instanceof ParamException || e instanceof ValidationException){
            rs = parseParamException(e);
        }else if(e instanceof WebApplicationException){
            if (e instanceof NotFoundException) {
                statusCode = Response.Status.NOT_FOUND;
                rs = buildErrorMessage(HTTP_ERROR_404);
            } else if (e instanceof NotAllowedException) {
                statusCode = Response.Status.METHOD_NOT_ALLOWED;
                rs = buildErrorMessage(HTTP_ERROR_405);
            } else if (e instanceof NotAcceptableException) {
                statusCode = Response.Status.NOT_ACCEPTABLE;
                rs = buildErrorMessage(HTTP_ERROR_406);
            } else {
                statusCode = Response.Status.INTERNAL_SERVER_ERROR;
                rs = buildErrorMessage(HTTP_ERROR_500);
            }
        }else {
            rs = buildErrorMessage(SERVICE_ERROR);
        }
        dealStackTraceElement(e);
        logger.error("ErrorMsg = {}",rs.getErrorInfo(),e);
        return Response.ok(rs).status(statusCode).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private RestResp<Object> parseParamException(Exception e) {
        RestResp<Object> rs = buildErrorMessage(PARAM_PARSE_ERROR);
        if(e instanceof ConstraintViolationException){
            ConstraintViolationException cve = (ConstraintViolationException) e;
            Response.ResponseBuilder response = Response.status(ValidationHelper.getResponseStatus(cve));

            Object property = config.getProperty(ServerProperties.BV_SEND_ERROR_IN_RESPONSE);

            if (property != null && Boolean.parseBoolean(property.toString())) {
                response.type(MediaType.APPLICATION_JSON_TYPE);
                List<ValidationErrorData> errors = ValidationHelper.constraintViolationToValidationErrors(cve);
                rs.setData(getValidationError(errors));
            }
        }
        return rs;
    }


    private List<ValidationErrorBean> getValidationError(List<ValidationErrorData> errors){
        List<ValidationErrorBean> list = new ArrayList<>();
        for (ValidationErrorData error : errors) {
            list.add(new ValidationErrorBean(error.getMessage(),error.getPath(),error.getInvalidValue()));
        }
        return list;
    }

}
