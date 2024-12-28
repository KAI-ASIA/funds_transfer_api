package com.kaiasia.app.service.fundstransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundsTransferIn {
    private String authenType;
    private String sessionId;
    private String customerID;
    private String company;
    private String OTP;
    private String transactionId;
    private String debitAccount;
    private String creditAccount;
    private String bankId;
    private String transAmount;
    private String transDesc;
}
