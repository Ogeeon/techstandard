package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.client.model.Evaluation;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.EvaluationService;
import ru.techstandard.shared.EvaluationServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent.ParseErrorHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class EvaluationInfoWindow extends Window {
	private final Images IMAGES = GWT.create(Images.class);

	TabItemConfig evalDataTabConfig;
	TabItemConfig evalDocsTabConfig;
	
	TextField evalId;
	ComboBox<Employee> employee;
	TextField position;
	ComboBox<DictionaryRecord> field;
	TextButton addFieldBtn;
	TextField certNum;
	DateField lastEvalDate;
	DateField nextEvalDate;
	List<Widget> validityChecks;
	
	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel evalDocsPanel;
	ToolBar topToolBar;
	ToggleButton toggleEditModeBtn;
	TextButton printDeviceBtn;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private  boolean isModified;
	private int action=0;
	private boolean canClose=false;
	private AttachmentsContainer attachCont;
	
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final EvaluationServiceAsync evaluationService = GWT.create(EvaluationService.class);
	
	public EvaluationInfoWindow() {
		super();
		this.setPixelSize(500, 330);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр информации о проведённой аттестации");
		
		theWindow = this;
		
		VerticalLayoutContainer evalInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(evalInfoTopContainer);
		
		ToolBar toolBar = new ToolBar();
		toolBar.setBorders(true);
				
		toggleEditModeBtn = new ToggleButton("Режим редактирования");
		toggleEditModeBtn.setIcon(IMAGES.edit());
		toolBar.add(toggleEditModeBtn);
		toggleEditModeBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ToggleButton btn = (ToggleButton) event.getSource();
				toggleEditMode(btn.getValue());
			}
		});
		
		printDeviceBtn = new TextButton("Печать");
		printDeviceBtn.setIcon(IMAGES.print());
		printDeviceBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				evaluationService.getPrintableEvaluationCard(Integer.valueOf(evalId.getText()), new AsyncCallback<String>() {
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
		toolBar.add(printDeviceBtn);
		
		evalInfoTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		TabPanel panel = new TabPanel();

		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer evaluationInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(evaluationInfoContainer);
		
		final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};

		int labelColWidth = 170;
		evalId = new TextField();
		
		EmployeeProps employeeProps = GWT.create(EmployeeProps.class);
	    final ListStore<Employee> employeeStore = new ListStore<Employee>(employeeProps.id());
	    employeeService.getAllEmployees(new AsyncCallback<List<Employee>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. EvalIW:174 - "+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<Employee> result) {
				employeeStore.addAll(result);
 			}
		});
	    
	    employee = new ComboBox<Employee>(employeeStore, employeeProps.nameLabel());
	    employee.addValueChangeHandler(new ValueChangeHandler<Employee>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<Employee> event) {
		    	  isModified = true;
		    	  updatePosition();
		    	  checkFormValidity();
		      }
		    });
	    employee.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
				isModified = true;
				updatePosition();
				checkFormValidity();
			}
		});
	    employee.addKeyUpHandler(fieldKeyUpHandler);
	    employee.setAllowBlank(false);
	    employee.setForceSelection(true);
	    employee.setTriggerAction(TriggerAction.ALL);
		FieldLabel responsibleLabel = new FieldLabel(employee, "Сотрудник");
		responsibleLabel.setLabelWidth(labelColWidth);
		evaluationInfoContainer.add(responsibleLabel, new VerticalLayoutData(1, -1));
		
		position = new TextField();
		position.setEnabled(false);
		FieldLabel positionLabel = new FieldLabel(position, "Должность");
		positionLabel.setLabelWidth(labelColWidth);
		evaluationInfoContainer.add(positionLabel, new VerticalLayoutData(1, -1));
		
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
		final ListStore<DictionaryRecord> evalFieldsStore = new ListStore<DictionaryRecord>(props.key());
		dictionaryService.getDictionaryContents(Constants.DICT_EVAL_FIELDS,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. EvalIW:219 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						evalFieldsStore.addAll(result);
					}
				});
	 
		field = new ComboBox<DictionaryRecord>(evalFieldsStore, props.nameLabel());
	    field.setAllowBlank(false);
	    field.setForceSelection(true);
	    field.setTriggerAction(TriggerAction.ALL);
	    
	    addFieldBtn = new TextButton("");
	    addFieldBtn.setIcon(Images.INSTANCE.add());
	    addFieldBtn.setToolTip("Добавить область аттестации");
	    addFieldBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final PromptMessageBox box = new PromptMessageBox("Область аттестации", "Введите новое значение для справочника<br>областей аттестации:");
	          box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
	            	if (event.getHideButton() == PredefinedButton.CANCEL) {
						return;
					}
	            	if (box.getValue() == null || box.getValue().trim().isEmpty()) {
	            		MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите название область аттестации.");
	            		d.setIcon(Images.INSTANCE.information());
						d.show();
						return;
	            	}
	            	dictionaryService.addRecord(Constants.DICT_EVAL_FIELDS, box.getValue().trim(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							DictionaryRecord rec = new DictionaryRecord(result, box.getValue().trim());
							evalFieldsStore.add(rec);
							field.setValue(rec);
						}
					});
	            }
	          });
	          box.show();
	        }
	      });
	    
	    HorizontalLayoutContainer evalFieldHLC = new HorizontalLayoutContainer();
	    evalFieldHLC.add(field, new HorizontalLayoutData(1, 1));
	    evalFieldHLC.add(addFieldBtn, new HorizontalLayoutData(-1, -1));
	    
		FieldLabel evalFieldLabel = new FieldLabel(evalFieldHLC, "Область аттестации");
		evalFieldLabel.setLabelWidth(labelColWidth);
		evalFieldLabel.setHeight(23);
		evaluationInfoContainer.add(evalFieldLabel, new VerticalLayoutData(1, -1));
		field.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
		    	  isModified = true;
		    	  checkFormValidity();
		      }
		    });
		field.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		field.addKeyUpHandler(fieldKeyUpHandler);
		
		certNum = new TextField();
		certNum.setAllowBlank(true);
		FieldLabel objRNumLabel = new FieldLabel(certNum, "№ удостоверения");
		objRNumLabel.setLabelWidth(labelColWidth);
		evaluationInfoContainer.add(objRNumLabel, new VerticalLayoutData(1, -1));
		certNum.addKeyUpHandler(fieldKeyUpHandler);
		
		lastEvalDate = new DateField();
		lastEvalDate.setAllowBlank(false);
		lastEvalDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    lastEvalDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    lastEvalDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
	    		checkFormValidity();
	    	}
	    });
	    lastEvalDate.addKeyUpHandler(fieldKeyUpHandler);
	    lastEvalDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});
	    FieldLabel lastEvalDateLabel = new FieldLabel(lastEvalDate, "Дата последней аттестации");
	    lastEvalDateLabel.setLabelWidth(labelColWidth);
	    evaluationInfoContainer.add(lastEvalDateLabel, new VerticalLayoutData(1, -1));

	    nextEvalDate = new DateField();
		nextEvalDate.setAllowBlank(false);
		nextEvalDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    nextEvalDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    nextEvalDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
	    		checkFormValidity();
	    	}
	    });
	    nextEvalDate.addKeyUpHandler(fieldKeyUpHandler);
	    nextEvalDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});
	    FieldLabel nextEvalDateLabel = new FieldLabel(nextEvalDate, "Дата следующей аттестации");
	    nextEvalDateLabel.setLabelWidth(labelColWidth);
	    evaluationInfoContainer.add(nextEvalDateLabel, new VerticalLayoutData(1, -1));

	    // документы
	    evalDocsPanel = new ContentPanel();
	    evalDocsPanel.setHeaderVisible(false);
	    evalDocsPanel.setBorders(false);
	    evalDocsPanel.setHeight(190);

	    attachCont = new AttachmentsContainer(Constants.EVAL_ATTACHMENTS);
	    evalDocsPanel.setWidget(attachCont);

	    // *** Добавление табов в контейнер

	    evalDataTabConfig = new TabItemConfig("Данные аттестации");
	    evalDataTabConfig.setIcon(IMAGES.view());

	    evalDocsTabConfig = new TabItemConfig("Связанная документация");
	    evalDocsTabConfig.setIcon(IMAGES.documents());

	    panel.add(infoPanel, evalDataTabConfig);
	    panel.add(evalDocsPanel, evalDocsTabConfig);


	    evalInfoTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));


	    validityChecks = new ArrayList<Widget>();

	    validityChecks.add(employee);
	    validityChecks.add(field);
	    validityChecks.add(certNum);
	    validityChecks.add(lastEvalDate);
	    validityChecks.add(nextEvalDate);

	    this.setButtonAlign(BoxLayoutPack.END);
	    
	    saveBtn = new TextButton("Сохранить");
	    saveBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		Evaluation eval = new Evaluation(Integer.valueOf(evalId.getValue()));
	    		eval.setEmployeeId(employee.getValue().getId());
	    		eval.setFieldId(field.getValue().getId());
	    		eval.setCertNum(certNum.getValue());
	    		eval.setLastEvalDate(lastEvalDate.getValue());
	    		eval.setNextEvalDate(nextEvalDate.getValue());
	    		//				При добавлении нового документа пустая запись уже была создана, её нужно только обновить.
	    		evaluationService.updateEvaluation(eval, new AsyncCallback<Boolean>() {
	    			@Override
	    			public void onSuccess(Boolean result) {
	    				//						Если создаём новую запись - нужно сообщить вызывающему коду, что мы создали.
	    				if (action == Constants.ACTION_ADD) {
	    					theWindow.setData("newEvaluationID", evalId.getValue());
	    				}
	    				canClose=true;
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
						theWindow.setData("hideButton", "save");
	    				theWindow.hide();
	    			}
	    			@Override
	    			public void onFailure(Throwable caught) {
	    				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить информацию.");
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
			evaluationService.deleteEvaluation(Integer.valueOf(evalId.getValue()), false, new AsyncCallback<Boolean>() {
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
		saveBtn.setEnabled(isModified && isFormValid);
	}
	
	private void fillWindowFields(Evaluation eval) {
		evalId.setValue(String.valueOf(eval.getId()));
		
		List<Employee> emps = employee.getStore().getAll();
		employee.clear();
		for (int i=0; i<emps.size(); i++) {
			if(emps.get(i).getName().equals(eval.getEmployeeName())) {
				employee.setValue(emps.get(i));
				break;
			}
		}
		employee.clearInvalid();
		updatePosition();
		
		List<DictionaryRecord> fields = field.getStore().getAll();
		field.clear();
		for (int i=0; i<fields.size(); i++) {
			if(fields.get(i).getName().equals(eval.getFieldName())) {
				field.setValue(fields.get(i));
				break;
			}
		}
		field.clearInvalid();
		
		certNum.setValue(eval.getCertNum());
		lastEvalDate.setValue(eval.getLastEvalDate());
		nextEvalDate.setValue(eval.getNextEvalDate());
		
		attachCont.init(eval.getId());
	}
	
	public void displayInfo(Evaluation eval, boolean canEdit, boolean canPrint) {
//		System.out.println("EvaluationIW.displayInfo:"+guide);
		action = -1;
		canClose=false;
		fillWindowFields(eval);

		isModified = false;	
		
		toggleEditMode(false);
		toggleEditModeBtn.setValue(false);
		toggleEditModeBtn.setVisible(canEdit);
		
		printDeviceBtn.setVisible(canPrint);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, 330);
		theWindow.forceLayout();
	}

	public void editInfo(Evaluation eval, boolean newEvaluation, boolean canPrint) {
//		System.out.println("EvaluationIW.editInfo:"+eval);
		action = newEvaluation ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		fillWindowFields(eval);

		isModified = false;
		
		toggleEditMode(true);
		toggleEditModeBtn.setValue(true);
		toggleEditModeBtn.setVisible(true);
		
		printDeviceBtn.setVisible(canPrint);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, 330);
		theWindow.forceLayout();
	}
	
	private void toggleEditMode(boolean editMode) {
		employee.setEnabled(editMode);
		field.setEnabled(editMode);
		addFieldBtn.setEnabled(editMode);
		certNum.setEnabled(editMode);
		lastEvalDate.setEnabled(editMode);
		nextEvalDate.setEnabled(editMode);
		attachCont.setEditMode(editMode);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование информации о проведённой аттестации");
		else
			setHeadingText("Просмотр информации о проведённой аттестации");
		saveBtn.setEnabled(false);
		
		theWindow.forceLayout();
	}
	
	private void updatePosition() {
		if (employee.getCurrentValue() == null) 
			position.setValue(null);
		else
			position.setValue(employee.getCurrentValue().getPositionName());
	}
}
