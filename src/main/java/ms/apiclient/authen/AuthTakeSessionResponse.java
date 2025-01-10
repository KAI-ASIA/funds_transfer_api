package ms.apiclient.authen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AuthTakeSessionResponse {
    private String responseCode;
    private String sessionId;
    private String username;
    private ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
}
