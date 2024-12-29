package com.kaiasia.app.service.fundstransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import javax.validation.constraints.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundsTransferIn {

    @NotBlank(message = "Authentication type is required")
    private String authenType;

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "Customer ID is required")
    private String customerID;

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "OTP is required")
    private String OTP;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Debit account is required")
    private String debitAccount;

    @NotBlank(message = "Credit account is required")
    private String creditAccount;

    @NotBlank(message = "Bank ID is required")
    private String bankId;

    @NotNull(message = "Transaction amount is required")
    @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Transaction amount must be a valid number")
    private String transAmount;

    @NotBlank(message = "Transaction description is required")
    private String transDesc;
}
