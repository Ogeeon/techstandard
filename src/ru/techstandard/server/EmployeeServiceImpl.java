package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.Employee;
import ru.techstandard.shared.EmployeeService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;

public class EmployeeServiceImpl extends RemoteServiceServlet implements EmployeeService {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;
	@Override
	public List<Employee> getAllEmployees() {
		return loadEmployees(" WHERE e.fired=false AND e.supervisor=false ", " ORDER BY e.name ", "");
	}

	@Override
	public List<Employee> getSubordinates(int deptId) {
		List<Employee> employees = new ArrayList<Employee>();

	    try {
	    	conn = DBConnect.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT e.*, d.name position, dpt.name deptName, g.grp_name"
					+ " FROM employees e LEFT JOIN dictionaries d ON e.position_id = d.id LEFT JOIN accessgroups g ON e.grp = g.id"
					+ " LEFT JOIN departments dpt ON e.department_id = dpt.id "
					+"WHERE e.fired=false AND e.department_id = "+deptId+" OR (e.boss = true AND e.department_id IN ("
					+ "SELECT id FROM departments WHERE parent_id = "+deptId
					+ "))");

//			+ "WHERE e.fired=false AND (e.department_id IN ("+DBConnect.getSubDeptsList(deptId)+"))");
			ResultSet result = ps.executeQuery();
			
			while (result.next()) {
				Employee empl = new Employee(result.getInt("id"));
				empl.setName(result.getString("name"));
				empl.setLogin(result.getString("login"));
				empl.setPassword(result.getString("password"));
				empl.setEmail(result.getString("email"));
				empl.setGroup(result.getInt("grp"));
				empl.setGroupName(result.getString("grp_name"));
				empl.setPositionId(result.getInt("position_id"));
				empl.setPositionName(result.getString("position"));
				empl.setDepartmentId(result.getInt("department_id"));
				empl.setDepartmentName(result.getString("deptName"));
				empl.setBoss(result.getInt("boss")==1);
				empl.setFired(result.getInt("fired")==1);
				employees.add(empl);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getSubordinates exception: "+sqle.getMessage());
		}
	    return employees;
		// SELECT e.name, e.boss, e.department_id FROM employees e WHERE e.department_id = 1 OR (e.boss = true AND e.department_id IN (SELECT d.id FROM departments d WHERE d.parent_id = 1))
	}
	
	@Override
	public PagingLoadResult<Employee> getEmployeesByDepartment(int deptId, boolean showFired, PagingLoadConfig config) {
		conn = DBConnect.getConnection();
		
		String sortClause = "";
		if (config.getSortInfo().size() > 0) {
			SortInfo sort = config.getSortInfo().get(0);
			
			if (sort.getSortField() != null) {
				String sortField = sort.getSortField();
				if (sortField != null) {
					sortClause = " ORDER BY "+ sortField + " " + sort.getSortDir().toString();
				}
			}
		} else {
			sortClause = " ORDER BY e.boss DESC, e.name ";
		}
		
		int start = config.getOffset();
		int limit = config.getLimit();
		String limitClause = " LIMIT " + start + ", " + limit;
		String whereClause = " WHERE e.supervisor=false AND "+
				(showFired ? "" : "e.fired=false AND")+
				" e.department_id=" + deptId;
		List<Employee> employees = loadEmployees(whereClause, sortClause, limitClause);
		
		return new PagingLoadResultBean<Employee>(employees, getCount(whereClause), config.getOffset());
	}

	@Override
	public List<Employee> getColleagues(int deptId) {
		conn = DBConnect.getConnection();
		String sortClause = " ORDER BY e.name ";
		String whereClause = " WHERE e.supervisor=false AND e.fired=false AND e.boss=false AND e.department_id=" + deptId;
		List<Employee> employees = loadEmployees(whereClause, sortClause, "");
		return employees;
	}
	
