package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.flex.versatileapi.config.ConstData;
import com.google.gson.Gson;

public class Test_UniqueApi {
	private static Gson gson = new Gson();
	private static RestTemplate restTemplate = new RestTemplate();
	
	@Test
	void UniqueApi_正常() {
		String repository = "UniqueApiTest";
		String baseUrl = Test_Config.ApiUrl() + repository;
		
		
		String repositoryInfo = String.format("{\r\n"
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
				+ "			\"behavior\": \"IptoId\"\r\n"
				+ "		},\r\n"
				+ "        {\r\n"
				+ "			\"httpMethod\": \"PUT\",\r\n"
				+ "			\"behavior\": \"IptoId\"\r\n"
				+ "		}\r\n"
				+ "	]\r\n"
				+  ",\"apiUrl\":\"%s\""
				+ ",\"authGroupId\":\"\""
				+ "}",repository);
		
		
		//Repository情報登録
		Test_Helper.post(Test_Config.ApiSettingUrl(), repositoryInfo,Test_Config.AuthHeader());
		
		
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");

		String requestJson = "{'name':'田中','age':55,'gender':'man'}";

		int resStatusCode = 0;
		try {
			String id = Test_Helper.post(baseUrl, requestJson);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 400);
		
		//登録
		String id = Test_Helper.post(baseUrl + "/" + ConstData.ID_UNIQUE, requestJson);
		String id2 = Test_Helper.post(baseUrl + "/" + ConstData.ID_UNIQUE, requestJson);
		assertTrue(id.equals(id2));
		
		//取得
		String responseJson1  = restTemplate.getForObject(baseUrl + "/" + id, String.class);
		
		//更新
		String id3 = Test_Helper.put(baseUrl + "/" + ConstData.ID_UNIQUE, requestJson);
		assertTrue(id.equals(id3));
		 
		//取得
		String responseJson2  = restTemplate.getForObject(baseUrl + "/" + id, String.class);
		
		assertTrue(Test_Helper.jsonEquals(responseJson1, responseJson2));
	}
}
