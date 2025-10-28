package ru.techstandard.server;

import javax.servlet.http.HttpSession;

import ru.techstandard.client.model.UserDTO;

public class Utils {
	static int getUserIdFromSession(HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj != null && userObj instanceof UserDTO) {
        	UserDTO loggedUser = (UserDTO) userObj;
        	return loggedUser.getEmployeeId();
        } else {
        	System.out.println("acts: gonna throw NotLoggedExc");
//        	throw new NotLoggedInException();
        	return 0;
        }
	}
}
