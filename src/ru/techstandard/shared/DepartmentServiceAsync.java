package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Department;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DepartmentServiceAsync {

	void addDepartment(Department department, AsyncCallback<Integer> callback);

	void deleteDepartment(int id, AsyncCallback<Boolean> callback);

	void getAllDepartments(AsyncCallback<List<Department>> callback);

	void getDepartments(int parentId, AsyncCallback<List<Department>> callback);

	void updateDepartment(Department department, AsyncCallback<Boolean> callback);

}
