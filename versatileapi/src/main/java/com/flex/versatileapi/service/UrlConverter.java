package com.flex.versatileapi.service;

import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.flex.versatileapi.model.RepositoryUrlInfo;

@Component
public class UrlConverter {

	// 1/2/3/4 repository:1/2/3 id:4
	public RepositoryUrlInfo getRepositoryInfo(String uri) {

		String[] urlArray = Stream.of(uri.split("/")).skip(2).toArray(String[]::new);

		String id = urlArray[urlArray.length - 1].toString();
		urlArray = Stream.of(urlArray).limit(urlArray.length - 1).toArray(String[]::new);
		String repositoryKey = String.join("/", urlArray);

		RepositoryUrlInfo info = new RepositoryUrlInfo();
		info.setId(id);
		info.setRepositoryKey(repositoryKey);
		return info;
	}

	// 1/2/3/4 repository:1 id:2/3/4
	public RepositoryUrlInfo getRepositoryInfo2(String uri) {

		String[] urlArray = Stream.of(uri.split("/")).skip(2).toArray(String[]::new);

		String repositoryKey = urlArray[0].toString();
		urlArray = Stream.of(urlArray).skip(1).toArray(String[]::new);
		String id = String.join("/", urlArray);

		RepositoryUrlInfo info = new RepositoryUrlInfo();
		info.setId(id);
		info.setRepositoryKey(repositoryKey);
		return info;
	}
}
