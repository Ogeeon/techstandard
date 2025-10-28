package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.Event;
import ru.techstandard.shared.DeletedObjectsService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DeletedObjectsServiceImpl extends RemoteServiceServlet implements DeletedObjectsService {
	private static final long serialVersionUID = 1L;
	EventServiceImpl eventServ = new EventServiceImpl();
	ActsJournalServiceImpl actsService = new ActsJournalServiceImpl();
	ClientServiceImpl clientsService = new ClientServiceImpl();
	ContractServiceImpl contractsService = new ContractServiceImpl();
	DeviceServiceImpl devicesService = new DeviceServiceImpl();
	DictionaryServiceImpl dictionaryService = new DictionaryServiceImpl();
	GuideServiceImpl guideService = new GuideServiceImpl();
	RequestServiceImpl requestService = new RequestServiceImpl();

	private Connection conn = null;
	@Override
	public List<DeletedObject> getDeletedObjects() {
		List<DeletedObject> objects = new ArrayList<DeletedObject>();
		objects.addAll(actsService.getDeletedObjects());
		objects.addAll(clientsService.getDeletedObjects());
		objects.addAll(contractsService.getDeletedObjects());
		objects.addAll(devicesService.getDeletedObjects());
		objects.addAll(dictionaryService.getDeletedObjects());
		objects.addAll(guideService.getDeletedObjects());
		objects.addAll(requestService.getDeletedObjects());
		return objects;
	}

	@Override
	public boolean applyObjectDelete(String table, int id) {
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM "+table+" WHERE id="+id);
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("applyObjectDelete exception: "+sqle.getMessage());
			MyLogger.warning("[DeletedObjectsServiceImpl] applyObjectDelete exception: "+sqle.getMessage());
			MyLogger.warning("[DeletedObjectsServiceImpl] applyObjectDelete exception: "+sqle.toString());
		}
		return false;
	}

	@Override
	public boolean undoObjectDelete(String table, int id, int userId, String denyReason) {
		eventServ.addEvent(new Event(new Date(), userId,
				"Удаление объекта отменено",
				"Выполненное Вами удаление объекта отменено по следующей причине: "+denyReason));
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE "+table+" SET deleted=false WHERE id="+id, Statement.RETURN_GENERATED_KEYS);
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("undoObjectDelete exception: "+sqle.getMessage());
			MyLogger.warning("[DeletedObjectsServiceImpl] undoObjectDelete exception: "+sqle.getMessage());
			MyLogger.warning("[DeletedObjectsServiceImpl] undoObjectDelete exception: "+sqle.toString());
		}
		return false;
	}

}
