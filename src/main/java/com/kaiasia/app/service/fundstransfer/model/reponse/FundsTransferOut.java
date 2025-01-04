package com.kaiasia.app.service.fundstransfer.model.reponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * Class này dùng để định nghĩa dữ liệu trả ra từ FundsTransfer và cũng có thể trả ra từ T2405
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FundsTransferOut {
    @NotBlank(message = "Response code is required")
    private String responseCode;

    @NotBlank(message = "FT is required")
    @JsonProperty("FT")
    private String transactionNO;

    private String napasRef;

    private String code;
    private String desc;
}
