package com.flex.versatileapi.service;

import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.model.RepositoryUrlInfo;
import com.flex.versatileapi.repository.RealtimeDatabaseRepotitory;
import com.google.api.client.http.HttpMethods;

@Component
public class Logging {
	@Autowired
	RealtimeDatabaseRepotitory realtimeDatabaseRepotitory;
	
	@Autowired
	UrlConverter urlConverter;

	public void writeLog(HttpServletRequest request, String body) {
		try {
			String ip = request.getRemoteAddr();

			// テストはログ出力しない
			if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
				System.out.println("non-writeLog");
				return;
			}
				
			Date d = new Date();
			SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
			String c1 = d1.format(d);

			SimpleDateFormat d2 = new SimpleDateFormat("HH:mm:ss");
			String c2 = d2.format(d);

			Map<String, Object> log = new HashMap<String, Object>();
			String url = request.getRequestURI();

			if (request.getQueryString() != null)
				url += URLDecoder.decode(request.getQueryString(), "UTF-8");
			log.put("uri", url);
			log.put("method", request.getMethod());
			log.put("body", body);
			log.put("authorization", request.getHeader(ConstData.AUTHORIZATION));
			log.put("referer", request.getHeader("REFERER"));
			log.put("ip", request.getRemoteAddr());
			
			RepositoryUrlInfo info = urlConverter.getRepositoryInfo(request.getRequestURI());
			String repositoryKey = info.getRepositoryKey();

			realtimeDatabaseRepotitory.insert(
					"log/" + repositoryKey.replace("/", "_") + "/" + c1 + "/" + request.getMethod(), c2, log);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void writeLog(HttpServletRequest request, long remaining, String key) {
		return;
//		try {
//			String ip = request.getRemoteAddr();
//
////		// テストはログ出力しない
////		if (ip.equals("127.0.0.1"))
////			return;
//
//			Date d = new Date();
//			SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
//			String c1 = d1.format(d);
//
//			SimpleDateFormat d2 = new SimpleDateFormat("HH:mm:ss");
//			String c2 = d2.format(d);
//
//			Map<String, Object> log = new HashMap<String, Object>();
//			String url = request.getRequestURI();
//			if(request.getQueryString() != null)
//				url += URLDecoder.decode(request.getQueryString(), "UTF-8");
//			log.put("uri", url);
//			log.put("method", request.getMethod());
//			log.put("referer", request.getHeader("REFERER"));
//			log.put("ip", request.getRemoteAddr());
//			log.put("key", key);
//			log.put("remaining", remaining);
//
//			realtimeDatabaseRepotitory.insert("rate-log/" + c1 + "/" + request.getMethod(), c2, log);
//		} catch (Exception e) {
//
//		}
	}

	public void write(Map<String, Object> log) {
		try {
			log.put(ConstData.REG_DATE, new Timestamp(System.currentTimeMillis()));
			realtimeDatabaseRepotitory.insert("log", UUID.randomUUID().toString(), log);
		} catch (Exception e) {

		}
	}

}
