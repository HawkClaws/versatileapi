package com.flex.versatileapi.interceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

public class PerClientRateLimitInterceptor implements HandlerInterceptor {

	//暫定1分に１２０回まで
	private Integer rateLimit = 120;

	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

		String key = request.getRemoteAddr() + request.getHeader("REFERER");

		Bucket requestBucket = this.buckets.computeIfAbsent(key, func -> createBucket());

		ConsumptionProbe probe = requestBucket.tryConsumeAndReturnRemaining(1);
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

	private Bucket createBucket() {
		return Bucket4j.builder()
				.addLimit(Bandwidth.classic(rateLimit, Refill.intervally(rateLimit, Duration.ofMinutes(1))))
				.build();
	}

}