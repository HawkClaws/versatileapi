package com.flex.versatileapi.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.NumberUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.versatileapi.config.SystemConfig;
import com.flex.versatileapi.exceptions.DBWriteException;
import com.flex.versatileapi.extend.CollectionEx;
import com.flex.versatileapi.model.QueryModel;
import com.flex.versatileapi.service.ODataMongoConverter;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;

@Configurable
public class MongoRepository implements IRepository {

//	private MongoClient mongoClient = null;

	private static ConcurrentHashMap<String, MongoDatabase> mongoDbList = new ConcurrentHashMap<String, MongoDatabase>();

	private ODataMongoConverter oDataMongoConverter = new ODataMongoConverter();

	private MongoDatabase db = null;

	public MongoRepository(String dbName) {
//		ConnectionString connectionString = new ConnectionString(SystemConfig.getMongoConnectionString());
//		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
//				.build();
//		MongoClient client = MongoClients.create(settings);
		this.db = getDatabase(dbName);
	}

	private MongoDatabase getDatabase(String dbName) {
		if (mongoDbList.containsKey(dbName)) {
			return mongoDbList.get(dbName);
		} else {
			ConnectionString connectionString = new ConnectionString(SystemConfig.getMongoConnectionString());
			MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
					.build();
			MongoClient client = MongoClients.create(settings);
			MongoDatabase dbTemp = client.getDatabase(dbName);
			mongoDbList.put(dbName, dbTemp);
			return dbTemp;
		}
	}

	@Override
	public Map<String, String> insert(String repository, String id, Map<String, Object> value) {

		try {
			MongoCollection<Document> docs = db.getCollection(repository);

			Document doc = new Document(value);
			docs.insertOne(doc);

			Map<String, String> res = new HashMap<String, String>();
			res.put("id", id);
			return res;
		} catch (MongoWriteException mwe) {
			throw new DBWriteException(mwe.getMessage());
		}

	}

	@Override
	public Map<String, List<String>> insertAll(String repositoryKey, Map<String, Map<String, Object>> idValues) {
		try {
			MongoCollection<Document> docs = db.getCollection(repositoryKey);

			List<Document> documents = idValues.values().stream().map(x -> new Document(x))
					.collect(Collectors.toList());
			docs.insertMany(documents);

			Map<String, List<String>> res = new HashMap<String, List<String>>();
			res.put("ids", idValues.keySet().stream().map(x -> x).collect(Collectors.toList()));

			return res;
		} catch (MongoWriteException mwe) {
			throw new DBWriteException(mwe.getMessage());
		}
	}

	@Override
	public Map<String, String> update(String repository, String id, Map<String, Object> value) {
		MongoCollection<Document> docs = db.getCollection(repository);

		// update??????????????????
		Document update = new Document();
		update.append("$set", value);

		// ????????????
		Document filter = new Document();
		filter.append("id", id);

		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		// ??????
		docs.updateOne(filter, update, options);

		Map<String, String> res = new HashMap<String, String>();
		res.put("id", id);

		return res;
	}

	@Override
	public Map<String, String> updateAll(String repositoryKey, Map<String, Map<String, Object>> idValues) {
		// TODO ?????????????????????????????????????????????
		return null;
	}

	@Override
	public void createIndex(String repositoryKey) {
		try {
			db.createCollection(repositoryKey);
		} catch (MongoCommandException me) {
		}

		MongoCollection<Document> docs = db.getCollection(repositoryKey);

		// Index?????????
		boolean existsIndex = false;
		boolean existsUnique = false;
		for (Document doc : docs.listIndexes()) {
			Map<String, Object> data = new ObjectMapper().convertValue(doc.get("key"), Map.class);
			if (data.containsKey("$**")) {
				existsIndex = true;
			}
			if (data.containsKey("id")) {
				existsUnique = true;
			}
		}
		if (existsIndex == false) {
			docs.createIndex(new Document("$**", 1));
		}
		if (existsUnique == false) {
			IndexOptions indexOptions = new IndexOptions().unique(true);
			docs.createIndex(Indexes.ascending("id"), indexOptions);
		}
	}

	@Override
	public Object get(String repositoryKey, String id) {

		MongoCollection<Document> docs = db.getCollection(repositoryKey);
		Document doc = docs.find(Filters.eq("id", id)).first();

		if (doc == null) {
			return null;
		}

		doc.remove("_id");
//		doc.remove("id");
		return doc;
	}

	@Override
	public List<Object> getAll(String repositoryKey, List<QueryModel> queries) {
		Bson filter = new Document();
		Document sort = new Document();
		int limit = 0;
		int skip = 0;
		for (QueryModel query : queries) {
			switch (query.getQueryType()) {
			case FILTER:
				filter = oDataMongoConverter.createFilter(query);
				break;

			case ORDERBY:
				sort = oDataMongoConverter.createSort(query);
				break;

			case SKIP:
				skip = NumberUtils.parseNumber(query.getValue(), Integer.class);
				break;

			case TOP:
			case LIMIT:
				limit = NumberUtils.parseNumber(query.getValue(), Integer.class);
				break;
			}
		}

		FindIterable<Document> docs = db.getCollection(repositoryKey).find(filter).sort(sort);

		if (limit > 0)
			docs.limit(limit);

		if (skip > 0)
			docs.skip(skip);

		List<Object> results = new ArrayList<Object>();
		for (Document doc : docs) {

			doc.remove("_id");
//			doc.remove("id");
			results.add(doc);
		}

		if (results.size() == 0) {
			return null;
		}

		return results;
	}

	@Override
	public Object getCount(String repositoryKey) {
		long count = db.getCollection(repositoryKey).count();
		Map<String, Long> res = new HashMap<String, Long>();
		res.put("count", count);
		return res;
	}

	@Override
	public Map<String, String> delete(String repositoryKey, String id) {
		MongoCollection<Document> docs = db.getCollection(repositoryKey);
		docs.deleteOne(Filters.eq("id", id));

		Map<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		return res;
	}

	public Map<String, String> deleteAll(String repositoryKey) {
		MongoCollection<Document> docs = db.getCollection(repositoryKey);
		docs.deleteMany(new Document());

		Map<String, String> res = new HashMap<String, String>();
		res.put("repositoryKey", repositoryKey);
		return res;
	}

	public String[] getRepository() {
		return CollectionEx.makeCollection(db.listCollectionNames()).toArray(new String[] {});
	}

	@Override
	public List<String> getIds(String repositoryKey) {
		FindIterable<Document> docs = db.getCollection(repositoryKey).find();
		List<String> results = new ArrayList<String>();
		for (Document doc : docs) {
			results.add(doc.keySet().toArray()[0].toString());
		}

		if (results.size() == 0) {
			return null;
		}

		return results;
	}

}
