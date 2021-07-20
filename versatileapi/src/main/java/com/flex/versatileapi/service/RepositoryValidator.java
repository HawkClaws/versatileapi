package com.flex.versatileapi.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.BehaviorType;
import com.flex.versatileapi.model.MethodSetting;
import com.flex.versatileapi.model.RepositoryInfo;
import com.google.gson.Gson;

import lombok.Data;

@Component
public class RepositoryValidator {

	protected JsonValidationService jvs = JsonValidationService.newInstance();
	protected ConcurrentHashMap<String, RepositoryInfo> schemaMap = new ConcurrentHashMap<String, RepositoryInfo>();
	Gson gson = new Gson();

	@Autowired
	private HashService hashService;

	public ResponseEntity judgeUse(RepositoryInfo info, String httpMethod, String authorization, String id) {
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
					|| info.getRepositorySecret().equals(hashService.generateHashPassword(authorization)) == false) {
				return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.FORBIDDEN);
			}
			break;

		case IptoId:
			if (id.equals(ConstData.UNIQUE) == false) {
				return new ResponseEntity<>("only call " + ConstData.UNIQUE, new HttpHeaders(), HttpStatus.BAD_REQUEST);
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

	public RepositoryInfo getRepositoryInfo(String repositoryKey, VersatileService versatileService) throws ODataParseException {
		if (schemaMap.containsKey(repositoryKey)) {
			return schemaMap.get(repositoryKey);
		} else {
			Object schemaData = versatileService.get(repositoryKey, ConstData.JSON_SCHEMA, "");
			if (schemaData == null)
				return null;

			Map<String, Object> mapObj = (Map<String, Object>) schemaData;

			String schemaStr = mapObj.get(ConstData.JSON_SCHEMA).toString();

			List<MethodSetting> methodSettings = new ArrayList<MethodSetting>();
			for (Object obj : new ArrayList<>((Collection<Object>) mapObj.get(ConstData.ALLOW_METHODS))) {
				methodSettings.add(gson.fromJson(gson.toJson(obj), MethodSetting.class));
			}

			MethodSetting[] methodSetting = methodSettings.toArray(new MethodSetting[] {});
			String repositorySecret = mapObj.get(ConstData.REPOSITORY_SECRET).toString();

//			String schema = ((Map<String, Object>) schemaData).get(ConstData.JSON_SCHEMA).toString();
//			if (schema == null )
//				return null;
			JsonSchema js = jvs.readSchema(new StringReader(schemaStr));

			RepositoryInfo info = new RepositoryInfo(js, repositorySecret, methodSetting);

			schemaMap.put(repositoryKey, info);
			return info;
		}
	}

	public void clearSchemaCache() {
		schemaMap = new ConcurrentHashMap<String, RepositoryInfo>();
	}

}
