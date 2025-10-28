package ru.techstandard.shared;

import ru.techstandard.client.model.Event;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("eventService")
public interface EventService extends RemoteService {
	PagingLoadResult<Event> getEvents(int employeeId, PagingLoadConfig config);
	void addEvent(Event event);
	void removeEvent(int id);
	int getEventsCount(int employeeId);
}
