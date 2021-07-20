package com.flex.versatileapi.service;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.model.RepositoryModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RepositoryInfo {	

	Gson gson = new GsonBuilder().
	        registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {   
	            @Override
	            public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
	                Integer value = (int)Math.round(src);
	                return new JsonPrimitive(value);
	            }
	         }).create();
	
	JsonValidationService jvs = JsonValidationService.newInstance();

	private HashService hashService;

	private RepositoryValidator repositoryValidator;
	
	private VersatileService versatileService;
	
	
	public RepositoryInfo(HashService hashService,RepositoryValidator repositoryValidator,VersatileService versatileService) {
		this.hashService = hashService;
		this.repositoryValidator = repositoryValidator;
		this.versatileService = versatileService;
	}	
	
	
	public List<Problem> validate(String body) {
		
		return repositoryValidator.validateJson(body, jvs.readSchema(new StringReader(repositorySchema)));
	}

	public Object create(String method, Object body) {

		RepositoryModel repoMpdel = gson.fromJson(body.toString(), RepositoryModel.class);
		
		JsonSchema js = jvs.readSchema(new StringReader(gson.toJson(repoMpdel.getJsonSchema())));
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put(ConstData.JSON_SCHEMA, gson.toJson(repoMpdel.getJsonSchema()));
		res.put(ConstData.ALLOW_METHODS, repoMpdel.getMethodSettings());
		
//		if(StringUtils.hasLength(repoMpdel.getRepositorySecret()))
		res.put(ConstData.REPOSITORY_SECRET, hashService.generateHashPassword(repoMpdel.getApiSecret()));
		
		// 本当はApiSettingはDB分けたい
		return versatileService.post(repoMpdel.getApiUrl(), ConstData.JSON_SCHEMA, "", gson.toJson(res), "");
	}
	
	private final String repositorySchema ="{\r\n"
			+ "  \"additionalProperties\": false,\r\n"
			+ "  \"type\": \"object\",\r\n"
			+ "  \"properties\": {\r\n"
			+ "    \"apiUrl\": {\r\n"
			+ "      \"type\": \"string\"\r\n"
			+ "    },\r\n"
			+ "    \"apiSecret\": {\r\n"
			+ "      \"type\": \"string\"\r\n"
			+ "    },\r\n"
			+ "    \"jsonSchema\": {\r\n"
			+ "      \"type\": \"object\"\r\n"
			+ "    },\r\n"
			+ "    \"methodSettings\": {\r\n"
			+ "      \"type\": \"array\",\r\n"
			+ "      \"items\": [\r\n"
			+ "        {\r\n"
			+ "          \"type\": \"object\",\r\n"
			+ "          \"properties\": {\r\n"
			+ "            \"httpMethod\": {\r\n"
			+ "              \"type\": \"string\",\r\n"
			+ "              \"enum\": [\"GET\",\"POST\",\"PUT\",\"DELETE\"]\r\n"
			+ "            },\r\n"
			+ "            \"behavior\": {\r\n"
			+ "              \"type\": \"string\",\r\n"
			+ "              \"enum\": [\"Allow\",\"Authorization\",\"NotImplemented\",\"IptoId\"]\r\n"
			+ "            }\r\n"
			+ "          },\r\n"
			+ "          \"required\": [\r\n"
			+ "            \"httpMethod\",\r\n"
			+ "            \"behavior\"\r\n"
			+ "          ]\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  },\r\n"
			+ "  \"required\": [\r\n"
			+ "    \"apiUrl\",\r\n"
			+ "    \"apiSecret\",\r\n"
			+ "    \"jsonSchema\",\r\n"
			+ "    \"methodSettings\"\r\n"
			+ "  ]\r\n"
			+ "}";
}
