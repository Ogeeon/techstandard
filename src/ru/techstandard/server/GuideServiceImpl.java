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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.Event;
import ru.techstandard.client.model.Guide;
import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.GuideService;
import ru.techstandard.shared.NotAuthorizedException;
import ru.techstandard.shared.NotLoggedInException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class GuideServiceImpl extends RemoteServiceServlet implements GuideService {

	private static final long serialVersionUID = 1L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;
	private String baseQry = "SELECT g.id, g.deleted, g.deleted_by, g.obj_name objName, d.name objTypeName, g.fnum, g.rnum, c.name clientName, e.name responsibleName, "
			+ "g.due_date dueDate, g.notes, cn.id contractId, cn.num contractNum, a.id actId, a.work_num actNum "
			+ "FROM guides g INNER JOIN clients c ON g.client_id = c.id INNER JOIN dictionaries d ON g.obj_type_id = d.id "
			+ "INNER JOIN employees e ON g.responsible_id = e.id INNER JOIN contracts cn ON g.contract_id=cn.id LEFT JOIN acts a ON g.act_id = a.id ";

	private void checkIfAllowed(int operation, String onObject) throws NotLoggedInException, NotAuthorizedException {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof UserDTO) {
        	UserDTO loggedUser = (UserDTO) userObj;
        	if (!loggedUser.getAccess().isAllowed(operation, onObject))
        		System.out.println("guides: gonna throw NotAuthExc");
//        		throw new NotAuthorizedException("У Вас нет прав на выполнение данной операции ("+
//        				Constants.SECTION_NAMES.get(onObject)+" - "+Constants.OPERATION_NAMES.get(operation)+").");
        } else {
        	System.out.println("guides: gonna throw NotLoggedExc");
//        	throw new NotLoggedInException();
        }
	}
	
	@Override
	public PagingLoadResult<Guide> getGuides(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_READ, "guides");
		
		List<Guide> records;
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
			sortClause = " ORDER BY g.id";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadGuides(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Guide>(records, getCount(havingClause), config.getOffset());
	}

	public Guide getGuide(int id) throws NotLoggedInException, NotAuthorizedException {
		List<Guide> records = loadGuides(" WHERE g.id="+id, "", "");
		if (records.size() == 0)
			return new Guide();
		else
			return records.get(0);
	}
	
	private List<Guide> loadGuides(String havingClause, String sortClause, String limitClause) {
		conn = DBConnect.getConnection();
		List<Guide> records = new ArrayList<Guide>();
	    try {
			PreparedStatement ps = conn.prepareStatement(baseQry+havingClause+sortClause+limitClause);
//			System.out.println("loadGuides clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Guide g = new Guide(result.getInt("id"));
				g.setObjName(result.getString("objName"));
				g.setObjTypeName(result.getString("objTypeName"));
				g.setFNum(result.getString("fnum"));
				g.setRNum(result.getString("rnum"));
				g.setClientName(result.getString("clientName"));
				g.setContractId(result.getInt("contractId"));
				g.setContractNum(result.getString("contractNum"));
				g.setActId(result.getInt("actId"));
				g.setActNum(result.getString("actNum"));
				g.setResponsibleName(result.getString("responsibleName"));
				g.setDueDate(result.getDate("dueDate"));
				g.setNotes(result.getString("notes"));
				g.setDeleted(result.getBoolean("deleted"));
				g.setDeletedBy(result.getInt("deleted_by"));
				records.add(g);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadGuides exception: "+sqle.getMessage());
			MyLogger.warning("[GuideServiceImpl] loadGuides exception: "+sqle.getMessage());
			MyLogger.warning("[GuideServiceImpl] loadGuides exception: "+sqle.toString());
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
			MyLogger.warning("[GuideServiceImpl] getCount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[GuideServiceImpl] getCount(s) exception: "+sqle.toString());
		}
	    return count;
	}
	
	private String getHavingClause(FilterPagingLoadConfig config) {
		List<FilterConfig> filters = config.getFilters();
		if (filters.size() == 0)
			return " HAVING g.deleted=false ";
		
		StringBuilder builder = new StringBuilder();
		builder.append(" HAVING g.deleted=false");
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
	public boolean deleteGuide(int recordID, boolean markOnly) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_DELETE, "guides");
		conn = DBConnect.getConnection();
		try {String qry;
		if (markOnly) { 
    		int deleter = Utils.getUserIdFromSession(this.getThreadLocalRequest().getSession());
			qry = "UPDATE guides SET deleted=true, deleted_by="+deleter+" WHERE id=";
			List<Integer> confirmers = emplServ.getDeleteConfirmers();
			for (int idx = 0; idx < confirmers.size(); idx++) {
				eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
						"Объект помечен на удаление",
						"Новый объект в категории \"руководства\" помечен на удаление."));
			}
		}
		else
			qry = "DELETE FROM guides WHERE id=";
		PreparedStatement ps = conn.prepareStatement(qry+recordID);
		ps.executeUpdate();
		ps.close();
		//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
			//			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
			//				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах для данного договора.");
			System.out.println("deleteGuide exception: "+sqle.getMessage());
		}
		if (markOnly)
			return true;

		AttachementServiceImpl attachServ = new AttachementServiceImpl();
		List<Integer> attachmentList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM attachments WHERE parent_id=" + recordID + " and parent_type="+Constants.GUIDE_ATTACHMENTS);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				attachmentList.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteGuide getfilename exception: "+sqle.getMessage());
		}
