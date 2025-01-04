package com.kaiasia.app.service.authen;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AuthRequest {
	private String authenType;
	private String username;
	private String sessionId;
	private String password;

}
