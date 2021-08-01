package com.flex.versatileapi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.springframework.core.io.ClassPathResource;

public class JsonSchemaEx {

	private static JsonValidationService jvs = JsonValidationService.newInstance();

	public static JsonSchema readJsonSchema(String adminSchemaPath) {
		JsonSchema js = null;
		try (InputStream is = new ClassPathResource(adminSchemaPath).getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			js = jvs.readSchema(br);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return js;
	}
}
