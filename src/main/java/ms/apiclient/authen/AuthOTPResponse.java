package ms.apiclient.authen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AuthOTPResponse {
    private String responseCode;
    private String transId;
    private ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
}
