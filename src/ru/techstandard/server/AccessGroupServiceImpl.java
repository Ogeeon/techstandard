package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.shared.AccessGroupService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AccessGroupServiceImpl extends RemoteServiceServlet implements AccessGroupService {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;

	@Override
	public List<AccessGroup> getAccessGroups() {
		return getAccessGroups("");
	}

	public List<AccessGroup> getAccessGroups(String whereClause) {
		conn = DBConnect.getConnection();
		List<AccessGroup> records = new ArrayList<AccessGroup>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM accessgroups g INNER JOIN accessrights r ON (g.id = r.group_id) "+whereClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				AccessGroup g = new AccessGroup(result.getInt("id"), result.getString("grp_name"));
				g.setDescription(result.getString("description"));
				g.setTaskCreator(result.getBoolean("task_creator"));
				g.setTaskApprover(result.getBoolean("task_approver"));
				g.setNeedApproval(result.getBoolean("need_approval"));
				g.setDeleteConfirmer(result.getBoolean("delete_confirmer"));
				Map<String, Integer> map = new HashMap<String, Integer>();
				for (int idx = 0; idx < Constants.SECTION_KEYS.length; idx++) {
					map.put(Constants.SECTION_KEYS[idx], result.getInt(Constants.SECTION_KEYS[idx]));
				}
				g.setAccess(map);
				records.add(g);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getAccessGroups exception: "+sqle.getMessage());
			MyLogger.warning("[AccessGroupServiceImpl] getAccessGroups exception: "+sqle.getMessage());
			MyLogger.warning("[AccessGroupServiceImpl] getAccessGroups exception: "+sqle.toString());
		}
	    return records;
	}
	
	@Override
	public int addAccessGroup(AccessGroup group) {
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO accessgroups (grp_name, description, task_creator, task_approver, task_confirmer, need_approval, delete_confirmer) VALUES (?, ?, "+
//				DBConnect.saveString(group.getName())+", " +
//				DBConnect.saveString(group.getDescription()) + ", " +
				group.isTaskCreator() + ", " +
				group.isTaskApprover() + ", " +
				group.isNeedApproval() + ", " +
				group.isDeleteConfirmer() + 
				")";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
//			System.out.println("addGuide qry="+qry);
			ps.setString(1, group.getName());
			ps.setString(2, group.getDescription());
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			addAccessRights(key, group.getAccess());
			return key;
		} catch (SQLException sqle) {
			System.out.println("addAccessGroup exception:"+sqle.getMessage());
			MyLogger.warning("[AccessGrpoupServiceImpl] addAccessGroup exception: "+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean updateAccessGroup(AccessGroup group, boolean skipAccessRights) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE accessgroups SET " +
				"grp_name = ?, " + 
				"description = ?, " + 
				"task_creator =" + group.isTaskCreator() + ", " +
				"task_approver =" + group.isTaskApprover() + ", " +
				"need_approval =" + group.isNeedApproval() + ", " +
				"delete_confirmer =" + group.isDeleteConfirmer() +
				" WHERE id=" + group.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, group.getName());
			ps.setString(2, group.getDescription());
			int rows = ps.executeUpdate();
			ps.close();
			if (!skipAccessRights)
				updateAccessRights(group.getId(), group.getAccess());
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateAccessGroup exception: "+sqle.getMessage());
			MyLogger.warning("[AccessGrpoupServiceImpl] updateAccessGroup exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public boolean deleteAccessGroup(int id) {
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM accessgroups WHERE id="+id);
			ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
//			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
//				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах для данного договора.");
			System.out.println("deleteAccessGroup exception: "+sqle.getMessage());
			MyLogger.warning("[AccessGrpoupServiceImpl] deleteAccessGroup exception: "+sqle.getMessage());
		}
		deleteAccessRights(id);
		return true;
	}

	private void addAccessRights(int groupId, Map<String, Integer> accessMap) {
		StringBuilder fields = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (int idx = 0; idx < Constants.SECTION_KEYS.length; idx++) {
			fields.append(", ");
			values.append(", ");
			fields.append(Constants.SECTION_KEYS[idx]);
			values.append(accessMap.get(Constants.SECTION_KEYS[idx]));
		}
		String qry = "INSERT INTO accessrights (group_id "+ fields.toString() + ") VALUES (" + groupId + values.toString() +")";
//		System.out.println("addAR qry="+qry);
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("addAccessRights exception: "+sqle.getMessage());
			MyLogger.warning("[AccessGrpoupServiceImpl] addAccessRightsAccessRights exception: "+sqle.getMessage());
		}
	}
	
	private void deleteAccessRights(int groupId) {
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM accessrights WHERE group_id="+groupId);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deleteAccessRights exception: "+sqle.getMessage());
			MyLogger.warning("[AccessGrpoupServiceImpl] deleteAccessRights exception: "+sqle.getMessage());
		}
	}
	
	private void updateAccessRights(int groupId, Map<String, Integer> accessMap) {
		StringBuilder data = new StringBuilder();
		boolean first = true;
		for (int idx = 0; idx < Constants.SECTION_KEYS.length; idx++) {
			if (first) {
				first = false;
			} else {
				data.append(", ");				
			}
			data.append(Constants.SECTION_KEYS[idx]); data.append("=");
			data.append(accessMap.get(Constants.SECTION_KEYS[idx]));
		}
		String qry = "UPDATE accessrights SET "+ data.toString() + " WHERE group_id=" + groupId;
//		System.out.println("updAR qry="+qry);
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("updateAccessRights exception: "+sqle.getMessage());
			MyLogger.warning("[AccessGrpoupServiceImpl] updateAccessRights exception: "+sqle.getMessage());
		}
	}
}
