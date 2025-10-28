package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id=0;
	private int created_by=0;
	private String creatorName="";
	private int typeId=0;
	private String typeName="";
	private int executorId=0;
	private String executorName="";
	private Date startDate=null;
	private Date dueDate=null;
	private Date completedDate=null;
	private String description="";
	private int status=0;
	private String completed="";
	private String notes="";
	private int followerId=0;
	private boolean viewed=false;
	
	public Task() {}
	public Task(int id, int created_by) {
		this.setId(id);
		this.setCreatedBy(created_by);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCreatedBy() {
		return created_by;
	}
	public void setCreatedBy(int created_by) {
		this.created_by = created_by;
	}
	public String getCreatorName() {
		return creatorName;
	}
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	public int getTypeId() {
		return typeId;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public int getExecutorId() {
		return executorId;
	}
	public void setExecutorId(int executorId) {
		this.executorId = executorId;
	}
	public String getExecutorName() {
		return executorName;
	}
	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public Date getCompletedDate() {
		return completedDate;
	}
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getCompleted() {
		return completed == null ? "" : completed;
	}
	public void setCompleted(String completed) {
		this.completed = completed == null ? "" : completed;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public int getFollowerId() {
		return followerId;
	}
	public void setFollowerId(int followerId) {
		this.followerId = followerId;
	}
	public boolean isViewed() {
		return viewed;
	}
	public void setViewed(boolean viewed) {
		this.viewed = viewed;
	}
	public String toString() {
		return "["+id+"] "+executorId+" : "+description;
	}
}
