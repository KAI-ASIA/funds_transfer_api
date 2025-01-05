package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class T24AccountInfoResponse {
    private String customerId;
    private String accountType;
    private String shortName;
    private String currency;
    private String accountId;
    private String altAccount;
    private String category;
    private String company;
    private String productCode;
    private String accountStatus;
    private String shortTitle;
    private String avaiBalance;
    private ApiError error;
}
