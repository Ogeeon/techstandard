package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Device;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.shared.DeviceService;
import ru.techstandard.shared.DeviceServiceAsync;
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
import com.sencha.gxt.widget.core.client.form.SpinnerField;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;

public class DeviceInfoWindow extends Window {
	private final Images IMAGES = GWT.create(Images.class);

	TabItemConfig deviceDataTabConfig;
	TabItemConfig deviceDocsTabConfig;
	
	TextField deviceId;
	TextField deviceTitle;
	TextField deviceType;
	TextField precision;
	TextField range;
	SpinnerField<Integer> num;
	TextField fnum;
	TextField checkCert;
	SpinnerField<Integer> checkPeriod;
	DateField lastChecked;
	ComboBox<DictionaryRecord> checker;
	TextButton addCheckerBtn;
	DateField nextCheck;
	SpinnerField<Integer> groen;
	ComboBox<Employee> responsibleEmpl;
	TextArea notes;
	List<Widget> validityChecks;
	
	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel deviceDocsPanel;
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
	private final DeviceServiceAsync deviceService = GWT.create(DeviceService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	
	public DeviceInfoWindow() {
		super();
		this.setPixelSize(500, 550);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр информации о средстве измерения");
		
		theWindow = this;

		VerticalLayoutContainer deviceInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(deviceInfoTopContainer);
		
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
				deviceService.getPrintableDeviceCard(Integer.valueOf(deviceId.getText()), new AsyncCallback<String>() {
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
		
		deviceInfoTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		TabPanel panel = new TabPanel();

		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer deviceInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(deviceInfoContainer);
		
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
		
		final KeyUpHandler spinnerKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};

		int labelColWidth = 120;
		int longLabelColWidth = 210;
		deviceId = new TextField();
		
		deviceTitle = new TextField();
		deviceTitle.setAllowBlank(false);
		deviceTitle.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel nameLabel = new FieldLabel(deviceTitle, "Наименование");
		nameLabel.setLabelWidth(labelColWidth);
		deviceInfoContainer.add(nameLabel, new VerticalLayoutData(1, -1));

		deviceType = new TextField();
		deviceType.addKeyUpHandler(fieldKeyUpHandler);
		deviceType.setAllowBlank(true);
		FieldLabel bossLabel = new FieldLabel(deviceType, "Тип");
		bossLabel.setLabelWidth(labelColWidth);
		deviceInfoContainer.add(bossLabel, new VerticalLayoutData(1, -1));

		precision = new TextField();
		precision.addKeyUpHandler(fieldKeyUpHandler);
		precision.setAllowBlank(true);
		FieldLabel addressLabel = new FieldLabel(precision, "Класс точности");
		addressLabel.setLabelWidth(labelColWidth);
		deviceInfoContainer.add(addressLabel, new VerticalLayoutData(1, -1));

		range = new TextField();
		range.addKeyUpHandler(fieldKeyUpHandler);
		range.setAllowBlank(true);
		FieldLabel phoneLabel = new FieldLabel(range, "Предел измерений");
		phoneLabel.setLabelWidth(labelColWidth);
		deviceInfoContainer.add(phoneLabel, new VerticalLayoutData(1, -1));

		num = new SpinnerField<Integer>(new IntegerPropertyEditor());
		num.setMinValue(0);
		num.setAllowNegative(false);
		num.setAllowBlank(true);
		num.setValue(0);
		num.addKeyUpHandler(spinnerKeyUpHandler);
		num.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		FieldLabel innLabel = new FieldLabel(num, "Кол-во/Кол-во в наборе");
		innLabel.setLabelWidth(longLabelColWidth);
		deviceInfoContainer.add(innLabel, new VerticalLayoutData(1, -1));

		fnum = new TextField();
		fnum.addKeyUpHandler(fieldKeyUpHandler);
		fnum.setAllowBlank(false);
		FieldLabel fnumLabel = new FieldLabel(fnum, "Заводской №");
		fnumLabel.setLabelWidth(labelColWidth);
		deviceInfoContainer.add(fnumLabel, new VerticalLayoutData(1, -1));
		
		checkCert = new TextField();
		checkCert.addKeyUpHandler(fieldKeyUpHandler);
		checkCert.setAllowBlank(true);
		FieldLabel certLabel = new FieldLabel(checkCert, "№ св-ва о поверке");
		certLabel.setLabelWidth(labelColWidth);
		deviceInfoContainer.add(certLabel, new VerticalLayoutData(1, -1));

		checkPeriod = new SpinnerField<Integer>(new IntegerPropertyEditor());
		checkPeriod.setMinValue(1);
		checkPeriod.setAllowNegative(false);
		checkPeriod.setAllowBlank(false);
		checkPeriod.addKeyUpHandler(spinnerKeyUpHandler);
		checkPeriod.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		checkPeriod.setValue(12);
		FieldLabel periodLabel = new FieldLabel(checkPeriod, "Периодичность поверки (месяцы)");
		periodLabel.setLabelWidth(longLabelColWidth);
		deviceInfoContainer.add(periodLabel, new VerticalLayoutData(1, -1));
		
		lastChecked = new DateField();
		lastChecked.setAllowBlank(true);
		lastChecked.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
		
		FieldLabel lastCheckLabel = new FieldLabel(lastChecked, "Дата последней поверки");
		lastCheckLabel.setLabelWidth(longLabelColWidth);
		deviceInfoContainer.add(lastCheckLabel, new VerticalLayoutData(1, -1));
		lastChecked.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
	    });
		lastChecked.addKeyUpHandler(fieldKeyUpHandler);
		lastChecked.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
		
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> checkersStore = new ListStore<DictionaryRecord>(props.key());
	 
	    checker = new ComboBox<DictionaryRecord>(checkersStore, props.nameLabel());
	    checker.setAllowBlank(true);
	    checker.setForceSelection(true);
	    checker.setTriggerAction(TriggerAction.ALL);
	    
	    addCheckerBtn = new TextButton("");
	    addCheckerBtn.setIcon(Images.INSTANCE.add());
	    addCheckerBtn.setToolTip("Добавить место проведения поверки");
	    addCheckerBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final PromptMessageBox box = new PromptMessageBox("Место проведения поверки", "Введите новое значение для справочника<br>мест проведения поверок:");
	          box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
	            	if (event.getHideButton() == PredefinedButton.CANCEL) {
						return;
					}
	            	if (box.getValue() == null || box.getValue().trim().isEmpty()) {
	            		MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите название места проведения поверки.");
	            		d.setIcon(Images.INSTANCE.information());
	            		d.show();
						return;
	            	}
	            	dictionaryService.addRecord(Constants.DICT_CHECKERS, box.getValue().trim(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							DictionaryRecord rec = new DictionaryRecord(result, box.getValue().trim());
							checkersStore.add(rec);
							checker.setValue(rec);
							isModified = true;
							checkFormValidity();
						}
					});
	            }
	          });
	          box.show();
	        }
	      });
	    
	    HorizontalLayoutContainer checkerHLC = new HorizontalLayoutContainer();
	    checkerHLC.add(checker, new HorizontalLayoutData(1, 1));
	    checkerHLC.add(addCheckerBtn, new HorizontalLayoutData(-1, -1));
	    
	    FieldLabel checkerLabel = new FieldLabel(checkerHLC, "Место проведения поверки");
		checkerLabel.setLabelWidth(longLabelColWidth);
		checkerLabel.setHeight(23);
		deviceInfoContainer.add(checkerLabel, new VerticalLayoutData(1, -1));
		checker.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
	    		isModified = true;
	    		checkFormValidity();
	    	}
	    });
		checker.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		
	    nextCheck = new DateField();
	    nextCheck.setAllowBlank(false);
	    nextCheck.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
		
		FieldLabel nextCheckLabel = new FieldLabel(nextCheck, "Сроки проведения поверки");
		nextCheckLabel.setLabelWidth(longLabelColWidth);
		deviceInfoContainer.add(nextCheckLabel, new VerticalLayoutData(1, -1));
		nextCheck.addChangeHandler(formChangedHandler);
		nextCheck.addKeyUpHandler(fieldKeyUpHandler);
		nextCheck.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		isModified = true;
				checkFormValidity();
	    	}
		});
	    
		groen = new SpinnerField<Integer>(new IntegerPropertyEditor());
		groen.setMinValue(1);
		groen.setMaxValue(18);
		groen.setAllowNegative(false);
		groen.setAllowBlank(true);
		groen.addKeyUpHandler(spinnerKeyUpHandler);
		groen.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				isModified = true;
				checkFormValidity();
			}
		});
		groen.setValue(12);
		FieldLabel groenLabel = new FieldLabel(groen, "Сфера ГРОЕН");
		groenLabel.setLabelWidth(labelColWidth);
		deviceInfoContainer.add(groenLabel, new VerticalLayoutData(1, -1));
		
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
		deviceInfoContainer.add(responsibleLabel, new VerticalLayoutData(1, -1));
		
	    notes = new TextArea();
	    notes.setAllowBlank(true);
	    FieldLabel notesLabel = new FieldLabel(notes, "Примечания");
	    notesLabel.setLabelWidth(labelColWidth);
	    deviceInfoContainer.add(notesLabel, new VerticalLayoutData(1, 60));
	    notes.addChangeHandler(formChangedHandler);
	    notes.addKeyUpHandler(fieldKeyUpHandler);
		
		validityChecks = new ArrayList<Widget>();
		
		validityChecks.add(deviceTitle);
		validityChecks.add(fnum);
		validityChecks.add(checkPeriod);
		validityChecks.add(nextCheck);
		validityChecks.add(responsibleEmpl);
		
		// документы
		deviceDocsPanel = new ContentPanel();
		deviceDocsPanel.setHeaderVisible(false);
		deviceDocsPanel.setBorders(false);
		deviceDocsPanel.setHeight(415);
		
		attachCont = new AttachmentsContainer(Constants.DEVICE_ATTACHMENTS);
		deviceDocsPanel.setWidget(attachCont);
		
		// *** Добавление табов в контейнер

		deviceDataTabConfig = new TabItemConfig("Данные устройства");
		deviceDataTabConfig.setIcon(IMAGES.view());

		deviceDocsTabConfig = new TabItemConfig("Связанная документация");
		deviceDocsTabConfig.setIcon(IMAGES.documents());

		panel.add(infoPanel, deviceDataTabConfig);
		panel.add(deviceDocsPanel, deviceDocsTabConfig);
		

		deviceInfoTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		this.setButtonAlign(BoxLayoutPack.END);
		
		saveBtn = new TextButton("Сохранить");
		saveBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Device device = new Device();
				device.setId(Integer.valueOf(deviceId.getValue()));
				device.setTitle(deviceTitle.getValue());
				device.setType(deviceType.getValue());
				device.setPrecision(precision.getValue());
				device.setRange(range.getValue());
				device.setNum(num.getValue());
				device.setFnum(fnum.getValue());
				device.setCheckCert(checkCert.getValue());
				device.setCheckPeriod(checkPeriod.getValue());
				device.setLastChecked(lastChecked.getValue());
				device.setCheckerId(checker.getValue()==null?null:checker.getValue().getId());
				device.setNextCheck(nextCheck.getValue());
				device.setGroen(groen.getValue());
				device.setResponsibleId(responsibleEmpl.getValue().getId());
				device.setNotes(notes.getValue());
