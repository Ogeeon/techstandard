package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Contract;
import ru.techstandard.client.model.ContractProps;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.ContractService;
import ru.techstandard.shared.ContractServiceAsync;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer.VBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.core.client.resources.ThemeStyles;

public class ClientInfoWindow extends Window {
	private final Images IMAGES = GWT.create(Images.class);

	ToggleButton toggleEditModeBtn;
	TextButton printClientBtn;
	TextButton actualizeBtn;
	TabItemConfig clientDataTabConfig;
	TabItemConfig clientContracsTabConfig;
	
	Label actualState;
	TextField clientId;
	TextField clientName;
	TextArea clientFullName;
	TextField clientBoss;
	TextField clientAddress;
	TextField clientAddress2;
	TextField clientPhone;
	TextField clientFax;
	TextField clientINN;
	TextField clientKpp;
	TextField clientEmail;
	TextArea clientBank;
	TextField clientRsch;
	TextField clientKsch;
	TextField clientOkpo;
	TextField clientOkato;
	TextField clientOgrn;

	List<Widget> validityChecks;
	
	Window theWindow;
	ToolBar topToolBar;
	TextButton viewContractBtn;
	TextButton addContractBtn;
	TextButton editContractBtn;
	TextButton deleteContractBtn;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	StringFilter<Contract> clientIdFilter;
	
	private  boolean isModified;
	private int action=0;
	private boolean isActualClient;
	private boolean canPrint=false;
	private boolean userMarksForDelete=true;
	private boolean canClose=false;
	private boolean needValidation=false;
	
	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final ContractServiceAsync contractService = GWT.create(ContractService.class);
	ContractInfoWindow contractInfoWindow;
	HandlerRegistration contractWndHideHandleReg = null;
	
	int wndHeight = 655;
	
	public ClientInfoWindow(boolean userOnlyMarksForDelete) {
		super();
		this.setPixelSize(500, wndHeight);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Просмотр информации о контрагенте");
		userMarksForDelete = userOnlyMarksForDelete;
		
		contractInfoWindow = new ContractInfoWindow();
		
		theWindow = this;
		
		VerticalLayoutContainer clientInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(clientInfoTopContainer);
		
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
		
		actualizeBtn = new TextButton("Перевести в актуальные");
		actualizeBtn.setIcon(IMAGES.checked());
		actualizeBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				needValidation = true;
				if (!checkFormValidity()) {
					MessageBox d = new MessageBox("Внимание", "Для выполнения этой операции необходимо заполнить обязательные поля формы.");
					d.setIcon(IMAGES.warning());
					d.show();
					return;
				}
				contractService.getCount(Integer.valueOf(clientId.getValue()), new AsyncCallback<Integer>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось проверить наличие договорных отношений с контрагентом.");
						d.show();
					}
					@Override
					public void onSuccess(Integer result) {
						if (result == 0) {
							MessageBox d = new MessageBox("Внимание", "Для выполнения этой операции необходимо наличие договорных отношений с контрагентом.");
							d.setIcon(IMAGES.warning());
							d.show();
						} else {
							clientService.setActual(Integer.valueOf(clientId.getValue()), true, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось обновить данные.");
									d.show();
								}
								@Override
								public void onSuccess(Boolean result) {
									actualizeBtn.setVisible(false);
									actualState.setText("Статус взаимоотношений: актуальный контрагент");
									theWindow.forceLayout();
									MessageBox d = new MessageBox("Готово", "Перевод контрагента в актуальные выполнен.");
									d.setIcon(IMAGES.information());
									d.show();
								}
							});
						}
					}
				});
			}
		});
		toolBar.add(actualizeBtn);
		
		printClientBtn = new TextButton("Печать");
		printClientBtn.setIcon(IMAGES.print());
		printClientBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				clientService.getPrintableClientCard(Integer.valueOf(clientId.getText()), new AsyncCallback<String>() {
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
		toolBar.add(printClientBtn);
		
		clientInfoTopContainer.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));

		TabPanel panel = new TabPanel();

		ContentPanel clientInfoPanel = new FramedPanel();
		clientInfoPanel.setHeaderVisible(false);
		VerticalLayoutContainer clientInfoContainer = new VerticalLayoutContainer();
		clientInfoPanel.setWidget(clientInfoContainer);
		
		final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode()==KeyCodes.KEY_TAB)
					return;
				isModified = true;
				checkFormValidity();
			}
		};

		clientId = new TextField();
		
		int labeColWidth = 140;
		
		clientName = new TextField();
		clientName.setAllowBlank(false);
		clientName.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel nameLabel = new FieldLabel(clientName, "Наименование краткое");
		nameLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(nameLabel, new VerticalLayoutData(1, -1));
		
		clientFullName = new TextArea();
