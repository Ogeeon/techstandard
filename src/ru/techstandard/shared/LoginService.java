package ru.techstandard.shared;


import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.LoginErrorException;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("loginService")
public interface LoginService extends RemoteService {
    /**
     * Utility class for simplifying access to the instance of async service.
     */
    public static class Util
    {
        private static LoginServiceAsync instance;
 
        public static LoginServiceAsync getInstance()
        {
            if (instance == null)
            {
                instance = GWT.create(LoginService.class);
            }
            return instance;
        }
    }
 
    UserDTO loginServer(String name, String password) throws LoginErrorException;
 
    UserDTO loginFromSessionServer();
     
    void logout();
}
