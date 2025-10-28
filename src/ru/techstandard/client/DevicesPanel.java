package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Device;
import ru.techstandard.client.model.DeviceProps;
import ru.techstandard.shared.DeviceService;
import ru.techstandard.shared.DeviceServiceAsync;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class DevicesPanel extends VBoxLayoutContainer {
	private final DeviceServiceAsync deviceService = GWT.create(DeviceService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Device>> gridLoader = null;
	
//	TextField deviceTitle;
//	TextField clientTitle;
//	ComboBox<DictionaryRecord> contractSubj;
//	ComboBox<Employee> responsible;
//	DateField signedDate;
//	DateField signedDateFrom;
//	DateField signedDateTo;
//	Radio radioClosedAll;
//	ToggleGroup closednessToggle;
//	
//	StringFilter<Contract> numberFilter;
//	StringFilter<Contract> titleFilter;
//	StringFilter<Contract> subjFilter;
//	StringFilter<Contract> responsibleFilter;
//	DateFilter<Contract> signedDateFilter;
//	StringFilter<Contract> closedFilter;
	
	Widget devicesGrid;
	DeviceInfoWindow deviceInfoWindow;
	HideHandler deviceWindowHideHandler;

	int loggedUserId;
	AccessGroup group;
	
	public DevicesPanel (int userId, AccessGroup accessGroup) {
		super();
		loggedUserId = userId;
		group = accessGroup;
		deviceInfoWindow = new DeviceInfoWindow();
		
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
		ToolBar topToolBar = new ToolBar();
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);
        this.add(topToolBar, flex);
        
        deviceWindowHideHandler = new HideHandler() {
	        @Override
	        public void onHide(HideEvent event) {
	        	Window wnd = (Window) event.getSource();
	        	if (wnd.getData("hideButton") == null)
	        		return;
	        	if (wnd.getData("hideButton").equals("save")) {
	        		PagingToolBar toolbar = ((Component) devicesGrid).getData("pagerToolbar");
	        		toolbar.refresh();
	        	}
	        }
		};
		deviceInfoWindow.addHideHandler(deviceWindowHideHandler);

		TextButton viewDeviceBtn = new TextButton("Просмотр");
		viewDeviceBtn.setIcon(IMAGES.view());
		topToolBar.add(viewDeviceBtn);
		
		viewDeviceBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
				Grid<Device> grid = ((Component) devicesGrid).getData("grid");
				Device rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите устройство, карточку которого Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
        	    
				deviceInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "devices"), group.isAllowed(Constants.ACCESS_PRINT, "devices"));	        	
	        }
		});
		
		if (group.isAllowed(Constants.ACCESS_INSERT, "devices")) {
			TextButton addDeviceBtn = new TextButton("Добавить");
			addDeviceBtn.setIcon(IMAGES.addRow());
			topToolBar.add(addDeviceBtn);
			addDeviceBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					//				Для того, чтобы можно было закачивать вложения сразу при создании устройства, создадим запись в базе - её нужно будет удалить при отмене
					final Device tmpDevice = new Device("временное");
					deviceService.addDevice(tmpDevice, new AsyncCallback<Integer>() {
						@Override
						public void onSuccess(Integer result) {
							tmpDevice.setId(result);
							deviceInfoWindow.editInfo(tmpDevice, true, group.isAllowed(Constants.ACCESS_PRINT, "devices"));
						}
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом устройстве.");
							d.show();
						}
					});
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_UPDATE, "devices")) {
			TextButton editDeviceBtn = new TextButton("Редактировать");
			editDeviceBtn.setIcon(IMAGES.editRow());
			topToolBar.add(editDeviceBtn);

			editDeviceBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Device> grid = ((Component) devicesGrid).getData("grid");
					Device rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите устройство, которое Вы хотите отредактировать.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}

					deviceInfoWindow.editInfo(rec, false, group.isAllowed(Constants.ACCESS_PRINT, "devices"));	        	
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_DELETE, "devices")) {
			final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
				@Override
				public void onDialogHide(DialogHideEvent event) {
					if (event.getHideButton() == PredefinedButton.YES) {
						Grid<Device> grid = ((Component) devicesGrid).getData("grid");
						Device rec = grid.getSelectionModel().getSelectedItem();
						if (rec == null) {
							MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите устройство, которое Вы хотите удалить.");
							d.setIcon(IMAGES.information());
							d.show();
							return;
						}
						deviceService.deleteDevice(rec.getId(), !group.isDeleteConfirmer(), new AsyncCallback<Boolean> () {
							@Override
							public void onFailure(Throwable caught) {
								AlertMessageBox d = new AlertMessageBox("Ошибка", caught.getMessage());
								d.show();
							}
							@Override
							public void onSuccess(Boolean result) {
								PagingToolBar toolbar = ((Component) devicesGrid).getData("pagerToolbar");
								toolbar.refresh();
							}
						});					
					}
				}
			};

			TextButton deleteDeviceBtn = new TextButton("Удалить");
			deleteDeviceBtn.setIcon(IMAGES.delete());
			topToolBar.add(deleteDeviceBtn);
			deleteDeviceBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Device> grid = ((Component) devicesGrid).getData("grid");
					Device rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите устройство, которое Вы хотите удалить.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранное устройство?");
					box.setIcon(IMAGES.warning());
					box.addDialogHideHandler(deleteHideHandler);
					box.show();
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_PRINT, "devices")) {
			TextButton printDevicesBtn = new TextButton("Печать");
			printDevicesBtn.setIcon(IMAGES.print());
			topToolBar.add(printDevicesBtn);
			printDevicesBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					deviceService.getPrintableDeviceList(new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
							d.show();
						}
						@Override
						public void onSuccess(String result) {
							String printHTML = result;
							Print.it(printHTML);
						}
					});
				}
			});
		}
		
