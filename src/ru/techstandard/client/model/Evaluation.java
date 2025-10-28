package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

public class Evaluation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id=0;
	private int employeeId=0;
	private String employeeName="";
	private String position="";
	private int fieldId=0;
	private String fieldName="";
	private String certNum="";
	private Date lastEvalDate=null;
	private Date nextEvalDate=null;

	public Evaluation() {
	}
	
	public Evaluation(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getFieldId() {
		return fieldId;
	}

	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getCertNum() {
		return certNum;
	}

	public void setCertNum(String certNum) {
		this.certNum = certNum;
	}

	public Date getLastEvalDate() {
		return lastEvalDate;
	}

	public void setLastEvalDate(Date lastEvalDate) {
		this.lastEvalDate = lastEvalDate;
	}

	public Date getNextEvalDate() {
		return nextEvalDate;
	}

	public void setNextEvalDate(Date nextEvalDate) {
		this.nextEvalDate = nextEvalDate;
	}
	
	public String toString() {
		return "["+id+"] for ["+employeeId+"] "+employeeName;
	}
}
