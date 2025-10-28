package ru.techstandard.client.model;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface EmployeeProps extends PropertyAccess<Employee> {
	@Path("id")
	ModelKeyProvider<Employee> id();

	@Path("name")
	LabelProvider<Employee> nameLabel();

	ValueProvider<Employee, String> name();
	ValueProvider<Employee, String> login();
	ValueProvider<Employee, String> firedStr();
	ValueProvider<Employee, String> password();
	ValueProvider<Employee, String> email();
	ValueProvider<Employee, String> groupName();
	ValueProvider<Employee, String> deptLeader();
	ValueProvider<Employee, String> positionName();
	ValueProvider<Employee, String> departmentName();
	
	ValueProvider<Employee, Boolean> boss();
}
