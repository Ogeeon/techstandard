package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ChatMsg implements Serializable, ChatMessage {
	private static final long serialVersionUID = 1L;

	private int id;
	private String author;
	private String room;
	private Long timeStamp;
	private String message;
	private boolean isSystemMessage;
	private List<String> loggedUsers;
	
	public ChatMsg() {}
	public ChatMsg(String author, Long timeStamp, String message, boolean isSystemMessage) {
		this.setAuthor(author);
		this.setTimeStamp(timeStamp);
		this.setMessage(message);
		this.setSystemMessage(isSystemMessage);
	}
	public ChatMsg(int id, String author, Long timeStamp, String message, boolean isSystemMessage) {
		this.id = id;
		this.setAuthor(author);
		this.setTimeStamp(timeStamp);
		this.setMessage(message);
		this.setSystemMessage(isSystemMessage);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public Long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean isSystemMessage() {
		return isSystemMessage;
	}
	
	public void setSystemMessage(boolean isSystemMessage) {
		this.isSystemMessage = isSystemMessage;
	}
	
	public List<String> getLoggedUsers() {
		return loggedUsers;
	}
	
	public void setLoggedUsers(List<String> loggedUsers) {
		this.loggedUsers = loggedUsers;
	}
	
	public Date getDate() {
		return new Date(timeStamp);
	}
	
	public void setDate(Date date) {
		timeStamp = date.getTime();
	}
	
	public String toString() {
		return "["+author+"@"+timeStamp+"] "+message;
	}
	
}
