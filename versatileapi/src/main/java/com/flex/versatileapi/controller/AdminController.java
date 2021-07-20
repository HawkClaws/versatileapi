package com.flex.versatileapi.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.stream.JsonParsingException;
import javax.servlet.http.HttpServletRequest;

import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.RepositoryUrlInfo;
import com.flex.versatileapi.service.HashService;
import com.flex.versatileapi.service.RepositoryInfo;
import com.flex.versatileapi.service.RepositoryValidator;
import com.flex.versatileapi.service.UrlConverter;
import com.flex.versatileapi.service.VersatileService;
import com.google.api.client.http.HttpMethods;
import com.google.gson.Gson;

@RestController
public class AdminController {
	@Autowired
	private VersatileService versatileService;

	@Autowired
	private UrlConverter urlConverter;

	@Autowired
	private HashService hashService;

	@Autowired
	private RepositoryValidator repositoryValidator;

	JsonValidationService jvs = JsonValidationService.newInstance();

	@Value("${spring.datasource.adminAuthorization}")
	private String adminAuthorization;

	Gson gson = new Gson();

	@RequestMapping(value = "/admin/repositorylist")
	public ResponseEntity<Object> getRepository(HttpServletRequest request) {
		// TODO 認証リファクタ
		ResponseEntity resEnt = null;
		resEnt = authorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		return new ResponseEntity<>(versatileService.getRepository(), new HttpHeaders(), HttpStatus.OK);
	}

	@RequestMapping(value = "/admin/apisetting/**")
	public ResponseEntity<Object> registerSchema(HttpServletRequest request) throws IOException, ODataParseException {
		ResponseEntity resEnt = null;
		resEnt = authorization(request.getHeader(ConstData.ADMIN_AUTHORIZATION));
		if (resEnt != null)
			return resEnt;

		RepositoryUrlInfo info = urlConverter.getRepositoryInfo2(request.getRequestURI());

		Object response = null;

		switch (request.getMethod()) {
		case HttpMethods.GET:
			response = versatileService.get(info.id, ConstData.JSON_SCHEMA, "");
			break;

		case HttpMethods.POST:
		case HttpMethods.PUT:
			RepositoryInfo repositoryInfo = new RepositoryInfo(hashService, repositoryValidator, versatileService);

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
				response = repositoryInfo.create(request.getMethod(), body);
			} catch (JsonValidatingException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}

			versatileService.clearSchemaCache();
			break;
		case HttpMethods.DELETE:
			response = versatileService.delete(info.id, ConstData.JSON_SCHEMA, "");
			versatileService.clearSchemaCache();
			break;
		}

		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	private ResponseEntity authorization(String authorization) {
		if (authorization == null
				|| adminAuthorization.equals(hashService.generateHashPassword(authorization)) == false) {
			return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.FORBIDDEN);

		}
		return null;
	}

}
