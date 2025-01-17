package com.kaiasia.app.service.fundstransfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInfo {
    private String transactionId;
    private String customerId;
    private String approvalMethod;
    private String otp;
    private String responseCode;
    private String responseStr;
    private String status;
    private String bankTransId;
    private Date insertTime;
    private Date lastUpdate;
    private String creditAccount;
    private String debitAccount;
    private String amount;
    private String bankCode;
}
