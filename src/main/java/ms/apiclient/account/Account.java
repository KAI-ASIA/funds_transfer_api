package ms.apiclient.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private String customerId;
    private String accountType;
    private String shortName;
    private String currency;
    private String accountId;
    private String altAccount;
    private String company;
    private String accountStatus;
    private String shortTitle;
    private ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
}
