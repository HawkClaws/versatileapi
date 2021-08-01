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

	//キャッシュ用　TODO上限？
	protected static ConcurrentHashMap<String, ApiSettingModel> apiSettingMap = new ConcurrentHashMap<String, ApiSettingModel>();

	private JsonSchema apiSettingSchema;
	
	public ApiSettingInfo() {
		this.apiSettingSchema = JsonSchemaEx.readJsonSchema("AdminSchema/ApiSettingSchema.json");
	}
	
	public List<Problem> validate(String body) {
		return repositoryValidator.validateJson(body, this.apiSettingSchema);
	}

	public ApiSettingModel toModel(String apiSettingStr) {
		ApiSettingModel repoMpdel = gson.fromJson(apiSettingStr.replace("$", ""), ApiSettingModel.class);

		JsonSchema js = jvs.readSchema(new StringReader(gson.toJson(repoMpdel.getJsonSchema())));

		// APIシークレットハッシュ化
		repoMpdel.setApiSecret(hashService.generateHashPassword(repoMpdel.getApiSecret()));

		return repoMpdel;
	}

	public ApiSettingModel get(String repositoryKey, VersatileBase versatileBase) throws ODataParseException {
		if (apiSettingMap.containsKey(repositoryKey)) {
			return apiSettingMap.get(repositoryKey);
		} else {
			Object repositoryInfo = versatileBase.get(repositoryKey, ConstData.JSON_SCHEMA, "");
			if (repositoryInfo == null)
				return null;

			ApiSettingModel info = gson.fromJson(gson.toJson(repositoryInfo), ApiSettingModel.class);

			info.setSchema(jvs.readSchema(new StringReader(gson.toJson(info.getJsonSchema()))));

			apiSettingMap.put(repositoryKey, info);
			return info;
		}
	}

	public void clearCache() {
		apiSettingMap = new ConcurrentHashMap<String, ApiSettingModel>();
	}
}
