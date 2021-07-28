package com.flex.versatileapi.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.stream.JsonParsingException;
import javax.servlet.http.HttpServletRequest;

import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.config.SystemConfig;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.ApiSettingModel;
import com.flex.versatileapi.model.RepositoryUrlInfo;
import com.flex.versatileapi.service.ApiSettingInfo;
import com.flex.versatileapi.service.AuthenticationService;
import com.flex.versatileapi.service.HashService;
import com.flex.versatileapi.service.RepositoryValidator;
import com.flex.versatileapi.service.UrlConverter;
import com.flex.versatileapi.service.VersatileBase;
import com.google.api.client.http.HttpMethods;
import com.google.gson.Gson;

@RestController
public class AdminController {

	@Autowired
	private UrlConverter urlConverter;

	@Autowired
	private HashService hashService;

//	@Autowired
//	private RepositoryValidator repositoryValidator;

	private JsonValidationService jvs = JsonValidationService.newInstance();

	@Autowired
	private VersatileBase versatileBase;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	ApiSettingInfo repositoryInfo;

	private Gson gson = new Gson();

	/**
	 * API定義のキーリストを返します
	 */
	@GetMapping(value = "/admin/apisettinglist")
	public ResponseEntity<Object> getRepository(HttpServletRequest request) {
		this.versatileBase.setRepositoryName(ConstData.API_SETTING_STORE);
		// TODO 認証リファクタ
		ResponseEntity resEnt = null;
		resEnt = adminAuthorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		return new ResponseEntity<>(this.versatileBase.getRepository(), new HttpHeaders(), HttpStatus.OK);
	}

	/**
	 * Api定義の登録・更新・取得・削除
	 */
	@RequestMapping(value = "/admin/apisetting/**")
	public ResponseEntity<Object> apiSetting(HttpServletRequest request) throws IOException, ODataParseException {
		this.versatileBase.setRepositoryName(ConstData.API_SETTING_STORE);
		RepositoryUrlInfo info = urlConverter.getRepositoryInfo2(request.getRequestURI());

		if (request.getMethod().equals(HttpMethods.GET)) {
			return new ResponseEntity<>(this.versatileBase.get(info.getId(), ConstData.JSON_SCHEMA, ""),
					new HttpHeaders(), HttpStatus.OK);
		}

		ResponseEntity resEnt = adminAuthorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		Object response = null;

		switch (request.getMethod()) {

		case HttpMethods.POST:
		case HttpMethods.PUT:
//			ApiSettingInfo repositoryInfo = new ApiSettingInfo(hashService, repositoryValidator, this.versatileBase);

			String body = request.getReader().lines().collect(Collectors.joining("\r\n"));

			List<Problem> problems = new ArrayList<Problem>();
			try {
				problems = repositoryInfo.validate(body);
			} catch (JsonParsingException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			if (problems.size() > 0)
				return new ResponseEntity<>(problems.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST);

			try {
				// Api定義作成
				ApiSettingModel apiSetting = repositoryInfo.toApiSettingModel(body);
				response = repositoryInfo.create(apiSetting);

			} catch (JsonValidatingException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}

			versatileBase.clearApiSettingCache();
			break;
		case HttpMethods.DELETE:
			response = versatileBase.delete(info.getId(), ConstData.JSON_SCHEMA, "");
			versatileBase.clearApiSettingCache();
			break;
		}

		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	/**
	 * authGroupの登録・更新・取得・削除
	 */
	@RequestMapping(value = "/admin/authgroup/**")
	public ResponseEntity<Object> authGroup(HttpServletRequest request) {
		return null;
	}
	
	/**
	 * ユーザーの登録・更新・取得・削除
	 */
	@RequestMapping(value = "/admin/user/**")
	public ResponseEntity<Object> user(HttpServletRequest request) {
		return null;
	}

	private ResponseEntity adminAuthorization(String authorization) {
		if (authorization == null || SystemConfig.getAdminAuthorization()
				.equals(hashService.generateHashPassword(authorization)) == false) {
			return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.FORBIDDEN);

		}
		return null;
	}

}
