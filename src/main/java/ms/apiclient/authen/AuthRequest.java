package ms.apiclient.authen;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class AuthRequest {
	private String authenType;
	private String username;
	private String sessionId;
	private String password;
	private String transId;
	private String transInfo;
	private String otp;
	private String transTime;
	private SmsParams smsParams;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SmsParams {
	private String tempId;
	private String content;
}
