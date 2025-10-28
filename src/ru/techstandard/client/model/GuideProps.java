package ru.techstandard.client.model;

import java.util.Date;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface GuideProps extends PropertyAccess<Guide> {
	ModelKeyProvider<Guide> id();

	ValueProvider<Guide, String> objName();
	ValueProvider<Guide, String> objTypeName();
	ValueProvider<Guide, String> fNum();
	ValueProvider<Guide, String> rNum();
	ValueProvider<Guide, String> clientName();
	ValueProvider<Guide, String> contractNum();
	ValueProvider<Guide, String> actNum();
	ValueProvider<Guide, String> responsibleName();
	ValueProvider<Guide, Date> dueDate();
	ValueProvider<Guide, String> notes();
}
