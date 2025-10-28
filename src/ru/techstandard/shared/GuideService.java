package ru.techstandard.shared;

import ru.techstandard.client.model.Guide;
import ru.techstandard.shared.NotAuthorizedException;
import ru.techstandard.shared.NotLoggedInException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("guideService")
public interface GuideService extends RemoteService {
	PagingLoadResult<Guide> getGuides(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException;
	boolean deleteGuide(int recordID, boolean markOnly) throws NotLoggedInException, NotAuthorizedException;
	int addGuide(Guide guide) throws NotLoggedInException, NotAuthorizedException;
	boolean updateGuide(Guide guide) throws NotLoggedInException, NotAuthorizedException;
	String getPrintableGuideList(FilterPagingLoadConfig config) throws NotLoggedInException, NotAuthorizedException;
	String getPrintableGuideCard(int id) throws NotLoggedInException, NotAuthorizedException;
	Guide getGuide(int id) throws NotLoggedInException, NotAuthorizedException;
}
