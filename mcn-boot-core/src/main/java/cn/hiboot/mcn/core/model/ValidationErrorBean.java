package cn.hiboot.mcn.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorBean {

    private String message;

    private String messageTemplate;

    private String path;

    private String invalidValue;

    public ValidationErrorBean(String message, String path, String invalidValue) {
        this.message = message;
        this.path = path;
        this.invalidValue = invalidValue;
    }

    public ValidationErrorBean(String message, String messageTemplate, String path, String invalidValue) {
        this.message = message;
        this.messageTemplate = messageTemplate;
        this.path = path;
        this.invalidValue = invalidValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }
}
