package com.flex.versatileapi.service;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.extend.GsonEx;
import com.flex.versatileapi.model.MethodSetting;
import com.flex.versatileapi.model.User;
import com.flex.versatileapi.repository.ApiSettingRepository;
import com.flex.versatileapi.repository.AuthenticationGroupRepository;
import com.flex.versatileapi.repository.UserRepository;
import com.google.gson.Gson;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
public class AuthenticationService {

	@Autowired
	private ApiSettingRepository apiSettingRepository;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthenticationGroupRepository authenticationGroupRepository;
	
	@Autowired
	private HashService hashService;
	
	@Autowired
	private RepositoryValidator repositoryValidator;
	
	private JsonValidationService jvs = JsonValidationService.newInstance();

	@Autowired
	private GsonEx gsonEx;
	
	private JsonSchema userSchema;
	private JsonSchema updateUserSchema;
	private JsonSchema authGroupSchema;
	
	
	public AuthenticationService() {
		this.userSchema = JsonSchemaEx.readJsonSchema("AdminSchema/UserSchema.json");
		this.updateUserSchema = JsonSchemaEx.readJsonSchema("AdminSchema/UpdateUserSchema.json");
		this.authGroupSchema = JsonSchemaEx.readJsonSchema("AdminSchema/AuthGroupSchema.json");
	}

	// 適度にキャッシュする

	// TODO適切なExceptionを返す
	
	/**
	 *  UserGroup認証を行う
	 */
	public Boolean isAllowedAuth(String userId, String password, String authGroupKey) throws ODataParseException {
		
		if(authUser(userId, password) == false)
			return false;

		return authRepositoryGroup(authGroupKey, userId);
	}
	
	public List<Problem> validateUser(String body) {
		return repositoryValidator.validateJson(body, this.userSchema);
	}
	
	public List<Problem> validateUpdateUser(String body) {
		return repositoryValidator.validateJson(body, this.updateUserSchema);
	}
	

	/**
	 *  Userを作成します
	 */
	public User toUser(String userId, String rawPassword, String email) {
//		this.versatileBase.setRepositoryName(DBName.USER_STORE);
		User user = new User();
		user.setUser_id(userId);
		user.setPassword(hashService.generateHashPassword(rawPassword));
		user.setEmail(email);
		return user;
	}	

	public List<Problem> validateAuthGroup(String body) {
		return repositoryValidator.validateJson(body, this.authGroupSchema);
	}
	
	/**
	 *  AuthGroupを作成します
	 */
	public Map<String, Object> toAuthGroup(String authGroupKey, String userId, String ipAddress) {

		Map<String, Object> authGroupUser = new HashMap<String, Object>();
		authGroupUser.put("user_id", userId);

		this.authenticationGroupRepository.post(userId, authGroupKey, "",
				gsonEx.g.toJson(authGroupUser), hashService.shortGenerateHashPassword(ipAddress));
		return authGroupUser;
	}
	
	/**
	 *  UserをAuthGroupに追加します
	 */
	public String addAuthGroup(String authGroupKey, String userId, String ipAddress) {

		Map<String, Object> authGroupUser = new HashMap<String, Object>();
		authGroupUser.put("user_id", userId);

		this.authenticationGroupRepository.post(userId, authGroupKey, "",
				gsonEx.g.toJson(authGroupUser), hashService.shortGenerateHashPassword(ipAddress));
		return "";
	}

	public Boolean authUser(String userId, String rawPassword) throws ODataParseException {
		// ユーザー認証
		Object userObj = this.userRepository.get(userId, ConstData.USER, "");
		if (userObj == null)
			return false;

		User user = gsonEx.g.fromJson(gsonEx.g.toJson(userObj), User.class);
		return user.getPassword().equals(hashService.generateHashPassword(rawPassword));
	}

	public Boolean authRepositoryGroup(String authGroupKey, String userId) throws ODataParseException {
		// AuthGroupにユーザーが含まれているかどうか
		Object userObj = this.authenticationGroupRepository.get(userId, authGroupKey, "");
		if (userObj == null)
			return false;
		
		return true;
	}
	
}
	
