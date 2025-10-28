package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.ClientProps;
import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.ContractProps;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.client.model.Task;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.ContractService;
import ru.techstandard.shared.ContractServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.TaskService;
import ru.techstandard.shared.TaskServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent.ParseErrorHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.info.Info;

public class FollowedTaskInfoWindow extends Window {
	
	TextField taskId;
	TextField authorId;
	ComboBox<DictionaryRecord> taskType;
	ComboBox<Client> client;
	ComboBox<Contract> contract;
	ComboBox<Employee> executor;
	DateField startDate;
	TasksPlanner planner;
	HorizontalLayoutContainer plannerHLC;
	FieldLabel startDateLabel;
	DateField dueDate;
	TextArea description;
	TextArea notes;
	ComboBox<Employee> follower;
	List<Widget> validityChecks;
	
	Window theWindow;
	
	private int currentContractId;
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private  boolean isModified;
	private boolean canClose=false;
	private int status=0;
	
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final TaskServiceAsync taskService = GWT.create(TaskService.class);
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final ContractServiceAsync contractService = GWT.create(ContractService.class);
	
	private int wndHeight = 570;
	Task task;
	
	@SuppressWarnings("deprecation")
	public FollowedTaskInfoWindow() {
		super();
		this.setPixelSize(500, wndHeight);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Создание задания");
		
		theWindow = this;
		
		VerticalLayoutContainer taskInfoContainer = new VerticalLayoutContainer();
		this.setWidget(taskInfoContainer);
		
		final ChangeHandler formChangedHandler = new ChangeHandler()  {
			@Override
			public void onChange(ChangeEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};
		
		final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};

		int labelColWidth = 160;
		taskId = new TextField();
		authorId = new TextField();
		
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
		final ListStore<DictionaryRecord> taskTypeStore = new ListStore<DictionaryRecord>(props.key());
		dictionaryService.getOrCreateRecByName(Constants.DICT_TASKTYPES, "Закрыться с контрагентом",
				new AsyncCallback<DictionaryRecord>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. FollowTask:148 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(DictionaryRecord result) {
						taskTypeStore.add(result);
					}
				});
	 
		taskType = new ComboBox<DictionaryRecord>(taskTypeStore, props.nameLabel());
	    taskType.setEnabled(false);
	    taskType.setTriggerAction(TriggerAction.ALL);
	    
		FieldLabel taskTypeLabel = new FieldLabel(taskType, "Тип задания");
		taskTypeLabel.setLabelWidth(labelColWidth);
		taskTypeLabel.setHeight(23);
		taskInfoContainer.add(taskTypeLabel, new VerticalLayoutData(1, -1, new Margins(5, 0, 5, 0)));
		
		ClientProps clientProps = GWT.create(ClientProps.class);
	    final ListStore<Client> clientsStore = new ListStore<Client>(clientProps.id());
	    
