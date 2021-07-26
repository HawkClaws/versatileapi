package com.flex.versatileapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.service.AuthenticationService;

@SpringBootTest
public class AuthenticationServiceTest {

	@Autowired
	private AuthenticationService authenticationService;

	@Test
	void createUserTest() throws ODataParseException {

		String userId = "tanaka";
		String password = "tanakanopassword";
		String ipAddress = "192.168.0.1";
		String authGroupKey ="authGroupKey";

		authenticationService.createUser(userId, password, ipAddress);
		authenticationService.addAuthGroup(authGroupKey, userId, ipAddress);
		
		boolean aaa =authenticationService.isAllowedAuth(userId, password, authGroupKey);
		int i=0;

	}
}
