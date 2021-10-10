package com.flex.versatileapi.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonReader;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.model.ApiSettingModel;
import com.flex.versatileapi.model.BehaviorType;
import com.flex.versatileapi.model.MethodSetting;
import com.google.gson.Gson;

@Component
public class RepositoryValidator {

	protected JsonValidationService jvs = JsonValidationService.newInstance();
	private Gson gson = new Gson();

	@Autowired
	private HashService hashService;

	public ResponseEntity judgeUse(ApiSettingModel info, String httpMethod, String authorization, String id) {
		BehaviorType behaviorType = BehaviorType.Allow;
		for (MethodSetting setting : info.getMethodSettings()) {
			if (setting.getHttpMethod().toString().equals(httpMethod)) {
				behaviorType = setting.getBehavior();
				break;
			}
		}

		switch (behaviorType) {
		case Allow:
			return null;

		case NotImplemented:
			return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);

		case Authorization:
			if (authorization == null
					|| info.getApiSecret().equals(hashService.generateHashPassword(authorization)) == false) {
				return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.FORBIDDEN);
			}
			break;

		case IptoId:
			if (id.equals(ConstData.ID_UNIQUE) == false) {
				return new ResponseEntity<>("only call " + ConstData.ID_UNIQUE, new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			break;
		}

		return null;
	}

	public List<Problem> validateJson(String json, JsonSchema schema) {
		List<Problem> result = new ArrayList<Problem>();

		JsonReader reader = jvs.createReader(new StringReader(json), schema, ProblemHandler.collectingTo(result));
		reader.read();

		return result;
	}




}
