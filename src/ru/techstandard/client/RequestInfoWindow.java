package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.ClientProps;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.client.model.Request;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.RequestService;
import ru.techstandard.shared.RequestServiceAsync;

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
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
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

public class RequestInfoWindow extends Window {
	private final Images IMAGES = GWT.create(Images.class);

	TabItemConfig requestDataTabConfig;
	TabItemConfig requestDocsTabConfig;
	
	TextField requestId;
	TextArea description;
	ComboBox<Client> client;
	ComboBox<Employee> responsibleEmpl;
	DateField dueDate;
	TextArea notes;
	List<Widget> validityChecks;
	
	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel requestDocsPanel;
	ToolBar topToolBar;
	ToggleButton toggleEditModeBtn;
	TextButton printDeviceBtn;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private boolean isModified;
	private boolean canClose=false;
	private int action=0;
	private AttachmentsContainer attachCont;
	private int wndHeight = 370;
	
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final RequestServiceAsync requestService = GWT.create(RequestService.class);
	
	public RequestInfoWindow() {
		super();
		this.setPixelSize(500, wndHeight);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр информации о задаче");
		
		theWindow = this;
		
		VerticalLayoutContainer requestInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(requestInfoTopContainer);
		
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
				requestService.getPrintableRequestCard(Integer.valueOf(requestId.getText()), new AsyncCallback<String>() {
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
		
		requestInfoTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		TabPanel panel = new TabPanel();

		infoPanel = new FramedPanel();
		infoPanel.setHeaderVisible(false);
		VerticalLayoutContainer guideInfoContainer = new VerticalLayoutContainer();
		infoPanel.setWidget(guideInfoContainer);
		
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
		requestId = new TextField();
		
		description = new TextArea();
		description.setAllowBlank(false);
		FieldLabel objectNameLabel = new FieldLabel(description, "Содержание задачи");
		objectNameLabel.setLabelWidth(labelColWidth);
		guideInfoContainer.add(objectNameLabel, new VerticalLayoutData(1, 60));
		description.addKeyUpHandler(fieldKeyUpHandler);
		
		ClientProps clientProps = GWT.create(ClientProps.class);
	    final ListStore<Client> clientStore = new ListStore<Client>(clientProps.id());
	 
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
	    client.setAllowBlank(false);
	    client.setForceSelection(true);
	    client.setTriggerAction(TriggerAction.ALL);
	    FieldLabel clientLabel = new FieldLabel(client, "Заказчик");
	    clientLabel.setLabelWidth(labelColWidth);
	    guideInfoContainer.add(clientLabel, new VerticalLayoutData(1, -1));
		
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
	    requestDocsPanel = new ContentPanel();
	    requestDocsPanel.setHeaderVisible(false);
	    requestDocsPanel.setBorders(false);
	    requestDocsPanel.setHeight(230);

	    attachCont = new AttachmentsContainer(Constants.REQUEST_ATTACHMENTS);
	    requestDocsPanel.setWidget(attachCont);

	    // *** Добавление табов в контейнер

	    requestDataTabConfig = new TabItemConfig("Данные задачи");
	    requestDataTabConfig.setIcon(IMAGES.view());

	    requestDocsTabConfig = new TabItemConfig("Связанная документация");
	    requestDocsTabConfig.setIcon(IMAGES.documents());

	    panel.add(infoPanel, requestDataTabConfig);
	    panel.add(requestDocsPanel, requestDocsTabConfig);

	    requestInfoTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));

	    validityChecks = new ArrayList<Widget>();

	    validityChecks.add(description);
	    validityChecks.add(client);
	    validityChecks.add(responsibleEmpl);
	    validityChecks.add(dueDate);

	    this.setButtonAlign(BoxLayoutPack.END);
	    
	    saveBtn = new TextButton("Сохранить");
	    saveBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		Request request = new Request(Integer.valueOf(requestId.getValue()));
	    		request.setDescription(description.getValue());
	    		request.setClientId(client.getValue().getId());
	    		request.setResponsibleId(responsibleEmpl.getValue().getId());
	    		request.setDueDate(dueDate.getValue());
	    		request.setNotes(notes.getValue());
	    		//				При добавлении нового документа пустая запись уже была создана, её нужно только обновить.
	    		requestService.updateRequest(request, new AsyncCallback<Boolean>() {
	    			@Override
	    			public void onSuccess(Boolean result) {
	    				//						Если создаём новую запись - нужно сообщить вызывающему коду, что мы создали.
	    				if (action == Constants.ACTION_ADD) {
	    					theWindow.setData("newRequestID", requestId.getValue());
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
//	    		System.out.println("cancel click: isModif="+isModified);
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
			requestService.deleteRequest(Integer.valueOf(requestId.getValue()), false, new AsyncCallback<Boolean>() {
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
	
	private void fillWindowFields(final Request request) {
		requestId.setValue(String.valueOf(request.getId()));
		
		description.setValue(request.getDescription());
		
	    
	    clientService.getAllClients(new AsyncCallback<List<Client>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. RequestIW:405 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Client> result) {
						client.getStore().replaceAll(result);
						
						List<Client> clients = result;
						client.clear();
						for (int i=0; i<clients.size(); i++) {
							if(clients.get(i).getName().equals(request.getClientName())) {
								client.setValue(clients.get(i));
								break;
							}
						}
						client.clearInvalid();
					}
				});

	    employeeService.getAllEmployees(new AsyncCallback<List<Employee>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. RequestIW:427 - "+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<Employee> result) {
				responsibleEmpl.getStore().replaceAll(result);
				
				List<Employee> emps = result;
				responsibleEmpl.clear();
				for (int i=0; i<emps.size(); i++) {
					if(emps.get(i).getName().equals(request.getResponsibleName())) {
						responsibleEmpl.setValue(emps.get(i));
						break;
					}
				}
				responsibleEmpl.clearInvalid();
 			}
		});
		
		dueDate.setValue(request.getDueDate());
		notes.setValue(request.getNotes());
		
		attachCont.init(request.getId());
	}
	
	public void displayInfo(Request request, boolean canEdit, boolean canPrint) {
//		System.out.println("RequestIW.displayInfo:"+request);
		action = -1;
		canClose=false;
		fillWindowFields(request);

		isModified = false;	
		
		toggleEditMode(false);
		
		toggleEditModeBtn.setVisible(canEdit);
		toggleEditModeBtn.setValue(false);
		
		printDeviceBtn.setVisible(canPrint);
		
		theWindow.forceLayout();
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, wndHeight);
	}

	public void editInfo(Request request, boolean newRequest, boolean canPrint) {
//		System.out.println("GuideIW.editInfo:"+guide);
		action = newRequest ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		fillWindowFields(request);

		isModified = false;
		
		toggleEditMode(true);
		toggleEditModeBtn.setVisible(true);
		toggleEditModeBtn.setValue(true);
		
		printDeviceBtn.setVisible(canPrint);
		
		theWindow.forceLayout();
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, wndHeight);
		
	}
	
	private void toggleEditMode(boolean editMode) {
		description.setEnabled(editMode);
		client.setEnabled(editMode);
		responsibleEmpl.setEnabled(editMode);
		dueDate.setEnabled(editMode);
		notes.setEnabled(editMode);
		
		attachCont.setEditMode(editMode);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование информации о задаче");
		else
			setHeadingText("Просмотр информации о задаче");
		saveBtn.setEnabled(false);
		
		theWindow.forceLayout();
	}
}
