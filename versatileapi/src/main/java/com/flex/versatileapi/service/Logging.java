package com.flex.versatileapi.service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.repository.RealtimeDatabaseRepotitory;

@Component
public class Logging {
	@Autowired
	RealtimeDatabaseRepotitory realtimeDatabaseRepotitory;

//	public void write(String log) {
//		realtimeDatabaseRepotitory.insert(log, log, null)
//		
//	}

	public void write(Map<String, Object> log) {
		try {
			log.put(ConstData.REG_DATE, new Timestamp(System.currentTimeMillis()));
			realtimeDatabaseRepotitory.insert("log", UUID.randomUUID().toString(), log);
		} catch (Exception e) {

		}
	}

}