/*		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
		 
		VerticalLayoutContainer filterContainerA = new VerticalLayoutContainer();
		filterContainerA.setWidth(400);
		 
		deviceTitle = new TextField();
		FieldLabel contractLabel = new FieldLabel(deviceTitle, "Наименование");
		contractLabel.setLabelWidth(130);
		deviceTitle.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(contractLabel, new VerticalLayoutData(1, -1));
		
		clientTitle = new TextField();
		FieldLabel titleLabel = new FieldLabel(clientTitle, "Контрагент");
		titleLabel.setLabelWidth(130);
		clientTitle.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(titleLabel, new VerticalLayoutData(1, -1));
		 
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(props.key());
	    
	    dictionaryService.getDictionaryContents(DICT_WORKSUBJS,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Alert", "data NOT received");
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						store.addAll(result);
					}
				});
	 
	    contractSubj = new ComboBox<DictionaryRecord>(store, props.nameLabel());
	    contractSubj.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
	    	  applyFilters();
	      }
	    });
	    contractSubj.setAllowBlank(true);
	    contractSubj.setForceSelection(true);
	    contractSubj.setTriggerAction(TriggerAction.ALL);
	    FieldLabel subjLabel = new FieldLabel(contractSubj, "Предмет договора");
	    subjLabel.setLabelWidth(130);
	    filterContainerA.add(subjLabel, new VerticalLayoutData(1, -1));
	    
	    EmployeeProps emplProps = GWT.create(EmployeeProps.class);
	    final ListStore<Employee> emplStore = new ListStore<Employee>(emplProps.id());
	    
	    emplooyeeService.getAllEmployees(
				new AsyncCallback<List<Employee>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Alert", "data NOT received");
						d.show();
					}

					@Override
					public void onSuccess(List<Employee> result) {
						emplStore.addAll(result);
					}
				});
	 
	    responsible = new ComboBox<Employee>(emplStore, emplProps.nameLabel());
	    responsible.addValueChangeHandler(new ValueChangeHandler<Employee>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<Employee> event) {
	    	  applyFilters();
	      }
	    });
	    responsible.setAllowBlank(true);
	    responsible.setForceSelection(true);
	    responsible.setTriggerAction(TriggerAction.ALL);
	    FieldLabel respLabel = new FieldLabel(responsible, "Ответственный");
	    respLabel.setLabelWidth(130);
	    filterContainerA.add(respLabel, new VerticalLayoutData(1, -1));

	    
		VerticalLayoutContainer filterContainerB = new VerticalLayoutContainer();
		filterContainerB.setWidth(300); 
		 
	    signedDate = new DateField();
	    signedDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    signedDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    FieldLabel signedDateLabel = new FieldLabel(signedDate, "Дата заключения");
	    signedDateLabel.setLabelWidth(150);
	    filterContainerB.add(signedDateLabel, new VerticalLayoutData(1, -1));
		 
		signedDateFrom = new DateField();
	    signedDateFrom.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    signedDateFrom.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    FieldLabel signedDateFromLabel = new FieldLabel(signedDateFrom, "Дата заключения от");
	    signedDateFromLabel.setLabelWidth(150);
		filterContainerB.add(signedDateFromLabel, new VerticalLayoutData(1, -1));
		
		signedDateTo = new DateField();
	    signedDateTo.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    signedDateTo.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    FieldLabel signedDateToLabel = new FieldLabel(signedDateTo, "Дата заключения до");
	    signedDateToLabel.setLabelWidth(150);
		filterContainerB.add(signedDateToLabel, new VerticalLayoutData(1, -1));
		
		Radio radioClosedYes = new Radio();
		radioClosedYes.setBoxLabel("Да");
	    
	    Radio radioClosedNo = new Radio();
	    radioClosedNo.setBoxLabel("Нет");
	    
	    radioClosedAll = new Radio();
	    radioClosedAll.setBoxLabel("Все");
	    radioClosedAll.setValue(true);
	    radioClosedAll.setHeight(18);
	 
	    HorizontalPanel hp = new HorizontalPanel();
	    hp.setSpacing(2);
	    hp.add(radioClosedYes);
	    hp.add(radioClosedNo);
	    hp.add(radioClosedAll);
	 
	    FieldLabel closedLabel = new FieldLabel(hp, "Закрытый");
	    closedLabel.setLabelWidth(150);
	    filterContainerB.add(closedLabel, new VerticalLayoutData(1, -1));
	 
	    closednessToggle = new ToggleGroup();
	    closednessToggle.add(radioClosedYes);
	    closednessToggle.add(radioClosedNo);
	    closednessToggle.add(radioClosedAll);
	    closednessToggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
	      public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
	        applyFilters();
	      }
	    });
	    closednessToggle.setValue(radioClosedAll);
	    
		
		HBoxLayoutContainer filtersContainer = new HBoxLayoutContainer();
		filtersContainer.setPadding(new Padding(0));
		filtersContainer.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
		
		filtersContainer.add(filterContainerA, new BoxLayoutData(new Margins(0, 15, 0, 0)));
		filtersContainer.add(filterContainerB, new BoxLayoutData(new Margins(0, 5, 0, 0)));
		
		VerticalLayoutContainer fltBtnsContainer = new VerticalLayoutContainer();
		fltBtnsContainer.setPixelSize(100, 50);
		TextButton resetFilters = new TextButton("Сбросить фильтры");
		resetFilters.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				resetFilters();
			}
		});
		fltBtnsContainer.add(resetFilters, new VerticalLayoutData(1, -1));
		 
		filtersContainer.add(fltBtnsContainer, new BoxLayoutData(new Margins(0, 5, 0, 0)));
		
		filtersPanel.add(filtersContainer);
*/
		ContentPanel devicesPanel = new ContentPanel();
		devicesPanel.setHeadingText("График поверки оборудования");
		devicesPanel.setBorders(true);
		devicesPanel.setBodyBorder(false);
		
