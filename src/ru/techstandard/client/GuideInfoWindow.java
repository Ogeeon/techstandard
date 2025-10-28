package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.ActsJournalRecord;
import ru.techstandard.client.model.ActsJournalRecordProps;
import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.ClientProps;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.ContractProps;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.client.model.Guide;
import ru.techstandard.shared.ActsJournalService;
import ru.techstandard.shared.ActsJournalServiceAsync;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.ContractService;
import ru.techstandard.shared.ContractServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.GuideService;
import ru.techstandard.shared.GuideServiceAsync;

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
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class GuideInfoWindow extends Window {
	private final Images IMAGES = GWT.create(Images.class);

	TabItemConfig guideDataTabConfig;
	TabItemConfig guideDocsTabConfig;
	
	TextField guideId;
	TextField objectName;
	ComboBox<DictionaryRecord> objType;
	TextButton addObjTypeBtn;
	TextField objFNum;
	TextField objRNum;
	ComboBox<Client> client;
	ComboBox<Contract> contract;
	TextField workSubj;
	ComboBox<ActsJournalRecord> act;
	ComboBox<Employee> responsibleEmpl;
	DateField dueDate;
	TextArea notes;
	List<Widget> validityChecks;
	
	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel guideDocsPanel;
	ToolBar topToolBar;
	ToggleButton toggleEditModeBtn;
	TextButton printGuideBtn;
	
	boolean canEdit;
	boolean canPrint;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private  boolean isModified;
	private boolean canClose=false;
	private int action=0;
	private AttachmentsContainer attachCont;
	
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final GuideServiceAsync guideService = GWT.create(GuideService.class);
	private final ContractServiceAsync contractService = GWT.create(ContractService.class);
	private final ActsJournalServiceAsync journalService = GWT.create(ActsJournalService.class);
	
	private int currentContractId;
	private int currentActId;
	private int wndHeight=485;
	
	public GuideInfoWindow() {
		super();
		this.setPixelSize(500, wndHeight);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр информации о руководстве/паспорте");
		
		theWindow = this;
		
		VerticalLayoutContainer guideInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(guideInfoTopContainer);
		
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
		
		printGuideBtn = new TextButton("Печать");
		printGuideBtn.setIcon(IMAGES.print());
		printGuideBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				guideService.getPrintableGuideCard(Integer.valueOf(guideId.getText()), new AsyncCallback<String>() {
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
		toolBar.add(printGuideBtn);
		
		guideInfoTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		TabPanel panel = new TabPanel();

		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer guideInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(guideInfoContainer);
		
		final ChangeHandler formChangedHandler = new ChangeHandler()  {
			@Override
			public void onChange(ChangeEvent event) {
				isModified = true;
				updateContractFields();
				// currentActId нужен для первоначальной загрузки данных, потом его можно обнулить
				currentActId = 0;
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
		guideId = new TextField();
		
		objectName = new TextField();
		objectName.setAllowBlank(false);
		FieldLabel objectNameLabel = new FieldLabel(objectName, "Наименование объекта");
		objectNameLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(objectNameLabel, new VerticalLayoutData(1, -1));
		objectName.addKeyUpHandler(fieldKeyUpHandler);
		
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
		final ListStore<DictionaryRecord> objTypeStore = new ListStore<DictionaryRecord>(props.key());
		
		objType = new ComboBox<DictionaryRecord>(objTypeStore, props.nameLabel());
	    objType.setAllowBlank(false);
	    objType.setForceSelection(true);
	    objType.setTriggerAction(TriggerAction.ALL);
	    
	    addObjTypeBtn = new TextButton("");
	    addObjTypeBtn.setIcon(Images.INSTANCE.add());
	    addObjTypeBtn.setToolTip("Добавить тип объекта");
	    addObjTypeBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final PromptMessageBox box = new PromptMessageBox("Тип объекта", "Введите новое значение для справочника<br>типов объектов:");
	          box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
					if (event.getHideButton() == PredefinedButton.CANCEL) {
						return;
					}
	            	if (box.getValue() == null || box.getValue().trim().isEmpty()) {
	            		MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите краткое название типа объекта.");
	            		d.setIcon(Images.INSTANCE.information());
						d.show();
						return;
	            	}
	            	dictionaryService.addRecord(Constants.DICT_OBJTYPES, box.getValue().trim(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							DictionaryRecord rec = new DictionaryRecord(result, box.getValue().trim());
							objTypeStore.add(rec);
							objType.setValue(rec);
							isModified = true;
							checkFormValidity();
						}
					});
	            }
	          });
	          box.show();
	        }
	      });
	    
	    HorizontalLayoutContainer objTypeHLC = new HorizontalLayoutContainer();
	    objTypeHLC.add(objType, new HorizontalLayoutData(1, 1));
	    objTypeHLC.add(addObjTypeBtn, new HorizontalLayoutData(-1, -1));
	    
		FieldLabel objTypeLabel = new FieldLabel(objTypeHLC, "Тип объекта");
		objTypeLabel.setLabelWidth(labelColWidth);
		objTypeLabel.setHeight(23);
		guideInfoContainer.add(objTypeLabel, new VerticalLayoutData(1, -1));
		objType.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
			@Override
			public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();				
			}
		});
		objType.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		
		objFNum = new TextField();
		objFNum.setAllowBlank(true);
		FieldLabel objFNumLabel = new FieldLabel(objFNum, "Заводской номер");
		objFNumLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(objFNumLabel, new VerticalLayoutData(1, -1));
		objFNum.addKeyUpHandler(fieldKeyUpHandler);
		
		objRNum = new TextField();
		objRNum.setAllowBlank(true);
		FieldLabel objRNumLabel = new FieldLabel(objRNum, "Регистрационный номер");
		objRNumLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(objRNumLabel, new VerticalLayoutData(1, -1));
		objRNum.addKeyUpHandler(fieldKeyUpHandler);
		
		ClientProps clientProps = GWT.create(ClientProps.class);
	    final ListStore<Client> clientStore = new ListStore<Client>(clientProps.id());
	 
	    client = new ComboBox<Client>(clientStore, clientProps.nameLabel());
	    client.addValueChangeHandler(new ValueChangeHandler<Client>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<Client> event) {
	    	  isModified = true;
	    	  currentContractId = 0;
	    	  currentActId = 0;
	    	  loadContracts();
	    	  checkFormValidity();
	      }
	    });
	    client.addSelectionHandler(new SelectionHandler<Client>() {
			@Override
			public void onSelection(SelectionEvent<Client> event) {
				isModified = true;
				currentContractId = 0;
				currentActId = 0;
				loadContracts();
				checkFormValidity();
			}
		});
	    client.setAllowBlank(false);
	    client.setForceSelection(true);
	    client.setTriggerAction(TriggerAction.ALL);
	    FieldLabel clientLabel = new FieldLabel(client, "Заказчик");
	    clientLabel.setLabelWidth(labelColWidth);
	    guideInfoContainer.add(clientLabel, new VerticalLayoutData(1, -1));
		
	    ContractProps contractProps = GWT.create(ContractProps.class);
	    ListStore<Contract> contractStore = new ListStore<Contract>(contractProps.id());
	    
	    contract = new ComboBox<Contract>(contractStore, contractProps.captionLabel());
		contract.setAllowBlank(false);
		contract.setForceSelection(true);
		contract.setTriggerAction(TriggerAction.ALL);
		
		FieldLabel contractLabel = new FieldLabel(contract, "Договор");
		contractLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(contractLabel, new VerticalLayoutData(1, -1));
		contract.addChangeHandler(formChangedHandler);
		contract.addSelectionHandler(new SelectionHandler<Contract>() {
			@Override
			public void onSelection(SelectionEvent<Contract> event) {
				isModified = true;
				contract.clearInvalid();
				currentActId = 0;
				updateContractFields();
				checkFormValidity();
			}
		});
		
	    workSubj = new TextField();
		FieldLabel workSubjLabel = new FieldLabel(workSubj, "Предмет договора");
		workSubjLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(workSubjLabel, new VerticalLayoutData(1, -1));
		workSubj.setEnabled(false);
	    
		ActsJournalRecordProps actProps = GWT.create(ActsJournalRecordProps.class);
	    ListStore<ActsJournalRecord> actStore = new ListStore<ActsJournalRecord>(actProps.id());
	    
	    act = new ComboBox<ActsJournalRecord>(actStore, actProps.numLabel());
		act.setAllowBlank(true);
		act.setForceSelection(false);
