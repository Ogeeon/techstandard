package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.AccessGroup;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("accessGroupService")
public interface AccessGroupService extends RemoteService {
	List<AccessGroup> getAccessGroups();
	int addAccessGroup(AccessGroup group);
	boolean updateAccessGroup(AccessGroup group, boolean skipAccessRights);
	boolean deleteAccessGroup(int id);
}
