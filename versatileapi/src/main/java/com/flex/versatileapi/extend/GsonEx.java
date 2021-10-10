package com.flex.versatileapi.extend;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

@Component
public class GsonEx {
	public Gson g = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
		@Override
		public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
			Integer value = (int) Math.round(src);
			return new JsonPrimitive(value);
		}
	}).create();

	public Map<String, Object> toMap(String json) {
		Map<String, Object> value = g.fromJson(json, Map.class);

		return value;

	}

	public List<Map<String, Object>> toMapList(String json) {
		Type listType = new TypeToken<List<Map<String, Object>>>() {
		}.getType();

		List<Map<String, Object>> values = g.fromJson(json, listType);

		return values;

	}
}
