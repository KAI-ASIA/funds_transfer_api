package ms.apiclient.t24util;

import lombok.Setter;
import ms.apiclient.client.common.ApiCallClient;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.model.ApiRequest;
import org.springframework.web.client.RestClientException;

@Setter
public class T24UtilClient extends ApiCallClient {

    public T24LoginResponse login(String location, T24Request t24request, ApiHeader header) throws RestClientException {
        t24request.setAuthenType("KAI.API.AUTHEN.GET.LOGIN");
        ApiRequest apiReq = buildENQUIRY(t24request, header);
        return this.call(location, apiReq, T24LoginResponse.class);
    }

    public T24CustomerInfoResponse getCustomerInfo(String location, T24Request t24request, ApiHeader header) throws RestClientException {
        t24request.setAuthenType("KAI.API.CUST.GET.INFO");
        ApiRequest apiReq = buildENQUIRY(t24request, header);
        return this.call(location, apiReq, T24CustomerInfoResponse.class);
    }

    public T24CustomerAccountResponse getCustomerAccount(String location, T24Request t24request, ApiHeader header) throws RestClientException {
        t24request.setAuthenType("KAI.API.CUSTOMER.GET.ACC");
        ApiRequest apiReq = buildENQUIRY(t24request, header);
        return this.call(location, apiReq, T24CustomerAccountResponse.class);
    }

    public T24AccountInfoResponse getAccountInfo(String location, T24Request t24request, ApiHeader header) throws RestClientException {
        t24request.setAuthenType("KAI.API.ACCOUNT.GET.INFO");
        ApiRequest apiReq = buildENQUIRY(t24request, header);
        return this.call(location, apiReq, T24AccountInfoResponse.class);
    }

    public T24UserInfoResponse getUserInfo(String location, T24Request t24request, ApiHeader header) throws RestClientException {
        t24request.setAuthenType("KAI.API.USER.GET.INFO");
        ApiRequest apiReq = buildENQUIRY(t24request, header);
        return this.call(location, apiReq, T24UserInfoResponse.class);
    }

    public T24FundTransferResponse fundTransfer(String location, T24Request t24request, ApiHeader header) throws RestClientException {
        t24request.setAuthenType("KAI.API.FT.PROCESS");
        ApiRequest apiReq = buildTransaction(t24request, header);
        return this.call(location, apiReq, T24FundTransferResponse.class);
    }

}
