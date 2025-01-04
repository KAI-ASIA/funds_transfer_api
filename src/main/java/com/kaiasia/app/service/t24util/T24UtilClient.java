package com.kaiasia.app.service.t24util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kaiasia.app.core.model.ApiHeader;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.client.t24util.common.CallApiHelper;


public class T24UtilClient extends CallApiHelper{
	
	@Autowired
	private CallApiHelper CallApiHelper;
	
	
	
	public LoginResult login(String location, T24Request t24request, ApiHeader header){
		t24request.setAuthenType("KAI.API.AUTHEN.GET.LOGIN");
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return CallApiHelper.commonRest(location, apiReq, LoginResult.class);
	}

	
	
	public CustomerResult getCustomerInfo(String location, T24Request t24request, ApiHeader header){
		t24request.setAuthenType("KAI.API.CUST.GET.INFO");
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return CallApiHelper.commonRest(location, apiReq, CustomerResult.class);
	}

}
