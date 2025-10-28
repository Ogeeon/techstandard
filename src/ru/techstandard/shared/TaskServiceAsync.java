package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Task;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface TaskServiceAsync {

	void addTask(Task Task, AsyncCallback<Integer> callback);

	void deleteTask(int id, AsyncCallback<Boolean> callback);

	void getPrintableTaskCard(int id, AsyncCallback<String> callback);

	void getPrintableTaskList(FilterPagingLoadConfig config,
			AsyncCallback<String> callback);

	void getTasks(FilterPagingLoadConfig config,
			AsyncCallback<PagingLoadResult<Task>> callback);

	void updateTask(Task task, AsyncCallback<Boolean> callback);

	void setCompleted(int id, String notes, AsyncCallback<Boolean> callback);

	void updateTaskStatus(int id, int newStatus, boolean eraseCompletedDate,
			AsyncCallback<Boolean> callback);

	void getTasksByEmployee(int employeeId, AsyncCallback<List<Task>> callback);

	void getPagedTasksByEmployee(int id, FilterPagingLoadConfig config,
			AsyncCallback<PagingLoadResult<Task>> callback);

	void getPagedTasksByDepartment(int id, FilterPagingLoadConfig config,
			AsyncCallback<PagingLoadResult<Task>> callback);

	void getOutdatedTasks(int employeeId, AsyncCallback<List<Task>> callback);

	void approveTask(Task task, AsyncCallback<Boolean> callback);

	void setViewed(int id, AsyncCallback<Void> callback);


}
