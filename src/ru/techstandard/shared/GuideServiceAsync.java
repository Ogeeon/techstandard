package ru.techstandard.shared;

import ru.techstandard.client.model.Guide;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface GuideServiceAsync {

	void addGuide(Guide guide, AsyncCallback<Integer> callback);

	void deleteGuide(int recordID, boolean markOnly, AsyncCallback<Boolean> callback);

	void getGuides(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<Guide>> callback);

	void getPrintableGuideCard(int id, AsyncCallback<String> callback);

	void getPrintableGuideList(FilterPagingLoadConfig config, AsyncCallback<String> callback);

	void updateGuide(Guide guide, AsyncCallback<Boolean> callback);

	void getGuide(int id, AsyncCallback<Guide> callback);

}
