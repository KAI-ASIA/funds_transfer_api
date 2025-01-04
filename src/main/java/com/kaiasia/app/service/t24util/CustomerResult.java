package com.kaiasia.app.service.t24util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kaiasia.app.core.model.ApiError;

import lombok.Getter;
import lombok.Setter;

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
