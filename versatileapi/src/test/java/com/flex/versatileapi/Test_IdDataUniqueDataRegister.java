package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

@SpringBootTest
public class Test_IdDataUniqueDataRegister {
	private static RestTemplate restTemplate = new RestTemplate();
	private static Gson gson = new Gson();
	
	@Test
	public void データにIDを付与_正常() {
		String repository = "test/repository_unique";
		String baseUrl = Test_Config.ApiUrl() + repository;
		
		String schema = String.format("{  'authGroupId':'','jsonSchema':{'$schema': 'http://json-schema.org/draft-04/schema#',  'additionalProperties': false, 'type': 'object',  'properties': {   'id': {          'type': 'string'        },      'category': {      'type': 'string'    },    'name': {      'type': 'string'    },    'value': {      'type': 'string'    },    'detail': {      'type': 'object',      'properties': {    'weight': {          'type': 'string'        },        'description': {          'type': 'string'        }      },      'required': [        'weight',        'description'      ]    }  }},'apiSecret':'','methodSettings':[],'apiUrl':'%s'}",repository);
		schema = schema.replace("'", "\"");


		try {
			restTemplate.delete(Test_Config.ApiSettingUrl() + repository);
		} catch (RestClientResponseException exception) {
		}

		Test_Helper.post(Test_Config.ApiSettingUrl() , schema, Test_Config.AuthHeader());
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");
		
		//データにIDを付与
		String requestJson = "{'id':'testid','category':'果物','name':'バナナ','value':'120'}";

		// 登録
		String id = Test_Helper.post(baseUrl, requestJson);
		
		// 取得
		String responseJson = restTemplate.getForObject(baseUrl + "/" + id, String.class);
		assertTrue(Test_Helper.jsonEquals(responseJson, requestJson), responseJson + ":" + requestJson);
		
		// 登録（ID重複登録）
		int resStatusCode = 0;
		try {
			Test_Helper.post(baseUrl, requestJson);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 400);
		
		//count
		Map<String,Object> map = restTemplate.getForObject(baseUrl + "/count", Map.class);
		assertTrue((int)map.get("count") == 1);
	}
	
}
