package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.client.model.Request;
import ru.techstandard.client.model.RequestProps;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;
import ru.techstandard.shared.RequestService;
import ru.techstandard.shared.RequestServiceAsync;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
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
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class RequestsJournalPanel extends VBoxLayoutContainer {
	private final EmployeeServiceAsync emplooyeeService = GWT.create(EmployeeService.class);
	private final RequestServiceAsync requestService = GWT.create(RequestService.class);
	private final Images IMAGES = GWT.create(Images.class);
	PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Request>> gridLoader = null;
	
	TextField clientName;
	TextField description;
	ComboBox<Employee> responsible;
	
	StringFilter<Request> clientNameFilter;
	StringFilter<Request> descriptionFilter;
	StringFilter<Request> responsibleFilter;
	
	RequestInfoWindow requestInfoWindow;
	
	Widget requestGrid;
	AccessGroup group;
	int loggedUserId;
	
	public RequestsJournalPanel(int userId, AccessGroup accessGroup) {
		super();
		loggedUserId = userId;
		group = accessGroup;
		requestInfoWindow = new RequestInfoWindow();
	    final HideHandler editHideHandler = new HideHandler() {
	        @Override
	        public void onHide(HideEvent event) {
	          Window wnd = (Window) event.getSource();
	          if (wnd.getData("hideButton") == null)
	        	  return;
	          if (wnd.getData("hideButton").equals("save")) {
	        	  PagingToolBar toolbar = ((Component) requestGrid).getData("pagerToolbar");
	        	  toolbar.refresh();
	          }
	        }
	    };	
		requestInfoWindow.addHideHandler(editHideHandler);
		
		ContentPanel filtersPanel = new FramedPanel();
		filtersPanel.setHeadingText("Фильтры");
		filtersPanel.setCollapsible(false);
			 
		VerticalLayoutContainer filterContainerA = new VerticalLayoutContainer();
		filterContainerA.setWidth(400);
	    VerticalLayoutContainer filterContainerB = new VerticalLayoutContainer();
	    filterContainerB.setWidth(400);
		
		int labelColWidth = 150;
		
		HBoxLayoutContainer filtersContainer = new HBoxLayoutContainer();
		filtersContainer.setPadding(new Padding(0));
		filtersContainer.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
		
		VerticalLayoutContainer fltBtnsContainer = new VerticalLayoutContainer();
		fltBtnsContainer.setPixelSize(100, 50);
	    
	    clientName = new TextField();
		FieldLabel clientNameLabel = new FieldLabel(clientName, "Заказчик");
		clientNameLabel.setLabelWidth(labelColWidth);
		clientName.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(clientNameLabel, new VerticalLayoutData(1, -1));
		
		description = new TextField();
		FieldLabel descriptionLabel = new FieldLabel(description, "Содержание задачи");
		descriptionLabel.setLabelWidth(labelColWidth);
		description.addKeyUpHandler(new KeyUpHandler() {public void onKeyUp(KeyUpEvent event) { if (event.getNativeKeyCode()!=KeyCodes.KEY_TAB)applyFilters(); }});
		filterContainerA.add(descriptionLabel, new VerticalLayoutData(1, -1));
		
		EmployeeProps emplProps = GWT.create(EmployeeProps.class);
		final ListStore<Employee> emplStore = new ListStore<Employee>(emplProps.id());

		emplooyeeService.getAllEmployees(
				new AsyncCallback<List<Employee>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Requests:156 - "+caught.getMessage());
						d.show();
					}

					@Override
					public void onSuccess(List<Employee> result) {
						emplStore.addAll(result);
					}
				});

		responsible = new ComboBox<Employee>(emplStore, emplProps.nameLabel());
		responsible.addValueChangeHandler(new ValueChangeHandler<Employee>() {
			@Override
			public void onValueChange(ValueChangeEvent<Employee> event) {
				applyFilters();
			}
		});
		responsible.addSelectionHandler(new SelectionHandler<Employee>() {
			@Override
			public void onSelection(SelectionEvent<Employee> event) {
				applyFilters();				
			}
		});
		responsible.setAllowBlank(true);
		responsible.setForceSelection(true);
		FieldLabel respLabel = new FieldLabel(responsible, "Ответственный");
		respLabel.setLabelWidth(labelColWidth);
		filterContainerB.add(respLabel, new VerticalLayoutData(1, -1));
		
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
		journalPanel.setHeadingText("Журнал");
		journalPanel.setBorders(true);
		journalPanel.setBodyBorder(false);
		
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(1);

        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);

        requestGrid = getJournalGrid(); 
        journalPanel.setWidget(requestGrid);
		 
		this.setPadding(new Padding(0));
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
        flex.setFlex(0);
        this.add(filtersPanel, flex);

        flex2.setFlex(3);
        this.add(journalPanel, flex2);
	}
	
	private String getFilterValue(String fieldValue) {
		String value = fieldValue.trim();
		return value.length() == 0 ? null : value;
	}
	
	private void applyFilters() {
		if (gridLoader != null) {
			try {
				TextField field = (TextField) clientNameFilter.getMenu().getWidget(0);
				clientNameFilter.setActive(false, false);
				field.setValue(getFilterValue(clientName.getText()), true);
				clientNameFilter.setActive(true, false);
				
				field = (TextField) descriptionFilter.getMenu().getWidget(0);
				descriptionFilter.setActive(false, false);
				field.setValue(getFilterValue(description.getText()), true);
				descriptionFilter.setActive(true, false);
				
				field = (TextField) responsibleFilter.getMenu().getWidget(0);
				responsibleFilter.setActive(false, false);
				if (responsible.getCurrentValue() != null) {
					field.setValue(getFilterValue(responsible.getCurrentValue().getName()), true);
				} else {
					field.setValue(null);
				}
				responsibleFilter.setActive(true, false);
				
			} catch (Exception e) {
				System.out.println("applyFilters got errored: "+e.getMessage()+" "+ e.toString());
			}
    	}
	}
	
	private void resetFilters() {
		clientName.setValue("");
		description.setValue("");
		responsible.setValue(null);
		applyFilters();
	}

	private Widget getJournalGrid () {
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Request>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Request>>() {
			@Override
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Request>> callback) {
				requestService.getRequests(loadConfig, callback);
			}
		};

		RequestProps props = GWT.create(RequestProps.class);

		final ListStore<Request> store = new ListStore<Request>(new ModelKeyProvider<Request>() {
					@Override
					public String getKey(Request item) {
						return "" + item.getId();
					}
				});

		gridLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Request>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
	      
		gridLoader.setRemoteSort(true);
		gridLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Request, PagingLoadResult<Request>>(store));

