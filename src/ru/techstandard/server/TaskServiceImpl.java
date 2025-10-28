package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.Event;
import ru.techstandard.client.model.Task;
import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.TaskService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class TaskServiceImpl extends RemoteServiceServlet implements TaskService {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;
	EventServiceImpl eventServ = new EventServiceImpl();
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();

	@Override
	public PagingLoadResult<Task> getTasks(FilterPagingLoadConfig config) {
		List<Task> records;
		String havingClause = getHavingClause(config);
		String sortClause = "";
		String limitClause = "";
		
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY t.due_date ASC";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadTasks(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Task>(records, getCount(havingClause), config.getOffset());
	}

	public PagingLoadResult<Task> getPagedTasksByEmployee(int id, FilterPagingLoadConfig config) {
		List<Task> records;
		String havingClause = getHavingClause(config);
		havingClause = havingClause + " AND t.executor_id="+id;
		String sortClause = "";
		String limitClause = "";
		
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY t.due_date ASC";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadTasks(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Task>(records, getCount(havingClause), config.getOffset());
	}
	
	public List<Task> getTasksByEmployee(int employeeId) {
		String havingClause = " WHERE t.executor_id = "+employeeId;
		String sortClause = " ORDER BY t.start_date ASC ";
		
		return loadTasks(havingClause, sortClause, "");
	}
	
	private Task getTask(int id) {
		String havingClause = " WHERE id = "+id;
		List<Task> tasks = loadJustTasks(havingClause);
		if (tasks.size()==0)
			return new Task();
		else
			return tasks.get(0);
	}
	
	public PagingLoadResult<Task> getPagedTasksByDepartment(int id, FilterPagingLoadConfig config) {
		List<Task> records;
		String havingClause = getHavingClause(config);
//		havingClause = havingClause + " AND (e.department_id IN ("+DBConnect.getSubDeptsList(id)+"))";
		havingClause = havingClause + " AND (e.department_id = "+ id +" OR t.created_by="+getLoggedUserId()+" OR (e.boss = true AND e.department_id IN ("
					+ "SELECT id FROM departments WHERE parent_id = "+id
					+ ")) )";
		String sortClause = "";
		String limitClause = "";
		
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY t.due_date ASC";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadTasks(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Task>(records, getCount(havingClause), config.getOffset());
	}
	
	private List<Task> loadTasks(String havingClause, String sortClause, String limitClause) {
		conn = DBConnect.getConnection();
		List<Task> records = new ArrayList<Task>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT t.id, t.created_by, cr.name creatorName, d.name typeName, e.name executorName, t.start_date startDate, t.due_date dueDate, " +
					"t.completed_date completedDate, t.description, t.status, t.notes, t.executor_id, t.follower_id, t.viewed, e.department_id, e.boss " +
					"FROM tasks t INNER JOIN (employees e, employees cr, dictionaries d) ON (t.executor_id = e.id AND t.type_id = d.id AND t.created_by = cr.id)"+
					havingClause+sortClause+limitClause);
//			System.out.println("loadTasks clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Task t = new Task(result.getInt("id"), result.getInt("created_by"));
				t.setCreatorName(result.getString("creatorName"));
				t.setTypeName(result.getString("typeName"));
				t.setExecutorName(result.getString("executorName"));
				t.setExecutorId(result.getInt("executor_id"));
				t.setDescription(result.getString("description"));
				t.setStartDate(result.getDate("startDate"));
				t.setDueDate(result.getDate("dueDate"));
				t.setCompletedDate(result.getDate("completedDate"));
				t.setStatus(result.getInt("status"));
				t.setCompleted(result.getInt("status")==1?"Да":(result.getInt("status")==0?"Нет":(result.getInt("status")==-1?"На утверждении":"На согласовании")));
				t.setNotes(result.getString("notes")==null?"":result.getString("notes"));
				t.setFollowerId(result.getInt("follower_id"));
				t.setViewed(result.getBoolean("viewed"));
				records.add(t);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadTasks exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] loadTasks exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] loadTasks exception: "+sqle.toString());
		}
	    return records;
	}
	
	private List<Task> loadJustTasks(String havingClause) {
		conn = DBConnect.getConnection();
		List<Task> records = new ArrayList<Task>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM tasks "+havingClause);
//			System.out.println("loadTasks clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Task t = new Task(result.getInt("id"), result.getInt("created_by"));
				t.setTypeId(result.getInt("type_id"));
				t.setExecutorId(result.getInt("executor_id"));
				t.setStartDate(result.getDate("start_date"));
				t.setDueDate(result.getDate("due_date"));
				t.setCompletedDate(result.getDate("completed_date"));
				t.setDescription(result.getString("description"));
				t.setStatus(result.getInt("status"));
				t.setCompleted(result.getInt("status")==1?"Да":(result.getInt("status")==0?"Нет":(result.getInt("status")==-1?"На утверждении":"На согласовании")));
				t.setNotes(result.getString("notes")==null?"":result.getString("notes"));
				t.setFollowerId(result.getInt("follower_id"));
				t.setViewed(result.getBoolean("viewed"));
				records.add(t);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadTasks exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] loadTasks exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] loadTasks exception: "+sqle.toString());
		}
	    return records;
	}
	
	private int getCount(String havingClause) {
		int count = 0;
		conn = DBConnect.getConnection();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM (SELECT t.id, t.created_by, d.name typeName, e.name executorName, t.start_date startDate, t.due_date dueDate, " +
					"t.completed_date completedDate, t.description, t.status, t.notes, t.executor_id, e.department_id, e.boss " +
					"FROM tasks t INNER JOIN (employees e, dictionaries d) ON (t.executor_id = e.id AND t.type_id = d.id) "+havingClause+") as tbl");
//			System.out.println("getcount(s) qry="+"SELECT count(*) FROM (SELECT t.id, d.name typeName, e.name executorName, t.start_date startDate, t.due_date dueDate, " +
//					"t.completed_date completedDate, t.description, t.status, t.notes, t.executor_id, e.department_id, e.boss " +
//					"FROM tasks t INNER JOIN (employees e, dictionaries d) ON (t.executor_id = e.id AND t.type_id = d.id) "+havingClause+") as tbl");
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] getCount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] getCount(s) exception: "+sqle.toString());
		}
	    return count;
	}
	
	private String getHavingClause(FilterPagingLoadConfig config) {
		List<FilterConfig> filters = config.getFilters();
		if (filters.size() == 0) {
			// Особый случай: задания в статусе "на согласовании" не должен видеть никто, кроме уполномоченных на это
			// Если фильтра нет, то выбраны все задания. для тех, кто не согласователь - нужно исключить согласовываемые задания
			if (!isApprover())
				return(" HAVING t.status != -2");
			else
				return "";
		}
		
		boolean first = true;
		boolean hasStatusFilter = false;
		StringBuilder builder = new StringBuilder();
		builder.append(" HAVING ");
		for (FilterConfig f : filters) {
			String type = f.getType();
			String value = f.getValue();
			String field = f.getField();
			if (value == null)
		          continue; 			 
			 
			if (!first)
				builder.append(" AND ");
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
			if ("string".equals(type)) {
				// Исключения для поля "Выполнено" - отображается текст "все"/"да"/"нет/на утвержд.", а в базе - tinyint
				// Если "все", то фильтр отключен, поэтому проверяем только три значения
				if (field.equals("completed")) {
					hasStatusFilter = true;
					builder.append("t.status = ");
					builder.append(value.equals("Нет")?"0":(value.equals("Да")?"1":(value.equals("На утвержд.")?"-1":"-2")));
				} else {
					builder.append(field+" LIKE '%");
					builder.append(value);
					builder.append("%'");
				}
			} else if ("date".equals(type)) {
				String cmp = f.getComparison().equals("on")?"=":(f.getComparison().equals("before")?"<=":">=");
				
				Calendar calendar = Calendar.getInstance();
			    calendar.setTimeInMillis(Long.valueOf(value));
				
				builder.append(field+cmp+" '");
				builder.append(dt.format(calendar.getTime()));
				builder.append("'");
			} 
			first = false;
		}
		// Особый случай: задания в статусе "на согласовании" не должен видеть никто, кроме уполномоченных на это
		// Если фильтра нет, то выбраны все задания. для тех, кто не согласователь - нужно исключить согласовываемые задания
		if (!hasStatusFilter && !isApprover())
			builder.append(" AND t.status != -2");
		
		return builder.toString();
	};
	
	@Override
	public boolean deleteTask(int id) {
//		System.out.println("Deleting task "+id);
		conn = DBConnect.getConnection();

		Task delTask = getTask(id);
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id="+id);
			ps.executeUpdate();
			ps.close();
			// Если удаляемое задание было в статусе "На согласовании", уведомлять не нужно
			if (delTask.getStatus() != -2 && delTask.getExecutorId() != 0) {
				eventServ.addEvent(new Event(new Date(), delTask.getExecutorId(),
						"Удаление задания",
						"Ваше задание \""+delTask.getDescription() + "\" удалено из системы."));
			}
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			System.out.println("deleteTask exception: "+sqle.getMessage());
		}
		
		AttachementServiceImpl attachServ = new AttachementServiceImpl();
		List<Integer> attachmentList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM attachments WHERE parent_id=" + id + " and parent_type="+Constants.TASK_ATTACHMENTS);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				attachmentList.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteTask getfilename exception: "+sqle.getMessage());
		}
