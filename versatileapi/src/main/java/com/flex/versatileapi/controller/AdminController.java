package com.flex.versatileapi.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.config.SystemConfig;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.extend.GsonEx;
import com.flex.versatileapi.model.ApiSettingModel;
import com.flex.versatileapi.model.RepositoryUrlInfo;
import com.flex.versatileapi.model.User;
import com.flex.versatileapi.repository.ApiSettingRepository;
import com.flex.versatileapi.repository.AuthenticationGroupRepository;
import com.flex.versatileapi.repository.DataStoreRepository;
import com.flex.versatileapi.repository.UserRepository;
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
	private ApiSettingRepository apiSettingRepository;
	
	@Autowired
	private DataStoreRepository dataStoreRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AuthenticationGroupRepository authenticationGroupRepository;

	@Autowired
	private AuthenticationService authService;

	@Autowired
	private ApiSettingInfo apiSettingInfo;

	@Autowired
	private GsonEx gsonEx;

	/**
	 * API定義のキーリストを返します
	 */
	@GetMapping(value = "/admin/apisettinglist")
	public ResponseEntity<Object> getRepository(HttpServletRequest request) {

		// TODO 認証リファクタ
		ResponseEntity resEnt = null;
		resEnt = adminAuthorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		return new ResponseEntity<>(this.apiSettingRepository.getRepository(), new HttpHeaders(), HttpStatus.OK);
	}

	/**
	 * Api定義の登録・更新・取得・削除
	 */
	@RequestMapping(value = "/admin/apisetting/**")
	public ResponseEntity<Object> apiSetting(HttpServletRequest request) throws IOException {
		this.apiSettingRepository.createIndex(ConstData.JSON_SCHEMA);
		RepositoryUrlInfo info = urlConverter.getRepositoryInfo2(request.getRequestURI());

		ResponseEntity resEnt = adminAuthorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		Object response = null;

		switch (request.getMethod()) {

		case HttpMethods.GET:
			return new ResponseEntity<>(this.apiSettingRepository.get(info.getId(), ConstData.JSON_SCHEMA, ""),
					new HttpHeaders(), HttpStatus.OK);

		case HttpMethods.POST:
		case HttpMethods.PUT:
//			ApiSettingInfo repositoryInfo = new ApiSettingInfo(hashService, repositoryValidator, this.versatileBase);

			String body = request.getReader().lines().collect(Collectors.joining("\r\n"));

			List<Problem> problems = new ArrayList<Problem>();
			try {
				problems = apiSettingInfo.validate(body);
			} catch (JsonParsingException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			if (problems.size() > 0)
				return new ResponseEntity<>(problems.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST);

			try {
				// Api定義作成
				ApiSettingModel apiSetting = apiSettingInfo.toModel(body);
				response = this.apiSettingRepository.put(apiSetting.getApiUrl(), ConstData.JSON_SCHEMA, "",
						gsonEx.g.toJson(apiSetting), "");
				
				
				// Indexを張る
				this.dataStoreRepository.createIndex(apiSetting.getApiUrl());
				
			} catch (JsonValidatingException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}

			this.apiSettingRepository.clearApiSettingCache();
			break;
		case HttpMethods.DELETE:
			response = this.apiSettingRepository.delete(info.getId(), ConstData.JSON_SCHEMA, "");
			this.apiSettingRepository.clearApiSettingCache();
			break;
		}

		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	/**
	 * ユーザーの登録・更新・取得・削除
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/admin/user/**")
	public ResponseEntity<Object> user(HttpServletRequest request) throws IOException {
		RepositoryUrlInfo info = urlConverter.getRepositoryInfo2(request.getRequestURI());

		ResponseEntity<Object> resEnt = adminAuthorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		String body = request.getReader().lines().collect(Collectors.joining("\r\n"));

		if (request.getMethod().toUpperCase().equals(HttpMethods.GET)) {
			Object getRes = this.userRepository.get(info.getId(), ConstData.USER, "");
			return new ResponseEntity<>(getRes, new HttpHeaders(), HttpStatus.OK);

		}

		List<Problem> problems = new ArrayList<Problem>();
		try {

			User user = gsonEx.g.fromJson(body, User.class);

			switch (request.getMethod()) {
			case HttpMethods.POST:
				problems = authService.validateUser(body);
				if (problems.size() > 0)
					return new ResponseEntity<>(problems.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST);

				Object exists = this.userRepository.get(user.getUser_id(), ConstData.USER, "");
				if (exists != null)
					return new ResponseEntity<>("This USER_ID is already in use", HttpStatus.BAD_REQUEST);

				// ユーザー作成
				user = authService.toUser(user.getUser_id(), user.getPassword(), user.getEmail());

				Object postRes = this.userRepository.post(user.getUser_id(), ConstData.USER, "", gsonEx.g.toJson(user),
						hashService.shortGenerateHashPassword(request.getRemoteAddr()));
				return new ResponseEntity<>(postRes, new HttpHeaders(), HttpStatus.CREATED);
			case HttpMethods.PUT:
				problems = authService.validateUpdateUser(body);
				if (problems.size() > 0)
					return new ResponseEntity<>(problems.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
				// User認証
				if (authService.authUser(user.getUser_id(), user.getPassword()) == false)
					return new ResponseEntity<>("Can't find USER_ID or authentication failure", HttpStatus.FORBIDDEN);

				// ユーザー更新(IDは変えられないようにする？)
				user = authService.toUser(user.getUser_id(), user.getNew_password(), user.getNew_email());
				Object updRes = this.userRepository.put(user.getUser_id(), ConstData.USER, "", gsonEx.g.toJson(user),
						hashService.shortGenerateHashPassword(request.getRemoteAddr()));
				return new ResponseEntity<>(updRes, new HttpHeaders(), HttpStatus.OK);
			case HttpMethods.DELETE:
				// User認証
				if (authService.authUser(user.getUser_id(), user.getPassword()) == false)
					return new ResponseEntity<>("Can't find USER_ID or authentication failure", HttpStatus.FORBIDDEN);
				// ユーザー削除
				Object delRes = this.userRepository.delete(user.getUser_id(), ConstData.USER, "");
				return new ResponseEntity<>(delRes, new HttpHeaders(), HttpStatus.NO_CONTENT);
			}
		} catch (JsonParsingException e) {
			return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
		}
//			versatileBase.clearApiSettingCache();

		return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);

	}

	/**
	 * authGroupの登録・更新・取得・削除 １．AuthGroupでバリデーション
	 * ２．AUTHENTICATION_GROUP_STOREに定義を入れる（API_SETTING_STOREに入れるとURLとして使用できてしまうため）
	 * ３．
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/admin/authgroup/**")
	public ResponseEntity<Object> authGroup(HttpServletRequest request) throws IOException {
		RepositoryUrlInfo info = urlConverter.getRepositoryInfo2(request.getRequestURI());

		ResponseEntity resEnt = adminAuthorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		Object response = null;

		switch (request.getMethod()) {

		case HttpMethods.GET:
			return new ResponseEntity<>(this.authenticationGroupRepository.get(info.getId(), ConstData.AUTHENTICATION_GROUP, ""),
					new HttpHeaders(), HttpStatus.OK);

		case HttpMethods.POST:
		case HttpMethods.PUT:

			String body = request.getReader().lines().collect(Collectors.joining("\r\n"));

			try {
				// Api定義作成
				ApiSettingModel apiSetting = apiSettingInfo.toModel(body);
				response = this.authenticationGroupRepository.post(apiSetting.getApiUrl(), ConstData.AUTHENTICATION_GROUP, "",
						gsonEx.g.toJson(apiSetting), hashService.shortGenerateHashPassword(request.getRemoteAddr()));

			} catch (JsonValidatingException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}

			this.apiSettingRepository.clearApiSettingCache();
			break;
		case HttpMethods.DELETE:
			response = this.authenticationGroupRepository.delete(info.getId(), ConstData.AUTHENTICATION_GROUP, "");
			this.apiSettingRepository.clearApiSettingCache();
			break;
		}

		return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);
	}

	private ResponseEntity adminAuthorization(String authorization) {
		if (authorization == null || SystemConfig.getAdminAuthorization()
				.equals(hashService.generateHashPassword(authorization)) == false) {
			return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.FORBIDDEN);

		}
		return null;
	}

}
