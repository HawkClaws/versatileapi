package com.flex.versatileapi.model;

import org.leadpony.justify.api.JsonSchema;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RepositoryInfo {
	public final JsonSchema jsonSchema;
	public final String repositorySecret;
	public final MethodSetting[] methodSettings;
}
