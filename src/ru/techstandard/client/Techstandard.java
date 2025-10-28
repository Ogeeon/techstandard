package ru.techstandard.client;
import java.util.ArrayList;
import java.util.List;

import org.atmosphere.gwt20.client.Atmosphere;
import org.atmosphere.gwt20.client.AtmosphereCloseHandler;
import org.atmosphere.gwt20.client.AtmosphereMessageHandler;
import org.atmosphere.gwt20.client.AtmosphereOpenHandler;
import org.atmosphere.gwt20.client.AtmosphereRequest;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig;
import org.atmosphere.gwt20.client.AtmosphereResponse;
import org.atmosphere.gwt20.client.AutoBeanClientSerializer;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Notification;
import ru.techstandard.client.model.Task;
import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.LoginErrorException;
import ru.techstandard.shared.LoginService;
import ru.techstandard.shared.TaskService;
import ru.techstandard.shared.TaskServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FormPanel.LabelAlign;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.SeparatorToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;


public class Techstandard implements EntryPoint {
	Widget evtTab;
	Widget jorunTab;
	Widget devTab;
	Widget evalTab;
	Widget dictTab;
	Widget taskTab;
	Widget emplTab;
	Widget rightsTab;
	Widget delObjTab;
	Widget chatTab;
	Widget chatLogTab;
	
	Window loginWindow;
	TextField userNameField;
	PasswordField passwordField;
	
	UserDTO user;
	TaskInfoWindow taskInfoWindow;
	HideHandler taskEditHideHandler;
	
	private MyBeanFactory beanFactory = GWT.create(MyBeanFactory.class);
	Atmosphere atmosphere;
	AtmosphereRequest jsonRequest;
	
	public void onModuleLoad() {
		
		taskInfoWindow = new TaskInfoWindow();
		taskEditHideHandler = new HideHandler() {
	        @Override
	        public void onHide(HideEvent event) {
	        	checkOutdatedTasks();
	        }
	    };
	    taskInfoWindow.addHideHandler(taskEditHideHandler);
	    
		String sessionID = Cookies.getCookie("sid");
	    if (sessionID == null) {
	        showLoginWindow("", "");
	    } else {
	        checkWithServerIfSessionIdIsStillLegal();
	    }		
	}
	
	private void checkWithServerIfSessionIdIsStillLegal()
	{
	    LoginService.Util.getInstance().loginFromSessionServer(new AsyncCallback<UserDTO>()
	    {
	        @Override
	        public void onFailure(Throwable caught) {
//	        	System.out.println("loginFromSessionServer failed");
	        	showLoginWindow("", "");
	        }
	 
	        @Override
	        public void onSuccess(UserDTO result)
	        {
//	        	System.out.println("loginFromSessionServer succed, result="+result);
	        	if (result == null) {
	            	showLoginWindow("", "");
	            } else  {
	                if (result.getLoggedIn()) {
	                	user = result;
//	                	showMainScreen();
	                	checkOutdatedTasks();
	                } else {
	                	showLoginWindow("", "");
	                }
	            }
	        }
	 
	    });
	}
	
