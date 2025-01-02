package com.kaiasia.app.service.fundstransfer.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Napas2In {
    private String authenType;
    private String senderAccount;
    private String amount;
    private String ccy;
    private String transRef;
    private String benAcc;
    private String bankId;
    private String transContent;
}
