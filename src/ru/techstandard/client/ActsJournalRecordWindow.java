package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.ClientProps;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.ContractProps;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.ActsJournalRecord;
import ru.techstandard.shared.ActsJournalService;
import ru.techstandard.shared.ActsJournalServiceAsync;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.ContractService;
import ru.techstandard.shared.ContractServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;

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
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ActsJournalRecordWindow extends Window {
	private int action; 
	private boolean isModified;

	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final ActsJournalServiceAsync actsJournalService = GWT.create(ActsJournalService.class);
	private final ContractServiceAsync contractService = GWT.create(ContractService.class);
	private final Images IMAGES = GWT.create(Images.class);
	
	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel actDocsPanel;
	ToggleButton toggleEditModeBtn;
	TextButton printDeviceBtn;
	TabItemConfig actDataTabConfig;
	TabItemConfig actDocsTabConfig;
	
	TextField workNum;
	DateField workDate;
	ComboBox<Client> clientName;
	ComboBox<Contract> contract;
	TextField workSubj;
	TextField responsible;
	ComboBox<DictionaryRecord> objType;
	TextButton addObjTypeBtn;
	TextField objName;
	TextField objFNum;
	TextField objRNum;
	DateField nextWorkDate;
	CheckBox completed;
	TextArea notes;
	List<Widget> validityChecks;
	
	final TextButton saveBtn;
	final TextButton cancelBtn;
	
	ActsJournalRecord record;
	private int currentContractId;
	private AttachmentsContainer attachCont;
	private boolean canClose=false;
	
	public ActsJournalRecordWindow() {
		super();
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр записи журнала экспертиз/актов");
		
		theWindow = this;
		
		final ChangeHandler formChangedHandler = new ChangeHandler()  {
			@Override
			public void onChange(ChangeEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};
		
		final ChangeHandler clientComboChangedHandler = new ChangeHandler()  {
			@Override
			public void onChange(ChangeEvent event) {
				System.out.println("ChangeEvent fired");
				isModified = true;
				currentContractId = 0;
				loadContracts();
				checkFormValidity();
			}
		};
		
		final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};

		VerticalLayoutContainer actTopContainer = new VerticalLayoutContainer();
		this.setWidget(actTopContainer);
		
		ToolBar toolBar = new ToolBar();
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
				actsJournalService.getPrintableActCard((Integer) theWindow.getData("recordID"), new AsyncCallback<String>() {
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
		
		actTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		TabPanel panel = new TabPanel();
		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer actInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(actInfoContainer);		
		
		int labelColWidth = 180;
		workNum = new TextField();
		workNum.setAllowBlank(false);
		FieldLabel workNumLabel = new FieldLabel(workNum, "Номер акта, экспертизы");
		workNumLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(workNumLabel, new VerticalLayoutData(1, -1));
		workNum.addKeyUpHandler(fieldKeyUpHandler);

		workDate = new DateField();
		workDate.setAllowBlank(false);
		workDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
		
		FieldLabel workDateLabel = new FieldLabel(workDate, "Дата проведения экспертизы");
		workDateLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(workDateLabel, new VerticalLayoutData(1, -1));
		workDate.addChangeHandler(formChangedHandler);
		workDate.addKeyUpHandler(fieldKeyUpHandler);
		workDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});

		ClientProps clientProps = GWT.create(ClientProps.class);
	    final ListStore<Client> clientsStore = new ListStore<Client>(clientProps.id());
	    
	    clientService.getClientsByActualness(true,  
				new AsyncCallback<List<Client>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. ActsIW:224 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Client> result) {
						clientsStore.addAll(result);
					}
				});
	 
	    clientName = new ComboBox<Client>(clientsStore, clientProps.nameLabel());
	    clientName.setAllowBlank(false);
	    clientName.setForceSelection(true);
	    clientName.setTriggerAction(TriggerAction.ALL);
	    clientName.setMinListWidth(290);

		FieldLabel clientNameLabel = new FieldLabel(clientName, "Контрагент");
		clientNameLabel.setHeight(23);
		clientNameLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(clientNameLabel, new VerticalLayoutData(1, -1));
		clientName.addChangeHandler(clientComboChangedHandler);
		clientName.addKeyUpHandler(fieldKeyUpHandler);
		clientName.addSelectionHandler(new SelectionHandler<Client>() {
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
		contract.setAllowBlank(false);
		contract.setForceSelection(true);
		contract.setTriggerAction(TriggerAction.ALL);
		contract.setWidth(290);
		contract.setMinListWidth(290);
		
		FieldLabel contractLabel = new FieldLabel(contract, "Договор");
		contractLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(contractLabel, new VerticalLayoutData(1, -1));
		contract.addChangeHandler(formChangedHandler);
		contract.addSelectionHandler(new SelectionHandler<Contract>() {
			@Override
			public void onSelection(SelectionEvent<Contract> event) {
				isModified = true;
//				contract.clearInvalid();
				updateContractFields();
				checkFormValidity();
			}
		});
		
	    workSubj = new TextField();
		FieldLabel workSubjLabel = new FieldLabel(workSubj, "Предмет договора");
		workSubjLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(workSubjLabel, new VerticalLayoutData(1, -1));
		workSubj.setEnabled(false);
		
		responsible = new TextField();
		FieldLabel responsibleLabel = new FieldLabel(responsible, "Ответственный");
		responsibleLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(responsibleLabel, new VerticalLayoutData(1, -1));
		responsible.setEnabled(false);
	
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
		final ListStore<DictionaryRecord> objTypeStore = new ListStore<DictionaryRecord>(props.key());
		dictionaryService.getDictionaryContents(Constants.DICT_OBJTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. ActsIW:296 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						objTypeStore.addAll(result);
					}
				});
	 
		objType = new ComboBox<DictionaryRecord>(objTypeStore, props.nameLabel());
	    objType.setAllowBlank(false);
	    objType.setForceSelection(true);
	    objType.setTriggerAction(TriggerAction.ALL);
	    objType.setMinListWidth(260);
	    
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
		actInfoContainer.add(objTypeLabel, new VerticalLayoutData(1, -1));
		objType.addChangeHandler(formChangedHandler);
		objType.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				isModified = true;
