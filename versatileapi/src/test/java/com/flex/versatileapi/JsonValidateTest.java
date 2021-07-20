package com.flex.versatileapi;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.flex.versatileapi.repository.RealtimeDatabaseRepotitory;
import com.flex.versatileapi.service.HashService;
//import com.flex.versatileapi.service.JsonValidate;
import com.flex.versatileapi.service.RepositoryValidator;

@SpringBootTest
public class JsonValidateTest {
	@Autowired
	RepositoryValidator repositoryValidator;
	protected JsonValidationService jvs = JsonValidationService.newInstance();
	
//	@Autowired
//	RealtimeDatabaseRepotitory realtimeDatabaseRepotitory;
	
	@Value("${spring.datasource.hashKey}")
	private String hashKey;

	@Test
	void test() throws NoSuchAlgorithmException {
		
        Date d = new Date();
        System.out.println(d); 

        SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
        String c1 = d1.format(new Date()); 
        System.out.println(c1); 
		
//		Map<String,Object>data = new HashMap<String,Object>();
//		data.put("key", "aaa");
//		realtimeDatabaseRepotitory.insert("repo","test",data);
		
//		Object get= realtimeDatabaseRepotitory.get("repo","test");
//		realtimeDatabaseRepotitory.deleteAll("log");
//		realtimeDatabaseRepotitory.deleteAll("repo");
		System.out.println("SHA-512：" + new HashService().generateHashPassword("test"));

		System.out.println("SHA-1：" + new HashService().shortGenerateHashPassword(hashKey + "aaaaa"));
	}

//	@Test
	void JsonValidate登録_正常() {

		String jsonSchema = "{    'type': 'object',    'properties': {        'name': {            'type': 'string'        },        'age': {            'type': 'integer',            'minimum': 0        },        'hobbies': {            'type': 'array',            'items': {                'type': 'string'            }        }    },    'required': ['name']}";
		jsonSchema = jsonSchema.replace("'", "\"");
//		ｊsonSchemaValidator.registerSchema("testSchema", jsonSchema);

		String json = "{    'age': -1,    'hobbies': ['野球', '柔道'] , 'selected':'bird'}";
		json = json.replace("'", "\"");

		jsonSchema = "  {    'type': 'object',    'properties': {  'selected': {      'type': 'string',      'enum': [        'dog',        'cat',        'monkey'      ]    }}}";
		jsonSchema = jsonSchema.replace("'", "\"");

		JsonSchema js = jvs.readSchema(new StringReader(jsonSchema));

		List<Problem> problems = repositoryValidator.validateJson(json, js);

		System.out.println(problems);

//		List<Problem> problems = ｊsonSchemaValidator.validate(json,  ｊsonSchemaValidator.getSchema("testSchema"));
//		List<Problem> problems2 = ｊsonSchemaValidator.validate(json, ｊsonSchemaValidator.getSchema("testSchema"));
//		System.out.println(problems);
	}
}
