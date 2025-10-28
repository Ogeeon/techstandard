package ru.techstandard.shared;

import ru.techstandard.client.model.ChatMsg;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("chatLoggerService")
public interface ChatLoggerService extends RemoteService {
	PagingLoadResult<ChatMsg> getChatMessages(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException;
}
