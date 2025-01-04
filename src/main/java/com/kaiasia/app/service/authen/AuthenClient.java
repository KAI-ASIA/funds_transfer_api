package com.kaiasia.app.service.authen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaiasia.app.core.model.ApiHeader;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.client.t24util.common.CallApiHelper;


public class AuthenClient extends CallApiHelper{
	
	@Autowired
	private CallApiHelper CallApiHelper;
	
	
	
	public LoginResult login(String location, AuthRequest t24request, ApiHeader header){
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return CallApiHelper.commonRest(location, apiReq, LoginResult.class);
	}

}
