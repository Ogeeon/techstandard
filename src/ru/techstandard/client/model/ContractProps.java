package ru.techstandard.client.model;

import java.util.Date;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface ContractProps extends PropertyAccess<Contract> {
	@Path("id")
	ModelKeyProvider<Contract> id();

	@Path("num")
	LabelProvider<Contract> numLabel();
	
	@Path("caption")
	LabelProvider<Contract> captionLabel();

	ValueProvider<Contract, Integer> clientID();
	ValueProvider<Contract, String> clientIdStr();
	ValueProvider<Contract, String> clientName();
	ValueProvider<Contract, Integer> subjID();
	ValueProvider<Contract, String> workSubj();
	ValueProvider<Contract, Integer> responsibleID();
	ValueProvider<Contract, String> responsibleName();
	ValueProvider<Contract, String> num();
	ValueProvider<Contract, Date> signed();
	ValueProvider<Contract, Date> expires();
	ValueProvider<Contract, String> status();
	ValueProvider<Contract, String> notes();
	ValueProvider<Contract, String> caption();
}
