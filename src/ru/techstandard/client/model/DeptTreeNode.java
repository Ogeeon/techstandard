package ru.techstandard.client.model;

import java.io.Serializable;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public class DeptTreeNode implements Serializable {
	public interface TreeNodeProps extends PropertyAccess<DeptTreeNode> {
		ModelKeyProvider<DeptTreeNode> id();

		ValueProvider<DeptTreeNode, String> name();
		ValueProvider<DeptTreeNode, Boolean> isLeaf();
	}
	
	private static final long serialVersionUID = 1L;

	private String id="";
	private String name="";
	private boolean isLeaf=false;
	
	public DeptTreeNode() {
		
	}
	public DeptTreeNode(String id) {
		this.id = id;
	}
	public DeptTreeNode(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}
	
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	
	public boolean hasChildren() {
		return !isLeaf;
	}
	
	public String toString() {
		return "["+id+"] "+name+", isLeaf="+isLeaf;
	}
}
