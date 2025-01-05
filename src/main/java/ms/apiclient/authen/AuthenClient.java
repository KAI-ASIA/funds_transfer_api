package ms.apiclient.authen;

import ms.apiclient.client.common.ApiCallClient;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;

import org.springframework.stereotype.Component;

@Component
public class AuthenClient extends ApiCallClient {
	/**
	 * Phương thức này dùng để gọi đến Auth-0
	 * @param location - Vị trí truy vết để ghi log.
	 * @param authRequest - Data cần truyền vào (Username, Password, LoginTime).
	 * @param header - Header của request đầu vào.
	 * @return Trả về AuthLoginResponse chứa (ResponseCode, SessionId, Username, TransId, PackageUser, Phone, CustomerID, CustomerName, CompanyName) hoặc 1 ApiError
	 */
	public AuthLoginResponse getLogin(String location, AuthRequest authRequest, ApiHeader header){
		authRequest.setAuthenType("getLogin");
		ApiRequest apiReq = buildENQUIRY(authRequest, header);
		return this.call(location, apiReq, AuthLoginResponse.class);
	}

	/**
	 * Phương thức này dùng để gọi đến Auth-1
	 * @param location - Vị trí truy vết để ghi log.
	 * @param authRequest - Data cần truyền vào (Chỉ cần sessionId).
	 * @param header - Header của request đầu vào.
	 * @return Trả về AuthLoginResponse chứa (ResponseCode, SessionId, Username) hoặc 1 ApiError
	 */
	public AuthLoginResponse takeSession(String location, AuthRequest authRequest, ApiHeader header){
		authRequest.setAuthenType("takeSession");
		ApiRequest apiReq = buildENQUIRY(authRequest, header);
		return this.call(location, apiReq, AuthLoginResponse.class);
	}

	/**
	 * Phương thức này dùng để gọi đến Auth-2
	 * @param location - Vị trí truy vết để ghi log.
	 * @param authRequest - Data cần truyền vào (Username, SessionId, Gmail, TransTime, TransId, TransInfo, SmsParams).
	 * @param header - Header của request đầu vào.
	 * @return Trả về AuthLoginResponse chứa (ResponseCode, TransId) hoặc 1 ApiError
	 */
	public AuthLoginResponse getOTP(String location, AuthRequest authRequest, ApiHeader header){
		authRequest.setAuthenType("getOTP");
		ApiRequest apiReq = buildENQUIRY(authRequest, header);
		return this.call(location, apiReq, AuthLoginResponse.class);
	}


	/**
	 * Phương thức này dùng để gọi đến Auth-3
	 * @param location - Vị trí truy vết để ghi log.
	 * @param authRequest - Data cần truyền vào (Username, SessionId, OTP, TransTime, TransId).
	 * @param header - Header của request đầu vào.
	 * @return Trả về AuthLoginResponse chứa (ResponseCode, TransId) hoặc 1 ApiError
	 */
	public AuthLoginResponse confirmOTP(String location, AuthRequest authRequest, ApiHeader header){
		authRequest.setAuthenType("confirmOTP");
		ApiRequest apiReq = buildENQUIRY(authRequest, header);
		return this.call(location, apiReq, AuthLoginResponse.class);
	}
}
