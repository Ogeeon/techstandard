package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Employee;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("employeeService")
public interface EmployeeService extends RemoteService {
	List<Employee> getAllEmployees();
	List<Employee> getSubordinates(int deptId);
	Employee getEmployeeInfo(int id);
	PagingLoadResult<Employee> getEmployeesByDepartment(int deptId, boolean showFired, PagingLoadConfig config);
	List<Employee> getEmployeesByAccessGroup(int groupId, boolean notInGroup);
	boolean updateEmployee(Employee employee);
	int addEmployee(Employee employee);
	boolean deleteEmployee(int id);
	List<Employee> getColleagues(int deptId);
}
