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

import ru.techstandard.client.model.ActsJournalRecord;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.Event;
import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.ActsJournalService;
import ru.techstandard.shared.NotAuthorizedException;
import ru.techstandard.shared.NotLoggedInException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class ActsJournalServiceImpl extends RemoteServiceServlet implements ActsJournalService {
	private static final long serialVersionUID = 54708466248638391L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;
	private String baseQry="SELECT a.id, a.deleted, a.deleted_by, a.contract_id, a.work_num workNum, cn.num contractNum, cn.signed contractDate, a.work_date workDate, c.name clientName,"
			+ "c.boss clientBoss, c.inn clientINN, c.address clientAddress, c.phone clientPhone, c.email clientEmail, "
			+ "w.name workSubj, o.name objType, a.obj_name objName, a.obj_fnum objFNum, a.obj_rnum objRNum, a.work_next_date nextWorkDate, a.completed, a.notes "
			+ "FROM acts a INNER JOIN (contracts cn, clients c, dictionaries o, dictionaries w) ON "
			+ "(a.contract_id = cn.id AND cn.client_id = c.id AND cn.subj_id = w.id AND a.obj_type = o.id)";
	
	private void checkIfAllowed(int operation, String onObject) throws NotLoggedInException, NotAuthorizedException {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof UserDTO) {
        	UserDTO loggedUser = (UserDTO) userObj;
        	if (!loggedUser.getAccess().isAllowed(operation, onObject)) {
        		System.out.println("acts: gonna throw NotAuthExc");
//        		throw new NotAuthorizedException("У Вас нет прав на выполнение данной операции ("+
//        				Constants.SECTION_NAMES.get(onObject)+" - "+Constants.OPERATION_NAMES.get(operation)+").");
        	}
        } else {
        	System.out.println("acts: gonna throw NotLoggedExc");
//        	throw new NotLoggedInException();
        }
	}
	
	private List<ActsJournalRecord> loadObjects(ResultSet result) {
		List<ActsJournalRecord> records = new ArrayList<ActsJournalRecord>();
		try {
			while (result.next()) {
				ActsJournalRecord rec = new ActsJournalRecord(result.getInt("id"));
				rec.setContractId(result.getInt("contract_id"));
				rec.setWorkNum(result.getString("workNum"));
				rec.setContractNum(result.getString("contractNum"));
				rec.setContractDate(result.getDate("contractDate"));
				rec.setWorkDate(result.getDate("workDate"));
				rec.setClientName(result.getString("clientName"));
				rec.setWorkSubj(result.getString("workSubj"));
				rec.setObjType(result.getString("objType"));
				rec.setObjName(result.getString("objName"));
				rec.setObjFNum(result.getString("objFNum"));
				rec.setObjRNum(result.getString("objRNum"));
				rec.setNextWorkDate(result.getDate("nextWorkDate"));
				rec.setDaysLeft(getDiffDays(result.getDate("nextWorkDate")));
				rec.setCompleted(result.getInt("completed")==1?"Да":"Нет");
				rec.setDone(result.getInt("completed")==1);
				rec.setClientAddress(result.getString("clientAddress"));
				rec.setClientPhone(result.getString("clientPhone"));
				rec.setClientEmail(result.getString("clientEmail"));
				rec.setClientBoss(result.getString("clientBoss"));
				rec.setNotes(result.getString("notes"));
				rec.setDeleted(result.getBoolean("deleted"));
				rec.setDeletedBy(result.getInt("deleted_by"));
				records.add(rec);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return records;
	}
	
	@Override
	public PagingLoadResult<ActsJournalRecord> getJournalRecords(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_READ, "acts");
		
		List<ActsJournalRecord> records;
		String havingClause = getHavingClause(config);
		String sortClause = "";
		String limitClause = "";
		
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					// Поле в модели JournalRecord (и в grid) называется daysLeft, но в запросе этого нет. Сортировка по дате повторной экспертизы эквивалентна сортировке по оставшимся дням.
					if (sortField.equals("daysLeft"))
						sortField = "nextWorkDate";
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY a.id";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadJournalRecords(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<ActsJournalRecord>(records, getCount(havingClause), config.getOffset());
	}

	private List<ActsJournalRecord> loadJournalRecords(String havingClause, String sortClause, String limitClause) {
		conn = DBConnect.getConnection();
		List<ActsJournalRecord> records=null;
	    try {
			PreparedStatement ps = conn.prepareStatement(baseQry+havingClause+sortClause+limitClause);
//			System.out.println("clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			records = loadObjects(result);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadrecords exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] loadJournalRecords exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] loadJournalRecords exception: "+sqle.toString());
		}
	    return records;
	}
	
	public List<ActsJournalRecord> getJournalRecordsByContract(int id) {
		String havingClause = " HAVING a.contract_id="+id; 
		return loadJournalRecords(havingClause, "", "");
	}
	
	public ActsJournalRecord getJournalRecord(int id) {
		String havingClause = " HAVING a.id="+id; 
		return loadJournalRecords(havingClause, "", "").get(0);
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
			System.out.println("getcount exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] getCount exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] getCount exception: "+sqle.toString());
		}
	    return count;
	}
	
	private Integer getDiffDays(Date target) {
		if (target == null)
			return 0;
		Calendar calendar1 = Calendar.getInstance();
	    Calendar calendar2 = Calendar.getInstance();
	    calendar1.setTime(new Date());
	    calendar2.setTime(target);
	    long milliseconds1 = calendar1.getTimeInMillis();
	    long milliseconds2 = calendar2.getTimeInMillis();
	    long diff = milliseconds2 - milliseconds1;
	    int diffDays = (int) Math.ceil(diff / (24 * 60 * 60 * 1000.0));
	    return diffDays;
	}

	private String getHavingClause(FilterPagingLoadConfig config) {
		List<FilterConfig> filters = config.getFilters();
		if (filters.size() == 0)
			return " HAVING deleted=false ";
		
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder builder = new StringBuilder();
		builder.append(" HAVING deleted=false");
		for (FilterConfig f : filters) {
			String type = f.getType();
			String value = f.getValue();
			String field = f.getField();
			if (value == null)
		          continue; 			 
			 
			builder.append(" AND ");
			if ("string".equals(type)) {
				// Исключения для поля "Выполнено" - отображается текст "все"/"да"/"нет", а в базе - boolean
				if (field.equals("completed")) {
					builder.append("a.completed = ");
					builder.append(value.equals("Нет")?"false":"true");
				} else {
					builder.append(field+" LIKE '%");
					builder.append(value);
					builder.append("%'");
				}
			} else if ("date".equals(type)) {
				String cmp = f.getComparison().equals("on")?"=":(f.getComparison().equals("before")?"<=":">=");
				
				Calendar calendar = Calendar.getInstance();
			    calendar.setTimeInMillis(Long.valueOf(value));
				
				builder.append("a.work_date "+cmp+" '");
				builder.append(dt.format(calendar.getTime()));
				builder.append("'");
			}
		}
		
		return builder.toString();
	};

	@Override
	public int addJournalRecord(ActsJournalRecord record) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_INSERT, "acts");
		String qry;
		qry = "INSERT INTO acts (contract_id, work_num, work_date, obj_type, obj_name, obj_fnum, obj_rnum, work_next_date, completed, notes) VALUES ("
				+ record.getContractId() + ", " + "?, "
				+ DBConnect.saveDate(record.getWorkDate())+", "
				+ record.getObjTypeId()+", "
				+ "?, ?, ?, "
				+ DBConnect.saveDate(record.getNextWorkDate())+", "
				+ record.isDone() + ", "
				+ "?)";
