package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Constants.Privilege;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.client.model.Task;
import ru.techstandard.client.model.TaskProps;
import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.TaskService;
import ru.techstandard.shared.TaskServiceAsync;

import com.google.gwt.cell.client.AbstractCell;
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
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class TasksPanel extends VBoxLayoutContainer {
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final TaskServiceAsync taskService = GWT.create(TaskService.class);
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Task>> gridLoader = null;
	
	ComboBox<Employee> executor;
	ComboBox<DictionaryRecord> taskType;
	Radio radioCompletedNo;
	ToggleGroup completenessToggle;
	
	DateField startDate;
	DateField startDateFrom;
	DateField startDateTo;
	
	DateField dueDate;
	DateField dueDateFrom;
	DateField dueDateTo;
	
	StringFilter<Task> executorFilter;
	StringFilter<Task> taskTypeFilter;
	StringFilter<Task> completedFilter;
	DateFilter<Task> startDateFilter;
	DateFilter<Task> dueDateFilter;
	
	TaskInfoWindow taskInfoWindow;
	FollowedTaskInfoWindow followedTaskInfoWindow;
	
	Widget taskGrid;
	UserDTO user;
	Privilege priv;
	
	public TasksPanel(UserDTO loggedUser) {
		super();
		user = loggedUser;
		if (user.getAccess().isTaskCreator())
			priv = Privilege.ADMIN;
		else
			priv = (user.isBoss() ? Privilege.BOSS : Privilege.USER);
		
		taskInfoWindow = new TaskInfoWindow();
	    final HideHandler editHideHandler = new HideHandler() {
	        @Override
	        public void onHide(HideEvent event) {
	          Window wnd = (Window) event.getSource();
	          if (wnd.getData("hideButton") == null)
	        	  return;
	          if (wnd.getData("hideButton").equals("save")) {
	        	  PagingToolBar toolbar = ((Component) taskGrid).getData("pagerToolbar");
	        	  toolbar.refresh();
	          }
	        }
	    };	
	    taskInfoWindow.addHideHandler(editHideHandler);
	    
	    followedTaskInfoWindow = new FollowedTaskInfoWindow();
	    followedTaskInfoWindow.addHideHandler(editHideHandler);
		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
			 
		VerticalLayoutContainer filterContainerA = new VerticalLayoutContainer();
		filterContainerA.setWidth(350);
	    VerticalLayoutContainer filterContainerB = new VerticalLayoutContainer();
	    filterContainerB.setWidth(220);
	    VerticalLayoutContainer filterContainerC = new VerticalLayoutContainer();
	    filterContainerC.setWidth(240);
		
		int labelColWidthA = 100;
		int labelColWidthB = 110;
		int labelColWidthC = 130;
		
		HBoxLayoutContainer filtersContainer = new HBoxLayoutContainer();
		filtersContainer.setPadding(new Padding(0));
		filtersContainer.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
		
		VerticalLayoutContainer fltBtnsContainer = new VerticalLayoutContainer();
		fltBtnsContainer.setPixelSize(100, 50);
	    
		DictionaryRecordProps taskTypeProps = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(taskTypeProps.key());
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_TASKTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Tasks:186 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						store.addAll(result);
					}
				});
	 
	    taskType = new ComboBox<DictionaryRecord>(store, taskTypeProps.nameLabel());
	    taskType.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
	    	  applyFilters();
	      }
	    });
	    taskType.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB) applyFilters(); }});
	    taskType.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				applyFilters();				
			}
		});
	    taskType.setAllowBlank(true);
	    taskType.setForceSelection(true);
	    taskType.setTriggerAction(TriggerAction.ALL);
	    FieldLabel taskTypeLabel = new FieldLabel(taskType, "Тип задания");
	    taskTypeLabel.setLabelWidth(labelColWidthA);
	    filterContainerA.add(taskTypeLabel, new VerticalLayoutData(1, -1));
	    
	    Radio radioCompletedYes = new Radio();
		radioCompletedYes.setBoxLabel("Да");
	    
	    radioCompletedNo = new Radio();
	    radioCompletedNo.setValue(true);
	    radioCompletedNo.setBoxLabel("Нет");
	    
	    Radio radioCompletedConfirm = new Radio();
	    radioCompletedConfirm.setBoxLabel("На утвержд.");
	    
	    Radio radioCompletedAll = new Radio();
	    radioCompletedAll.setBoxLabel("Все");
	    radioCompletedAll.setHeight(18);
	 
	    VerticalLayoutContainer statusContainer = new VerticalLayoutContainer();
	    HorizontalPanel hp = new HorizontalPanel();
	    hp.setSpacing(2);
	    hp.add(radioCompletedYes);
	    hp.add(radioCompletedNo);
	    hp.add(radioCompletedConfirm);
	    hp.add(radioCompletedAll);
	    statusContainer.add(hp, new VerticalLayoutData(1, -1));
	 
	    FieldLabel completedLabel = new FieldLabel(statusContainer, "Выполнено");
	    completedLabel.setLabelWidth(labelColWidthA);
	    filterContainerA.add(completedLabel, new VerticalLayoutData(1, -1));
	 
	    completenessToggle = new ToggleGroup();
	    completenessToggle.add(radioCompletedYes);
	    completenessToggle.add(radioCompletedNo);
	    completenessToggle.add(radioCompletedConfirm);
	    completenessToggle.add(radioCompletedAll);
	    completenessToggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
	      public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
	        applyFilters();
	      }
	    });

	    if (user.getAccess().isTaskApprover()) {
	    	Radio radioCompletedApprove = new Radio();
	    	radioCompletedApprove.setBoxLabel("На согласовании");
	    	completenessToggle.add(radioCompletedApprove);
	    	statusContainer.add(radioCompletedApprove, new VerticalLayoutData(1, -1, new Margins(0, 0, 0, 2)));
	    }
	    if (user.getAccess().isTaskCreator())
	    	completenessToggle.setValue(radioCompletedConfirm);
	    else 
	    	completenessToggle.setValue(radioCompletedNo);
	    
		EmployeeProps emplProps = GWT.create(EmployeeProps.class);
		final ListStore<Employee> emplStore = new ListStore<Employee>(emplProps.id());
		executor = new ComboBox<Employee>(emplStore, emplProps.nameLabel());

		if (priv == Privilege.ADMIN || priv == Privilege.BOSS) {
			employeeService.getSubordinates(loggedUser.getDepartmentId(), new AsyncCallback<List<Employee>>() {
				public void onFailure(Throwable caught) {
					AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Tasks:273 - "+caught.getMessage());
					d.show();
				}
				@Override
				public void onSuccess(List<Employee> result) {
					emplStore.addAll(result);
				}
			});
		} else { // это просто юзер
			employeeService.getEmployeeInfo(loggedUser.getEmployeeId(),
					new AsyncCallback<Employee>() {
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Tasks:285 - "+caught.getMessage());
							d.show();
						}
						@Override
						public void onSuccess(Employee result) {
							emplStore.add(result);
							executor.setValue(result);
						}
					});
		}
		
		executor.addValueChangeHandler(new ValueChangeHandler<Employee>() {
			@Override
			public void onValueChange(ValueChangeEvent<Employee> event) {
				applyFilters();
			}
		});
		executor.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
				applyFilters();				
			}
		});
		executor.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB) applyFilters(); }});
		executor.setAllowBlank(true);
		executor.setForceSelection(true);
		executor.setTriggerAction(TriggerAction.ALL);
		FieldLabel execLabel = new FieldLabel(executor, "Исполнитель");
		execLabel.setLabelWidth(labelColWidthA);
		if (priv != Privilege.USER)
			filterContainerA.add(execLabel, new VerticalLayoutData(1, -1));
	    
	    startDate = new DateField();
	    startDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    startDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    startDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    startDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
		});
	    FieldLabel startDateLabel = new FieldLabel(startDate, "Дата начала");
	    startDateLabel.setLabelWidth(labelColWidthB);
	    filterContainerB.add(startDateLabel, new VerticalLayoutData(1, -1));
		 
		startDateFrom = new DateField();
	    startDateFrom.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    startDateFrom.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    startDateFrom.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    startDateFrom.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
		});
	    FieldLabel startDateFromLabel = new FieldLabel(startDateFrom, "Дата начала от");
	    startDateFromLabel.setLabelWidth(labelColWidthB);
	    filterContainerB.add(startDateFromLabel, new VerticalLayoutData(1, -1));
		
		startDateTo = new DateField();
	    startDateTo.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    startDateTo.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    startDateTo.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    startDateTo.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
		});
	    FieldLabel startDateToLabel = new FieldLabel(startDateTo, "Дата начала до");
	    startDateToLabel.setLabelWidth(labelColWidthB);
	    filterContainerB.add(startDateToLabel, new VerticalLayoutData(1, -1));
		
	    dueDate = new DateField();
	    dueDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    dueDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    dueDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    dueDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
		});
	    FieldLabel dueDateLabel = new FieldLabel(dueDate, "Дата завершения");
	    dueDateLabel.setLabelWidth(labelColWidthC);
	    filterContainerC.add(dueDateLabel, new VerticalLayoutData(1, -1));
		 
		dueDateFrom = new DateField();
	    dueDateFrom.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    dueDateFrom.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    dueDateFrom.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    dueDateFrom.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
		});
	    FieldLabel dueDateFromLabel = new FieldLabel(dueDateFrom, "Дата завершения от");
	    dueDateFromLabel.setLabelWidth(labelColWidthC);
	    filterContainerC.add(dueDateFromLabel, new VerticalLayoutData(1, -1));
		
		dueDateTo = new DateField();
	    dueDateTo.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    dueDateTo.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    dueDateTo.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    dueDateTo.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
		});
	    FieldLabel dueDateToLabel = new FieldLabel(dueDateTo, "Дата завершения до");
	    dueDateToLabel.setLabelWidth(labelColWidthC);
	    filterContainerC.add(dueDateToLabel, new VerticalLayoutData(1, -1));
	    
		filtersContainer.add(filterContainerA, new BoxLayoutData(new Margins(0, 15, 0, 0)));
		filtersContainer.add(filterContainerB, new BoxLayoutData(new Margins(0, 15, 0, 0)));
		filtersContainer.add(filterContainerC, new BoxLayoutData(new Margins(0, 10, 0, 0)));
		
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
		
		ContentPanel tasksPanel = new ContentPanel();
		tasksPanel.setHeadingText("Задания");
		tasksPanel.setBorders(true);
		tasksPanel.setBodyBorder(false);
		
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(1);

        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);

        taskGrid = getTaskGrid(); 
        tasksPanel.setWidget(taskGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
        flex.setFlex(0);
        this.add(topToolBar, flex);
        this.add(filtersPanel, flex);

        flex2.setFlex(3);
        this.add(tasksPanel, flex2);
        
        this.forceLayout();
	}
	
	private String getFilterValue(String fieldValue) {
		String value = fieldValue.trim();
		return value.length() == 0 ? null : value;
	}
	
	private void applyFilters() {
		if (gridLoader != null) {
			try {				
				TextField field = (TextField) executorFilter.getMenu().getWidget(0);
				executorFilter.setActive(false, false);
				if (executor.getCurrentValue() != null) {
					field.setValue(getFilterValue(executor.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				executorFilter.setActive(true, false);
				
				field = (TextField) taskTypeFilter.getMenu().getWidget(0);
				taskTypeFilter.setActive(false, false);
				if (taskType.getCurrentValue() != null) {
					field.setValue(getFilterValue(taskType.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				taskTypeFilter.setActive(true, false);
				
				Radio radio = (Radio)completenessToggle.getValue();
				
				if (radio.getBoxLabel().equals("Все")) {
					completedFilter.setActive(false, false);
				} else {
					field = (TextField) completedFilter.getMenu().getWidget(0);
					completedFilter.setActive(false, false);
					field.setValue(radio.getBoxLabel(), true);
					completedFilter.setActive(true, false);
				}
				
				startDateFilter.setActive(false, false);
//				Конечная дата интервала
				CheckMenuItem before = (CheckMenuItem) startDateFilter.getMenu().getWidget(0);
				DatePicker beforePicker = (DatePicker) before.getSubMenu().getWidget(0);
				if (startDateTo.getValue() != null)
					beforePicker.setValue(startDateTo.getValue());
				else
					before.setChecked(false, false);
//				Начальная дата интервала
				CheckMenuItem after = (CheckMenuItem) startDateFilter.getMenu().getWidget(1);
				DatePicker afterPicker = (DatePicker) after.getSubMenu().getWidget(0);
				if (startDateFrom.getValue() != null)
					afterPicker.setValue(startDateFrom.getValue());
				else
					after.setChecked(false, false);
//				Точная дата
				CheckMenuItem on = (CheckMenuItem) startDateFilter.getMenu().getWidget(3);
				DatePicker onPicker = (DatePicker) on.getSubMenu().getWidget(0);
				if (startDate.getValue() != null)
					onPicker.setValue(startDate.getValue());
				else
					on.setChecked(false, false);				
				startDateFilter.setActive(true, false);
				
				dueDateFilter.setActive(false, false);
//				Конечная дата интервала
				before = (CheckMenuItem) dueDateFilter.getMenu().getWidget(0);
				beforePicker = (DatePicker) before.getSubMenu().getWidget(0);
				if (dueDateTo.getValue() != null)
					beforePicker.setValue(dueDateTo.getValue());
				else
					before.setChecked(false, false);
//				Начальная дата интервала
				after = (CheckMenuItem) dueDateFilter.getMenu().getWidget(1);
				afterPicker = (DatePicker) after.getSubMenu().getWidget(0);
				if (dueDateFrom.getValue() != null)
					afterPicker.setValue(dueDateFrom.getValue());
				else
					after.setChecked(false, false);
//				Точная дата
				on = (CheckMenuItem) dueDateFilter.getMenu().getWidget(3);
				onPicker = (DatePicker) on.getSubMenu().getWidget(0);
				if (dueDate.getValue() != null)
					onPicker.setValue(dueDate.getValue());
				else
					on.setChecked(false, false);				
				dueDateFilter.setActive(true, false);
				
			} catch (Exception e) {
				System.out.println("applyFilters got errored: "+e.getMessage()+" "+ e.toString());
			}
    	}
	}
	
	private void resetFilters() {
		if (priv != Privilege.USER)
			executor.setValue(null);
		taskType.setValue(null);
		completenessToggle.setValue(radioCompletedNo);

		startDate.setValue(null);
		startDateFrom.setValue(null);
		startDateTo.setValue(null);
		
		dueDate.setValue(null);
		dueDateFrom.setValue(null);
		dueDateTo.setValue(null);
		
		applyFilters();
	}

	private Widget getTaskGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Task>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Task>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Task>> callback) {
				// директорат в любом случае получит свой верхний уровень, остальные адекватно получат уровень своего подразделения и ниже
				if (priv == Privilege.ADMIN || priv == Privilege.BOSS)
					taskService.getPagedTasksByDepartment(user.getDepartmentId(), loadConfig, callback);
				else
					taskService.getPagedTasksByEmployee(user.getEmployeeId(), loadConfig, callback);
			}
		};

		TaskProps props = GWT.create(TaskProps.class);

		ListStore<Task> store = new ListStore<Task>(new ModelKeyProvider<Task>() {
					@Override
					public String getKey(Task item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Task>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Task, PagingLoadResult<Task>>(store));

		ColumnConfig<Task, String> creatorColumn = new ColumnConfig<Task, String>(props.creatorName(), 8, SafeHtmlUtils.fromTrustedString("Инициатор"));
		ColumnConfig<Task, String> executorColumn = new ColumnConfig<Task, String>(props.executorName(), 8, SafeHtmlUtils.fromTrustedString("Исполнитель"));
		if (priv == Privilege.USER)
			executorColumn.setHidden(true);
		ColumnConfig<Task, String> taskTypeColumn = new ColumnConfig<Task, String>(props.typeName(), 15, SafeHtmlUtils.fromTrustedString("Тип задания"));
		ColumnConfig<Task, String> descriptionColumn = new ColumnConfig<Task, String>(props.description(), 40, SafeHtmlUtils.fromTrustedString("Содержание задания"));
		ColumnConfig<Task, Date> startDateColumn = new ColumnConfig<Task, Date>(props.startDate(), 8, SafeHtmlUtils.fromTrustedString("Дата начала"));
		ColumnConfig<Task, Date> dueDateColumn = new ColumnConfig<Task, Date>(props.dueDate(), 8, SafeHtmlUtils.fromTrustedString("Срок исполнения"));
		ColumnConfig<Task, String> statusColumn = new ColumnConfig<Task, String>(props.completed(), 8, SafeHtmlUtils.fromTrustedString("Завершено"));
		ColumnConfig<Task, Date> completedDateColumn = new ColumnConfig<Task, Date>(props.dueDate(), 8, SafeHtmlUtils.fromTrustedString("Дата завершения"));
		ColumnConfig<Task, String> notesColumn = new ColumnConfig<Task, String>(props.notes(), 15, SafeHtmlUtils.fromTrustedString("Примечание"));
		
		List<ColumnConfig<Task, ?>> l = new ArrayList<ColumnConfig<Task, ?>>();
		l.add(creatorColumn);
		l.add(executorColumn);
		l.add(taskTypeColumn);
		l.add(descriptionColumn);
		l.add(startDateColumn);
		l.add(dueDateColumn);
		l.add(statusColumn);
		l.add(completedDateColumn);
		l.add(notesColumn);
	
		ColumnModel<Task> cm = new ColumnModel<Task>(l);

		final Grid<Task> tasksGrid = new Grid<Task>(store, cm) {
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

		AbstractCell<String> stringCell = new AbstractCell<String>() {
			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb) {
				Task rec = tasksGrid.getStore().get(context.getIndex());
				String style = "style='font-weight: " + ((rec.getExecutorId() == user.getEmployeeId() && !rec.isViewed()) ? "bold" : "normal") + "'";
				sb.appendHtmlConstant("<span " + style + " >" + value + "</span>");
			}
		};
		AbstractCell<Date> dateCell = new AbstractCell<Date>() {
			@Override
			public void render(Context context, Date value, SafeHtmlBuilder sb) {
				Task rec = tasksGrid.getStore().get(context.getIndex());
				DateTimeFormat fmt = DateTimeFormat.getFormat("dd.MM.yyyy");
				String style = "style='font-weight: " + ((rec.getExecutorId() == user.getEmployeeId() && !rec.isViewed()) ? "bold" : "normal") + "'";
				sb.appendHtmlConstant("<span " + style + " >" + fmt.format(value) + "</span>");
			}
		};
		
		creatorColumn.setCell(stringCell);
		executorColumn.setCell(stringCell);
		taskTypeColumn.setCell(stringCell);
		descriptionColumn.setCell(stringCell);
		startDateColumn.setCell(dateCell);
		dueDateColumn.setCell(dateCell);
		statusColumn.setCell(stringCell);
		completedDateColumn.setCell(dateCell);
		notesColumn.setCell(stringCell);

		tasksGrid.setLoadMask(true);
		tasksGrid.setLoader(gridLoader);
		tasksGrid.getView().setForceFit(true);
		tasksGrid.getView().setAutoExpandColumn(descriptionColumn);
		tasksGrid.getView().setStripeRows(true);
		tasksGrid.getView().setColumnLines(true);
		tasksGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
				
		tasksGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Task rec = tasksGrid.getSelectionModel().getSelectedItem();
				if (!rec.isViewed() && rec.getExecutorId() == user.getEmployeeId())
					setTaskViewed(rec.getId());
				taskInfoWindow.displayInfo(rec, user);
			}
		});
		
		GridFilters<Task> filters = new GridFilters<Task>(gridLoader);
		filters.initPlugin(tasksGrid);

		executorFilter = new StringFilter<Task>(props.executorName());
		if (priv == Privilege.USER) {
			TextField fld = (TextField) executorFilter.getMenu().getWidget(0);
			fld.setValue(getFilterValue(user.getName()), true);
		}
		filters.addFilter(executorFilter);
		executorFilter.setActive(true, false);
		
		taskTypeFilter = new StringFilter<Task>(props.typeName());
		filters.addFilter(taskTypeFilter);
		
		completedFilter = new StringFilter<Task>(props.completed());
		TextField field = (TextField) completedFilter.getMenu().getWidget(0);
		if (user.getAccess().isTaskCreator())
			field.setValue("На утвержд.", true);
		else
			field.setValue("Нет", true);
		completedFilter.setActive(true, false);
		
		filters.addFilter(completedFilter);
		
		startDateFilter = new DateFilter<Task>(props.startDate());
		filters.addFilter(startDateFilter);
		dueDateFilter = new DateFilter<Task>(props.dueDate());
		filters.addFilter(dueDateFilter);

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(false);
		con.add(tasksGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar toolBar = new PagingToolBar(30);
		toolBar.addStyleName(ThemeStyles.get().style().borderTop());
		toolBar.getElement().getStyle().setProperty("borderBottom", "none");
		toolBar.bind(gridLoader);

		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.setData("grid", tasksGrid);

		con.setData("pagerToolbar", toolBar);
		return con;
	}
	
	public FlowLayoutContainer getButtonsContainer() {
		FlowLayoutContainer flc = new FlowLayoutContainer();
		
		TextButton viewTaskBtn = new TextButton("Просмотреть задание");
		viewTaskBtn.setIcon(IMAGES.view());
		flc.add(viewTaskBtn, new MarginData(new Margins(0, 0, -5, 5)));
		
		viewTaskBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Task> grid = ((Component) taskGrid).getData("grid");
				Task rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание", "Пожалуйста, выберите задание, которое Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				if (!rec.isViewed() && rec.getExecutorId() == user.getEmployeeId())
					setTaskViewed(rec.getId());
				taskInfoWindow.displayInfo(rec, user);
			}
		});
		// здесь имеет значение только наличие права создавать задания, поэтому нет " || priv == Privilege.BOSS"
		if (priv == Privilege.ADMIN) {
			TextButton addTaskBtn = new TextButton("Добавить задание");
			addTaskBtn.setIcon(IMAGES.addRow());
			flc.add(addTaskBtn, new MarginData(new Margins(0, 0, -5, 0)));

			addTaskBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					final Task tmpTask = new Task(0, user.getEmployeeId());
					tmpTask.setStatus(user.getAccess().isNeedApproval() ? -2 : 0);
					taskService.addTask(tmpTask, new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом задании.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							tmpTask.setId(result);
							taskInfoWindow.editInfo(tmpTask, true, user);
						}
					});
				}
			});
		}
		
		if (priv == Privilege.ADMIN) {
			TextButton addTaskBtn = new TextButton("Добавить задание закрытия с контрагентом");
			addTaskBtn.setIcon(IMAGES.addRed());
			flc.add(addTaskBtn, new MarginData(new Margins(0, 0, -5, 0)));

			addTaskBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					final Task tmpTask = new Task(0, user.getEmployeeId());
					tmpTask.setStatus(user.getAccess().isNeedApproval() ? -2 : 0);
					tmpTask.setCreatedBy(user.getEmployeeId());
					taskService.addTask(tmpTask, new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом задании.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							tmpTask.setId(result);
							followedTaskInfoWindow.editInfo(tmpTask);
						}
					});
				}
			});
		}
		
		TextButton editTaskBtn = new TextButton("Редактировать задание");
		editTaskBtn.setIcon(IMAGES.editRow());
		flc.add(editTaskBtn, new MarginData(new Margins(0, 0, -5, 0)));
		
		editTaskBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Task> grid = ((Component) taskGrid).getData("grid");
				Task rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите задание, которое Вы хотите отредактировать.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				if (!rec.isViewed() && rec.getExecutorId() == user.getEmployeeId())
					setTaskViewed(rec.getId());
				taskInfoWindow.editInfo(rec, false, user);
			}
		});
		
		if (user.getAccess().isTaskApprover()) {
			TextButton deleteTaskBtn = new TextButton("Удалить задание");
			deleteTaskBtn.setIcon(IMAGES.deleteRow());
			flc.add(deleteTaskBtn, new MarginData(new Margins(0, 0, -5, 0)));

			final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
				@Override
				public void onDialogHide(DialogHideEvent event) {
					if (event.getHideButton() == PredefinedButton.YES) {
						Grid<Task> grid = ((Component) taskGrid).getData("grid");
						Task rec = grid.getSelectionModel().getSelectedItem();	        	  
						taskService.deleteTask(rec.getId(), new AsyncCallback<Boolean>(){
							@Override
							public void onFailure(Throwable caught) {
								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить задание.");
								d.show();
							}
							@Override
							public void onSuccess(Boolean result) {
								PagingToolBar toolbar = ((Component) taskGrid).getData("pagerToolbar");
								toolbar.refresh();
							}
						});
					}
				}
			};

			deleteTaskBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Task> grid = ((Component) taskGrid).getData("grid");
					Task rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите задание, которое Вы хотите удалить.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранное задание?");
					box.setIcon(IMAGES.warning());
					box.addDialogHideHandler(deleteHideHandler);
					box.show();
				}
			});
		}
		
		TextButton printTaskBtn = new TextButton("Печать");
		printTaskBtn.setIcon(IMAGES.print());
		flc.add(printTaskBtn, new MarginData(new Margins(0, 0, -5, 0)));
		
		printTaskBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Task> grid = ((Component) taskGrid).getData("grid");
				FilterPagingLoadConfig config = (FilterPagingLoadConfig) grid.getLoader().getLastLoadConfig();
				taskService.getPrintableTaskList(config, new AsyncCallback<String>() {
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
		return flc;
	}
	
	private void setTaskViewed(int id) {
		taskService.setViewed(id, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {}
			@Override
			public void onSuccess(Void result) {
				PagingToolBar toolbar = ((Component) taskGrid).getData("pagerToolbar");
				toolbar.refresh();
			}
		});
	}
}
