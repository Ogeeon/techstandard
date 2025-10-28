package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.DictionaryRecord;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("dictionaryService")
public interface DictionaryService extends RemoteService {
	List<DictionaryRecord> getDictionaryContents(int type) throws IllegalArgumentException;
	PagingLoadResult<DictionaryRecord> getDictContPaged(int type, PagingLoadConfig config);
	int addRecord(int type, String value);
	boolean updateRecord(int id, String value);
	boolean deleteRecord(int id, boolean markOnly) throws IllegalArgumentException;
	String getPrintableDictionaryContents(int type);
	DictionaryRecord getOrCreateRecByName(int type, String name);
}