//		System.out.println("attachmentList = "+attachmentList.toString());
		attachServ.deleteAttachments(attachmentList);
		
		return true;
	}

	@Override
	public int addGuide(Guide guide) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_INSERT, "guides");
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO guides (obj_type_id, obj_name, rnum, fnum, client_id, contract_id, act_id, responsible_id, due_date, notes) VALUES ("+
				guide.getObjType() + ", " +
				"?, ?, ?, "+
//				DBConnect.saveString(guide.getObjName()) + ", " +
//				DBConnect.saveString(guide.getRNum()) + ", " +
//				DBConnect.saveString(guide.getFNum()) + ", " +
				guide.getClientId() + ", " +
				guide.getContractId() + ", " +
				guide.getActId() + ", " +
				guide.getResponsibleId() + ", " +
				DBConnect.saveDate(guide.getDueDate())+", " +
				"? )";
//				DBConnect.saveString(guide.getNotes()) +
//				")";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, guide.getObjName());
			ps.setString(2, guide.getRNum());
			ps.setString(3, guide.getFNum());
			ps.setString(4, guide.getNotes());
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
			System.out.println("addGuide exception:"+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean updateGuide(Guide guide) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_UPDATE, "guides");
		conn = DBConnect.getConnection();
		String qry = "UPDATE guides SET obj_type_id=" + guide.getObjType() + ", " +
				"obj_name = ?, " + //DBConnect.saveString(guide.getObjName()) + ", " +
				"rnum = ?, " + //DBConnect.saveString(guide.getRNum()) + ", " +
				"fnum = ?, " + //DBConnect.saveString(guide.getFNum()) + ", " +
				"client_id =" + guide.getClientId() + ", " +
				"contract_id =" + guide.getContractId() + ", " +
				"act_id =" + guide.getActId() + ", " +
				"responsible_id =" + guide.getResponsibleId() + ", " +
				"due_date =" + DBConnect.saveDate(guide.getDueDate())+", " +
				"notes = ? " + //DBConnect.saveString(guide.getNotes()) +
				" WHERE id=" + guide.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, guide.getObjName());
			ps.setString(2, guide.getRNum());
			ps.setString(3, guide.getFNum());
			ps.setString(4, guide.getNotes());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateGuide exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public String getPrintableGuideList(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_PRINT, "guides");
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Руководства, паспорта</title></head><body>");
		builder.append("<h1>Перечень руководств, паспортов</h1>");
		
		List<Guide> guides;
		String havingClause = getHavingClause(config);
		String sortClause = " ORDER BY g.due_date DESC ";
		guides = loadGuides(havingClause, sortClause, "");
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("Наименование объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append("Тип объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append("Заводской номер"); builder.append("</td>");
		builder.append("<td>"); builder.append("Регистрационный номер"); builder.append("</td>");
		builder.append("<td>"); builder.append("Заказчик"); builder.append("</td>");
		builder.append("<td>"); builder.append("№ договора"); builder.append("</td>");
		builder.append("<td>"); builder.append("№ экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append("Исполнитель"); builder.append("</td>");
		builder.append("<td>"); builder.append("Срок исполнения"); builder.append("</td>");
		builder.append("<td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("</tr>");
		for (Guide g: guides) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(g.getObjName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getObjTypeName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getFNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getRNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getClientName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getContractNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getActNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getResponsibleName())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(g.getDueDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(g.getNotes())); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}

	@Override
	public String getPrintableGuideCard(int id) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_PRINT, "guides");
		conn = DBConnect.getConnection();
		Guide g = new Guide();
		try {
			PreparedStatement ps = conn.prepareStatement(baseQry + " WHERE g.id="+id);
			ResultSet result = ps.executeQuery();
			result.first();
			g.setObjName(result.getString("objName"));
			g.setObjTypeName(result.getString("objTypeName"));
			g.setFNum(result.getString("fnum"));
			g.setRNum(result.getString("rnum"));
			g.setClientName(result.getString("clientName"));
			g.setContractNum(result.getString("contractNum"));
			g.setActNum(result.getString("actNum"));
			g.setResponsibleName(result.getString("responsibleName"));
			g.setDueDate(result.getDate("dueDate"));
			g.setNotes(result.getString("notes"));
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getPrintableContractCard exception: "+sqle.getMessage());
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка руководства, паспорта</title></head><body>");
		builder.append("<h1>Карточка руководства, паспорта</h1><br><br>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("Наименование объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getObjName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Тип объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getObjTypeName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Заводской номер"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getFNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Регистрационный номер"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getRNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Заказчик"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getClientName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("№ договора"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getContractNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("№ экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getActNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Исполнитель"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getResponsibleName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Срок исполнения"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(g.getDueDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(g.getNotes())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}
	
	private String v(String value) {
		return value == null?"":value;
	}
	
	public List<DeletedObject> getDeletedObjects() {
		List<DeletedObject> deleted = new ArrayList<DeletedObject>();
		List<Guide> deletedGuides = loadGuides(" HAVING deleted=true ", "", "");
		
		for (int idx = 0; idx < deletedGuides.size(); idx++) {
			String descr = "Руководство/паспорт к объекту "+deletedGuides.get(idx).getObjName()+", рег.№ "+deletedGuides.get(idx).getRNum();
			int deleter = deletedGuides.get(idx).getDeletedBy();
			String userName = emplServ.getEmployeeInfo(deleter).getName();
			deleted.add(new DeletedObject("guides_"+idx, "guides", "Журнал руководств, паспортов", deletedGuides.get(idx).getId(), descr, deleter, userName));
		}
		return deleted;
	}
}
