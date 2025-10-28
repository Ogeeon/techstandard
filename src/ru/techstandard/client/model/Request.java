package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id=0;
	private String description="";
	private Integer clientId=0;
	private String clientName="";
	private Integer responsibleId=0;
	private String responsibleName="";
	private Date dueDate=null;
	private String notes="";
	private boolean deleted=false;
	private int deletedBy=0;
	
	public Request () {
	}
	public Request (int id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getClientId() {
		return clientId;
	}
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public Integer getResponsibleId() {
		return responsibleId;
	}
	public void setResponsibleId(Integer responsibleId) {
		this.responsibleId = responsibleId;
	}
	public String getResponsibleName() {
		return responsibleName;
	}
	public void setResponsibleName(String responsibleName) {
		this.responsibleName = responsibleName;
	}
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public int getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(int deletedBy) {
		this.deletedBy = deletedBy;
	}
	
	public String toString() {
		return "["+id+"]"+description+" from ["+clientId+"] "+clientName;
	}
}
