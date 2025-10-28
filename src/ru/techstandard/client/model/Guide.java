package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

public class Guide implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id=0;
	private String objName="";
	private Integer objType=0;
	private String objTypeName="";
	private String fNum="";
	private String rNum="";
	private Integer clientId=0;
	private String clientName="";
	private Integer contractId=0;
	private String contractNum="";
	private Integer actId=0;
	private String actNum="";
	private Integer responsibleId=0;
	private String responsibleName="";
	private Date dueDate=null;
	private String notes="";
	private boolean deleted=false;
	private int deletedBy=0;
	
	public Guide () {
	}
	public Guide (int id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getObjName() {
		return objName;
	}
	public void setObjName(String objName) {
		this.objName = objName;
	}
	public Integer getObjType() {
		return objType;
	}
	public void setObjType(Integer objType) {
		this.objType = objType;
	}
	public String getObjTypeName() {
		return objTypeName;
	}
	public void setObjTypeName(String objTypeName) {
		this.objTypeName = objTypeName;
	}
	public String getFNum() {
		return fNum;
	}
	public void setFNum(String fNum) {
		this.fNum = fNum;
	}
	public String getRNum() {
		return rNum;
	}
	public void setRNum(String rNum) {
		this.rNum = rNum;
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
	public Integer getContractId() {
		return contractId;
	}
	public void setContractId(Integer contractId) {
		this.contractId = contractId;
	}
	public String getContractNum() {
		return contractNum;
	}
	public void setContractNum(String contractNum) {
		this.contractNum = contractNum;
	}
	public Integer getActId() {
		return actId;
	}
	public void setActId(Integer actId) {
		this.actId = actId;
	}
	public String getActNum() {
		return actNum;
	}
	public void setActNum(String actNum) {
		this.actNum = actNum;
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
		return "["+id+"]"+objName+"(["+objType+"] "+objTypeName+")";
	}
}
