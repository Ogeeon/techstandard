package ru.techstandard.client;

import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.ActsJournalRecord;
import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.DeletedObject;
import ru.techstandard.client.model.DeletedObject.DeletedObjectProps;
import ru.techstandard.client.model.Device;
import ru.techstandard.client.model.Guide;
import ru.techstandard.client.model.Request;
import ru.techstandard.shared.ActsJournalService;
import ru.techstandard.shared.ActsJournalServiceAsync;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.ContractService;
import ru.techstandard.shared.ContractServiceAsync;
import ru.techstandard.shared.DeletedObjectsService;
import ru.techstandard.shared.DeletedObjectsServiceAsync;
import ru.techstandard.shared.DeviceService;
import ru.techstandard.shared.DeviceServiceAsync;
import ru.techstandard.shared.GuideService;
import ru.techstandard.shared.GuideServiceAsync;
import ru.techstandard.shared.RequestService;
import ru.techstandard.shared.RequestServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class DeletedObjectsPanel extends VBoxLayoutContainer {
	private DeletedObjectsServiceAsync delObjService = GWT.create(DeletedObjectsService.class);
	private ActsJournalServiceAsync actsService = GWT.create(ActsJournalService.class);
	private ClientServiceAsync clientService = GWT.create(ClientService.class);
	private ContractServiceAsync contractService = GWT.create(ContractService.class);
	private DeviceServiceAsync devicesService = GWT.create(DeviceService.class);
	private GuideServiceAsync guidesService = GWT.create(GuideService.class);
	private RequestServiceAsync requestsService = GWT.create(RequestService.class);
	private final Images IMAGES = GWT.create(Images.class);

	Grid<DeletedObject> objectsGrid;
	
	public DeletedObjectsPanel () {
		super();
		
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
		ToolBar topToolBar = new ToolBar();
		topToolBar.setHeight(30);
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);
        this.add(topToolBar, flex);


		TextButton refreshBtn = new TextButton("Обновить данные");
		refreshBtn.setIcon(IMAGES.refresh());
		topToolBar.add(refreshBtn);
		refreshBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				delObjService.getDeletedObjects(new AsyncCallback<List<DeletedObject>>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. DelObj:88 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DeletedObject> result) {
						objectsGrid.getStore().replaceAll(result);
					}
				});
			}
		});
		
		TextButton showObjectBtn = new TextButton("Открыть карточку объекта");
		showObjectBtn.setIcon(IMAGES.view());
		topToolBar.add(showObjectBtn);
		showObjectBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	        	DeletedObject rec = objectsGrid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите объект, карточку которого вы хотите открыть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				showObject(rec.getTable(), rec.getObjId());
	        }
		});
		
		TextButton undeleteBtn = new TextButton("Отменить удаление");
		undeleteBtn.setIcon(IMAGES.undo2());
		topToolBar.add(undeleteBtn);
		undeleteBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	        	List<DeletedObject> recs = objectsGrid.getSelectionModel().getSelectedItems();
				if (recs.size() == 0) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите объект, для которого нужно отменить удаление.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				final DeletedObject rec = recs.get(0);
				final PromptMessageBox box = new PromptMessageBox("Отмена удаления", "Пожалуйста, укажите причину отмены удаления объекта:<br>"+rec.getDescription());
				box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
				box.addDialogHideHandler(new DialogHideHandler() {
					@Override
					public void onDialogHide(DialogHideEvent event) {
						if (event.getHideButton() == PredefinedButton.CANCEL) {
							return;
						}
						if (box.getValue() == null || box.getValue().trim().isEmpty()) {
							MessageBox d = new MessageBox("Недостаточно данных", "Пожалуйста, укажите причину отмены удаления объекта.");
							d.setIcon(Images.INSTANCE.information());
							d.show();
							return;
						}
						delObjService.undoObjectDelete(rec.getTable(), rec.getObjId(), rec.getDeleter(), box.getValue().trim(), new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
								d.show();
							}
							@Override
							public void onSuccess(Boolean result) {
								delObjService.getDeletedObjects(new AsyncCallback<List<DeletedObject>>() {
									@Override
									public void onFailure(Throwable caught) {
										AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
										d.show();
									}
									@Override
									public void onSuccess(List<DeletedObject> result) {
										objectsGrid.getStore().replaceAll(result);
										objectsGrid.getSelectionModel().deselectAll();
									}
								});
							}
						});
					}
				});
				box.setWidth(348);
				box.show();
				box.getTextField().setWidth(336);
				box.getTextField().focus();
				box.forceLayout();
	        }
		});
		
		final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
	        @Override
	        public void onDialogHide(DialogHideEvent event) {
	        	if (event.getHideButton() == PredefinedButton.YES) {
	        	  List<DeletedObject> recs = objectsGrid.getSelectionModel().getSelectedItems();
	        	  for (int idx=0; idx < recs.size(); idx++) {
	        		  final DeletedObject rec = recs.get(idx);
	        		  delObjService.applyObjectDelete(rec.getTable(), rec.getObjId(), new AsyncCallback<Boolean>() {
	        			  @Override
	        			  public void onFailure(Throwable caught) {
	        				  AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
	        				  d.show();
	        			  }
	        			  @Override
	        			  public void onSuccess(Boolean result) {
	        				  delObjService.getDeletedObjects(new AsyncCallback<List<DeletedObject>>() {
	        					  @Override
	        					  public void onFailure(Throwable caught) {
	        						  AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
	        						  d.show();
	        					  }
	        					  @Override
	        					  public void onSuccess(List<DeletedObject> result) {
	        						  objectsGrid.getStore().replaceAll(result);
	        						  objectsGrid.getSelectionModel().deselectAll();
	        					  }
	        				  });
	        			  }
	        		  });
	        	  }
	          }
	        }
	    };
	    
	    TextButton applyDeleteBtn = new TextButton("Подтвердить удаление");
	    applyDeleteBtn.setIcon(IMAGES.delete());
		topToolBar.add(applyDeleteBtn);
		applyDeleteBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	        	List<DeletedObject> recs = objectsGrid.getSelectionModel().getSelectedItems();
				if (recs.size() == 0) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите хотя бы один объект, который нужно удалить.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите подтвердить удаление объектов ("+recs.size()+")?");
				box.setIcon(IMAGES.warning());
				box.addDialogHideHandler(deleteHideHandler);
				box.show();
	        }
		});
		
		ContentPanel objectsPanel = new ContentPanel();
		objectsPanel.setHeadingText("Список удалённых объектов");
		objectsPanel.setBorders(true);
		objectsPanel.setBodyBorder(false);
		
        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));

        objectsGrid = getObjectsGrid();
        delObjService.getDeletedObjects(new AsyncCallback<List<DeletedObject>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
				d.show();
			}
			@Override
			public void onSuccess(List<DeletedObject> result) {
				objectsGrid.getStore().addAll(result);
			}
		});
        objectsPanel.setWidget(objectsGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
        flex2.setFlex(5);
        this.add(objectsPanel, flex2);
	}


	private Grid<DeletedObject> getObjectsGrid () {
		DeletedObjectProps props = GWT.create(DeletedObjectProps.class);
		ListStore<DeletedObject> store = new ListStore<DeletedObject>(props.id());

		ColumnConfig<DeletedObject, String> sectionColumn = new ColumnConfig<DeletedObject, String>(props.section(), 2, "Раздел программы");
		ColumnConfig<DeletedObject, String> userNameColumn = new ColumnConfig<DeletedObject, String>(props.userName(), 2, "Сотрудник");
		ColumnConfig<DeletedObject, String> descriptionColumn = new ColumnConfig<DeletedObject, String>(props.description(), 10, "Описание объекта");
		
		List<ColumnConfig<DeletedObject, ?>> l = new ArrayList<ColumnConfig<DeletedObject, ?>>();
		l.add(sectionColumn);
		l.add(userNameColumn);
		l.add(descriptionColumn);
	
		ColumnModel<DeletedObject> cm = new ColumnModel<DeletedObject>(l);

		final Grid<DeletedObject> objectsGrid = new Grid<DeletedObject>(store, cm);

		objectsGrid.setLoadMask(true);
		objectsGrid.getView().setForceFit(true);
		objectsGrid.getView().setAutoExpandColumn(sectionColumn);
		objectsGrid.getView().setStripeRows(true);
		objectsGrid.getView().setColumnLines(true);
		objectsGrid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
		
		objectsGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				DeletedObject rec = objectsGrid.getSelectionModel().getSelectedItem();
				showObject(rec.getTable(), rec.getObjId());
			}
		});

		return objectsGrid;
	}

	private void showObject(String table, int id) {
		int tblNum=-1;
		for (int idx = 0; idx < Constants.TABLES.length; idx++)
			if (Constants.TABLES[idx].equals(table)) {
				tblNum = idx;
				break;
			}
		if (tblNum == -1)
			return;
		switch (tblNum) {
			case 0: { // "acts" 
				actsService.getJournalRecord(id, new AsyncCallback<ActsJournalRecord>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
						d.show();
					}
					@Override
					public void onSuccess(ActsJournalRecord result) {
						ActsJournalRecordWindow journRecWindow = new ActsJournalRecordWindow();
						journRecWindow.displayRecord(result, false, true);
					}
				});
				break;
			}
			case 1: { // "clients" 
				clientService.getClientInfoById(id, new AsyncCallback<Client>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
						d.show();
					}

					@Override
					public void onSuccess(Client result) {
						ClientInfoWindow clientInfoWindow = new ClientInfoWindow(false);
						clientInfoWindow.displayInfo(result, false, true);
					}
				});
				break;
			}
			case 2: { // "contracts" 
				contractService.getContractById(id, new AsyncCallback<Contract>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
						d.show();
					}

					@Override
					public void onSuccess(Contract result) {
						ContractInfoWindow contractInfoWindow = new ContractInfoWindow();
						contractInfoWindow.displayInfo(result, false, true);
					}
				});
				break;
			}
			case 3: { // "devices" 
				devicesService.getDevice(id, new AsyncCallback<Device>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
						d.show();
					}

					@Override
					public void onSuccess(Device result) {
						DeviceInfoWindow deviceInfoWindow = new DeviceInfoWindow();
						deviceInfoWindow.displayInfo(result, false, true);
					}
				});
				break;
			}
			case 4: { // "dictionaries" 
				MessageBox d = new MessageBox("Нет данных для отображения","Объект не содержит дополнительных свойств.");
				d.setIcon(IMAGES.information());
				d.show();
				break;
			}
			case 5: { // "guides" 
				guidesService.getGuide(id, new AsyncCallback<Guide>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
						d.show();
					}

					@Override
					public void onSuccess(Guide result) {
						GuideInfoWindow guideInfoWindow = new GuideInfoWindow();
						guideInfoWindow.displayInfo(result, false, true);
					}
				});
				break;
			}
			case 6: { // "requests" 
				requestsService.getRequest(id, new AsyncCallback<Request>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
						d.show();
					}

					@Override
					public void onSuccess(Request result) {
						RequestInfoWindow requestInfoWindow = new RequestInfoWindow();
						requestInfoWindow.displayInfo(result, false, true);
					}
				});
				break;
			}
		}
	}
	
	public void forceRefresh() {
		delObjService.getDeletedObjects(new AsyncCallback<List<DeletedObject>>() {
			@Override
			public void onFailure(Throwable caught) {}

			@Override
			public void onSuccess(List<DeletedObject> result) {
				objectsGrid.getStore().replaceAll(result);
			}
		});
	}
}
