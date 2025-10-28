package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Attachement;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("attachementService")
public interface AttachementService extends RemoteService {
	List<Attachement> getAllAttachements(int clientId);
	PagingLoadResult<Attachement> getAttachements(FilterPagingLoadConfig config);
	void deleteAttachments(List<Integer> attachmentList);
	void deleteAttachments(List<Integer> attachmentList, boolean markOnly);
	void deleteFile(int attachementId);
	boolean updateAttachment(Attachement attach);
}
