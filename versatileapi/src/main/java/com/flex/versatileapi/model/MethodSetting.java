package com.flex.versatileapi.model;

import org.springframework.http.HttpMethod;

import lombok.Data;

@Data
public class MethodSetting {
	public HttpMethod httpMethod;
	public BehaviorType behavior;
}