//		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
//        flex.setFlex(1);
//
        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
//        flex2.setFlex(3);

        devicesGrid = getDevicesGrid(); 
        devicesPanel.setWidget(devicesGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
//        flex.setFlex(0);
//        this.add(filtersPanel, flex);

        flex2.setFlex(5);
        this.add(devicesPanel, flex2);
	}


	private Widget getDevicesGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Device>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Device>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Device>> callback) {
				deviceService.getDevices(loadConfig, callback);
			}
		};

		DeviceProps props = GWT.create(DeviceProps.class);

		ListStore<Device> store = new ListStore<Device>(new ModelKeyProvider<Device>() {
					@Override
					public String getKey(Device item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Device>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Device, PagingLoadResult<Device>>(store));

		ColumnConfig<Device, String> titleColumn = new ColumnConfig<Device, String>(props.title(), 35, SafeHtmlUtils.fromTrustedString("Наименование,<br>обозначение"));
		ColumnConfig<Device, String> typeColumn = new ColumnConfig<Device, String>(props.type(), 15, SafeHtmlUtils.fromTrustedString("Тип<br>&nbsp;"));
		ColumnConfig<Device, String> precisionColumn = new ColumnConfig<Device, String>(props.precision(), 10, SafeHtmlUtils.fromTrustedString("Класс<br>точности"));
		ColumnConfig<Device, String> rangeColumn = new ColumnConfig<Device, String>(props.range(), 10, SafeHtmlUtils.fromTrustedString("Предел<br>измерений"));
		ColumnConfig<Device, Integer> numColumn = new ColumnConfig<Device, Integer>(props.num(), 5, SafeHtmlUtils.fromTrustedString("Кол-<br>во"));
		ColumnConfig<Device, String> fNumColumn = new ColumnConfig<Device, String>(props.fnum(), 10, SafeHtmlUtils.fromTrustedString("Заводской<br>номер"));
		ColumnConfig<Device, String> certColumn = new ColumnConfig<Device, String>(props.checkCert(), 15, SafeHtmlUtils.fromTrustedString("№ св-ва о<br>поверке"));
		ColumnConfig<Device, Integer> periodColumn = new ColumnConfig<Device, Integer>(props.checkPeriod(), 10, SafeHtmlUtils.fromTrustedString("Периодичность<br>поверки (мес.)"));
		ColumnConfig<Device, Date> lastCheckColumn = new ColumnConfig<Device, Date>(props.lastChecked(), 10, SafeHtmlUtils.fromTrustedString("Дата пред.<br>поверки"));
		lastCheckColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<Device, String> checkerColumn = new ColumnConfig<Device, String>(props.checker(), 20, SafeHtmlUtils.fromTrustedString("Место проведения<br>поверки"));
		ColumnConfig<Device, Date> nextCheckColumn = new ColumnConfig<Device, Date>(props.nextCheck(), 10, SafeHtmlUtils.fromTrustedString("Срок след.<br>поверки"));
		nextCheckColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<Device, Integer> groenColumn = new ColumnConfig<Device, Integer>(props.groen(), 10, SafeHtmlUtils.fromTrustedString("Сфера<br>ГРОЕН"));
		ColumnConfig<Device, String> emplColumn = new ColumnConfig<Device, String>(props.responsibleName(), 15, SafeHtmlUtils.fromTrustedString("Ответственный"));
		
		List<ColumnConfig<Device, ?>> l = new ArrayList<ColumnConfig<Device, ?>>();
		l.add(titleColumn);
		l.add(typeColumn);
		l.add(precisionColumn);
		l.add(rangeColumn);
		l.add(numColumn);
		l.add(fNumColumn);
		l.add(certColumn);
		l.add(periodColumn);
		l.add(lastCheckColumn);
		l.add(checkerColumn);
		l.add(nextCheckColumn);
		l.add(groenColumn);
		l.add(emplColumn);
	
		ColumnModel<Device> cm = new ColumnModel<Device>(l);

		final Grid<Device> devicesGrid = new Grid<Device>(store, cm) {
			@Override
			protected void onAfterFirstAttach() {
				super.onAfterFirstAttach();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						gridLoader.load();
					}
				});
			}
		};
		devicesGrid.getView().setViewConfig(new GridViewConfig<Device>() {
			@Override
			public String getColStyle(Device req, ValueProvider<? super Device, ?> valueProvider, int rowIndex, int colIndex) {
				int responsibleId = req.getResponsibleId();
				String style = null;
				if (loggedUserId == responsibleId)
					style = "yellow";
				else if (rowIndex % 2 == 1)
					style = "darkened";
				return style;
			}
			@Override
			public String getRowStyle(Device model, int rowIndex) {return null;}
		});

		devicesGrid.setLoadMask(true);
		devicesGrid.setLoader(gridLoader);
		devicesGrid.getView().setForceFit(true);
		devicesGrid.getView().setAutoExpandColumn(titleColumn);
		devicesGrid.getView().setColumnLines(true);
		devicesGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		devicesGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Device rec = devicesGrid.getSelectionModel().getSelectedItem();
				deviceInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "devices"), group.isAllowed(Constants.ACCESS_PRINT, "devices"));
			}
		});
		
		GridFilters<Device> filters = new GridFilters<Device>(gridLoader);
		filters.initPlugin(devicesGrid);
		