	    clientService.getClientsByActualness(true,  
				new AsyncCallback<List<Client>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. FollowTask:173 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Client> result) {
						clientsStore.addAll(result);
					}
				});
	 
	    client = new ComboBox<Client>(clientsStore, clientProps.nameLabel());
	    client.setWidth(373);
	    client.setAllowBlank(false);
	    client.setForceSelection(true);
	    client.setTriggerAction(TriggerAction.ALL);

		FieldLabel clientNameLabel = new FieldLabel(client, "Контрагент");
		clientNameLabel.setHeight(23);
		clientNameLabel.setLabelWidth(labelColWidth);
		taskInfoContainer.add(clientNameLabel, new VerticalLayoutData(1, -1));
		client.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				currentContractId = 0;
				loadContracts();
				checkFormValidity();
			}
		});
		client.addSelectionHandler(new SelectionHandler<Client>() {
			@Override
			public void onSelection(SelectionEvent<Client> event) {
				isModified = true;
				currentContractId = 0;
				loadContracts();
				checkFormValidity();
			}
		});
		
		ContractProps contractProps = GWT.create(ContractProps.class);
	    ListStore<Contract> contractStore = new ListStore<Contract>(contractProps.id());
	    
		contract = new ComboBox<Contract>(contractStore, contractProps.captionLabel());
		contract.setWidth(373);
		contract.setAllowBlank(false);
		contract.setForceSelection(true);
		contract.setTriggerAction(TriggerAction.ALL);
		
		FieldLabel contractLabel = new FieldLabel(contract, "Договор");
		contractLabel.setLabelWidth(labelColWidth);
		taskInfoContainer.add(contractLabel, new VerticalLayoutData(1, -1));
		contract.addChangeHandler(formChangedHandler);
		contract.addSelectionHandler(new SelectionHandler<Contract>() {
			@Override
			public void onSelection(SelectionEvent<Contract> event) {
				isModified = true;
				contract.clearInvalid();
				checkFormValidity();
			}
		});
		
	    EmployeeProps employeeProps = GWT.create(EmployeeProps.class);
	    final ListStore<Employee> employeeStore = new ListStore<Employee>(employeeProps.id());
	    final ListStore<Employee> followerStore = new ListStore<Employee>(employeeProps.id());
	    employeeService.getAllEmployees(
				new AsyncCallback<List<Employee>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. FollowTask:238 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Employee> result) {
						employeeStore.replaceAll(result);
						followerStore.replaceAll(result);
					}
				});
	    
	    executor = new ComboBox<Employee>(employeeStore, employeeProps.nameLabel());
	    executor.setWidth(373);
	    executor.addValueChangeHandler(new ValueChangeHandler<Employee>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<Employee> event) {
		    	  isModified = true;
		    	  checkFormValidity();
		    	  updatePlanner(event.getValue().getId());
		      }
		    });
	    executor.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
				isModified = true;
				checkFormValidity();
				updatePlanner(executor.getCurrentValue().getId());
			}
		});
	    executor.addKeyUpHandler(fieldKeyUpHandler);
	    executor.setAllowBlank(false);
	    executor.setForceSelection(true);
	    executor.setTriggerAction(TriggerAction.ALL);
		FieldLabel executorLabel = new FieldLabel(executor, "Исполнитель");
		executorLabel.setLabelWidth(labelColWidth);
		taskInfoContainer.add(executorLabel, new VerticalLayoutData(1, -1));

	    description = new TextArea();
	    description.setWidth(360);
	    description.setEnabled(false);
	    FieldLabel descriptionLabel = new FieldLabel(description, "Описание задачи");
	    descriptionLabel.setLabelWidth(labelColWidth);
	    taskInfoContainer.add(descriptionLabel, new VerticalLayoutData(1, 70));
	    
		startDate = new DateField();
		startDate.setWidth(110);
		startDate.setAllowBlank(false);
		startDate.setEnabled(false);
		startDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    
	    plannerHLC = new HorizontalLayoutContainer();
	    Label sdl = new Label("Дата начала");
	    sdl.setPixelSize(165, 25);
	    plannerHLC.add(sdl, new HorizontalLayoutData(-1, 1));
	    
	    planner = new TasksPlanner();
	    Date now = new Date();
    	planner.setPeriod(now.getMonth(), 1900 + now.getYear());
	    planner.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				startDate.setValue(event.getValue());
				startDate.clearInvalid();
				Date due = addWorkDays(5, event.getValue());
				dueDate.setValue(due);
				dueDate.clearInvalid();
				
			}
		});
	    plannerHLC.add(planner, new HorizontalLayoutData(-1, -1));
	    plannerHLC.add(startDate, new HorizontalLayoutData(-1, 1));
	    plannerHLC.setHeight(185);
	    taskInfoContainer.add(plannerHLC, new VerticalLayoutData(1, -1));
	    
		dueDate = new DateField();
		dueDate.setWidth(130);
		dueDate.setAllowBlank(false);
		dueDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    FieldLabel dueDateLabel = new FieldLabel(dueDate, "Срок исполнения");
	    dueDateLabel.setLabelWidth(labelColWidth);
	    taskInfoContainer.add(dueDateLabel, new VerticalLayoutData(-1, -1));
	    dueDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    dueDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
	    		checkFormValidity();
	    	}
	    });
	    dueDate.addKeyUpHandler(fieldKeyUpHandler);
	    
	    
	    notes = new TextArea();
	    notes.setAllowBlank(true);
	    FieldLabel notesLabel = new FieldLabel(notes, "Примечания");
	    notesLabel.setLabelWidth(labelColWidth);
	    taskInfoContainer.add(notesLabel, new VerticalLayoutData(1, 60));
	    notes.addChangeHandler(formChangedHandler);
	    notes.addKeyUpHandler(fieldKeyUpHandler);

	    follower = new ComboBox<Employee>(followerStore, employeeProps.nameLabel());
	    follower.setWidth(373);
	    follower.addValueChangeHandler(new ValueChangeHandler<Employee>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<Employee> event) {
		    	  isModified = true;
		    	  checkFormValidity();
		      }
		    });
	    follower.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
				isModified = true;
				checkFormValidity();
			}
		});
	    follower.addKeyUpHandler(fieldKeyUpHandler);
	    follower.setAllowBlank(false);
	    follower.setForceSelection(true);
	    follower.setTriggerAction(TriggerAction.ALL);
		FieldLabel followerLabel = new FieldLabel(follower, "После завершения задачу передать");
		followerLabel.setLabelWidth(labelColWidth);
		taskInfoContainer.add(followerLabel, new VerticalLayoutData(1, -1));
	    
	    validityChecks = new ArrayList<Widget>();
	    validityChecks.add(client);
	    validityChecks.add(contract);
	    validityChecks.add(executor);
	    validityChecks.add(startDate);
	    validityChecks.add(dueDate);
	    validityChecks.add(follower);

	    this.setButtonAlign(BoxLayoutPack.END);
	    
	    saveBtn = new TextButton("Сохранить");
	    saveBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		Task task = new Task(Integer.valueOf(taskId.getValue()), Integer.valueOf(authorId.getValue()));
	    		task.setTypeId(taskType.getValue().getId());
	    		task.setExecutorId(executor.getValue().getId());
	    		task.setStartDate(startDate.getValue());
	    		task.setDueDate(dueDate.getValue());
	    		task.setDescription(description.getValue());
	    		task.setNotes(notes.getValue());
	    		task.setStatus(status);
	    		task.setFollowerId(follower.getValue().getId());
	    		Cookies.setCookie("follower_id", String.valueOf(follower.getValue().getId()));
	    		//				При добавлении нового документа пустая запись уже была создана, её нужно только обновить.
	    		taskService.updateTask(task, new AsyncCallback<Boolean>() {
	    			@Override
	    			public void onSuccess(Boolean result) {
	    				canClose=true;
	    				theWindow.setData("hideButton", "save");
	    				theWindow.hide();
	    			}
	    			@Override
	    			public void onFailure(Throwable caught) {
	    				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить информацию о задании.");
	    				d.show();
	    			}
	    		});
	    	}
	    });
	    this.addButton(saveBtn);
	    this.setData("saveBtn", saveBtn);

	    cancelBtn = new TextButton("Отмена");
	    cancelBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		canClose=false; // код в обработчике скрытия вызовет запрос на подтверждение отказа от изменений, если будет необходимо
	    		theWindow.hide();
	    	}
	    });
	    this.addButton(cancelBtn);
	    this.addBeforeHideHandler(new BeforeHideHandler() {
			@Override
			public void onBeforeHide(BeforeHideEvent event) {
				if (!isModified || canClose) {
					unCreateRecIfNeeded();
				} else {
					event.setCancelled(true);
					
					final ConfirmMessageBox box = new ConfirmMessageBox("Отмена изменений", "Вы уверены, что хотите закрыть окно, не сохраняя внесённые изменения?");
					box.addDialogHideHandler(new DialogHideHandler() {
						@Override
						public void onDialogHide(DialogHideEvent event) {
							theWindow.setOnEsc(true);
							if (event.getHideButton() == PredefinedButton.YES) {
								unCreateRecIfNeeded();
								canClose=true;
								theWindow.hide();
							}
						}
					});
					theWindow.setOnEsc(false);
					box.show();
				}
			}
		});
	}

	private void unCreateRecIfNeeded() {
		taskService.deleteTask(Integer.valueOf(taskId.getValue()), new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {}
			@Override
			public void onSuccess(Boolean result) {}
		});
	}
	
	private void checkFormValidity() {
		boolean isFormValid = true;
		Widget w;
		for (int i=0; i < validityChecks.size(); i++) {
			w = validityChecks.get(i);
			if (((ValueBaseField<?>) w).getCurrentValue() == null) {
				((ValueBaseField<?>) w).forceInvalid("Поле не должно быть пустым");
				isFormValid = false;
			} else {
				((ValueBaseField<?>) w).clearInvalid();
			}
		}
		saveBtn.setEnabled(isModified && isFormValid);
		
		String descr = taskType.getValue().getName();
		if (client.getCurrentValue() != null)
			descr += " "+client.getCurrentValue().getName();
		if (contract.getCurrentValue() != null)
			descr += " по договору " +contract.getCurrentValue().getCaption();
		description.setValue(descr);
		description.clearInvalid();
	}

	public void editInfo(Task task) {
		canClose=false;
//		System.out.println("TaskIW.editInfo:"+guide);
		taskId.setValue(String.valueOf(task.getId()));
		authorId.setValue(String.valueOf(task.getCreatedBy()));
		taskType.setValue(taskType.getStore().get(0));
		client.setValue(null);
		contract.setValue(null);
		executor.setValue(null);
		startDate.setValue(null);
		dueDate.setValue(null);
		notes.setValue(null);
		follower.setValue(null);
		String prevFollowerStr = Cookies.getCookie("follower_id");
		int prevFollower = (prevFollowerStr == null)?0:Integer.valueOf(prevFollowerStr);
		List<Employee> emps = follower.getStore().getAll();
		follower.clear();
		for (int i=0; i<emps.size(); i++) {
			if(emps.get(i).getId() == prevFollower) {
				follower.setValue(emps.get(i));
				break;
			}
		}
		executor.clearInvalid();
		status = task.getStatus();

		isModified = false;
		saveBtn.setEnabled(false);
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(550, wndHeight);
		theWindow.forceLayout();
		checkFormValidity();
	}

	private void updatePlanner(int executorId) {
		taskService.getTasksByEmployee(executorId, new AsyncCallback<List<Task>>() {
			@Override
			public void onFailure(Throwable caught) {}
			@Override
			public void onSuccess(List<Task> result) {
				planner.setExecutorTasks(result);
				
			}
		});
	}

	private void loadContracts() {
		final ListStore<Contract> contractStore = contract.getStore();
		if (client.getCurrentValue() == null) {
			contract.setValue(null);
			contract.forceInvalid("Необходимо заполнить");
			contractStore.clear();
			return;
		}
		
		contractService.getContractsForClient(client.getCurrentValue().getId(),  
				new AsyncCallback<List<Contract>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. FollowTask:542 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Contract> result) {
						contractStore.replaceAll(result);
						
						contract.setValue(null);
						List<Contract> contracts = contract.getStore().getAll();
						for (int i=0; i<contracts.size(); i++){
							if(contracts.get(i).getId() == currentContractId) {
								contract.setValue(contracts.get(i));
								break;
							}
						}
						if (contract.getCurrentValue() == null)
							contract.forceInvalid("Необходимо заполнить");
						else
							contract.clearInvalid();
						checkFormValidity();
					}
				});
	}
	
	private Date addWorkDays(int days, Date toDate) {
		int daysToAdd = days;
		DateWrapper wr = new DateWrapper(toDate);
		// добавляем days рабочих дней - будет срок исполнения. 
		// если срок приходится на воскресенье или понедельник, то не считаем рабочий день добавленным - "к понедельнику" означает что в субботу и воскресенье не работали
		// календарь не учитываем - поле дано на редактирование, пользователь сам поменяет, если что не так
		while (daysToAdd > 0) {
			if (wr.getDayInWeek() != 6 && wr.getDayInWeek() != 0) {
				daysToAdd--;
			}
			wr = wr.addDays(1);
		}
		return wr.asDate();
	}
}
