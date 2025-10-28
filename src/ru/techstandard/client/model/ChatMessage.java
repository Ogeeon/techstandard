package ru.techstandard.client.model;

import java.util.Date;
import java.util.List;


public interface ChatMessage {

	int getId();
	void setId(int id);
	
	String getRoom();
	void setRoom(String room);
	
	Long getTimeStamp();
	void setTimeStamp(Long timStamp);
	
	Date getDate();
	void setDate(Date date);
    
    String getAuthor();
    void setAuthor(String author);
    
    String getMessage();
    void setMessage(String message);
    
    boolean isSystemMessage();
    void setSystemMessage(boolean isSystemMessage);
    
    List<String> getLoggedUsers();
    void setLoggedUsers(List<String> users);
}