package com.flex.versatileapi.service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.exceptions.DBWriteException;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.repository.ApiSettingRepository;
import com.google.api.client.http.HttpMethods;
import com.google.firebase.internal.Objects;
import com.google.gson.Gson;

@Component
public class VersatileService {

	private Gson gson = new Gson();

	@Autowired
	private HashService hashService;
	
	@Autowired
	private ApiSettingRepository apiSettingRepository;

	public ResponseEntity<Object> execute(String repositoryKey, String id, String method, String body,
			String authorization, String ipAddress, String queryString, VersatileBase versatileBase)
			throws IOException, InterruptedException, ExecutionException {

		ResponseEntity responseEntity = null;

		// JsonSchemaバリデーション・認証・許可されているメソッドをチェック
		responseEntity = apiSettingRepository.checkUseApi(repositoryKey, id, method, body, authorization);

		if (responseEntity != null)
			return responseEntity;

		Object response = null;
		// 各メソッドの処理を実行
		switch (method) {
		case HttpMethods.GET:
			try {
				response = versatileBase.get(id, repositoryKey, queryString);
			} catch (ODataParseException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			if (response == null || Objects.equal(response, "")) {
				return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
			}

		case HttpMethods.POST:
			String userId = hashService.shortGenerateHashPassword(ipAddress);
			String postId = UUID.randomUUID().toString();
			
			// TODO UNIQUE(非推奨)
			if (id.equals(ConstData.ID_UNIQUE)) {
				postId = userId;
			}else if (id.equals(ConstData.ID_ALL)) {
				postId = id;
			}else {
				if (repositoryKey.equals("") == false)
					repositoryKey += "/";
				repositoryKey += id;
			}
			try {
				response = versatileBase.post(postId, repositoryKey, queryString, body, userId);
			} catch (DBWriteException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.CREATED);

		case HttpMethods.PUT:

			String userId2 = hashService.shortGenerateHashPassword(ipAddress);
			
			// TODO UNIQUE(非推奨)
			if (id.equals(ConstData.ID_UNIQUE)) {
				id = userId2;
			}

			response = versatileBase.put(id, repositoryKey, queryString, body, userId2);
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);

		case HttpMethods.DELETE:
			response = versatileBase.delete(id, repositoryKey, queryString);
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.NO_CONTENT);

		}
		return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);
	}
}
