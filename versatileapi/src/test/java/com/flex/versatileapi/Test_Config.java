package com.flex.versatileapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.flex.versatileapi.config.SystemConfig;
import com.google.gson.Gson;

public class Test_Config {
	private static Gson gson = new Gson();
	
	public static String BASE_URL = "http://localhost:8080/";
//	public static String BASE_URL = "https://versatileapi.herokuapp.com/";
	
	private static String API_BASE_URL = "api/";
	private static String ADMIN_BASE_URL = "admin/";
	private static String API_SETTING = "apisetting/";
	
	public static Map<String,String> AuthHeader() {
		Map<String,String> header = new HashMap<String,String>();
		header.put("Admin-Authorization", SystemConfig.getRawAdminAuthorization());
		return header;
	}
	
	public static String ApiUrl() {
		return BASE_URL + API_BASE_URL;
	}
	
	public static String AdminUrl() {
		return BASE_URL + ADMIN_BASE_URL;
	}
	
	public static String ApiSettingUrl() {
		return BASE_URL + ADMIN_BASE_URL + API_SETTING;
	}
}
