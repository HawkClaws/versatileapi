package com.flex.versatileapi.repository;

import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.service.VersatileBase;

@Component
public class AuthenticationGroupRepository extends VersatileBase {

	public AuthenticationGroupRepository() {
		super(DBName.AUTHENTICATION_GROUP_STORE);
	}
}