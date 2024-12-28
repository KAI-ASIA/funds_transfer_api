package com.kaiasia.app.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse {
    private ApiHeader header;
    private ApiBody body;
    private ApiError error;

    /**
     * Thiết lập thông tin lỗi cho phản hồi.
     * Nếu lỗi không null và mã lỗi không phải mã OK, nội dung body được khởi tạo với trạng thái "FAILE".
     *
     * @param error Thông tin lỗi cần thiết lập.
     */
    public void setError(ApiError error) {
        this.error = error;
        if (error != null && !ApiError.OK_CODE.equals(error.getCode())) {
            this.body = new ApiBody();
            this.body.put("status", "FAILE");
        }
    }
}