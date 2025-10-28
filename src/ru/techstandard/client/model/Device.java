package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

public class Device implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id=0;
	private String type="";
	private String title="";
	private String precision="";
	private String range="";
	private Integer num=0;
	private String fnum="";
	private String check_cert="";
	private Integer check_period=1;
	private Date lastChecked=null;
	private Integer checker_id=0;
	private String checker="";
	private Date next_check=null;
	private Integer groen=0;
	private String notes="";
	private boolean deleted=false;
	private int deletedBy=0;
	private int responsibleId=0;
	private String responsibleName="";

	public Device() {
	}
	
	public Device(int id) {
		this.setId(id);
	}
	
	public Device(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPrecision() {
		return precision;
	}

	public void setPrecision(String precision) {
		this.precision = precision;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public String getFnum() {
		return fnum;
	}

	public void setFnum(String fnum) {
		this.fnum = fnum;
	}

	public String getCheckCert() {
		return check_cert;
	}

	public void setCheckCert(String check_cert) {
		this.check_cert = check_cert;
	}

	public Integer getCheckPeriod() {
		return check_period;
	}

	public void setCheckPeriod(Integer check_period) {
		this.check_period = check_period;
	}

	public Date getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Date lastChecked) {
		this.lastChecked = lastChecked;
	}

	public Integer getCheckerId() {
		return checker_id;
	}

	public void setCheckerId(Integer checker_id) {
		this.checker_id = checker_id;
	}

	public String getChecker() {
		return checker;
	}

	public void setChecker(String checker) {
		this.checker = checker;
	}

	public Date getNextCheck() {
		return next_check;
	}

	public void setNextCheck(Date next_check) {
		this.next_check = next_check;
	}

	public Integer getGroen() {
		return groen;
	}

	public void setGroen(Integer groen) {
		this.groen = groen;
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

	public int getResponsibleId() {
		return responsibleId;
	}

	public void setResponsibleId(int responsibleId) {
		this.responsibleId = responsibleId;
	}

	public String getResponsibleName() {
		return responsibleName;
	}

	public void setResponsibleName(String responsibleName) {
		this.responsibleName = responsibleName;
	}

	public String toString() {
		return "Device["+ id +"] = title:"+title;
	}
}
