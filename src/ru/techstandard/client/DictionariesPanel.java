package ru.techstandard.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.ClientProps;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

@SuppressWarnings("unchecked")
public class DictionariesPanel extends VBoxLayoutContainer {
	
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final Images IMAGES = GWT.create(Images.class);
	ComboBox<DictionaryRecord> dictionarySelector;
	
	VBoxLayoutContainer thisContainer;
	CardLayoutContainer cardLayout;
    TextButton printDictBtn;
    TextButton printContractBtn;
    TextButton viewItemBtn;
    
    private List<Integer> dictionaries;
    private List<Integer> clientLists;
    
    private Map<Integer, PagingLoader<PagingLoadConfig, PagingLoadResult<DictionaryRecord>>> loaders;
    private Map<Integer, String> colNames;
    private Map<Integer, Widget> grids;
    private Map<Integer, String> titles;
    private Map<Integer, String> msgs;
    private Map<Integer, String> errMsgs;
    
	ClientInfoWindow clientInfoWindow;
	TemplateContractWindow templateContractWindow;
	
	AccessGroup group;
	
	public DictionariesPanel (AccessGroup accessGroup) {
		super();
		group = accessGroup;
		thisContainer = this;
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		
		if (clientInfoWindow == null) {
	    	clientInfoWindow = new ClientInfoWindow(!group.isDeleteConfirmer());
	    	clientInfoWindow.addHideHandler(new HideHandler() {
				@Override
				public void onHide(HideEvent event) {
					int index = dictionarySelector.getValue().getId();
					String grd = "";
					// Вообще-то выход из окна просмотра/редактирования клиента подразумевает, что выбран режим контры/возм.контры, 
					// но на всякий случай добавим выход из обработчика, если это не так. 
					if (index == Constants.DICT_CLIENTS) 
						grd = "clientGrid";
					else if (index == Constants.DICT_PROB_CLIENTS)
						grd = "probClientGrid";
					else
						return;
					Grid<Client> grid = (Grid<Client>) thisContainer.getData(grd);
					//	раз сюда попали, значит, можно снимать регистрацию обработчика
					Window wnd = (Window) event.getSource();
					if (wnd.getData("hideButton") == null)
						return;
					if (wnd.getData("hideButton").equals("save")) {
						PagingToolBar toolbar = ((Component) grid.getParent()).getData("pagerToolbar");
						toolbar.refresh();
					}
				}
			});
	    }
		if (templateContractWindow == null) {
			templateContractWindow = new TemplateContractWindow();
	    }
		
		dictionaries = new ArrayList<Integer>();
		dictionaries.add(Constants.DICT_ATTACHTYPES);
		dictionaries.add(Constants.DICT_CHECKERS);
		dictionaries.add(Constants.DICT_EVAL_FIELDS);
		dictionaries.add(Constants.DICT_OBJTYPES);
		dictionaries.add(Constants.DICT_POSITIONS);
		dictionaries.add(Constants.DICT_WORKSUBJS);
		dictionaries.add(Constants.DICT_TASKTYPES);
		
	    clientLists = new ArrayList<Integer>();
	    clientLists.add(Constants.DICT_CLIENTS);
	    clientLists.add(Constants.DICT_PROB_CLIENTS);
	    
	    loaders = new HashMap<Integer, PagingLoader<PagingLoadConfig, PagingLoadResult<DictionaryRecord>>>();
	    
	    colNames = new HashMap<Integer, String>();
	    colNames.put(Constants.DICT_WORKSUBJS, "Формулировка предмета договора");
	    colNames.put(Constants.DICT_OBJTYPES, "Тип объекта");
	    colNames.put(Constants.DICT_CHECKERS, "Место проведения поверки");
	    colNames.put(Constants.DICT_ATTACHTYPES, "Тип документа");
	    colNames.put(Constants.DICT_POSITIONS, "Должность");
	    colNames.put(Constants.DICT_EVAL_FIELDS, "Область аттестации");
	    colNames.put(Constants.DICT_TASKTYPES, "Тип задания");
	    
	    cardLayout = new CardLayoutContainer();
	    grids = new HashMap<Integer, Widget>();
	    
	    for (int idx=0; idx < dictionaries.size(); idx++) {
	    	grids.put(dictionaries.get(idx), getDictGrid(dictionaries.get(idx)));
	    	cardLayout.add(grids.get(dictionaries.get(idx)));
	    }
        grids.put(Constants.DICT_CLIENTS, getClientsGrid(true));
        cardLayout.add(grids.get(Constants.DICT_CLIENTS));
        grids.put(Constants.DICT_PROB_CLIENTS, getClientsGrid(false));
        cardLayout.add(grids.get(Constants.DICT_PROB_CLIENTS));

        cardLayout.setActiveWidget(grids.get(Constants.DICT_CLIENTS));
        
        titles = new HashMap<Integer, String>();
        titles.put(Constants.DICT_WORKSUBJS, "Предмет договора");
        titles.put(Constants.DICT_OBJTYPES, "Тип объекта");
        titles.put(Constants.DICT_CHECKERS, "Место проведения поверки");
        titles.put(Constants.DICT_ATTACHTYPES, "Тип документа");
        titles.put(Constants.DICT_POSITIONS, "Должность");
        titles.put(Constants.DICT_EVAL_FIELDS, "Область аттестации");
        titles.put(Constants.DICT_TASKTYPES, "Тип задания");
        
        msgs = new HashMap<Integer, String>();
        msgs.put(Constants.DICT_WORKSUBJS, "Введите значение для справочника<br>предметов договоров:");
        msgs.put(Constants.DICT_OBJTYPES, "Введите значение для справочника<br>типов объектов:");
        msgs.put(Constants.DICT_CHECKERS, "Введите значение для справочника<br>мест проведения поверок:");
        msgs.put(Constants.DICT_ATTACHTYPES, "Введите значение для справочника<br>типов документов:");
        msgs.put(Constants.DICT_POSITIONS, "Введите значение для справочника<br>должностей:");
        msgs.put(Constants.DICT_EVAL_FIELDS, "Введите значение для справочника<br>областей аттестации:");
        msgs.put(Constants.DICT_TASKTYPES, "Введите значение для справочника<br>типов заданий:");
        
        errMsgs = new HashMap<Integer, String>();
        errMsgs.put(Constants.DICT_WORKSUBJS, "Пожалуйста, введите краткую формулировку предмета договора.");
        errMsgs.put(Constants.DICT_OBJTYPES, "Пожалуйста, введите краткое название типа объекта.");
        errMsgs.put(Constants.DICT_CHECKERS, "Пожалуйста, введите краткое название места проведения поверки.");
        errMsgs.put(Constants.DICT_ATTACHTYPES, "Пожалуйста, введите краткое название типа документа.");
        errMsgs.put(Constants.DICT_POSITIONS, "Пожалуйста, введите название должности.");
        errMsgs.put(Constants.DICT_EVAL_FIELDS, "Пожалуйста, введите описание области аттестации.");
        errMsgs.put(Constants.DICT_TASKTYPES, "Пожалуйста, введите описание типа задания.");

        
		final ToolBar topToolBar = new ToolBar();
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);
        this.add(topToolBar, flex);
        
        DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(props.key());
	    store.add(new DictionaryRecord(Constants.DICT_CLIENTS, "Контрагенты"));
	    store.add(new DictionaryRecord(Constants.DICT_PROB_CLIENTS, "Возможные контрагенты"));
	    store.add(new DictionaryRecord(Constants.DICT_WORKSUBJS, "Предметы договоров")); 
	    store.add(new DictionaryRecord(Constants.DICT_CHECKERS, "Места проведения поверок"));
	    store.add(new DictionaryRecord(Constants.DICT_OBJTYPES, "Типы объектов"));
	    store.add(new DictionaryRecord(Constants.DICT_ATTACHTYPES, "Типы документов"));
	    store.add(new DictionaryRecord(Constants.DICT_POSITIONS, "Должности"));
	    store.add(new DictionaryRecord(Constants.DICT_EVAL_FIELDS, "Области аттестации"));
	    store.add(new DictionaryRecord(Constants.DICT_TASKTYPES, "Типы заданий"));
	    
	    dictionarySelector = new ComboBox<DictionaryRecord>(store, props.nameLabel());
	    dictionarySelector.setEditable(false);
	    dictionarySelector.setAllowBlank(false);
	    dictionarySelector.setForceSelection(true);
	    dictionarySelector.setTriggerAction(TriggerAction.ALL);
	    dictionarySelector.setValue(store.get(0));
	    dictionarySelector.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				DictionaryRecord item = event.getSelectedItem();
				cardLayout.setActiveWidget(grids.get(item.getId()));
				cardLayout.forceLayout();
//				Кнопки показываем только на справочниках клиентов, на словарях они не нужны
				viewItemBtn.setVisible(!(dictionaries.contains(item.getId())));
				if (printContractBtn != null)
					printContractBtn.setVisible(!(dictionaries.contains(item.getId())));
				topToolBar.forceLayout();
			}
		});
	    
	    
	    FieldLabel selectorLbl = new FieldLabel(dictionarySelector, "Справочник");
	    selectorLbl.setLabelWidth(70);
	    selectorLbl.setWidth(270);
        
	    topToolBar.add(selectorLbl);
	    
	    viewItemBtn = new TextButton("Просмотреть запись");
	    viewItemBtn.setIcon(IMAGES.view());
		topToolBar.add(viewItemBtn);
		
		
		viewItemBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	        	final int index = dictionarySelector.getValue().getId();
	        	String grd = (index == Constants.DICT_CLIENTS) ? "clientGrid" : "probClientGrid";
	        	final Grid<Client> grid = (Grid<Client>) thisContainer.getData(grd);
				Client rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				clientInfoWindow.displayInfo(rec, group.isAllowed(Constants.ACCESS_UPDATE, "dictionaries"), group.isAllowed(Constants.ACCESS_PRINT, "dictionaries"));
	        }
	      });
	    
		if (group.isAllowed(Constants.ACCESS_INSERT, "dictionaries")) {
			TextButton addItemBtn = new TextButton("Добавить запись");
			addItemBtn.setIcon(IMAGES.addRow());
			topToolBar.add(addItemBtn);

			addItemBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					final int index = dictionarySelector.getValue().getId();
					if (dictionaries.contains(index)) {
						String title = titles.get(index);
						String message = msgs.get(index);
						final String errorMessage = errMsgs.get(index);
						final Grid<DictionaryRecord> grid = (Grid<DictionaryRecord>) thisContainer.getData("dictGrid" + index);

						final PromptMessageBox box = new PromptMessageBox(title, message);
						box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
						box.addDialogHideHandler(new DialogHideHandler() {
							@Override
							public void onDialogHide(DialogHideEvent event) {
								if (event.getHideButton() == null || event.getHideButton() == PredefinedButton.CANCEL) {
									return;
								}
								if (box.getValue() == null || box.getValue().trim().isEmpty()) {
									MessageBox d = new MessageBox("Недостаточно данных", errorMessage);
									d.setIcon(Images.INSTANCE.information());
									d.show();
									return;
								}
								dictionaryService.addRecord(index, box.getValue().trim(), new AsyncCallback<Integer>() {
									@Override
									public void onFailure(Throwable caught) {
										AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
										d.show();
									}
									@Override
									public void onSuccess(Integer result) {
										PagingToolBar toolbar = ((Component) grid.getParent()).getData("pagerToolbar");
										toolbar.refresh();
									}
								});
							}
						});
						box.setClosable(true);
						box.setOnEsc(true);
						box.show();
						box.getTextField().focus();
					} else if (clientLists.contains(index)) {
						if (index == Constants.DICT_CLIENTS) {
							MessageBox d = new MessageBox("Сообщение", "Для добавления контрагента его необходимо добавить в возможные контрагенты, а затем перевести в актуальные.");
							d.setIcon(Images.INSTANCE.information());
							d.show();
							return;
						}

//						Для того, чтобы можно было закачивать вложения сразу при создании контрагента, создадим запись в базе - её нужно будет удалить при отмене
						final Client tmpClient = new Client("");
						clientService.addClient(tmpClient, new AsyncCallback<Integer>() {
							@Override
							public void onSuccess(Integer result) {
								tmpClient.setId(result);
								clientInfoWindow.editInfo(tmpClient, true, group.isAllowed(Constants.ACCESS_PRINT, "dictionaries"));
							}
							@Override
							public void onFailure(Throwable caught) {
								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом контрагенте.");
								d.show();
							}
						});
					}
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_UPDATE, "dictionaries")) {
			TextButton editItemBtn = new TextButton("Редактировать запись");
			editItemBtn.setIcon(IMAGES.editRow());
			topToolBar.add(editItemBtn);

			editItemBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					final int index = dictionarySelector.getValue().getId();

					if (dictionaries.contains(index)) {
						DictionaryRecord rec = null;
						final Grid<DictionaryRecord> grid = (Grid<DictionaryRecord>)thisContainer.getData("dictGrid"+index);
						rec = grid.getSelectionModel().getSelectedItem();
						if (rec == null) {
							MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите отредактировать.");
							d.setIcon(IMAGES.information());
							d.show();
							return;
						}

						String title = titles.get(index);
						String message = msgs.get(index);
						final String errorMessage = errMsgs.get(index);

						final PromptMessageBox box = new PromptMessageBox(title, message);
						box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
						box.getTextField().setValue(rec.getName());
						final int updatedRecordID = rec.getId();
						box.addDialogHideHandler(new DialogHideHandler() {
							@Override
							public void onDialogHide(DialogHideEvent event) {
								if (event.getHideButton() == PredefinedButton.CANCEL) {
									return;
								}
								if (box.getValue() == null || box.getValue().trim().isEmpty()) {
									MessageBox d = new MessageBox("Недостаточно данных", errorMessage);
									d.setIcon(Images.INSTANCE.information());
									d.show();
									return;
								}
								dictionaryService.updateRecord(updatedRecordID, box.getValue().trim(), new AsyncCallback<Boolean>() {
									@Override
									public void onFailure(Throwable caught) {
										AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
										d.show();
									}
									@Override
									public void onSuccess(Boolean result) {
										PagingToolBar toolbar = ((Component) grid.getParent()).getData("pagerToolbar");
										toolbar.refresh();
									}
								});
							}
						});
						box.show();
						box.getTextField().focus();
					} else if (clientLists.contains(index)) {
						String grd = (index == Constants.DICT_CLIENTS) ? "clientGrid" : "probClientGrid";
						final Grid<Client> grid = (Grid<Client>) thisContainer.getData(grd);
						Client rec = grid.getSelectionModel().getSelectedItem();
						if (rec == null) {
							MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите отредактировать.");
							d.setIcon(IMAGES.information());
							d.show();
							return;
						}

						clientInfoWindow.editInfo(rec, false, group.isAllowed(Constants.ACCESS_PRINT, "dictionaries"));
					}
				}
			});
		}
		
		if (group.isAllowed(Constants.ACCESS_DELETE, "dictionaries")) {
			TextButton deleteItemBtn = new TextButton("Удалить запись");
			deleteItemBtn.setIcon(IMAGES.deleteRow());
			topToolBar.add(deleteItemBtn);

			final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
				@Override
				public void onDialogHide(DialogHideEvent event) {
					if (event.getHideButton() == PredefinedButton.YES) {
						int index = dictionarySelector.getValue().getId();
						if (dictionaries.contains(index)) {
							final Grid<DictionaryRecord> grid = (Grid<DictionaryRecord>) thisContainer.getData("dictGrid" + index);
							DictionaryRecord rec = grid.getSelectionModel().getSelectedItem();
							dictionaryService.deleteRecord(rec.getId(), !group.isDeleteConfirmer(), new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									AlertMessageBox d = new AlertMessageBox("Ошибка", caught.getMessage());
									d.show();
								}
								@Override
								public void onSuccess(Boolean result) {
									PagingToolBar toolbar = ((Component) grid.getParent()).getData("pagerToolbar");
									toolbar.refresh();
								}
							});
						} else if (clientLists.contains(index)) {
							String grd = (index == Constants.DICT_CLIENTS) ? "clientGrid" : "probClientGrid";
							final Grid<Client> grid = (Grid<Client>) thisContainer.getData(grd);
							Client rec = grid.getSelectionModel().getSelectedItem();
							if (rec == null) {
								MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите запись, которую Вы хотите удалить.");
								d.setIcon(IMAGES.information());
								d.show();
								return;
							}
							boolean markOnly = !group.isDeleteConfirmer();
							clientService.deleteRecord(rec.getId(), markOnly, new AsyncCallback<Boolean> () {
								@Override
								public void onFailure(Throwable caught) {
									AlertMessageBox d = new AlertMessageBox("Ошибка", caught.getMessage());
									d.show();
								}
								@Override
								public void onSuccess(Boolean result) {
									PagingToolBar toolbar = ((Component) grid.getParent()).getData("pagerToolbar");
									toolbar.refresh();
								}
							});
						}
					}
				}
			};

			deleteItemBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					int index = dictionarySelector.getValue().getId();
					Object rec = null;
					if (dictionaries.contains(index)) {
						Grid<DictionaryRecord> grid = (Grid<DictionaryRecord>)thisContainer.getData("dictGrid"+index);
						rec = grid.getSelectionModel().getSelectedItem();
					} else if (clientLists.contains(index)) {
						String grd = (index == Constants.DICT_CLIENTS) ? "clientGrid" : "probClientGrid";
						final Grid<Client> grid = (Grid<Client>) thisContainer.getData(grd);
						rec = grid.getSelectionModel().getSelectedItem();
					}
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
		
		if (group.isAllowed(Constants.ACCESS_PRINT, "dictionaries")) {
			printDictBtn = new TextButton("Печать");
			printDictBtn.setIcon(IMAGES.print());
			topToolBar.add(printDictBtn);
			printDictBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					int index = dictionarySelector.getValue().getId();
					if (dictionaries.contains(index)) {
						dictionaryService.getPrintableDictionaryContents(index, new AsyncCallback<String>() {
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
					} else {
						clientService.getPrintableClientList(index == Constants.DICT_CLIENTS, new AsyncCallback<String>() {
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
				}
			});
			
			printContractBtn = new TextButton("Сформировать типовой договор");
			printContractBtn.setIcon(IMAGES.signature());
			topToolBar.add(printContractBtn);
			printContractBtn.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					templateContractWindow.displayWindow();
				}
			});
		}
        
        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);

        this.add(cardLayout, flex2);
	}
	
	private Widget getDictGrid (final int type) {
		final int dictType = type;
		RpcProxy<PagingLoadConfig, PagingLoadResult<DictionaryRecord>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<DictionaryRecord>>() {
			@Override
			public void load(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<DictionaryRecord>> callback) {
				dictionaryService.getDictContPaged(dictType, loadConfig, callback);
			}
		};
		
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);

		ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(new ModelKeyProvider<DictionaryRecord>() {
					@Override
					public String getKey(DictionaryRecord item) {
						return "" + item.getId();
					}
				});
		
		loaders.put(dictType, new PagingLoader<PagingLoadConfig, PagingLoadResult<DictionaryRecord>>(proxy));
		loaders.get(dictType).setRemoteSort(true);
		loaders.get(dictType).addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, DictionaryRecord, PagingLoadResult<DictionaryRecord>>(store));
		
		String colName = colNames.get(dictType);
		ColumnConfig<DictionaryRecord, String> nameColumn = new ColumnConfig<DictionaryRecord, String>(props.name(), 10, colName);
		
		List<ColumnConfig<DictionaryRecord, ?>> l = new ArrayList<ColumnConfig<DictionaryRecord, ?>>();
		l.add(nameColumn);
		
		ColumnModel<DictionaryRecord> cm = new ColumnModel<DictionaryRecord>(l);
		
		Grid<DictionaryRecord> dictGrid = new Grid<DictionaryRecord>(store, cm) {
			@Override
			protected void onAfterFirstAttach() {
				super.onAfterFirstAttach();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						loaders.get(type).load();
					}
				});
			}
		};
		
		this.setData("dictGrid"+type, dictGrid);
		
		dictGrid.setLoadMask(true);
		dictGrid.setLoader(loaders.get(type));
		dictGrid.getView().setSortingEnabled(false);
		dictGrid.getView().setForceFit(true);
		dictGrid.getView().setAutoExpandColumn(nameColumn);
		dictGrid.getView().setStripeRows(true);
		dictGrid.getView().setColumnLines(true);
		dictGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(true);
		con.add(dictGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar toolBar = new PagingToolBar(30);
		toolBar.bind(loaders.get(type));
		toolBar.addStyleName(ThemeStyles.get().style().borderTop());
		toolBar.getElement().getStyle().setProperty("borderBottom", "none");

		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.setData("grid", dictGrid);

		con.setData("pagerToolbar", toolBar);
		return con;
	}

	private Widget getClientsGrid (final boolean actual) {
		RpcProxy<PagingLoadConfig, PagingLoadResult<Client>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<Client>>() {
			@Override
			public void load(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Client>> callback) {
				clientService.getClientsPaged(loadConfig, actual, callback);
			}
		};
		
		ClientProps props = GWT.create(ClientProps.class);

		ListStore<Client> store = new ListStore<Client>(new ModelKeyProvider<Client>() {
					@Override
					public String getKey(Client item) {
						return "" + item.getId();
					}
				});
		
		final PagingLoader<PagingLoadConfig, PagingLoadResult<Client>> clientGridLoader = new PagingLoader<PagingLoadConfig, PagingLoadResult<Client>>(proxy);
		clientGridLoader.setRemoteSort(true);
		clientGridLoader.addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, Client, PagingLoadResult<Client>>(store));
		
		ColumnConfig<Client, String> nameColumn = new ColumnConfig<Client, String>(props.name(), 30, "Наименование контрагента");
		ColumnConfig<Client, String> bossNameColumn = new ColumnConfig<Client, String>(props.boss(), 10, "ФИО руководителя");
		ColumnConfig<Client, String> addressColumn = new ColumnConfig<Client, String>(props.address(), 10, "Адрес");
		ColumnConfig<Client, String> phoneColumn = new ColumnConfig<Client, String>(props.phone(), 10, "Телефон");
		ColumnConfig<Client, String> innColumn = new ColumnConfig<Client, String>(props.inn(), 10, "ИНН");
		ColumnConfig<Client, String> emailColumn = new ColumnConfig<Client, String>(props.email(), 10, "E-mail");
		
		List<ColumnConfig<Client, ?>> l = new ArrayList<ColumnConfig<Client, ?>>();
		l.add(nameColumn);
		l.add(bossNameColumn);
		l.add(addressColumn);
		l.add(phoneColumn);
		l.add(innColumn);
		l.add(emailColumn);
		
		ColumnModel<Client> cm = new ColumnModel<Client>(l);
		
		final Grid<Client> clientGrid = new Grid<Client>(store, cm) {
			@Override
			protected void onAfterFirstAttach() {
				super.onAfterFirstAttach();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						clientGridLoader.load();
					}
				});
			}
		};
		
		clientGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Client c = clientGrid.getSelectionModel().getSelectedItem();
				clientInfoWindow.displayInfo(c, group.isAllowed(Constants.ACCESS_UPDATE, "dictionaries"), group.isAllowed(Constants.ACCESS_PRINT, "dictionaries"));
			}
		});
		
		if (actual)
			this.setData("clientGrid", clientGrid);
		else
			this.setData("probClientGrid", clientGrid);
		
		clientGrid.setLoadMask(true);
		clientGrid.setLoader(clientGridLoader);
		clientGrid.getView().setForceFit(true);
		clientGrid.getView().setAutoExpandColumn(nameColumn);
		clientGrid.getView().setStripeRows(true);
		clientGrid.getView().setColumnLines(true);
		clientGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(true);
		con.add(clientGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar toolBar = new PagingToolBar(30);
		toolBar.bind(clientGridLoader);
		toolBar.addStyleName(ThemeStyles.get().style().borderTop());
		toolBar.getElement().getStyle().setProperty("borderBottom", "none");

		con.add(toolBar, new VerticalLayoutData(1, -1));
		con.setData("grid", clientGrid);

		con.setData("pagerToolbar", toolBar);
		return con;
	}
}
