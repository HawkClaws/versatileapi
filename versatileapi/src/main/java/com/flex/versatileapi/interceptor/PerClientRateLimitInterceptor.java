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
import com.flex.versatileapi.service.Logging;
import com.google.api.client.http.HttpMethods;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

public class PerClientRateLimitInterceptor implements HandlerInterceptor {

	@Autowired
	private Logging logging;

	// 暫定1分に１２０回まで
	private Integer GET_LIMIT = 120;
	private Integer POST_LIMIT = 100;
	

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

		logging.writeLog(request, probe.getRemainingTokens(), key);

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
}