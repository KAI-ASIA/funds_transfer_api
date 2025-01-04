package com.kaiasia.app.service.fundstransfer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kaiasia.app.service.fundstransfer.model.validation.SuccessGroup;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * Class này dùng để định nghĩa dữ liệu trả ra từ Auth-1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth1Out extends BaseResponse {
    @NotBlank(message = "SessionId is required", groups = SuccessGroup.class)
    private String sessionId;

    @NotBlank(message = "Username is required", groups = SuccessGroup.class)
    private String username;

}
