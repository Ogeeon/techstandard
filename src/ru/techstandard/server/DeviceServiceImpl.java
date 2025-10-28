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
import ru.techstandard.client.model.Device;
import ru.techstandard.client.model.Event;
import ru.techstandard.shared.DeviceService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;


public class DeviceServiceImpl extends RemoteServiceServlet implements DeviceService {
	private static final long serialVersionUID = 1L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;
	private String baseQry="SELECT d.id, d.deleted, d.deleted_by, d.title, d.device_type, d.precision, d.range, d.num, d.fnum, d.check_cert checkCert, d.check_period checkPeriod, "+
			"d.last_checked lastChecked, dc.name checker, d.next_check nextCheck, d.groen, d.notes, d.responsible_id, d.task_created, e.name "+
			"FROM devices d LEFT JOIN dictionaries dc ON d.checker_id = dc.id LEFT JOIN employees e ON d.responsible_id=e.id ";

	private List<Device> loadDevices(ResultSet result) {
		List<Device> devices = new ArrayList<Device>(); 
		try {
			while (result.next()) {
				Device dev = new Device(result.getInt("id"));
				dev.setTitle(result.getString("title"));
				dev.setType(result.getString("device_type"));
				dev.setPrecision(result.getString("precision"));
				dev.setRange(result.getString("range"));
				dev.setNum(result.getInt("num"));
				dev.setFnum(result.getString("fnum"));
				dev.setCheckCert(result.getString("checkCert"));
				dev.setCheckPeriod(result.getInt("checkPeriod"));
				dev.setLastChecked(result.getDate("lastChecked"));
				dev.setChecker(result.getString("checker"));
				dev.setNextCheck(result.getDate("nextCheck"));
				dev.setGroen(result.getInt("groen"));
				dev.setNotes(result.getString("notes"));
				dev.setDeleted(result.getBoolean("deleted"));
				dev.setDeletedBy(result.getInt("deleted_by"));
				dev.setResponsibleId(result.getInt("responsible_id"));
				dev.setResponsibleName(result.getString("name"));
				devices.add(dev);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return devices;
	}
	
	public PagingLoadResult<Device> getDevices(FilterPagingLoadConfig config) {
		String havingClause = getHavingClause(config);
		
		String sortClause = " ORDER BY d.id";
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY d.id";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		String limitClause = " LIMIT " + start + ", " + limit;

		List<Device> devices = null;

	    try {
	    	conn = DBConnect.getConnection();
			PreparedStatement ps = conn.prepareStatement(baseQry+havingClause + sortClause + limitClause);
			ResultSet result = ps.executeQuery();
			devices = loadDevices(result);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getDevices exception: "+sqle.getMessage());
		}
		
		return new PagingLoadResultBean<Device>(devices, getCount(havingClause), config.getOffset());
	}

	public Device getDevice(int id) {
		conn = DBConnect.getConnection();
		List<Device> devices = null;
		try {
			PreparedStatement ps = conn.prepareStatement(baseQry+" WHERE d.id="+id);
			ResultSet result = ps.executeQuery();
			devices = loadDevices(result);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getAllDevices exception: "+sqle.getMessage());
		}
		if (devices.size()==0)
			return new Device();
		else
			return devices.get(0);
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
			MyLogger.warning("[DeviceServiceImpl] getCount exception: "+sqle.getMessage());
			MyLogger.warning("[DeviceServiceImpl] getCount exception: "+sqle.toString());
		}
	    return count;
	}

	private String getHavingClause(FilterPagingLoadConfig config) {
//		List<FilterConfig> filters = config.getFilters();
//		if (filters.size() == 0)
//			return "";
//		
//		boolean first = true;
//		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
//		StringBuilder builder = new StringBuilder();
//		builder.append(" HAVING ");
//		for (FilterConfig f : filters) {
//			String type = f.getType();
//			String value = f.getValue();
//			String field = f.getField();
//			if (value == null)
//		          continue; 			 
//			 
//			if (!first)
//				builder.append(" AND ");
//			if ("string".equals(type)) {
//				// Исключения для поля "Выполнено" - отображается текст "все"/"да"/"нет", а в базе - boolean
//				if (field.equals("status")) {
//					builder.append("cn.closed = ");
//					builder.append(value.equals("Нет")?"false":"true");
//				} else {
//					builder.append(field+" LIKE '%");
//					builder.append(value);
//					builder.append("%'");
//				}
//			} else if ("date".equals(type)) {
//				String cmp = f.getComparison().equals("on")?"=":(f.getComparison().equals("before")?"<=":">=");
//				
//				Calendar calendar = Calendar.getInstance();
//			    calendar.setTimeInMillis(Long.valueOf(value));
//				
//				builder.append("cn.signed "+cmp+" '");
//				builder.append(dt.format(calendar.getTime()));
//				builder.append("'");
//			}
//			first = false;
//		}
//		
//		return builder.toString();
		return " HAVING d.deleted=false ";
	}

	private List<Device> getAllDevices(String havingClause) {
		conn = DBConnect.getConnection();
		List<Device> devices = null;
		try {
			PreparedStatement ps = conn.prepareStatement(baseQry+havingClause+"ORDER BY d.next_check");
			ResultSet result = ps.executeQuery();
			devices = loadDevices(result);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getAllDevices exception: "+sqle.getMessage());
		}
		return devices;
	}
	
	@Override
	public int addDevice(Device device) {
		conn = DBConnect.getConnection();
		
		String qry = "INSERT INTO  devices (title, device_type, `precision`, `range`, num, fnum, check_cert, check_period, last_checked, checker_id, next_check, groen, notes, responsible_id) VALUES ("
//				+ DBConnect.saveString(device.getTitle())+", "
//				+ DBConnect.saveString(device.getType())+", "
//				+ DBConnect.saveString(device.getPrecision())+", "
//				+ DBConnect.saveString(device.getRange())+", "
				+ "?, ?, ?, ?, " + device.getNum() + ", ?, ?, " 
//				+ DBConnect.saveString(device.getFnum())+", "
//				+ DBConnect.saveString(device.getCheckCert())+", "
				+ device.getCheckPeriod()+", "
				+ DBConnect.saveDate(device.getLastChecked())+", "
				+ device.getCheckerId()+", "
				+ DBConnect.saveDate(device.getNextCheck())+", "
				+ device.getGroen()+", "
				+ "?, " +
//				+ DBConnect.saveString(device.getNotes())+", "
				+ device.getResponsibleId()+
				")";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
//			System.out.println("addDevice qry="+qry);
			ps.setString(1, device.getTitle());
			ps.setString(2, device.getType());
			ps.setString(3, device.getPrecision());
			ps.setString(4, device.getRange());
			ps.setString(5, device.getFnum());
			ps.setString(6, device.getCheckCert());
			ps.setString(7, device.getNotes());
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addDevice exception:"+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean updateDevice(Device device) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE devices SET title=?, " + //DBConnect.saveString(device.getTitle())+", " +
				"device_type=?, " + //DBConnect.saveString(device.getType())+", " +
				"`precision`=?, " + //DBConnect.saveString(device.getPrecision())+", " +
				"`range`=?, " + //DBConnect.saveString(device.getRange())+", " +
				"num=" + device.getNum() + ", " +
				"fnum=?, " + //DBConnect.saveString(device.getFnum())+", " +
				"check_cert=?, " + //DBConnect.saveString(device.getCheckCert())+", " +
				"check_period=" + device.getCheckPeriod()+", " +
				"last_checked=" + DBConnect.saveDate(device.getLastChecked())+", " +
				"checker_id=" + device.getCheckerId()+", " +
				"next_check=" + DBConnect.saveDate(device.getNextCheck())+", " +
				"groen=" + device.getGroen() +", " +
				"notes=?, " + //DBConnect.saveString(device.getNotes())+", " +
				"responsible_id=" + device.getResponsibleId()+
				" WHERE id="+device.getId();
		try {
//			System.out.println("updateDevice qry="+qry);
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, device.getTitle());
			ps.setString(2, device.getType());
			ps.setString(3, device.getPrecision());
			ps.setString(4, device.getRange());
			ps.setString(5, device.getFnum());
			ps.setString(6, device.getCheckCert());
			ps.setString(7, device.getNotes());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateDevice exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public boolean deleteDevice(int id, boolean markOnly) {
		conn = DBConnect.getConnection();
		try {
			String qry;
	    	if (markOnly) { 
	    		int deleter = Utils.getUserIdFromSession(this.getThreadLocalRequest().getSession());
	    		qry = "UPDATE devices SET deleted=true, deleted_by="+deleter+" WHERE id=";
	    		List<Integer> confirmers = emplServ.getDeleteConfirmers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Объект помечен на удаление",
							"Новый объект в категории \"поверки\" помечен на удаление."));
				}
	    	}
	    	else
	    		qry = "DELETE FROM devices WHERE id=";
			PreparedStatement ps = conn.prepareStatement(qry+id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteDevice exception: "+sqle.getMessage());
		}
		if (markOnly)
			return true;
		
		AttachementServiceImpl attachServ = new AttachementServiceImpl();
		List<Integer> attachmentList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM attachments WHERE parent_id=" + id + " AND parent_type=" + Constants.DEVICE_ATTACHMENTS);
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
		// если выполнение добралось сюда - значит, удаляется объект, к которому относятся вложения, и удаление одобрено
		attachServ.deleteAttachments(attachmentList, false);
		
		return true;
	}

