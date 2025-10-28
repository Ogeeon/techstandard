package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Attachement;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface AttachementServiceAsync {

	void getAllAttachements(int clientId, AsyncCallback<List<Attachement>> callback);

	void getAttachements(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<Attachement>> callback);

	void updateAttachment(Attachement attach, AsyncCallback<Boolean> callback);

	void deleteAttachments(List<Integer> attachmentList, AsyncCallback<Void> callback);
	void deleteAttachments(List<Integer> attachmentList, boolean markOnly, AsyncCallback<Void> callback);

	void deleteFile(int attachementId, AsyncCallback<Void> callback);


}
