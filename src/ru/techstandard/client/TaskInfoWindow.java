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
import ru.techstandard.client.model.UserDTO;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
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
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class TaskInfoWindow extends Window {
	private final Images IMAGES = GWT.create(Images.class);

	TabItemConfig taskDataTabConfig;
	TabItemConfig taskDocsTabConfig;
	
	private int taskId=0;
//	TextField taskId;
	private int authorId=0;
//	TextField authorId;
	ComboBox<DictionaryRecord> taskType;
	TextButton addTaskTypeBtn;
	ComboBox<Employee> executor;
	DateField startDate;
	TasksPlanner planner;
	HorizontalLayoutContainer plannerHLC;
	FieldLabel startDateLabel;
	DateField dueDate;
	DateField completeDate;
	TextArea description;
	TextArea notes;
	Label statusLabel;
	List<Widget> validityChecks;
	
	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel taskDocsPanel;
	ToolBar toolBar;
	ToggleButton toggleEditModeBtn;
	TextButton setCompletedBtn;
	TextButton approveBtn;
	TextButton declineBtn;
	TextButton confirmCompletedBtn;
	TextButton rejectCompletedBtn;
	TextButton revokeCompletedBtn;
	TextButton printTaskBtn;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private  boolean isModified;
	private boolean canClose=false;
	private int action=0;
	private int status=0;
	private int follower_id=0;
	UserDTO user;
	private AttachmentsContainer attachCont;
	
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final TaskServiceAsync taskService = GWT.create(TaskService.class);
	
	private int wndHeight = 615;
	Task task;
	
	public TaskInfoWindow() {
		super();
		this.setPixelSize(500, wndHeight);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр информации о задании");
		
		theWindow = this;
		
		VerticalLayoutContainer taskInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(taskInfoTopContainer);
		
		toolBar = new ToolBar();
		toolBar.setBorders(true);
				
		toggleEditModeBtn = new ToggleButton("Режим редактирования");
		toggleEditModeBtn.setIcon(IMAGES.edit());
		toggleEditModeBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ToggleButton btn = (ToggleButton) event.getSource();
				toggleEditMode(btn.getValue(), task);
			}
		});
		
		final DialogHideHandler completeConfirmHandler = new DialogHideHandler() {
			@Override
			public void onDialogHide(DialogHideEvent event) {
				if (event.getHideButton() == PredefinedButton.NO) 
					return;
				theWindow.mask("Идёт отправка информации на сервер...");
				taskService.setCompleted(taskId, notes.getCurrentValue(), new AsyncCallback<Boolean>(){
					@Override
					public void onFailure(Throwable caught) {
						theWindow.unmask();
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось отметить задание как выполненное.");
						d.show();
					}
					@Override
					public void onSuccess(Boolean result) {
						theWindow.unmask();
						MessageBox d = new MessageBox("Выполнено", "Задание переведено в статус 'На утверждении'.");
	            		d.setIcon(Images.INSTANCE.information());
						d.show();
	    				canClose=true;
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
						theWindow.setData("hideButton", "save");
						theWindow.hide();
					}
				});
			}
		};
		
		setCompletedBtn = new TextButton("Отметить как выполненное");
		setCompletedBtn.setIcon(IMAGES.checked());
		setCompletedBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				if (notes.getCurrentValue() == null || notes.getCurrentValue().isEmpty()) {
					MessageBox d = new MessageBox("Недостаточно данных", "Для того, чтобы отметить задание как выполненное, Вы должны заполнить поле 'Примечание'");
            		d.setIcon(Images.INSTANCE.information());
					d.show();
					return;
				}
				ConfirmMessageBox box = new ConfirmMessageBox("Выполнение задания", "Вы уверены, что хотите отметить задание как выполненное?");
				box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
	    		box.addDialogHideHandler(completeConfirmHandler);
	    		box.show();
			}
			
		});
		
		final DialogHideHandler approveConfirmHandler = new DialogHideHandler() {
			@Override
			public void onDialogHide(DialogHideEvent event) {
				if (event.getHideButton() == PredefinedButton.NO)
					return;
				theWindow.mask("Идёт отправка информации на сервер...");
				Task task = new Task(taskId, authorId);
	    		task.setTypeId(taskType.getValue().getId());
	    		task.setExecutorId(executor.getValue().getId());
	    		task.setStartDate(startDate.getValue());
	    		task.setDueDate(dueDate.getValue());
	    		task.setDescription(description.getValue());
	    		task.setNotes(notes.getValue());
	    		task.setStatus(status);
				taskService.approveTask(task, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						theWindow.unmask();
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось согласовать задание.");
						d.show();
					}
					@Override
					public void onSuccess(Boolean result) {
						theWindow.unmask();
						MessageBox d = new MessageBox("Выполнено", "Задание переведено в статус 'На исполнении'.");
	            		d.setIcon(Images.INSTANCE.information());
						d.show();
						theWindow.setData("hideButton", "save");
						theWindow.hide();
					}
				});
			}
		};
		
		approveBtn = new TextButton("Согласовать");
		approveBtn.setIcon(IMAGES.inbox());
		approveBtn.setToolTip("Сохранение изменений и назначение задания исполнителю.");
		approveBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ConfirmMessageBox box = new ConfirmMessageBox("Согласование задания", "Вы уверены, что хотите передать задание исполнителю?");
				box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
	    		box.addDialogHideHandler(approveConfirmHandler);
	    		box.show();
			}
			
		});
		
		declineBtn = new TextButton("Отклонить");
		declineBtn.setIcon(IMAGES.trash());
		declineBtn.setToolTip("Удаление задания");
		declineBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Отклонение задания приведёт к его удалению из системы. Вы уверены, что хотите это сделать?");
				box.setIcon(IMAGES.warning());
				box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
				box.addDialogHideHandler(new DialogHideHandler() {
					@Override
					public void onDialogHide(DialogHideEvent event) {
						if (event.getHideButton() == PredefinedButton.NO)
							return;
						theWindow.mask("Идёт отправка информации на сервер...");
						taskService.deleteTask(taskId, new AsyncCallback<Boolean>(){
							@Override
							public void onFailure(Throwable caught) {
								theWindow.unmask();
								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить задание.");
								d.show();
							}
							@Override
							public void onSuccess(Boolean result) {
								theWindow.unmask();
								MessageBox d = new MessageBox("Выполнено", "Задание удалено.");
			            		d.setIcon(Images.INSTANCE.information());
								d.show();
								theWindow.setData("hideButton", "save");
								theWindow.hide();
							}
						});						
					}});
				box.show();
			}
		});
		
		confirmCompletedBtn = new TextButton("Подтвердить выполнение");
		confirmCompletedBtn.setIcon(IMAGES.checked());
		confirmCompletedBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ConfirmMessageBox box = new ConfirmMessageBox("Подтверждение выполнения", "Вы уверены, что хотите подтвердить, что задание выполнено исполнителем?");
				box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
	    		box.addDialogHideHandler(new DialogHideHandler() {
	    			@Override
	    			public void onDialogHide(DialogHideEvent event) {
	    				if (event.getHideButton() == PredefinedButton.YES) {
	    					theWindow.mask("Идёт отправка информации на сервер...");
	    					taskService.updateTaskStatus(taskId, 1, false, new AsyncCallback<Boolean>(){
	    						@Override
	    						public void onFailure(Throwable caught) {
	    							theWindow.unmask();
	    							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось отметить задание как выполненное.");
	    							d.show();
	    						}
	    						@Override
	    						public void onSuccess(Boolean result) {
	    							theWindow.unmask();
	    							MessageBox d = new MessageBox("Выполнено", "Задание переведено в статус 'Выполнено'.");
	    							d.setIcon(Images.INSTANCE.information());
	    							d.show();
	    							theWindow.setData("hideButton", "save");
	    							theWindow.hide();
	    						}
	    					});
	    				}
	    			}
	    		});
				box.show();
			}
		});
		
		rejectCompletedBtn = new TextButton("Отклонить выполнение");
		rejectCompletedBtn.setIcon(IMAGES.delete());
		rejectCompletedBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ConfirmMessageBox box = new ConfirmMessageBox("Отклонение выполнения", "Вы уверены, что хотите отменить задание как не выполненное исполнителем?");
				box.setIcon(IMAGES.warning());
				box.addDialogHideHandler(new DialogHideHandler() {
					@Override
					public void onDialogHide(DialogHideEvent event) {
						if (event.getHideButton() == PredefinedButton.YES) {	
							theWindow.mask("Идёт отправка информации на сервер...");
							taskService.updateTaskStatus(taskId, 0, true, new AsyncCallback<Boolean>(){
								@Override
								public void onFailure(Throwable caught) {
									theWindow.unmask();
									AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось изменить статус задания.");
									d.show();
								}
								@Override
								public void onSuccess(Boolean result) {
									theWindow.unmask();
									MessageBox d = new MessageBox("Выполнено", "Задание переведено в статус 'Не выполнено'.");
									d.setIcon(Images.INSTANCE.information());
									d.show();
									theWindow.setData("hideButton", "save");
									theWindow.hide();
								}
							});
						}
					}
				});
				box.show();
			}
		});
		
		revokeCompletedBtn = new TextButton("Отозвать из выполненных");
		revokeCompletedBtn.setIcon(IMAGES.undo());
		revokeCompletedBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ConfirmMessageBox box = new ConfirmMessageBox("Отзыв из выполненных", "Вы уверены, что хотите вернуть задание в статус 'Не выполнено'?");
				box.setIcon(IMAGES.warning());
	    		box.addDialogHideHandler(new DialogHideHandler() {
	    			@Override
	    			public void onDialogHide(DialogHideEvent event) {
	    				if (event.getHideButton() == PredefinedButton.YES) {
	    					theWindow.mask("Идёт отправка информации на сервер...");
	    					taskService.updateTaskStatus(taskId, 0, true, new AsyncCallback<Boolean>(){
	    						@Override
	    						public void onFailure(Throwable caught) {
	    							theWindow.unmask();
	    							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось изменить статус задания.");
	    							d.show();
	    						}
	    						@Override
	    						public void onSuccess(Boolean result) {
	    							theWindow.unmask();
	    							MessageBox d = new MessageBox("Выполнено", "Задание переведено в статус 'Не выполнено'.");
	    		            		d.setIcon(Images.INSTANCE.information());
	    							d.show();
	    							theWindow.setData("hideButton", "save");
	    							theWindow.hide();
	    						}
	    					});
	    				}
	    			}
	    		});
	    		box.show();
			}
		});
		
		printTaskBtn = new TextButton("Печать");
		printTaskBtn.setIcon(IMAGES.print());
		printTaskBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				taskService.getPrintableTaskCard(taskId, new AsyncCallback<String>() {
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
		toolBar.add(printTaskBtn);
		
		taskInfoTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		TabPanel panel = new TabPanel();

		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer taskInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(taskInfoContainer);
		
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
		
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
		final ListStore<DictionaryRecord> objTypeStore = new ListStore<DictionaryRecord>(props.key());
		dictionaryService.getDictionaryContents(Constants.DICT_TASKTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. TaskIW:445 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						objTypeStore.addAll(result);
					}
				});
	 
		taskType = new ComboBox<DictionaryRecord>(objTypeStore, props.nameLabel());
	    taskType.setAllowBlank(false);
	    taskType.setForceSelection(true);
	    taskType.setTriggerAction(TriggerAction.ALL);
	    
	    addTaskTypeBtn = new TextButton("");
	    addTaskTypeBtn.setIcon(Images.INSTANCE.add());
	    addTaskTypeBtn.setToolTip("Добавить тип задания");
	    addTaskTypeBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final PromptMessageBox box = new PromptMessageBox("Тип задания", "Введите новое значение для справочника<br>типов заданий:");
	          box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
	            	theWindow.setOnEsc(true);
					if (event.getHideButton() == null || event.getHideButton() == PredefinedButton.CANCEL) {
						return;
					}
	            	if (box.getValue() == null || box.getValue().trim().isEmpty()) {
	            		MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите краткое название типа задания.");
	            		d.setIcon(Images.INSTANCE.information());
						d.show();
						return;
	            	}
	            	dictionaryService.addRecord(Constants.DICT_TASKTYPES, box.getValue().trim(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							DictionaryRecord rec = new DictionaryRecord(result, box.getValue().trim());
							objTypeStore.add(rec);
							taskType.setValue(rec);
							taskType.clearInvalid();
							isModified = true;
							checkFormValidity();
						}
					});
	            }
	          });
	          theWindow.setOnEsc(false);
	          box.show();
	          box.getField().focus();
	          box.setClosable(true);
	          box.setOnEsc(true);
	        }
	      });
	    
	    HorizontalLayoutContainer objTypeHLC = new HorizontalLayoutContainer();
	    objTypeHLC.add(taskType, new HorizontalLayoutData(1, 1));
	    objTypeHLC.add(addTaskTypeBtn, new HorizontalLayoutData(-1, -1));
	    
		FieldLabel taskTypeLabel = new FieldLabel(objTypeHLC, "Тип задания");
		taskTypeLabel.setLabelWidth(labelColWidth);
		taskTypeLabel.setHeight(23);
		taskInfoContainer.add(taskTypeLabel, new VerticalLayoutData(1, -1));
		taskType.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
			@Override
			public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();				
			}
		});
		taskType.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		taskType.addKeyUpHandler(fieldKeyUpHandler);
		
		
	    EmployeeProps employeeProps = GWT.create(EmployeeProps.class);
	    ListStore<Employee> employeeStore = new ListStore<Employee>(employeeProps.id());
	    
	    executor = new ComboBox<Employee>(employeeStore, employeeProps.nameLabel());
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
	    description.setAllowBlank(true);
	    FieldLabel descriptionLabel = new FieldLabel(description, "Описание задачи");
	    descriptionLabel.setLabelWidth(labelColWidth);
	    taskInfoContainer.add(descriptionLabel, new VerticalLayoutData(1, 60));
	    description.addChangeHandler(formChangedHandler);
	    description.addKeyUpHandler(fieldKeyUpHandler);
	    
		startDate = new DateField();
		startDate.setAllowBlank(false);
		startDate.setEnabled(false);
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
	    		isModified = true;
	    		checkFormValidity();
	    	}
	    });
	    startDate.addKeyUpHandler(fieldKeyUpHandler);
	    
	    plannerHLC = new HorizontalLayoutContainer();

	    Label sdl = new Label("Дата начала");
	    sdl.setPixelSize(165, 25);
	    plannerHLC.add(sdl, new HorizontalLayoutData(-1, 1));
	    
	    planner = new TasksPlanner();
	    planner.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				startDate.setValue(event.getValue());
				startDate.clearInvalid();
				isModified = true;
	    		checkFormValidity();
			}
		});
	    plannerHLC.add(planner, new HorizontalLayoutData(-1, 1));
	    plannerHLC.add(startDate, new HorizontalLayoutData(-1, 1));
	    plannerHLC.setHeight(180);
	    taskInfoContainer.add(plannerHLC, new VerticalLayoutData(1, -1));
	    
		dueDate = new DateField();
		dueDate.setAllowBlank(false);
		dueDate.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				System.out.println("dueDate change event fired");
			}
		});
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
	    		isModified = true;
				checkFormValidity();
	    	}
	    });
	    dueDate.addKeyUpHandler(fieldKeyUpHandler);
	    dueDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});
	    FieldLabel dueDateLabel = new FieldLabel(dueDate, "Срок исполнения");
	    dueDateLabel.setLabelWidth(labelColWidth);
	    taskInfoContainer.add(dueDateLabel, new VerticalLayoutData(1, -1));
	    
	    completeDate = new DateField();
	    completeDate.setEnabled(false);
	    completeDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    FieldLabel completeDateLabel = new FieldLabel(completeDate, "Дата завершения");
	    completeDateLabel.setLabelWidth(labelColWidth);
	    taskInfoContainer.add(completeDateLabel, new VerticalLayoutData(1, -1));
	    
	    notes = new TextArea();
	    notes.setAllowBlank(true);
	    FieldLabel notesLabel = new FieldLabel(notes, "Примечания");
	    notesLabel.setLabelWidth(labelColWidth);
	    taskInfoContainer.add(notesLabel, new VerticalLayoutData(1, 60));
	    notes.addChangeHandler(formChangedHandler);
	    notes.addKeyUpHandler(fieldKeyUpHandler);
	    
	    
	    statusLabel = new Label("Статус: ");
	    taskInfoContainer.add(statusLabel, new VerticalLayoutData(1, -1));

	    // документы
	    taskDocsPanel = new ContentPanel();
	    taskDocsPanel.setHeaderVisible(false);
	    taskDocsPanel.setBorders(false);
	    taskDocsPanel.setHeight(460);

	    attachCont = new AttachmentsContainer(Constants.TASK_ATTACHMENTS);
	    taskDocsPanel.setWidget(attachCont);

	    // *** Добавление табов в контейнер

	    taskDataTabConfig = new TabItemConfig("Данные задания");
	    taskDataTabConfig.setIcon(IMAGES.view());

	    taskDocsTabConfig = new TabItemConfig("Связанная документация");
	    taskDocsTabConfig.setIcon(IMAGES.documents());

	    panel.add(infoPanel, taskDataTabConfig);
	    panel.add(taskDocsPanel, taskDocsTabConfig);


	    taskInfoTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));


	    validityChecks = new ArrayList<Widget>();

	    validityChecks.add(taskType);
	    validityChecks.add(executor);
	    validityChecks.add(startDate);
	    validityChecks.add(dueDate);
	    validityChecks.add(description);

	    this.setButtonAlign(BoxLayoutPack.END);
	    
	    saveBtn = new TextButton("Сохранить");
	    saveBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		Task task = new Task(taskId, authorId);
	    		task.setTypeId(taskType.getCurrentValue().getId());
	    		task.setExecutorId(executor.getCurrentValue().getId());
	    		task.setStartDate(startDate.getCurrentValue());
	    		task.setDueDate(dueDate.getCurrentValue());
	    		task.setDescription(description.getCurrentValue());
	    		task.setNotes(notes.getCurrentValue());
	    		task.setStatus(status);
	    		task.setFollowerId(follower_id);
	    		theWindow.mask("Идёт отправка информации на сервер...");
