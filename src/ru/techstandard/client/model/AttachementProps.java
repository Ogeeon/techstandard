package ru.techstandard.client.model;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface AttachementProps extends PropertyAccess<Attachement> {
	@Path("id")
	ModelKeyProvider<Attachement> id();

	ValueProvider<Attachement, String> title();
	ValueProvider<Attachement, String> filename();
	ValueProvider<Attachement, Integer> attachType();
	ValueProvider<Attachement, String> attachTypeName();
	ValueProvider<Attachement, String> parentTypeStr();
	ValueProvider<Attachement, String> parentIdStr();
}