//		System.out.println("add qry="+qry);
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, record.getWorkNum());
			ps.setString(2, record.getObjName());
			ps.setString(3, record.getObjFNum());
			ps.setString(4, record.getObjRNum());
			ps.setString(5, record.getNotes());
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addJournalRecord exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] addJournalRecord exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] addJournalRecord exception: "+sqle.toString());
		}
		return 0;
	}
	
	@Override
	public boolean deleteJournalRecord(int recordID, boolean markOnly) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_DELETE, "acts");
		conn = DBConnect.getConnection();
	    try {
	    	String qry;
	    	if (markOnly) {
	    		int deleter = Utils.getUserIdFromSession(this.getThreadLocalRequest().getSession());
	    		qry = "UPDATE acts SET deleted=true, deleted_by="+deleter+" WHERE id=";
	    		List<Integer> confirmers = emplServ.getDeleteConfirmers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Объект помечен на удаление",
							"Новый объект в категории \"экспертизы, акты\" помечен на удаление."));
				}
	    	}
	    	else
	    		qry = "DELETE FROM acts WHERE id=";
			PreparedStatement ps = conn.prepareStatement(qry+recordID);
			ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			System.out.println("deleteJournalRecord exception ["+sqle.getErrorCode()+"]: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] deleteJournalRecord exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] deleteJournalRecord exception: "+sqle.toString());
		}
		if (markOnly)
			return true;
		
	    AttachementServiceImpl attachServ = new AttachementServiceImpl();
		List<Integer> attachmentList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM attachments WHERE parent_id=" + recordID + " and parent_type="+Constants.ACT_ATTACHMENTS);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				attachmentList.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteAttachments getfilename exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] deleteJournalRecord exception: "+sqle.getMessage());
		}
