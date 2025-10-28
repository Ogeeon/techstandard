package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.Event;
import ru.techstandard.shared.ClientService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class ClientServiceImpl extends RemoteServiceServlet implements ClientService {
	private static final long serialVersionUID = 8282425178066280262L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;

	@Override
	public Client getClientInfoByJournId(int journRecID) {
		conn = DBConnect.getConnection();
		Client client = new Client();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT clients.* FROM acts INNER JOIN (clients, contracts) on "
					+ "(contracts.client_id = clients.id AND acts.contract_id = contracts.id) WHERE acts.id = "+String.valueOf(journRecID));
			ResultSet result = ps.executeQuery();
			
			while (result.next()) {
				client.setId(result.getInt("id"));
				client.setName(result.getString("name"));
				client.setFullName(result.getString("full_name"));
				client.setBoss(result.getString("boss"));
				client.setAddress(result.getString("address"));
				client.setAddress2(result.getString("address2"));
				client.setPhone(result.getString("phone"));
				client.setFax(result.getString("fax"));
				client.setInn(result.getString("inn"));
				client.setKpp(result.getString("kpp"));
				client.setEmail(result.getString("email"));
				client.setActual(result.getBoolean("actual"));
				client.setBankName(result.getString("bank_name"));
				client.setRsch(result.getString("rsch"));
				client.setKsch(result.getString("ksch"));
				client.setOkpo(result.getString("okpo"));
				client.setOkato(result.getString("okato"));
				client.setOgrn(result.getString("ogrn"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getClientInfoByJournId sqle: "+sqle.getMessage());
		}
		return client;
	}

	@Override
	public Client getClientInfoById(int id) {
		conn = DBConnect.getConnection();
		Client client = new Client();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM clients WHERE id = "+id);
			ResultSet result = ps.executeQuery();
			
			while (result.next()) {
				client.setId(result.getInt("id"));
				client.setName(result.getString("name"));
				client.setFullName(result.getString("full_name"));
				client.setBoss(result.getString("boss"));
				client.setAddress(result.getString("address"));
				client.setAddress2(result.getString("address2"));
				client.setPhone(result.getString("phone"));
				client.setFax(result.getString("fax"));
				client.setInn(result.getString("inn"));
				client.setKpp(result.getString("kpp"));
				client.setEmail(result.getString("email"));
				client.setActual(result.getBoolean("actual"));
				client.setBankName(result.getString("bank_name"));
				client.setRsch(result.getString("rsch"));
				client.setKsch(result.getString("ksch"));
				client.setOkpo(result.getString("okpo"));
				client.setOkato(result.getString("okato"));
				client.setOgrn(result.getString("ogrn"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getClientInfo sqle: "+sqle.getMessage());
		}
		return client;
	}
	
	@Override
	public List<Client> getAllClients() {
		return getClients(" WHERE deleted=false ");
	}
	
	@Override
	public List<Client> getClientsByActualness(boolean actual) {
	    return getClients(" WHERE deleted=false AND actual=" + (actual?"1":"0"));
	}
	
	private List<Client> getClients(String whereClause) {
		conn = DBConnect.getConnection();
		List<Client> clients = new ArrayList<Client>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM clients "+whereClause+" ORDER BY name");
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Client client = new Client();
				client.setId(result.getInt("id"));
				client.setName(result.getString("name"));
				client.setFullName(result.getString("full_name"));
				client.setBoss(result.getString("boss"));
				client.setAddress(result.getString("address"));
				client.setAddress2(result.getString("address2"));
				client.setPhone(result.getString("phone"));
				client.setFax(result.getString("fax"));
				client.setInn(result.getString("inn"));
				client.setKpp(result.getString("kpp"));
				client.setEmail(result.getString("email"));
				client.setActual(result.getBoolean("actual"));
				client.setBankName(result.getString("bank_name"));
				client.setRsch(result.getString("rsch"));
				client.setKsch(result.getString("ksch"));
				client.setOkpo(result.getString("okpo"));
				client.setOkato(result.getString("okato"));
				client.setOgrn(result.getString("ogrn"));
				client.setDeletedBy(result.getInt("deleted_by"));
				clients.add(client);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getClients exception: "+sqle.getMessage());
		}
	    return clients;
	}

	@Override
	public boolean updateClient(Client client) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE clients SET name=?, "+//DBConnect.saveString(client.getName())+", "+
					"full_name=?, "+//DBConnect.saveString(client.getFullName())+", "+
					"boss=?, "+//DBConnect.saveString(client.getBoss())+", "+
					"address=?, "+//DBConnect.saveString(client.getAddress())+", "+
					"address2=?, "+//DBConnect.saveString(client.getAddress2())+", "+
					"phone=?, "+//DBConnect.saveString(client.getPhone())+", "+
					"fax=?, "+//DBConnect.saveString(client.getFax())+", "+
					"inn=?, "+//DBConnect.saveString(client.getInn())+", "+
					"kpp=?, "+//DBConnect.saveString(client.getKpp())+", "+
					"email=?, "+//DBConnect.saveString(client.getEmail())+", "+
					"actual="+(client.isActual()?"1":"0")+", "+
					"bank_name=?, "+//DBConnect.saveString(client.getBankName())+", "+
					"rsch=?, "+//DBConnect.saveString(client.getRsch())+", "+
					"ksch=?, "+//DBConnect.saveString(client.getKsch())+", "+
					"okpo=?, "+//DBConnect.saveString(client.getOkpo())+", "+
					"okato=?, "+//DBConnect.saveString(client.getOkato())+", "+
					"ogrn=?"+//DBConnect.saveString(client.getOgrn())+
					" WHERE id="+client.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, client.getName());
			ps.setString(2, client.getFullName());
			ps.setString(3, client.getBoss());
			ps.setString(4, client.getAddress());
			ps.setString(5, client.getAddress2());
			ps.setString(6, client.getPhone());
			ps.setString(7, client.getFax());
			ps.setString(8, client.getInn());
			ps.setString(9, client.getKpp());
			ps.setString(10, client.getEmail());
			ps.setString(11, client.getBankName());
			ps.setString(12, client.getRsch());
			ps.setString(13, client.getKsch());
			ps.setString(14, client.getOkpo());
			ps.setString(15, client.getOkato());
			ps.setString(16, client.getOgrn());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateClient exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public boolean setActual(int id, boolean actual) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE clients SET actual="+(actual?"1":"0")+" WHERE id="+id;
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("setActual exception: "+sqle.getMessage());
		}
		return false;
	}
	
	@Override
	public int addClient(Client client) {
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO clients (name, full_name, boss, address, address2, phone, fax, inn, kpp, "
				+ "email, actual, bank_name, rsch, ksch, okpo, okato, ogrn) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
				+ (client.isActual()?"1":"0")+", ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, client.getName());
			ps.setString(2, client.getFullName());
			ps.setString(3, client.getBoss());
			ps.setString(4, client.getAddress());
			ps.setString(5, client.getAddress2());
			ps.setString(6, client.getPhone());
			ps.setString(7, client.getFax());
			ps.setString(8, client.getInn());
			ps.setString(9, client.getKpp());
			ps.setString(10, client.getEmail());
			ps.setString(11, client.getBankName());
			ps.setString(12, client.getRsch());
			ps.setString(13, client.getKsch());
			ps.setString(14, client.getOkpo());
			ps.setString(15, client.getOkato());
			ps.setString(16, client.getOgrn());
//			System.out.println("addclient qry="+qry);
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addClient exception:"+sqle.getMessage());
		}
		return 0;
	}

	private int getCount(boolean actual) {
		int count = 0; 
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM clients  WHERE deleted=false AND actual=" + (actual?"1":"0"));
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
	public PagingLoadResult<Client> getClientsPaged(PagingLoadConfig config, boolean actual) {
		conn = DBConnect.getConnection();
		
		String sortClause = "";
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY name ";
		}
		
		int start = config.getOffset();
		int limit = config.getLimit();
		String limitClause = " LIMIT " + start + ", " + limit;
		
		List<Client> clients = new ArrayList<Client>();
		String whereClause = " WHERE deleted=false AND actual=" + (actual?"1":"0") + " ";

	    try {
			PreparedStatement ps = conn.prepareStatement("select * from clients " + whereClause + sortClause + limitClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Client client = new Client();
				client.setId(result.getInt("id"));
				client.setName(result.getString("name"));
				client.setFullName(result.getString("full_name"));
				client.setBoss(result.getString("boss"));
				client.setAddress(result.getString("address"));
				client.setAddress2(result.getString("address2"));
				client.setPhone(result.getString("phone"));
				client.setFax(result.getString("fax"));
				client.setInn(result.getString("inn"));
				client.setKpp(result.getString("kpp"));
				client.setEmail(result.getString("email"));
				client.setActual(result.getBoolean("actual"));
				client.setBankName(result.getString("bank_name"));
				client.setRsch(result.getString("rsch"));
				client.setKsch(result.getString("ksch"));
				client.setOkpo(result.getString("okpo"));
				client.setOkato(result.getString("okato"));
				client.setOgrn(result.getString("ogrn"));
				clients.add(client);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getClientsPaged exception: "+sqle.getMessage());
		}
		
		return new PagingLoadResultBean<Client>(clients, getCount(actual), config.getOffset());
	}

	@Override
	public boolean deleteRecord(int id, boolean markOnly) throws IllegalArgumentException {
		conn = DBConnect.getConnection();
		try {
			String qry;
	    	if (markOnly) { 
	    		int deleter = Utils.getUserIdFromSession(this.getThreadLocalRequest().getSession());
	    		qry = "UPDATE clients SET deleted=true, deleted_by="+deleter+" WHERE id=";
	    		List<Integer> confirmers = emplServ.getDeleteConfirmers();
				for (int idx = 0; idx < confirmers.size(); idx++) {
					eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
							"Объект помечен на удаление",
							"Новый объект в категории \"клиенты\" помечен на удаление."));
				}
	    	}
	    	else
	    		qry = "DELETE FROM clients WHERE id=";
			PreparedStatement ps = conn.prepareStatement(qry+id);
			ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах для данного клиента.");
			System.out.println("deleteRecord exception: "+sqle.getMessage());
		}
		
		ContractServiceImpl contractServ = new ContractServiceImpl();
		List<Integer> contractList = new ArrayList<Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM contracts WHERE client_id=" + id);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				contractList.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("acquring contracts list exception: "+sqle.getMessage());
		}
