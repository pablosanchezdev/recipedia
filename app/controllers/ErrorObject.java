package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public class ErrorObject {

    private String code;
    private String message;

    public ErrorObject(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonNode toJson() {
        return Json.toJson(this);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
