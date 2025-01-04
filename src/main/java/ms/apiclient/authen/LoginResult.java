package ms.apiclient.authen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class LoginResult {
	private String packageUser;
	private String phone;
	private ApiError error;
	/*
	 * todo
	 */

}
