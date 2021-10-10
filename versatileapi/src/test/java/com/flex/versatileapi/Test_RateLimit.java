package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class Test_RateLimit {
	private static RestTemplate restTemplate = new RestTemplate();
	
//	@Test
	void RateLimit_超過() {
		String repository = "ratelimit";// なんでもOK!なのがこのシステムの魅力！
		String baseUrl = Test_Config.ApiUrl() + repository;
		
		String schema = String.format("{  'jsonSchema':{'$schema': 'http://json-schema.org/draft-04/schema#',  'additionalProperties': false, 'type': 'object',  'properties': {    'category': {      'type': 'string'    },    'name': {      'type': 'string'    },    'value': {      'type': 'string'    },    'detail': {      'type': 'object',      'properties': {        'weight': {          'type': 'string'        },        'description': {          'type': 'string'        }      },      'required': [        'weight',        'description'      ]    }  }},'apiSecret':'','methodSettings':[],'apiUrl':'%s'}",repository);
		schema = schema.replace("'", "\"");
		try {
			restTemplate.delete(Test_Config.ApiSettingUrl() + repository);
		} catch (RestClientResponseException exception) {
		}

		Test_Helper.post(Test_Config.ApiSettingUrl() , schema, Test_Config.AuthHeader());
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");

		
		String requestJson = "{'category':'果物','name':'バナナ','value':'120'}";

		int resStatusCode = 0;
		
		try {
			
			for (int i = 0; i < 12; i++) {
				// 登録
				String id = Test_Helper.post(baseUrl, requestJson);
			}
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}

		assertTrue(resStatusCode == 429);

	}
}
