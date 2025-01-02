package com.kaiasia.app.service.fundstransfer.model.reponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class Auth1Out {
    @NotBlank(message = "Response code is required")
    private String responseCode;

    @NotBlank(message = "SessionId is required")
    private String sessionId;

    @NotBlank(message = "Username is required")
    private String username;
}
