package ru.techstandard.client;

import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.AccessGroup.AccessGroupProps;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.client.model.Employee;
import ru.techstandard.shared.AccessGroupService;
import ru.techstandard.shared.AccessGroupServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class EmployeeInfoPanel extends VerticalLayoutContainer implements HasValue<Employee> {
	private int action;
	
	Employee cachedEmplData;
	TextField name;
	TextField login;
	TextField password;
	TextField email;
	TextField department;
	ComboBox<DictionaryRecord> position;
	TextButton addPositionBtn;
	ComboBox<AccessGroup> accessGroup;
	TextArea grpDescription;
	Radio isWorkerRadio;
    Radio isBossRadio;
    ToggleGroup deptStatusToggle;
    Radio isEmployeeRadio;
    Radio isFiredRadio;
    ToggleGroup firmStatusToggle;
	List<Widget> validityChecks;
	
	VerticalLayoutContainer theContainer;
	ContentPanel infoPanel;
	ToolBar topToolBar;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private boolean isModified;
	private boolean canEdit;
	
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final AccessGroupServiceAsync accessGroupService = GWT.create(AccessGroupService.class);
	
	public EmployeeInfoPanel() {
		super();
		theContainer = this;
	
		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer emplInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(emplInfoContainer);
		
		final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};

		int labelColWidth = 180;
		int fieldColWidth = 250;
		
		name = new TextField();
		name.setAllowBlank(false);
		name.setWidth(fieldColWidth);
		FieldLabel emplNameLabel = new FieldLabel(name, "ФИО сотрудника");
		emplNameLabel.setLabelWidth(labelColWidth);
		emplInfoContainer.add(emplNameLabel, new VerticalLayoutData(-1, -1, new Margins(0)));
		name.addKeyUpHandler(fieldKeyUpHandler);
		
		department = new TextField();
		department.setWidth(fieldColWidth);
		department.setEnabled(false);
		FieldLabel departmentLabel = new FieldLabel(department, "Подразделение");
		departmentLabel.setLabelWidth(labelColWidth);
		emplInfoContainer.add(departmentLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));
		
	    DictionaryRecordProps dictionaryRecordProps = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> positionsStore = new ListStore<DictionaryRecord>(dictionaryRecordProps.key());
	    dictionaryService.getDictionaryContents(Constants.DICT_POSITIONS, new AsyncCallback<List<DictionaryRecord>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.<br>"+caught.getLocalizedMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<DictionaryRecord> result) {
				positionsStore.replaceAll(result);
 			}
		});
	    
	    position = new ComboBox<DictionaryRecord>(positionsStore, dictionaryRecordProps.nameLabel());
	    position.setWidth(fieldColWidth-25); // минус ширина кнопки
	    position.addValueChangeHandler(new ValueChangeHandler<DictionaryRecord>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<DictionaryRecord> event) {
		    	  isModified = true;
		    	  checkFormValidity();
		      }
		    });
	    position.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				isModified = true;
				checkFormValidity();
			}
		});
	    position.setAllowBlank(false);
	    position.setForceSelection(true);
	    position.setTriggerAction(TriggerAction.ALL);
	    
	    addPositionBtn = new TextButton("");
	    addPositionBtn.setIcon(Images.INSTANCE.add());
	    addPositionBtn.setToolTip("Добавить должность");
	    addPositionBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final PromptMessageBox box = new PromptMessageBox("Должность", "Введите новое значение для справочника<br>должностей:");
	          box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
	            	if (event.getHideButton() == PredefinedButton.CANCEL) {
						return;
					}
	            	if (box.getValue() == null || box.getValue().trim().isEmpty()) {
	            		MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите должность.");
	            		d.setIcon(Images.INSTANCE.information());
	            		d.show();
						return;
	            	}
	            	dictionaryService.addRecord(Constants.DICT_POSITIONS, box.getValue().trim(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							DictionaryRecord rec = new DictionaryRecord(result, box.getValue().trim());
							positionsStore.add(rec);
							position.setValue(rec);
							position.clearInvalid();
						}
					});
	            }
	          });
	          box.show();
	          box.getField().focus();
	        }
	      });
	    HorizontalLayoutContainer positionHLC = new HorizontalLayoutContainer();
	    positionHLC.add(position, new HorizontalLayoutData(-1, 1));
	    positionHLC.add(addPositionBtn, new HorizontalLayoutData(-1, -1));
	    
		FieldLabel positionLabel = new FieldLabel(positionHLC, "Должность");
		positionLabel.setLabelWidth(labelColWidth);
		emplInfoContainer.add(positionLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));

		login = new TextField();
		login.setAllowBlank(false);
		login.setWidth(fieldColWidth);
		FieldLabel loginLabel = new FieldLabel(login, "Имя пользователя в системе");
		loginLabel.setLabelWidth(labelColWidth);
		emplInfoContainer.add(loginLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));
		login.addKeyUpHandler(fieldKeyUpHandler);
		
		password = new TextField();
		password.setAllowBlank(false);
		password.setWidth(fieldColWidth);
		FieldLabel passwordLabel = new FieldLabel(password, "Пароль");
		passwordLabel.setLabelWidth(labelColWidth);
		emplInfoContainer.add(passwordLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));
		password.addKeyUpHandler(fieldKeyUpHandler);

		email = new TextField();
		email.setAllowBlank(false);
		email.setWidth(fieldColWidth);
		FieldLabel emailLabel = new FieldLabel(email, "e-mail");
		emailLabel.setLabelWidth(labelColWidth);
		emplInfoContainer.add(emailLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));
		email.addKeyUpHandler(fieldKeyUpHandler);
		
		AccessGroupProps accessGroupProps = GWT.create(AccessGroupProps.class);
	    final ListStore<AccessGroup> accGrpStore = new ListStore<AccessGroup>(accessGroupProps.id());
	    
	    accessGroupService.getAccessGroups(new AsyncCallback<List<AccessGroup>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.<br>"+caught.getLocalizedMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<AccessGroup> result) {
				accGrpStore.replaceAll(result);
			}
		});
	 
	    accessGroup = new ComboBox<AccessGroup>(accGrpStore, accessGroupProps.nameLabel());
	    accessGroup.setWidth(fieldColWidth);
	    accessGroup.addValueChangeHandler(new ValueChangeHandler<AccessGroup>() {
	      @Override
	      public void onValueChange(ValueChangeEvent<AccessGroup> event) {
	    	  grpDescription.setValue(accessGroup.getCurrentValue().getDescription());
	    	  isModified = true;
	    	  checkFormValidity();
	      }
	    });
	    accessGroup.addSelectionHandler(new SelectionHandler<AccessGroup>() {
			@Override
			public void onSelection(SelectionEvent<AccessGroup> event) {
				grpDescription.setValue(accessGroup.getCurrentValue().getDescription());
				isModified = true;
				checkFormValidity();
			}
		});
	    accessGroup.setAllowBlank(false);
	    accessGroup.setForceSelection(true);
	    accessGroup.setTriggerAction(TriggerAction.ALL);
	    FieldLabel accGrpLabel = new FieldLabel(accessGroup, "Группа доступа");
	    accGrpLabel.setLabelWidth(labelColWidth);
	    emplInfoContainer.add(accGrpLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));
	    
	    grpDescription = new TextArea();
	    grpDescription.setEnabled(false);
	    FieldLabel grpDescriptionLabel = new FieldLabel(grpDescription, "Описание группы доступа");
	    grpDescriptionLabel.setLabelWidth(labelColWidth);
	    emplInfoContainer.add(grpDescriptionLabel, new VerticalLayoutData(435, 60, new Margins(10, 0, 0, 0)));
	    
		isWorkerRadio = new Radio();
		isWorkerRadio.setWidth(fieldColWidth/2);
	    isWorkerRadio.setBoxLabel("Сотрудник");
	    isWorkerRadio.setValue(true);
	 
	    isBossRadio = new Radio();
	    isBossRadio.setWidth(fieldColWidth/2);
	    isBossRadio.setBoxLabel("Руководитель");
	 
	    HorizontalPanel deptStatusHP = new HorizontalPanel();
	    deptStatusHP.add(isWorkerRadio);
	    deptStatusHP.add(isBossRadio);
	 
	    FieldLabel deptStatusLabel = new FieldLabel(deptStatusHP, "Статус в подразделении");
	    deptStatusLabel.setLabelWidth(labelColWidth);
	    emplInfoContainer.add(deptStatusLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));
	 
	    deptStatusToggle = new ToggleGroup();
	    deptStatusToggle.add(isWorkerRadio);
	    deptStatusToggle.add(isBossRadio);
	    deptStatusToggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
			@Override
			public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
				isModified = true;
				checkFormValidity();
			}
		});
	    
	    isEmployeeRadio = new Radio();
		isEmployeeRadio.setWidth(fieldColWidth/2);
	    isEmployeeRadio.setBoxLabel("Работающий");
	    isEmployeeRadio.setValue(true);
	 
	    isFiredRadio = new Radio();
	    isFiredRadio.setWidth(fieldColWidth/2);
	    isFiredRadio.setBoxLabel("Уволенный");
	 
	    HorizontalPanel firmStatusHP = new HorizontalPanel();
	    firmStatusHP.add(isEmployeeRadio);
	    firmStatusHP.add(isFiredRadio);
	 
	    FieldLabel firmStatusLabel = new FieldLabel(firmStatusHP, "Статус в организации");
	    firmStatusLabel.setLabelWidth(labelColWidth);
	    emplInfoContainer.add(firmStatusLabel, new VerticalLayoutData(-1, -1, new Margins(10, 0, 0, 0)));
	 
	    firmStatusToggle = new ToggleGroup();
	    firmStatusToggle.add(isEmployeeRadio);
	    firmStatusToggle.add(isFiredRadio);
	    firmStatusToggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {
			@Override
			public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
				isModified = true;
				checkFormValidity();
			}
		});
	    
	    theContainer.add(infoPanel, new VerticalLayoutData(-1, 1));


	    validityChecks = new ArrayList<Widget>();

	    validityChecks.add(name);
	    validityChecks.add(position);
	    validityChecks.add(login);
	    validityChecks.add(password);
	    validityChecks.add(email);
	    validityChecks.add(accessGroup);
	    
	    FramedPanel buttonsPanel = new FramedPanel();
	    buttonsPanel.setHeaderVisible(false);
	    buttonsPanel.setBorders(false);
	    FlowLayoutContainer buttonsContainer = new FlowLayoutContainer();
	    buttonsPanel.setWidget(buttonsContainer);

	    saveBtn = new TextButton("Сохранить");
	    saveBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		cachedEmplData.setName(name.getValue());
	    		cachedEmplData.setLogin(login.getValue());
	    		cachedEmplData.setPassword(password.getValue());
	    		cachedEmplData.setEmail(email.getValue());
	    		cachedEmplData.setPositionId(position.getValue().getId());
	    		cachedEmplData.setGroup(accessGroup.getValue().getId());
	    		cachedEmplData.setBoss(deptStatusToggle.getValue()==isBossRadio);
	    		cachedEmplData.setFired(firmStatusToggle.getValue()==isFiredRadio);
	    		
	    		employeeService.updateEmployee(cachedEmplData, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
						d.show();
					}
					@Override
					public void onSuccess(Boolean result) {
						ValueChangeEvent.fire((EmployeeInfoPanel) theContainer, cachedEmplData);
					}
				});
	    		saveBtn.setEnabled(false);
	    		cancelBtn.setEnabled(false);
	    	}
	    });
	    buttonsContainer.add(saveBtn, new MarginData(5, 5, 0, 10));

	    cancelBtn = new TextButton("Отмена");
	    cancelBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		if (action == Constants.ACTION_ADD) {
	    			employeeService.deleteEmployee(cachedEmplData.getId(), new AsyncCallback<Boolean>() {
						@Override public void onFailure(Throwable caught) {}
						@Override public void onSuccess(Boolean result) {}
					});
	    			ValueChangeEvent.fire((EmployeeInfoPanel) theContainer, null);
	    			return;
	    		}
	    		fillFormFields(canEdit);
	    		ValueChangeEvent.fire((EmployeeInfoPanel) theContainer, null);
	    		saveBtn.setEnabled(false);
	    		cancelBtn.setEnabled(false);
	    	}
	    });
	    buttonsContainer.add(cancelBtn, new MarginData(5, 5, 0, 10));
	    
	    theContainer.add(buttonsPanel, new VerticalLayoutData(-1, -1, new Margins(-5, -5, -5, -5)));
	    theContainer.forceLayout();
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
		cancelBtn.setEnabled((action == Constants.ACTION_ADD) || isModified);
	}
	
	public void init(int employeeId, boolean newEmployee, boolean canUpdate) {
		canEdit = canUpdate;
		action = newEmployee ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		employeeService.getEmployeeInfo(employeeId, new AsyncCallback<Employee>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.<br>"+caught.getLocalizedMessage());
				d.show();
				return;
			}
			@Override
			public void onSuccess(Employee result) {
				cachedEmplData = result;
				fillFormFields(canEdit);
				checkFormValidity();
			}
		});
		isModified = false;
		saveBtn.setEnabled(false);
		// Если создаём нового сотрудника, кнопку отмена можно включить сразу, чтобы пользователь мог отменить создание
		cancelBtn.setEnabled(newEmployee);
	}

	private void fillFormFields(boolean canEdit) {
		name.setValue(cachedEmplData.getName());
		name.clearInvalid();
		name.setEnabled(canEdit);
		
		department.setValue(cachedEmplData.getDepartmentName());
		department.clearInvalid();

		List<DictionaryRecord> emps = position.getStore().getAll();
		position.clear();
		for (int i=0; i<emps.size(); i++) {
			if(emps.get(i).getName().equals(cachedEmplData.getPositionName())) {
				position.setValue(emps.get(i));
				break;
			}
		}
		position.clearInvalid();
		position.setEnabled(canEdit);
		addPositionBtn.setEnabled(canEdit);
		
		login.setValue(cachedEmplData.getLogin());
		login.clearInvalid();
		login.setEnabled(canEdit);
		
		password.setValue(cachedEmplData.getPassword());
		password.clearInvalid();
		password.setEnabled(canEdit);
		
		email.setValue(cachedEmplData.getEmail());
		email.clearInvalid();
		email.setEnabled(canEdit);
		
		List<AccessGroup> accessGroups = accessGroup.getStore().getAll();
		accessGroup.clear();
		grpDescription.setValue(null);
		for (int i=0; i<accessGroups.size(); i++) {
			if(accessGroups.get(i).getName().equals(cachedEmplData.getGroupName())) {
				accessGroup.setValue(accessGroups.get(i));
				grpDescription.setValue(accessGroups.get(i).getDescription());
				break;
			}
		}
		accessGroup.clearInvalid();
		accessGroup.setEnabled(canEdit);
		
		if (cachedEmplData.isBoss())
			deptStatusToggle.setValue(isBossRadio);
		else
			deptStatusToggle.setValue(isWorkerRadio);
		isBossRadio.setEnabled(canEdit);
		isWorkerRadio.setEnabled(canEdit);
		
		if (cachedEmplData.isFired())
			firmStatusToggle.setValue(isFiredRadio);
		else
			firmStatusToggle.setValue(isEmployeeRadio);
		isFiredRadio.setEnabled(canEdit);
		isEmployeeRadio.setEnabled(canEdit);
		
		this.forceLayout();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Employee> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Employee getValue() {
		return cachedEmplData;
	}

	@Override
	public void setValue(Employee value) {
	}

	@Override
	public void setValue(Employee value, boolean fireEvents) {
		
	}
}
