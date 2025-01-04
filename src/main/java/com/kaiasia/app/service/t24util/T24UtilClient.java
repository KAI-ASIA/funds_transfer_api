package com.kaiasia.app.service.t24util;

import com.kaiasia.app.core.model.ApiHeader;
import com.kaiasia.app.core.model.ApiRequest;
import com.kaiasia.client.common.CallApiHelper;

public class T24UtilClient extends CallApiHelper{

	public LoginResult login(String location, T24Request t24request, ApiHeader header){
		t24request.setAuthenType("KAI.API.AUTHEN.GET.LOGIN");
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return this.commonRest(location, apiReq, LoginResult.class);
	}

	public CustomerResult getCustomerInfo(String location, T24Request t24request, ApiHeader header){
		t24request.setAuthenType("KAI.API.CUST.GET.INFO");
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return this.commonRest(location, apiReq, CustomerResult.class);
	}

}