//				При добавлении нового устройства пустая запись уже была создана, её нужно только обновить.
				deviceService.updateDevice(device, new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
//						Если создаём новую запись - нужно сообщить вызывающему коду, что мы создали.
						if (action == Constants.ACTION_ADD) {
							theWindow.setData("newDeviceID", deviceId.getValue());
							theWindow.setData("newDeviceTitle", deviceTitle.getValue());
						}
						canClose=true;
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
						theWindow.setData("hideButton", "save");
	    				theWindow.hide();
					}
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить информацию об устройстве.");
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
		this.setFocusWidget(this.getButtonBar().getWidget(0));
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
			deviceService.deleteDevice(Integer.valueOf(deviceId.getValue()), false, new AsyncCallback<Boolean>() {
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
	
	public void displayInfo(Device device, boolean canEdit, boolean canPrint) {
		action = -1;
		canClose=false;
		deviceId.setValue(String.valueOf(device.getId()));
		
		deviceTitle.setValue(device.getTitle()); 
		deviceType.setValue(device.getType());
		precision.setValue(device.getPrecision());
		range.setValue(device.getRange());
		num.setValue(device.getNum());
		fnum.setValue(device.getFnum());
		checkCert.setValue(device.getCheckCert());
		checkPeriod.setValue(device.getCheckPeriod());
		lastChecked.setValue(device.getLastChecked());
		updateChecker(device.getChecker());
		
		nextCheck.setValue(device.getNextCheck());
		groen.setValue((device.getGroen()==null||device.getGroen()==0)?null:device.getGroen());
		notes.setValue(device.getNotes());
		updateResponsible(device.getResponsibleId());
		
		attachCont.init(device.getId());
		
		TextButton saveBtn = (TextButton) theWindow.getData("saveBtn");
		saveBtn.setVisible(true);
		saveBtn.setEnabled(false);
		theWindow.setHeadingText("Просмотр информации о средстве измерения");
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
		theWindow.setPixelSize(500, 570);
		theWindow.forceLayout();
	}

	public void editInfo(Device device, boolean newDevice, boolean canPrint) {
		action = newDevice ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		deviceId.setValue(String.valueOf(device.getId()));
		
//		// при первоначальном заполнении данных устройства мы имеем дело с заглушкой, названной "(временный)" - не надо это пользователю показывать
		deviceTitle.setValue(action == Constants.ACTION_ADD ? null : device.getTitle());
		deviceType.setValue(device.getType());
		precision.setValue(device.getPrecision());
		range.setValue(device.getRange());
		num.setValue(device.getNum());
		fnum.setValue(device.getFnum());
		checkCert.setValue(device.getCheckCert());
		checkPeriod.setValue(device.getCheckPeriod());
		lastChecked.setValue(device.getLastChecked());
		
		updateChecker(device.getChecker());
		
		nextCheck.setValue(device.getNextCheck());
		groen.setValue((device.getGroen()==null||device.getGroen()==0)?null:device.getGroen());
		
		updateResponsible(device.getResponsibleId());
		
		notes.setValue(device.getNotes());
		
		attachCont.init(device.getId());
		
		TextButton saveBtn = (TextButton) theWindow.getData("saveBtn");
		saveBtn.setVisible(true);
		saveBtn.setEnabled(false);
		theWindow.setHeadingText("Редактирование информации о средстве измерения");
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
		theWindow.setPixelSize(500, 570);
	}
	
	private void toggleEditMode(boolean editMode) {
		deviceTitle.setEnabled(editMode);
		deviceType.setEnabled(editMode);
		precision.setEnabled(editMode);
		range.setEnabled(editMode);
		num.setEnabled(editMode);
		fnum.setEnabled(editMode);
		checkCert.setEnabled(editMode);
		checkPeriod.setEnabled(editMode);
		lastChecked.setEnabled(editMode);
		checker.setEnabled(editMode);
		addCheckerBtn.setEnabled(editMode);
		nextCheck.setEnabled(editMode);
		groen.setEnabled(editMode);
		responsibleEmpl.setEnabled(editMode);
		notes.setEnabled(editMode);

		attachCont.setEditMode(editMode);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование информации о средстве измерения");
		else
			setHeadingText("Просмотр информации о средстве измерения");
		
		theWindow.forceLayout();
	}
	
	private void updateChecker(final String checkerName) {
	    dictionaryService.getDictionaryContents(Constants.DICT_CHECKERS,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. DeviceIW:708 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						checker.getStore().replaceAll(result);
						
						List<DictionaryRecord> checkers = result;
						checker.clear();
						for (int i=0; i<checkers.size(); i++) {
							if(checkers.get(i).getName().equals(checkerName)) {
								checker.setValue(checkers.get(i));
								break;
							}
						}
						checker.clearInvalid();
					}
				});
	}
	
	private void updateResponsible(final int responsibleId) {
		employeeService.getAllEmployees(new AsyncCallback<List<Employee>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. DeviceIW:732 - "+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<Employee> result) {
				responsibleEmpl.getStore().replaceAll(result);
				
				List<Employee> emps = result;
				responsibleEmpl.clear();
				for (int i=0; i<emps.size(); i++) {
					if(emps.get(i).getId() == responsibleId) {
						responsibleEmpl.setValue(emps.get(i));
						break;
					}
				}
				responsibleEmpl.clearInvalid();
 			}
		});
	}
}
