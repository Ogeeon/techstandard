package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.ChatMsg;
import ru.techstandard.client.model.ChatMsgProps;
import ru.techstandard.shared.ChatLoggerService;
import ru.techstandard.shared.ChatLoggerServiceAsync;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.DatePicker;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent.ParseErrorHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.DateFilter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class ChatLogPanel extends VBoxLayoutContainer {
	private final ChatLoggerServiceAsync chatLoggerService = GWT.create(ChatLoggerService.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ChatMsg>> gridLoader = null;
	
	TextField senderName;
	TextField message;
	DateField dateFrom;
	DateField dateTo;
	
	StringFilter<ChatMsg> senderNameFilter;
	StringFilter<ChatMsg> messageFilter;
	DateFilter<ChatMsg> dateFilter;
	
	Widget chatLogGrid;
	
	public ChatLogPanel() {
		super();
		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
		filtersPanel.setPixelSize(-1, 100);
			 
		VerticalLayoutContainer filterContainerA = new VerticalLayoutContainer();
		filterContainerA.setWidth(300);
	    VerticalLayoutContainer filterContainerB = new VerticalLayoutContainer();
	    filterContainerB.setWidth(200);
		
		int labelColWidth = 90;
		
		HBoxLayoutContainer filtersContainer = new HBoxLayoutContainer();
		filtersContainer.setPadding(new Padding(0));
		filtersContainer.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
		
		VerticalLayoutContainer fltBtnsContainer = new VerticalLayoutContainer();
		fltBtnsContainer.setPixelSize(100, 50);
	    
	    senderName = new TextField();
		FieldLabel senderNameLabel = new FieldLabel(senderName, "Отправитель");
		senderNameLabel.setLabelWidth(labelColWidth);
		senderName.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(senderNameLabel, new VerticalLayoutData(1, -1));
		
		message = new TextField();
		FieldLabel messageLabel = new FieldLabel(message, "Сообщение");
		messageLabel.setLabelWidth(labelColWidth);
		message.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(messageLabel, new VerticalLayoutData(1, -1));
		
		dateFrom = new DateField();
	    dateFrom.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    dateFrom.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    dateFrom.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    FieldLabel dateFromLabel = new FieldLabel(dateFrom, "Дата от");
	    dateFromLabel.setLabelWidth(labelColWidth);
	    filterContainerB.add(dateFromLabel, new VerticalLayoutData(1, -1));
		
		dateTo = new DateField();
	    dateTo.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
	    dateTo.addParseErrorHandler(new ParseErrorHandler() {
	    	@Override
	    	public void onParseError(ParseErrorEvent event) {
	    		Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
	    	}
	    });
	 
	    dateTo.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		applyFilters();
	    	}
	    });
	    FieldLabel dateToLabel = new FieldLabel(dateTo, "Дата до");
	    dateToLabel.setLabelWidth(labelColWidth);
	    filterContainerB.add(dateToLabel, new VerticalLayoutData(1, -1));
		
		filtersContainer.add(filterContainerA, new BoxLayoutData(new Margins(0, 15, 0, 0)));
		filtersContainer.add(filterContainerB, new BoxLayoutData(new Margins(0, 5, 0, 0)));
		
		TextButton resetFilters = new TextButton("Сбросить фильтры");
		resetFilters.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				resetFilters();
			}
		});
		fltBtnsContainer.add(resetFilters, new VerticalLayoutData(1, -1));
		 
		filtersContainer.add(fltBtnsContainer, new BoxLayoutData(new Margins(0, 5, 0, 0)));
		filtersPanel.add(filtersContainer);

		ContentPanel journalPanel = new ContentPanel();
		journalPanel.setHeadingText("История сообщений");
		journalPanel.setBorders(true);
		journalPanel.setBodyBorder(false);
		
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);

        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);

        chatLogGrid = getJournalGrid(); 
        journalPanel.setWidget(chatLogGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
        this.add(filtersPanel, flex);
        this.add(journalPanel, flex2);
        this.forceLayout();
	}
	
	private String getFilterValue(String fieldValue) {
		String value = fieldValue.trim();
		return value.length() == 0 ? null : value;
	}
	
	private void applyFilters() {
		if (gridLoader != null) {
			try {
				TextField field = (TextField) senderNameFilter.getMenu().getWidget(0);
				senderNameFilter.setActive(false, false);
				field.setValue(getFilterValue(senderName.getText()), true);
				senderNameFilter.setActive(true, false);
				
				field = (TextField) messageFilter.getMenu().getWidget(0);
				messageFilter.setActive(false, false);
				field.setValue(getFilterValue(message.getText()), true);
				messageFilter.setActive(true, false);
				
				dateFilter.setActive(false, false);
//				Конечная дата интервала
				CheckMenuItem before = (CheckMenuItem) dateFilter.getMenu().getWidget(0);
				DatePicker beforePicker = (DatePicker) before.getSubMenu().getWidget(0);
				if (dateTo.getValue() != null)
					beforePicker.setValue(dateTo.getValue());
				else
					before.setChecked(false, false);
//				Начальная дата интервала
				CheckMenuItem after = (CheckMenuItem) dateFilter.getMenu().getWidget(1);
				DatePicker afterPicker = (DatePicker) after.getSubMenu().getWidget(0);
				if (dateFrom.getValue() != null)
					afterPicker.setValue(dateFrom.getValue());
				else
					after.setChecked(false, false);				
				dateFilter.setActive(true, false);
				
			} catch (Exception e) {
				System.out.println("applyFilters got errored: "+e.getMessage()+" "+ e.toString());
			}
    	}
	}
	
	private void resetFilters() {
		senderName.setValue("");
		message.setValue("");
		dateFrom.setValue(null);
		dateTo.setValue(null);
		applyFilters();
	}

	private Widget getJournalGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<ChatMsg>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<ChatMsg>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<ChatMsg>> callback) {
				chatLoggerService.getChatMessages(loadConfig, callback);
			}
		};

		ChatMsgProps props = GWT.create(ChatMsgProps.class);

		ListStore<ChatMsg> store = new ListStore<ChatMsg>(new ModelKeyProvider<ChatMsg>() {
					@Override
					public String getKey(ChatMsg item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ChatMsg>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, ChatMsg, PagingLoadResult<ChatMsg>>(store));

		ColumnConfig<ChatMsg, Date> dateColumn = new ColumnConfig<ChatMsg, Date>(props.date(), 10, "Отправлено");
		dateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss")));
		ColumnConfig<ChatMsg, String> senderColumn = new ColumnConfig<ChatMsg, String>(props.author(), 10, "Отправитель");
		ColumnConfig<ChatMsg, String> messageColumn = new ColumnConfig<ChatMsg, String>(props.message(), 50, "Сообщение");
		
		
		List<ColumnConfig<ChatMsg, ?>> l = new ArrayList<ColumnConfig<ChatMsg, ?>>();
		l.add(dateColumn);
		l.add(senderColumn);
		l.add(messageColumn);
	
		ColumnModel<ChatMsg> cm = new ColumnModel<ChatMsg>(l);

		final Grid<ChatMsg> journalGrid = new Grid<ChatMsg>(store, cm) {
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

		journalGrid.setLoadMask(true);
		journalGrid.setLoader(gridLoader);
		journalGrid.getView().setForceFit(true);
		journalGrid.getView().setAutoExpandColumn(messageColumn);
		journalGrid.getView().setStripeRows(true);
		journalGrid.getView().setColumnLines(true);

		GridFilters<ChatMsg> filters = new GridFilters<ChatMsg>(gridLoader);
		filters.initPlugin(journalGrid);

		senderNameFilter = new StringFilter<ChatMsg>(props.author());
		filters.addFilter(senderNameFilter);
		messageFilter = new StringFilter<ChatMsg>(props.message());
		filters.addFilter(messageFilter);
		dateFilter = new DateFilter<ChatMsg>(props.date());
		filters.addFilter(dateFilter);

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(false);
		con.add(journalGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar toolBar = new PagingToolBar(30);
		toolBar.addStyleName(ThemeStyles.get().style().borderTop());
		toolBar.getElement().getStyle().setProperty("borderBottom", "none");
		toolBar.bind(gridLoader);

		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.setData("grid", journalGrid);

		con.setData("pagerToolbar", toolBar);
		return con;
	}
	
}
