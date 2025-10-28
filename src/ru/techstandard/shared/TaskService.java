package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Task;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("taskService")
public interface TaskService extends RemoteService {
	PagingLoadResult<Task> getTasks(FilterPagingLoadConfig config);
	List<Task> getTasksByEmployee(int employeeId);
	List<Task> getOutdatedTasks(int employeeId);
	boolean deleteTask(int id);
	int addTask(Task Task);
	boolean updateTask(Task task);
	boolean approveTask(Task task);
	boolean setCompleted(int id, String notes);
	boolean updateTaskStatus(int id, int newStatus, boolean eraseCompletedDate);
	String getPrintableTaskList(FilterPagingLoadConfig config);
	String getPrintableTaskCard(int id);
	PagingLoadResult<Task> getPagedTasksByEmployee(int id, FilterPagingLoadConfig config);
	PagingLoadResult<Task> getPagedTasksByDepartment(int id, FilterPagingLoadConfig config);
	void setViewed(int id);
}
