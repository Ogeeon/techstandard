package ru.techstandard.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.LoginErrorException;
import ru.techstandard.shared.LoginService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
	private static final long serialVersionUID = 1L;
	private Connection conn = null;

	@Override
    public UserDTO loginServer(String name, String password) throws LoginErrorException
    {
		AccessGroupServiceImpl accGrpServ = new AccessGroupServiceImpl();
		
		conn = DBConnect.getConnection();
		UserDTO user = null;
		PreparedStatement command;
		try {
			command = conn.prepareStatement("select * from employees e where e.login=? and e.password=?");
			command.setString(1, name);
			command.setString(2, password);
			ResultSet result = command.executeQuery();
			while (result.next()) {
				saveAudit(name, password, true);
				if (result.getBoolean("fired"))
					throw new LoginErrorException("Ваша учётная запись заблокирована.");
				user = new UserDTO(result.getInt("id"), result.getString("name"));
				user.setDepartmentId(result.getInt("department_id"));
				user.setGroupId(result.getInt("grp"));
				user.setBoss(result.getBoolean("boss"));
		        user.setSessionId(this.getThreadLocalRequest().getSession().getId());
			}
			if (user == null) {
				saveAudit(name, password, false);
				throw new LoginErrorException("Неверное сочетание логина и пароля.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		List<AccessGroup> groups = accGrpServ.getAccessGroups(" WHERE g.id="+user.getGroupId());
		if (groups.size()==1)
			user.setAccess(groups.get(0));
		else
			user.setAccess(new AccessGroup());
		user.setLoggedIn(true);
		storeUserInSession(user); 
        return user;
    }
 
    @Override
    public UserDTO loginFromSessionServer()
    {
        return getUserAlreadyFromSession();
    }
 
    @Override
    public void logout()
    {
        deleteUserFromSession();
    }
 
    public UserDTO getUserAlreadyFromSession()
    {
        UserDTO user = null;
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
//        System.out.println("session id="+session.getId());
        Object userObj = session.getAttribute("user");
//        System.out.println("getUserAlreadyFromSession: userObj="+(UserDTO)userObj);
        if (userObj != null && userObj instanceof UserDTO)
        {
            user = (UserDTO) userObj;
        }
        return user;
    }
 
    private void storeUserInSession(UserDTO user)
    {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute("user", user);
//        System.out.println("session id="+session.getId());
    }
 
    private void deleteUserFromSession()
    {
        HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("user");
    }

    private void saveAudit(String name, String password, boolean success) {
    	String qry = "INSERT INTO loginaudit (timestamp, username, password, successful) VALUES ("+
    			DBConnect.saveDateTime(new Date()) + ", ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, name);
			ps.setString(2, password);
			ps.setBoolean(3, success);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			System.out.println("saveAudit exception:"+sqle.getMessage());
		}
    }
}
