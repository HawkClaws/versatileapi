package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SpringBootTest
public class Test_RegisterList {

	private static RestTemplate restTemplate = new RestTemplate();
	private static Gson gson = new Gson();

	@Test
	public void RegisterAll_正常() {
		String repository = "test/repository";// なんでもOK!なのがこのシステムの魅力！
		String baseUrl = Test_Config.ApiUrl() + repository;

		String schema = String.format(
				"{  'authGroupId':'','jsonSchema':{'$schema': 'http://json-schema.org/draft-04/schema#',  'additionalProperties': false, 'type': 'object',  'properties': {    'category': {      'type': 'string'    },    'name': {      'type': 'string'    },    'value': {      'type': 'string'    },    'detail': {      'type': 'object',      'properties': {        'weight': {          'type': 'string'        },        'description': {          'type': 'string'        }      },      'required': [        'weight',        'description'      ]    }  }},'apiSecret':'','methodSettings':[],'apiUrl':'%s'}",
				repository);
		schema = schema.replace("'", "\"");

		try {
			restTemplate.delete(Test_Config.ApiSettingUrl() + repository);
		} catch (RestClientResponseException exception) {
		}

		Test_Helper.post(Test_Config.ApiSettingUrl(), schema, Test_Config.AuthHeader());

		// テスト前クリーン
		restTemplate.delete(baseUrl + "/all");

		String requestJson = "[{'category':'果物','name':'バナナ','value':'120'},{'category':'果物','name':'りんご','value':'80','detail':{'weight':'40','description':'赤いいフルーツ'}}]";

		// 登録
		Map resIds = restTemplate.postForEntity(baseUrl + "/all", requestJson, Map.class).getBody();

		// count
		Map<String, Object> map = restTemplate.getForObject(baseUrl + "/count", Map.class);
		assertTrue((int) map.get("count") == 2);

		List<String> ids = new ArrayList<String>((Collection<String>) resIds.get("ids"));
		for (String id : ids) {
			restTemplate.delete(baseUrl + "/" + id);
		}

		// count
		map = restTemplate.getForObject(baseUrl + "/count", Map.class);
		assertTrue((int) map.get("count") == 0);

	}
}
