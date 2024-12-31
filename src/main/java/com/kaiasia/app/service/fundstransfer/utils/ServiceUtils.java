package com.kaiasia.app.service.fundstransfer.utils;

import com.kaiasia.app.core.model.*;
import com.kaiasia.app.core.utils.GetErrorUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceUtils {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * Kiểm tra tính hợp lệ của đối tượng yêu cầu dựa trên một kiểu lớp được chỉ định,
     * đảm bảo cấu trúc và nội dung của nó tuân thủ các ràng buộc đã định nghĩa.
     *
     * @param <T>           Kiểu của đối tượng cần kiểm tra.
     * @param req           Đối tượng yêu cầu API cần kiểm tra.
     * @param clazz         Lớp được sử dụng để kiểm tra tính hợp lệ.
     * @param apiErrorUtils Tiện ích để tạo thông báo lỗi.
     * @param transOrEnquiry Phần trong nội dung yêu cầu cần kiểm tra, ví dụ: "TRANSACTION" hoặc "ENQUIRY".
     * @return Trả về đối tượng ApiError, chứa mã lỗi và mô tả lỗi nếu không hợp lệ; ngược lại trả về mã OK nếu hợp lệ.
     */
    public static <T> ApiError validate(ApiRequest req, Class<T> clazz, GetErrorUtils apiErrorUtils, String transOrEnquiry) {
        try {
            ApiBody body = req.getBody();
            if (body == null) {
                return apiErrorUtils.getError("804", new String[]{"Missing request body"});
            }

            if(body.get(transOrEnquiry.toLowerCase()) == null) {
                return apiErrorUtils.getError("804", new String[]{transOrEnquiry + " part is required"});
            }

            T input = ObjectAndJsonUtils.fromObject(body.get(transOrEnquiry.toLowerCase()), clazz);
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

    /**
     * Kiểm tra tính hợp lệ của phản hồi API dựa trên một kiểu lớp được chỉ định,
     * đảm bảo cấu trúc và nội dung của nó tuân thủ các ràng buộc đã định nghĩa.
     *
     * @param <T>           Kiểu của đối tượng cần kiểm tra.
     * @param req           Đối tượng phản hồi API cần kiểm tra.
     * @param clazz         Lớp được sử dụng để kiểm tra tính hợp lệ.
     * @param apiErrorUtils Tiện ích để tạo thông báo lỗi.
     * @param transOrEnquiry Phần trong nội dung phản hồi cần kiểm tra, ví dụ: "TRANSACTION" hoặc "ENQUIRY".
     * @return Trả về đối tượng ApiError, chứa mã lỗi và mô tả lỗi nếu không hợp lệ; ngược lại trả về mã OK nếu hợp lệ.
     */
    public static <T> ApiError validate(ApiResponse req, Class<T> clazz, GetErrorUtils apiErrorUtils, String transOrEnquiry) {
        try {
            ApiBody body = req.getBody();
            if (body == null) {
                return apiErrorUtils.getError("804", new String[]{"Missing response body"});
            }

            if(body.get(transOrEnquiry.toLowerCase()) == null) {
                return apiErrorUtils.getError("804", new String[]{transOrEnquiry + " part is required"});
            }

            T input = ObjectAndJsonUtils.fromObject(body.get(transOrEnquiry.toLowerCase()), clazz);
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
