package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import ru.techstandard.client.MyBeanFactory;
import ru.techstandard.client.model.ChatMsg;
import ru.techstandard.shared.ChatLoggerService;
import ru.techstandard.shared.NotAuthorizedException;
import ru.techstandard.shared.NotLoggedInException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class ChatLoggerServiceImpl extends RemoteServiceServlet implements ChatLoggerService {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;

	MyBeanFactory factory = AutoBeanFactorySource.create(MyBeanFactory.class);
	
	@Override
	public PagingLoadResult<ChatMsg> getChatMessages(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException {
		List<ChatMsg> records;
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
			sortClause = " ORDER BY l.timestamp";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		limitClause = " LIMIT " + start + ", " + limit;
				
		records = loadMessages(havingClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<ChatMsg>(records, getCount(havingClause), config.getOffset());
	}


	private List<ChatMsg> loadMessages(String havingClause, String sortClause, String limitClause) {
		conn = DBConnect.getConnection();
		List<ChatMsg> records = new ArrayList<ChatMsg>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM chatlogs l"+havingClause+sortClause+limitClause);
//			System.out.println("loadMessages clauses: "+ havingClause+sortClause+limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				ChatMsg m = new ChatMsg();
				m.setId(result.getInt("id"));
				m.setTimeStamp(result.getLong("timestamp"));
				m.setDate(new Date(result.getLong("timestamp")));
				m.setAuthor(result.getString("author"));
				m.setRoom(result.getString("room"));
				m.setMessage(result.getString("message"));
				records.add(m);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("loadMessages exception: "+sqle.getMessage());
			MyLogger.warning("[ChatLoggerServiceImpl] loadMessages exception: "+sqle.getMessage());
			MyLogger.warning("[ChatLoggerServiceImpl] loadMessages exception: "+sqle.toString());
		}
	    return records;
	}
	
	private int getCount(String havingClause) {
		int count = 0;
		conn = DBConnect.getConnection();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM chatlogs l "+havingClause);
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[ChatLoggerServiceImpl] getCount(s) exception: "+sqle.getMessage());
			MyLogger.warning("[ChatLoggerServiceImpl] getCount(s) exception: "+sqle.toString());
		}
	    return count;
	}
	
	private String getHavingClause(FilterPagingLoadConfig config) {
		List<FilterConfig> filters = config.getFilters();
		if (filters.size() == 0)
			return "";
		
		boolean first = true;
		StringBuilder builder = new StringBuilder();
		builder.append(" WHERE ");
		for (FilterConfig f : filters) {
			String type = f.getType();
			String value = f.getValue();
			String field = f.getField();
			if (value == null)
		          continue; 			 
			 
			if (!first)
				builder.append(" AND ");
			if ("string".equals(type)) {
				builder.append(field+" LIKE '%");
				builder.append(value);
				builder.append("%'");
			} else if ("date".equals(type)) {
				String cmp = f.getComparison().equals("on")?"=":(f.getComparison().equals("before")?"<=":">=");
				builder.append("timestamp "+cmp+Long.valueOf(value));
			} 
			first = false;
		}
		
		return builder.toString();
	};
	
	
	public void saveLog(LinkedHashMap<String, Object> message) {
		conn = DBConnect.getConnection();
		try {
//			System.out.println("saving "+message);
			PreparedStatement ps = conn.prepareStatement("INSERT INTO chatlogs (author, timestamp, room, message) VALUES (?, ?, ?, ?)");
			ps.setString(1, message.get("author")==null ? "" : (String) message.get("author"));
			ps.setLong(2, message.get("timeStamp")==null ? 0: Long.valueOf((String) message.get("timeStamp")));
			ps.setString(3, (String) message.get("room")==null ? "" : (String) message.get("room"));
			ps.setString(4, (String) message.get("message")==null ? "" : (String) message.get("message"));
			ps.executeUpdate();
		} catch (SQLException sqle) {
			System.out.println("saveLog exception:"+sqle.getMessage());
		}
	}
}
