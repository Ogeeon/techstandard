package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface ClientServiceAsync {

	void addClient(Client client, AsyncCallback<Integer> callback);

	void deleteRecord(int id, boolean markOnly, AsyncCallback<Boolean> callback);

	void getAllClients(AsyncCallback<List<Client>> callback);

	void getClientsPaged(PagingLoadConfig config, boolean actual, AsyncCallback<PagingLoadResult<Client>> callback);

	void updateClient(Client client, AsyncCallback<Boolean> callback);

	void getPrintableClientList(boolean actual, AsyncCallback<String> callback);

	void getPrintableClientCard(int id, AsyncCallback<String> callback);

	void setActual(int id, boolean actual, AsyncCallback<Boolean> callback);

	void getClientsByActualness(boolean actual,
			AsyncCallback<List<Client>> callback);

	void getClientInfoByJournId(int journRecID, AsyncCallback<Client> callback);

	void getClientInfoById(int id, AsyncCallback<Client> callback);
	
}
