package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.client.model.Guide;
import ru.techstandard.client.model.GuideProps;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.GuideService;
import ru.techstandard.shared.GuideServiceAsync;

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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
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
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class GuidesJournalPanel extends VBoxLayoutContainer {
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final EmployeeServiceAsync emplooyeeService = GWT.create(EmployeeService.class);
	private final GuideServiceAsync guideService = GWT.create(GuideService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Guide>> gridLoader = null;
	
	TextField objectName;
	ComboBox<DictionaryRecord> objectType;
	TextField clientName;
	TextField contractNum;
	TextField actNum;
	ComboBox<Employee> responsible;
	
	StringFilter<Guide> objNameFilter;
	StringFilter<Guide> objTypeFilter;
	StringFilter<Guide> clientNameFilter;
	StringFilter<Guide> contractNumFilter;
	StringFilter<Guide> actNumFilter;
	StringFilter<Guide> responsibleFilter;
	
	GuideInfoWindow guideInfoWindow;
	
	Widget guideGrid;
	AccessGroup group;
	int loggedUserId;
	
	public GuidesJournalPanel(int userId, AccessGroup accessGroup) {
		super();
		loggedUserId = userId;
		group = accessGroup;
		guideInfoWindow = new GuideInfoWindow();
	    final HideHandler editHideHandler = new HideHandler() {
	        @Override
	        public void onHide(HideEvent event) {
	          Window wnd = (Window) event.getSource();
	          if (wnd.getData("hideButton") == null)
	        	  return;
	          if (wnd.getData("hideButton").equals("save")) {
	        	  PagingToolBar toolbar = ((Component) guideGrid).getData("pagerToolbar");
	        	  toolbar.refresh();
	          }
	        }
	    };	
		guideInfoWindow.addHideHandler(editHideHandler);
		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
			 
		VerticalLayoutContainer filterContainerA = new VerticalLayoutContainer();
		filterContainerA.setWidth(400);
		 
		int labelColWidth = 150;
		
		objectName = new TextField();
		FieldLabel objNameLabel = new FieldLabel(objectName, "Наименование объекта");
		objNameLabel.setLabelWidth(labelColWidth);
		objectName.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(objNameLabel, new VerticalLayoutData(1, -1));

		
	    DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(props.key());
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_OBJTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Guides:154 - "+caught.getMessage());
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
	    FieldLabel objTypeLabel = new FieldLabel(objectType, "Тип объекта");
		objTypeLabel.setLabelWidth(labelColWidth);
	    filterContainerA.add(objTypeLabel, new VerticalLayoutData(1, -1));
	    
	    actNum = new TextField();
		FieldLabel actNumLabel = new FieldLabel(actNum, "Номер экспертизы");
		actNumLabel.setLabelWidth(labelColWidth);
		actNum.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(actNumLabel, new VerticalLayoutData(1, -1));
	    
	    VerticalLayoutContainer filterContainerB = new VerticalLayoutContainer();
	    filterContainerB.setWidth(400);
	    
	    clientName = new TextField();
		FieldLabel clientNameLabel = new FieldLabel(clientName, "Заказчик");
		clientNameLabel.setLabelWidth(labelColWidth);
		clientName.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerB.add(clientNameLabel, new VerticalLayoutData(1, -1));
		
		contractNum = new TextField();
		FieldLabel contractNumLabel = new FieldLabel(contractNum, "Номер договора");
		contractNumLabel.setLabelWidth(labelColWidth);
		contractNum.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerB.add(contractNumLabel, new VerticalLayoutData(1, -1));
		
		EmployeeProps emplProps = GWT.create(EmployeeProps.class);
		final ListStore<Employee> emplStore = new ListStore<Employee>(emplProps.id());

		emplooyeeService.getAllEmployees(
				new AsyncCallback<List<Employee>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Guides:212 - "+caught.getMessage());
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
		respLabel.setLabelWidth(labelColWidth);
		filterContainerB.add(respLabel, new VerticalLayoutData(1, -1));
		
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

        guideGrid = getJournalGrid(); 
        journalPanel.setWidget(guideGrid);
		 
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
				TextField field = (TextField) objNameFilter.getMenu().getWidget(0);
				objNameFilter.setActive(false, false);
				field.setValue(getFilterValue(objectName.getText()), true);
				objNameFilter.setActive(true, false);
				
				field = (TextField) objTypeFilter.getMenu().getWidget(0);
				objTypeFilter.setActive(false, false);
				if (objectType.getCurrentValue() != null) {
					field.setValue(getFilterValue(objectType.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				objTypeFilter.setActive(true, false);
				
				field = (TextField) clientNameFilter.getMenu().getWidget(0);
				clientNameFilter.setActive(false, false);
				field.setValue(getFilterValue(clientName.getText()), true);
				clientNameFilter.setActive(true, false);
				
				field = (TextField) contractNumFilter.getMenu().getWidget(0);
				contractNumFilter.setActive(false, false);
				field.setValue(getFilterValue(contractNum.getText()), true);
				contractNumFilter.setActive(true, false);
				
				field = (TextField) actNumFilter.getMenu().getWidget(0);
				actNumFilter.setActive(false, false);
				field.setValue(getFilterValue(actNum.getText()), true);
				actNumFilter.setActive(true, false);
				
				field = (TextField) responsibleFilter.getMenu().getWidget(0);
				responsibleFilter.setActive(false, false);
				if (responsible.getCurrentValue() != null) {
					field.setValue(getFilterValue(responsible.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				responsibleFilter.setActive(true, false);
				
			} catch (Exception e) {
				System.out.println("applyFilters got errored: "+e.getMessage()+" "+ e.toString());
			}
    	}
	}
	
	private void resetFilters() {
		objectName.setValue("");
		objectType.setValue(null);
		clientName.setValue("");
		contractNum.setValue("");
		actNum.setValue("");
		responsible.setValue(null);
		applyFilters();
	}

	private Widget getJournalGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Guide>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Guide>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Guide>> callback) {
				guideService.getGuides(loadConfig, callback);
			}
		};

		GuideProps props = GWT.create(GuideProps.class);

		ListStore<Guide> store = new ListStore<Guide>(new ModelKeyProvider<Guide>() {
					@Override
					public String getKey(Guide item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Guide>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Guide, PagingLoadResult<Guide>>(store));

		ColumnConfig<Guide, String> objNameColumn = new ColumnConfig<Guide, String>(props.objName(), 20, SafeHtmlUtils.fromTrustedString("Наименование объекта"));
		ColumnConfig<Guide, String> objTypeNameColumn = new ColumnConfig<Guide, String>(props.objTypeName(), 10, SafeHtmlUtils.fromTrustedString("Тип объекта"));
		ColumnConfig<Guide, String> fNumColumn = new ColumnConfig<Guide, String>(props.fNum(), 10, SafeHtmlUtils.fromTrustedString("Заводской номер"));
		ColumnConfig<Guide, String> rNumColumn = new ColumnConfig<Guide, String>(props.rNum(), 10, SafeHtmlUtils.fromTrustedString("Регистрационный номер"));
		ColumnConfig<Guide, String> clientColumn = new ColumnConfig<Guide, String>(props.clientName(), 20, SafeHtmlUtils.fromTrustedString("Заказчик"));
		ColumnConfig<Guide, String> contractColumn = new ColumnConfig<Guide, String>(props.contractNum(), 5, SafeHtmlUtils.fromTrustedString("№ договора"));
		ColumnConfig<Guide, String> actColumn = new ColumnConfig<Guide, String>(props.actNum(), 5, SafeHtmlUtils.fromTrustedString("№ экспертизы"));
		ColumnConfig<Guide, String> responsibleColumn = new ColumnConfig<Guide, String>(props.responsibleName(), 10, SafeHtmlUtils.fromTrustedString("Исполнитель"));
		ColumnConfig<Guide, Date> dueDateColumn = new ColumnConfig<Guide, Date>(props.dueDate(), 5, SafeHtmlUtils.fromTrustedString("Срок исполнения"));
		dueDateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		
		List<ColumnConfig<Guide, ?>> l = new ArrayList<ColumnConfig<Guide, ?>>();
		l.add(objNameColumn);
		l.add(objTypeNameColumn);
		l.add(fNumColumn);
		l.add(rNumColumn);
		l.add(clientColumn);
		l.add(contractColumn);
		l.add(actColumn);
		l.add(responsibleColumn);
		l.add(dueDateColumn);
	
		ColumnModel<Guide> cm = new ColumnModel<Guide>(l);

		final Grid<Guide> journalGrid = new Grid<Guide>(store, cm) {
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
		journalGrid.getView().setViewConfig(new GridViewConfig<Guide>() {
			@Override
			public String getColStyle(Guide req, ValueProvider<? super Guide, ?> valueProvider, int rowIndex, int colIndex) {
				int responsibleId = req.getResponsibleId();
				String style = null;
				if (loggedUserId == responsibleId)
					style = "yellow";
				else if (rowIndex % 2 == 1)
					style = "darkened";
				return style;
			}
			@Override
			public String getRowStyle(Guide model, int rowIndex) {return null;}
		});

		journalGrid.setLoadMask(true);
		journalGrid.setLoader(gridLoader);
		journalGrid.getView().setForceFit(true);
		journalGrid.getView().setAutoExpandColumn(objNameColumn);
		journalGrid.getView().setColumnLines(true);
		journalGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		journalGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Guide rec = journalGrid.getSelectionModel().getSelectedItem();
				guideInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "guides"), group.isAllowed(Constants.ACCESS_PRINT, "guides"));
			}
		});
		
		GridFilters<Guide> filters = new GridFilters<Guide>(gridLoader);
		filters.initPlugin(journalGrid);
		
		objNameFilter = new StringFilter<Guide>(props.objName());
		filters.addFilter(objNameFilter);
		objTypeFilter = new StringFilter<Guide>(props.objTypeName());
		filters.addFilter(objTypeFilter);
		clientNameFilter = new StringFilter<Guide>(props.clientName());
		filters.addFilter(clientNameFilter);
		contractNumFilter = new StringFilter<Guide>(props.contractNum());
		filters.addFilter(contractNumFilter);
		actNumFilter = new StringFilter<Guide>(props.actNum());
		filters.addFilter(actNumFilter);
		responsibleFilter = new StringFilter<Guide>(props.responsibleName());
		filters.addFilter(responsibleFilter);

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
		
		TextButton viewJournRecBtn = new TextButton("Просмотреть запись");
		viewJournRecBtn.setIcon(IMAGES.view());
		flc.add(viewJournRecBtn, new MarginData(new Margins(0, 0, -5, 5)));
		viewJournRecBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Guide> grid = ((Component) guideGrid).getData("grid");
				Guide rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание", "Пожалуйста, выберите запись, которую Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				
				guideInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "guides"), group.isAllowed(Constants.ACCESS_PRINT, "guides"));
			}
		});
		
		if (group.isAllowed(Constants.ACCESS_INSERT, "guides")) {
			TextButton addJournRecBtn = new TextButton("Добавить запись");
			addJournRecBtn.setIcon(IMAGES.addRow());
			flc.add(addJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));
			addJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					final Guide tmpGuide = new Guide(0);
					guideService.addGuide(tmpGuide, new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом документе.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							tmpGuide.setId(result);
							guideInfoWindow.editInfo(tmpGuide, true, group.isAllowed(Constants.ACCESS_PRINT, "guides"));
						}
					});
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_UPDATE, "guides")) {
			TextButton editJournRecBtn = new TextButton("Редактировать запись");
			editJournRecBtn.setIcon(IMAGES.editRow());
			flc.add(editJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));
			editJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Guide> grid = ((Component) guideGrid).getData("grid");
					Guide rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите отредактировать.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					guideInfoWindow.editInfo(rec, false, group.isAllowed(Constants.ACCESS_PRINT, "guides"));
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_DELETE, "guides")) {
			final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
				@Override
				public void onDialogHide(DialogHideEvent event) {
					if (event.getHideButton() == PredefinedButton.YES) {
						Grid<Guide> grid = ((Component) guideGrid).getData("grid");
						Guide rec = grid.getSelectionModel().getSelectedItem();	        	  
						guideService.deleteGuide(rec.getId(), !group.isDeleteConfirmer(), new AsyncCallback<Boolean>(){
							@Override
							public void onFailure(Throwable caught) {
								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить запись из журнала.");
								d.show();
							}
							@Override
							public void onSuccess(Boolean result) {
								PagingToolBar toolbar = ((Component) guideGrid).getData("pagerToolbar");
								toolbar.refresh();
							}
						});
					}
				}
			};

			TextButton deleteJournRecBtn = new TextButton("Удалить запись");
			deleteJournRecBtn.setIcon(IMAGES.deleteRow());
			flc.add(deleteJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));
			deleteJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Guide> grid = ((Component) guideGrid).getData("grid");
					Guide rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите удалить.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранную запись?");
					box.setIcon(IMAGES.warning());
					box.addDialogHideHandler(deleteHideHandler);
					box.show();
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_PRINT, "guides")) {
			TextButton printJournBtn = new TextButton("Печать");
			printJournBtn.setIcon(IMAGES.print());
			flc.add(printJournBtn, new MarginData(new Margins(0, 0, -5, 0)));
			printJournBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Guide> grid = ((Component) guideGrid).getData("grid");
					FilterPagingLoadConfig config = (FilterPagingLoadConfig) grid.getLoader().getLastLoadConfig();
					guideService.getPrintableGuideList(config, new AsyncCallback<String>() {
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
