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
import ru.techstandard.client.model.Evaluation;
import ru.techstandard.client.model.Event;
import ru.techstandard.shared.EvaluationService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class EvaluationServiceImpl extends RemoteServiceServlet implements EvaluationService {
	private static final long serialVersionUID = 1L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;

	@Override
	public PagingLoadResult<Evaluation> getEvaluations(FilterPagingLoadConfig config) {
		List<Evaluation> records;
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
			sortClause = " ORDER BY ev.id";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadEvaluations(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Evaluation>(records, getCount(havingClause), config.getOffset());
	}

	private List<Evaluation> loadEvaluations(String havingClause, String sortClause, String limitClause) {
		conn = DBConnect.getConnection();
		List<Evaluation> records = new ArrayList<Evaluation>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT ev.id, ev.deleted, e.name employeeName, p.name position, f.name fieldName, ev.cert_num certNum, "
					+ "ev.last_eval_date lastEvalDate, ev.next_eval_date nextEvalDate "
					+ "FROM evaluations ev INNER JOIN (dictionaries f, dictionaries p, employees e) "
					+ "ON (ev.employee_id = e.id AND e.position_id = p.id AND ev.field_id = f.id)"+havingClause+sortClause+limitClause);
//			System.out.println("loadEvaluations clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Evaluation e = new Evaluation(result.getInt("id"));
				e.setEmployeeName(result.getString("employeeName"));
				e.setPosition(result.getString("position"));
				e.setFieldName(result.getString("fieldName"));
				e.setCertNum(result.getString("certNum"));
				e.setLastEvalDate(result.getDate("lastEvalDate"));
				e.setNextEvalDate(result.getDate("nextEvalDate"));
				records.add(e);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadEvaluations exception: "+sqle.getMessage());
			MyLogger.warning("[EvaluationServiceImpl] loadGuides exception: "+sqle.getMessage());
			MyLogger.warning("[EvaluationServiceImpl] loadGuides exception: "+sqle.toString());
		}
	    return records;
	}
	
	private int getCount(String havingClause) {
		int count = 0;
		conn = DBConnect.getConnection();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM (SELECT ev.id, ev.deleted, e.name employeeName, p.name position, f.name fieldName, ev.cert_num certNum, "
					+ "ev.last_eval_date lastEvalDate, ev.next_eval_date nextEvalDate "
					+ "FROM evaluations ev INNER JOIN (dictionaries f, dictionaries p, employees e) "
					+ "ON (ev.employee_id = e.id AND e.position_id = p.id AND ev.field_id = f.id) "+havingClause+") as t");
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[EvaluationServiceImpl] getCount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[EvaluationServiceImpl] getCount(s) exception: "+sqle.toString());
		}
	    return count;
	}
	
	private String getHavingClause(FilterPagingLoadConfig config) {
		List<FilterConfig> filters = config.getFilters();
		if (filters.size() == 0)
			return " HAVING deleted=false ";
		
		StringBuilder builder = new StringBuilder();
		builder.append(" HAVING deleted=false ");
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
	public boolean deleteEvaluation(int id, boolean markOnly) {
		conn = DBConnect.getConnection();
		try {
			String qry;
	    	if (markOnly) { 
	    		qry = "UPDATE evaluations SET deleted=true WHERE id=";
	    		List<Integer> confirmers = emplServ.getDeleteConfirmers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Объект помечен на удаление",
							"Новый объект в категории \"аттестация\" помечен на удаление."));
				}
	    	}
	    	else
	    		qry = "DELETE FROM evaluations WHERE id=";
			PreparedStatement ps = conn.prepareStatement(qry+id);
			ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
//			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
//				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах для данного договора.");
			System.out.println("deleteEvaluation exception: "+sqle.getMessage());
		}
		if (markOnly)
			return true;
		
		AttachementServiceImpl attachServ = new AttachementServiceImpl();
		List<Integer> attachmentList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM attachments WHERE parent_id=" + id + " and parent_type="+Constants.EVAL_ATTACHMENTS);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				attachmentList.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteEvaluation getfilename exception: "+sqle.getMessage());
		}
