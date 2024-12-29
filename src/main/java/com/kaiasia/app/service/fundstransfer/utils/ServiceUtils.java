package com.kaiasia.app.service.fundstransfer.utils;

import com.kaiasia.app.core.model.ApiBody;
import com.kaiasia.app.core.model.ApiError;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.app.core.utils.GetErrorUtils;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceUtils {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> ApiError validate(ApiRequest req, Class<T> clazz, GetErrorUtils apiErrorUtils) {
        try {
            ApiBody body = req.getBody();
            if (body == null) {
                return apiErrorUtils.getError("804", new String[]{"Missing request body"});
            }

            if(body.get("transaction") == null) {
                return apiErrorUtils.getError("804", new String[]{"Transaction part is required"});
            }
            System.out.println(body.get("transaction"));

            T input = ObjectAndJsonUtils.fromObject(body.get("transaction"), clazz);
            Set<ConstraintViolation<T>> violations = validator.validate(input);

            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                                                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                                                .collect(Collectors.joining(", "));
                return apiErrorUtils.getError("804", new String[]{"Validation failed: " + errorMessage});
            }

            return new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
        } catch (IllegalArgumentException e) {
            return apiErrorUtils.getError("600", new String[]{"Invalid request body format"});
        }
    }
}
