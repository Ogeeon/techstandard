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
import ru.techstandard.client.model.Evaluation;
import ru.techstandard.client.model.EvaluationProps;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.EvaluationService;
import ru.techstandard.shared.EvaluationServiceAsync;

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
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;  

public class EvaluationsPanel extends VBoxLayoutContainer {
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final EmployeeServiceAsync emplooyeeService = GWT.create(EmployeeService.class);
	private final EvaluationServiceAsync evaluationService = GWT.create(EvaluationService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Evaluation>> gridLoader = null;
	
	ComboBox<Employee> employee;
	ComboBox<DictionaryRecord> evalField;
	
	StringFilter<Evaluation> employeeFilter;
	StringFilter<Evaluation> evalFieldFilter;
	
	EvaluationInfoWindow evalInfoWindow;
	
	Widget evalGrid;
	
	AccessGroup group;
	
	public EvaluationsPanel(AccessGroup accessGroup) {
		super();
		group = accessGroup;
		evalInfoWindow = new EvaluationInfoWindow();
	    final HideHandler editHideHandler = new HideHandler() {
	        @Override
	        public void onHide(HideEvent event) {
	          Window wnd = (Window) event.getSource();
	          if (wnd.getData("hideButton") == null)
	        	  return;
	          if (wnd.getData("hideButton").equals("save")) {
	        	  PagingToolBar toolbar = ((Component) evalGrid).getData("pagerToolbar");
	        	  toolbar.refresh();
	          }
	        }
	    };	
		evalInfoWindow.addHideHandler(editHideHandler);
		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
		filtersPanel.setPixelSize(-1, 70);
			 
		VerticalLayoutContainer filterContainerA = new VerticalLayoutContainer();
		filterContainerA.setWidth(400);
		 
	    DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(props.key());
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_EVAL_FIELDS,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Eval:136 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						store.addAll(result);
					}
				});
	 
	    evalField = new ComboBox<DictionaryRecord>(store, props.nameLabel());
	    evalField.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
	    	  applyFilters();
	      }
	    });
	    evalField.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
	    evalField.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				applyFilters();				
			}
		});
	    evalField.setAllowBlank(true);
	    evalField.setForceSelection(true);
	    evalField.setTriggerAction(TriggerAction.ALL);
	    FieldLabel evalFieldLabel = new FieldLabel(evalField, "Область аттестации");
		evalFieldLabel.setLabelWidth(120);
	    filterContainerA.add(evalFieldLabel, new VerticalLayoutData(1, -1));
	    
	    VerticalLayoutContainer filterContainerB = new VerticalLayoutContainer();
	    filterContainerB.setWidth(400);
		
		EmployeeProps emplProps = GWT.create(EmployeeProps.class);
		final ListStore<Employee> emplStore = new ListStore<Employee>(emplProps.id());

		emplooyeeService.getAllEmployees(
				new AsyncCallback<List<Employee>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Eval:176 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<Employee> result) {
						emplStore.addAll(result);
					}
				});

		employee = new ComboBox<Employee>(emplStore, emplProps.nameLabel());
		employee.addValueChangeHandler(new ValueChangeHandler<Employee>() {
			@Override
			public void onValueChange(ValueChangeEvent<Employee> event) {
				applyFilters();
			}
		});
		employee.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
				applyFilters();				
			}
		});
		employee.setAllowBlank(true);
		employee.setForceSelection(true);
		employee.setTriggerAction(TriggerAction.ALL);
		FieldLabel employeeLabel = new FieldLabel(employee, "Сотрудник");
		employeeLabel.setLabelWidth(80);
		filterContainerB.add(employeeLabel, new VerticalLayoutData(1, -1));
		
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

		
		ToolBar topToolBar = new ToolBar();
		topToolBar.add(getButtonsContainer());

		ContentPanel journalPanel = new ContentPanel();
		journalPanel.setHeadingText("Сведения об аттестации");
		journalPanel.setBorders(true);
		journalPanel.setBodyBorder(false);
		
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(1);

        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);

        evalGrid = getJournalGrid(); 
        journalPanel.setWidget(evalGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
        flex.setFlex(0);
        this.add(topToolBar, flex);
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
				TextField field = (TextField) employeeFilter.getMenu().getWidget(0);
				employeeFilter.setActive(false, false);
				if (employee.getCurrentValue() != null) {
					field.setValue(getFilterValue(employee.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				employeeFilter.setActive(true, false);
				
				field = (TextField) evalFieldFilter.getMenu().getWidget(0);
				evalFieldFilter.setActive(false, false);
				if (evalField.getCurrentValue() != null) {
					field.setValue(getFilterValue(evalField.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				evalFieldFilter.setActive(true, false);
				
			} catch (Exception e) {
				System.out.println("applyFilters got errored: "+e.getMessage()+" "+ e.toString());
			}
    	}
	}
	
	private void resetFilters() {
		evalField.setValue(null);
		employee.setValue(null);
		applyFilters();
	}

	private Widget getJournalGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Evaluation>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Evaluation>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Evaluation>> callback) {
				evaluationService.getEvaluations(loadConfig, callback);
			}
		};

		EvaluationProps props = GWT.create(EvaluationProps.class);

		ListStore<Evaluation> store = new ListStore<Evaluation>(new ModelKeyProvider<Evaluation>() {
					@Override
					public String getKey(Evaluation item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Evaluation>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Evaluation, PagingLoadResult<Evaluation>>(store));

		ColumnConfig<Evaluation, String> emplNameColumn = new ColumnConfig<Evaluation, String>(props.employeeName(), 20, SafeHtmlUtils.fromTrustedString("Сотрудник<br>&nbsp;"));
		ColumnConfig<Evaluation, String> positionColumn = new ColumnConfig<Evaluation, String>(props.position(), 10, SafeHtmlUtils.fromTrustedString("Должность<br>&nbsp;"));
		ColumnConfig<Evaluation, String> fieldNameColumn = new ColumnConfig<Evaluation, String>(props.fieldName(), 20, SafeHtmlUtils.fromTrustedString("Область аттестации<br>&nbsp;"));
		ColumnConfig<Evaluation, String> certNumColumn = new ColumnConfig<Evaluation, String>(props.certNum(), 10, SafeHtmlUtils.fromTrustedString("№ удостоверения<br>&nbsp;"));
		ColumnConfig<Evaluation, Date> lastEvalDateColumn = new ColumnConfig<Evaluation, Date>(props.lastEvalDate(), 5, SafeHtmlUtils.fromTrustedString("Дата последней<br>аттестации"));
		lastEvalDateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<Evaluation, Date> nextEvalDateColumn = new ColumnConfig<Evaluation, Date>(props.nextEvalDate(), 5, SafeHtmlUtils.fromTrustedString("Дата следующей<br>аттестации"));
		nextEvalDateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		
		List<ColumnConfig<Evaluation, ?>> l = new ArrayList<ColumnConfig<Evaluation, ?>>();
		l.add(emplNameColumn);
		l.add(positionColumn);
		l.add(fieldNameColumn);
		l.add(certNumColumn);
		l.add(lastEvalDateColumn);
		l.add(nextEvalDateColumn);
	
		ColumnModel<Evaluation> cm = new ColumnModel<Evaluation>(l);

		final Grid<Evaluation> journalGrid = new Grid<Evaluation>(store, cm) {
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

		journalGrid.setLoadMask(true);
		journalGrid.setLoader(gridLoader);
		journalGrid.getView().setForceFit(true);
		journalGrid.getView().setAutoExpandColumn(emplNameColumn);
		journalGrid.getView().setStripeRows(true);
		journalGrid.getView().setColumnLines(true);
		journalGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		journalGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Evaluation rec = journalGrid.getSelectionModel().getSelectedItem();
				evalInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "evaluations"), group.isAllowed(Constants.ACCESS_PRINT, "evaluations"));
			}
		});
		
		GridFilters<Evaluation> filters = new GridFilters<Evaluation>(gridLoader);
		filters.initPlugin(journalGrid);
		
		employeeFilter = new StringFilter<Evaluation>(props.employeeName());
		filters.addFilter(employeeFilter);
		evalFieldFilter = new StringFilter<Evaluation>(props.fieldName());
		filters.addFilter(evalFieldFilter);

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
				Grid<Evaluation> grid = ((Component) evalGrid).getData("grid");
				Evaluation rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание", "Пожалуйста, выберите запись, которую Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				
				evalInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "evaluations"), group.isAllowed(Constants.ACCESS_PRINT, "evaluations"));
			}
		});
		
		if (group.isAllowed(Constants.ACCESS_INSERT, "evaluations")) {
			TextButton addJournRecBtn = new TextButton("Добавить запись");
			addJournRecBtn.setIcon(IMAGES.addRow());
			flc.add(addJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));

			addJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					final Evaluation tmpEval = new Evaluation();
					evaluationService.addEvaluation(tmpEval, new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить новую запись.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							tmpEval.setId(result);
							evalInfoWindow.editInfo(tmpEval, true, group.isAllowed(Constants.ACCESS_PRINT, "evaluations"));
						}
					});
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_UPDATE, "evaluations")) {
			TextButton editJournRecBtn = new TextButton("Редактировать запись");
			editJournRecBtn.setIcon(IMAGES.editRow());
			flc.add(editJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));

			editJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Evaluation> grid = ((Component) evalGrid).getData("grid");
					Evaluation rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите отредактировать.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					evalInfoWindow.editInfo(rec, false, group.isAllowed(Constants.ACCESS_PRINT, "evaluations"));
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_DELETE, "evaluations")) {
			TextButton deleteJournRecBtn = new TextButton("Удалить запись");
			deleteJournRecBtn.setIcon(IMAGES.deleteRow());
			flc.add(deleteJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));

			final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
				@Override
				public void onDialogHide(DialogHideEvent event) {
					if (event.getHideButton() == PredefinedButton.YES) {
						Grid<Evaluation> grid = ((Component) evalGrid).getData("grid");
						Evaluation rec = grid.getSelectionModel().getSelectedItem();	        	  
						evaluationService.deleteEvaluation(rec.getId(), !group.isDeleteConfirmer(), new AsyncCallback<Boolean>(){
							@Override
							public void onFailure(Throwable caught) {
								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить запись.");
								d.show();
							}
							@Override
							public void onSuccess(Boolean result) {
								PagingToolBar toolbar = ((Component) evalGrid).getData("pagerToolbar");
								toolbar.refresh();
							}
						});
					}
				}
			};

			deleteJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Evaluation> grid = ((Component) evalGrid).getData("grid");
					Evaluation rec = grid.getSelectionModel().getSelectedItem();
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
		
		if (group.isAllowed(Constants.ACCESS_PRINT, "evaluations")) {
			TextButton printJournBtn = new TextButton("Печать");
			printJournBtn.setIcon(IMAGES.print());
			flc.add(printJournBtn, new MarginData(new Margins(0, 0, -5, 0)));

			printJournBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Evaluation> grid = ((Component) evalGrid).getData("grid");
					FilterPagingLoadConfig config = (FilterPagingLoadConfig) grid.getLoader().getLastLoadConfig();
					evaluationService.getPrintableEvaluationList(config, new AsyncCallback<String>() {
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
