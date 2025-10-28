package ru.techstandard.shared;


import ru.techstandard.client.model.Evaluation;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("evaluationService")
public interface EvaluationService extends RemoteService {
	PagingLoadResult<Evaluation> getEvaluations(FilterPagingLoadConfig config);
	boolean deleteEvaluation(int id, boolean markOnly);
	int addEvaluation(Evaluation evaluation);
	boolean updateEvaluation(Evaluation evaluation);
	String getPrintableEvaluationList(FilterPagingLoadConfig config);
	String getPrintableEvaluationCard(int id);
}
