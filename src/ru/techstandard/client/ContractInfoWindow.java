package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.ClientProps;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Contract;
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
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
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
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ContractInfoWindow extends Window {
	private final Images IMAGES = GWT.create(Images.class);

	TabItemConfig contractDataTabConfig;
	TabItemConfig contractDocsTabConfig;
	
	TextField contractId;
	ComboBox<Client> client;
	TextField contractNum;
	DateField signedDate;
	ComboBox<DictionaryRecord> contractSubj;
	TextButton addContractSubjBtn;
	ComboBox<Employee> responsibleEmpl;
	DateField expiryDate;
	Radio inWorkRadio;
    Radio closedRadio;
    ToggleGroup statusToggle;
	TextArea notes;
	List<Widget> validityChecks;
	
	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel contractDocsPanel;
	ToolBar topToolBar;
	ToggleButton toggleEditModeBtn;
	TextButton printContratBtn;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private boolean isModified;
	private int action=0;
	private AttachmentsContainer attachCont;
	private boolean canClose=false;
	
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final ContractServiceAsync contractService = GWT.create(ContractService.class);
	
	public ContractInfoWindow() {
		super();
		this.setPixelSize(500, 310);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр информации о договоре");
		
		theWindow = this;
		
		VerticalLayoutContainer contractInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(contractInfoTopContainer);
		
		ToolBar toolBar = new ToolBar();
		toolBar.setBorders(true);
				
		toggleEditModeBtn = new ToggleButton("Режим редактирования");
		toggleEditModeBtn.setIcon(IMAGES.edit());
		toolBar.add(toggleEditModeBtn);
		toggleEditModeBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ToggleButton btn = (ToggleButton) event.getSource();
				toggleEditMode(btn.getValue(), false);
			}
		});
		
		printContratBtn = new TextButton("Печать");
		printContratBtn.setIcon(IMAGES.print());
		printContratBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				contractService.getPrintableContractCard(Integer.valueOf(contractId.getText()), new AsyncCallback<String>() {
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
		toolBar.add(printContratBtn);
		
		contractInfoTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		TabPanel panel = new TabPanel();

		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer contractInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(contractInfoContainer);
		
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
		contractId = new TextField();
		
		ClientProps clientProps = GWT.create(ClientProps.class);
	    final ListStore<Client> clientStore = new ListStore<Client>(clientProps.id());
	    
	    clientService.getAllClients(new AsyncCallback<List<Client>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. ContractIW:198 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Client> result) {
						clientStore.addAll(result);
					}
				});
	 
	    client = new ComboBox<Client>(clientStore, clientProps.nameLabel());
	    client.addValueChangeHandler(new ValueChangeHandler<Client>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<Client> event) {
	    	  isModified = true;
	    	  checkFormValidity();
	      }
	    });
	    client.addSelectionHandler(new SelectionHandler<Client>() {
			@Override
			public void onSelection(SelectionEvent<Client> event) {
				isModified = true;
				checkFormValidity();
			}
		});
	    client.addKeyUpHandler(fieldKeyUpHandler);
	    client.setAllowBlank(true);
	    client.setForceSelection(true);
	    client.setTriggerAction(TriggerAction.ALL);
	    FieldLabel clientLabel = new FieldLabel(client, "Контрагент");
	    clientLabel.setLabelWidth(labelColWidth);
	    contractInfoContainer.add(clientLabel, new VerticalLayoutData(1, -1));
		
		contractNum = new TextField();
		contractNum.setAllowBlank(false);
		contractNum.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel numLabel = new FieldLabel(contractNum, "Номер договора");
		numLabel.setLabelWidth(labelColWidth);
		contractInfoContainer.add(numLabel, new VerticalLayoutData(1, -1));
		
		signedDate = new DateField();
		signedDate.setAllowBlank(false);
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
	    		isModified = true;
	    		checkFormValidity();
	    	}
	    });
	    signedDate.addKeyUpHandler(fieldKeyUpHandler);
	    signedDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});
	    FieldLabel signedDateLabel = new FieldLabel(signedDate, "Дата заключения");
	    signedDateLabel.setLabelWidth(labelColWidth);
	    contractInfoContainer.add(signedDateLabel, new VerticalLayoutData(1, -1));

		DictionaryRecordProps workSubjProps = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> workSubjStore = new ListStore<DictionaryRecord>(workSubjProps.key());
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_WORKSUBJS,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. ContractIW:272 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						workSubjStore.addAll(result);
					}
				});
	 
	    contractSubj = new ComboBox<DictionaryRecord>(workSubjStore, workSubjProps.nameLabel());
	    contractSubj.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
	    	  isModified = true;
	    	  checkFormValidity();
	      }
	    });
	    contractSubj.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();
			}
		});
	    contractSubj.addKeyUpHandler(fieldKeyUpHandler);
	    contractSubj.setAllowBlank(false);
	    contractSubj.setForceSelection(true);
	    contractSubj.setTriggerAction(TriggerAction.ALL);
	    
	    addContractSubjBtn = new TextButton("");
	    addContractSubjBtn.setIcon(Images.INSTANCE.add());
	    addContractSubjBtn.setToolTip("Добавить предмет договора");
	    addContractSubjBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final PromptMessageBox box = new PromptMessageBox("Предмет договора", "Введите новое значение для справочника<br>предметов договоров:");
	          box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
	            	if (event.getHideButton() == PredefinedButton.CANCEL) {
						return;
					}
	            	if (box.getValue() == null || box.getValue().trim().isEmpty()) {
	            		MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите предмет договора.");
	            		d.setIcon(Images.INSTANCE.information());
	            		d.show();
						return;
	            	}
	            	dictionaryService.addRecord(Constants.DICT_WORKSUBJS, box.getValue().trim(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							DictionaryRecord rec = new DictionaryRecord(result, box.getValue().trim());
							workSubjStore.add(rec);
							contractSubj.setValue(rec);
						}
					});
	            }
	          });
	          box.show();
	        }
	      });
	    HorizontalLayoutContainer subjHLC = new HorizontalLayoutContainer();
	    subjHLC.add(contractSubj, new HorizontalLayoutData(1, 1));
	    subjHLC.add(addContractSubjBtn, new HorizontalLayoutData(-1, -1));
	    
	    
	    FieldLabel subjLabel = new FieldLabel(subjHLC, "Предмет договора");
	    subjLabel.setLabelWidth(labelColWidth);
	    subjLabel.setHeight(23);
	    contractInfoContainer.add(subjLabel, new VerticalLayoutData(1, -1));
		
