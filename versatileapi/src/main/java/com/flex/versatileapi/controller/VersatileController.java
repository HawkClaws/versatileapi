package com.flex.versatileapi.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.json.stream.JsonParsingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.RepositoryUrlInfo;
import com.flex.versatileapi.repository.RealtimeDatabaseRepotitory;
import com.flex.versatileapi.service.Logging;
import com.flex.versatileapi.service.UrlConverter;
import com.flex.versatileapi.service.VersatileService;
import com.google.api.client.http.HttpMethods;
import com.google.firebase.internal.Objects;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@RestController
public class VersatileController {

//	private static List<String>  IdMethods = new ArrayList<String>(Arrays.asList("GET","PUT","DELETE"));

	@Autowired
	private VersatileService versatileService;

	@Autowired
	private UrlConverter urlConverter;
	
	@Autowired
	RealtimeDatabaseRepotitory realtimeDatabaseRepotitory;

	Gson gson = new Gson();

//	@CrossOrigin()
	@RequestMapping(value = "/api/**")
	public ResponseEntity<Object> versatileApi(HttpServletRequest request)
			throws IOException, InterruptedException, ExecutionException {
		
		//URLからリソース（DBテーブル）やリソースのID特定
		RepositoryUrlInfo info = urlConverter.getRepositoryInfo(request.getRequestURI());
		String method = request.getMethod();
		String body = request.getReader().lines().collect(Collectors.joining("\r\n"));

		writeLog(request, body);

		ResponseEntity responseEntity = null;

			//JsonSchemaバリデーション・認証・許可されているメソッドをチェック
			responseEntity = versatileService.checkUseApi(info.repositoryKey, info.id, method, body,
					request.getHeader(ConstData.AUTHORIZATION));


		if (responseEntity != null)
			return responseEntity;

		Object response = null;
		//各メソッドの処理を実行
		switch (method) {
		case HttpMethods.GET:
			try {
				response = versatileService.get(info.id, info.repositoryKey, request.getQueryString());
			} catch (ODataParseException e) {
				return new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
			}
			if (response == null || Objects.equal(response, "")) {
				return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
			}

		case HttpMethods.POST:
			response = versatileService.post(info.id, info.repositoryKey, request.getQueryString(), body,
					request.getRemoteAddr());
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.CREATED);
			
		case HttpMethods.PUT:
			response = versatileService.put(info.id, info.repositoryKey, request.getQueryString(), body,
					request.getRemoteAddr());
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
			
		case HttpMethods.DELETE:
			response = versatileService.delete(info.id, info.repositoryKey, request.getQueryString());
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.NO_CONTENT);

		}
		return new ResponseEntity<>("", new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);
	}

	private void writeLog(HttpServletRequest request, String body) {
		try {
		String ip = request.getRemoteAddr();

		// テストはログ出力しない
		if (ip.equals("127.0.0.1"))
			return;

		Date d = new Date();
        SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
        String c1 = d1.format(d); 
        
        SimpleDateFormat d2 = new SimpleDateFormat("HH:mm:ss");
        String c2 = d2.format(d); 
		
		Map<String, Object> log = new HashMap<String, Object>();
		log.put("uri", request.getRequestURI());
		log.put("method", request.getMethod());
		log.put("body", body);
		log.put("authorization", request.getHeader(ConstData.AUTHORIZATION));
		log.put("referer", request.getHeader("REFERER"));
		log.put("ip", request.getRemoteAddr());
		
		
		realtimeDatabaseRepotitory.insert("log/"+c1+"/"+request.getMethod(), c2, log);
		} catch (Exception e) {

		}
	}

}
