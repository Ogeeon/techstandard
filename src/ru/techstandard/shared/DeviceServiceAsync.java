package ru.techstandard.shared;

import ru.techstandard.client.model.Device;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public interface DeviceServiceAsync {

	void getDevices(FilterPagingLoadConfig config, AsyncCallback<PagingLoadResult<Device>> callback);

	void addDevice(Device device, AsyncCallback<Integer> callback);

	void updateDevice(Device device, AsyncCallback<Boolean> callback);

	void deleteDevice(int id, boolean markOnly, AsyncCallback<Boolean> callback);

	void getPrintableDeviceCard(int id, AsyncCallback<String> callback);

	void getPrintableDeviceList(AsyncCallback<String> callback);

	void getDevice(int id, AsyncCallback<Device> callback);

}