//		System.out.println("attachmentList = "+attachmentList.toString());
		for (int idx=0; idx<contractList.size(); idx++) 
			contractServ.deleteContract(contractList.get(idx), markOnly);
		
		return true;
	}

	@Override
	public String getPrintableClientList(boolean actual) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Контрагенты</title></head><body>");
		builder.append("<h1>Перечень "+(actual?"":"возможных")+" контрагентов</h1>");
		
		List<Client> clients = getClientsByActualness(actual);
		builder.append("<table border=\"1\" >");
		builder.append("<tr>");
		builder.append("<td>"); builder.append("Наименование<br>контрагента"); builder.append("</td>");
		builder.append("<td>"); builder.append("Фио<br>руководителя"); builder.append("</td>");
		builder.append("<td>"); builder.append("Адрес"); builder.append("</td>");
		builder.append("<td>"); builder.append("Телефон"); builder.append("</td>");
		builder.append("<td>"); builder.append("ИНН"); builder.append("</td>");
		builder.append("<td>"); builder.append("E-mail"); builder.append("</td>");
		builder.append("</tr>");
		for (Client c: clients) {
			builder.append("<tr>");
			builder.append("<td>"); builder.append(v(c.getName())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getBoss())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getAddress())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getPhone())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getInn())); builder.append("</td>");
			builder.append("<td>"); builder.append(v(c.getEmail())); builder.append("</td>");
			builder.append("</tr>");
		}
		builder.append("</table></body>");
		
		return builder.toString();
	}

	private String v(String value) {
		return value == null?"":value;
	}

	@Override
	public String getPrintableClientCard(int id) {
		conn = DBConnect.getConnection();
		Client c = new Client();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM clients WHERE id="+id);
			ResultSet result = ps.executeQuery();
			result.first();
			c.setId(result.getInt("id"));
			c.setFullName(result.getString("full_name"));
			c.setBoss(result.getString("boss"));
			c.setAddress(result.getString("address"));
			c.setAddress2(result.getString("address2"));
			c.setPhone(result.getString("phone"));
			c.setFax(result.getString("fax"));
			c.setInn(result.getString("inn"));
			c.setKpp(result.getString("kpp"));
			c.setEmail(result.getString("email"));
			c.setActual(result.getBoolean("actual"));
			c.setBankName(result.getString("bank_name"));
			c.setRsch(result.getString("rsch"));
			c.setKsch(result.getString("ksch"));
			c.setOkpo(result.getString("okpo"));
			c.setOkato(result.getString("okato"));
			c.setOgrn(result.getString("ogrn"));
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getPrintableContractCard exception: "+sqle.getMessage());
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Карточка контрагента</title></head><body>");
		builder.append("<h1>Карточка "+(c.isActual()?"":"возможного ")+"контрагента</h1><br><br>");
		
		builder.append("<table border=\"0\" >");
		builder.append("<tr><td>"); builder.append("Наименование"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("ФИО руководителя"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getBoss())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Почтовый адрес"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getAddress())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Юридический адрес"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getAddress2())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Телефон"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getPhone())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Факс"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getFax())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("ИНН"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getInn())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("КПП"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getKpp())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("e-mail"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getEmail())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Банк плательщика"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getBankName())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("Р/счёт"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getRsch())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("К/счёт"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getKsch())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("ОКПО"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getOkpo())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("ОКАТО"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getOkato())); builder.append("</td></tr>");
		builder.append("<tr><td>"); builder.append("ОГРН"); builder.append("</td>");
		builder.append("<td>"); builder.append(v(c.getOgrn())); builder.append("</td></tr>");
		
		builder.append("</table></body>");
		
		return builder.toString();
	}

	public List<DeletedObject> getDeletedObjects() {
		List<DeletedObject> deleted = new ArrayList<DeletedObject>();
		List<Client> deletedClients = getClients(" WHERE deleted=true ");
		
		for (int idx = 0; idx < deletedClients.size(); idx++) {
			String descr = (deletedClients.get(idx).isActual()?"Контрагент ":"Возможный контрагент ")+deletedClients.get(idx).getName();
			int deleter = deletedClients.get(idx).getDeletedBy();
			String userName = emplServ.getEmployeeInfo(deleter).getName();
			deleted.add(new DeletedObject("clients_"+idx, "clients", "Контрагенты, возможные контрагенты", deletedClients.get(idx).getId(), descr, deleter, userName));
		}
		return deleted;
	}
}