//				При добавлении нового документа пустая запись уже была создана, её нужно только обновить.
	    		taskService.updateTask(task, new AsyncCallback<Boolean>() {
	    			@Override
	    			public void onSuccess(Boolean result) {
	    				theWindow.unmask();
//						Если создаём новую запись - нужно сообщить вызывающему коду, что мы создали.
	    				if (action == Constants.ACTION_ADD) {
	    					theWindow.setData("newTaskID", String.valueOf(taskId));
	    				}
	    				canClose=true;
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
						theWindow.setData("hideButton", "save");
	    				theWindow.hide();
	    			}
	    			@Override
	    			public void onFailure(Throwable caught) {
	    				theWindow.unmask();
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
		if (action == Constants.ACTION_ADD) {
			taskService.deleteTask(taskId, new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(Boolean result) {}
			});
		}
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
		if (action == Constants.ACTION_ADD && dueDate.getCurrentValue() != null && startDate.getCurrentValue() != null 
		        && dueDate.getCurrentValue().before(startDate.getCurrentValue())) {
		    dueDate.forceInvalid("Срок исполнения меньше даты начала");
		    isFormValid = false;
		}
		saveBtn.setEnabled(isModified && isFormValid);
	}
	
	@SuppressWarnings("deprecation")
	private void fillWindowFields(final Task task) {
//		taskId.setValue(String.valueOf(task.getId()));
		taskId = task.getId();
//		authorId.setValue(String.valueOf(task.getCreatedBy()));
		authorId = task.getCreatedBy();
		
		final ListStore<DictionaryRecord> objTypeStore = taskType.getStore();
		dictionaryService.getDictionaryContents(Constants.DICT_TASKTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. TaskIW:811 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						objTypeStore.replaceAll(result);
						taskType.clear();
						for (int i=0; i<result.size(); i++) {
							if(result.get(i).getName().equals(task.getTypeName())) {
								taskType.setValue(result.get(i));
								break;
							}
						}
						// проверка выполнится, когда вернётся асинхронный запрос. к тому моменту все остальные поля уже будут заполнены
						checkFormValidity();
					}
				});
		
		List<Employee> emps = executor.getStore().getAll();
		executor.clear();
		for (int i=0; i<emps.size(); i++) {
			if(emps.get(i).getName().equals(task.getExecutorName())) {
				executor.setValue(emps.get(i));
				break;
			}
		}
		executor.clearInvalid();
		
		startDate.setValue(task.getStartDate());
		if (task.getExecutorId() != user.getEmployeeId()) {
			taskService.getTasksByEmployee(task.getExecutorId(), new AsyncCallback<List<Task>>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(List<Task> result) {
					planner.setExecutorTasks(result);
				}
			});
		} else {
			List<Task> singleTaskList = new ArrayList<Task>();
			singleTaskList.add(task);
			planner.setExecutorTasks(singleTaskList);
		}
		if (task.getStartDate() != null)
	    	planner.setValue(task.getStartDate());
	    else {
	    	Date now = new Date();
	    	planner.setPeriod(now.getMonth(), 1900 + now.getYear());
	    }
