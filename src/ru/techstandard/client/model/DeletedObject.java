package ru.techstandard.client.model;

import java.io.Serializable;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public class DeletedObject implements Serializable {
	public interface DeletedObjectProps extends PropertyAccess<DeletedObject> {
		@Path("id")
		ModelKeyProvider<DeletedObject> id();

		ValueProvider<DeletedObject, String> table();
		ValueProvider<DeletedObject, String> section();
		ValueProvider<DeletedObject, String> description();
		ValueProvider<DeletedObject, String> userName();
	}
	
	private static final long serialVersionUID = 1L;
	
	private String id="";
	private String table="";
	private String section="";
	private int objId=0;
	private String description="";
	private int deleter=0;
	private String userName="";
	
	public DeletedObject(){};
	public DeletedObject(String id, String table, String section, int objId, String description, int deleter, String userName) {
		this.setId(id);
		this.setTable(table);
		this.setSection(section);
		this.setObj(objId);
		this.setDescription(description);
		this.setDeleter(deleter);
		this.setUserName(userName);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public int getObjId() {
		return objId;
	}
	public void setObj(int objId) {
		this.objId = objId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getDeleter() {
		return deleter;
	}
	public void setDeleter(int deleter) {
		this.deleter = deleter;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