	private void checkOutdatedTasks() {
		TaskServiceAsync taskService = GWT.create(TaskService.class);
		taskService.getOutdatedTasks(user.getEmployeeId(), new AsyncCallback<List<Task>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные.");
				d.show();
			}
			@Override
			public void onSuccess(final List<Task> result) {
				if (result.size() == 0) {
					if (loginWindow != null)
						loginWindow.mask("Идёт загрузка интерфейса...");
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
			            public void execute() {
							showMainScreen();
						}
					});
					
				}
				else {
					if (loginWindow != null)
						loginWindow.hide();
					AlertMessageBox d = new AlertMessageBox("Внимание", "У Вас имеются просроченные задачи с незаполненным полем 'Примечание'. Укажите в примечании причину задержки выполнения задания.");
					d.addHideHandler(new HideHandler() {
						@Override
						public void onHide(HideEvent event) {
						    taskInfoWindow.editInfo(result.get(0), false, user);
						}
					});
					d.show();
				}
			}
		});
	}
	
	private void showMainScreen() {
		VerticalLayoutContainer topLevelContainer = new VerticalLayoutContainer();
		ToolBar toolBar = new ToolBar();

		final List<ToggleButton> buttons = new ArrayList<ToggleButton>();
		final CardLayoutContainer layout = new CardLayoutContainer();

		ToggleButton eventToggle = new ToggleButton("События");
		eventToggle.setValue(true);
		toolBar.add(eventToggle);
		buttons.add(eventToggle);
		toolBar.add(new SeparatorToolItem());
		
		evtTab = new EventsPanel(user);
		layout.add(evtTab);
		
		eventToggle.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				layout.setActiveWidget(evtTab);
				((VBoxLayoutContainer)evtTab).forceLayout();
				for (int idx = 0; idx < buttons.size(); idx++) {
					buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
				}
			}
		});
		
		ToggleButton taskToggle = new ToggleButton("Задания");
		taskToggle.setValue(false);
		toolBar.add(taskToggle);
		buttons.add(taskToggle);
		toolBar.add(new SeparatorToolItem());
		
		taskTab = new TasksPanel(user);
		layout.add(taskTab);
		
		taskToggle.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				layout.setActiveWidget(taskTab);
				((VBoxLayoutContainer)taskTab).forceLayout();
				for (int idx = 0; idx < buttons.size(); idx++) {
					buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
				}
			}
		});
		
		AccessGroup grp = user.getAccess();
		if (grp.isAllowed(Constants.ACCESS_READ, "acts") ||
				grp.isAllowed(Constants.ACCESS_READ, "contracts") ||
				grp.isAllowed(Constants.ACCESS_READ, "guides") ||
				grp.isAllowed(Constants.ACCESS_READ, "requests") 
			) {
			ToggleButton journalsToggle = new ToggleButton("Журналы");
			toolBar.add(journalsToggle);
			toolBar.add(new SeparatorToolItem());
			buttons.add(journalsToggle);
			
			jorunTab = new JournalsPanel(user.getEmployeeId(), grp);
			layout.add(jorunTab);

			journalsToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(jorunTab);
					((VBoxLayoutContainer)jorunTab).forceLayout();
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}

		if (grp.isAllowed(Constants.ACCESS_READ, "devices")) {
			ToggleButton devicesToggle = new ToggleButton("Поверки");
			devicesToggle.setValue(false);
			toolBar.add(devicesToggle);
			toolBar.add(new SeparatorToolItem());
			buttons.add(devicesToggle);
			
			devTab = new DevicesPanel(user.getEmployeeId(), grp);
			layout.add(devTab);
			
			devicesToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(devTab);
					((VBoxLayoutContainer)devTab).forceLayout();
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}
		

		if (grp.isAllowed(Constants.ACCESS_READ, "evaluations")) {
			ToggleButton evalToggle = new ToggleButton("Аттестация");
			evalToggle.setValue(false);
			toolBar.add(evalToggle);
			toolBar.add(new SeparatorToolItem());
			buttons.add(evalToggle);
			
			evalTab = new EvaluationsPanel(grp);
			layout.add(evalTab);
			
			evalToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(evalTab);
					((VBoxLayoutContainer)evalTab).forceLayout();
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}
		
		if (grp.isAllowed(Constants.ACCESS_READ, "dictionaries")) {
			ToggleButton dictToggle = new ToggleButton("Справочники");
			dictToggle.setValue(false);
			toolBar.add(dictToggle);
			buttons.add(dictToggle);
			toolBar.add(new SeparatorToolItem());

			dictTab = new DictionariesPanel(grp);
			layout.add(dictTab);

			dictToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(dictTab);
					((VBoxLayoutContainer)dictTab).forceLayout();
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}

		if (grp.isAllowed(Constants.ACCESS_READ, "employees")) {
			ToggleButton emplToggle = new ToggleButton("Сотрудники");
			emplToggle.setValue(false);
			toolBar.add(emplToggle);
			buttons.add(emplToggle);
			toolBar.add(new SeparatorToolItem());
			
			emplTab = new EmployeesPanel(grp);
			layout.add(emplTab);
			
			emplToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(emplTab);
					((BorderLayoutContainer)emplTab).forceLayout();
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}
		
		if (grp.isAllowed(Constants.ACCESS_READ, "rights")) {
			ToggleButton rightsToggle = new ToggleButton("Права доступа");
			rightsToggle.setValue(false);
			toolBar.add(rightsToggle);
			buttons.add(rightsToggle);
			toolBar.add(new SeparatorToolItem());

			rightsTab = new RightsEditPanel(Unit.PCT, grp);
			layout.add(rightsTab);
			
			rightsToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(rightsTab);
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}
		
		if (grp.isDeleteConfirmer()) {
			ToggleButton delObjToggle = new ToggleButton("Удалённые объекты");
			delObjToggle.setValue(false);
			toolBar.add(delObjToggle);
			buttons.add(delObjToggle);
			toolBar.add(new SeparatorToolItem());

			delObjTab = new DeletedObjectsPanel();
			layout.add(delObjTab);
			
			delObjToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(delObjTab);
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}
		
		ToggleButton chatToggle = new ToggleButton("Чат");
		chatToggle.setValue(false);
		toolBar.add(chatToggle);
		buttons.add(chatToggle);
		toolBar.add(new SeparatorToolItem());

		chatTab = new ChatPanel(user.getName());
		layout.add(chatTab);
		
		chatToggle.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				layout.setActiveWidget(chatTab);
				for (int idx = 0; idx < buttons.size(); idx++) {
					buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
				}
			}
		});
		
		if (grp.isTaskApprover()) {
			ToggleButton chatLogToggle = new ToggleButton("История чата");
			chatLogToggle.setValue(false);
			toolBar.add(chatLogToggle);
			buttons.add(chatLogToggle);
			toolBar.add(new SeparatorToolItem());

			chatLogTab = new ChatLogPanel();
			layout.add(chatLogTab);
			
			chatLogToggle.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					layout.setActiveWidget(chatLogTab);
					((VBoxLayoutContainer)chatLogTab).forceLayout();
					for (int idx = 0; idx < buttons.size(); idx++) {
						buttons.get(idx).setValue(buttons.get(idx) == event.getSource());
					}
				}
			});
		}
		
		toolBar.add(new FillToolItem());
		Label userName = new Label(user.getName());
		userName.setStyleName("username");
		toolBar.add(userName);
		toolBar.add(new SeparatorToolItem());
		
		TextButton logout = new TextButton("Выход");
		toolBar.add(logout);
		logout.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Cookies.removeCookie("sid");
				LoginService.Util.getInstance().logout(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {}

					@Override
					public void onSuccess(Void result) {
						showLoginWindow("", "");
					}
				});
				((ChatPanel) chatTab).sendLogoutNotification();
				atmosphere.unsubscribe();
			}
		});
		
		layout.setActiveWidget(evtTab);
		
		topLevelContainer.add(toolBar, new VerticalLayoutData(1, -1));
		topLevelContainer.add(layout, new VerticalLayoutData(1, 1));

		if (loginWindow != null)
			loginWindow.hide();
		
		Viewport viewport = new Viewport();
		viewport.add(topLevelContainer);

		RootPanel.get().add(viewport);
		
		setupAtmosphere();
	}
	
	private void showLoginWindow(String message, String userName) {
		RootPanel.get().clear();
		
		loginWindow = new Window();
		loginWindow.setHeadingText("Вход в \"ООО\" Техстандарт");
//	      window.getHeader().setIcon(DesktopImages.INSTANCE.door_in());
		loginWindow.setPixelSize(200, 210);
		loginWindow.setButtonAlign(BoxLayoutPack.END);
		loginWindow.setModal(true);
		loginWindow.setBlinkModal(true);
		loginWindow.setClosable(false);
		loginWindow.setOnEsc(false);

		VerticalLayoutContainer loginContainer = new VerticalLayoutContainer();
//		ContentPanel pnl = new ContentPanel();
//		pnl.setPixelSize(130, 130);
//		pnl.setHeaderVisible(false);
//		pnl.setBodyStyleName("logo");
//		loginContainer.add(pnl);
		
		if (message != null && !message.isEmpty()) {
			Label msgLabel = new Label(message);
			msgLabel.setStyleName("errormessage");
			loginContainer.add(msgLabel, new VerticalLayoutData(1, -1, new Margins(5)));
		}
		
		userNameField = new TextField();
		FieldLabel userNameFieldLabel = new FieldLabel(userNameField, "Имя пользователя");
		userNameFieldLabel.setLabelAlign(LabelAlign.TOP);
		loginContainer.add(userNameFieldLabel, new VerticalLayoutData(1, -1, new Margins(5)));
		userNameField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					doLogin();
				}
			}
		});
		// Если есть сообщение об ошибке, оставим в поле имени пользователя последнее значение
		if (!userName.isEmpty())
			userNameField.setValue(userName);
		else
			userNameField.setValue(Cookies.getCookie("login_name"));
		
		passwordField = new PasswordField();
		FieldLabel passwordFieldLabel = new FieldLabel(passwordField, "Пароль");
		passwordFieldLabel.setLabelAlign(LabelAlign.TOP);
		passwordField.setSelectOnFocus(true);
		passwordField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					doLogin();
				}
			}
		});
		loginContainer.add(passwordFieldLabel, new VerticalLayoutData(1, -1, new Margins(5)));

		loginWindow.add(loginContainer);
		
		TextButton loginButton = new TextButton("Войти");
		loginButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				doLogin();
			}
		});
		
		loginWindow.addButton(loginButton);
		loginWindow.show();
