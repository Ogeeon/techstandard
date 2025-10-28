package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.Event;
import ru.techstandard.client.model.TemplateContract;
import ru.techstandard.shared.ContractService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class ContractServiceImpl extends RemoteServiceServlet implements ContractService {
	private static final long serialVersionUID = 54708466248638391L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;
	private String baseQry = "SELECT cn.id, cn.deleted, cn.deleted_by, cn.num, cn.signed, cn.expires, cn.notes, cn.closed status, cn.client_id, cl.name clientName, e.name responsibleName, d.name workSubj "+
			"FROM contracts cn INNER JOIN (clients cl, employees e, dictionaries d) ON (cn.client_id = cl.id AND cn.responsible_id = e.id AND cn.subj_id = d.id) ";
	
	@Override
	public PagingLoadResult<Contract> getContracts(FilterPagingLoadConfig config) {
		List<Contract> records;
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
			sortClause = " ORDER BY cn.expires DESC";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadContracts(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Contract>(records, getCount(havingClause), config.getOffset());
	}

	@Override
	public List<Contract> getContractsForClient(int clientId) {
		String havingClause = " WHERE cn.client_id = "+clientId;
		String sortClause = " ORDER BY cn.signed DESC ";
		
		return loadContracts(havingClause, sortClause, "");
	}
	
	@Override
	public Contract getContractById(int id) {
		String havingClause = " WHERE cn.id = "+id;
		List<Contract> contracts = loadContracts(havingClause, "", "");
		if (contracts.size() == 0)
			return new Contract();
		else
			return contracts.get(0);
	}
	
	private List<Contract> loadContracts(String havingClause, String sortClause, String limitClause) {
		conn = DBConnect.getConnection();
		List<Contract> records = new ArrayList<Contract>();
	    try {
			PreparedStatement ps = conn.prepareStatement(baseQry+havingClause+sortClause+limitClause);
//			System.out.println("loadContracts clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Contract cnt = new Contract(result.getInt("id"));
				cnt.setClientName(result.getString("clientName"));
				cnt.setClientID(result.getInt("client_id"));
				cnt.setWorkSubj(result.getString("workSubj"));
				cnt.setResponsibleName(result.getString("responsibleName"));
				cnt.setNum(result.getString("num"));
				cnt.setSigned(result.getDate("signed"));
				cnt.setExpires(result.getDate("expires"));
				cnt.setClosed(result.getInt("status")==1?true:false);
				cnt.setNotes(result.getString("notes"));
				cnt.setDeleted(result.getBoolean("deleted"));
				cnt.setDeletedBy(result.getInt("deleted_by"));
				records.add(cnt);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadrecords exception: "+sqle.getMessage());
			MyLogger.warning("[ContractServiceImpl] loadContracts exception: "+sqle.getMessage());
			MyLogger.warning("[ContractServiceImpl] loadContracts exception: "+sqle.toString());
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
			MyLogger.warning("[ContractServiceImpl] getCount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[ContractServiceImpl] getCount(s) exception: "+sqle.toString());
		}
	    return count;
	}
	
	public int getCount(int id) {
		int count = 0;
		conn = DBConnect.getConnection();

	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM ("+baseQry+" WHERE cn.deleted=false AND cn.client_id="+id+") as t");
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount(i) exception: "+sqle.getMessage());
			MyLogger.warning("[ContractServiceImpl] getCount(i) exception: "+sqle.getMessage());
			MyLogger.warning("[ContractServiceImpl] getCount(i) exception: "+sqle.toString());
		}
	    return count;
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
				// Исключения для поля "Выполнено" - фильтр использует текст на радиокнопках: "все"/"да"/"нет", а в базе - boolean
				if (field.equals("status")) {
					builder.append("cn.closed = ");
					builder.append(value.equals("Нет")?"false":"true");
				} else 
				// Исключение для поля "id клиента"
					if (field.equals("clientIdStr")) {
						builder.append("cn.client_id = "+value);
				} else {
					builder.append(field+" LIKE '%");
					builder.append(value);
					builder.append("%'");
				}
			} else if ("date".equals(type)) {
				String cmp = f.getComparison().equals("on")?"=":(f.getComparison().equals("before")?"<=":">=");
				
				Calendar calendar = Calendar.getInstance();
			    calendar.setTimeInMillis(Long.valueOf(value));
				
				builder.append("cn.signed "+cmp+" '");
				builder.append(dt.format(calendar.getTime()));
				builder.append("'");
			}
		}
		
		return builder.toString();
	};
	
	@Override
	public boolean deleteContract(int recordID, boolean markOnly) {
		conn = DBConnect.getConnection();
		try {
			String qry;
	    	if (markOnly) { 
	    		int deleter = Utils.getUserIdFromSession(this.getThreadLocalRequest().getSession());
	    		qry = "UPDATE contracts SET deleted=true, deleted_by="+deleter+" WHERE id=";
	    		List<Integer> confirmers = emplServ.getDeleteConfirmers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Объект помечен на удаление",
							"Новый объект в категории \"договоры\" помечен на удаление."));
				}
	    	}
	    	else
	    		qry = "DELETE FROM contracts WHERE id=";
			PreparedStatement ps = conn.prepareStatement(qry+recordID);
			ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах для данного договора.");
			System.out.println("deleteContract exception: "+sqle.getMessage());
		}
		if (markOnly)
			return true;
		
		AttachementServiceImpl attachServ = new AttachementServiceImpl();
		List<Integer> attachmentList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM attachments WHERE parent_id=" + recordID + " and parent_type="+Constants.CONTRACT_ATTACHMENTS);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				attachmentList.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteAttachments getfilename exception: "+sqle.getMessage());
		}
