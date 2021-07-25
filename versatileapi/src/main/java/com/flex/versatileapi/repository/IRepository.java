package com.flex.versatileapi.repository;

import java.util.List;
import java.util.Map;

import com.flex.versatileapi.model.QueryModel;

public interface IRepository {

	public Map<String, String> insert(String repositoryKey, String id, Map<String, Object> value);

	public Map<String, String> update(String repositoryKey, String id, Map<String, Object> value);

	public Object get(String repositoryKey, String id);

	public Object getCount(String repositoryKey);

	public List<Object> getAll(String repositoryKey, List<QueryModel> queries);

	public Map<String, String> delete(String repositoryKey, String id);

	public Map<String, String> deleteAll(String repositoryKey);

	public List<String> getIds(String repositoryKey);

	public String[] getRepository();
}
