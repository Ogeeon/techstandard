package ru.techstandard.client.model;

import java.io.Serializable;

public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private int employeeId=0;
	private int departmentId=0;
	private String name="";
	private boolean loggedIn=false;
	private int groupId=0;
	private boolean boss; 
	private String sessionId="";
	private AccessGroup access=null;
	
	public UserDTO() {}
	
	public UserDTO(int empl, String fio) {
		setEmployeeId(empl);
		setName(fio);
	}

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean logged) {
		this.loggedIn = logged;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public boolean isBoss() {
		return boss;
	}

	public void setBoss(boolean boss) {
		this.boss = boss;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public AccessGroup getAccess() {
		return access;
	}

	public void setAccess(AccessGroup access) {
		this.access = access;
	}
	
	public String toString() {
		return "["+employeeId+"] "+name+", "+(loggedIn?"":"not ")+"logged in";
	}
}
