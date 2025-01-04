package ms.apiclient.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import ms.apiclient.model.ApiError;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CustomerResult {
	private String id;
	private String cifName;
	private String legalId;
	private ApiError error;
	/*
	 * todo
	 */

}
