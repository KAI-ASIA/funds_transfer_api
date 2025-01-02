package com.kaiasia.app.service.fundstransfer.model.reponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * Class này dùng để định nghĩa dữ liệu trả ra từ Auth-3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth3Out {
    @NotBlank(message = "Response code is required")
    private String responseCode;

    @NotBlank(message = "TransactionId is required")
    private String transId;
}
