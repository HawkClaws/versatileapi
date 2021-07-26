package com.flex.versatileapi.service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.config.SystemConfig;

@Component
public class HashService {

	public String generateHashPassword(String rawPassword) {
		MessageDigest sha512 = null;
		try {
			sha512 = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		byte[] sha512_result = sha512.digest(rawPassword.getBytes());
		return String.format("%040x", new BigInteger(1, sha512_result));
	}

	public String shortGenerateHashPassword(String rawPassword) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		rawPassword  += SystemConfig.getHashKey();
		
		byte[] result = digest.digest(rawPassword.getBytes());

		return String.format("%040x", new BigInteger(1, result));
	}
}