//		System.out.println("attachmentList = "+attachmentList.toString());
		attachServ.deleteAttachments(attachmentList);
		
		return true;
	}

	@Override
	public int addTask(Task task) {
		// Можно было бы создание задачи отправить в события здесь, но данный метод вызывается только для создания заготовки задания
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO tasks (type_id, created_by, executor_id, start_date, due_date, description, status, follower_id, viewed) VALUES ("+
				task.getTypeId() + ", " +
				task.getCreatedBy() + ", " +
				task.getExecutorId() + ", " +
				DBConnect.saveDate(task.getStartDate()) + ", " +
				DBConnect.saveDate(task.getDueDate()) + ", " +
				"?, " +//DBConnect.saveString(task.getDescription()) + ", " +
				task.getStatus() + ", " +
				task.getFollowerId() + 
				", false)";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, task.getDescription());
//			System.out.println("addTask qry="+qry);
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addTask exception:"+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean updateTask(Task task) {
		Task prevTaskState = getTask(task.getId());
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		// Такого не должно быть, но на всякий случай
//		TODO fire an exception
		if (prevTaskState == null)
			return false;
		conn = DBConnect.getConnection();
		String qry = "UPDATE tasks SET " +
				"type_id =" + task.getTypeId() + ", " +
				"executor_id =" + task.getExecutorId() + ", " +
				"description = ?, " + //DBConnect.saveString(task.getDescription()) + ", " +
				"start_date =" + DBConnect.saveDate(task.getStartDate())+", " +
				"due_date =" + DBConnect.saveDate(task.getDueDate())+", " +
				"notes = ?, " + //DBConnect.saveString(task.getNotes()) + ", " +
				"follower_id = " + task.getFollowerId() +
				" WHERE id=" + task.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, task.getDescription());
			ps.setString(2, task.getNotes());
			int rows = ps.executeUpdate();
			ps.close();