//				contract.clearInvalid();
				checkFormValidity();
			}
		});
		
		objName = new TextField();
		objName.setAllowBlank(false);
		FieldLabel objNameLabel = new FieldLabel(objName, "Марка объекта");
		objNameLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(objNameLabel, new VerticalLayoutData(1, -1));
		objName.addKeyUpHandler(fieldKeyUpHandler);
		
		objFNum = new TextField();
		FieldLabel objFNumLabel = new FieldLabel(objFNum, "Заводской №");
		objFNumLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(objFNumLabel, new VerticalLayoutData(1, -1));
		objFNum.setAllowBlank(true);
		objFNum.addKeyUpHandler(fieldKeyUpHandler);
		
		objRNum = new TextField();
		FieldLabel objRNumLabel = new FieldLabel(objRNum, "Регистрационный №");
		objRNumLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(objRNumLabel, new VerticalLayoutData(1, -1));
		objRNum.setAllowBlank(true);
		objRNum.addKeyUpHandler(fieldKeyUpHandler);
		
		nextWorkDate = new DateField();
		nextWorkDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
		FieldLabel nextWorkDateLabel = new FieldLabel(nextWorkDate, "Дата повторной экспертизы");
		nextWorkDateLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(nextWorkDateLabel, new VerticalLayoutData(1, -1));
		nextWorkDate.addChangeHandler(formChangedHandler);
		nextWorkDate.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});
		
		completed = new CheckBox();
		FieldLabel completedLabel = new FieldLabel(completed, "Завершено");
		completedLabel.setLabelWidth(labelColWidth);
		actInfoContainer.add(completedLabel, new VerticalLayoutData(1, -1));
		completed.addChangeHandler(formChangedHandler);
		
		notes = new TextArea();
	    notes.setAllowBlank(true);
	    FieldLabel notesLabel = new FieldLabel(notes, "Примечания");
	    notesLabel.setLabelWidth(labelColWidth);
	    actInfoContainer.add(notesLabel, new VerticalLayoutData(1, 60));
	    notes.addChangeHandler(formChangedHandler);
	    notes.addKeyUpHandler(fieldKeyUpHandler);
		
		// документы
		actDocsPanel = new ContentPanel();
		actDocsPanel.setHeaderVisible(false);
		actDocsPanel.setBorders(false);
		actDocsPanel.setHeight(395);

		attachCont = new AttachmentsContainer(Constants.ACT_ATTACHMENTS);
		actDocsPanel.setWidget(attachCont);

		// добавляем табы в контейнер 
		
		actDataTabConfig = new TabItemConfig("Данные акта/экспертизы");
		actDataTabConfig.setIcon(IMAGES.view());

		actDocsTabConfig = new TabItemConfig("Связанная документация");
		actDocsTabConfig.setIcon(IMAGES.documents());

		panel.add(infoPanel, actDataTabConfig);
		panel.add(actDocsPanel, actDocsTabConfig);

		actTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		
		validityChecks = new ArrayList<Widget>();
		
		validityChecks.add(workNum);
		validityChecks.add(contract);
		validityChecks.add(workDate);
		validityChecks.add(clientName);
		validityChecks.add(objType);
		validityChecks.add(objName);
		validityChecks.add(nextWorkDate);
		
		this.setButtonAlign(BoxLayoutPack.END);
		
		saveBtn = new TextButton("Сохранить");
		saveBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				ActsJournalRecord rec = new ActsJournalRecord();
				rec.setId((Integer) theWindow.getData("recordID"));
				rec.setContractId(contract.getValue().getId());
				rec.setWorkNum(workNum.getValue());
				rec.setWorkDate(workDate.getValue());
				rec.setObjTypeId(objType.getValue().getId());
				rec.setObjName(objName.getValue());
				rec.setObjFNum(objFNum.getValue());
				rec.setObjRNum(objRNum.getValue());
				rec.setNextWorkDate(nextWorkDate.getValue());
				rec.setCompleted(completed.getValue()?"1":"0");
				rec.setNotes(notes.getValue());
				actsJournalService.updateJournalRecord(rec, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить запись в журнал.");
						d.show();
					}
					@Override
					public void onSuccess(Boolean result) {
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
						canClose=true;
						theWindow.setData("hideButton", "save");
	    				theWindow.hide();
					}
				});
			}
		});
		this.addButton(saveBtn);
		
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
			actsJournalService.deleteJournalRecord((Integer) theWindow.getData("recordID"), false, new AsyncCallback<Boolean>() {
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
	
	public void editRecord(ActsJournalRecord record, boolean newRecord, boolean canPrint) {
		action = newRecord?Constants.ACTION_ADD:Constants.ACTION_EDIT;
		fillFormFields(record);
		canClose=false;
		
		theWindow.setHeadingText("Редактирование записи журнала экспертиз/актов");
		isModified = false;
		
		toggleEditMode(true);
		toggleEditModeBtn.setValue(true);
		toggleEditModeBtn.setVisible(true);
		checkFormValidity();
		
		printDeviceBtn.setVisible(canPrint);
		
		this.setPixelSize(500, 540);
		this.forceLayout();
		this.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		isModified = false;
		
		workNum.focus();
	}
	
	public void displayRecord(ActsJournalRecord record, boolean isEditable, boolean canPrint) {
		fillFormFields(record);
		updateCombos();
		canClose=false;
		
		theWindow.setHeadingText("Просмотр записи журнала экспертиз/актов");
		isModified = false;
		
		toggleEditMode(false);
		toggleEditModeBtn.setValue(false);
		toggleEditModeBtn.setVisible(isEditable);

		printDeviceBtn.setVisible(canPrint);
		
		this.setPixelSize(500, 540);
		this.forceLayout();
		this.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
	}
	
	private void fillFormFields(ActsJournalRecord record) {
		this.setData("recordID", record.getId());
		this.record = record;
		updateCombos();
		
		workNum.setValue(record.getWorkNum());
		workNum.clearInvalid();
		workDate.setValue(record.getWorkDate());
		workDate.clearInvalid();
		
		objName.setValue(record.getObjName());
		objName.clearInvalid();
		objFNum.setValue(record.getObjFNum());
		objFNum.clearInvalid();
		objRNum.setValue(record.getObjRNum());
		objRNum.clearInvalid();
		nextWorkDate.setValue(record.getNextWorkDate());
		nextWorkDate.clearInvalid();
		completed.setValue(record.getCompleted().equalsIgnoreCase("Да"));
		notes.setValue(record.getNotes());

		attachCont.init(record.getId());
		
		checkFormValidity();
	}
	
	private void updateCombos() {
	    clientService.getClientsByActualness(true,  
				new AsyncCallback<List<Client>>() {
					public void onFailure(Throwable caught) {
						System.out.println("updatecombos client fail: "+caught.getMessage());
					}
					@Override
					public void onSuccess(List<Client> result) {
						clientName.getStore().replaceAll(result);
						List<Client> clients = clientName.getStore().getAll();
						clientName.clear();
						for (int i=0; i<clients.size(); i++){
							if(clients.get(i).getName().equals(record.getClientName())) {
								clientName.setValue(clients.get(i));
								break;
							}
						}
						clientName.clearInvalid();
						currentContractId = record.getContractId();
						loadContracts();
					}
				});
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_OBJTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						System.out.println("updatecombos objtype fail: "+caught.getMessage());
					}

					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						objType.getStore().replaceAll(result);
						List<DictionaryRecord> types = objType.getStore().getAll();
						objType.clear();
						for (int i=0; i<types.size(); i++){
							if(types.get(i).getName().equals(record.getObjType())) {
								objType.setValue(types.get(i));
								break;
							}
						}
						objType.clearInvalid();
					}
				});
	}
	
	private void loadContracts() {
		final ListStore<Contract> contractStore = contract.getStore();
		if (clientName.getCurrentValue() == null) {
			contract.setValue(null);
			contract.forceInvalid("Необходимо заполнить");
			contractStore.clear();
			updateContractFields();
			return;
		}
		
		contractService.getContractsForClient(clientName.getCurrentValue().getId(),  
				new AsyncCallback<List<Contract>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. ActsIW:689 - "+caught.getMessage());
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
	
	private void updateContractFields() {
		if (contract.getCurrentValue() == null) {
			workSubj.setValue(null);
			responsible.setValue(null);
		} else {
			workSubj.setValue(contract.getCurrentValue().getWorkSubj());
			responsible.setValue(contract.getCurrentValue().getResponsibleName());
		}
	}
	
	private void toggleEditMode(boolean editMode) {
		workNum.setEnabled(editMode);
		workDate.setEnabled(editMode);
		clientName.setEnabled(editMode);
		contract.setEnabled(editMode);
		objType.setEnabled(editMode);
		addObjTypeBtn.setEnabled(editMode);
		objName.setEnabled(editMode);
		objFNum.setEnabled(editMode);
		objRNum.setEnabled(editMode);
		nextWorkDate.setEnabled(editMode);
		completed.setEnabled(editMode);
		notes.setEnabled(editMode);

		attachCont.setEditMode(editMode);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование записи журнала экспертиз/актов");
		else
			setHeadingText("Просмотр записи журнала экспертиз/актов");
		
		theWindow.forceLayout();
	}
}


