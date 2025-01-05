package ms.apiclient.authen;

import ms.apiclient.client.common.ApiCallClient;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenClient extends ApiCallClient {
	
	@Autowired
	private ApiCallClient CallApiHelper;
	
	public LoginResult login(String location, AuthRequest t24request, ApiHeader header){
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return this.call(location, apiReq, LoginResult.class);
	}

}
