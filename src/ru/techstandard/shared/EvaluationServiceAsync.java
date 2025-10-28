package ru.techstandard.shared;

import ru.techstandard.client.model.Evaluation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface EvaluationServiceAsync {

	void addEvaluation(Evaluation evaluation, AsyncCallback<Integer> callback);

	void deleteEvaluation(int id, boolean markOnly,
			AsyncCallback<Boolean> callback);

	void getEvaluations(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<Evaluation>> callback);

	void getPrintableEvaluationCard(int id, AsyncCallback<String> callback);

	void getPrintableEvaluationList(FilterPagingLoadConfig config, AsyncCallback<String> callback);

	void updateEvaluation(Evaluation evaluation, AsyncCallback<Boolean> callback);

}