	@Override
	public List<Employee> getEmployeesByAccessGroup(int groupId, boolean notInGroup) {
		conn = DBConnect.getConnection();
		String whereClause = " WHERE e.supervisor=false AND e.fired=false AND e.grp" + (notInGroup?"!=":"=") + groupId;
		String sortClause = " ORDER BY e.boss DESC, e.name ";
		String limitClause = "";
		return loadEmployees(whereClause, sortClause, limitClause);
	}
	
	@Override
	public Employee getEmployeeInfo(int id) {
		List<Employee> employees = loadEmployees(" WHERE e.id="+id, "", "");
		if (employees.size() == 0) {
		    MyLogger.warning("[__FILE__:getEmployeeInfo] Employee with id=" + id + " not found.");
		    return null;
		}
		return employees.get(0);
	}

	public List<Integer> getTaskApprovers() {
		List<Integer> confirmers = new ArrayList<Integer>();

	    try {
	    	conn = DBConnect.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT e.id FROM employees e INNER JOIN accessgroups g on e.grp=g.id WHERE g.task_approver=1");
			ResultSet result = ps.executeQuery();
			
			while (result.next()) {
				confirmers.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getTaskConfirmers exception: "+sqle.getMessage());
		}
	    return confirmers;
	}

	public List<Integer> getDeleteConfirmers() {
		List<Integer> confirmers = new ArrayList<Integer>();

	    try {
	    	conn = DBConnect.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT e.id FROM employees e INNER JOIN accessgroups g on e.grp=g.id WHERE g.delete_confirmer=1");
			ResultSet result = ps.executeQuery();
			
			while (result.next()) {
				confirmers.add(result.getInt("id"));
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getDeleteConfirmers exception: "+sqle.getMessage());
		}
	    return confirmers;
	}
	
	private List<Employee> loadEmployees(String whereClause, String sortClause, String limitClause) {
		List<Employee> employees = new ArrayList<Employee>();

	    try {
	    	conn = DBConnect.getConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT e.*, d.name position, dpt.name deptName, g.grp_name"
					+ " FROM employees e LEFT JOIN dictionaries d ON e.position_id = d.id LEFT JOIN accessgroups g ON e.grp = g.id"
					+ " LEFT JOIN departments dpt ON e.department_id = dpt.id " +whereClause + sortClause + limitClause);
			ResultSet result = ps.executeQuery();
//			System.out.println("loadEmpls qry="+"SELECT e.*, d.name position, dpt.name deptName, g.grp_name"
//					+ " FROM employees e LEFT JOIN dictionaries d ON e.position_id = d.id LEFT JOIN accessgroups g ON e.grp = g.id"
//					+ " LEFT JOIN departments dpt ON e.department_id = dpt.id " +whereClause + sortClause + limitClause);
			
			while (result.next()) {
				Employee empl = new Employee(result.getInt("id"));
				empl.setName(result.getString("name"));
				empl.setLogin(result.getString("login"));
				empl.setPassword(result.getString("password"));
				empl.setEmail(result.getString("email"));
				empl.setGroup(result.getInt("grp"));
				empl.setGroupName(result.getString("grp_name"));
				empl.setPositionId(result.getInt("position_id"));
				empl.setPositionName(result.getString("position"));
				empl.setDepartmentId(result.getInt("department_id"));
				empl.setDepartmentName(result.getString("deptName"));
				empl.setBoss(result.getInt("boss")==1);
				empl.setFired(result.getInt("fired")==1);
				employees.add(empl);
			}
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getAllEmployees exception: "+sqle.getMessage());
		}
	    return employees;
	}
	
	private int getCount(String whereClause) {
		int count = 0; 
	    try {
			PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM employees e " + whereClause);
			ResultSet result = ps.executeQuery();
			result.first();
			count = result.getInt(1);
			result.close();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("getcount exception: "+sqle.getMessage());
		}
	    return count;
	}
	
	@Override
	public boolean updateEmployee(Employee employee) {
		// Если сотрудник помечен как начальник подразделения, предыдущего начальника нужно разжаловать
		if (employee.isBoss())
			deBoss(employee.getDepartmentId());
		conn = DBConnect.getConnection();
		String qry = "UPDATE employees SET " + 
				"name = ?, " + //DBConnect.saveString(employee.getName()) + ", " +
				"login = ?, " + //DBConnect.saveString(employee.getLogin()) + ", " + 
				"password = ?, " + //DBConnect.saveString(employee.getPassword()) + ", " + 
				"email = ?, " + //DBConnect.saveString(employee.getEmail()) + ", " + 
				"grp =" + employee.getGroup() + ", " +
				"position_id =" + employee.getPositionId() + ", " +
				"department_id =" + employee.getDepartmentId() + ", " +
				"boss =" + employee.isBoss() + ", " +
				"fired =" + employee.isFired() +
				" WHERE id=" + employee.getId();
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.setString(1, employee.getName());
			ps.setString(2, employee.getLogin());
			ps.setString(3, employee.getPassword());
			ps.setString(4, employee.getEmail());
			int rows = ps.executeUpdate();
			ps.close();
			return (rows > 0);
		} catch (SQLException sqle) {
			System.out.println("updateEmployee exception: "+sqle.getMessage());
		}
		return false;
	}

