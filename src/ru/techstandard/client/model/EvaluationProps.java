package ru.techstandard.client.model;

import java.util.Date;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface EvaluationProps extends PropertyAccess<Evaluation> {
	ModelKeyProvider<Evaluation> id();

	ValueProvider<Evaluation, String> employeeName();
	ValueProvider<Evaluation, String> position();
	ValueProvider<Evaluation, String> fieldName();
	ValueProvider<Evaluation, String> certNum();
	ValueProvider<Evaluation, Date> lastEvalDate();
	ValueProvider<Evaluation, Date> nextEvalDate();
}
