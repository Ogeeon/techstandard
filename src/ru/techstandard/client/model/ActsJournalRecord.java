package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

public class ActsJournalRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id=0;
	private Integer contractId=0;
	private String workNum="";
	private String contractNum="";
	private Date contractDate=null;
	private Date workDate=null;
	private String clientName="";
	private int clientId=0;
	private String workSubj="";
	private int workSubjId=0;
	private String objType="";
	private int objTypeId=0;
	private String objName="";
	private String objFNum="";
	private String objRNum="";
	private String clientAddress="";
	private String clientPhone="";
	private String clientEmail="";
	private String clientBoss="";
	private String clientINN="";
	private Date nextWorkDate=null;
	private Integer daysLeft=0;
	private boolean done=false;
	private String completed="";
	private String notes="";
	private boolean deleted=false;
	private int deletedBy=0;


	public ActsJournalRecord() {
		this.id = 0;
	}

	public ActsJournalRecord(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id == null ? 0 : id;
	}

	public void setId(Integer id) {
		this.id = id == null ? 0 : id;
	}

	public Integer getContractId() {
		return contractId;
	}

	public void setContractId(Integer contractId) {
		this.contractId = contractId;
	}

	public String getWorkNum() {
		return workNum == null ? "" : workNum;
	}

	public void setWorkNum(String workNum) {
		this.workNum = workNum == null ? "" : workNum;
	}

	public String getContractNum() {
		return contractNum == null ? "" : contractNum;
	}

	public void setContractNum(String contractNum) {
		this.contractNum = contractNum == null ? "" : contractNum;
	}

	public Date getContractDate() {
		return contractDate;
	}

	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}

	public Date getWorkDate() {
		return workDate;
	}

	public void setWorkDate(Date workDate) {
		this.workDate = workDate;
	}  

	public String getClientName() {
		return clientName == null ? "" : clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName == null ? "" : clientName;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getWorkSubj() {
		return workSubj == null ? "" : workSubj;
	}

	public void setWorkSubj(String workSubj) {
		this.workSubj = workSubj == null ? "" : workSubj;
	}

	public int getWorkSubjId() {
		return workSubjId;
	}

	public void setWorkSubjId(int workSubjId) {
		this.workSubjId = workSubjId;
	}

	public String getObjType() {
		return objType == null ? "" : objType;
	}

	public void setObjType(String objType) {
		this.objType = objType == null ? "" : objType;
	}

	public int getObjTypeId() {
		return objTypeId;
	}

	public void setObjTypeId(int objTypeId) {
		this.objTypeId = objTypeId;
	}

	public String getObjName() {
		return objName == null ? "" : objName;
	}

	public void setObjName(String objName) {
		this.objName = objName == null ? "" : objName;
	}

	public String getObjFNum() {
		return objFNum == null ? "" : objFNum;
	}

	public void setObjFNum(String objFNum) {
		this.objFNum = objFNum == null ? "" : objFNum;
	}

	public String getObjRNum() {
		return objRNum == null ? "" : objRNum;
	}

	public void setObjRNum(String objRNum) {
		this.objRNum = objRNum == null ? "" : objRNum;
	}

	public String getClientAddress() {
		return clientAddress == null ? "" : clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress == null ? "" : clientAddress;
	}

	public String getClientPhone() {
		return clientPhone == null ? "" : clientPhone;
	}

	public void setClientPhone(String clientPhone) {
		this.clientPhone = clientPhone == null ? "" : clientPhone;
	}

	public String getClientEmail() {
		return clientEmail == null ? "" : clientEmail;
	}

	public void setClientEmail(String clientEmail) {
		this.clientEmail = clientEmail == null ? "" : clientEmail;
	}

	public String getClientBoss() {
		return clientBoss == null ? "" : clientBoss;
	}

	public void setClientBoss(String clientBoss) {
		this.clientBoss = clientBoss == null ? "" : clientBoss;
	}

	public String getClientINN() {
		return clientINN == null ? "" : clientINN;
	}

	public void setClientINN(String clientINN) {
		this.clientINN = clientINN == null ? "" : clientINN;
	}

	public Date getNextWorkDate() {
		return nextWorkDate;
	}

	public void setNextWorkDate(Date nextWorkDate) {
		this.nextWorkDate = nextWorkDate;
	}

	public Integer getDaysLeft() {
		return daysLeft == null ? 0 : daysLeft;
	}

	public void setDaysLeft(Integer daysLeft) {
		this.daysLeft = daysLeft == null ? 0 : daysLeft;
	}

	public String getCompleted() {
		return completed == null ? "" : completed;
	}

	public void setCompleted(String completed) {
		this.completed = completed == null ? "" : completed;
	}

	public String toString() {
		return getWorkNum();
	}

	public boolean isDone() {
		return done;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setDone(boolean done) {
		this.done = done;
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

}
