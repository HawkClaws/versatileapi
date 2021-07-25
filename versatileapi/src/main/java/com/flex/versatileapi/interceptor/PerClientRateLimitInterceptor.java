package com.flex.versatileapi.interceptor;

import java.io.BufferedReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import com.flex.versatileapi.config.ConstData;
import com.flex.versatileapi.repository.RealtimeDatabaseRepotitory;
import com.google.api.client.http.HttpMethods;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

public class PerClientRateLimitInterceptor implements HandlerInterceptor {

	@Autowired
	RealtimeDatabaseRepotitory realtimeDatabaseRepotitory;

	// 暫定1分に１２０回まで
	private Integer GET_LIMIT = 120;
	private Integer POST_LIMIT = 10;
	

	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

		String key = request.getRemoteAddr() + request.getHeader("REFERER");

		Bucket requestBucket = null;

		if (request.getMethod().toUpperCase().equals(HttpMethods.GET)) {
			requestBucket = this.buckets.computeIfAbsent(key, func -> getBucket());
		} else {
			key += HttpMethods.POST;
			requestBucket = this.buckets.computeIfAbsent(key, func -> postBucket());
		}

		ConsumptionProbe probe = requestBucket.tryConsumeAndReturnRemaining(1);

		writeLog(request, probe.getRemainingTokens(), key);

		if (probe.isConsumed()) {
			response.addHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
			return true;
		}

		// 429
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.addHeader("X-Rate-Limit-Retry-After-Milliseconds",
				Long.toString(TimeUnit.NANOSECONDS.toMillis(probe.getNanosToWaitForRefill())));
		return false;
	}

	private Bucket getBucket() {
		return Bucket4j.builder()
				.addLimit(Bandwidth.classic(GET_LIMIT, Refill.intervally(GET_LIMIT, Duration.ofMinutes(1)))).build();
	}

	private Bucket postBucket() {
		return Bucket4j.builder()
				.addLimit(Bandwidth.classic(POST_LIMIT, Refill.intervally(POST_LIMIT, Duration.ofMinutes(1)))).build();
	}

	private void writeLog(HttpServletRequest request, long remaining, String key) {
		try {
			String ip = request.getRemoteAddr();

//		// テストはログ出力しない
//		if (ip.equals("127.0.0.1"))
//			return;

			Date d = new Date();
			SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
			String c1 = d1.format(d);

			SimpleDateFormat d2 = new SimpleDateFormat("HH:mm:ss");
			String c2 = d2.format(d);

			Map<String, Object> log = new HashMap<String, Object>();
			String url = request.getRequestURI();
			if(request.getQueryString() != null)
				url += URLDecoder.decode(request.getQueryString(), "UTF-8");
			log.put("uri", url);
			log.put("method", request.getMethod());
			log.put("referer", request.getHeader("REFERER"));
			log.put("ip", request.getRemoteAddr());
			log.put("key", key);
			log.put("remaining", remaining);

			realtimeDatabaseRepotitory.insert("rate-log/" + c1 + "/" + request.getMethod(), c2, log);
		} catch (Exception e) {

		}
	}

}