package ru.techstandard.server;

import gwtupload.server.exceptions.UploadActionException;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Attachement;
import ru.techstandard.client.model.Event;
import ru.techstandard.shared.AttachementService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;


public class AttachementServiceImpl extends RemoteServiceServlet implements AttachementService {
	private static final long serialVersionUID = 1L;
	EmployeeServiceImpl emplServ = new EmployeeServiceImpl();
	EventServiceImpl eventServ = new EventServiceImpl();
	private Connection conn = null;

	public List<Attachement> getAllAttachements(int clientId) {
		return null;
	}

	public PagingLoadResult<Attachement> getAttachements(FilterPagingLoadConfig config) {
		String sortClause = " ORDER BY a.attach_type";
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY a.attach_type";
		}

		int start = config.getOffset();
		int limit = config.getLimit();
		String limitClause = " LIMIT " + start + ", " + limit;
		
		String parent_id = "0";
		String parent_type = "0";
		List<FilterConfig> filters = config.getFilters();
		for (FilterConfig f : filters) {
			String value = f.getValue();
			String field = f.getField();
			if (value == null)
		          continue; 			 
			if (field.equals("parentIdStr")) {
				parent_id = value;
			}
			if (field.equals("parentTypeStr")) {
				parent_type = value;
			}
		}

		List<Attachement> attachements = new ArrayList<Attachement>();

	    try {
	    	conn = DBConnect.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT a.id, a.attach_type, a.title, a.filename, d.name attachTypeName, a.parent_id, a.parent_type "+
					"FROM attachments a INNER JOIN dictionaries d ON (a.attach_type = d.id) WHERE a.parent_type="+parent_type+" AND a.parent_id="+parent_id + sortClause + limitClause);
			ResultSet result = ps.executeQuery();
			
			while (result.next()) {
				Attachement att = new Attachement(result.getInt("id"));
				att.setAttachTypeName(result.getString("attachTypeName"));
				att.setAttachType(result.getInt("attach_type"));
				att.setTitle(result.getString("title"));
				att.setFilename(result.getString("filename"));
				att.setParentId(result.getShort("parent_id"));
				att.setParentType(result.getInt("parent_type"));
				attachements.add(att);
			}
//			System.out.println("attach serv impl: getAtts, atts="+attachements);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getAttachements exception: "+sqle.getMessage());
			MyLogger.warning("[AttachementServiceImpl] getAttachements exception: "+sqle.toString());
		}
		
		return new PagingLoadResultBean<Attachement>(attachements, getCount(parent_type, parent_id), config.getOffset());
	}

	private int getCount(String parent_type, String parent_id) {
		int count = 0;
		conn = DBConnect.getConnection();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM attachments WHERE parent_type="+parent_type+" AND parent_id="+parent_id);
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount exception: "+sqle.getMessage());
			MyLogger.warning("[AttachementServiceImpl] getCount exception: "+sqle.getMessage());
			MyLogger.warning("[AttachementServiceImpl] getCount exception: "+sqle.toString());
		}
	    return count;
	}

	public void deleteAttachments(List<Integer> attachmentList) throws IllegalArgumentException {
		deleteAttachments(attachmentList, false);
	}
	
	public void deleteAttachments(List<Integer> attachmentList, boolean markOnly) throws IllegalArgumentException {
		conn = DBConnect.getConnection();
		PreparedStatement ps;
		ResultSet result;
		String filename = "";
		for (int a = 0; a < attachmentList.size(); a++) {
			try {
				ps = conn.prepareStatement("SELECT saved_as FROM attachments WHERE id=" + attachmentList.get(a));
				result = ps.executeQuery();
				result.first();
				filename = result.getString(1);
				result.close();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("deleteAttachments getfilename exception: "+sqle.getMessage());
				MyLogger.warning("[AttachementServiceImpl] deleteAttachments getfilename exception: "+sqle.toString());
			}
			try {
				String qry;
		    	if (markOnly) { 
		    		qry = "UPDATE attachments SET deleted=true WHERE id=";
		    		List<Integer> confirmers = emplServ.getDeleteConfirmers();
					for (int idx = 0; idx < confirmers.size(); idx++) {
						eventServ.addEvent(new Event(new Date(), confirmers.get(idx),
								"Объект помечен на удаление",
								"Новый объект в категории \"приложения\" помечен на удаление."));
					}
		    	}
		    	else
		    		qry = "DELETE FROM attachments WHERE id=";
				ps = conn.prepareStatement(qry+attachmentList.get(a));
				ps.executeUpdate();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("deleteAttachments deleteRecord exception ["+sqle.getErrorCode()+"]: "+sqle.getMessage());
				MyLogger.warning("[AttachementServiceImpl] deleteAttachments deleteRecord exception: "+sqle.toString());
			}
			if (markOnly)
				return;
			try {
				File file = new File(filename);
			    if (file != null) {
			      file.delete();
//			      System.out.println("file "+filename+ " deleted");
			    }
			} catch (Exception e) {
				System.out.println("deleteAttachments delete file exception: "+e.getMessage());
				MyLogger.warning("[AttachementServiceImpl] deleteAttachments delete file exception: "+e.getMessage());
			}
		}
	}

	public boolean updateAttachment(Attachement attach) {
		conn = DBConnect.getConnection();
		try {
//			Не обновляем поля `filename` и `saved_as`, т.к. они меняются только при загрузке нового файла.
			String qry = "UPDATE `attachments` SET " +
					"`parent_id`="+ attach.getParentIdStr() + ", " +
					"`parent_type`="+ attach.getParentTypeStr() + ", " +
					"`title`='" + attach.getTitle() + "', " +
					"`attach_type`=" + attach.getAttachType() +
					" WHERE id=" + attach.getId();
//			System.out.println("attach update qry = "+qry);
			try {
				PreparedStatement ps = conn.prepareStatement(qry);
				ps.executeUpdate();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("attachment update exception: "+sqle.getMessage());
				MyLogger.warning("[AttachementServiceImpl] updateAttachment exception: "+sqle.toString());
				throw new UploadActionException(sqle.getMessage());
			}

			PreparedStatement ps = conn.prepareStatement(qry);
			int rows = ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateAttachment exception: "+sqle.getMessage());
			MyLogger.warning("[AttachementServiceImpl] updateAttachment exception: "+sqle.toString());
		}
		return false;
	}

	
	@Override
	public void deleteFile(int attachementId) {
		conn = DBConnect.getConnection();
		PreparedStatement ps;
		ResultSet result;
		String filename = "";
		try {
			ps = conn.prepareStatement("SELECT saved_as FROM attachments WHERE id=" + attachementId);
			result = ps.executeQuery();
			result.first();
			filename = result.getString(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteFile getfilename exception: "+sqle.getMessage());
		}
		try {
			File file = new File(filename);
			if (file != null) {
				file.delete();
				//			      System.out.println("file "+filename+ " deleted");
			}
		} catch (Exception e) {
			System.out.println("deleteFile delete file exception: "+e.getMessage());
		}
		try {
			ps = conn.prepareStatement("UPDATE attachments SET saved_as='', filename='' WHERE id=" + attachementId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteFile update exception: "+sqle.getMessage());
		}
	}

}
