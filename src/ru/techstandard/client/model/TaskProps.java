package ru.techstandard.client.model;

import java.util.Date;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface TaskProps extends PropertyAccess<Task> {
	ModelKeyProvider<Task> id();

	ValueProvider<Task, String> creatorName();
	ValueProvider<Task, String> typeName();
	ValueProvider<Task, String> executorName();
	ValueProvider<Task, Date> startDate();
	ValueProvider<Task, Date> dueDate();
	ValueProvider<Task, Date> completedDate();
	ValueProvider<Task, String> description();
	ValueProvider<Task, String> completed();
	ValueProvider<Task, String> notes();
}
