package ru.techstandard.client;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.atmosphere.gwt20.client.Atmosphere;
import org.atmosphere.gwt20.client.AtmosphereCloseHandler;
import org.atmosphere.gwt20.client.AtmosphereMessageHandler;
import org.atmosphere.gwt20.client.AtmosphereOpenHandler;
import org.atmosphere.gwt20.client.AtmosphereRequest;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig;
import org.atmosphere.gwt20.client.AtmosphereResponse;
import org.atmosphere.gwt20.client.AutoBeanClientSerializer;

import ru.techstandard.client.model.ChatMessage;
import ru.techstandard.client.model.ChatUser;
import ru.techstandard.client.model.ChatUser.ChatUserProps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RichTextArea;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

public class ChatPanel extends VBoxLayoutContainer {
	static final String MESSAGE_JOINED_ROOM = "присоединяется к чату.";
	static final String MESSAGE_LEFT_ROOM = "покидает чат.";
	static final String MESSAGE_DROPPED = "отключается из-за потери связи.";
	static final String MESSAGE_TIMEOUT = "Произошла потеря связи с сервером. Обновите страницу.";
	static final String MESSAGE_ROOM_CONNECTED = "[Вы вошли в чат]";
	static final String MESSAGE_ROOM_DISCONNECTED = "[Вы вышли из чата]";
	static final String MESSAGE_ROOM_ERROR = "Ошибка: ";
	static final String COLOR_SYSTEM_MESSAGE = "grey";
	static final String COLOR_MESSAGE_SELF = "MediumBlue";
	static final String COLOR_MESSAGE_OTHERS = "CornflowerBlue";

	int count = 0;

	DateTimeFormat timeFormat = DateTimeFormat.getFormat("[dd.MM.yy HH:mm]");
	String author;

	String room="room1";

	VerticalLayoutContainer chatLog;
	RichTextArea chat;
	TextField msg;
	TextButton sendBtn;

	ListStore<ChatUser> userStore;
	Grid<ChatUser> userGrid;
	
	private MyBeanFactory beanFactory = GWT.create(MyBeanFactory.class);
	AtmosphereRequest jsonRequest;
	
