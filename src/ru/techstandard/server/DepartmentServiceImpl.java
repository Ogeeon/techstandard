package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.Department;
import ru.techstandard.shared.DepartmentService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DepartmentServiceImpl extends RemoteServiceServlet implements DepartmentService {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;

	@Override
	public List<Department> getDepartments(int parentId) {
	    return getDepartments(" WHERE parent_id="+parentId);
	}

	@Override
	public List<Department> getAllDepartments() {
		return getDepartments("");
	}

	private List<Department> getDepartments(String whereClause) {
		conn = DBConnect.getConnection();
		List<Department> records = new ArrayList<Department>();
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM departments "+whereClause);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				Department d = new Department(result.getInt("id"));
				d.setName(result.getString("name"));
				d.setParentId(result.getInt("parent_id"));
				records.add(d);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getDepartments exception: "+sqle.getMessage());
			MyLogger.warning("[DepartmentServiceImpl] getDepartments exception: "+sqle.getMessage());
			MyLogger.warning("[DepartmentServiceImpl] getDepartments exception: "+sqle.toString());
		}
	    return records;
	}
	
	@Override
	public boolean deleteDepartment(int id) {
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM employees e where e.department_id="+id);
			ResultSet result = ps.executeQuery();
			int count=0;
			while (result.next()) {
				count++;
			}
			result.close();
			ps.close();
			// В подразделении есть сотрудники, отменяем удаление
			if (count > 0)
				return false;
			
			ps = conn.prepareStatement("DELETE FROM departments WHERE id="+id);
			ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
//			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
//				throw new IllegalArgumentException("Невозможно удалить подразделение, в котором есть сотрудники.");
			System.out.println("deleteDepartment exception: "+sqle.getMessage());
		}
	
		return true;
	}

	@Override
	public int addDepartment(Department department) {
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO departments (parent_id, name) VALUES ("+ department.getParentId() + ", ?)";
//				DBConnect.saveString(department.getName()) +
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, department.getName());
//			System.out.println("addGuide qry="+qry);
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addDepartment exception:"+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean updateDepartment(Department department) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE departments SET parent_id=" + department.getParentId() + ", " +
				"name = ?" + //DBConnect.saveString(department.getName()) + 
				" WHERE id=" + department.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, department.getName());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateDepartment exception: "+sqle.getMessage());
		}
		return false;
	}

}
