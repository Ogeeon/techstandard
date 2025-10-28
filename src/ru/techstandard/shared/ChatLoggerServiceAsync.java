package ru.techstandard.shared;

import ru.techstandard.client.model.ChatMsg;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface ChatLoggerServiceAsync {

	void getChatMessages(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<ChatMsg>> callback);

}
