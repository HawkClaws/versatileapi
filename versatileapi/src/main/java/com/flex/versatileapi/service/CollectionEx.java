package com.flex.versatileapi.service;

import java.util.ArrayList;
import java.util.Collection;

public class CollectionEx {
	public static <E> Collection<E> makeCollection(Iterable<E> iter) {
		Collection<E> list = new ArrayList<E>();
	    for (E item : iter) {
	        list.add(item);
	    }
	    return list;
	}
}
