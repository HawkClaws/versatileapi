package com.flex.versatileapi.repository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.flex.versatileapi.model.QueryModel;
import com.google.api.core.SettableApiFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Service
public class RealtimeDatabaseRepotitory implements IRepository {

	@Value("${spring.datasource.firebaseJsonFileName}")
	private String firebaseJsonFileName;

	@Value("${spring.datasource.realtimeDatabaseUrl}")
	private String realtimeDatabaseUrl;

	public RealtimeDatabaseRepotitory() {

	}

	private void FirebaseInit() {
		InputStream stream_json = null;
		try {
			stream_json = new FileInputStream(firebaseJsonFileName);
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		FirebaseOptions options = new FirebaseOptions.Builder().setServiceAccount(stream_json)
				.setDatabaseUrl(realtimeDatabaseUrl).build();
//		FirebaseApp firebaseApp;

		List<FirebaseApp> apps = FirebaseApp.getApps();
		if (apps.size() == 0) {
			FirebaseApp.initializeApp(options);
		} else {
//			firebaseApp = apps.get(0);
		}
	}

	private DatabaseReference DatabaseReference(String repositoryKey) {

		FirebaseInit();

		DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

		DatabaseReference usersRef = ref.child(repositoryKey);
		return usersRef;
	}

	public Map<String, String> update(String repositoryKey, String id, Map<String, Object> value) {
		return insert(repositoryKey, id, value);
	}

	public Map<String, String> insert(String repositoryKey, String id, Map<String, Object> value) {
		DatabaseReference ref = DatabaseReference(repositoryKey);

		ref = ref.child(id);

//		try {
//			upsertCommon(ref, value);
//		} catch (InterruptedException | ExecutionException e) {
//			// TODO 自動生成された catch ブロック
//			e.printStackTrace();
//		}
		asyncUpsertCommon(ref, value);
		Map<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		return res;
	}

	public Map<String, String> delete(String repositoryKey, String id) {
		DatabaseReference ref = DatabaseReference(repositoryKey);

		ref = ref.child(id);

		try {
			upsertCommon(ref, null);
		} catch (InterruptedException | ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		Map<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		return res;
	}

	@Override
	public Map<String, String> deleteAll(String repositoryKey) {
		DatabaseReference ref = DatabaseReference(repositoryKey);

		try {
			upsertCommon(ref, null);
		} catch (InterruptedException | ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		Map<String, String> res = new HashMap<String, String>();
		res.put("repositoryKey", repositoryKey);
		return res;
	}
	
	
	//非同期
	private void asyncUpsertCommon(DatabaseReference ref, Object value) {
		ref.setValue(value);
	}
	
	
	private void upsertCommon(DatabaseReference ref, Object value) throws InterruptedException, ExecutionException {
		final SettableApiFuture<Void> future = SettableApiFuture.create();
		ref.setValue(value, new DatabaseReference.CompletionListener() {
			@Override
			public void onComplete(DatabaseError error, DatabaseReference ref) {
				if (error != null) {
					future.setException(error.toException());
				} else {
					future.set(null);
				}
			}
		});

		future.get();
	}

	public Object get(String repositoryKey, String id) {
		DatabaseReference ref = DatabaseReference(repositoryKey);

		ref = ref.child(id);

		return getCommon(ref).getValue();
	}

	public List<Object> getAll(String repositoryKey,List<QueryModel> queries) {
		DatabaseReference ref = DatabaseReference(repositoryKey);
		List<Object> res = new ArrayList<Object>();
		for (DataSnapshot snap : getCommon(ref).getChildren()) {
			res.add(snap.getValue());
		}
		return res;
	}

	private DataSnapshot getCommon(DatabaseReference ref) {
		final SettableApiFuture<DataSnapshot> future = SettableApiFuture.create();
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				future.set(dataSnapshot);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				future.setException(databaseError.toException());
			}
		});
		try {
			DataSnapshot data = future.get();
//			if (data == null) {
//				data = "";
//			}
			return data;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> getIds(String repositoryKey) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String[] getRepository() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Object getCount(String repositoryKey) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
