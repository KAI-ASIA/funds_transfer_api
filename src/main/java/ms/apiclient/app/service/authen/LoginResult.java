package ms.apiclient.app.service.authen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ms.apiclient.app.core.model.ApiError;

import lombok.Getter;
import lombok.Setter;

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
