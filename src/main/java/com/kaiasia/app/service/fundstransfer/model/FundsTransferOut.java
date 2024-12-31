package com.kaiasia.app.service.fundstransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * Class này dùng để định nghĩa dữ liệu trả ra từ FundsTransfer và cũng có thể trả ra từ T2405
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundsTransferOut {
    @NotBlank(message = "Response code is required")
    private String responseCode;

    @NotBlank(message = "FT is required")
    @JsonProperty("FT")
    private String transactionNO;

    private String napasRef;
}
