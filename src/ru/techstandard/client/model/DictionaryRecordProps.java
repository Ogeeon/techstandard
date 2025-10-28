package ru.techstandard.client.model;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface DictionaryRecordProps extends PropertyAccess<DictionaryRecord> {
	@Path("id")
	ModelKeyProvider<DictionaryRecord> key();

	@Path("name")
	LabelProvider<DictionaryRecord> nameLabel();

	ValueProvider<DictionaryRecord, String> name();

}
