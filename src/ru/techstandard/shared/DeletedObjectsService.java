package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.DeletedObject;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("deletedObjectsService")
public interface DeletedObjectsService extends RemoteService {

	List<DeletedObject> getDeletedObjects();

	boolean applyObjectDelete(String table, int id);

	boolean undoObjectDelete(String table, int id, int userId, String denyReason);

}
