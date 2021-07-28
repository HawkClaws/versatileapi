package com.flex.versatileapi.model;

import org.leadpony.justify.api.JsonSchema;
import org.springframework.http.HttpMethod;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * API設定設定
 */
@Data
@RequiredArgsConstructor
public class ApiSettingModel {
	//ApiのURL
	String apiUrl;
	
	//Api認証用のシークレット
	String apiSecret;
	
	//AuthGroupのキー（先にAuthGroupを登録する必要がある）
	String authGroupKey;
	
	//DB保存用
	Object jsonSchema;
	
	//バリデーション用
	JsonSchema schema;
	
	//各メソッドの許可・認証などの設定
	MethodSetting[] methodSettings;
}
