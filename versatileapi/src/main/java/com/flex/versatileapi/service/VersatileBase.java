package com.flex.versatileapi.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.stream.JsonParsingException;

import org.leadpony.justify.api.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.config.SystemConfig;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.ApiSettingModel;
import com.flex.versatileapi.repository.IRepository;
import com.flex.versatileapi.repository.MongoRepository;
import com.google.api.client.http.HttpMethods;
import com.google.gson.Gson;

@Component
public class VersatileBase {
//	@Autowired
//	RealtimeDatabaseRepotitory repository;
	private IRepository repository = null;

	public void setRepositoryName(String repositoryName) {
		this.repository = new MongoRepository(repositoryName);
	}

	@Autowired
	ODataParser oDataParser;

	@Autowired
	RepositoryValidator repositoryValidator;
	
	@Autowired
	private HashService hashService;

	private final static String ALL = "all";
	private final static String COUNT = "count";
	
	
	private Gson gson = new Gson();

	public Object get(String id, String repositoryKey, String queryString) throws ODataParseException {
		if (ALL.equals(id)) {
			return repository.getAll(repositoryKey, oDataParser.parse(queryString));
		} else if (COUNT.equals(id)) {
			return repository.getCount(repositoryKey);
		} else {
			return repository.get(repositoryKey, id);
		}
	}

	public Object post(String id, String repositoryKey, String queryString, String body, String userId) {
		Map<String, Object> value = gson.fromJson(body, Map.class);

		Timestamp now = new Timestamp(System.currentTimeMillis());
		value.put(ConstData.REG_DATE, now);
		value.put(ConstData.UPD_DATE, now);

		value.put(ConstData.UNIQUE_ID, userId);

		return repository.insert(repositoryKey, id, value);

		// TODOリファクタ
//		if (id.equals(ConstData.UNIQUE)) {
//			return repository.insert(repositoryKey, userId, value);
//		} else if (repositoryKey.equals(ConstData.JSON_SCHEMA)) {
//			return repository.insert(repositoryKey, id, value);
//		} else {
//			if (repositoryKey.equals("") == false)
//				repositoryKey += "/";
//			repositoryKey += id;
//			return repository.insert(repositoryKey, UUID.randomUUID().toString(), value);
//		}
	}

	public Object put(String id, String repositoryKey, String queryString, String body, String userId) {
		Map<String, Object> value = gson.fromJson(body, Map.class);
		value.put(ConstData.UPD_DATE, new Timestamp(System.currentTimeMillis()));
		
		return repository.update(repositoryKey, id, value);

//		// TODOリファクタ
//		if (id.equals(ConstData.UNIQUE)) {
//			return repository.update(repositoryKey, userId, value);
//		} else {
//			return repository.update(repositoryKey, id, value);
//		}
	}

	public Object delete(String id, String repositoryKey, String queryString) {
		if (ALL.equals(id)) {
			return repository.deleteAll(repositoryKey);
		} else {
			return repository.delete(repositoryKey, id);
		}
	}

	public ResponseEntity checkUseApi(String repositoryKey, String id, String method, String json,
			String authorization) {

		// DB分けたので「JSON_SCHEMA」でもOK
//		if (repositoryKey.toUpperCase().equals(ConstData.JSON_SCHEMA)) {
//			return new ResponseEntity<>(ConstData.JSON_SCHEMA + " cannot be specified because it is a reserved word",
//					new HttpHeaders(), HttpStatus.BAD_REQUEST);
//		}

		if (method.equals(HttpMethods.POST.toString()) && id.equals(ConstData.UNIQUE) == false) {
			if (repositoryKey.equals("") == false)
				repositoryKey += "/";
			repositoryKey += id;
		}

		if (SystemConfig.isUseSchema() == false)
			return null;

		ApiSettingModel info = null;

		if (SystemConfig.isOnlyDataSchemaRepository()) {
			try {
				ApiSettingInfo repositoryInfo = new ApiSettingInfo(hashService, repositoryValidator, this);
				info = repositoryInfo.getApiSetting(repositoryKey, this);
				if (info == null)
					return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);
			} catch (Exception ex) {
				return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
		}

		if (info != null) {
			// Can't find ApiDifin
			ResponseEntity res = repositoryValidator.judgeUse(info, method, authorization, id);
			if (res != null)
				return res;
		}

		if (method.equals(HttpMethods.POST) || method.equals(HttpMethods.PUT)) {

			if (info != null) {
				List<Problem> problems = new ArrayList<Problem>();

				try {
					problems = repositoryValidator.validateJson(json, info.getSchema());
				} catch (JsonParsingException e) {
					return new ResponseEntity<>(gson.toJson("JsonParseError:" + e.getMessage()), new HttpHeaders(),
							HttpStatus.BAD_REQUEST);
				}

				if (problems.size() > 0) {
					List<String> problemList = problems.stream().map(x -> x.getMessage()).collect(Collectors.toList());
					return new ResponseEntity<>(gson.toJson(problemList), new HttpHeaders(), HttpStatus.BAD_REQUEST);
				}
			}
		}

		return null;
	}

	public void clearRepositoryInfoCache() {
		new ApiSettingInfo(hashService, repositoryValidator, this).clearRepositoryInfoCache();
	}

	public String[] getRepository() {
		return repository.getRepository();
	}
}
