package ru.techstandard.shared;

import ru.techstandard.client.model.Event;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface EventServiceAsync {

	void addEvent(Event event, AsyncCallback<Void> callback);

	void getEvents(int employeeId, PagingLoadConfig config, AsyncCallback<PagingLoadResult<Event>> callback);

	void removeEvent(int id, AsyncCallback<Void> callback);
	
	void getEventsCount(int id, AsyncCallback<Integer> callback);

}
