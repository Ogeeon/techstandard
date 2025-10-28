package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.Event;
import ru.techstandard.shared.DictionaryService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;


public class DictionaryServiceImpl extends RemoteServiceServlet implements DictionaryService {
	private static final long serialVersionUID = 9216237923015976649L;
	EventServiceImpl eventServ = new EventServiceImpl();
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	private Connection conn = null;
	
	public List<DictionaryRecord> getDictionaryContents(int type) throws IllegalArgumentException {
		List<DictionaryRecord> dictRecords = new ArrayList<DictionaryRecord>();
		conn = DBConnect.getConnection();

	    try {
			PreparedStatement ps = conn.prepareStatement("select id, name from dictionaries where deleted=false AND type = " + String.valueOf(type) + " ORDER BY name");
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				dictRecords.add(new DictionaryRecord(result.getInt(1), result.getString(2)));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getDictionaryContents exception: "+sqle.getMessage());
			throw new IllegalArgumentException(sqle.getMessage()+sqle.toString());
		}
	    
	    return dictRecords;
	}

	@Override
	public int addRecord(int type, String value) {
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO dictionaries (deleted, type, name) VALUES (0, "+String.valueOf(type)+", ?)", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, value);
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addRecord exception: "+sqle.getMessage());
		}
		return 0;
	}

	private int getCount(int type) {
		int count = 0; 
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM dictionaries WHERE deleted=false AND type="+String.valueOf(type));
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount exception: "+sqle.getMessage());
		}
	    return count;
	}
	
	@Override
	public PagingLoadResult<DictionaryRecord> getDictContPaged(int type, PagingLoadConfig config) {
		conn = DBConnect.getConnection();
		
		int start = config.getOffset();
		int limit = config.getLimit();
		String limitClause = " LIMIT " + start + ", " + limit;
		
		List<DictionaryRecord> dictRecords = new ArrayList<DictionaryRecord>();

	    try {
			PreparedStatement ps = conn.prepareStatement("select id, name from dictionaries where deleted=false AND type = " + String.valueOf(type) + " ORDER BY name" + limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				dictRecords.add(new DictionaryRecord(result.getInt(1), result.getString(2)));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getDictContPaged exception: "+sqle.getMessage());
		}
		
		return new PagingLoadResultBean<DictionaryRecord>(dictRecords, getCount(type), config.getOffset());
	}

	@Override
	public boolean deleteRecord(int id, boolean markOnly) throws IllegalArgumentException {
		conn = DBConnect.getConnection();
		try {
	    	String qry;
	    	if (markOnly) {
	    		int deleter = Utils.getUserIdFromSession(this.getThreadLocalRequest().getSession());
	    		qry = "UPDATE dictionaries SET deleted=true, deleted_by="+deleter+" WHERE id=";
	    		List<Integer> confirmers = emplServ.getDeleteConfirmers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Объект помечен на удаление",
							"Новый объект в категории \"справочники\" помечен на удаление."));
				}
	    	}
	    	else
	    		qry = "DELETE FROM dictionaries WHERE id=";
			PreparedStatement ps = conn.prepareStatement(qry+id);
			int rows = ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("deleteRecord exception ["+sqle.getErrorCode()+"]: "+sqle.getMessage());
			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах, использующих данное значение справочника.");
		}
		return false;
	}

	@Override
	public boolean updateRecord(int id, String value) {
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE dictionaries SET name=? WHERE id="+id);
			ps.setString(1, value);
			int rows = ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateRecord exception: "+sqle.getMessage());
		}
		return false;
	}

	public DictionaryRecord getOrCreateRecByName(int type, String name) {
		conn = DBConnect.getConnection();
		DictionaryRecord rec = null;
//		System.out.println("getOrCreateRecByName("+type+", "+name+")");
		try {
			if (name == null)
				return new DictionaryRecord();
			PreparedStatement ps = conn.prepareStatement("select * from dictionaries where type=? AND name=?");
			ps.setInt(1, type);
			ps.setString(2, name);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				rec = new DictionaryRecord(result.getInt("id"), result.getString("name"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getOrCreateRecByName get rec exception: "+sqle.getMessage());
		}
		if (rec != null) {
			return rec;
		} else {
			int id = addRecord(type, name);
			try {
				PreparedStatement ps = conn.prepareStatement("select id, name from dictionaries where id="+id);
				ResultSet result = ps.executeQuery();
				while (result.next()) {
					rec = new DictionaryRecord(result.getInt(1), result.getString(2));
				}
				result.close();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("getOrCreateRecByName create exception: "+sqle.getMessage());
			}
		}
		return rec;
	}
	
	@Override
	public String getPrintableDictionaryContents(int type) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Справочник</title></head><body>");
		builder.append("<h1>Перечень ");
		builder.append(type == 1 ? "предметов договоров" : (type == 2 ? "типов объектов" : "типов документов"));
		builder.append("</h1>");
		
		List<DictionaryRecord> dictRecords = getDictionaryContents(type);
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td><b>"); builder.append("Значение записи справочника"); builder.append("</b></td>");
		builder.append("</tr>");
		for (DictionaryRecord dr: dictRecords) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(dr.getName()); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}
	
	public List<DeletedObject> getDeletedObjects() {
		List<DeletedObject> deleted = new ArrayList<DeletedObject>();
		List<DictionaryRecord> deletedDictRecords = new ArrayList<DictionaryRecord>();
		conn = DBConnect.getConnection();
	    try {
			PreparedStatement ps = conn.prepareStatement("select * from dictionaries where deleted=true");
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				DictionaryRecord rec = new DictionaryRecord(result.getInt("id"), result.getString("name"));
				rec.setType(result.getInt("type"));
				rec.setDeleted(result.getBoolean("deleted"));
				rec.setDeletedBy(result.getInt("deleted_by"));
				deletedDictRecords.add(rec);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getDictionaryContents exception: "+sqle.getMessage());
			throw new IllegalArgumentException(sqle.getMessage()+sqle.toString());
		}
		
		for (int idx = 0; idx < deletedDictRecords.size(); idx++) {
			String type = "";
			switch (deletedDictRecords.get(idx).getType()) {
				case Constants.DICT_WORKSUBJS: type = "Виды работ"; break;
				case Constants.DICT_OBJTYPES: type = "Типы объектов"; break;
				case Constants.DICT_CHECKERS: type = "Места проведения поверок"; break;
				case Constants.DICT_ATTACHTYPES: type = "Типы вложений"; break;
				case Constants.DICT_EVAL_FIELDS: type = "Области аттестации"; break;
				case Constants.DICT_POSITIONS: type = "Должности"; break;
				case Constants.DICT_TASKTYPES: type = "Типы заданий"; break;
			}
			
			String descr = "Раздел справочника \""+type+"\", значение: "+deletedDictRecords.get(idx).getName();
			int deleter = deletedDictRecords.get(idx).getDeletedBy();
			String userName = emplServ.getEmployeeInfo(deleter).getName();
			deleted.add(new DeletedObject("dictionaries_"+idx, "dictionaries", "Справочники", deletedDictRecords.get(idx).getId(), descr, deleter, userName));
		}
		return deleted;
	}
}
