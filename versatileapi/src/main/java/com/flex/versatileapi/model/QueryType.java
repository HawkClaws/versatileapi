package com.flex.versatileapi.model;


public enum QueryType {
	FILTER, 
	ORDERBY, 
	TOP, 
	SKIP,
	@Deprecated
	LIMIT, //TODO ODataにlimitなんてない。Topの間違い。
}