	@Override
	public int addEmployee(Employee employee) {
		// Если сотрудник помечен как начальник подразделения, предыдущего начальника нужно разжаловать
		if (employee.isBoss())
			deBoss(employee.getDepartmentId());
		conn = DBConnect.getConnection();
		String qry = "INSERT INTO employees (name, login, password, email, grp, position_id, department_id, boss, fired) VALUES (?, ?, ?, ?, "+
//				DBConnect.saveString(employee.getName()) + ", " +
//				DBConnect.saveString(employee.getLogin()) + ", " +
//				DBConnect.saveString(employee.getPassword()) + ", " +
//				DBConnect.saveString(employee.getEmail()) + ", " +
				employee.getGroup() + ", " +
				employee.getPositionId() + ", " +
				employee.getDepartmentId() + ", " +
				employee.isBoss() + ", " +
				employee.isFired() +
				")";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
//			System.out.println("addContract qry="+qry);
			ps.setString(1, employee.getName());
			ps.setString(2, employee.getLogin());
			ps.setString(3, employee.getPassword());
			ps.setString(4, employee.getEmail());
			int result = ps.executeUpdate();
			if (result == 0)
				return 0;
			ResultSet rs = ps.getGeneratedKeys();
			rs.first();
			int key = rs.getInt(1);
			ps.close();
			return key;
		} catch (SQLException sqle) {
			System.out.println("addEmployee exception:"+sqle.getMessage());
		}
		return 0;
	}

	@Override
	public boolean deleteEmployee(int id) {
		conn = DBConnect.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE id="+id);
			ps.executeUpdate();
			ps.close();
//			System.out.println("rows = "+rows);
		} catch (SQLException sqle) {
			// Error: 1451 SQLSTATE: 23000 (ER_ROW_IS_REFERENCED_2) Message: Cannot delete or update a parent row: a foreign key constraint fails (%s)
//			if (sqle.getErrorCode() == 1451 || sqle.getErrorCode() == 1217)
//				throw new IllegalArgumentException("Удаление невозможно: в базе данных присутствуют записи об экспертизах/актах для данного договора.");
			System.out.println("deleteGuide exception: "+sqle.getMessage());
		}
		return true;
	}

	private void deBoss(int whichDept) {
		conn = DBConnect.getConnection();
		String qry = "UPDATE employees SET boss = false WHERE boss=true AND department_id=" + whichDept;
		try {
			PreparedStatement ps = conn.prepareStatement(qry);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("deBoss exception: "+sqle.getMessage());
		}
	}

}
