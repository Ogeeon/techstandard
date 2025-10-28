package ru.techstandard.server;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

public class FileUploadServlet extends UploadAction {

	private static final long serialVersionUID = 1L;
	private Connection conn = null;

	public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
		conn = DBConnect.getConnection();
		
		int attachId = 0;
		int parentId = 0;
		int parentType = 0;
		int attachTypeId = 0;
		String attachTitle = "";
		String origName = "";
		File file = null;
		for (FileItem item : sessionFiles) {
			if (!item.isFormField()) {
				try {
					origName = item.getName();
					String ext = item.getName().substring(item.getName().lastIndexOf("."));
//					file = File.createTempFile("attachment-", ext, new File(System.getProperty("user.home") + File.separator + "TechStandard_DB_Attachments"));
					file = File.createTempFile("attachment-", ext, new File("/usr/local/" + "TechStandard_DB_Attachments"));
//					System.out.println("File [" + file.getPath() + "] is created!");
					item.write(file);
				} catch (Exception e) {
					throw new UploadActionException(e.getMessage());
				}
			} else {
				if (item.getFieldName().equals("attachId"))
					attachId = Integer.valueOf(item.getString());
				if (item.getFieldName().equals("parentId"))
					parentId = Integer.valueOf(item.getString());
				if (item.getFieldName().equals("parentType"))
					parentType = Integer.valueOf(item.getString());
				if (item.getFieldName().equals("attachTypeId"))
					attachTypeId = Integer.valueOf(item.getString());
				if (item.getFieldName().equals("attachTitle"))
					try {
						attachTitle = item.getString("UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
			}
		}

		try {
			// Remove files from session
			removeSessionFileItems(request);
		} catch (Exception e) {
			throw new UploadActionException(e.getMessage());
		}

		if (attachId == 0) {
			String qry = "INSERT INTO `attachments` (`parent_id`, `parent_type`, `title`, `attach_type`, `filename`, `saved_as`) VALUES ("+ 
					String.valueOf(parentId) + ", " +
					String.valueOf(parentType) + ", " +
					"'" + attachTitle + "', " +
					String.valueOf(attachTypeId) + ", " +
					"'" + origName + "', " +
					"'" + file.getPath().replace("\\", "\\\\") + "')";
//			System.out.println("attach insert qry = "+qry);
			try {
				PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
				ps.executeUpdate();
				ResultSet rs = ps.getGeneratedKeys();
				rs.first();
				attachId = rs.getInt(1);
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("attachment insert exception: "+sqle.getMessage());
				throw new UploadActionException(sqle.getMessage());
			}
		} else {
			String qry = "UPDATE `attachments` SET " +
					"`parent_id`="+String.valueOf(parentId) + ", " +
					"`parent_type`="+String.valueOf(parentType) + ", " +
					"`title`='" + attachTitle + "', " +
					"`attach_type`=" + String.valueOf(attachTypeId) +", "+
					"`filename`='" + origName + "', " +
					"`saved_as`='" + file.getPath().replace("\\", "\\\\") + "' WHERE id="+attachId;
//			System.out.println("attach update qry = "+qry);
			try {
				PreparedStatement ps = conn.prepareStatement(qry);
				ps.executeUpdate();
				ps.close();
			} catch (SQLException sqle) {
				System.out.println("attachment update exception: "+sqle.getMessage());
				throw new UploadActionException(sqle.getMessage());
			}
		}
		
		return String.valueOf(attachId);
	}

}
