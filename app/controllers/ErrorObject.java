package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public class ErrorObject {

    private static int errorIndex = 1;
    static final String USER_UNAUTHORIZED = String.valueOf(errorIndex++);
    static final String DUPLICATE_RECIPE = String.valueOf(errorIndex++);
    static final String DUPLICATE_INGREDIENT = String.valueOf(errorIndex++);
    static final String DUPLICATE_TAG = String.valueOf(errorIndex++);
    static final String DUPLICATE_REVIEW = String.valueOf(errorIndex++);
    static final String DUPLICATE_USER = String.valueOf(errorIndex++);
    static final String UPDATE_UNAUTHORIZED = String.valueOf(errorIndex++);
    static final String DELETE_UNAUTHORIZED = String.valueOf(errorIndex);

    private String code;
    private String message;

    ErrorObject(String code, String message) {
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
