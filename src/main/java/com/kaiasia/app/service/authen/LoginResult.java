package com.kaiasia.app.service.authen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kaiasia.app.core.model.ApiError;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class LoginResult {
	private String packageUser;
	private String phone;
	private ApiError error;
	/*
	 * todo
	 */

}
