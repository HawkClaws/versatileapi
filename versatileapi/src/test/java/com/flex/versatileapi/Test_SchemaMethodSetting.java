package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

@SpringBootTest
class Test_SchemaMethodSetting {
	private static Gson gson = new Gson();
	private static RestTemplate restTemplate = new RestTemplate();

	@Test
	void JsonSchema_正常() {
		String repository = "JsonSchemaTest";
		String baseUrl = Test_Config.ApiUrl() + repository;
		
		
		String repositoryInfo =String.format("{\r\n"
				+ "	\"jsonSchema\": {\r\n"
				+ "        \"additionalProperties\":false,\r\n"
				+ "		\"type\": \"object\",\r\n"
				+ "		\"properties\": {\r\n"
				+ "			\"name\": {\r\n"
				+ "				\"type\": \"string\"\r\n"
				+ "			},\r\n"
				+ "			\"age\": {\r\n"
				+ "				\"type\": \"integer\"\r\n"
				+ "			},\r\n"
				+ "			\"gender\": {\r\n"
				+ "				\"type\": \"string\"\r\n"
				+ "			}\r\n"
				+ "		},\r\n"
				+ "		\"required\": [\r\n"
				+ "			\"name\",\r\n"
				+ "			\"age\",\r\n"
				+ "			\"gender\"\r\n"
				+ "		]\r\n"
				+ "	},\r\n"
				+ "	\"apiSecret\": \"VersatileApi123\",\r\n"
				+ "	\"methodSettings\": [\r\n"
				+ "		{\r\n"
				+ "			\"httpMethod\": \"GET\",\r\n"
				+ "			\"behavior\": \"Allow\"\r\n"
				+ "		},\r\n"
				+ "        {\r\n"
				+ "			\"httpMethod\": \"POST\",\r\n"
				+ "			\"behavior\": \"Authorization\"\r\n"
				+ "		},\r\n"
				+ "        {\r\n"
				+ "			\"httpMethod\": \"PUT\",\r\n"
				+ "			\"behavior\": \"NotImplemented\"\r\n"
				+ "		}\r\n"
				+ "	]\r\n"
				+ ",\"authGroupId\":\"\""
				+  ",\"apiUrl\":\"%s\""
				+ "}",repository);
		
		
		//Repository情報登録
		Test_Helper.post(Test_Config.ApiSettingUrl(), repositoryInfo,Test_Config.AuthHeader());
		
		
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");
		
		
		String requestJson = "{'category':'果物','name':'バナナ','value':'120'}";

		// 登録(認証エラー)
		int resStatusCode = 0;
		try {
			Test_Helper.post(baseUrl, requestJson);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 403);
		
		
		// 登録(認証エラー2)
		Map<String,String> header = new HashMap<String,String>();
		header.put("Authorization", "asdasd");
		resStatusCode = 0;
		try {
			Test_Helper.post(baseUrl, requestJson,header);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 403);
		
		// 登録(requiredエラー)
		header = new HashMap<String,String>();
		header.put("Authorization", "VersatileApi123"); 
		resStatusCode = 0;
		try {
			Test_Helper.post(baseUrl, requestJson,header);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 400);
		
		// 登録(正常)
		requestJson = "{'name':'田中','age':55,'gender':'man'}";
		header = new HashMap<String,String>();
		header.put("Authorization", "VersatileApi123"); 
		resStatusCode = 0;
		String resId = Test_Helper.post(baseUrl, requestJson,header);
		
		// 更新(NotImplementedエラー)
		header = new HashMap<String,String>();
		header.put("Authorization", "VersatileApi123"); 
		resStatusCode = 0;
		try {
			Test_Helper.put(baseUrl + "/" + resId, requestJson,header);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 501);
		
		//取得-認証なし（正常）
		String responseJson = restTemplate.getForObject(baseUrl + "/" + resId, String.class);
		assertTrue(Test_Helper.jsonEquals(responseJson, requestJson));
	}

}
