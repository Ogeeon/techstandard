package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("clientService")
public interface ClientService extends RemoteService {
	Client getClientInfoByJournId(int journRecID);
	List<Client> getAllClients();
	List<Client> getClientsByActualness(boolean actual);
	PagingLoadResult<Client> getClientsPaged(PagingLoadConfig config, boolean actual);
	boolean updateClient(Client client);
	boolean setActual(int id, boolean actual);
	int addClient(Client client);
	boolean deleteRecord(int id, boolean markOnly) throws IllegalArgumentException;
	String getPrintableClientList(boolean actual);
	String getPrintableClientCard(int id);
	Client getClientInfoById(int id);
}
