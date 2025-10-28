package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.ContractProps;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.ContractService;
import ru.techstandard.shared.ContractServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.core.client.util.ToggleGroup;
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
import com.sencha.gxt.widget.core.client.DatePicker;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent.ParseErrorHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.filters.DateFilter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class ContractsJournalPanel extends VBoxLayoutContainer {
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final ContractServiceAsync contractService = GWT.create(ContractService.class);
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final EmployeeServiceAsync emplooyeeService = GWT.create(EmployeeService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Contract>> gridLoader = null;
	
	TextField contractNum;
	TextField clientTitle;
	ComboBox<DictionaryRecord> contractSubj;
	ComboBox<Employee> responsible;
	DateField signedDate;
	DateField signedDateFrom;
	DateField signedDateTo;
	Radio radioClosedAll;
	ToggleGroup closednessToggle;
	
	StringFilter<Contract> numberFilter;
	StringFilter<Contract> titleFilter;
	StringFilter<Contract> subjFilter;
	StringFilter<Contract> responsibleFilter;
	DateFilter<Contract> signedDateFilter;
	StringFilter<Contract> closedFilter;
	
	ClientInfoWindow clientInfoWindow;
	ContractInfoWindow contractInfoWindow;
	HandlerRegistration clientWndHideHandleReg = null;
	HandlerRegistration contractWndHideHandleReg = null;
	
	Widget journGrid;
	AccessGroup group;
	int loggedUserId;
	
	public ContractsJournalPanel(int userId, AccessGroup grp) {
		super();
		clientInfoWindow = new ClientInfoWindow(!grp.isDeleteConfirmer());
		contractInfoWindow = new ContractInfoWindow();
		loggedUserId = userId;
		group = grp;
		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
		 
		VerticalLayoutContainer filterContainerA = new VerticalLayoutContainer();
		filterContainerA.setWidth(400);
		 
		contractNum = new TextField();
		FieldLabel contractNumLabel = new FieldLabel(contractNum, "№ договора");
		contractNumLabel.setLabelWidth(130);
		contractNum.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(contractNumLabel, new VerticalLayoutData(1, -1));
		
		clientTitle = new TextField();
		FieldLabel titleLabel = new FieldLabel(clientTitle, "Контрагент");
		titleLabel.setLabelWidth(130);
		clientTitle.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(titleLabel, new VerticalLayoutData(1, -1));
		 
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(props.key());
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_WORKSUBJS,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Contracts:168 - "+caught.getMessage());
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
	    contractSubj.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
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
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Contracts:204 - "+caught.getMessage());
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
	    responsible.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
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
	    signedDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
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
		signedDateFrom.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
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
		signedDateTo.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
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

		ContentPanel journalPanel = new ContentPanel();
		journalPanel.setHeadingText("Журнал");
		journalPanel.setBorders(true);
		journalPanel.setBodyBorder(false);
		
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(1);

        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);

        journGrid = getJournalGrid(); 
        journalPanel.setWidget(journGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
        flex.setFlex(0);
        this.add(filtersPanel, flex);

        flex2.setFlex(3);
        this.add(journalPanel, flex2);
	}
	
	private String getFilterValue(String fieldValue) {
		String value = fieldValue.trim();
		return value.length() == 0 ? null : value;
	}
	
	private void applyFilters() {
		if (gridLoader != null) {
			try {				
				TextField field = (TextField) numberFilter.getMenu().getWidget(0);
				numberFilter.setActive(false, false);
				field.setValue(getFilterValue(contractNum.getText()), true);
				numberFilter.setActive(true, false);
				
				field = (TextField) titleFilter.getMenu().getWidget(0);
				titleFilter.setActive(false, false);
				field.setValue(getFilterValue(clientTitle.getText()), true);
				titleFilter.setActive(true, false);
				
				field = (TextField) subjFilter.getMenu().getWidget(0);
				subjFilter.setActive(false, false);
				if (contractSubj.getCurrentValue() != null) {
					field.setValue(getFilterValue(contractSubj.getCurrentValue().getName()), true);
					subjFilter.setActive(true, false);
				}
				
				field = (TextField) responsibleFilter.getMenu().getWidget(0);
				responsibleFilter.setActive(false, false);
				if (responsible.getCurrentValue() != null) {
					field.setValue(getFilterValue(responsible.getCurrentValue().getName()), true);
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
		contractNum.setValue("");
		clientTitle.setValue("");
		contractSubj.setValue(null);
		responsible.setValue(null);
		signedDate.setValue(null);
		signedDateFrom.setValue(null);
		signedDateTo.setValue(null);
		closednessToggle.setValue(radioClosedAll);
		applyFilters();
	}

	private Widget getJournalGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Contract>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Contract>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Contract>> callback) {
				contractService.getContracts(loadConfig, callback);
			}
		};

		ContractProps props = GWT.create(ContractProps.class);

		ListStore<Contract> store = new ListStore<Contract>(new ModelKeyProvider<Contract>() {
					@Override
					public String getKey(Contract item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Contract>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Contract, PagingLoadResult<Contract>>(store));

		ColumnConfig<Contract, String> contractNumColumn = new ColumnConfig<Contract, String>(props.num(), 10, SafeHtmlUtils.fromTrustedString("Номер договора"));
		ColumnConfig<Contract, Date> contractSignedColumn = new ColumnConfig<Contract, Date>(props.signed(), 10, SafeHtmlUtils.fromTrustedString("Дата заключения"));
		contractSignedColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<Contract, String> workSubjColumn = new ColumnConfig<Contract, String>(props.workSubj(), 30, SafeHtmlUtils.fromTrustedString("Предмет договора"));
		ColumnConfig<Contract, String> clientNameColumn = new ColumnConfig<Contract, String>(props.clientName(), 30, SafeHtmlUtils.fromTrustedString("Наименование контрагента"));
		ColumnConfig<Contract, String> responsibleColumn = new ColumnConfig<Contract, String>(props.responsibleName(), 20, SafeHtmlUtils.fromTrustedString("Ответственный"));
		ColumnConfig<Contract, Date> expiresColumn = new ColumnConfig<Contract, Date>(props.expires(), 10, SafeHtmlUtils.fromTrustedString("Срок истечения"));
		expiresColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<Contract, String> statusColumn = new ColumnConfig<Contract, String>(props.status(), 10, SafeHtmlUtils.fromTrustedString("Статус"));
		
		
		List<ColumnConfig<Contract, ?>> l = new ArrayList<ColumnConfig<Contract, ?>>();
		l.add(contractNumColumn);
		l.add(contractSignedColumn);
		l.add(workSubjColumn);
		l.add(clientNameColumn);
		l.add(responsibleColumn);
		l.add(expiresColumn);
		l.add(statusColumn);
	
		ColumnModel<Contract> cm = new ColumnModel<Contract>(l);

		final Grid<Contract> journalGrid = new Grid<Contract>(store, cm) {
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
		journalGrid.getView().setViewConfig(new GridViewConfig<Contract>() {
			@Override
			public String getColStyle(Contract req, ValueProvider<? super Contract, ?> valueProvider, int rowIndex, int colIndex) {
				int responsibleId = req.getResponsibleID();
				String style = null;
				if (loggedUserId == responsibleId)
					style = "yellow";
				else if (rowIndex % 2 == 1)
					style = "darkened";
				return style;
			}
			@Override
			public String getRowStyle(Contract model, int rowIndex) {return null;}
		});
		
		journalGrid.setLoadMask(true);
		journalGrid.setLoader(gridLoader);
		journalGrid.getView().setForceFit(true);
		journalGrid.getView().setAutoExpandColumn(clientNameColumn);
		journalGrid.getView().setColumnLines(true);
		journalGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		journalGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Contract rec = journalGrid.getSelectionModel().getSelectedItem();
				contractInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "contracts"), group.isAllowed(Constants.ACCESS_PRINT, "contracts"));
			}
		});
		
		GridFilters<Contract> filters = new GridFilters<Contract>(gridLoader);
		filters.initPlugin(journalGrid);
		
		numberFilter = new StringFilter<Contract>(props.num());
		filters.addFilter(numberFilter);
		titleFilter = new StringFilter<Contract>(props.clientName());
		filters.addFilter(titleFilter);
		subjFilter = new StringFilter<Contract>(props.workSubj());
		filters.addFilter(subjFilter);
		responsibleFilter = new StringFilter<Contract>(props.responsibleName());
		filters.addFilter(responsibleFilter);
		signedDateFilter = new DateFilter<Contract>(props.signed());
		filters.addFilter(signedDateFilter);
		closedFilter = new StringFilter<Contract>(props.status());
		closedFilter.setActive(false, false);
		filters.addFilter(closedFilter);
		

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(false);
		con.add(journalGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar toolBar = new PagingToolBar(30);
		toolBar.addStyleName(ThemeStyles.get().style().borderTop());
		toolBar.getElement().getStyle().setProperty("borderBottom", "none");
		toolBar.bind(gridLoader);

		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.setData("grid", journalGrid);

		con.setData("pagerToolbar", toolBar);
		return con;
	}
	
	public FlowLayoutContainer getButtonsContainer() {
		FlowLayoutContainer flc = new FlowLayoutContainer();
		
		final HideHandler editHideHandler = new HideHandler() {
			@Override
			public void onHide(HideEvent event) {
				Window wnd = (Window) event.getSource();
				if (wnd.getData("hideButton") == null)
					return;
				if (wnd.getData("hideButton").equals("save")) {
					PagingToolBar toolbar = ((Component) journGrid).getData("pagerToolbar");
					toolbar.refresh();
				}
			}
		};
		
	    final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
	        @Override
	        public void onDialogHide(DialogHideEvent event) {
	        	if (event.getHideButton() == PredefinedButton.YES) {
	        	  Grid<Contract> grid = ((Component) journGrid).getData("grid");
	        	  Contract rec = grid.getSelectionModel().getSelectedItem();
	        	// если нет полномочий подтверждать удаление - пользователь может только помечать объекты на удаление
	        	  boolean markOnly = !group.isDeleteConfirmer();
	        	  contractService.deleteContract(rec.getId(), markOnly, new AsyncCallback<Boolean>(){
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить договор.");
						d.show();
					}
					@Override
					public void onSuccess(Boolean result) {
						PagingToolBar toolbar = ((Component) journGrid).getData("pagerToolbar");
						toolbar.refresh();
					}
	        	  });
	          }
	        }
	    };
	    
		if (group.isAllowed(Constants.ACCESS_READ, "dictionaries")) {
			TextButton viewClient = new TextButton("Просмотр контрагента");
			viewClient.setIcon(IMAGES.view());
			flc.add(viewClient, new MarginData(new Margins(0, 0, -5, 5)));
			
			viewClient.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Contract> grid = ((Component) journGrid).getData("grid");
					Contract rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание", "Прежде чем воспользоваться просмотром данных о контрагенте, пожалуйста, выберите какую-нибудь запись в журнале.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					
					clientService.getClientInfoById(rec.getClientID(), new AsyncCallback<Client>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные о контрагенте.");
							d.show();
						}
						@Override
						public void onSuccess(Client result) {
							clientInfoWindow.displayInfo(result, group.isAllowed(Constants.ACCESS_UPDATE, "dictionaries"), group.isAllowed(Constants.ACCESS_PRINT, "dictionaries"));
						}
					});
				}
			});
		}
		
		TextButton viewContract = new TextButton("Просмотр договора");
		viewContract.setIcon(IMAGES.view());
		flc.add(viewContract, new MarginData(new Margins(0, 0, -5, 5)));
		
		viewContract.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Contract> grid = ((Component) journGrid).getData("grid");
				Contract rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание", "Прежде чем воспользоваться просмотром данных о договоре, пожалуйста, выберите какую-нибудь запись в журнале.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				contractInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "contracts"), group.isAllowed(Constants.ACCESS_PRINT, "contracts"));
			}
		});
		
		if (group.isAllowed(Constants.ACCESS_INSERT, "contracts")) {
			TextButton addJournRecBtn = new TextButton("Добавить договор");
			addJournRecBtn.setIcon(IMAGES.addRow());
			flc.add(addJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));
			
			addJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					if (contractWndHideHandleReg == null)
						contractWndHideHandleReg = contractInfoWindow.addHideHandler(editHideHandler);
					final Contract tmpContract = new Contract(0);
					contractService.addContract(tmpContract, new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом договоре.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							tmpContract.setId(result);
							contractInfoWindow.editInfo(tmpContract, true, group.isAllowed(Constants.ACCESS_PRINT, "contracts"));
						}
					});
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_UPDATE, "contracts")) {
			TextButton editJournRecBtn = new TextButton("Редактировать договор");
			editJournRecBtn.setIcon(IMAGES.editRow());
			flc.add(editJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));
			
			editJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Contract> grid = ((Component) journGrid).getData("grid");
					Contract rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите договор, который Вы хотите отредактировать.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					if (contractWndHideHandleReg == null)
						contractWndHideHandleReg = contractInfoWindow.addHideHandler(editHideHandler);
					contractInfoWindow.editInfo(rec, false, group.isAllowed(Constants.ACCESS_PRINT, "contracts"));
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_DELETE, "contracts")) {
			TextButton deleteJournRecBtn = new TextButton("Удалить договор");
			deleteJournRecBtn.setIcon(IMAGES.deleteRow());
			flc.add(deleteJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));	  
			
			deleteJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Contract> grid = ((Component) journGrid).getData("grid");
					Contract rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите договор, который Вы хотите удалить.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранный договор?");
					box.setIcon(IMAGES.warning());
					box.addDialogHideHandler(deleteHideHandler);
					box.show();
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_PRINT, "contracts")) {
			TextButton printJournBtn = new TextButton("Печать");
			printJournBtn.setIcon(IMAGES.print());
			flc.add(printJournBtn, new MarginData(new Margins(0, 0, -5, 0)));
			
			printJournBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Contract> grid = ((Component) journGrid).getData("grid");
					FilterPagingLoadConfig config = (FilterPagingLoadConfig) grid.getLoader().getLastLoadConfig();
					contractService.getPrintableContractList(config, new AsyncCallback<String>() {
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

		return flc;
	}
}
