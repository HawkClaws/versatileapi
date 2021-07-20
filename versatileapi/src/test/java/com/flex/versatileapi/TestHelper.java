package com.flex.versatileapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.flex.versatileapi.config.ConstData;
import com.google.gson.Gson;

public class TestHelper {
	private static RestTemplate restTemplate = new RestTemplate();
	private static Gson gson = new Gson();

	public static boolean jsonEquals(String json1, String json2) {
		if (Objects.equals(json1, json2)) {
			return true;
		}

		// idは比較しない？暫定
		Map<String, Object> jsonMap1 = gson.fromJson(json1, Map.class);
		removeManageItem(jsonMap1);
		Map<String, Object> jsonMap2 = gson.fromJson(json2, Map.class);
		removeManageItem(jsonMap2);

		return jsonMap1.equals(jsonMap2);
	}

	private static void removeManageItem(Map<String, Object> map) {
		map.remove("id");
		map.remove(ConstData.UNIQUE_ID);
		map.remove(ConstData.REG_DATE);
		map.remove(ConstData.UPD_DATE);
	}

	public static String post(String url, String json) {

		return upsertHelper(url, json, HttpMethod.POST, new HashMap<String, String>());
	}

	public static String post(String url, String json, Map<String, String> additionalHeaders) {

		return upsertHelper(url, json, HttpMethod.POST, additionalHeaders);
	}

	public static String put(String url, String json) {
		return upsertHelper(url, json, HttpMethod.PUT, new HashMap<String, String>());
	}

	public static String put(String url, String json, Map<String, String> additionalHeaders) {
		return upsertHelper(url, json, HttpMethod.PUT, additionalHeaders);
	}

	private static String upsertHelper(String url, String json, HttpMethod httpMethod,
			Map<String, String> additionalHeaders) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		for (String key : additionalHeaders.keySet()) {
			headers.add(key, additionalHeaders.get(key));
		}

		HttpEntity<String> entity = new HttpEntity<String>(json, headers);

		ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
		String body = response.getBody();

		if (body.equals(""))
			return null;
		return gson.fromJson(body, Map.class).get("id").toString();
	}
}
