package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.DeletedObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DeletedObjectsServiceAsync {

	void getDeletedObjects(AsyncCallback<List<DeletedObject>> callback);

	void applyObjectDelete(String table, int id, AsyncCallback<Boolean> callback);

	void undoObjectDelete(String table, int id, int userId, String denyReason, AsyncCallback<Boolean> callback);

}