//			TODO fire an exception
			if (rows == 0)
				return false;
			// статус -2 означает, что создано задание, требующее согласования. в этом случае пока не уведомляем исполнителя, только тех, кто должен согласовывать
			System.out.println("updating task, status="+task.getStatus());
			if (task.getStatus() == -2) {
//				System.out.println("notifying confirmers");
				List<Integer> confirmers = emplServ.getTaskApprovers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Задание поступило на согласование",
							"Создано задание \""+task.getDescription() + "\", требующее согласования."));
				}
				return true;
			}
			
			// При первоначальном создании "заготовки" для задания исполнитель не указывается. Уведомляем, когда задание сохраняется с указанным исполнителем
			// или когда задание согласовано
			if ((task.getExecutorId() != prevTaskState.getExecutorId() && prevTaskState.getExecutorId() == 0) || (prevTaskState.getStatus() == -2)) {
				System.out.println("notifying executor");
				eventServ.addEvent(new Event(new Date(), task.getExecutorId(),
						"Новое задание",
						"Вы назначены исполнителем по заданию \""+task.getDescription()+"\"."));
			}
			// О смене параметров задания не уведомляем, если предыдущий статус - "на согласовании", т.к. для исполнителя это будет новое задание
			if (prevTaskState.getStatus() == -2)
				return true;
			
			if (task.getExecutorId() != prevTaskState.getExecutorId() && prevTaskState.getExecutorId() != 0) {
				Employee prevEmpl = emplServ.getEmployeeInfo(prevTaskState.getExecutorId());
				Employee newEmpl = emplServ.getEmployeeInfo(task.getExecutorId());
				eventServ.addEvent(new Event(new Date(), task.getExecutorId(),
						"Смена исполнителя задания",
						"Вы назначены исполнителем по заданию \""+task.getDescription()+"\". Ранее - "+prevEmpl.getName()));
				eventServ.addEvent(new Event(new Date(), prevTaskState.getExecutorId(),
						"Смена исполнителя задания",
						"Ваше задание \""+task.getDescription()+"\" передано на исполнение сотруднику "+newEmpl.getName()));
				if (prevTaskState.getExecutorId() != prevTaskState.getCreatedBy())
					eventServ.addEvent(new Event(new Date(), prevTaskState.getCreatedBy(),
							"Смена исполнителя задания",
							"Ваше задание \""+task.getDescription()+"\" передано на исполнение сотруднику "+newEmpl.getName()));
				
			}
			if (!task.getStartDate().equals(prevTaskState.getStartDate()) && prevTaskState.getStartDate() != null) {
				eventServ.addEvent(new Event(new Date(), task.getExecutorId(),
						"Смена сроков задания",
						"Изменена дата начала работ по заданию \""+task.getDescription()+"\", новая дата - "+dt.format(task.getStartDate())));
			}
			if (prevTaskState.getDueDate() != null && !task.getDueDate().equals(prevTaskState.getDueDate())) {
				eventServ.addEvent(new Event(new Date(), task.getExecutorId(),
						"Смена сроков задания",
						"Изменена дата завершения работ по заданию \""+task.getDescription()+"\", новая дата - "+dt.format(task.getDueDate())));
			}
			
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateTask exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public boolean approveTask(Task task) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE tasks SET " +
				"type_id =" + task.getTypeId() + ", " +
				"executor_id =" + task.getExecutorId() + ", " +
				"description = ?, " + //DBConnect.saveString(task.getDescription()) + ", " +
				"start_date =" + DBConnect.saveDate(task.getStartDate())+", " +
				"due_date =" + DBConnect.saveDate(task.getDueDate())+", " +
				"notes = ?, " + //DBConnect.saveString(task.getNotes()) + ", " +
				"follower_id = " + task.getFollowerId() + ", " +
				"status = 0" +
				" WHERE id=" + task.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, task.getDescription());
			ps.setString(2, task.getNotes());
			int rows = ps.executeUpdate();
			ps.close();
			if (rows > 0) 
				eventServ.addEvent(new Event(new Date(), task.getExecutorId(),
						"Новое задание",
						"Вы назначены исполнителем по заданию \""+task.getDescription()+"\"."));
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("approveTask exception: "+sqle.getMessage());
		}
		return false;
	}
	
	@Override
	public String getPrintableTaskList(FilterPagingLoadConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Различные задачи</title></head><body>");
		builder.append("<h1>Перечень прочих задач</h1>");
		
		List<Task> tasks;
		String havingClause = getHavingClause(config);
		String sortClause = " ORDER BY t.due_date ASC ";
		tasks = loadTasks(havingClause, sortClause, "");
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("Исполнитель"); builder.append("</td>");
		builder.append("<td>"); builder.append("Тип задания"); builder.append("</td>");
		builder.append("<td>"); builder.append("Содержание задания"); builder.append("</td>");
		builder.append("<td>"); builder.append("Дата начала"); builder.append("</td>");
		builder.append("<td>"); builder.append("Срок исполнения"); builder.append("</td>");
		builder.append("<td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("<td>"); builder.append("Статус"); builder.append("</td>");
		builder.append("</tr>");
		for (Task t: tasks) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(t.getExecutorName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(t.getTypeName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(t.getDescription())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(t.getStartDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(t.getDueDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(t.getNotes())); builder.append("</td>");
			builder.append("<td>"); builder.append(t.getStatus()==1?"Да":(t.getStatus()==0?"Нет":(t.getStatus()==-1?"На утверждении":"На согласовании"))); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}

	@Override
	public String getPrintableTaskCard(int id) {
		conn = DBConnect.getConnection();
		Task t;
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT t.id, t.created_by, cr.name cratorName, d.name typeName, e.name executorName, t.start_date startDate, t.due_date dueDate, " +
					"t.completed_date completedDate, t.description, t.status, t.notes " +
					"FROM tasks t INNER JOIN (employees e, employees cr, dictionaries d) ON (t.executor_id = e.id AND t.type_id = d.id AND t.created_by = cr.id) WHERE t.id="+id);
			ResultSet result = ps.executeQuery();
			result.first();
			t = new Task(result.getInt("id"), result.getInt("created_by"));
			t.setCreatorName(result.getString("creatorName"));
			t.setTypeName(result.getString("typeName"));
			t.setExecutorName(result.getString("executorName"));
			t.setDescription(result.getString("description"));
			t.setStartDate(result.getDate("startDate"));
			t.setDueDate(result.getDate("dueDate"));
			t.setCompletedDate(result.getDate("completedDate"));
			t.setStatus(result.getInt("status"));
			t.setCompleted(result.getInt("status")==1?"Да":(result.getInt("status")==0?"Нет":(result.getInt("status")==-1?"На утверждении":"На согласовании")));
			t.setNotes(result.getString("notes"));
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getPrintableTaskCard exception: "+sqle.getMessage());
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка задания</title></head><body>");
		builder.append("<h1>Карточка задания</h1><br><br>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("Инициатор"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(t.getCreatorName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Исполнитель"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(t.getExecutorName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Тип задания"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(t.getTypeName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Содержание задания"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(t.getDescription())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата начала"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(t.getStartDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Срок исполнения"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(t.getDueDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(t.getNotes())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Статус"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(t.getCompleted())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}

	private String v(String value) {
		return value == null?"":value;
	}

	@Override
	public boolean setCompleted(int id, String notes) {
		Date now = new Date();
		conn = DBConnect.getConnection();
		String qry = "UPDATE tasks SET " +
				"completed_date = " + DBConnect.saveDate(now) + ", " +
				"notes = ?, " + //DBConnect.saveString(notes) + ", " +
				"status = -1" +
				" WHERE id=" + id;
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, notes);
			int rows = ps.executeUpdate();
			ps.close();
			if (rows > 0) {
				Task task = getTask(id);
				eventServ.addEvent(new Event(new Date(), task.getCreatedBy(),
						"Задание поступило на утверждение",
						"Исполнитель пометил задание \""+task.getDescription() + "\" как выполненное."));
			}
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("setCompleted exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public boolean updateTaskStatus(int id, int newStatus, boolean eraseCompletedDate) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE tasks SET status = " + newStatus +
				(eraseCompletedDate?", completed_date=null":"") +
				" WHERE id=" + id;
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			int rows = ps.executeUpdate();
			ps.close();
			if (rows > 0) {
				Task updTask = getTask(id);
				if (newStatus == 0)
					eventServ.addEvent(new Event(new Date(), updTask.getExecutorId(),
							"Изменение статуса задания",
							"Ваше задание \""+updTask.getDescription() + "\" возвращено в статус \"Не выполненное\"."));
				else if (newStatus == 1) {
					eventServ.addEvent(new Event(new Date(), updTask.getExecutorId(),
							"Изменение статуса задания",
							"Ваше задание \""+updTask.getDescription() + "\" утверждено в статусе \"Выполненное\"."));
					// Если задание с последователем, нужно создать новое
					if (updTask.getFollowerId() != 0) {
//						System.out.println("updating task");
						updTask.setExecutorId(updTask.getFollowerId());
						updTask.setStartDate(new Date());
						updTask.setDueDate(addWorkDays(5, new Date()));
						updTask.setStatus(0);
						updTask.setFollowerId(0);
						addTask(updTask);
						eventServ.addEvent(new Event(new Date(), updTask.getExecutorId(),
								"Новое задание",
								"Вы назначены исполнителем по заданию \""+updTask.getDescription()+"\"."));
					}
				}
			}
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateTaskStatus exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public List<Task> getOutdatedTasks(int employeeId) {
		conn = DBConnect.getConnection();
		List<Task> records = new ArrayList<Task>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT t.id, t.created_by, d.name typeName, e.name executorName, t.start_date startDate, t.due_date dueDate, " +
					"t.completed_date completedDate, t.description, t.status, t.notes, t.executor_id " +
					"FROM tasks t INNER JOIN (employees e, dictionaries d) ON (t.executor_id = e.id AND t.type_id = d.id) "
					+ "WHERE t.status=0 AND t.due_date < "+ DBConnect.saveDate(new Date())
					+ " AND t.notes IS NULL AND t.executor_id="+employeeId);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Task t = new Task(result.getInt("id"), result.getInt("created_by"));
				t.setTypeName(result.getString("typeName"));
				t.setExecutorName(result.getString("executorName"));
				t.setExecutorId(result.getInt("executor_id"));
				t.setDescription(result.getString("description"));
				t.setStartDate(result.getDate("startDate"));
				t.setDueDate(result.getDate("dueDate"));
				t.setCompletedDate(result.getDate("completedDate"));
				t.setStatus(result.getInt("status"));
				t.setCompleted(result.getInt("status")==1?"Да":(result.getInt("status")==0?"Нет":(result.getInt("status")==-1?"На утверждении":"На согласовании")));
				t.setNotes(result.getString("notes"));
				records.add(t);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getOutdatedTasks exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] getOutdatedTasks exception: "+sqle.getMessage());
			MyLogger.warning("[TaskServiceImpl] getOutdatedTasks exception: "+sqle.toString());
		}
	    return records;
	}
	
	private Date addWorkDays(int days, Date toDate) {
		int daysToAdd = days;
		DateWrapper wr = new DateWrapper(toDate);
		// добавляем days рабочих дней - будет срок исполнения. 
		// если срок приходится на воскресенье или понедельник, то не считаем рабочий день добавленным - "к понедельнику" означает что в субботу и воскресенье не работали
		// календарь не учитываем - поле дано на редактирование, пользователь сам поменяет, если что не так
		while (daysToAdd > 0) {
			if (wr.getDayInWeek() != 6 && wr.getDayInWeek() != 0) {
				daysToAdd--;
			}
			wr = wr.addDays(1);
		}
		return wr.asDate();
	}
	
	private boolean isApprover() {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof UserDTO) {
        	UserDTO loggedUser = (UserDTO) userObj;
        	return (loggedUser.getAccess().isTaskApprover());
        } else {
        	return false;
        }
	}

	private int getLoggedUserId() {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof UserDTO) {
        	UserDTO loggedUser = (UserDTO) userObj;
        	return loggedUser.getEmployeeId();
        } else {
        	return 0;
        }
	}

	@Override
	public void setViewed(int id) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE tasks SET viewed = true WHERE id=" + id;
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("setViewed exception: "+sqle.getMessage());
		}
	}
}
