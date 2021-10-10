package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@SpringBootTest
class Test_VersatileApi {

	private static RestTemplate restTemplate = new RestTemplate();
	private static Gson gson = new Gson();

	@Test
	void CrudApi_正常() {
		String repository = "test/repository";// なんでもOK!なのがこのシステムの魅力！
		String baseUrl = Test_Config.ApiUrl() + repository;
		
		String schema = String.format("{  'authGroupId':'','jsonSchema':{'$schema': 'http://json-schema.org/draft-04/schema#',  'additionalProperties': false, 'type': 'object',  'properties': {    'category': {      'type': 'string'    },    'name': {      'type': 'string'    },    'value': {      'type': 'string'    },    'detail': {      'type': 'object',      'properties': {        'weight': {          'type': 'string'        },        'description': {          'type': 'string'        }      },      'required': [        'weight',        'description'      ]    }  }},'apiSecret':'','methodSettings':[],'apiUrl':'%s'}",repository);
		schema = schema.replace("'", "\"");


		try {
			restTemplate.delete(Test_Config.ApiSettingUrl() + repository);
		} catch (RestClientResponseException exception) {
		}

		Test_Helper.post(Test_Config.ApiSettingUrl() , schema, Test_Config.AuthHeader());
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");

		String requestJson = "{'category':'果物','name':'バナナ','value':'120'}";

		// 登録
		String id = Test_Helper.post(baseUrl, requestJson);

		// 取得
		String responseJson = restTemplate.getForObject(baseUrl + "/" + id, String.class);
		assertTrue(Test_Helper.jsonEquals(responseJson, requestJson), responseJson + ":" + requestJson);

		String requestJson2 = "{'category':'果物','name':'りんご','value':'80','detail':{'weight':'40','description':'赤いいフルーツ'}}";
		// 更新
		Test_Helper.put(baseUrl + "/" + id, requestJson2);
		// 取得
		responseJson = restTemplate.getForObject(baseUrl + "/" + id, String.class);
		assertTrue(Test_Helper.jsonEquals(responseJson, requestJson2), responseJson);

		// 取得
		responseJson = restTemplate.getForObject(baseUrl + "/" + id, String.class);
		assertTrue(Test_Helper.jsonEquals(responseJson, requestJson2), responseJson);

		// 削除
		restTemplate.delete(baseUrl + "/" + id);

		int resStatusCode = 0;
		try {
			restTemplate.getForObject(baseUrl + "/" + id, String.class);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 404);
	}

	@Test
	void CrudApi_自動採番_全取得_正常() {
		String repository = "repositoryEx";
		String baseUrl = Test_Config.ApiUrl() + repository;

		String schema = String.format("{  'authGroupId':'','jsonSchema':{'$schema': 'http://json-schema.org/draft-04/schema#', 'additionalProperties': true,  'type': 'object',  'properties': {    'category': {      'type': 'string'    },    'name': {      'type': 'string'    },    'value': {      'type': 'string'    },    'detail': {      'type': 'object',      'properties': {        'weight': {          'type': 'string'        },        'description': {          'type': 'string'        }      },      'required': [        'weight',        'description'      ]    }  }},'apiSecret':'','methodSettings':[],'apiUrl':'%s'}",repository);
		schema = schema.replace("'", "\"");


		try {
			restTemplate.delete(Test_Config.ApiSettingUrl() + repository);
		} catch (RestClientResponseException exception) {
		}

		Test_Helper.post(Test_Config.ApiSettingUrl() , schema, Test_Config.AuthHeader());
		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");

		String requestJson1 = "{'category':'動物','name':'サル','weight':'40'}";
		String requestJson2 = "{'category':'動物','name':'馬','weight':'80'}";
		String requestJson3 = "{'category':'果物','name':'りんご','value':'80'}";

		// 登録
		String id1 = Test_Helper.post(baseUrl, requestJson1);
		String id2 = Test_Helper.post(baseUrl, requestJson2);
		String id3 = Test_Helper.post(baseUrl, requestJson3);

		// 全取得
		Object[] datas = restTemplate.getForObject(baseUrl + "/all", new Object[] {}.getClass());
		for (Object data : datas) {
			assertTrue(Test_Helper.jsonEquals(requestJson1, gson.toJson(data))
					|| Test_Helper.jsonEquals(requestJson2, gson.toJson(data))
					|| Test_Helper.jsonEquals(requestJson3, gson.toJson(data)), data.toString());
		}
		
		//count
		Map<String,Object> map = restTemplate.getForObject(baseUrl + "/count", Map.class);
		assertTrue((int)map.get("count") == 3);
		
		// 削除
		restTemplate.delete(baseUrl + "/" + id1);
		restTemplate.delete(baseUrl + "/" + id2);
		restTemplate.delete(baseUrl + "/" + id3);

		int resStatusCode = 0;
		try {
			restTemplate.getForObject(baseUrl + "/all", String.class);
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 404);
	}

}