	public ChatPanel(String login) {
		super();
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
		author = login;
		
		HorizontalLayoutContainer topHLC = new HorizontalLayoutContainer();
		
		ContentPanel chatLogPanel = new FramedPanel();
		chatLogPanel.setHeaderVisible(false);
		chatLogPanel.setCollapsible(false);
		
		chatLog = new VerticalLayoutContainer();
		chatLog.setStyleName("chatlog");
		chatLog.setBorders(true);
		chatLog.setAdjustForScroll(true);
		chatLog.getScrollSupport().setScrollMode(ScrollMode.AUTO);

		chatLogPanel.setWidget(chatLog);
		
		topHLC.add(chatLogPanel, new HorizontalLayoutData(1,1, new Margins(0, 0, 0, 0)));
		
		ChatUserProps chatUserProps = GWT.create(ChatUserProps.class);

		userStore = new ListStore<ChatUser>(new ModelKeyProvider<ChatUser>() {
					@Override
					public String getKey(ChatUser item) {
						return item.getName();
					}
				});
		
		ColumnConfig<ChatUser, String> nameColumn = new ColumnConfig<ChatUser, String>(chatUserProps.name(), 10, "Список пользователей в чате");
		nameColumn.setMenuDisabled(true);
		
		List<ColumnConfig<ChatUser, ?>> el = new ArrayList<ColumnConfig<ChatUser, ?>>();
		el.add(nameColumn);
		
		ColumnModel<ChatUser> ucm = new ColumnModel<ChatUser>(el);
		
		userGrid = new Grid<ChatUser>(userStore, ucm) {};
		userGrid.setWidth(200);
		userGrid.setBorders(true);
		userGrid.setLoadMask(true);
		userGrid.getView().setForceFit(true);
		userGrid.getView().setAutoExpandColumn(nameColumn);
		userGrid.getView().setStripeRows(false);
		userGrid.getView().setColumnLines(false);
		userGrid.getSelectionModel().addSelectionHandler(new SelectionHandler<ChatUser>() {
			@Override
			public void onSelection(SelectionEvent<ChatUser> event) {
//				System.out.println("selected item is "+(event.getSelectedItem().getName()));
				if (!event.getSelectedItem().getName().equals(author)) {
//					System.out.println("pushing recepient to input");
					String recepient = event.getSelectedItem().getName();
					msg.reset();
					msg.setValue(">>"+recepient+": ", true, true);
					
				}
				Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
		            @Override
		            public void execute() {
//		            	System.out.println("deselecting");
		            	userGrid.getSelectionModel().deselectAll();
						msg.focus();
		            }
				});
			}
		});
		
		ContentPanel chatUsersPanel = new FramedPanel();
		chatUsersPanel.setHeaderVisible(false);
		chatUsersPanel.setWidget(userGrid);
		
		topHLC.add(chatUsersPanel, new HorizontalLayoutData(-1, 1));
		
		BoxLayoutData flex1 = new BoxLayoutData(new Margins(0));
		flex1.setFlex(10);
		this.add(topHLC, flex1);
		
		ContentPanel messagePanel = new FramedPanel();
		messagePanel.setHeight(45);
		messagePanel.setHeaderVisible(false);
		messagePanel.setCollapsible(false);
		
		HorizontalLayoutContainer hlc = new HorizontalLayoutContainer();
		messagePanel.setWidget(hlc);
	 
		msg = new TextField();
		msg.setEmptyText("Введите сообщение и нажмите \"Ввод\" или кнопку \"Отправить\"");
		msg.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			            @Override
			            public void execute() {
			            	sendMessage(msg.getCurrentValue());
			            	msg.setValue(null, true, true);
			            	msg.focus();
			            }
			        });
				}
			}
		});
		
		hlc.add(msg, new HorizontalLayoutData(1, 1, new Margins(0, 10, 0, 0)));
		
		sendBtn = new TextButton("Отправить");
		sendBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				sendMessage(msg.getCurrentValue());
            	msg.setValue(null, true, true);
            	msg.focus();
			}
		});
		hlc.add(sendBtn, new HorizontalLayoutData(-1, -1));
				
		BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
		flex2.setFlex(0);
		this.add(messagePanel, flex2);
		msg.focus();
		
		changeRoom(room);
		
		this.addShowHandler(new ShowHandler() {
			@Override
			public void onShow(ShowEvent event) {
				msg.focus();
			}
		});
		
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
		    public void onWindowClosing(Window.ClosingEvent closingEvent) {
		    	sendLogoutNotification();
		    }
		});
		
		setupAtmosphere();
	}
    

	void sendMessage(String message) {
		if (message == null || message.trim().length() == 0)
			return;
		Date ts = new Date();		
		try {
			ChatMessage msg = beanFactory.create(ChatMessage.class).as();
			msg.setAuthor(author);
			msg.setTimeStamp(ts.getTime());
			msg.setSystemMessage(false);
			msg.setMessage(message);
			jsonRequest.push(msg);
		} catch (SerializationException ex) {
			System.out.println("Failed to serialize message"+ex.getMessage());
		}
    }
    
    void changeRoom(final String newRoom) {
//	    chatService.login(author, new AsyncCallback<String>() {
//
//	        @Override
//	        public void onSuccess(String result) {
//            	addChatLine(MESSAGE_ROOM_CONNECTED, COLOR_SYSTEM_MESSAGE);
//            	chatService.getLoggedUsers(new AsyncCallback<List<ChatUser>>() {
//					@Override
//					public void onFailure(Throwable caught) {}
//
//					@Override
//					public void onSuccess(List<ChatUser> result) {
//						userStore.replaceAll(result);
//					}
//				});
//	            initChatListener();
//	        }
//
//	        @Override
//	        public void onFailure(Throwable caught) {
//	            System.err.println(caught.getMessage());
//	        }
//	    });
    }
    
	void clearChat() {
//        chat.setText("");
    }
    
    void addChatLine(String line, String color) {
        Label msg = new Label();
        msg.getElement().getStyle().setColor(color);
        msg.getElement().setInnerHTML(line);
        chatLog.add(msg);
        chatLog.forceLayout();
        chatLog.getScrollSupport().scrollToBottom();
    }
    
    private void setupAtmosphere() {
    	AutoBeanClientSerializer json_serializer = new AutoBeanClientSerializer();
        json_serializer.registerBeanFactory(beanFactory, ChatMessage.class);        
                       
        // setup JSON Atmosphere connection
        AtmosphereRequestConfig jsonRequestConfig = AtmosphereRequestConfig.create(json_serializer);
        jsonRequestConfig.setUrl(GWT.getModuleBaseURL() + "atmosphere/chat");
        jsonRequestConfig.setContentType("application/json; charset=UTF-8");
        jsonRequestConfig.setTransport(AtmosphereRequestConfig.Transport.STREAMING);
        jsonRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
        jsonRequestConfig.setOpenHandler(new AtmosphereOpenHandler() {
            @Override
            public void onOpen(AtmosphereResponse response) {
//                System.out.println("JSON Connection opened");
                try {
	    			ChatMessage message = beanFactory.create(ChatMessage.class).as();
	    			Date ts = new Date();
	    			message.setAuthor(author);
	    			message.setTimeStamp(ts.getTime());
	    			message.setSystemMessage(true);
	    			message.setMessage(author + " " + MESSAGE_JOINED_ROOM);
	    			jsonRequest.push(message);
	    		} catch (SerializationException ex) {
	    			System.out.println("Failed to serialize message"+ex.getMessage());
	    		}
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
                List<ChatMessage> events = response.getMessages();
                for (ChatMessage message : events) {
                	String user = message.getAuthor();
                	String color = (user.equals(author) ? COLOR_MESSAGE_SELF : COLOR_MESSAGE_OTHERS);
                	String line;
                	if (message.isSystemMessage()) {
                		color = COLOR_SYSTEM_MESSAGE;
                		line = SafeHtmlUtils.htmlEscape(message.getMessage());
                	} else {
                		line = timeFormat.format(new Date(message.getTimeStamp())) + " <b>" + user + ":" + "</b> " + SafeHtmlUtils.htmlEscape(message.getMessage());
                	}
                	addChatLine(line, color);
                	updateUserList(message.getLoggedUsers());
                }
            }
        });
        jsonRequestConfig.setHeader("username", author);
        
        Atmosphere atmosphere = Atmosphere.create();
        jsonRequest = atmosphere.subscribe(jsonRequestConfig);

    }
    
    private void updateUserList(List<String> users) {
    	List<ChatUser> chatUsers = new ArrayList<ChatUser>();
    	for (String name: users) {
    		ChatUser user = new ChatUser(name);
    		chatUsers.add(user);
    	}
    	userStore.replaceAll(chatUsers);
    }
    
    public void sendLogoutNotification() {
    	try {
			ChatMessage message = beanFactory.create(ChatMessage.class).as();
			Date ts = new Date();
			message.setAuthor(author);
			message.setTimeStamp(ts.getTime());
			message.setSystemMessage(true);
			message.setMessage(author + " " + MESSAGE_LEFT_ROOM);
			jsonRequest.push(message);
		} catch (SerializationException ex) {
			System.out.println("Failed to serialize message"+ex.getMessage());
		}
    }
}