package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;


public class TemplateContract implements Serializable {
	private static final long serialVersionUID = 1L;

	private int clientId;
	private String num;
	private String signer;
	private Date signed;
	private String foundation;
	private String subject;
	private int duration;
	private int prePay;
	private String unitName;
	private Double unitPrice;
	private Double totalPrice;
	private Date dueDate;
	private boolean multipleItems;
	
	public TemplateContract() {}
	
	public int getClientId() {
		return clientId;
	}
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public String getSigner() {
		return signer;
	}
	public void setSigner(String signer) {
		this.signer = signer;
	}
	public Date getSigned() {
		return signed;
	}
	public void setSigned(Date signed) {
		this.signed = signed;
	}
	public String getFoundation() {
		return foundation;
	}
	public void setFoundation(String foundation) {
		this.foundation = foundation;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getPrePay() {
		return prePay;
	}
	public void setPrePay(int prePay) {
		this.prePay = prePay;
	}
	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public Double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public boolean isMultipleItems() {
		return multipleItems;
	}
	public void setMultipleItems(boolean multipleItems) {
		this.multipleItems = multipleItems;
	}
	public String toString() {
		return "num="+num;
	}
}
