package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.DictionaryRecord;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface DictionaryServiceAsync {

	void addRecord(int type, String value, AsyncCallback<Integer> callback);

	void deleteRecord(int id, boolean markOnly, AsyncCallback<Boolean> callback);

	void getDictContPaged(int type, PagingLoadConfig config,
			AsyncCallback<PagingLoadResult<DictionaryRecord>> callback);

	void getDictionaryContents(int type,
			AsyncCallback<List<DictionaryRecord>> callback);

	void updateRecord(int id, String value, AsyncCallback<Boolean> callback);

	void getPrintableDictionaryContents(int type, AsyncCallback<String> callback);

	void getOrCreateRecByName(int type, String name,
			AsyncCallback<DictionaryRecord> callback);

}
