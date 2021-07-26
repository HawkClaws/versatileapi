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
import com.flex.versatileapi.exceptions.ODataParseException;
import com.google.api.client.http.HttpMethods;
import com.google.firebase.internal.Objects;
import com.google.gson.Gson;

@Component
public class VersatileService {

	private Gson gson = new Gson();

	@Autowired
	private VersatileBase versatileBase;

	@Autowired
	private HashService hashService;

	public ResponseEntity<Object> execute(String repositoryKey, String id, String method, String body,
			String authorization, String ipAddress, String queryString, String targetRepository)
			throws IOException, InterruptedException, ExecutionException {

		ResponseEntity responseEntity = null;

		// JsonSchemaバリデーション・認証・許可されているメソッドをチェック
		this.versatileBase.setRepositoryName(ConstData.API_SETTING_STORE);
		responseEntity = versatileBase.checkUseApi(repositoryKey, id, method, body, authorization);

		if (responseEntity != null)
			return responseEntity;

		Object response = null;
		// 各メソッドの処理を実行
		this.versatileBase.setRepositoryName(targetRepository);
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
			if (id.equals(ConstData.UNIQUE)) {
				postId = userId;
			}else {
				if (repositoryKey.equals("") == false)
					repositoryKey += "/";
				repositoryKey += id;
			}

			response = versatileBase.post(postId, repositoryKey, queryString, body, userId);
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.CREATED);

		case HttpMethods.PUT:

			String userId2 = hashService.shortGenerateHashPassword(ipAddress);
			
			// TODO UNIQUE(非推奨)
			if (id.equals(ConstData.UNIQUE)) {
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
