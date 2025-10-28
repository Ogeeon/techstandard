package ru.techstandard.client.model;

import java.io.Serializable;

public class Attachement implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int ID=0;
	private String title="";
	private String filename="";
	private int attach_type=0;
	private String attach_type_name="";
	private int parent_type=0;
	private int parent_id=0;

	public Attachement() {
		this.ID = 0;
	}
	
	public Attachement(int ID) {
		this.setId(ID);
	}
	
	public Attachement(int ID, String title, String filename, int attach_type, int parent_type, int parent_id) {
		this.setId(ID);
		this.setTitle(title);
		this.setFilename(filename);
		this.setAttachType(attach_type);
		this.setParentType(parent_type);
		this.setParentId(parent_id);
	}

	public int getId() {
		return ID;
	}

	public void setId(int iD) {
		ID = iD;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public int getAttachType() {
		return attach_type;
	}

	public void setAttachType(int attach_type) {
		this.attach_type = attach_type;
	}

	public String getAttachTypeName() {
		return attach_type_name;
	}

	public void setAttachTypeName(String attach_type_name) {
		this.attach_type_name = attach_type_name;
	}

	public int getParentType() {
		return parent_type;
	}
	
	public String getParentTypeStr() {
		return String.valueOf(parent_type);
	}

	public void setParentType(int parent_type) {
		this.parent_type = parent_type;
	}
	
	public void setParentTypeStr(String parent_type) {
		try {
			this.parent_type = Integer.valueOf(parent_type);
		} catch (Exception e) {
			this.parent_type = 0;
		} 
	}
	
	public int getParentId() {
		return parent_id;
	}
	
	public String getParentIdStr() {
		return String.valueOf(parent_id);
	}

	public void setParentId(int parent_id) {
		this.parent_id = parent_id;
	}
	
	public void setParentIdStr(String parent_id) {
		try {
			this.parent_id = Integer.valueOf(parent_id);
		} catch (Exception e) {
			this.parent_id = 0;
		} 
	}

	public String toString() {
		return this.title+"|"+this.filename+", ["+parent_id+":"+parent_type+"]"+"("+attach_type+")";
	}
}
