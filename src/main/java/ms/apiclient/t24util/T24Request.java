package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class T24Request {
    private String authenType;
    private String username;
    private String password;
    private String customerId;
    private String accountId;
    private String transactionId;
    private String debitAccount;
    private String creditAccount;
    private String bankId;
    private String transAmount;
    private String transDesc;
    private String company;
    private String channel;
}
