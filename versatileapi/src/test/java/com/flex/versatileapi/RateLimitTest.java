package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class RateLimitTest {
	private static RestTemplate restTemplate = new RestTemplate();
	
//	@Test
	void RateLimit_超過() {
		String repository = "ratelimit";// なんでもOK!なのがこのシステムの魅力！
		String baseUrl = TestConfig.ApiUrl() + repository;
		
		String schema = String.format("{  'jsonSchema':{'$schema': 'http://json-schema.org/draft-04/schema#',  'additionalProperties': false, 'type': 'object',  'properties': {    'category': {      'type': 'string'    },    'name': {      'type': 'string'    },    'value': {      'type': 'string'    },    'detail': {      'type': 'object',      'properties': {        'weight': {          'type': 'string'        },        'description': {          'type': 'string'        }      },      'required': [        'weight',        'description'      ]    }  }},'apiSecret':'','methodSettings':[],'apiUrl':'%s'}",repository);
		schema = schema.replace("'", "\"");
		try {
			restTemplate.delete(TestConfig.ApiSettingUrl() + repository);
		} catch (RestClientResponseException exception) {
		}

		TestHelper.post(TestConfig.ApiSettingUrl() , schema, TestConfig.AuthHeader());
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");

		
		String requestJson = "{'category':'果物','name':'バナナ','value':'120'}";

		int resStatusCode = 0;
		
		try {
			
			for (int i = 0; i < 12; i++) {
				// 登録
				String id = TestHelper.post(baseUrl, requestJson);
			}
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}

		assertTrue(resStatusCode == 429);

	}
}
