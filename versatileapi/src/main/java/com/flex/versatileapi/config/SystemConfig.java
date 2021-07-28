package com.flex.versatileapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
public class SystemConfig {

	public SystemConfig(@Value("${spring.datasource.onlyDataSchemaRepository}") boolean onlyDataSchemaRepository,
			@Value("${spring.datasource.useSchema}") boolean useSchema,
			@Value("${spring.datasource.hashKey}") String hashKey,
			@Value("${spring.datasource.adminAuthorization}") String adminAuthorization,
			@Value("${spring.datasource.mongoConnectionString}") String mongoConnectionString,
			@Value("${spring.datasource.rawAdminAuthorization}") String rawAdminAuthorization) {

		SystemConfig.onlyDataSchemaRepository = onlyDataSchemaRepository;
		SystemConfig.useSchema = useSchema;
		SystemConfig.hashKey = hashKey;
		SystemConfig.adminAuthorization = adminAuthorization;
		SystemConfig.mongoConnectionString = mongoConnectionString;
		SystemConfig.rawAdminAuthorization = rawAdminAuthorization;
	}

	@Getter
	static boolean onlyDataSchemaRepository;

	@Getter
	static boolean useSchema;

	@Getter
	static String hashKey;

	@Getter
	static String adminAuthorization;

	@Getter
	static String mongoConnectionString;
	
	@Getter
	static String rawAdminAuthorization;
}
