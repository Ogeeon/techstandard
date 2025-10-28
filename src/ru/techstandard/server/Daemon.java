package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Event;
import ru.techstandard.client.model.Task;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.core.client.util.DateWrapper;

public class Daemon extends RemoteServiceServlet {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;
	EventServiceImpl eventServ = new EventServiceImpl();

	public Daemon() {
//		Timer minuteTimer = new Timer();
//		minuteTimer.scheduleAtFixedRate(new TimerTask() {
//			@Override
//			public void run() {
//			}
//		}, 60*1000, 60*1000);
		
		Timer dailyTimer = new Timer();
		dailyTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				purgeChatLogs();
				notifiyOfEvaluations();
				createDeviceCheckTasks();
			}
		}, 24*60*60*1000, 24*60*60*1000);
//		}, 1000, 10*1000);
	}
	
	private void purgeChatLogs() {
		conn = DBConnect.getConnection();
		DateWrapper now = new DateWrapper();
		DateWrapper then = now.addYears(-1);
//		System.out.println("timestamp="+then.getTime()+", datetime="+then);
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM `chatlogs` where `timestamp` < "+then.getTime());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("purgeChatLogs exception: "+sqle.getMessage());
		}
	}
	
	private void notifiyOfEvaluations() {
		conn = DBConnect.getConnection();
		SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy");
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT e.id, e.employee_id, e.next_eval_date, d.name "
					+ "FROM evaluations e INNER JOIN dictionaries d ON e.field_id = d.id "
					+ "WHERE DATEDIFF(e.next_eval_date, curdate()) < 45 AND "
					+ "((e.notification_sent is null) OR DATEDIFF(curdate(), e.notification_sent) > 45)");
			ResultSet result = ps.executeQuery();
			StringBuilder recsToUpdate = new StringBuilder();
			boolean first = true;
			int notifsSent = 0;
			while (result.next()) {
				notifsSent++;
				if (first) {
				  first = false;
				} else {
				  recsToUpdate.append(",");
				}
				recsToUpdate.append(result.getInt("id"));
				eventServ.addEvent(new Event(new Date(), result.getInt("employee_id"),
						"Переаттестация",
						"Дата Вашей повторной аттестации в области \""+result.getString("name")+"\" - "+dt.format(result.getDate("next_eval_date"))));
			}
			ps.close();
			if (notifsSent == 0)
				return;
			ps = conn.prepareStatement("UPDATE evaluations SET notification_sent="+DBConnect.saveDate(new Date())+" WHERE id in ("+recsToUpdate+")");
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("notifiyOfEvaluations exception: "+sqle.getMessage());
		}
	}
	
	private void createDeviceCheckTasks() {
		conn = DBConnect.getConnection();
		TaskServiceImpl taskService = new TaskServiceImpl();
		DictionaryServiceImpl dictService = new DictionaryServiceImpl();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT d.id, d.title, d.next_check, d.responsible_id, d.task_created "
					+ "FROM devices d WHERE DATEDIFF(d.next_check, curdate()) <= 15 AND ((d.task_created is null) OR DATEDIFF(curdate(), d.task_created) >= 15)");
			ResultSet result = ps.executeQuery();
			StringBuilder recsToUpdate = new StringBuilder();
			boolean first = true;
			int notifsSent = 0;
			while (result.next()) {
				notifsSent++;
				if (first) {
				  first = false;
				} else {
				  recsToUpdate.append(",");
				}
				recsToUpdate.append(result.getInt("id"));
				Task t = new Task();
				t.setCreatedBy(Constants.ADMIN_ID);
				t.setDescription("Выполнить поверку оборудования: "+result.getString("title"));
				t.setExecutorId(result.getInt("responsible_id"));
				t.setStartDate(new Date());
				t.setDueDate(result.getDate("next_check"));
				t.setTypeId(dictService.getOrCreateRecByName(Constants.DICT_TASKTYPES, "Выполнение поверки").getId());
				t.setStatus(0);
				taskService.addTask(t);
				
				eventServ.addEvent(new Event(new Date(), result.getInt("responsible_id"),
						"Новое задание",
						"Вы назначены исполнителем по заданию \"Выполнить поверку оборудования: "+result.getString("title")+"\"."));
			}
			ps.close();
			if (notifsSent == 0)
				return;
			ps = conn.prepareStatement("UPDATE devices SET task_created="+DBConnect.saveDate(new Date())+" WHERE id in ("+recsToUpdate+")");
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("createDeviceCheckTasks exception: "+sqle.getMessage());
		}
	}	
}
