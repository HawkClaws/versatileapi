package com.flex.versatileapi.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;

import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.QueryModel;
import com.flex.versatileapi.model.QueryType;

@Component
public class ODataParser {
	public List<QueryModel> queryParser(String queryString) throws ODataParseException {
		if (queryString == null) {
			return new ArrayList<QueryModel>();
		}
		try {
			queryString = URLDecoder.decode(queryString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		if (queryString.contains("$") == false) {
			return new ArrayList<QueryModel>();
		}

		List<QueryModel> queryModels = new ArrayList<QueryModel>();
		for (String query : queryString.split("\\&")) {
			
			if (query == "" || query.contains("$") == false)
				continue;

			String[] splited = query.split("=");
			if(splited.length != 2)
				throw new ODataParseException(query);
			QueryModel queryModel = new QueryModel();

			queryModel.setQueryType(QueryType.valueOf(splited[0].toUpperCase().replace("$", "")));
			queryModel.setValue(splited[1]);
			queryModels.add(queryModel);
		}

		return queryModels;
	}

	public Object toObject(String objectString) {

		if (objectString.charAt(0) == '\'' && objectString.charAt(objectString.length() - 1) == '\'') {
			String strValue = objectString.substring(1, objectString.length() - 1);
			try {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
				return df.parse(strValue);
			} catch (Exception ex) {
				return strValue;
			}
		}

		try {
			return NumberUtils.parseNumber(objectString, Long.class);
		} catch (Exception ex) {

		}

		switch (objectString) {
		case "true":
			return true;
		case "false":
			return false;
		}

		return null;
	}
}
