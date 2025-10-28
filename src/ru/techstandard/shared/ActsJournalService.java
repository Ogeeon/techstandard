package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.ActsJournalRecord;
import ru.techstandard.shared.NotAuthorizedException;
import ru.techstandard.shared.NotLoggedInException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("actsJournalService")
public interface ActsJournalService extends RemoteService {
	PagingLoadResult<ActsJournalRecord> getJournalRecords(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException;
	List<ActsJournalRecord> getJournalRecordsByContract(int id);
	ActsJournalRecord getJournalRecord(int id);
	int addJournalRecord(ActsJournalRecord record) throws NotLoggedInException, NotAuthorizedException;
	boolean deleteJournalRecord(int recordID, boolean markOnly) throws NotLoggedInException, NotAuthorizedException;
	boolean updateJournalRecord(ActsJournalRecord record) throws NotLoggedInException, NotAuthorizedException;
	String getPrintableJournal(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException;
	String getPrintableActCard(int id) throws NotLoggedInException, NotAuthorizedException;
}