//		taskService.getTasksByEmployee(task.getExecutorId(), new AsyncCallback<List<Task>>() {
//			@Override
//			public void onFailure(Throwable caught) {
//			}
//			@Override
//			public void onSuccess(List<Task> result) {
//				planner.setExecutorTasks(result);
//				if (task.getStartDate() != null)
//			    	planner.setValue(task.getStartDate());
//			    else {
//			    	Date now = new Date();
//			    	planner.setPeriod(now.getMonth(), 1900 + now.getYear());
//			    }
//			}
//		});
		
//		startDateLabel.setHeight(145);
//		plannerHLC.setHeight(180);
		
		dueDate.setValue(task.getDueDate());
		completeDate.setValue(task.getCompletedDate());
		description.setValue(task.getDescription());
		notes.setValue(task.getNotes());
		
		status = task.getStatus();
		statusLabel.setText("Статус задания: "+(task.getStatus()==1?"Выполнено":(task.getStatus()==0?"Не выполнено":(task.getStatus()==-1?"На утверждении":"На согласовании"))));
		statusLabel.setVisible(action != Constants.ACTION_ADD);
		follower_id= task.getFollowerId();
		
		attachCont.init(task.getId());
		printTaskBtn.removeFromParent();
		
		// править поля можно только при наличии полномочий администратора заданий/начальника и при статусе "не выполнено"
		// либо при наличии полномочий согласования и при статусе "на согласовании
		// и ещё сам пользователь должен видеть эту кнопку, если задание ещё не выполнено
		boolean isAttached = (toggleEditModeBtn.getParent() != null);
		if (((user.getAccess().isTaskCreator() ||user.isBoss()) && task.getStatus()==0) ||
				((user.getAccess().isTaskApprover() || user.getAccess().isTaskCreator()) && task.getStatus()==-2) ||
				(task.getStatus()==0 && (task.getExecutorId()==user.getEmployeeId() || task.getCreatedBy()==user.getEmployeeId()))) {
			if (!isAttached) {
				toolBar.add(toggleEditModeBtn);
			}
		} else {
			toggleEditModeBtn.removeFromParent();
		}
		
