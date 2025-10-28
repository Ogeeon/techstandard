package ru.techstandard.shared;

import ru.techstandard.client.model.UserDTO;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {

	void loginServer(String name, String password,
			AsyncCallback<UserDTO> callback);

	void loginFromSessionServer(AsyncCallback<UserDTO> callback);

	void logout(AsyncCallback<Void> callback);

}
