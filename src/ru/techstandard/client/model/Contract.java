package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class Contract implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id=0;
	private int clientID=0;
	private String clientName="";
	private int subjID=0;
	private String workSubj="";
	private int responsibleId=0;
	private String employeeName="";
	private String num="";
	private Date signed=null;
	private Date expires=null;
	private boolean closed=false;
	private String notes="";
	private boolean deleted=false;
	private int deletedBy=0;
	
	public Contract() {
	}
	
	public Contract(int ID) {
		this.id = ID;
	}
	
	public Contract(int ID, int clientID) {
		this.id = ID;
		this.clientID = clientID;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int iD) {
		id = iD;
	}
	public int getClientID() {
		return clientID;
	}
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	public String getClientIdStr() {
		return String.valueOf(clientID);
	}
	public void setClientIdStr(String clientID) {
		try {
			this.clientID = Integer.valueOf(clientID);
		} catch (Exception e) {
			this.clientID = 0;
		} 
	}
	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public int getSubjID() {
		return subjID;
	}
	public void setSubjID(int subjID) {
		this.subjID = subjID;
	}
	public String getWorkSubj() {
		return workSubj;
	}

	public void setWorkSubj(String workSubj) {
		this.workSubj = workSubj;
	}

	public int getResponsibleID() {
		return responsibleId;
	}
	public void setResponsibleID(int responsibleID) {
		this.responsibleId = responsibleID;
	}
	public String getResponsibleName() {
		return employeeName;
	}

	public void setResponsibleName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public Date getSigned() {
		return signed;
	}
	public void setSigned(Date signed) {
		this.signed = signed;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public String getStatus() {
		return closed?"Закрытый":"Исполняемый";
	}

	public void setStatus(String status) {
		this.closed = status.equals("Закрытый"); 
	}

	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCaption() {
		DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy");
		return "№ " + num + " от " + dateFormat.format(signed);
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
		DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MMMM.yyyy");		
		return "["+id+"] "+num + " @ " + (signed==null ? "" : dateFormat.format(signed)) + ", [" +clientID+"] "+clientName;
	}
}