//		System.out.println("attachmentList = "+attachmentList.toString());
		attachServ.deleteAttachments(attachmentList);
		
		return true;
	}

	@Override
	public int addContract(Contract contract) {
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO contracts (client_id, subj_id, responsible_id, num, signed, expires, closed, notes) VALUES ("+
				contract.getClientID() + ", " +
				contract.getSubjID() + ", " +
				contract.getResponsibleID() + ", " +
//				DBConnect.saveString(contract.getNum()) + ", " +
				"?, "+
				DBConnect.saveDate(contract.getSigned())+", " +
				DBConnect.saveDate(contract.getExpires())+", " +
				contract.isClosed() + ", " +
//				DBConnect.saveString(contract.getNotes()) +
				"?)";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, contract.getNum());
			ps.setString(2, contract.getNotes());
//			System.out.println("addContract qry="+qry);
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addContract exception:"+sqle.getMessage());
		}
		return 0;
	}
	
	@Override
	public boolean updateContract(Contract contract) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE contracts SET client_id=" + contract.getClientID() + ", " +
					"subj_id =" + contract.getSubjID() + ", " +
					"responsible_id =" + contract.getResponsibleID() + ", " +
					"num =?, " + //DBConnect.saveString(contract.getNum()) + ", " +
					"signed = " + DBConnect.saveDate(contract.getSigned())+", " +
					"expires = " + DBConnect.saveDate(contract.getExpires())+", " +
					"closed = " + contract.isClosed() + ", " +
					"notes = ?" + //DBConnect.saveString(contract.getNotes()) +
					" WHERE id=" + contract.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, contract.getNum());
			ps.setString(2, contract.getNotes());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateContract exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public String getPrintableContractList(FilterPagingLoadConfig config) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Договора</title></head><body>");
		builder.append("<h1>Перечень договоров</h1>");
		
		List<Contract> contracts;
		String havingClause = getHavingClause(config);
		String sortClause = " ORDER BY cn.signed";
		contracts = loadContracts(havingClause, sortClause, "");
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("Номер договора"); builder.append("</td>");
		builder.append("<td>"); builder.append("Дата заключения"); builder.append("</td>");
		builder.append("<td>"); builder.append("Предмет договора"); builder.append("</td>");
		builder.append("<td>"); builder.append("Наименование контрагента"); builder.append("</td>");
		builder.append("<td>"); builder.append("Ответственный"); builder.append("</td>");
		builder.append("<td>"); builder.append("Срок истечения"); builder.append("</td>");
		builder.append("<td>"); builder.append("Статус"); builder.append("</td>");
		builder.append("<td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("</tr>");
		for (Contract c: contracts) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(c.getNum())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(c.getSigned())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getWorkSubj())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getClientName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getResponsibleName())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(c.getExpires())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getStatus())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getNotes())); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}

	private String v(String value) {
		return value == null?"":value;
	}

	@Override
	public String getPrintableContractCard(int id) {
		conn = DBConnect.getConnection();
		Contract c = getContractById(id);
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка договора</title></head><body>");
		builder.append("<h1>Карточка договора</h1><br><br>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("Номер договора"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getNum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата заключения"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(c.getSigned())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Предмет договора"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getWorkSubj())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Наименование контрагента"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getClientName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Ответственный"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getResponsibleName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Срок истечения"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(c.getExpires())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Статус"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getStatus())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}

	@Override
	public int storeTemplate(TemplateContract template) {
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO templates (client_id, num, signer, signed, foundation, subject, duration, "
				+ "prepay, unit_name, unit_price, total_price, due_date, multiple_items) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, template.getClientId());
			ps.setString(2, template.getNum());
			ps.setString(3, template.getSigner());
			ps.setTimestamp(4, new Timestamp(template.getSigned().getTime()));
			ps.setString(5, template.getFoundation());
			ps.setString(6, template.getSubject());
			ps.setInt(7, template.getDuration());
			ps.setInt(8, template.getPrePay());
			ps.setString(9, template.getUnitName());
			ps.setDouble(10, template.getUnitPrice()==null ? 0.0 : template.getUnitPrice());
			ps.setDouble(11, template.getTotalPrice());
			ps.setTimestamp(12, new Timestamp(template.getDueDate().getTime()));
			ps.setBoolean(13, template.isMultipleItems());
			
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("storeTemplate exception:"+sqle.getMessage());
		}
		return 0;
	}

	public List<DeletedObject> getDeletedObjects() {
		List<DeletedObject> deleted = new ArrayList<DeletedObject>();
		List<Contract> deletedContracts = loadContracts(" HAVING deleted=true ", "", "");
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		
		for (int idx = 0; idx < deletedContracts.size(); idx++) {
			String descr = "Договор № " + deletedContracts.get(idx).getNum() + " от " + dt.format(deletedContracts.get(idx).getSigned()) + " с " + deletedContracts.get(idx).getClientName();
			int deleter = deletedContracts.get(idx).getDeletedBy();
			String userName = emplServ.getEmployeeInfo(deleter).getName();
			deleted.add(new DeletedObject("contracts_"+idx, "contracts", "Журнал договоров", deletedContracts.get(idx).getId(), descr, deleter, userName));
		}
		return deleted;
	}
}