//		clientFullName.setAllowBlank(false);
		clientFullName.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel fullNameLabel = new FieldLabel(clientFullName, "Наименование полное");
		fullNameLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(fullNameLabel, new VerticalLayoutData(1, 40));

		clientBoss = new TextField();
		clientBoss.addKeyUpHandler(fieldKeyUpHandler);
//		clientBoss.setAllowBlank(true);
		FieldLabel bossLabel = new FieldLabel(clientBoss, "ФИО руководителя");
		bossLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(bossLabel, new VerticalLayoutData(1, -1));

		clientAddress = new TextField();
		clientAddress.addKeyUpHandler(fieldKeyUpHandler);
//		clientAddress.setAllowBlank(false);
		FieldLabel addressLabel = new FieldLabel(clientAddress, "Почтовый адрес");
		addressLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(addressLabel, new VerticalLayoutData(1, -1));
		
		clientAddress2 = new TextField();
		clientAddress2.addKeyUpHandler(fieldKeyUpHandler);
//		clientAddress2.setAllowBlank(false);
		FieldLabel address2Label = new FieldLabel(clientAddress2, "Юридический адрес");
		address2Label.setLabelWidth(labeColWidth);
		clientInfoContainer.add(address2Label, new VerticalLayoutData(1, -1));

		clientPhone = new TextField();
		clientPhone.addKeyUpHandler(fieldKeyUpHandler);
		clientPhone.setAllowBlank(true);
		FieldLabel phoneLabel = new FieldLabel(clientPhone, "Телефон");
		phoneLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(phoneLabel, new VerticalLayoutData(1, -1));
		
		clientFax = new TextField();
		clientFax.addKeyUpHandler(fieldKeyUpHandler);
//		clientFax.setAllowBlank(true);
		FieldLabel faxLabel = new FieldLabel(clientFax, "Факс");
		faxLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(faxLabel, new VerticalLayoutData(1, -1));

		clientEmail = new TextField();
		clientEmail.addKeyUpHandler(fieldKeyUpHandler);
//		clientEmail.setAllowBlank(true);
		FieldLabel emailLabel = new FieldLabel(clientEmail, "e-mail");
		emailLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(emailLabel, new VerticalLayoutData(1, -1));

		clientINN = new TextField();
		clientINN.addKeyUpHandler(fieldKeyUpHandler);
		clientINN.setAllowBlank(false);
		FieldLabel innLabel = new FieldLabel(clientINN, "ИНН");
		innLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(innLabel, new VerticalLayoutData(1, -1));
		
		clientKpp = new TextField();
		clientKpp.addKeyUpHandler(fieldKeyUpHandler);
//		clientKpp.setAllowBlank(false);
		FieldLabel kppLabel = new FieldLabel(clientKpp, "КПП");
		kppLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(kppLabel, new VerticalLayoutData(1, -1));
		
		clientBank = new TextArea();
//		clientBank.setAllowBlank(false);
		clientBank.addKeyUpHandler(fieldKeyUpHandler);
		
		FieldLabel bankLabel = new FieldLabel(clientBank, "Банк плательщика");
		bankLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(bankLabel, new VerticalLayoutData(1, 60));
		
		clientRsch = new TextField();
		clientRsch.addKeyUpHandler(fieldKeyUpHandler);
//		clientRsch.setAllowBlank(false);
		FieldLabel rschLabel = new FieldLabel(clientRsch, "Р/счёт");
		rschLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(rschLabel, new VerticalLayoutData(1, -1));
		
		clientKsch = new TextField();
		clientKsch.addKeyUpHandler(fieldKeyUpHandler);