//		System.out.println("attachmentList = "+attachmentList.toString());
		attachServ.deleteAttachments(attachmentList);
		
		return true;
	}

	@Override
	public int addEvaluation(Evaluation evaluation) {
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO evaluations (employee_id, field_id, cert_num, last_eval_date, next_eval_date) VALUES ("+
				evaluation.getEmployeeId() + ", " +
				evaluation.getFieldId() + ", " +
				"?, " +
//				DBConnect.saveString(evaluation.getCertNum()) + ", " +
				DBConnect.saveDate(evaluation.getLastEvalDate())+", " +
				DBConnect.saveDate(evaluation.getNextEvalDate()) +
				")";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, evaluation.getCertNum());
//			System.out.println("addEvaluation qry="+qry);
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addEvaluation exception:"+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean updateEvaluation(Evaluation evaluation) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE evaluations SET " +
				"employee_id=" + evaluation.getEmployeeId() + ", " + 
				"field_id=" + evaluation.getFieldId() + ", " +
				"cert_num = ?, " + //DBConnect.saveString(evaluation.getCertNum()) + ", " +
				"last_eval_date =" + DBConnect.saveDate(evaluation.getLastEvalDate())+", " +
				"next_eval_date =" + DBConnect.saveDate(evaluation.getNextEvalDate()) +
				" WHERE id=" + evaluation.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, evaluation.getCertNum());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateEvaluation exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public String getPrintableEvaluationList(FilterPagingLoadConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Сведения об аттестации персонала</title></head><body>");
		builder.append("<h1>Сведени об аттестации персонала</h1>");
		
		List<Evaluation> evaluations;
		String havingClause = getHavingClause(config);
		String sortClause = " ORDER BY e.name, f.name ";
		evaluations = loadEvaluations(havingClause, sortClause, "");
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("ФИО сотрудника"); builder.append("</td>");
		builder.append("<td>"); builder.append("Должность"); builder.append("</td>");
		builder.append("<td>"); builder.append("Область аттестации"); builder.append("</td>");
		builder.append("<td>"); builder.append("№ удостоверения"); builder.append("</td>");
		builder.append("<td>"); builder.append("Дата последней<br>аттестации"); builder.append("</td>");
		builder.append("<td>"); builder.append("Дата следующей<br>аттестации"); builder.append("</td>");
		builder.append("</tr>");
		for (Evaluation e: evaluations) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(e.getEmployeeName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(e.getPosition())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(e.getFieldName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(e.getCertNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(e.getLastEvalDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(e.getNextEvalDate())); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}

	@Override
	public String getPrintableEvaluationCard(int id) {
		conn = DBConnect.getConnection();
		Evaluation e = new Evaluation();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT ev.id, e.name employeeName, p.name position, f.name fieldName, ev.cert_num certNum, "
					+ "ev.last_eval_date lastEvalDate, ev.next_eval_date nextEvalDate "
					+ "FROM evaluations ev INNER JOIN (dictionaries f, dictionaries p, employees e) "
					+ "ON (ev.employee_id = e.id AND e.position_id = p.id AND ev.field_id = f.id) WHERE ev.id="+id);
			ResultSet result = ps.executeQuery();
			result.first();
			e.setEmployeeName(result.getString("employeeName"));
			e.setPosition(result.getString("position"));
			e.setFieldName(result.getString("fieldName"));
			e.setCertNum(result.getString("certNum"));
			e.setLastEvalDate(result.getDate("lastEvalDate"));
			e.setNextEvalDate(result.getDate("nextEvalDate"));
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getPrintableEvaluationCard exception: "+sqle.getMessage());
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка сведений об аттестации</title></head><body>");
		builder.append("<h1>Карточка сведений об аттестации</h1><br><br>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("ФИО сотрудника"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(e.getEmployeeName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Должность"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(e.getPosition())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Область аттестации"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(e.getFieldName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("№ удостоверения"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(e.getCertNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата последней аттестации"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(e.getLastEvalDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата следующей аттестации"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(e.getNextEvalDate())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}

	private String v(String value) {
		return value == null?"":value;
	}
}
