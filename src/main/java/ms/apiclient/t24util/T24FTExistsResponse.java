package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class T24FTExistsResponse {
    private String transactionNO;
    private String responseCode;
    private ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
}