//		clientKsch.setAllowBlank(false);
		FieldLabel kschLabel = new FieldLabel(clientKsch, "К/счёт");
		kschLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(kschLabel, new VerticalLayoutData(1, -1));
		
		clientOkpo = new TextField();
		clientOkpo.addKeyUpHandler(fieldKeyUpHandler);
//		clientOkpo.setAllowBlank(true);
		FieldLabel okpoLabel = new FieldLabel(clientOkpo, "ОКПО");
		okpoLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(okpoLabel, new VerticalLayoutData(1, -1));
		
		clientOkato = new TextField();
		clientOkato.addKeyUpHandler(fieldKeyUpHandler);
//		clientOkato.setAllowBlank(true);
		FieldLabel okatoLabel = new FieldLabel(clientOkato, "ОКАТО");
		okatoLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(okatoLabel, new VerticalLayoutData(1, -1));
		
		clientOgrn = new TextField();
		clientOgrn.addKeyUpHandler(fieldKeyUpHandler);
//		clientOgrn.setAllowBlank(true);
		FieldLabel ogrnLabel = new FieldLabel(clientOgrn, "ОГРН");
		ogrnLabel.setLabelWidth(labeColWidth);
		clientInfoContainer.add(ogrnLabel, new VerticalLayoutData(1, -1));
		
		validityChecks = new ArrayList<Widget>();
		validityChecks.add(clientName);
		validityChecks.add(clientFullName);
		validityChecks.add(clientBoss);
		validityChecks.add(clientAddress);
		validityChecks.add(clientAddress2);
		validityChecks.add(clientPhone);
		validityChecks.add(clientINN);
		validityChecks.add(clientKpp);
		validityChecks.add(clientBank);
		validityChecks.add(clientRsch);
		validityChecks.add(clientKsch);

		actualState = new Label("Статус взаимоотношений: возможный контрагент");
		clientInfoContainer.add(actualState, new VerticalLayoutData(1, -1));

		// Договоры
		ContentPanel clientContractsPanel = new ContentPanel();
		clientContractsPanel.setHeaderVisible(false);
		clientContractsPanel.setBorders(false);
		clientContractsPanel.setHeight(515);
		
		VBoxLayoutContainer clientDocsContainer = new VBoxLayoutContainer();
		clientDocsContainer.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		clientContractsPanel.setWidget(clientDocsContainer);
		clientDocsContainer.setBorders(false);
		
		topToolBar = new ToolBar();
		topToolBar.setHeight(30);
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);
        clientDocsContainer.add(topToolBar, flex);
        
        viewContractBtn = new TextButton("Просмотреть");
        viewContractBtn.setIcon(IMAGES.view());
		topToolBar.add(viewContractBtn);
		
		viewContractBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Contract> contractGrid = theWindow.getData("contractGrid");
				Contract rec = contractGrid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите договор, который Вы хотите просмотреть.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				// Если кнопка видна, значит режим редактирвования разрешён
				contractInfoWindow.displayInfo(rec, toggleEditModeBtn.isVisible(), canPrint);
			}
		});
		
		final HideHandler editHideHandler = new HideHandler() {
	        @Override
	        public void onHide(HideEvent event) {
	          Window wnd = (Window) event.getSource();
	          if (wnd.getData("hideButton") == null)
	        	  return;
	          if (wnd.getData("hideButton").equals("save")) {
	        	  PagingToolBar toolbar = theWindow.getData("pagerToolbar");
	        	  toolbar.refresh();
	          }
	        }
	    };
		
		addContractBtn = new TextButton("Создать");
        addContractBtn.setIcon(IMAGES.add());
		topToolBar.add(addContractBtn);
		addContractBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				if (contractWndHideHandleReg == null)
					contractWndHideHandleReg = contractInfoWindow.addHideHandler(editHideHandler);
				final Contract tmpContract = new Contract(0, Integer.valueOf(clientId.getValue()));
				tmpContract.setClientName(clientName.getValue());
				contractService.addContract(tmpContract, new AsyncCallback<Integer>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом договоре.");
						d.show();
					}
					@Override
					public void onSuccess(Integer result) {
						tmpContract.setId(result);

						System.out.println("tmp contract:"+tmpContract);
						contractInfoWindow.editInfo(tmpContract, true, true);
					}
				});
			}
		});
		
		editContractBtn = new TextButton("Редактировать");
		editContractBtn.setIcon(IMAGES.edit());
		topToolBar.add(editContractBtn);
		editContractBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Contract> grid = theWindow.getData("contractGrid");
				Contract rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите договор, который Вы хотите отредактировать.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				if (contractWndHideHandleReg == null)
					contractWndHideHandleReg = contractInfoWindow.addHideHandler(editHideHandler);
				contractInfoWindow.editInfo(rec, false, canPrint);
			}
		});
		
		final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
	        @Override
	        public void onDialogHide(DialogHideEvent event) {
	        	if (event.getHideButton() == PredefinedButton.YES) {
	        	  Grid<Contract> grid = theWindow.getData("contractGrid");
	        	  Contract rec = grid.getSelectionModel().getSelectedItem();
	        	  contractService.deleteContract(rec.getId(), userMarksForDelete, new AsyncCallback<Boolean>(){
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось удалить договор.");
						d.show();
					}
					@Override
					public void onSuccess(Boolean result) {
						PagingToolBar toolbar = theWindow.getData("pagerToolbar");
						toolbar.refresh();
					}
	        	  });
	          }
	        }
	    };
	    
		deleteContractBtn = new TextButton("Удалить");
        deleteContractBtn.setIcon(IMAGES.deleteRow());
		topToolBar.add(deleteContractBtn);
	      
		deleteContractBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Grid<Contract> grid = theWindow.getData("contractGrid");
				Contract rec = grid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание", "Пожалуйста, выберите договор, который Вы хотите удалить.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранный договор?");
				box.setIcon(IMAGES.warning());
				box.addDialogHideHandler(deleteHideHandler);
				box.show();
			}
		});
		
		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Contract>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Contract>>() {
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Contract>> callback) {
				contractService.getContracts(loadConfig, callback);
			}
		};
		
		
		ContractProps props = GWT.create(ContractProps.class);
		ListStore<Contract> store = new ListStore<Contract>(new ModelKeyProvider<Contract>() {
			@Override
			public String getKey(Contract item) {
				return "" + item.getId();
			}
		});

		final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Contract>> contractsLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Contract>>(proxy) {
	        @Override
	        protected FilterPagingLoadConfig newLoadConfig() {
	          return new FilterPagingLoadConfigBean();
	        }
	      };
		contractsLoader.setRemoteSort(true);
		contractsLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Contract, PagingLoadResult<Contract>>(store));
		
		ColumnConfig<Contract, String> parentIdColumn = new ColumnConfig<Contract, String>(props.clientIdStr(), 1, "client_id");
		parentIdColumn.setHidden(true);
		ColumnConfig<Contract, String> numColumn = new ColumnConfig<Contract, String>(props.num(), 10, "Номер");
		ColumnConfig<Contract, Date> signedColumn = new ColumnConfig<Contract, Date>(props.signed(), 10, "От");
		signedColumn.setCell(new DateCell(DateTimeFormat.getFormat("dd.MM.yyyy")));
		ColumnConfig<Contract, String> workSubjColumn = new ColumnConfig<Contract, String>(props.workSubj(), 30, "Предмет договора");
		ColumnConfig<Contract, String> statusColumn = new ColumnConfig<Contract, String>(props.status(), 10, "Статус");
		
		
		List<ColumnConfig<Contract, ?>> l = new ArrayList<ColumnConfig<Contract, ?>>();
		l.add(parentIdColumn);
		l.add(numColumn);
		l.add(signedColumn);
		l.add(workSubjColumn);
		l.add(statusColumn);
		ColumnModel<Contract> cm = new ColumnModel<Contract>(l);
		
		final Grid<Contract> contractGrid = new Grid<Contract>(store, cm) {
			@Override
			protected void onAfterFirstAttach() {
				super.onAfterFirstAttach();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
					}
				});
			}
		};
		contractGrid.setLoadMask(true);
		contractGrid.setLoader(contractsLoader);