//		System.out.println("attachmentList = "+attachmentList.toString());
		attachServ.deleteAttachments(attachmentList);
		
		return true;
	}

	@Override
	public boolean updateJournalRecord(ActsJournalRecord record) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_UPDATE, "acts");
		String qry;
		qry = "UPDATE acts SET " +
				"contract_id="+record.getContractId()+", "+
				"work_num=?, "+
				"work_date="+DBConnect.saveDate(record.getWorkDate())+", "+
				"obj_type="+record.getObjTypeId()+", "+
				"obj_name=?, "+
				"obj_fnum=?, "+
				"obj_rnum=?, "+
				"work_next_date="+DBConnect.saveDate(record.getNextWorkDate())+", "+
				"completed="+record.getCompleted()+", "+	// из обработчика кнопки "сохранить" приходит запись, у которой в этом поле 0/1
				"notes=?"+
				" WHERE id="+record.getId();
		
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, record.getWorkNum());
			ps.setString(2, record.getObjName());
			ps.setString(3, record.getObjFNum());
			ps.setString(4, record.getObjRNum());
			ps.setString(5, record.getNotes());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateJournalRecord exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] updateJournalRecord exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] updateJournalRecord exception: "+sqle.toString());
		}
		return false;
	}

	@Override
	public String getPrintableJournal(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_PRINT, "acts");
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Журнал</title></head><body>");
		builder.append("<h1>Журнал экспертиз и актов</h1>");
		
		List<ActsJournalRecord> records;
		String havingClause = getHavingClause(config);
		String sortClause = "";
		
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					// Поле в модели JournalRecord (и в grid) называется daysLeft, но в запросе этого нет. Сортировка по дате повторной экспертизы эквивалентна сортировке по оставшимся дням.
					if (sortField.equals("daysLeft"))
						sortField = "nextWorkDate";
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY a.id";
		}
				
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		records = loadJournalRecords(havingClause, sortClause, "");
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("Номер<br>экспертизы,<br>акта"); builder.append("</td>");
		builder.append("<td>"); builder.append("Номер<br>договора"); builder.append("</td>");
		builder.append("<td>"); builder.append("Дата<br>заключения<br>договора"); builder.append("</td>");
		builder.append("<td>"); builder.append("Дата<br>проведения<br>экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append("Наименование<br>контрагента"); builder.append("</td>");
		builder.append("<td>"); builder.append("Предмет<br>договора"); builder.append("</td>");
		builder.append("<td>"); builder.append("Тип объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append("Марка объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append("Зав. №"); builder.append("</td>");
		builder.append("<td>"); builder.append("Рег. №"); builder.append("</td>");
		builder.append("<td>"); builder.append("ФИО<br>руководителя"); builder.append("</td>");
		builder.append("<td>"); builder.append("Дата<br>повторной<br>экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append("Осталось до<br>повторной<br>экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append("Выполнение"); builder.append("</td>");
		builder.append("</tr>");
		for (ActsJournalRecord r: records) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(r.getWorkNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getContractNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(r.getContractDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(r.getWorkDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getClientName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getWorkSubj())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getObjType())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getObjName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getObjFNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getObjRNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(r.getClientBoss())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(r.getNextWorkDate())); builder.append("</td>");
			builder.append("<td>"); builder.append(r.getDaysLeft()); builder.append("</td>");
			builder.append("<td>"); builder.append(r.getCompleted()); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}
	
	private String v(String value) {
		return value == null?"":value;
	}

	@Override
	public String getPrintableActCard(int id) throws NotLoggedInException, NotAuthorizedException {
		checkIfAllowed(Constants.ACCESS_PRINT, "acts");
		conn = DBConnect.getConnection();
		ActsJournalRecord rec = null;
		try {
			PreparedStatement ps = conn.prepareStatement(baseQry+" WHERE a.id="+id);
			ResultSet result = ps.executeQuery();
			result.beforeFirst();
			List<ActsJournalRecord> records = loadObjects(result);
			rec = records.get(0);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getPrintableContractCard exception: "+sqle.getMessage());
			MyLogger.warning("[ActsJournalServiceImpl] getPrintableContractCard exception: "+sqle.toString());
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка акта/экспертизы</title></head><body>");
		builder.append("<h1>Карточка акта/экспертизы</h1><br><br>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("Номер акта, экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getWorkNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Номер договора"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getContractNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата заключения договора"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(rec.getContractDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата проведения экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(rec.getWorkDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Наименование контрагента"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getClientName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Предмет договора"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getWorkSubj())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Тип объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getObjType())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Марка объекта"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getObjName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Заводской №"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getObjFNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Регистрационный №"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getObjRNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата повторной экспертизы"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(rec.getNextWorkDate())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Выполнено"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getCompleted())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(rec.getNotes())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}

	public List<DeletedObject> getDeletedObjects() {
		List<DeletedObject> deleted = new ArrayList<DeletedObject>();
		List<ActsJournalRecord> deletedActs = loadJournalRecords(" HAVING deleted=true ", "", "");
		
		for (int idx = 0; idx < deletedActs.size(); idx++) {
			SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
			String descr = "Экспертиза/акт "+deletedActs.get(idx).getWorkNum() + " от " + dt.format(deletedActs.get(idx).getWorkDate()) + 
					" по договору № " + deletedActs.get(idx).getContractNum() + " c " + deletedActs.get(idx).getClientName();
			int deleter = deletedActs.get(idx).getDeletedBy();
			String userName = emplServ.getEmployeeInfo(deleter).getName();
			deleted.add(new DeletedObject("acts_"+idx, "acts", "Журнал экспертиз, актов", deletedActs.get(idx).getId(), descr, deleter, userName));
		}
		return deleted;
	}
}