//		// Расставляем кнопки управления статусом
		isAttached = (approveBtn.getParent() != null);
		if (task.getStatus()==-2 && user.getAccess().isTaskApprover()) {
			if (!isAttached) {
				toolBar.add(approveBtn);
			}
		} else {
			approveBtn.removeFromParent();
		}
		
		isAttached = (declineBtn.getParent() != null);
		if (task.getStatus()==-2 && user.getAccess().isTaskApprover()) {
			if (!isAttached) {
				toolBar.add(declineBtn);
			}
		} else {
			declineBtn.removeFromParent();
		}
		
//		setCompletedBtn.setVisible(task.getStatus()==0 && task.getExecutorId()==user.getEmployeeId());
		isAttached = (setCompletedBtn.getParent() != null);
		if (task.getStatus()==0 && task.getExecutorId()==user.getEmployeeId()) {
			if (!isAttached) {
				toolBar.add(setCompletedBtn);
			}
		} else {
			setCompletedBtn.removeFromParent();
		}
//		confirmCompletedBtn.setVisible(user.getAccess().isTaskConfirmer() && task.getStatus()==-1);
		isAttached = (confirmCompletedBtn.getParent() != null);
		if (task.getStatus()==-1 && user.getAccess().isTaskCreator() 
		        // Задания можно утверждать свои - или созданные автоматически
		        && (task.getCreatedBy() == user.getEmployeeId() || task.getCreatedBy() == Constants.ADMIN_ID)) {
			if (!isAttached) {
				toolBar.add(confirmCompletedBtn);
			}
		} else {
			confirmCompletedBtn.removeFromParent();
		}