//		contractGrid.getView().setSortingEnabled(false);
		contractGrid.getView().setForceFit(true);
		contractGrid.getView().setAutoExpandColumn(workSubjColumn);
		contractGrid.getView().setStripeRows(true);
		contractGrid.getView().setColumnLines(true);
		contractGrid.setBorders(false);
		contractGrid.addRowDoubleClickHandler(new RowDoubleClickHandler() {
			@Override
			public void onRowDoubleClick(RowDoubleClickEvent event) {
				Contract rec = contractGrid.getSelectionModel().getSelectedItem();
				// Если кнопка видна, значит режим редактирвования разрешён
				contractInfoWindow.displayInfo(rec, toggleEditModeBtn.isVisible(), canPrint);
			}
		});
		
		theWindow.setData("contractGrid", contractGrid);
		
		GridFilters<Contract> filters = new GridFilters<Contract>(contractsLoader);
		clientIdFilter = new StringFilter<Contract>(props.clientIdStr());
		
		filters.initPlugin(contractGrid);
		filters.addFilter(clientIdFilter);
		
		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(false);
		con.add(contractGrid, new VerticalLayoutData(1, 1));

		final PagingToolBar pagingToolBar = new PagingToolBar(10);
		pagingToolBar.bind(contractsLoader);
		pagingToolBar.addStyleName(ThemeStyles.get().style().borderTop());
		pagingToolBar.getElement().getStyle().setProperty("borderBottom", "none");
		con.add(pagingToolBar, new VerticalLayoutData(1, 25));
		theWindow.setData("pagerToolbar", pagingToolBar);
		
		BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
		flex2.setFlex(3);
        clientDocsContainer.add(con, flex2);
		
		// *** Добавление табов в контейнер

		clientDataTabConfig = new TabItemConfig("Данные контрагента");
		clientDataTabConfig.setIcon(IMAGES.view());

		clientContracsTabConfig = new TabItemConfig("Договоры");
		clientContracsTabConfig.setIcon(IMAGES.documents());

		panel.add(clientInfoPanel, clientDataTabConfig);
		panel.add(clientContractsPanel, clientContracsTabConfig);

		clientInfoTopContainer.add(panel, new VerticalLayoutData(1, -1, new Margins(0, 0, 5, 0)));
		
		this.setButtonAlign(BoxLayoutPack.END);
		
		saveBtn = new TextButton("Сохранить");
		saveBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Client client = new Client();
				client.setId(Integer.valueOf(clientId.getValue()));
				client.setName(clientName.getValue());
				client.setFullName(clientFullName.getValue());
				client.setBoss(clientBoss.getValue());
				client.setAddress(clientAddress.getValue());
				client.setAddress2(clientAddress2.getValue());
				client.setPhone(clientPhone.getValue());
				client.setFax(clientFax.getValue());
				client.setEmail(clientEmail.getValue());
				client.setInn(clientINN.getValue());
				client.setKpp(clientKpp.getValue());
				client.setBankName(clientBank.getValue());
				client.setRsch(clientRsch.getValue());
				client.setKsch(clientKsch.getValue());
				client.setOkpo(clientOkpo.getValue());
				client.setOkato(clientOkato.getValue());
				client.setOgrn(clientOgrn.getValue());
				client.setActual(isActualClient);
