package com.flex.versatileapi.service;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.ApiSettingModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@Component
public class ApiSettingInfo {

	Gson gson = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
		@Override
		public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
			Integer value = (int) Math.round(src);
			return new JsonPrimitive(value);
		}
	}).create();

	private JsonValidationService jvs = JsonValidationService.newInstance();

	@Autowired
	private HashService hashService;

	@Autowired
	private RepositoryValidator repositoryValidator;

	@Autowired
	private VersatileBase versatileBase;

	protected static ConcurrentHashMap<String, ApiSettingModel> repositoryInfoMap = new ConcurrentHashMap<String, ApiSettingModel>();

//	public ApiSettingInfo(HashService hashService, RepositoryValidator repositoryValidator,
//			VersatileBase versatileBase) {
//		this.hashService = hashService;
//		this.repositoryValidator = repositoryValidator;
//		this.versatileBase = versatileBase;
//	}

	public List<Problem> validate(String body) {

		return repositoryValidator.validateJson(body, jvs.readSchema(new StringReader(repositorySchema)));
	}

	public ApiSettingModel toApiSettingModel(String apiSettingStr) {
		ApiSettingModel repoMpdel = gson.fromJson(apiSettingStr.replace("$", ""), ApiSettingModel.class);

		JsonSchema js = jvs.readSchema(new StringReader(gson.toJson(repoMpdel.getJsonSchema())));

		// APIシークレットハッシュ化
		repoMpdel.setApiSecret(hashService.generateHashPassword(repoMpdel.getApiSecret()));

		return repoMpdel;
	}

	public Object create(ApiSettingModel apiSetting) {

		return versatileBase.post(apiSetting.getApiUrl(), ConstData.JSON_SCHEMA, "", gson.toJson(apiSetting), "");
	}

	public ApiSettingModel getApiSetting(String repositoryKey, VersatileBase versatileBase) throws ODataParseException {
		if (repositoryInfoMap.containsKey(repositoryKey)) {
			return repositoryInfoMap.get(repositoryKey);
		} else {
			Object repositoryInfo = versatileBase.get(repositoryKey, ConstData.JSON_SCHEMA, "");
			if (repositoryInfo == null)
				return null;

			ApiSettingModel info = gson.fromJson(gson.toJson(repositoryInfo), ApiSettingModel.class);

			info.setSchema(jvs.readSchema(new StringReader(gson.toJson(info.getJsonSchema()))));

			repositoryInfoMap.put(repositoryKey, info);
			return info;
		}
	}

	public void clearRepositoryInfoCache() {
		repositoryInfoMap = new ConcurrentHashMap<String, ApiSettingModel>();
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
