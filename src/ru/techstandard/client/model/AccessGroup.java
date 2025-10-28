package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public class AccessGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public interface AccessGroupProps extends PropertyAccess<AccessGroup> {
		ModelKeyProvider<AccessGroup> id();
		
		@Path("name")
		LabelProvider<AccessGroup> nameLabel();
		
		ValueProvider<AccessGroup, String> name();
		ValueProvider<AccessGroup, String> description();
		ValueProvider<AccessGroup, String> creator();
		ValueProvider<AccessGroup, String> approver();
		ValueProvider<AccessGroup, String> needApproval();
		ValueProvider<AccessGroup, String> deleteConfirmer();
	}

	private int id=0;
	private String name="";
	private String description="";
	private boolean taskCreator=false;
	private boolean taskApprover=false;
	private boolean needApproval=false;
	private boolean deleteConfirmer=false;
	private Map<String, Integer> access = new HashMap<String, Integer>();
	
	public AccessGroup() {
		initAccess();
	}
	
	public AccessGroup(int id, String name) {
		this.setId(id);
		this.setName(name);
		initAccess();
	}
	
	private void initAccess() {
		for (int idx = 0; idx < Constants.SECTION_KEYS.length; idx++) {
			access.put(Constants.SECTION_KEYS[idx], 0);
		}
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isTaskCreator() {
		return taskCreator;
	}

	public void setTaskCreator(boolean taskCreator) {
		this.taskCreator = taskCreator;
	}

	public boolean isTaskApprover() {
		return taskApprover;
	}

	public void setTaskApprover(boolean taskApprover) {
		this.taskApprover = taskApprover;
	}

	public String getCreator() {
		return taskCreator ? "Да" : "Нет";
	}
	
	public void setCreator(String creator) {
		if (creator == null)
			taskCreator = false;
		else
			taskCreator = creator.equals("Да");
	}
	
	public String getApprover() {
		return taskApprover ? "Да" : "Нет";
	}
	
	public void setApprover(String approver) {
		if (approver == null)
			taskApprover = false;
		else
			taskApprover = approver.equals("Да");
	}

	public String getNeedApproval() {
		return needApproval ? "Да" : "Нет";
	}
	
	public void setNeedApproval(String needApprovalStr) {
		if (needApprovalStr == null)
			needApproval = false;
		else
			needApproval = needApprovalStr.equals("Да");
	}
	
	public boolean isNeedApproval() {
		return needApproval;
	}

	public void setNeedApproval(boolean needApproval) {
		this.needApproval = needApproval;
	}

	public boolean isDeleteConfirmer() {
		return deleteConfirmer;
	}

	public void setDeleteConfirmer(boolean deleteConfirmer) {
		this.deleteConfirmer = deleteConfirmer;
	}

	public String getDeleteConfirmer() {
		return deleteConfirmer ? "Да" : "Нет";
	}
	
	public void setDeleteConfirmer(String deleteConfirmerStr) {
		if (deleteConfirmerStr == null)
			deleteConfirmer = false;
		else
			deleteConfirmer = deleteConfirmerStr.equals("Да");
	}
	
	public Map<String, Integer> getAccess() {
		return access;
	}

	public void setAccess(Map<String, Integer> access) {
		this.access = access;
	}

	public boolean isAllowed(int operation, String onObject) {
		if (!access.containsKey(onObject))
			return false;
		int rights = access.get(onObject);
		// i.e. 0110 & 0100 == 0100 
		return (rights & operation) == operation;
	}
	
	public void setAllowed(int operation, String onObject, boolean allowed) {
		if (!access.containsKey(onObject))
			return;
		int currAccess = access.get(onObject);
		access.put(onObject, allowed ? (currAccess | operation) : (currAccess & ~operation));
	}
	
	public String toString() {
		return "["+id+"] "+name+", access="+access;
	}
}
