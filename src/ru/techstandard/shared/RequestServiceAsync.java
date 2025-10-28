package ru.techstandard.shared;

import ru.techstandard.client.model.Request;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface RequestServiceAsync {

	void addRequest(Request request, AsyncCallback<Integer> callback);

	void deleteRequest(int recordID, boolean markOnly, AsyncCallback<Boolean> callback);

	void getPrintableRequestCard(int id, AsyncCallback<String> callback);

	void getPrintableRequestList(FilterPagingLoadConfig config, AsyncCallback<String> callback);

	void getRequests(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<Request>> callback);

	void updateRequest(Request request, AsyncCallback<Boolean> callback);

	void getRequest(int id, AsyncCallback<Request> callback);

}
