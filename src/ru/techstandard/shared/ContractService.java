package ru.techstandard.shared;

import java.util.List;

import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.TemplateContract;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("contractService")
public interface ContractService extends RemoteService {
	PagingLoadResult<Contract> getContracts(FilterPagingLoadConfig config);
	boolean deleteContract(int recordID, boolean markOnly);
	int addContract(Contract contract);
	boolean updateContract(Contract contract);
	String getPrintableContractList(FilterPagingLoadConfig config);
	String getPrintableContractCard(int id);
	int getCount(int id);
	List<Contract> getContractsForClient(int clientId);
	Contract getContractById(int id);
	int storeTemplate(TemplateContract template);
}
