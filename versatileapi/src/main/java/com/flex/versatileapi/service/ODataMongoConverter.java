package com.flex.versatileapi.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flex.versatileapi.model.QueryModel;

import lombok.Data;

@Component
public class ODataMongoConverter {

	@Autowired
	ODataParser oDataParser;

	// TODO適切なエラーを返す

	public Document createSort(QueryModel sortModel) {
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
	}

	public Document createFilter(QueryModel filterModel) {
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

		List<Document> filters = new ArrayList<Document>();

		for (String queryString : queries) {

			Document values = new Document();
			ComparisonModel comparison = getComparison(queryString);

			values.put(comparison.getComparison(), oDataParser.toObject(comparison.getValues()[1]));
			Document filter = new Document();
			filter.append(comparison.getValues()[0], values);
			filters.add(filter);
		}

		if (parentFilter.equals("") == false) {
			Document filter = new Document();
			filter.put(parentFilter, filters);
			return filter;
		}

		return filters.get(0);
	}

	private ComparisonModel getComparison(String comparison) {

		ComparisonModel comp = new ComparisonModel(comparison, "eq", "$eq");
		if (comp.getComparison() != null)
			return comp;

		comp = new ComparisonModel(comparison, "ne", "$ne");
		if (comp.getComparison() != null)
			return comp;

		comp = new ComparisonModel(comparison, "gt", "$gt");
		if (comp.getComparison() != null)
			return comp;

		comp = new ComparisonModel(comparison, "ge", "$gte");
		if (comp.getComparison() != null)
			return comp;

		comp = new ComparisonModel(comparison, "lt", "$lt");
		if (comp.getComparison() != null)
			return comp;

		comp = new ComparisonModel(comparison, "le", "$lte");
		if (comp.getComparison() != null)
			return comp;

		return null;// 適切なException
	}

}

@Data
class ComparisonModel {
	public String comparison;
	public String[] values;

	public ComparisonModel(String value, String splitter, String mongoComp) {

		String splitterStr = ' ' + splitter + ' ';
		if (value.contains(splitterStr)) {
			setComparison(mongoComp);
			setValues(value.split(splitterStr));
		}
	}
}
