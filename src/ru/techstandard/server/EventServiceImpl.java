package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.DefaultBroadcasterFactory;

import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.Event;
import ru.techstandard.shared.EventService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

/*
 * Генерируемые события:
 *  из TaskServiceImpl - создание задания, удаление, обновление, отправка на утверждение, подтверждение/отклонение выполнения, отзыв из выполненных 
 */
public class EventServiceImpl extends RemoteServiceServlet implements EventService {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	
	@Override
	public PagingLoadResult<Event> getEvents(int employeeId, PagingLoadConfig config) {
		conn = DBConnect.getConnection();
		List<Event> events = new ArrayList<Event>();
		int start = config.getOffset();
		int limit = config.getLimit();
		String limitClause = " LIMIT " + start + ", " + limit;
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM events e WHERE recepient_id="+employeeId + limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Event e = new Event(result.getInt("id"), result.getTimestamp("created"), result.getInt("recepient_id"), result.getString("title"), result.getString("description"));
				events.add(e);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getEvents exception: "+sqle.getMessage());
			MyLogger.warning("[EventServiceImpl] getEvents exception: "+sqle.getMessage());
			MyLogger.warning("[EventServiceImpl] getEvents exception: "+sqle.toString());
		}
	    return new PagingLoadResultBean<Event>(events, getCount(employeeId), config.getOffset());
	}

	private int getCount(int employeeId) {
		int count = 0; 
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM events e WHERE recepient_id=" + employeeId);
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
	public void addEvent(Event event) {
		conn = DBConnect.getConnection();
//		System.out.println("adding event "+event);
		String qry = "INSERT INTO events (created, recepient_id, title, description) VALUES ("+
				DBConnect.saveDateTime(event.getCreated()) + ", " +
				event.getRecepientId() + ", ?, ?)";
//				DBConnect.saveString(event.getTitle()) + ", " +
//				DBConnect.saveString(event.getDescription()) +
//				")";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, event.getTitle());
			ps.setString(2, event.getDescription());
//			System.out.println("addEvent qry="+qry);
			ps.executeUpdate();
			
			Employee recepient = emplServ.getEmployeeInfo(event.getRecepientId());
			if (recepient != null) {
    			if (recepient.getEmail() == null || recepient.getEmail().isEmpty()) {
    				List<Integer> admins = emplServ.getDeleteConfirmers();
    				if (admins.size() == 0) // не должно такого быть
    					return;
    				addEvent(new Event(new Date(), admins.get(0), "Ошибка отправки уведомления по почте", "У сотрудника "+recepient.getName()+" не указан адрес электронной почты."));
    				return;
    			}
			}
			
			/*
			Properties props = System.getProperties();
			props.put("mail.smtp.host", "smtp.mail.ru");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.ssl.enable", "true");
			Session session = Session.getDefaultInstance(props, new Authenticator(){
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("tsmailer", "TS_mailer2014");
				}
			});
			
			// create a new mail session and the mime message
			MimeMessage mime = new MimeMessage(session);
			mime.setText(event.getDescription());
//			mime.setContent(multiPart);
			mime.setSubject(event.getTitle());
			mime.setFrom(new InternetAddress("tsmailer@mail.ru"));
			mime.addRecipient(Message.RecipientType.TO, new InternetAddress(recepient.getEmail()));
			// send the message
			Transport.send(mime);
			
			LinkedHashMap<String, Object> notification = new LinkedHashMap<String, Object>();
			notification.put("title", event.getTitle());
			notification.put("message", event.getDescription());
			@SuppressWarnings("deprecation")
			Broadcaster brc = DefaultBroadcasterFactory.getDefault().lookup("NotifBroadcaster_"+event.getRecepientId());
            if (brc != null) {
            	brc.broadcast(notification);
            }
            */
			
		} catch (SQLException sqle) {
			System.out.println("addEvent exception:"+sqle.getMessage());
		}
	}

	@Override
	public void removeEvent(int id) {
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE id="+id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("removeEvent exception: "+sqle.getMessage());
		}
	}

	@Override
	public int getEventsCount(int employeeId) {
		int count = 0; 
		conn = DBConnect.getConnection();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM events e WHERE recepient_id=" + employeeId);
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getEventsCount exception: "+sqle.getMessage());
		}
	    return count;
	}

}
