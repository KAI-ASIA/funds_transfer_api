package ms.apiclient.authen;

import ms.apiclient.client.common.CallApiHelper;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenClient extends ms.apiclient.client.common.CallApiHelper {
	
	@Autowired
	private CallApiHelper CallApiHelper;
	
	public LoginResult login(String location, AuthRequest t24request, ApiHeader header){
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return CallApiHelper.commonRest(location, apiReq, LoginResult.class);
	}

}
