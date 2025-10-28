package ru.techstandard.client.model;

import java.util.Date;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface DeviceProps extends PropertyAccess<Device> {
	@Path("id")
	ModelKeyProvider<Device> id();

	ValueProvider<Device, String> title();
	ValueProvider<Device, String> type();
	ValueProvider<Device, String> precision();
	ValueProvider<Device, String> range();
	ValueProvider<Device, Integer> num();
	ValueProvider<Device, String> fnum();
	ValueProvider<Device, String> checkCert();
	ValueProvider<Device, Integer> checkPeriod();
	ValueProvider<Device, Date> lastChecked();
	ValueProvider<Device, String> checker();
	ValueProvider<Device, Date> nextCheck();
	ValueProvider<Device, Integer> groen();
	ValueProvider<Device, String> notes();
	ValueProvider<Device, String> responsibleName();
}
