package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.ActsJournalRecord;
import ru.techstandard.client.model.ActsJournalRecordProps;
import ru.techstandard.shared.ActsJournalService;
import ru.techstandard.shared.ActsJournalServiceAsync;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;

import com.google.gwt.cell.client.AbstractCell;
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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
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
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.DateFilter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class ActsJournalPanel extends VBoxLayoutContainer {
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final ActsJournalServiceAsync journalService = GWT.create(ActsJournalService.class);
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ActsJournalRecord>> gridLoader = null;
	
	TextField clientTitle;
	TextField clientBossName;
	TextField clientINN;
	TextField clientAddress;
	TextField contractNum;
	ComboBox<DictionaryRecord> objectType;
	DateField workDate;
	DateField workDateFrom;
	DateField workDateTo;
	Radio radioCompletedAll;
	ToggleGroup completenessToggle;
	
	StringFilter<ActsJournalRecord> titleFilter;
	StringFilter<ActsJournalRecord> bossFilter;
	StringFilter<ActsJournalRecord> innFilter;
	StringFilter<ActsJournalRecord> addressFilter;
	StringFilter<ActsJournalRecord> contractFilter;
	StringFilter<ActsJournalRecord> objTypeFilter;
	DateFilter<ActsJournalRecord> workDateFilter;
	StringFilter<ActsJournalRecord> completedFilter;
	
	ClientInfoWindow clientInfoWindow;
	ActsJournalRecordWindow journRecWindow;
	HandlerRegistration journWndHideHandleReg = null;
	HandlerRegistration clientWndHideHandleReg = null;
	
	Widget journGrid;
	AccessGroup group;
	
	public ActsJournalPanel(AccessGroup accessGroup) {
		super();
		group = accessGroup;
		clientInfoWindow = new ClientInfoWindow(!group.isDeleteConfirmer());
		journRecWindow = new ActsJournalRecordWindow();
		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
		
		FieldSet clientFieldSet = new FieldSet();
		clientFieldSet.setHeadingText("Контрагент");
		clientFieldSet.setWidth(400);
		 
		VerticalLayoutContainer clientContainer = new VerticalLayoutContainer();
		clientFieldSet.add(clientContainer);
		 
		clientTitle = new TextField();
		FieldLabel titleLabel = new FieldLabel(clientTitle, "Наименование");
		titleLabel.setLabelWidth(130);
		clientTitle.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		clientContainer.add(titleLabel, new VerticalLayoutData(1, -1));
		 
		clientBossName = new TextField();
		FieldLabel fioLabel = new FieldLabel(clientBossName, "ФИО руководителя");
		fioLabel.setLabelWidth(130);
		clientBossName.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		clientContainer.add(fioLabel, new VerticalLayoutData(1, -1));
		 
		clientINN = new TextField();
		FieldLabel innLabel = new FieldLabel(clientINN, "ИНН");
		innLabel.setLabelWidth(130);
		clientINN.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		clientContainer.add(innLabel, new VerticalLayoutData(1, -1));
		
		clientAddress = new TextField();
		FieldLabel addressLabel = new FieldLabel(clientAddress, "Адрес");
		addressLabel.setLabelWidth(130);
		clientAddress.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		clientContainer.add(addressLabel, new VerticalLayoutData(1, -1));
	    
		contractNum = new TextField();
		FieldLabel contractLabel = new FieldLabel(contractNum, "№ договора");
		contractLabel.setLabelWidth(130);
		contractNum.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		clientContainer.add(contractLabel, new VerticalLayoutData(1, -1));

		FieldSet workFieldSet = new FieldSet();
		workFieldSet.setHeadingText("Экспертиза");
		workFieldSet.setWidth(400);
		 
		VerticalLayoutContainer workContainer = new VerticalLayoutContainer();
		workFieldSet.add(workContainer);
		 
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(props.key());
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_OBJTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Acts:195 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						store.addAll(result);
					}
				});
	 
	    objectType = new ComboBox<DictionaryRecord>(store, props.nameLabel());
	    objectType.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
			@Override
			public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
				applyFilters();				
			}
		});
	    objectType.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
	    objectType.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				applyFilters();				
			}
		});
	    objectType.setAllowBlank(true);
	    objectType.setForceSelection(true);
	    objectType.setTriggerAction(TriggerAction.ALL);
 
		workContainer.add(new FieldLabel(objectType, "Тип объекта"), new VerticalLayoutData(1, -1));
		 
	    workDate = new DateField();
	    workDate.setWidth(100);
	    workDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    workDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    workDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    workContainer.add(new FieldLabel(workDate, "Дата"), new VerticalLayoutData(1, -1));
		 
		workDateFrom = new DateField();
	    workDateFrom.setWidth(100);
	    workDateFrom.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    workDateFrom.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    workDateFrom.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
		workContainer.add(new FieldLabel(workDateFrom, "Дата от"), new VerticalLayoutData(1, -1));
		
		workDateTo = new DateField();
		workDateTo.setWidth(100);
		workDateTo.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    workDateTo.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    workDateTo.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
		workContainer.add(new FieldLabel(workDateTo, "Дата до"), new VerticalLayoutData(1, -1));
		
		Radio radioCompletedYes = new Radio();
		radioCompletedYes.setBoxLabel("Да");
	    
	    Radio radioCompletedNo = new Radio();
	    radioCompletedNo.setBoxLabel("Нет");
	    
	    radioCompletedAll = new Radio();
	    radioCompletedAll.setBoxLabel("Все");
	    radioCompletedAll.setValue(true);
	    radioCompletedAll.setHeight(18);
	 
	    HorizontalPanel hp = new HorizontalPanel();
	    hp.setSpacing(2);
	    hp.add(radioCompletedYes);
	    hp.add(radioCompletedNo);
	    hp.add(radioCompletedAll);
	 
	    workContainer.add(new FieldLabel(hp, "Выполнено"), new VerticalLayoutData(1, -1));
	 
	    completenessToggle = new ToggleGroup();
	    completenessToggle.add(radioCompletedYes);
	    completenessToggle.add(radioCompletedNo);
	    completenessToggle.add(radioCompletedAll);
	    completenessToggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
	      public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
	        applyFilters();
	      }
	    });
	    completenessToggle.setValue(radioCompletedAll);
	    
		
		HBoxLayoutContainer fieldsetsContainer = new HBoxLayoutContainer();
		fieldsetsContainer.setPadding(new Padding(0));
		fieldsetsContainer.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
		
		fieldsetsContainer.add(clientFieldSet, new BoxLayoutData(new Margins(0, 5, 0, 0)));
		fieldsetsContainer.add(workFieldSet, new BoxLayoutData(new Margins(0, 5, 0, 0)));
		
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
		 
		fieldsetsContainer.add(fltBtnsContainer, new BoxLayoutData(new Margins(5, 5, 0, 0)));
		
		filtersPanel.add(fieldsetsContainer);

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
				TextField field = (TextField) titleFilter.getMenu().getWidget(0);
				titleFilter.setActive(false, false);
				field.setValue(getFilterValue(clientTitle.getText()), true);
				titleFilter.setActive(true, false);
				
				field = (TextField) bossFilter.getMenu().getWidget(0);
				bossFilter.setActive(false, false);
				field.setValue(getFilterValue(clientBossName.getText()), true);
				bossFilter.setActive(true, false);
				
				field = (TextField) innFilter.getMenu().getWidget(0);
				innFilter.setActive(false, false);
				field.setValue(getFilterValue(clientINN.getText()), true);
				innFilter.setActive(true, false);
				
				field = (TextField) addressFilter.getMenu().getWidget(0);
				addressFilter.setActive(false, false);
				field.setValue(getFilterValue(clientAddress.getText()), true);
				addressFilter.setActive(true, false);
				
				field = (TextField) contractFilter.getMenu().getWidget(0);
				contractFilter.setActive(false, false);
				field.setValue(getFilterValue(contractNum.getText()), true);
				contractFilter.setActive(true, false);
				
				field = (TextField) objTypeFilter.getMenu().getWidget(0);
				objTypeFilter.setActive(false, false);
				if (objectType.getCurrentValue() != null) {
					field.setValue(getFilterValue(objectType.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				objTypeFilter.setActive(true, false);
				
				workDateFilter.setActive(false, false);
//				Конечная дата интервала
				CheckMenuItem before = (CheckMenuItem) workDateFilter.getMenu().getWidget(0);
				DatePicker beforePicker = (DatePicker) before.getSubMenu().getWidget(0);
				if (workDateTo.getValue() != null)
					beforePicker.setValue(workDateTo.getValue());
				else
					before.setChecked(false, false);
//				Начальная дата интервала
				CheckMenuItem after = (CheckMenuItem) workDateFilter.getMenu().getWidget(1);
				DatePicker afterPicker = (DatePicker) after.getSubMenu().getWidget(0);
				if (workDateFrom.getValue() != null)
					afterPicker.setValue(workDateFrom.getValue());
				else
					after.setChecked(false, false);
//				Точная дата
				CheckMenuItem on = (CheckMenuItem) workDateFilter.getMenu().getWidget(3);
				DatePicker onPicker = (DatePicker) on.getSubMenu().getWidget(0);
				if (workDate.getValue() != null)
					onPicker.setValue(workDate.getValue());
				else
					on.setChecked(false, false);				
				workDateFilter.setActive(true, false);
				
				Radio radio = (Radio)completenessToggle.getValue();
				if (radio.getBoxLabel().equals("Все")) {
					completedFilter.setActive(false, false);
				} else {
					field = (TextField) completedFilter.getMenu().getWidget(0);
					completedFilter.setActive(false, false);
					field.setValue(radio.getBoxLabel(), true);
					completedFilter.setActive(true, false);
				}
				
			} catch (Exception e) {
				System.out.println("applyFilters got errored: "+e.getMessage()+" "+ e.toString());
			}
    	}
	}
	
	private void resetFilters() {
		clientTitle.setValue("");
		clientBossName.setValue("");
		clientINN.setValue("");;
		clientAddress.setValue("");
		contractNum.setValue("");
		objectType.setValue(null);
		workDate.setValue(null);
		workDateFrom.setValue(null);
		workDateTo.setValue(null);
		completenessToggle.setValue(radioCompletedAll);
		applyFilters();
	}

	private Widget getJournalGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<ActsJournalRecord>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<ActsJournalRecord>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<ActsJournalRecord>> callback) {
				journalService.getJournalRecords(loadConfig, callback);
			}
		};

		ActsJournalRecordProps props = GWT.create(ActsJournalRecordProps.class);

		ListStore<ActsJournalRecord> store = new ListStore<ActsJournalRecord>(new ModelKeyProvider<ActsJournalRecord>() {
					@Override
					public String getKey(ActsJournalRecord item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ActsJournalRecord>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, ActsJournalRecord, PagingLoadResult<ActsJournalRecord>>(store));
		
		ColumnConfig<ActsJournalRecord, String> workNumColumn = new ColumnConfig<ActsJournalRecord, String>(props.workNum(), 10, SafeHtmlUtils.fromTrustedString("Номер акта,<br>экспертизы"));
		ColumnConfig<ActsJournalRecord, String> contractNumColumn = new ColumnConfig<ActsJournalRecord, String>(props.contractNum(), 10, SafeHtmlUtils.fromTrustedString("Номер<br>договора"));
		ColumnConfig<ActsJournalRecord, Date> contractDateColumn = new ColumnConfig<ActsJournalRecord, Date>(props.contractDate(), 10, SafeHtmlUtils.fromTrustedString("Дата заключения<br>договора"));
		contractDateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<ActsJournalRecord, Date> workDateColumn = new ColumnConfig<ActsJournalRecord, Date>(props.workDate(), 10, SafeHtmlUtils.fromTrustedString("Дата проведения<br>экспертизы"));
		workDateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<ActsJournalRecord, String> clientNameColumn = new ColumnConfig<ActsJournalRecord, String>(props.clientName(), 30, SafeHtmlUtils.fromTrustedString("Наименование контрагента<br>&nbsp;"));
		ColumnConfig<ActsJournalRecord, String> workSubjColumn = new ColumnConfig<ActsJournalRecord, String>(props.workSubj(), 30, SafeHtmlUtils.fromTrustedString("Предмет договора<br>&nbsp;"));
		ColumnConfig<ActsJournalRecord, String> objTypeColumn = new ColumnConfig<ActsJournalRecord, String>(props.objType(), 20, SafeHtmlUtils.fromTrustedString("Тип объекта<br>&nbsp;"));
		ColumnConfig<ActsJournalRecord, String> objNameColumn = new ColumnConfig<ActsJournalRecord, String>(props.objName(), 15, SafeHtmlUtils.fromTrustedString("Марка объекта<br>&nbsp;"));
		ColumnConfig<ActsJournalRecord, String> objFNumColumn = new ColumnConfig<ActsJournalRecord, String>(props.objFNum(), 10, SafeHtmlUtils.fromTrustedString("Зав. №<br>&nbsp;"));
		ColumnConfig<ActsJournalRecord, String> objRNumColumn = new ColumnConfig<ActsJournalRecord, String>(props.objRNum(), 10, SafeHtmlUtils.fromTrustedString("Рег. №<br>&nbsp;"));
		ColumnConfig<ActsJournalRecord, Date> nextWorkDateColumn = new ColumnConfig<ActsJournalRecord, Date>(props.nextWorkDate(), 10, SafeHtmlUtils.fromTrustedString("Дата повторной<br>экспертизы"));
		nextWorkDateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<ActsJournalRecord, Integer> daysLeftColumn = new ColumnConfig<ActsJournalRecord, Integer>(props.daysLeft(), 10, SafeHtmlUtils.fromTrustedString("Дней до повт.<br>экспертизы"));

		ColumnConfig<ActsJournalRecord, String> completedColumn = new ColumnConfig<ActsJournalRecord, String>(props.completed(), 5, SafeHtmlUtils.fromTrustedString("Выполнено<br>&nbsp;"));
		
		
		List<ColumnConfig<ActsJournalRecord, ?>> l = new ArrayList<ColumnConfig<ActsJournalRecord, ?>>();
		l.add(workNumColumn);
		l.add(contractNumColumn);
		l.add(contractDateColumn);
		l.add(workDateColumn);
		l.add(clientNameColumn);
		l.add(workSubjColumn);
		l.add(objTypeColumn);
		l.add(objNameColumn);
		l.add(objFNumColumn);
		l.add(objRNumColumn);
		l.add(nextWorkDateColumn);
		l.add(daysLeftColumn);
		l.add(completedColumn);
	
		ColumnModel<ActsJournalRecord> cm = new ColumnModel<ActsJournalRecord>(l);

		final Grid<ActsJournalRecord> journalGrid = new Grid<ActsJournalRecord>(store, cm) {
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
		
		daysLeftColumn.setCell(new AbstractCell<Integer>() {
	        @Override
	        public void render(Context context, Integer value, SafeHtmlBuilder sb) {
	          ActsJournalRecord rec = journalGrid.getStore().get(context.getIndex());
	          String style = "style='color: " + ((value < 0 && !rec.isDone()) ? "red" : "black") + "'";
	          sb.appendHtmlConstant("<span " + style + " >" + String.valueOf(value) + "</span>");
	        }
	      });

		journalGrid.setLoadMask(true);
		journalGrid.setLoader(gridLoader);
		journalGrid.getView().setForceFit(true);
		journalGrid.getView().setAutoExpandColumn(clientNameColumn);
		journalGrid.getView().setStripeRows(true);
		journalGrid.getView().setColumnLines(true);
		journalGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		journalGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				ActsJournalRecord rec = journalGrid.getSelectionModel().getSelectedItem();
				journRecWindow.displayRecord(rec, group.isAllowed(Constants.ACCESS_UPDATE, "acts"), group.isAllowed(Constants.ACCESS_PRINT, "acts"));
			}
		});
		
		GridFilters<ActsJournalRecord> filters = new GridFilters<ActsJournalRecord>(gridLoader);
		filters.initPlugin(journalGrid);
		
		titleFilter = new StringFilter<ActsJournalRecord>(props.clientName());
		filters.addFilter(titleFilter);
		bossFilter = new StringFilter<ActsJournalRecord>(props.clientBoss());
		filters.addFilter(bossFilter);
		innFilter = new StringFilter<ActsJournalRecord>(props.clientINN());
		filters.addFilter(innFilter);
		addressFilter = new StringFilter<ActsJournalRecord>(props.clientAddress());
		filters.addFilter(addressFilter);
		contractFilter = new StringFilter<ActsJournalRecord>(props.contractNum());
		filters.addFilter(contractFilter);
		objTypeFilter = new StringFilter<ActsJournalRecord>(props.objType());
		filters.addFilter(objTypeFilter);
		workDateFilter = new DateFilter<ActsJournalRecord>(props.workDate());
		filters.addFilter(workDateFilter);
		completedFilter = new StringFilter<ActsJournalRecord>(props.completed());
		completedFilter.setActive(false, false);
		filters.addFilter(completedFilter);
		

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
	        	  Grid<ActsJournalRecord> grid = ((Component) journGrid).getData("grid");
	        	  ActsJournalRecord rec = grid.getSelectionModel().getSelectedItem();
	        	  // если нет полномочий подтверждать удаление - пользователь может только помечать объекты на удаление
	        	  boolean markOnly = !group.isDeleteConfirmer();
	        	  journalService.deleteJournalRecord(rec.getId(), markOnly, new AsyncCallback<Boolean>(){
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить запись из журнала.");
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
	    
		FlowLayoutContainer flc = new FlowLayoutContainer();
		
		if (group.isAllowed(Constants.ACCESS_READ, "dictionaries")) {
			TextButton viewClientBtn = new TextButton("Просмотр контрагента");
			viewClientBtn.setIcon(IMAGES.view());
			flc.add(viewClientBtn, new MarginData(new Margins(0, 0, -5, 5)));
			
			viewClientBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<ActsJournalRecord> grid = ((Component) journGrid).getData("grid");
					ActsJournalRecord rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание", "Прежде чем воспользоваться просмотром данных о контрагенте, пожалуйста, выберите запись в журнале экспертиз.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					
					clientService.getClientInfoByJournId(rec.getId(), new AsyncCallback<Client>() {
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
		
		TextButton viewJournRecBtn = new TextButton("Просмотреть запись");
		viewJournRecBtn.setIcon(IMAGES.view());
		flc.add(viewJournRecBtn, new MarginData(new Margins(0, 0, -5, 5)));
		
		viewJournRecBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<ActsJournalRecord> grid = ((Component) journGrid).getData("grid");
				ActsJournalRecord rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				journRecWindow.displayRecord(rec, group.isAllowed(Constants.ACCESS_UPDATE, "acts"), group.isAllowed(Constants.ACCESS_PRINT, "acts"));
			}
		});
		
		if (group.isAllowed(Constants.ACCESS_INSERT, "acts")) {
			TextButton addJournRecBtn = new TextButton("Добавить запись");
			addJournRecBtn.setIcon(IMAGES.addRow());
			flc.add(addJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));
			
			addJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
//					Для того, чтобы можно было закачивать вложения сразу при создании акта, создадим запись в базе - её нужно будет удалить при отмене
					final ActsJournalRecord tmpRec = new ActsJournalRecord();
					journalService.addJournalRecord(tmpRec, new AsyncCallback<Integer>() {
						@Override
						public void onSuccess(Integer result) {
							tmpRec.setId(result);
							if (journWndHideHandleReg == null)
								journWndHideHandleReg = journRecWindow.addHideHandler(editHideHandler);
							journRecWindow.editRecord(tmpRec, true, group.isAllowed(Constants.ACCESS_PRINT, "acts"));
						}
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новой записи.");
							d.show();
						}
					});
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_UPDATE, "acts")) {
			TextButton editJournRecBtn = new TextButton("Редактировать запись");
			editJournRecBtn.setIcon(IMAGES.editRow());
			flc.add(editJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));		
			editJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<ActsJournalRecord> grid = ((Component) journGrid).getData("grid");
					ActsJournalRecord rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите отредактировать.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					if (journWndHideHandleReg == null)
						journWndHideHandleReg = journRecWindow.addHideHandler(editHideHandler);
					journRecWindow.editRecord(rec, false, group.isAllowed(Constants.ACCESS_PRINT, "acts"));
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_DELETE, "acts")) {
			TextButton deleteJournRecBtn = new TextButton("Удалить запись");
			deleteJournRecBtn.setIcon(IMAGES.deleteRow());
			flc.add(deleteJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));  
			
			deleteJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<ActsJournalRecord> grid = ((Component) journGrid).getData("grid");
					ActsJournalRecord rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите удалить.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранную запись?");
					box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
					box.setIcon(IMAGES.warning());
					box.addDialogHideHandler(deleteHideHandler);
					box.show();
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_PRINT, "acts")) {
			TextButton printJournBtn = new TextButton("Печать");
			printJournBtn.setIcon(IMAGES.print());
			flc.add(printJournBtn, new MarginData(new Margins(0, 0, -5, 0)));
		
			printJournBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<ActsJournalRecord> grid = ((Component) journGrid).getData("grid");
					FilterPagingLoadConfig config = (FilterPagingLoadConfig) grid.getLoader().getLastLoadConfig();
					journalService.getPrintableJournal(config, new AsyncCallback<String>() {
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
