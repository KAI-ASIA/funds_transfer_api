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
    public String responseCode;
    public String responseStr;
    public String status;
    public String bankTransId;
    public Date insertTime;
    public Date lastUpdate;
}
