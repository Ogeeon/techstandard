package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Employee;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface EmployeeServiceAsync {

	void getAllEmployees(AsyncCallback<List<Employee>> callback);

	void updateEmployee(Employee employee, AsyncCallback<Boolean> callback);

	void getEmployeesByDepartment(int deptId, boolean showFired, PagingLoadConfig config, AsyncCallback<PagingLoadResult<Employee>> callback);

	void getEmployeeInfo(int id, AsyncCallback<Employee> callback);

	void addEmployee(Employee employee, AsyncCallback<Integer> callback);

	void deleteEmployee(int id, AsyncCallback<Boolean> callback);

	void getEmployeesByAccessGroup(int groupId, boolean notInGroup,
			AsyncCallback<List<Employee>> callback);

	void getSubordinates(int deptId, AsyncCallback<List<Employee>> callback);

	void getColleagues(int deptId, AsyncCallback<List<Employee>> callback);

}
