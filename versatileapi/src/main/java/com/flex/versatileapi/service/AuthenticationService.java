package com.flex.versatileapi.service;

import java.util.HashMap;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.MethodSetting;
//import com.flex.versatileapi.model.RepositoryInfo;
import com.google.gson.Gson;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
public class AuthenticationService {

	@Autowired
	private VersatileBase versatileBase;

	@Autowired
	private HashService hashService;

	private Gson gson = new Gson();

	// 適度にキャッシュする

	// TODO適切なExceptionを返す
	public Boolean isAllowedAuth(String userId, String password, String authGroupKey) throws ODataParseException {
		this.versatileBase.setRepositoryName(ConstData.API_SETTING_STORE);
		
		if(authUser(userId, password) == false)
			return false;

		this.versatileBase.setRepositoryName(ConstData.AUTHENTICATION_GROUP_STORE);
		return authRepositoryGroup(authGroupKey, userId);
	}


	public String createUser(String userId, String rawPassword, String ipAddress) {
		this.versatileBase.setRepositoryName(ConstData.API_SETTING_STORE);
		User user = new User();
		user.setUser_id(userId);
		user.setPassword(hashService.generateHashPassword(rawPassword));

		this.versatileBase.post(userId, ConstData.USER, "", gson.toJson(user),
				hashService.shortGenerateHashPassword(ipAddress));

		return "";
	}
	

	/**
	 *  AuthGroupを作成します
	 */
	public String createAuthGroup(String authGroupKey, String userId, String ipAddress) {
		this.versatileBase.setRepositoryName(ConstData.AUTHENTICATION_GROUP_STORE);

		Map<String, Object> authGroupUser = new HashMap<String, Object>();
		authGroupUser.put("user_id", userId);

		this.versatileBase.post(userId, authGroupKey, "",
				gson.toJson(authGroupUser), hashService.shortGenerateHashPassword(ipAddress));
		return "";
	}
	
	/**
	 *  UserをAuthGroupに追加します
	 */
	public String addAuthGroup(String authGroupKey, String userId, String ipAddress) {
		this.versatileBase.setRepositoryName(ConstData.AUTHENTICATION_GROUP_STORE);

		Map<String, Object> authGroupUser = new HashMap<String, Object>();
		authGroupUser.put("user_id", userId);

		this.versatileBase.post(userId, authGroupKey, "",
				gson.toJson(authGroupUser), hashService.shortGenerateHashPassword(ipAddress));
		return "";
	}

	private Boolean authUser(String userId, String rawPassword) throws ODataParseException {
		// ユーザー認証
		Object userObj = this.versatileBase.get(userId, ConstData.USER, "");
		if (userObj == null)
			return false;

		User user = gson.fromJson(gson.toJson(userObj), User.class);
		return user.password.equals(hashService.generateHashPassword(rawPassword));
	}

	private Boolean authRepositoryGroup(String authGroupKey, String userId) throws ODataParseException {
		// AuthGroupにユーザーが含まれているかどうか

		Object userObj = this.versatileBase.get(userId, authGroupKey, "");
		if (userObj == null)
			return false;
		
		return true;
	}
}

@Data
@RequiredArgsConstructor
class User {
	public String user_id;
	public String password;
}
