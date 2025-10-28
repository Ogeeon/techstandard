package ru.techstandard.client.model;

import java.io.Serializable;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public class Department implements Serializable {
	public interface DepartmentProps extends PropertyAccess<Department> {
		ModelKeyProvider<Department> id();

		ValueProvider<Department, String> name();
	}
	
	private static final long serialVersionUID = 1L;

	private Integer id=0;
	private String name="";
	private Integer parentId=0;
	
	public Department() {
		
	}
	public Department(int id) {
		this.id = id;
	}
	public Department(String name) {
		this.name = name;
	}
	public Department(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getParentId() {
		return parentId;
	}
	
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
	public String toString() {
		return "["+id+"] "+name+", parent="+parentId;
	}
}
