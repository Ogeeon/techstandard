package ru.techstandard.client.model;

import java.io.Serializable;

public class DictionaryRecord implements Serializable {
	private static final long serialVersionUID = 5079971339927809523L;
	
	private Integer id=0;
	private int type=0;
	private String name="";
	private boolean deleted=false;
	private int deletedBy=0;
 
  public DictionaryRecord() {
  }
 
  public DictionaryRecord(String name) {
    this();
    this.name = name;
  }
 
  public DictionaryRecord(Integer id, String name) {
    this.id = id;
    this.name = name;
  }
 
  public Integer getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }
 
  public void setId(Integer id) {
    this.id = id;
  }
 
  public int getType() {
	return type;
}

public void setType(int type) {
	this.type = type;
}

public void setName(String name) {
    this.name = name;
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
    return "["+id+"]"+name;
  }

}
