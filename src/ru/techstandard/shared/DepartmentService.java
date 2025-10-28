package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Department;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("departmentService")
public interface DepartmentService extends RemoteService {
	List<Department> getDepartments(int parentId);
	List<Department> getAllDepartments();
	boolean deleteDepartment(int id);
	int addDepartment(Department department);
	boolean updateDepartment(Department department);
}
