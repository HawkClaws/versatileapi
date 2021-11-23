package com.flex.versatileapi.repository;

import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.service.VersatileBase;

@Component
public class UserRepository extends VersatileBase {

	public UserRepository() {
		super(DBName.USER_STORE);
	}
}
