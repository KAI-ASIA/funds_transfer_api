package ms.apiclient.authen;

import ms.apiclient.client.common.ApiCallClient;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;

import org.springframework.stereotype.Component;

@Component
public class AuthenClient extends ApiCallClient {
	
	public LoginResult login(String location, AuthRequest t24request, ApiHeader header){
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return this.call(location, apiReq, LoginResult.class);
	}

}
