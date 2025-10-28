package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.ActsJournalRecord;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface ActsJournalServiceAsync {

	void getJournalRecords(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<ActsJournalRecord>> callback);

	void addJournalRecord(ActsJournalRecord record, AsyncCallback<Integer> callback);
	
	void deleteJournalRecord(int recordID, boolean markOnly, AsyncCallback<Boolean> callback);

	void updateJournalRecord(ActsJournalRecord record, AsyncCallback<Boolean> callback);

	void getPrintableJournal(FilterPagingLoadConfig config,	AsyncCallback<String> callback);

	void getPrintableActCard(int id, AsyncCallback<String> callback);

	void getJournalRecordsByContract(int id,
			AsyncCallback<List<ActsJournalRecord>> callback);

	void getJournalRecord(int id, AsyncCallback<ActsJournalRecord> callback);

}
