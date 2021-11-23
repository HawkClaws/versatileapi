package com.flex.versatileapi.repository;

import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.service.VersatileBase;

@Component
public class ApiSettingRepository extends VersatileBase {

	public ApiSettingRepository() {
		super(DBName.API_SETTING_STORE);
	}
}