package ru.techstandard.shared;

import ru.techstandard.client.model.Request;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("requestService")
public interface RequestService extends RemoteService {
	PagingLoadResult<Request> getRequests(FilterPagingLoadConfig config);
	Request getRequest(int id);
	boolean deleteRequest(int recordID, boolean markOnly);
	int addRequest(Request request);
	boolean updateRequest(Request request);
	String getPrintableRequestList(FilterPagingLoadConfig config);
	String getPrintableRequestCard(int id);
}
