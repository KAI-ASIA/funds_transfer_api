package com.kaiasia.app.service.fundstransfer.utils.napasclient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NapasRequest {
    private String authenType;
    private String senderAccount;
    private String senderName;
    private String accountId;
    private String bankId;
    private String amount;
    private String ccy;
    private String transRef;
    private String benAcc;
    private String transContent;
}
