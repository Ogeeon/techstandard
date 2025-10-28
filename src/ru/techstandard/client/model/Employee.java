package ru.techstandard.client.model;

import java.io.Serializable;

public class Employee implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id=0;
	private String name="";
	private String login="";
	private String password="";
	private String email="";
	private Integer group=0;
	private String groupName="";
	private Integer positionId=0;
	private String positionName="";
	private Integer departmentId=0;
	private String departmentName="";
	private boolean boss=false;
	private boolean fired=false;
	
	public Employee() {
	}
	
	public Employee(int ID) {
		this.id = ID;
	}
	
	public Employee(int ID, String name) {
		this.id = ID;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int iD) {
		id = iD;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getGroup() {
		return group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getPositionId() {
		return positionId;
	}

	public void setPositionId(Integer position) {
		this.positionId = position;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public boolean isBoss() {
		return boss;
	}

	public void setBoss(boolean boss) {
		this.boss = boss;
	}

	public String getDeptLeader() {
		return boss?"Да":"Нет";
	}
	
	public void setDeptLeader(String isLeader) {
		boss = isLeader.equals("Да");
	}
	
	public boolean isFired() {
		return fired;
	}

	public void setFired(boolean fired) {
		this.fired = fired;
	}
	
	public String getFiredStr() {
		return fired?"Да":"Нет";
	}

	public void setFiredStr(String fired) {
		this.fired = fired.equals("Да");
	}

	public String toString() {
		return "["+id+"] "+name;
	}
}