//		rejectCompletedBtn.setVisible(user.getAccess().isTaskConfirmer() && task.getStatus()==-1);
		isAttached = (rejectCompletedBtn.getParent() != null);
		if (task.getStatus()==-1 && user.getAccess().isTaskCreator() 
		        // Задания можно отклонять свои - или созданные автоматически
                && (task.getCreatedBy() == user.getEmployeeId() || task.getCreatedBy() == Constants.ADMIN_ID)) {
			if (!isAttached) {
				toolBar.add(rejectCompletedBtn);
			}
		} else {
			rejectCompletedBtn.removeFromParent();
		}
//		revokeCompletedBtn.setVisible(user.getAccess().isTaskConfirmer() && task.getStatus()==1);
		isAttached = (revokeCompletedBtn.getParent() != null);
		if (task.getStatus()==1 && user.getAccess().isTaskCreator() 
		     // Задания можно возвращать на исполнение свои - или созданные автоматически
                && (task.getCreatedBy() == user.getEmployeeId() || task.getCreatedBy() == Constants.ADMIN_ID)) {
			if (!isAttached) {
				toolBar.add(revokeCompletedBtn);
			}
		} else {
			revokeCompletedBtn.removeFromParent();
		}
		
		toolBar.add(printTaskBtn);
		toolBar.forceLayout();
	}
	
	public void displayInfo(Task task, UserDTO loggedUser) {
//		System.out.println("TaskIW.displayInfo:"+guide);
		action = -1;
		canClose=false;
		
		user = loggedUser;
		this.task = task;
		fillEmployeeStore(task);

		isModified = false;	
		
		toggleEditMode(false, task);
		toggleEditModeBtn.setValue(false);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(550, wndHeight);
		theWindow.forceLayout();
	}

	public void editInfo(Task task, boolean newTask, UserDTO loggedUser) {
//		System.out.println("TaskIW.editInfo:"+guide);
		action = newTask ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		user = loggedUser;
		this.task = task;
		fillEmployeeStore(task);

		isModified = false;
		
		toggleEditMode(true, task);
		toggleEditModeBtn.setValue(true);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(550, wndHeight);
		theWindow.forceLayout();
	}
	
	private void toggleEditMode(boolean editMode, Task task) {
		boolean executorEditable = false;
		boolean formEditable = false;
		boolean notesEditable = false;
		if (task.getStatus() == -2) {// на согласовании
			executorEditable = editMode;
			formEditable = editMode && (user.getAccess().isTaskCreator() || user.getAccess().isTaskApprover());
			notesEditable = false;
		} else if (task.getStatus() == -1) { // на утверждении
			executorEditable = false;
			formEditable = false;
			notesEditable = false;
		} else if (task.getStatus() == 0) { // на исполнении, или первоначальное создание
			executorEditable = editMode;
			formEditable = editMode && (action == Constants.ACTION_ADD || task.getCreatedBy()==user.getEmployeeId());
			notesEditable = editMode && task.getExecutorId()==user.getEmployeeId();
		} else if (task.getStatus() == 1) { // выполнено
			executorEditable = false;
			formEditable = false;
			notesEditable = false;
		} 
		
		taskType.setEnabled(formEditable);
		addTaskTypeBtn.setEnabled(formEditable);
		executor.setEnabled(executorEditable);
//		startDate.setEnabled(editMode);
		planner.setEnabled(formEditable);
//		startDateLabel.setHeight(145);
//		plannerHLC.setHeight(180);
		
		dueDate.setEnabled(formEditable);
		description.setEnabled(formEditable);
		notes.setEnabled(notesEditable);
		
		attachCont.setEditMode(formEditable);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование информации о задании");
		else
			setHeadingText("Просмотр информации о задании");
		saveBtn.setEnabled(false);
		
		theWindow.forceLayout();
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
	
	private void fillEmployeeStore(final Task task) {
		Privilege priv;
		if (user.getAccess().isTaskCreator())
			priv = Privilege.ADMIN;
		else
			priv = (user.isBoss() ? Privilege.BOSS : Privilege.USER);
		
		final ListStore<Employee> employeeStore = executor.getStore();
		employeeStore.clear();
		// В статусе "Выполнено" и "На утверждении", когда поле "Исполнитель" нельзя редактировать - достаточно просто показать исполнителя
		if (task.getStatus()==-1 || task.getStatus()==1) {
			employeeService.getEmployeeInfo(task.getExecutorId(), new AsyncCallback<Employee>() {
				public void onFailure(Throwable caught) {
					AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. TaskIW:1086 - "+caught.getMessage());
					d.show();
				}
				@Override
				public void onSuccess(Employee result) {
					employeeStore.add(result);
					fillWindowFields(task);
				}
			});
			return;
		}
		if (priv == Privilege.ADMIN || priv == Privilege.BOSS) {
	    	employeeService.getSubordinates(user.getDepartmentId(), new AsyncCallback<List<Employee>>() {
				public void onFailure(Throwable caught) {
					AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. TaskIW:1100 - "+caught.getMessage());
					d.show();
				}
				@Override
				public void onSuccess(List<Employee> result) {
					employeeStore.replaceAll(result);
					fillWindowFields(task);
				}
			});
		} else { // это уже просто юзер
			employeeService.getColleagues(user.getDepartmentId(),
					new AsyncCallback<List<Employee>>() {
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. TaskIW:1113 - "+caught.getMessage());
							d.show();
						}
						@Override
						public void onSuccess(List<Employee> result) {
							employeeStore.replaceAll(result);
							fillWindowFields(task);
						}
					});
		}
	}
}