//		act.setTriggerAction(TriggerAction.ALL);
		
		FieldLabel actLabel = new FieldLabel(act, "Экспертиза");
		actLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(actLabel, new VerticalLayoutData(1, -1));
		act.addChangeHandler(formChangedHandler);
		act.addSelectionHandler(new SelectionHandler<ActsJournalRecord>() {
			@Override
			public void onSelection(SelectionEvent<ActsJournalRecord> event) {
				// currentActId нужен для первоначальной загрузки данных, потом его можно обнулить
				currentActId = 0;
				isModified = true;
				checkFormValidity();
			}
		});
		
	    EmployeeProps employeeProps = GWT.create(EmployeeProps.class);
	    final ListStore<Employee> employeeStore = new ListStore<Employee>(employeeProps.id());
	    
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
	    responsibleEmpl.setAllowBlank(false);
	    responsibleEmpl.setForceSelection(true);
	    responsibleEmpl.setTriggerAction(TriggerAction.ALL);
		FieldLabel responsibleLabel = new FieldLabel(responsibleEmpl, "Ответственный");
		responsibleLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(responsibleLabel, new VerticalLayoutData(1, -1));
		
		dueDate = new DateField();
		dueDate.setAllowBlank(false);
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
	    FieldLabel signedDateLabel = new FieldLabel(dueDate, "Срок исполнения");
	    signedDateLabel.setLabelWidth(labelColWidth);
	    guideInfoContainer.add(signedDateLabel, new VerticalLayoutData(1, -1));

	    notes = new TextArea();
	    notes.setAllowBlank(true);
	    FieldLabel notesLabel = new FieldLabel(notes, "Примечания");
	    notesLabel.setLabelWidth(labelColWidth);
	    guideInfoContainer.add(notesLabel, new VerticalLayoutData(1, 60));
	    notes.addChangeHandler(formChangedHandler);

	    // документы
	    guideDocsPanel = new ContentPanel();
	    guideDocsPanel.setHeaderVisible(false);
	    guideDocsPanel.setBorders(false);
	    guideDocsPanel.setHeight(350);

	    attachCont = new AttachmentsContainer(Constants.GUIDE_ATTACHMENTS);
	    guideDocsPanel.setWidget(attachCont);

	    // *** Добавление табов в контейнер

	    guideDataTabConfig = new TabItemConfig("Данные руководства/паспорта");
	    guideDataTabConfig.setIcon(IMAGES.view());

	    guideDocsTabConfig = new TabItemConfig("Связанная документация");
	    guideDocsTabConfig.setIcon(IMAGES.documents());

	    panel.add(infoPanel, guideDataTabConfig);
	    panel.add(guideDocsPanel, guideDocsTabConfig);


	    guideInfoTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));


	    validityChecks = new ArrayList<Widget>();

	    validityChecks.add(objectName);
	    validityChecks.add(objType);
	    validityChecks.add(client);
	    validityChecks.add(contract);
	    validityChecks.add(responsibleEmpl);
	    validityChecks.add(dueDate);

	    this.setButtonAlign(BoxLayoutPack.END);
	    
	    saveBtn = new TextButton("Сохранить");
	    saveBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		Guide guide = new Guide(Integer.valueOf(guideId.getValue()));
	    		guide.setObjName(objectName.getValue());
	    		guide.setObjType(objType.getValue().getId());
	    		guide.setFNum(objFNum.getValue());
	    		guide.setRNum(objRNum.getValue());
	    		guide.setClientId(client.getValue().getId());
	    		guide.setContractId(contract.getValue().getId());
	    		if (act.getValue() != null)
	    			guide.setActId(act.getValue().getId());
	    		guide.setResponsibleId(responsibleEmpl.getValue().getId());
	    		guide.setDueDate(dueDate.getValue());
	    		guide.setNotes(notes.getValue());
	    		//				При добавлении нового документа пустая запись уже была создана, её нужно только обновить.
	    		guideService.updateGuide(guide, new AsyncCallback<Boolean>() {
	    			@Override
	    			public void onSuccess(Boolean result) {
	    				//						Если создаём новую запись - нужно сообщить вызывающему коду, что мы создали.
	    				if (action == Constants.ACTION_ADD) {
	    					theWindow.setData("newGuideID", guideId.getValue());
	    				}
	    				canClose=true;
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
						theWindow.setData("hideButton", "save");
	    				theWindow.hide();
	    			}
	    			@Override
	    			public void onFailure(Throwable caught) {
	    				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить информацию о документе.");
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
			guideService.deleteGuide(Integer.valueOf(guideId.getValue()), false, new AsyncCallback<Boolean>() {
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
	
	private void fillWindowFields(final Guide guide) {
		guideId.setValue(String.valueOf(guide.getId()));
		
		objectName.setValue(guide.getObjName());
		
		dictionaryService.getDictionaryContents(Constants.DICT_OBJTYPES, new AsyncCallback<List<DictionaryRecord>>() {
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. GuideIW:590 - "+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<DictionaryRecord> result) {
				objType.getStore().replaceAll(result);

				List<DictionaryRecord> objTypes = result;
				objType.clear();
				for (int i=0; i<objTypes.size(); i++) {
					if(objTypes.get(i).getName().equals(guide.getObjTypeName())) {
						objType.setValue(objTypes.get(i));
						break;
					}
				}
				objType.clearInvalid();
			}
		});
	 
		
		objFNum.setValue(guide.getFNum());
		objRNum.setValue(guide.getRNum());
		
	    clientService.getAllClients(new AsyncCallback<List<Client>>() {
	    	public void onFailure(Throwable caught) {
	    		AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. GuideIW:615 - "+caught.getMessage());
	    		d.show();
	    	}
	    	@Override
	    	public void onSuccess(List<Client> result) {
	    		client.getStore().replaceAll(result);
	    		List<Client> clients = result;
	    		client.clear();
	    		for (int i=0; i<clients.size(); i++) {
	    			if(clients.get(i).getName().equals(guide.getClientName())) {
	    				client.setValue(clients.get(i));
	    				break;
	    			}
	    		}
	    		client.clearInvalid();

	    		currentContractId = guide.getContractId();
	    		currentActId = guide.getActId();
	    		loadContracts();	    		
	    	}
	    });
		
		
		employeeService.getAllEmployees(new AsyncCallback<List<Employee>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. GuideIW:641 - "+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<Employee> result) {
				responsibleEmpl.getStore().replaceAll(result);
				List<Employee> emps = result;
				responsibleEmpl.clear();
				for (int i=0; i<emps.size(); i++) {
					if(emps.get(i).getName().equals(guide.getResponsibleName())) {
						responsibleEmpl.setValue(emps.get(i));
						break;
					}
				}
				responsibleEmpl.clearInvalid();
 			}
		});
		
		dueDate.setValue(guide.getDueDate());
		notes.setValue(guide.getNotes());
		
		attachCont.init(guide.getId());
	}
	
	public void displayInfo(Guide guide, boolean canEdit, boolean canPrint) {
//		System.out.println("GuideIW.displayInfo:"+guide);
		action = -1;
		canClose=false;
		fillWindowFields(guide);

		isModified = false;	

		this.canEdit = canEdit;
		this.canPrint = canPrint;
		
		toggleEditMode(false);
		toggleEditModeBtn.setValue(false);
		toggleEditModeBtn.setVisible(canEdit);
		
		printGuideBtn.setVisible(canPrint);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, wndHeight);
		theWindow.forceLayout();
	}

	public void editInfo(Guide guide, boolean newGuide, boolean canPrint) {
//		System.out.println("GuideIW.editInfo:"+guide);
		action = newGuide ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		fillWindowFields(guide);

		isModified = false;
		
		toggleEditModeBtn.setVisible(true);
		toggleEditMode(true);
		toggleEditModeBtn.setValue(true);
		
		printGuideBtn.setVisible(canPrint);
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, wndHeight);
		theWindow.forceLayout();
	}
	
	private void toggleEditMode(boolean editMode) {
		objectName.setEnabled(editMode);
		objType.setEnabled(editMode);
		addObjTypeBtn.setEnabled(editMode);
		objFNum.setEnabled(editMode);
		objRNum.setEnabled(editMode);
		client.setEnabled(editMode);
		contract.setEnabled(editMode);
		act.setEnabled(editMode);
		responsibleEmpl.setEnabled(editMode);
		dueDate.setEnabled(editMode);
		notes.setEnabled(editMode);
		
		attachCont.setEditMode(editMode);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование информации о руководстве/паспорте");
		else
			setHeadingText("Просмотр информации о руководстве/паспорте");
		saveBtn.setEnabled(false);
		
		theWindow.forceLayout();
	}
	
	private void updateContractFields() {
		final ListStore<ActsJournalRecord> actStore = act.getStore();
		act.setValue(null);
		if (contract.getCurrentValue() == null) {
			workSubj.setValue(null);
		} else {
			workSubj.setValue(contract.getCurrentValue().getWorkSubj());
			journalService.getJournalRecordsByContract(contract.getCurrentValue().getId(), new AsyncCallback<List<ActsJournalRecord>>() {
				@Override
				public void onFailure(Throwable caught) {
					AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные об экспертизах по договору получены."+caught.getMessage());
					d.show();
				}
				@Override
				public void onSuccess(List<ActsJournalRecord> result) {
					actStore.replaceAll(result);
					
					List<ActsJournalRecord> acts = act.getStore().getAll();
					for (int i=0; i<acts.size(); i++){
						if(acts.get(i).getId() == currentActId) {
							act.setValue(acts.get(i));
							break;
						}
					}
				}
			});
		}
	}
	
	private void loadContracts() {
		final ListStore<Contract> contractStore = contract.getStore();
		if (client.getCurrentValue() == null) {
			contract.setValue(null);
			contract.forceInvalid("Необходимо заполнить");
			contractStore.clear();
			updateContractFields();
			return;
		}
		
		contractService.getContractsForClient(client.getCurrentValue().getId(),  
				new AsyncCallback<List<Contract>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. GuideIW:786 - "+caught.getMessage());
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
						updateContractFields();
						checkFormValidity();
					}
				});
	}
}