//	    TODO может понадобиться специальная обработка на случай смены ответственного
	    EmployeeProps employeeProps = GWT.create(EmployeeProps.class);
	    ListStore<Employee> employeeStore = new ListStore<Employee>(employeeProps.id());
	    
	    responsibleEmpl = new ComboBox<Employee>(employeeStore, employeeProps.nameLabel());
	    responsibleEmpl.addValueChangeHandler(new ValueChangeHandler<Employee>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<Employee> event) {
		    	  isModified = true;
		    	  checkFormValidity();
		      }
		    });
	    responsibleEmpl.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
				isModified = true;
				checkFormValidity();				
			}
		});
	    responsibleEmpl.addKeyUpHandler(fieldKeyUpHandler);
	    responsibleEmpl.setAllowBlank(false);
	    responsibleEmpl.setForceSelection(true);
	    responsibleEmpl.setTriggerAction(TriggerAction.ALL);
		FieldLabel responsibleLabel = new FieldLabel(responsibleEmpl, "Ответственный");
		responsibleLabel.setLabelWidth(labelColWidth);
		contractInfoContainer.add(responsibleLabel, new VerticalLayoutData(1, -1));
		
		expiryDate = new DateField();
		expiryDate.setAllowBlank(false);
	    expiryDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    expiryDate.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
		
		FieldLabel expiryDateLabel = new FieldLabel(expiryDate, "Срок истечения договора");
		expiryDateLabel.setLabelWidth(labelColWidth);
		contractInfoContainer.add(expiryDateLabel, new VerticalLayoutData(1, -1));
		expiryDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
	    		checkFormValidity();
	    	}
	    });
		expiryDate.addKeyUpHandler(fieldKeyUpHandler);
		expiryDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});
	    
		inWorkRadio = new Radio();
	    inWorkRadio.setBoxLabel("Исполняемый");
	 
	    closedRadio = new Radio();
	    closedRadio.setBoxLabel("Закрытый");
	    closedRadio.setValue(true);
	 
	    HorizontalPanel hp = new HorizontalPanel();
	    hp.add(inWorkRadio);
	    hp.add(closedRadio);
	 
	    FieldLabel statusLabel = new FieldLabel(hp, "Статус");
	    statusLabel.setLabelWidth(labelColWidth);
	    contractInfoContainer.add(statusLabel);
	 
	    statusToggle = new ToggleGroup();
	    statusToggle.add(inWorkRadio);
	    statusToggle.add(closedRadio);
	    statusToggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
			@Override
			public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		 
		 notes = new TextArea();
		 notes.setAllowBlank(true);
		 //		    notes.addValidator(new MinLengthValidator(10));
		 FieldLabel notesLabel = new FieldLabel(notes, "Примечания");
		 notesLabel.setLabelWidth(labelColWidth);
		 contractInfoContainer.add(notesLabel, new VerticalLayoutData(1, 60));
		 notes.addChangeHandler(formChangedHandler);
		 
		 validityChecks = new ArrayList<Widget>();

		 validityChecks.add(client);
		 validityChecks.add(contractNum);
		 validityChecks.add(signedDate);
		 validityChecks.add(contractSubj);
		 validityChecks.add(responsibleEmpl);
		 validityChecks.add(expiryDate);
		
		// документы
		contractDocsPanel = new ContentPanel();
		contractDocsPanel.setHeaderVisible(false);
		contractDocsPanel.setBorders(false);
		contractDocsPanel.setHeight(270);
		
		attachCont = new AttachmentsContainer(Constants.CONTRACT_ATTACHMENTS);
		contractDocsPanel.setWidget(attachCont);
				
		// *** Добавление табов в контейнер

		contractDataTabConfig = new TabItemConfig("Данные договора");
		contractDataTabConfig.setIcon(IMAGES.view());

		contractDocsTabConfig = new TabItemConfig("Связанная документация");
		contractDocsTabConfig.setIcon(IMAGES.documents());

		panel.add(infoPanel, contractDataTabConfig);
		panel.add(contractDocsPanel, contractDocsTabConfig);
		

		contractInfoTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		this.setButtonAlign(BoxLayoutPack.END);
		
		saveBtn = new TextButton("Сохранить");
		saveBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Contract contract = new Contract(Integer.valueOf(contractId.getValue()));
				contract.setClientID(client.getValue().getId());
				contract.setSubjID(contractSubj.getValue().getId());
				contract.setResponsibleID(responsibleEmpl.getValue().getId());
				contract.setNum(contractNum.getValue());
				contract.setSigned(signedDate.getValue());
				contract.setExpires(expiryDate.getValue());
				contract.setClosed(closedRadio.getValue());
				contract.setNotes(notes.getValue());
