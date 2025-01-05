package ms.apiclient.t24util;

import ms.apiclient.client.common.ApiCallClient;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;

public class T24UtilClient extends ApiCallClient {

	public LoginResult login(String location, T24Request t24request, ApiHeader header){
		t24request.setAuthenType("KAI.API.AUTHEN.GET.LOGIN");
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return this.call(location, apiReq, LoginResult.class);
	}

	public CustomerResult getCustomerInfo(String location, T24Request t24request, ApiHeader header){
		t24request.setAuthenType("KAI.API.CUST.GET.INFO");
		ApiRequest apiReq = buildENQUIRY(t24request, header);
		return this.call(location, apiReq, CustomerResult.class);
	}

}