//				При добавлении нового контрагента пустая запись уже была создана, её нужно только обновить.
				clientService.updateClient(client, new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
//						Если создаём новую запись - нужно сообщить вызывающему коду, что мы создали.
						if (action == Constants.ACTION_ADD) {
							theWindow.setData("newClientID", clientId.getValue());
							theWindow.setData("newClientName", clientName.getValue());
						}
						action = -1;
						canClose=true;
						theWindow.setData("hideButton", "save");
						theWindow.hide();
					}
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить информацию о контрагенте.");
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
			clientService.deleteRecord(Integer.valueOf(clientId.getValue()), false, new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(Boolean result) {}
			});
		}
	}
	
	public void displayInfo(Client client, boolean canEdit, boolean canPrint) {
		action = -1;
		canClose=false;
		fillWindowFields(client);
		toggleEditMode(false);
		toggleEditModeBtn.setValue(false);
		toggleEditModeBtn.setVisible(canEdit);
		printClientBtn.setVisible(canPrint);
		
		clientContracsTabConfig.setEnabled(true);
		
		isModified = false;
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, wndHeight);
		theWindow.forceLayout();
	}

	public void editInfo(Client client, boolean newClient, boolean canPrint) {
//		System.out.println("editInfo("+client.toString()+")");
		action = newClient ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		fillWindowFields(client);
		toggleEditMode(true);
		toggleEditModeBtn.setValue(true);
		toggleEditModeBtn.setVisible(true);
		printClientBtn.setVisible(canPrint);
		
		isModified = false;
		
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, wndHeight);
		theWindow.forceLayout();
	}
	
	private void fillWindowFields(Client client) {
		clientId.setValue(String.valueOf(client.getId()));

		TextField field = (TextField) clientIdFilter.getMenu().getWidget(0);
		clientIdFilter.setActive(false, false);
		field.setValue(clientId.getValue(), true);
		clientIdFilter.setActive(true,false);
		
		// при первоначальном заполнении данных контрагента мы имеем дело с заглушкой, названной "(временный)" - не надо это пользователю показывать
		clientName.setValue(action == Constants.ACTION_ADD ? null : client.getName());
		clientFullName.setValue(client.getFullName());
		clientBoss.setValue(client.getBoss());
		clientAddress.setValue(client.getAddress());
		clientAddress2.setValue(client.getAddress2());
		clientPhone.setValue(client.getPhone());
		clientFax.setValue(client.getFax());
		clientINN.setValue(client.getInn());
		clientKpp.setValue(client.getKpp());
		clientEmail.setValue(client.getEmail());
		clientBank.setValue(client.getBankName());
		clientRsch.setValue(client.getRsch());
		clientKsch.setValue(client.getKsch());
		clientOkpo.setValue(client.getOkpo());
		clientOkato.setValue(client.getOkato());
		clientOgrn.setValue(client.getOgrn());
		TextButton saveBtn = (TextButton) theWindow.getData("saveBtn");
		saveBtn.setVisible(true);
		saveBtn.setEnabled(false);
		theWindow.setHeadingText("Редактирование информации о контрагенте");
//		Актуализировать можно только возможного контра, и только при наличии прав на изменение справочника
		actualizeBtn.setVisible(!client.isActual() && toggleEditModeBtn.isVisible());
		actualState.setText("Статус взаимоотношений: "+(client.isActual()?"актуальный":"возможный")+" контрагент");
		isActualClient = client.isActual();
		
		needValidation = isActualClient;
		checkFormValidity();
		
		// Запретим добавлять договора при создании контрагента, т.к. иначе поле "контрагент" будет пустым
		clientContracsTabConfig.setEnabled(action == Constants.ACTION_UPDATE);
	}
	
	private void toggleEditMode(boolean editMode) {
		actualizeBtn.setVisible(editMode & !isActualClient);
		
		clientName.setEnabled(editMode);
		clientFullName.setEnabled(editMode);
		clientBoss.setEnabled(editMode);
		clientAddress.setEnabled(editMode);
		clientAddress2.setEnabled(editMode);
		clientPhone.setEnabled(editMode);
		clientFax.setEnabled(editMode);
		clientINN.setEnabled(editMode);
		clientKpp.setEnabled(editMode);
		clientEmail.setEnabled(editMode);
		clientBank.setEnabled(editMode);
		clientRsch.setEnabled(editMode);
		clientKsch.setEnabled(editMode);
		clientOkpo.setEnabled(editMode);
		clientOkato.setEnabled(editMode);
		clientOgrn.setEnabled(editMode);
		
		addContractBtn.setVisible(editMode);
		editContractBtn.setVisible(editMode);
		deleteContractBtn.setVisible(editMode);
		
		saveBtn.setVisible(editMode);
		if (editMode)
			setHeadingText("Редактирование информации о контрагенте");
		else
			setHeadingText("Просмотр информации о контрагенте");
		
		theWindow.forceLayout();
	}
	
	private boolean checkFormValidity() {
		boolean isFormValid = true;
		Widget w;
		for (int i=0; i < validityChecks.size(); i++) {
			w = validityChecks.get(i);
			if (w != clientName && !needValidation) {
				((ValueBaseField<?>) w).clearInvalid();
				continue;
			}
			if (((ValueBaseField<?>) w).getCurrentValue() == null) {
				((ValueBaseField<?>) w).forceInvalid("Поле не должно быть пустым");
				isFormValid = false;
			} else {
				((ValueBaseField<?>) w).clearInvalid();
			}
		}
		saveBtn.setEnabled(isModified && isFormValid);
		return isFormValid;
	}
	
}
