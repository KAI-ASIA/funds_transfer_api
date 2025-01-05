package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class T24CustomerInfoResponse {
    private String id;
    private String cifName;
    private String legalId;
    private String cifStatus;
    private String language;
    private String coCode;
    private String phone;
    private String email;
    private String country;
    private String address;
    private String legalDocName;
    private String legalExpDate;
    private String customerType;
    private ApiError error;
}
