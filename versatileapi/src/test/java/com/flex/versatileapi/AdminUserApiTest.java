package com.flex.versatileapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.flex.versatileapi.service.HashService;
import com.google.gson.Gson;

@SpringBootTest
public class AdminUserApiTest {
	private static Gson gson = new Gson();
	private static RestTemplate restTemplate = new RestTemplate();
	private JsonValidationService jvs = JsonValidationService.newInstance();

	@Autowired
	private HashService hashService;

	@Test
	void amindUser正常() throws IOException {
		String baseUrl = TestConfig.AdminUrl() + "user/";
		String userId = "testUser";

		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("user_id", userId);
		testData.put("password", "PASSWORD");
		testData.put("email", "aaa@versatile.com");

		// テスト前クリーン
		testData.put("password", "PASSWORD2");
		try {
			TestHelper.delete(baseUrl + userId, gson.toJson(testData), TestConfig.AuthHeader());
		} catch (RestClientResponseException exception) {
		}
		testData.put("password", "PASSWORD");
		try {
			TestHelper.delete(baseUrl + userId, gson.toJson(testData), TestConfig.AuthHeader());
		} catch (RestClientResponseException exception) {
		}

		// 登録
		TestHelper.post(baseUrl, gson.toJson(testData), TestConfig.AuthHeader());

		// 取得
		Map<String, Object> res = gson.fromJson(TestHelper.get(baseUrl + userId, TestConfig.AuthHeader()), Map.class);
		assertTrue(res.get("user_id").toString().equals(testData.get("user_id").toString()));
		assertTrue(res.get("email").toString().equals(testData.get("email").toString()));
		assertTrue(res.get("password").toString()
				.equals(hashService.generateHashPassword(testData.get("password").toString())));

		// 更新（認証エラー）
		testData.put("password", "PASSWORD2");
		testData.put("new_password", "PASSWORD2");
		testData.remove("email");
		testData.put("new_email","bbb@versatile.com");
		int resStatusCode = 0;
		try {
			TestHelper.put(baseUrl, gson.toJson(testData), TestConfig.AuthHeader());
		} catch (RestClientResponseException exception) {
			resStatusCode = exception.getRawStatusCode();
		}
		assertTrue(resStatusCode == 403);
		
		// 更新
		testData.put("password", "PASSWORD");
		TestHelper.put(baseUrl, gson.toJson(testData), TestConfig.AuthHeader());
		
		// 取得(更新確認)
		res = gson.fromJson(TestHelper.get(baseUrl + userId, TestConfig.AuthHeader()), Map.class);
		assertTrue(res.get("user_id").toString().equals(testData.get("user_id").toString()));
		assertTrue(res.get("email").toString().equals(testData.get("new_email").toString()));
		assertTrue(res.get("password").toString()
				.equals(hashService.generateHashPassword(testData.get("new_password").toString())));
		
		
		
	}
}
