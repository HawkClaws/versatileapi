package com.flex.versatileapi.repository;

import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.service.VersatileBase;

@Component
public class DataStoreRepository extends VersatileBase {

	public DataStoreRepository() {
		super(DBName.DATA_STORE);
	}
}
