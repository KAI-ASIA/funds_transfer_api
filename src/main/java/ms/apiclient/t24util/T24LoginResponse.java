package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class T24LoginResponse {
    private String packageUser;
    private String phone;
    private String customerID;
    private String customerName;
    private String companyCode;
    private String username;
    private boolean firstLogin;
    private ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
}
