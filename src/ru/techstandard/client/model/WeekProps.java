package ru.techstandard.client.model;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface WeekProps extends PropertyAccess<Week> {
	ModelKeyProvider<Task> id();

	ValueProvider<Week, Integer> mon();
	ValueProvider<Week, Integer> tue();
	ValueProvider<Week, Integer> wed();
	ValueProvider<Week, Integer> thu();
	ValueProvider<Week, Integer> fri();
	ValueProvider<Week, Integer> sat();
	ValueProvider<Week, Integer> sun();
}
