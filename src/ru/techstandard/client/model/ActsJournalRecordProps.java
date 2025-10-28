package ru.techstandard.client.model;

import java.util.Date;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface ActsJournalRecordProps extends PropertyAccess<DictionaryRecord> {
	ModelKeyProvider<ActsJournalRecord> id();
	
	@Path("workNum")
	LabelProvider<ActsJournalRecord> numLabel();

	ValueProvider<ActsJournalRecord, String> workNum();
	ValueProvider<ActsJournalRecord, String> contractNum();
	ValueProvider<ActsJournalRecord, Date> contractDate();
	ValueProvider<ActsJournalRecord, Date> workDate();
	ValueProvider<ActsJournalRecord, String> clientName();
	ValueProvider<ActsJournalRecord, String> workSubj();
	ValueProvider<ActsJournalRecord, String> objType();
	ValueProvider<ActsJournalRecord, String> objName();
	ValueProvider<ActsJournalRecord, String> objFNum();
	ValueProvider<ActsJournalRecord, String> objRNum();
	ValueProvider<ActsJournalRecord, String> clientAddress();
	ValueProvider<ActsJournalRecord, String> clientPhone();
	ValueProvider<ActsJournalRecord, String> clientEmail();
	ValueProvider<ActsJournalRecord, String> clientBoss();
	ValueProvider<ActsJournalRecord, String> clientINN();
	ValueProvider<ActsJournalRecord, Date> nextWorkDate();
	ValueProvider<ActsJournalRecord, Integer> daysLeft();
	ValueProvider<ActsJournalRecord, String> completed();
	ValueProvider<ActsJournalRecord, Boolean> done();
}
