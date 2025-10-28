package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.AccessGroup;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AccessGroupServiceAsync {


	void getAccessGroups(AsyncCallback<List<AccessGroup>> callback);

	void addAccessGroup(AccessGroup group, AsyncCallback<Integer> callback);

	void updateAccessGroup(AccessGroup group, boolean skipAccessRights, AsyncCallback<Boolean> callback);

	void deleteAccessGroup(int id, AsyncCallback<Boolean> callback);

}
