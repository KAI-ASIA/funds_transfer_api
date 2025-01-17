package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class T24BankListResponse {
    private List<Bank> banks;
    private ApiError error = new ApiError(ApiError.OK_CODE, ApiError.OK_DESC);
}
