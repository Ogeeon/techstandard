package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Event;
import ru.techstandard.client.model.Event.EventProps;
import ru.techstandard.client.model.UserDTO;
import ru.techstandard.shared.EventService;
import ru.techstandard.shared.EventServiceAsync;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class EventsPanel extends VBoxLayoutContainer {
	private final EventServiceAsync eventService = GWT.create(EventService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<PagingLoadConfig, PagingLoadResult<Event>> gridLoader = null;

	Widget eventsGrid;
	
	UserDTO user;
	
	public EventsPanel (UserDTO loggedUser) {
		super();
		user = loggedUser;
		
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
		ToolBar topToolBar = new ToolBar();
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);
        this.add(topToolBar, flex);

		TextButton removeEventBtn = new TextButton("Отметить как прочитанное");
		removeEventBtn.setIcon(IMAGES.apply2());
		topToolBar.add(removeEventBtn);
		
		removeEventBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
				Grid<Event> grid = ((Component) eventsGrid).getData("grid");
				List<Event> recs = grid.getSelectionModel().getSelectedItems();
				if (recs.size() == 0) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите событие, которое хотите отметить как прочитанное.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				for (int idx = 0; idx < recs.size(); idx++)
					eventService.removeEvent(recs.get(idx).getId(), new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
							d.show();
						}
						@Override
						public void onSuccess(Void result) {
							PagingToolBar toolBar = ((Component) eventsGrid).getData("pagerToolbar");
							toolBar.refresh();
							updateEventCount();
						}
					});
	        }
		});
		
		
		ContentPanel eventsPanel = new ContentPanel();
		eventsPanel.setHeadingText("Список событий");
		eventsPanel.setBorders(true);
		eventsPanel.setBodyBorder(false);
		
        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));

        eventsGrid = getEventsGrid(); 
        eventsPanel.setWidget(eventsGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
        flex2.setFlex(5);
        this.add(eventsPanel, flex2);
        
        updateEventCount();
	}


	private Widget getEventsGrid () {
		
		RpcProxy<PagingLoadConfig, PagingLoadResult<Event>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<Event>>() {
			@Override
			public void load(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Event>> callback) {
				eventService.getEvents(user.getEmployeeId(), loadConfig, callback);
			}
		};

		EventProps props = GWT.create(EventProps.class);

		ListStore<Event> store = new ListStore<Event>(new ModelKeyProvider<Event>() {
					@Override
					public String getKey(Event item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<PagingLoadConfig, PagingLoadResult<Event>>(proxy) {
	        @Override
	        protected PagingLoadConfig newLoadConfig() {
	          return new PagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, Event, PagingLoadResult<Event>>(store));

		ColumnConfig<Event, Date> createdColumn = new ColumnConfig<Event, Date>(props.created(), 1, "Дата");
		createdColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss")));
		ColumnConfig<Event, String> titleColumn = new ColumnConfig<Event, String>(props.title(), 2, "Тип события");
		ColumnConfig<Event, String> descriptionColumn = new ColumnConfig<Event, String>(props.description(), 8, "Описание события");
		
		List<ColumnConfig<Event, ?>> l = new ArrayList<ColumnConfig<Event, ?>>();
		l.add(createdColumn);
		l.add(titleColumn);
		l.add(descriptionColumn);
	
		ColumnModel<Event> cm = new ColumnModel<Event>(l);

		final Grid<Event> eventsGrid = new Grid<Event>(store, cm) {
			@Override
			protected void onAfterFirstAttach() {
				super.onAfterFirstAttach();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						gridLoader.load();
					}
				});
			}
		};

		eventsGrid.setLoadMask(true);
		eventsGrid.setLoader(gridLoader);
		eventsGrid.getView().setForceFit(true);
		eventsGrid.getView().setAutoExpandColumn(titleColumn);
		eventsGrid.getView().setStripeRows(true);
		eventsGrid.getView().setColumnLines(true);
		eventsGrid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(false);
		con.add(eventsGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar toolBar = new PagingToolBar(30);
		toolBar.addStyleName(ThemeStyles.get().style().borderTop());
		toolBar.getElement().getStyle().setProperty("borderBottom", "none");
		toolBar.bind(gridLoader);

		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.setData("grid", eventsGrid);

		con.setData("pagerToolbar", toolBar);
		return con;
	}
	
	public void forceRefresh() {
		PagingToolBar toolBar = ((Component) eventsGrid).getData("pagerToolbar");
		toolBar.refresh();
		updateEventCount();
	}
	
	private void updateEventCount() {
		eventService.getEventsCount(user.getEmployeeId(), new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось получить данные о количестве уведомлений.");
				d.show();
			}
			@Override
			public void onSuccess(Integer result) {
				String name = "ООО \"ТехСтандарт\"";
				String title = result == 0 ? name : "(" + result + ") " + name;
				Window.setTitle(title);
			}
		});		
	}
}
