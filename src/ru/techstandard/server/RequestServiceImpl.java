package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.Event;
import ru.techstandard.client.model.Request;
import ru.techstandard.shared.RequestService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class RequestServiceImpl extends RemoteServiceServlet implements RequestService {
	private static final long serialVersionUID = 1L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;
	String baseQry = "SELECT r.id, r.deleted, r.deleted_by, r.description, c.name clientName, r.responsible_id, e.name responsibleName, r.due_date dueDate, r.notes"
			+ " FROM requests r INNER JOIN (clients c, employees e)"
			+ " ON (r.client_id = c.id AND r.responsible_id = e.id)";

	@Override
	public PagingLoadResult<Request> getRequests(FilterPagingLoadConfig config) {
		List<Request> records;
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
			sortClause = " ORDER BY r.id";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadRequests(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Request>(records, getCount(havingClause), config.getOffset());
	}

	public Request getRequest(int id) {
		List<Request> records = loadRequests("  HAVING r.id="+id, "", "");
		if (records.size() == 0)
			return new Request();
		else
			return records.get(0);
	}
	
	private List<Request> loadRequests(String havingClause, String sortClause, String limitClause) {
		conn = DBConnect.getConnection();
		List<Request> records = new ArrayList<Request>();
	    try {
			PreparedStatement ps = conn.prepareStatement(baseQry+havingClause+sortClause+limitClause);
//			System.out.println("loadGuides clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Request r = new Request(result.getInt("id"));
				r.setDescription(result.getString("description"));
				r.setClientName(result.getString("clientName"));
				r.setResponsibleId(result.getInt("responsible_id"));
				r.setResponsibleName(result.getString("responsibleName"));
				r.setDueDate(result.getDate("dueDate"));
				r.setNotes(result.getString("notes"));
				r.setDeleted(result.getBoolean("deleted"));
				r.setDeletedBy(result.getInt("deleted_by"));
				records.add(r);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadRequests exception: "+sqle.getMessage());
			MyLogger.warning("[RequestServiceImpl] loadRequests exception: "+sqle.getMessage());
			MyLogger.warning("[RequestServiceImpl] loadRequests exception: "+sqle.toString());
		}
	    return records;
	}
	
	private int getCount(String havingClause) {
		int count = 0;
		conn = DBConnect.getConnection();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM ("+baseQry+havingClause+") as t");
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[RequestServiceImpl] getCount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[RequestServiceImpl] getCount(s) exception: "+sqle.toString());
		}
	    return count;
	}
	
	private String getHavingClause(FilterPagingLoadConfig config) {
		List<FilterConfig> filters = config.getFilters();
		if (filters.size() == 0)
			return " HAVING r.deleted=false ";
		
		StringBuilder builder = new StringBuilder();
		builder.append(" HAVING r.deleted=false ");
		for (FilterConfig f : filters) {
			String type = f.getType();
			String value = f.getValue();
			String field = f.getField();
			if (value == null)
		          continue; 			 
			 
			builder.append(" AND ");
			if ("string".equals(type)) {
				builder.append(field+" LIKE '%");
				builder.append(value);
				builder.append("%'");
			} 
		}
		
		return builder.toString();
	};
	
	@Override
	public boolean deleteRequest(int recordID, boolean markOnly) {
		conn = DBConnect.getConnection();
		try {
			String qry;
			if (markOnly) {
				int deleter = Utils.getUserIdFromSession(this.getThreadLocalRequest().getSession());
				qry = "UPDATE requests SET deleted=true, deleted_by="+deleter+" WHERE id=";
				List<Integer> confirmers = emplServ.getDeleteConfirmers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Объект помечен на удаление",
							"Новый объект в категории \"прочие задачи\" помечен на удаление."));
				}
			}
			else
				qry = "DELETE FROM requests WHERE id=";
			PreparedStatement ps = conn.prepareStatement(qry+recordID);
			ps.executeUpdate();
			ps.close();
			//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
