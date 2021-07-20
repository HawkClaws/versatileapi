package com.flex.versatileapi.model;

import org.springframework.http.HttpMethod;

import lombok.Data;

@Data
public class RepositoryModel {
	public String apiUrl;
	public String apiSecret;
	public Object jsonSchema;
	public MethodSetting[] methodSettings;
}

