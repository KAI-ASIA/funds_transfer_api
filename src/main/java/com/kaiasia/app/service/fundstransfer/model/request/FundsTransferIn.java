package com.kaiasia.app.service.fundstransfer.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaiasia.app.service.fundstransfer.model.validation.FundsTransferOptional;
import com.kaiasia.app.service.fundstransfer.model.validation.FundsTransferRequired;
import lombok.*;
import javax.validation.constraints.*;

/**
 * Class này dùng để định nghĩa dữ liệu cần gửi tới FundsTransfer và cũng có thể gửi tới T2405
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundsTransferIn {

    @NotBlank(message = "Authentication type is required", groups = FundsTransferOptional.class)
    private String authenType;

    @NotBlank(message = "Session ID is required", groups = FundsTransferOptional.class)
    private String sessionId;

    @NotBlank(message = "Customer ID is required", groups = FundsTransferOptional.class)
    private String customerID;

    @NotBlank(message = "Company is required", groups = FundsTransferOptional.class)
    private String company;

    @NotBlank(message = "OTP is required", groups = FundsTransferOptional.class)
    @JsonProperty("OTP")
    private String otp;

    @NotBlank(message = "Transaction ID is required", groups = FundsTransferOptional.class)
    private String transactionId;

    @NotBlank(message = "Debit account is required", groups = FundsTransferOptional.class)
    private String debitAccount;

    @NotBlank(message = "Credit account is required", groups = FundsTransferOptional.class)
    private String creditAccount;

    @NotBlank(message = "Bank ID is required", groups = FundsTransferRequired.class)
    private String bankId;

    @NotNull(message = "Transaction amount is required", groups = FundsTransferOptional.class)
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Transaction amount must be a valid number", groups = FundsTransferOptional.class)
    private String transAmount;

    @NotBlank(message = "Transaction description is required", groups = FundsTransferOptional.class)
    private String transDesc;
}
