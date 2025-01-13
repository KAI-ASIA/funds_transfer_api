package com.kaiasia.app.service.fundstransfer.exception;

public abstract class ApiErrorCode {
    public static String ERROR_INSERT_REQUEST_TO_DB = "996";
    public static String CANNOT_PROCESSED = "997";
    public static String TIMEOUT = "998";
    public static String INTERNAL_ERROR = "999";
    public static String INVALID_REQUEST_TYPE = "801";
    public static String CALCULATE_TIMEOUT_ERROR = "802";
    public static String REQUEST_ID_NOT_EXIST = "803";
    public static String REQUEST_LACK_VALID_INFORMATION = "804";
    public static String API_KEY_NOT_EXIST = "700";
    public static String API_NOT_EXIST = "701";
    public static String FAILED_TO_INSERT_TO_DB = "501";
    public static String FAILED_TO_UPDATE_TO_DB = "502";
    public static String FAILED_TO_READ_FROM_DB = "503";
    public static String FAILED_TO_DELETE_FROM_DB = "504";
    public static String FAILED_TO_CALL_API = "505";

}
