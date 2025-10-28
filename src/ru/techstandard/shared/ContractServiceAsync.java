package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.TemplateContract;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface ContractServiceAsync {

	void getContracts(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<Contract>> callback);

	void deleteContract(int recordID, boolean markOnly,
			AsyncCallback<Boolean> callback);

	void updateContract(Contract contract, AsyncCallback<Boolean> callback);

	void getPrintableContractList(FilterPagingLoadConfig config, AsyncCallback<String> callback);

	void addContract(Contract contract, AsyncCallback<Integer> callback);

	void getPrintableContractCard(int id, AsyncCallback<String> callback);

	void getCount(int id, AsyncCallback<Integer> callback);

	void getContractsForClient(int clientId, AsyncCallback<List<Contract>> callback);

	void storeTemplate(TemplateContract template,
			AsyncCallback<Integer> callback);

	void getContractById(int id, AsyncCallback<Contract> callback);

}
