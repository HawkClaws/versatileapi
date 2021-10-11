package com.flex.versatileapi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.exceptions.ODataParseException;
import com.flex.versatileapi.model.QueryModel;
import com.mongodb.client.model.Filters;

import lombok.Data;

@Configurable
public class ODataMongoConverter {

//	@Autowired
	ODataParser oDataParser = new ODataParser();

	// TODO適切なエラーを返す

	public Document createSort(QueryModel sortModel) {
		try {
			String[] sort = sortModel.getValue().split(" ");
			int sortNum = 0;

			switch (sort[1]) {
			case "asc":
				sortNum = 1;
				break;
			case "desc":
				sortNum = -1;
				break;
			}

			Document values = new Document();
			values.put(sort[0], sortNum);
			return values;
		} catch (Exception e) {
			throw new ODataParseException("ODataParseError:" + e.getMessage());
		}
	}

	public Bson createFilter(QueryModel filterModel) {
		try {
			final String AND = " and ";
			final String OR = " or ";

			String parentFilter = "";

			List<String> queries = new ArrayList<String>();

			if (filterModel.getValue().contains(AND)) {
				queries = Arrays.asList(filterModel.getValue().split(AND));
				parentFilter = "$and";
			} else if (filterModel.getValue().contains(OR)) {
				queries = Arrays.asList(filterModel.getValue().split(OR));
				parentFilter = "$or";
			} else {
				queries.add(filterModel.getValue());
			}

			List<Bson> filters = new ArrayList<Bson>();

			for (String queryString : queries) {
				filters.add(getFilter(queryString));
			}

			if (parentFilter.equals("") == false) {
				Document filter = new Document();
				filter.put(parentFilter, filters);
				return filter;
			}

			return filters.get(0);
		} catch (ODataParseException oe) {
			throw oe;
		} catch (Exception e) {
			throw new ODataParseException("ODataParseError:" + e.getMessage());
		}
	}

	private Bson getFilter(String queryString) {

		Bson filter = toFilter(queryString, "eq", "$eq");
		if (filter != null)
			return filter;

		filter = toFilter(queryString, "ne", "$ne");
		if (filter != null)
			return filter;

		filter = toFilter(queryString, "gt", "$gt");
		if (filter != null)
			return filter;

		filter = toFilter(queryString, "ge", "$gte");
		if (filter != null)
			return filter;

		filter = toFilter(queryString, "lt", "$lt");
		if (filter != null)
			return filter;

		filter = toFilter(queryString, "le", "$lte");
		if (filter != null)
			return filter;

		filter = toLikeFilter(queryString, "startsWith", "^%s");
		if (filter != null)
			return filter;

		filter = toLikeFilter(queryString, "endsWith", "%s$");
		if (filter != null)
			return filter;

		filter = toLikeFilter(queryString, "contains", "%s");
		if (filter != null)
			return filter;

		throw new ODataParseException("ODataParseError:" + queryString);
	}

	private Bson toLikeFilter(String queryString, String funcName, String format) {
		String regex = funcName + "\\((.*)\\)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(queryString);

		String[] args = new String[] {};
		if (m.find()) {
			args = m.group(1).split(",");
		}

		if (args.length != 2)
			return null;

		String value = "";
		if (args[1].charAt(0) == '\'' && args[1].charAt(args[1].length() - 1) == '\'') {
			value = args[1].substring(1, args[1].length() - 1);
		} else {
			return null;
		}
		value =  Pattern.quote(value);

		return Filters.regex(args[0], String.format(format, value));
	}

	private Document toFilter(String value, String splitter, String mongoComp) {
		String comparison = "";
		String[] values = new String[] {};

		String splitterStr = ' ' + splitter + ' ';
		if (value.contains(splitterStr)) {
			comparison = mongoComp;
			values = value.split(splitterStr);
		}

		if (comparison.equals(""))
			return null;

		Document doc = new Document();
		doc.put(comparison, oDataParser.toObject(values[1]));
		Document filter = new Document();
		filter.append(values[0], doc);

		return filter;
	}
}
