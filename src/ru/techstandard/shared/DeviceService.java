package ru.techstandard.shared;

import ru.techstandard.client.model.Device;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

@RemoteServiceRelativePath("deviceService")
public interface DeviceService extends RemoteService {
	PagingLoadResult<Device> getDevices(FilterPagingLoadConfig config);
	Device getDevice(int id);
	int addDevice(Device device);
	boolean updateDevice(Device device);
	boolean deleteDevice(int id, boolean markOnly);
	String getPrintableDeviceCard(int id);
	String getPrintableDeviceList();
}