	@Override
	public String getPrintableDeviceCard(int id) {
		conn = DBConnect.getConnection();
		Device dev = new Device(id);
		try {
			PreparedStatement ps = conn.prepareStatement(baseQry+"WHERE d.id="+id);
			ResultSet result = ps.executeQuery();
			result.first();
			dev.setTitle(result.getString("title"));
			dev.setType(result.getString("device_type"));
			dev.setPrecision(result.getString("precision"));
			dev.setRange(result.getString("range"));
			dev.setNum(result.getInt("num"));
			dev.setFnum(result.getString("fnum"));
			dev.setCheckCert(result.getString("checkCert"));
			dev.setCheckPeriod(result.getInt("checkPeriod"));
			dev.setLastChecked(result.getDate("lastChecked"));
			dev.setChecker(result.getString("checker"));
			dev.setNextCheck(result.getDate("nextCheck"));
			dev.setGroen(result.getInt("groen"));
			dev.setNotes(result.getString("notes"));
			dev.setResponsibleName(result.getString("name"));
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getPrintableDeviceCard exception: "+sqle.getMessage());
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка средства измерений</title></head><body>");
		builder.append("<h1>Карточка средства измерений</h1><br><br>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("Наименование, заводское обозначение"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getTitle())); builder.append("</td></tr>");
		builder.append("<trd><td>"); builder.append("Тип"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getType())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Класс точности, погрешность"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getPrecision())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Предел (диапазон) измерений"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getRange())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Кол-во/Кол-во в наборе"); builder.append("</td>");
		builder.append("<td>"); builder.append(dev.getNum()==0?"":dev.getNum()); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Заводской №"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getFnum())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("№ св-ва о поверке"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getCheckCert())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Периодичность поверки (месяцы)"); builder.append("</td>");
		builder.append("<td>"); builder.append(dev.getCheckPeriod()); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Дата последней поверки"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(dev.getLastChecked())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Место проведения поверки"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getChecker())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Сроки проведения поверки"); builder.append("</td>");
		builder.append("<td>"); builder.append(dt.format(dev.getNextCheck())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Сфера ГРОЕН"); builder.append("</td>");
		builder.append("<td>"); builder.append(dev.getGroen()==0?"":dev.getGroen()); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Ответственный"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getResponsibleName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Примечания"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(dev.getNotes())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}

