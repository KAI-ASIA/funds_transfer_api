package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bank {
    private String bankCode;
    private String bankName;
    private String status;
    private String napasId;
}
