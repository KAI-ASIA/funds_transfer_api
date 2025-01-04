package ms.apiclient.app.service.authen;

import ms.apiclient.client.common.CallApiHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ms.apiclient.app.core.model.ApiHeader;
import ms.apiclient.app.core.model.ApiRequest;

@Component
public class AuthenClient extends ms.apiclient.client.common.CallApiHelper {
	
	@Autowired
	private CallApiHelper CallApiHelper;
	
	public LoginResult login(String location, AuthRequest t24request, ApiHeader header){
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return CallApiHelper.commonRest(location, apiReq, LoginResult.class);
	}

}
