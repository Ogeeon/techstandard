package ru.techstandard.client.model;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface ClientProps extends PropertyAccess<Client> {
	@Path("id")
	ModelKeyProvider<Client> id();

	@Path("name")
	LabelProvider<Client> nameLabel();

	ValueProvider<Client, String> name();
	ValueProvider<Client, String> boss();
	ValueProvider<Client, String> address();
	ValueProvider<Client, String> phone();
	ValueProvider<Client, String> inn();
	ValueProvider<Client, String> email();
}