//		numberFilter = new StringFilter<Contract>(props.num());
//		filters.addFilter(numberFilter);
//		titleFilter = new StringFilter<Contract>(props.clientName());
//		filters.addFilter(titleFilter);
//		subjFilter = new StringFilter<Contract>(props.workSubj());
//		filters.addFilter(subjFilter);
//		responsibleFilter = new StringFilter<Contract>(props.employeeName());
//		filters.addFilter(responsibleFilter);
//		signedDateFilter = new DateFilter<Contract>(props.signed());
//		filters.addFilter(signedDateFilter);
//		closedFilter = new StringFilter<Contract>(props.status());
//		closedFilter.setActive(false, false);
//		filters.addFilter(closedFilter);
		

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(false);
		con.add(devicesGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar toolBar = new PagingToolBar(30);
		toolBar.addStyleName(ThemeStyles.get().style().borderTop());
		toolBar.getElement().getStyle().setProperty("borderBottom", "none");
		toolBar.bind(gridLoader);

		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.setData("grid", devicesGrid);

		con.setData("pagerToolbar", toolBar);
		return con;
	}
	
/*	
	private String getFilterValue(String fieldValue) {
		String value = fieldValue.trim();
		return value.length() == 0 ? null : value;
	}
	
	private void applyFilters() {
		if (gridLoader != null) {
			try {				
				TextField field = (TextField) numberFilter.getMenu().getWidget(0);
				numberFilter.setActive(false, false);
				field.setValue(getFilterValue(deviceTitle.getText()), true);
				numberFilter.setActive(true, false);
				
				field = (TextField) titleFilter.getMenu().getWidget(0);
				titleFilter.setActive(false, false);
				field.setValue(getFilterValue(clientTitle.getText()), true);
				titleFilter.setActive(true, false);
				
				field = (TextField) subjFilter.getMenu().getWidget(0);
				subjFilter.setActive(false, false);
				if (contractSubj.getValue() != null) {
					field.setValue(getFilterValue(contractSubj.getValue().getName()), true);
					subjFilter.setActive(true, false);
				}
				
				field = (TextField) responsibleFilter.getMenu().getWidget(0);
				responsibleFilter.setActive(false, false);
				if (responsible.getValue() != null) {
					field.setValue(getFilterValue(responsible.getValue().getName()), true);
					responsibleFilter.setActive(true, false);
				}
								
				signedDateFilter.setActive(false, false);
//				Конечная дата интервала
				CheckMenuItem before = (CheckMenuItem) signedDateFilter.getMenu().getWidget(0);
				DatePicker beforePicker = (DatePicker) before.getSubMenu().getWidget(0);
				if (signedDateTo.getValue() != null)
					beforePicker.setValue(signedDateTo.getValue());
				else
					before.setChecked(false, false);
//				Начальная дата интервала
				CheckMenuItem after = (CheckMenuItem) signedDateFilter.getMenu().getWidget(1);
				DatePicker afterPicker = (DatePicker) after.getSubMenu().getWidget(0);
				if (signedDateFrom.getValue() != null)
					afterPicker.setValue(signedDateFrom.getValue());
				else
					after.setChecked(false, false);
//				Точная дата
				CheckMenuItem on = (CheckMenuItem) signedDateFilter.getMenu().getWidget(3);
				DatePicker onPicker = (DatePicker) on.getSubMenu().getWidget(0);
				if (signedDate.getValue() != null)
					onPicker.setValue(signedDate.getValue());
				else
					on.setChecked(false, false);				
				signedDateFilter.setActive(true, false);
				
				Radio radio = (Radio)closednessToggle.getValue();
				if (radio.getBoxLabel().equals("Все")) {
					closedFilter.setActive(false, false);
				} else {
					field = (TextField) closedFilter.getMenu().getWidget(0);
					closedFilter.setActive(false, false);
					field.setValue(radio.getBoxLabel(), true);
					closedFilter.setActive(true, false);
				}
				
			} catch (Exception e) {
				System.out.println("applyFilters got errored: "+e.getMessage()+" "+ e.toString());
			}
    	}
	}
	
	private void resetFilters() {		
		deviceTitle.setValue("");
		clientTitle.setValue("");
		contractSubj.setValue(null);
		responsible.setValue(null);
		signedDate.setValue(null);
		signedDateFrom.setValue(null);
		signedDateTo.setValue(null);
		closednessToggle.setValue(radioClosedAll);
		applyFilters();
	}
*/	
}