	@Override
	public String getPrintableDeviceList() {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>ГРАФИК поверки (калибровки) средств измерений</title></head><body>");
		builder.append("<h1>ГРАФИК<br>поверки (калибровки) средств измерений, аттестации ИО</h1>");
		
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		List<Device> devices = getAllDevices(" WHERE d.deleted = false ");
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td rowspan=\"2\">"); builder.append("Наименование,<br>заводское<br>обозначение"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Тип"); builder.append("</td>");
		builder.append("<td colspan=\"2\">"); builder.append("Метрологические характеристики"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Кол-во/<br>Кол-во в<br>наборе"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Завод-<br>ской №"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("№ св-ва о<br>поверке"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Периодично-<br>сть поверки<br>(месяцы)"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Дата<br>последней<br>поверки"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Место<br>проведения<br>поверки"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Сроки<br>проведения<br>поверки"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Сфера<br>ГРОЕН"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Ответственный"); builder.append("</td>");
		builder.append("<td rowspan=\"2\">"); builder.append("Примечания"); builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("Класс<br>точности,<br>погрешность"); builder.append("</td>");
		builder.append("<td>"); builder.append("Предел<br>(диапазон)<br>измерений"); builder.append("</td>");
		builder.append("</tr>");
		for (Device d: devices) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(d.getTitle())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getType())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getPrecision())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getRange())); builder.append("</td>");
			builder.append("<td>"); builder.append(d.getNum()==0?"":d.getNum()); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getFnum())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getCheckCert())); builder.append("</td>");
			builder.append("<td>"); builder.append(d.getCheckPeriod()); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(d.getLastChecked())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getChecker())); builder.append("</td>");
			builder.append("<td>"); builder.append(dt.format(d.getNextCheck())); builder.append("</td>");
			builder.append("<td>"); builder.append(d.getGroen()==0?"":d.getGroen()); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getResponsibleName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(d.getNotes())); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	};
	

	private String v(String value) {
		return value == null?"":value;
	}

	public List<DeletedObject> getDeletedObjects() {
		List<Device> deletedDevices = getAllDevices(" WHERE d.deleted = true ");
		List<DeletedObject> deleted = new ArrayList<DeletedObject>();
		
		for (int idx = 0; idx < deletedDevices.size(); idx++) {
			String descr = deletedDevices.get(idx).getTitle()+(deletedDevices.get(idx).getType()==null?"":"("+deletedDevices.get(idx).getType()+")")+" № "+deletedDevices.get(idx).getFnum();
			int deleter = deletedDevices.get(idx).getDeletedBy();
			String userName = emplServ.getEmployeeInfo(deleter).getName();
			deleted.add(new DeletedObject("devices_"+idx, "devices", "Поверки", deletedDevices.get(idx).getId(), descr, deleter, userName));
		}
		return deleted;
	}
}