//		passwordField.focus();
		loginWindow.setFocusWidget(passwordField);
	}
	
	private void doLogin() {
		final String userName = (userNameField.getCurrentValue() == null || userNameField.getCurrentValue().isEmpty())?null:userNameField.getCurrentValue().trim();
		String password = (passwordField.getCurrentValue() == null || passwordField.getCurrentValue().isEmpty())?null:passwordField.getCurrentValue().trim();
		if (userName == null || password == null)
			return;
		LoginService.Util.getInstance().loginServer(userName, password, new AsyncCallback<UserDTO>()
			    {
			        @Override
			        public void onFailure(Throwable caught)
			        {
			        	String message = "";
			        	if (caught instanceof LoginErrorException)
			        		message = caught.getMessage();
			        	showLoginWindow(message, userName);
			        }
			 
			        @Override
			        public void onSuccess(UserDTO result)
			        {
			        	if (result == null)
			            {
			            	showLoginWindow("", "");
			            } else  {
			                if (result.getLoggedIn()) {
			                	Cookies.setCookie("login_name", userNameField.getCurrentValue().trim());
			                	DateWrapper w = new DateWrapper();
			                	// Печенька протухнет через 3 часа
	                            Cookies.setCookie("sid", result.getSessionId(), w.addHours(3).asDate(), null, "/", false);
			                	user = result;
//			                	showMainScreen();
			                	checkOutdatedTasks();
			                } else {
			                	showLoginWindow("", "");
			                }
			            }
			        }
			 
			    });		
	}
	
	private void setupAtmosphere() {
    	AutoBeanClientSerializer json_serializer = new AutoBeanClientSerializer();
        json_serializer.registerBeanFactory(beanFactory, Notification.class);        
                       
        // setup JSON Atmosphere connection
        AtmosphereRequestConfig jsonRequestConfig = AtmosphereRequestConfig.create(json_serializer);
        jsonRequestConfig.setUrl(GWT.getModuleBaseURL() + "atmosphere/notifications");
        jsonRequestConfig.setContentType("application/json; charset=UTF-8");
        jsonRequestConfig.setTransport(AtmosphereRequestConfig.Transport.STREAMING);
        jsonRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
        jsonRequestConfig.setOpenHandler(new AtmosphereOpenHandler() {
            @Override
            public void onOpen(AtmosphereResponse response) {
//                System.out.println("Notification JSON Connection opened");
            }
        });
        jsonRequestConfig.setCloseHandler(new AtmosphereCloseHandler() {
            @Override
            public void onClose(AtmosphereResponse response) {
//            	System.out.println("JSON Connection closed");
            }
        });
        jsonRequestConfig.setMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
                List<Notification> events = response.getMessages();
                for (Notification message : events) {
                	showNotification(message.getMessage());
                	((EventsPanel) evtTab).forceRefresh();
                	((DeletedObjectsPanel) delObjTab).forceRefresh();
                }
            }
        });
        jsonRequestConfig.setHeader("userid", String.valueOf(user.getEmployeeId()));
        
        atmosphere = Atmosphere.create();
        jsonRequest = atmosphere.subscribe(jsonRequestConfig);
    }
	
	public native void  showNotification(String text) /*-{
	if (!("Notification" in window)) {
	    alert("Ваш браузер не поддерживает отображение уведомлений.");
	  }

	  // Let's check if the user is okay to get some notification
	  else if (Notification.permission === "granted") {
	    // If it's okay let's create a notification
	    var notification = new Notification(text, {icon:"images/logo_small.jpg"});
	  }

	  // Otherwise, we need to ask the user for permission
	  // Note, Chrome does not implement the permission static property
	  // So we have to check for NOT 'denied' instead of 'default'
	  else if (Notification.permission !== 'denied') {
	    Notification.requestPermission(function (permission) {

	      // Whatever the user answers, we make sure we store the information
	      if(!('permission' in Notification)) {
	        Notification.permission = permission;
	      }

	      // If the user is okay, let's create a notification
	      if (permission === "granted") {
	        var notification = new Notification(text, {icon:"images/logo_small.jpg"});
	      }
	    });
	  }
	}-*/;
}
