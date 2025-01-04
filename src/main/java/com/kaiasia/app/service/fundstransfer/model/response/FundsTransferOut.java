package com.kaiasia.app.service.fundstransfer.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * Class này dùng để định nghĩa dữ liệu trả ra từ FundsTransfer và cũng có thể trả ra từ T2405
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FundsTransferOut extends BaseResponse{
    @NotBlank(message = "FT is required")
    @JsonProperty("FT")
    private String transactionNO;

    private String napasRef;
}
