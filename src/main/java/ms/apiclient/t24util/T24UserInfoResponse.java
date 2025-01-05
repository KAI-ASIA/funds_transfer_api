package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class T24UserInfoResponse {
    private String customerId;
    private String customerType;
    private String name;
    private String company;
    private String phone;
    private String email;
    private String mainAccount;
    private String language;
    private String pwDate;
    private String userStatus;
    private ApiError error;
}
