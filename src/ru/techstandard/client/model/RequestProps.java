package ru.techstandard.client.model;

import java.util.Date;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface RequestProps extends PropertyAccess<Request> {
	ModelKeyProvider<Request> id();

	ValueProvider<Request, String> description();
	ValueProvider<Request, String> clientName();
	ValueProvider<Request, String> responsibleName();
	ValueProvider<Request, Date> dueDate();
	ValueProvider<Request, String> notes();
}
