package com.flex.versatileapi.controller;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.config.DBName;
import com.flex.versatileapi.model.RepositoryUrlInfo;
import com.flex.versatileapi.repository.DataStoreRepository;
import com.flex.versatileapi.service.Logging;
import com.flex.versatileapi.service.UrlConverter;
import com.flex.versatileapi.service.VersatileService;

@RestController
public class VersatileController {

	@Autowired
	private VersatileService versatileService;

	@Autowired
	private Logging logging;

	@Autowired
	private UrlConverter urlConverter;

	@Autowired
	private DataStoreRepository dataStoreRepository;
	
	/**
	 * versatileApiの実行
	 */
//	@CrossOrigin()
	@RequestMapping(value = "/api/**")
	public ResponseEntity<Object> versatileApi(HttpServletRequest request)
			throws IOException, InterruptedException, ExecutionException {
		// URLからリソース（DBテーブル）やリソースのID特定
		RepositoryUrlInfo info = urlConverter.getRepositoryInfo(request.getRequestURI());
		String method = request.getMethod();
		String body = request.getReader().lines().collect(Collectors.joining("\r\n"));

		logging.writeLog(request, body);

		return versatileService.execute(info.getRepositoryKey(), info.getId(), method, body,
				request.getHeader(ConstData.AUTHORIZATION), request.getRemoteAddr(), request.getQueryString(),
				dataStoreRepository);
	}
	
	
	/**
	 * authGroupにユーザーを追加（Api認証を使用）
	 */
	@RequestMapping(value = "/auth_group_user/**")
	public ResponseEntity<Object> authGroupUser(HttpServletRequest request){
		return null;
	}
}