//				При добавлении нового договора пустая запись уже была создана, её нужно только обновить.
				contractService.updateContract(contract, new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						System.out.println("update succ, res="+result);
//						Если создаём новую запись - нужно сообщить вызывающему коду, что мы создали.
						if (action == Constants.ACTION_ADD) {
							theWindow.setData("newContractID", contractId.getValue());
						}
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
						canClose=true;
						theWindow.setData("hideButton", "save");
						theWindow.hide();
					}
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить информацию о договоре.");
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
					box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
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
			contractService.deleteContract(Integer.valueOf(contractId.getValue()), false, new AsyncCallback<Boolean>() {
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
	
	private void fillWindowFields(Contract contract) {
		contractId.setValue(String.valueOf(contract.getId()));
		
		updateCombos(contract);
		
		contractNum.setValue(contract.getNum());
		signedDate.setValue(contract.getSigned());
		
		expiryDate.setValue(contract.getExpires());
		if (contract.getStatus().equals("Закрытый"))
			statusToggle.setValue(closedRadio);
		else
			statusToggle.setValue(inWorkRadio);
		
		notes.setValue(contract.getNotes());
		
		attachCont.init(contract.getId());
	}
	
	public void displayInfo(Contract contract, boolean isEditable, boolean canPrint) {
//		System.out.println("ContractIW.displayInfo:"+contract);
		action = -1;
		canClose=false;
		fillWindowFields(contract);

		isModified = false;	
		
		toggleEditMode(false, false);
		toggleEditModeBtn.setValue(false);
		toggleEditModeBtn.setVisible(isEditable);

		printContratBtn.setVisible(canPrint);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, 410);
		theWindow.forceLayout();
	}

	public void editInfo(Contract contract, boolean newContract, boolean canPrint) {
		 editInfo(contract, newContract, false, canPrint);
	}
	
	public void editInfo(Contract contract, boolean newContract, boolean fixClient, boolean canPrint) {
//		System.out.println("ContractIW.editInfo:"+contract);
		action = newContract ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		fillWindowFields(contract);

		isModified = false;
		
		toggleEditMode(true, fixClient);
		toggleEditModeBtn.setValue(true);
		toggleEditModeBtn.setVisible(true);
		
		printContratBtn.setVisible(canPrint);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, 410);
		theWindow.forceLayout();
	}
	
	private void toggleEditMode(boolean editMode, boolean fixClient) {
		client.setEnabled(editMode && !fixClient);
		contractNum.setEnabled(editMode);
		signedDate.setEnabled(editMode);
		contractSubj.setEnabled(editMode);
		addContractSubjBtn.setEnabled(editMode);
		responsibleEmpl.setEnabled(editMode);
		expiryDate.setEnabled(editMode);
		inWorkRadio.setEnabled(editMode);
	    closedRadio.setEnabled(editMode);
		notes.setEnabled(editMode);
		
		attachCont.setEditMode(editMode);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование информации о договоре");
		else
			setHeadingText("Просмотр информации о договоре");
		saveBtn.setEnabled(false);
		
		theWindow.forceLayout();
	}
	
	private void updateCombos(final Contract contract) {
	    clientService.getAllClients(  
				new AsyncCallback<List<Client>>() {
					public void onFailure(Throwable caught) {
						System.out.println("updatecombos client fail: "+caught.getMessage());
					}
					@Override
					public void onSuccess(List<Client> result) {
						client.getStore().replaceAll(result);
						
						List<Client> clients = client.getStore().getAll();
						client.clear();
						for (int i=0; i<clients.size(); i++) {
							if(clients.get(i).getName().equals(contract.getClientName())) {
								client.setValue(clients.get(i));
								break;
							}
						}
						client.clearInvalid();
					}
				});
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_WORKSUBJS,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						System.out.println("updatecombos objtype fail: "+caught.getMessage());
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						contractSubj.getStore().replaceAll(result);
						
						List<DictionaryRecord> subjs = contractSubj.getStore().getAll();
						contractSubj.clear();
						for (int i=0; i<subjs.size(); i++) {
							if(subjs.get(i).getName().equals(contract.getWorkSubj())) {
								contractSubj.setValue(subjs.get(i));
								break;
							}
						}
						contractSubj.clearInvalid();
					}
				});
	    
	    employeeService.getAllEmployees(new AsyncCallback<List<Employee>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. ContractIW:720 - "+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<Employee> result) {
				responsibleEmpl.getStore().replaceAll(result);
				
				List<Employee> emps = result;
				responsibleEmpl.clear();
				for (int i=0; i<emps.size(); i++) {
					if(emps.get(i).getName().equals(contract.getResponsibleName())) {
						responsibleEmpl.setValue(emps.get(i));
						break;
					}
				}
				responsibleEmpl.clearInvalid();
 			}
		});
	}
}