//			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
//				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах для данного договора.");
			System.out.println("deleteRequest exception: "+sqle.getMessage());
		}
		if (markOnly)
			return true;
		
		AttachementServiceImpl attachServ = new AttachementServiceImpl();
		List<Integer> attachmentList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM attachments WHERE parent_id=" + recordID + " and parent_type="+Constants.REQUEST_ATTACHMENTS);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				attachmentList.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteRequest getfilename exception: "+sqle.getMessage());
		}
//		System.out.println("attachmentList = "+attachmentList.toString());
		attachServ.deleteAttachments(attachmentList);
		
		return true;
	}

	@Override
	public int addRequest(Request request) {
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO requests (description, client_id, responsible_id, due_date, notes) VALUES ("+
				"?, "+//DBConnect.saveString(request.getDescription()) + ", " +
				request.getClientId() + ", " +
				request.getResponsibleId() + ", " +
				DBConnect.saveDate(request.getDueDate())+", " +
//				DBConnect.saveString(request.getNotes()) +
				"?)";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, request.getDescription());
			ps.setString(2, request.getNotes());
//			System.out.println("addGuide qry="+qry);
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addRequest exception:"+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean updateRequest(Request request) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE requests SET " +
				"description = ?, " + //DBConnect.saveString(request.getDescription()) + ", " +
				"client_id =" + request.getClientId() + ", " +
				"responsible_id =" + request.getResponsibleId() + ", " +
				"due_date =" + DBConnect.saveDate(request.getDueDate())+", " +
				"notes = ? " + //DBConnect.saveString(request.getNotes()) +
				" WHERE id=" + request.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, request.getDescription());
			ps.setString(2, request.getNotes());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateRequest exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public String getPrintableRequestList(FilterPagingLoadConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Различные задачи</title></head><body>");
		builder.append("<h1>Перечень прочих задач</h1>");
		
		List<Request> requests;
		String havingClause = getHavingClause(config);
		String sortClause = " ORDER BY r.due_date DESC ";
		requests = loadRequests(havingClause, sortClause, "");
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("Содержание задачи"); builder.append("</td>");
		builder.append("<td>"); builder.append("Заказчик"); builder.append("</td>");
		builder.append("<td>"); builder.append("Исполнитель"); builder.append("</td>");
		builder.append("<td>"); builder.append("Срок исполнения"); builder.append("</td>");
		builder.append("<td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("</tr>");
		for (Request r: requests) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(r.getDescription())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getClientName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getResponsibleName())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(r.getDueDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getNotes())); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}

	@Override
	public String getPrintableRequestCard(int id) {
		conn = DBConnect.getConnection();
		Request r = new Request();
		try {
			PreparedStatement ps = conn.prepareStatement(baseQry+" WHERE r.id="+id);
			ResultSet result = ps.executeQuery();
			result.first();
			r.setDescription(result.getString("description"));
			r.setClientName(result.getString("clientName"));
			r.setResponsibleName(result.getString("responsibleName"));
			r.setDueDate(result.getDate("dueDate"));
			r.setNotes(result.getString("notes"));
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getPrintableRequestCard exception: "+sqle.getMessage());
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка задачи</title></head><body>");
		builder.append("<h1>Карточка задачи</h1><br><br>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("Содержание задачи"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(r.getDescription())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Заказчик"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(r.getClientName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Исполнитель"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(r.getResponsibleName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Срок исполнения"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(r.getDueDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(r.getNotes())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}
	
	private String v(String value) {
		return value == null?"":value;
	}
	

	public List<DeletedObject> getDeletedObjects() {
		List<DeletedObject> deleted = new ArrayList<DeletedObject>();
		List<Request> deletedRequests = loadRequests(" HAVING deleted=true ", "", "");
		
		for (int idx = 0; idx < deletedRequests.size(); idx++) {
			String descr = "Задача \""+deletedRequests.get(idx).getDescription() + " от " + deletedRequests.get(idx).getClientName(); 
			int deleter = deletedRequests.get(idx).getDeletedBy();
			String userName = emplServ.getEmployeeInfo(deleter).getName();
			deleted.add(new DeletedObject("requests_"+idx, "requests", "Журнал прочих задач", deletedRequests.get(idx).getId(), descr, deleter, userName));
		}
		return deleted;
	}
}