//		AbstractCell<String> stringCell = new AbstractCell<String>() {
//			@Override
//			public void render(Context context, String value, SafeHtmlBuilder sb) {
//				Request currRequest = store.get(context.getIndex());
//				int responsibleId = currRequest.getResponsibleId();
//				String style = "";
//				if (loggedUserId == responsibleId)
//					style = "style='color: yellow'";
//				sb.appendHtmlConstant("<span " + style + ">" + value + "</span>");
//			}
//		};
		
		ColumnConfig<Request, String> descriptionColumn = new ColumnConfig<Request, String>(props.description(), 50, SafeHtmlUtils.fromTrustedString("Содержание задачи"));
		ColumnConfig<Request, String> clientColumn = new ColumnConfig<Request, String>(props.clientName(), 20, SafeHtmlUtils.fromTrustedString("Заказчик"));
		ColumnConfig<Request, String> responsibleColumn = new ColumnConfig<Request, String>(props.responsibleName(), 20, SafeHtmlUtils.fromTrustedString("Исполнитель"));
		ColumnConfig<Request, Date> dueDateColumn = new ColumnConfig<Request, Date>(props.dueDate(), 10, SafeHtmlUtils.fromTrustedString("Срок исполнения"));
		dueDateColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		
		List<ColumnConfig<Request, ?>> l = new ArrayList<ColumnConfig<Request, ?>>();
		l.add(descriptionColumn);
		l.add(clientColumn);
		l.add(responsibleColumn);
		l.add(dueDateColumn);
	
		ColumnModel<Request> cm = new ColumnModel<Request>(l);

		final Grid<Request> journalGrid = new Grid<Request>(store, cm) {
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
		journalGrid.getView().setViewConfig(new GridViewConfig<Request>() {
			@Override
			public String getColStyle(Request req, ValueProvider<? super Request, ?> valueProvider, int rowIndex, int colIndex) {
				int responsibleId = req.getResponsibleId();
				String style = null;
				if (loggedUserId == responsibleId)
					style = "yellow";
				else if (rowIndex % 2 == 1)
					style = "darkened";
				return style;
			}
			@Override
			public String getRowStyle(Request model, int rowIndex) {return null;}
		});
		
		journalGrid.setLoadMask(true);
		journalGrid.setLoader(gridLoader);
		journalGrid.getView().setForceFit(true);
		journalGrid.getView().setAutoExpandColumn(descriptionColumn);
		journalGrid.getView().setColumnLines(true);
		journalGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		journalGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Request rec = journalGrid.getSelectionModel().getSelectedItem();
				requestInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "requests"), group.isAllowed(Constants.ACCESS_PRINT, "requests"));
			}
		});
		
		GridFilters<Request> filters = new GridFilters<Request>(gridLoader);
		filters.initPlugin(journalGrid);

		clientNameFilter = new StringFilter<Request>(props.clientName());
		filters.addFilter(clientNameFilter);
		descriptionFilter = new StringFilter<Request>(props.description());
		filters.addFilter(descriptionFilter);
		responsibleFilter = new StringFilter<Request>(props.responsibleName());
		filters.addFilter(responsibleFilter);

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
	
	public FlowLayoutContainer getButtonsContainer() {
		FlowLayoutContainer flc = new FlowLayoutContainer();
		
		TextButton viewJournRecBtn = new TextButton("Просмотреть запись");
		viewJournRecBtn.setIcon(IMAGES.view());
		flc.add(viewJournRecBtn, new MarginData(new Margins(0, 0, -5, 5)));
		
		viewJournRecBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Request> grid = ((Component) requestGrid).getData("grid");
				Request rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание", "Пожалуйста, выберите запись, которую Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				requestInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "requests"), group.isAllowed(Constants.ACCESS_PRINT, "requests"));
			}
		});
		
		if (group.isAllowed(Constants.ACCESS_INSERT, "requests")) {
			TextButton addJournRecBtn = new TextButton("Добавить запись");
			addJournRecBtn.setIcon(IMAGES.addRow());
			flc.add(addJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));

			addJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					final Request tmpRequest = new Request(0);
					requestService.addRequest(tmpRequest, new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом документе.");
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							tmpRequest.setId(result);
							requestInfoWindow.editInfo(tmpRequest, true, group.isAllowed(Constants.ACCESS_PRINT, "requests"));
						}
					});
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_UPDATE, "requests")) {
			TextButton editJournRecBtn = new TextButton("Редактировать запись");
			editJournRecBtn.setIcon(IMAGES.editRow());
			flc.add(editJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));

			editJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Request> grid = ((Component) requestGrid).getData("grid");
					Request rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите отредактировать.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					requestInfoWindow.editInfo(rec, false, group.isAllowed(Constants.ACCESS_PRINT, "requests"));
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_DELETE, "requests")) {
			TextButton deleteJournRecBtn = new TextButton("Удалить запись");
			deleteJournRecBtn.setIcon(IMAGES.deleteRow());
			flc.add(deleteJournRecBtn, new MarginData(new Margins(0, 0, -5, 0)));

			final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
				@Override
				public void onDialogHide(DialogHideEvent event) {
					if (event.getHideButton() == PredefinedButton.YES) {
						Grid<Request> grid = ((Component) requestGrid).getData("grid");
						Request rec = grid.getSelectionModel().getSelectedItem();	        	  
						requestService.deleteRequest(rec.getId(), !group.isDeleteConfirmer(), new AsyncCallback<Boolean>(){
							@Override
							public void onFailure(Throwable caught) {
								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить запись из журнала.");
								d.show();
							}
							@Override
							public void onSuccess(Boolean result) {
								PagingToolBar toolbar = ((Component) requestGrid).getData("pagerToolbar");
								toolbar.refresh();
							}
						});
					}
				}
			};

			deleteJournRecBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Request> grid = ((Component) requestGrid).getData("grid");
					Request rec = grid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите удалить.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранную запись?");
					box.setIcon(IMAGES.warning());
					box.addDialogHideHandler(deleteHideHandler);
					box.show();
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_PRINT, "requests")) {
			TextButton printJournBtn = new TextButton("Печать");
			printJournBtn.setIcon(IMAGES.print());
			flc.add(printJournBtn, new MarginData(new Margins(0, 0, -5, 0)));

			printJournBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					Grid<Request> grid = ((Component) requestGrid).getData("grid");
					FilterPagingLoadConfig config = (FilterPagingLoadConfig) grid.getLoader().getLastLoadConfig();
					requestService.getPrintableRequestList(config, new AsyncCallback<String>() {
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
		}
		
		return flc;
	}
}
