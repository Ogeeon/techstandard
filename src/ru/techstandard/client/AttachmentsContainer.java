package ru.techstandard.client;

import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.Attachement;
import ru.techstandard.client.model.AttachementProps;
import ru.techstandard.shared.AttachementService;
import ru.techstandard.shared.AttachementServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class AttachmentsContainer extends VBoxLayoutContainer {

	private int parentType;
	private List<Integer> tempAttachIDs = new ArrayList<Integer>();
	AttachementUploadWindow attachUploadWnd;
	ToolBar topToolBar;
	TextButton addDocBtn;
	TextButton openDocBtn;
	TextButton editDocBtn;
	TextButton deleteDocBtn;

	Grid<Attachement> atchmGrid;
	PagingToolBar pagingToolBar;
	StringFilter<Attachement> parentTypeFilter;
	StringFilter<Attachement> parentIdFilter;
	
	private final AttachementServiceAsync attachementService = GWT.create(AttachementService.class);
	private final Images IMAGES = GWT.create(Images.class);
	VBoxLayoutContainer self;
	List<TextButton> buttons = new ArrayList<TextButton>();
	
	public AttachmentsContainer(int type) {
		super();
		this.parentType = type;
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		this.setBorders(false);
		self = this;
		
		attachUploadWnd = new AttachementUploadWindow();
		attachUploadWnd.addHideHandler(new HideHandler() {
			@Override
			public void onHide(HideEvent event) {
				Window wnd = (Window) event.getSource();
				if (wnd.getData("hideButton") == null)
					return;
				if (wnd.getData("hideButton").equals("save")) {
					pagingToolBar.refresh();
				}
			}
		});

		topToolBar = new ToolBar();
		topToolBar.setBorders(true);
		topToolBar.setPixelSize(-1, 30);
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
		flex.setFlex(0);
		this.add(topToolBar, flex);

		addDocBtn = new TextButton("Закачать");
		addDocBtn.setIcon(IMAGES.upload());
		topToolBar.add(addDocBtn);
		buttons.add(addDocBtn);

		addDocBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Integer id = (Integer) self.getData("parentId");
				System.out.println("add doc params: id="+id+", type="+parentType+", attachIDs="+tempAttachIDs);
				attachUploadWnd.addAttachment(id, parentType, tempAttachIDs);
			}
		});

		openDocBtn = new TextButton("Скачать");
		openDocBtn.setIcon(IMAGES.download());
		topToolBar.add(openDocBtn);
		openDocBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Attachement rec = null;
				rec = atchmGrid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите документ, который Вы хотите скачать.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				String url = GWT.getModuleBaseURL() + "downloadService?fileName=" + rec.getFilename() + "&attachId="+rec.getId() + "&type=attachment";
				//						com.google.gwt.user.client.Window.open( url, "_blank", "status=0,toolbar=0,menubar=0,location=0");
				com.google.gwt.user.client.Window.open(url, "_self", "");
			}
		});
		buttons.add(openDocBtn);

		editDocBtn = new TextButton("Редактировать");
		editDocBtn.setIcon(IMAGES.edit());
		topToolBar.add(editDocBtn);
		editDocBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Attachement rec = null;
				rec = atchmGrid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите документ, который Вы хотите отредактировать.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}

				attachUploadWnd.editAttachment(rec);
			}
		});
		buttons.add(editDocBtn);


		deleteDocBtn = new TextButton("Удалить");
		deleteDocBtn.setIcon(IMAGES.deleteRow());
		topToolBar.add(deleteDocBtn);
		buttons.add(deleteDocBtn);

		final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
			@Override
			public void onDialogHide(DialogHideEvent event) {
				if (event.getHideButton() == PredefinedButton.YES) {
					Attachement rec = atchmGrid.getSelectionModel().getSelectedItem();
					if (rec == null) {
						MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите  документ, который  Вы хотите удалить.");
						d.setIcon(IMAGES.information());
						d.show();
						return;
					}
					List<Integer> attach = new ArrayList<Integer>();
					attach.add(rec.getId());
					attachementService.deleteAttachments(attach, new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", caught.getMessage());
							d.show();
						}

						@Override
						public void onSuccess(Void result) {
							pagingToolBar.refresh();
						}
					});
				}
			}
		};

		deleteDocBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				Object rec = null;
				rec = atchmGrid.getSelectionModel().getSelectedItem();
				if (rec == null) {
					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите документ, который Вы хотите удалить.");
					d.setIcon(IMAGES.information());
					d.show();
					return;
				}
				ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранный документ?");
				box.setIcon(IMAGES.warning());
				box.addDialogHideHandler(deleteHideHandler);
				box.show();
			}
		});

		RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Attachement>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Attachement>>() {
			public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Attachement>> callback) {
				attachementService.getAttachements(loadConfig, callback);

			}
		};


		AttachementProps attachmentProps = GWT.create(AttachementProps.class);
		ListStore<Attachement> store = new ListStore<Attachement>(new ModelKeyProvider<Attachement>() {
			@Override
			public String getKey(Attachement item) {
				return "" + item.getId();
			}
		});

		final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Attachement>> attachementsLoader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Attachement>>(proxy) {
			@Override
			protected FilterPagingLoadConfig newLoadConfig() {
				return new FilterPagingLoadConfigBean();
			}
		};
		attachementsLoader.setRemoteSort(true);
		attachementsLoader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Attachement, PagingLoadResult<Attachement>>(store));

		ColumnConfig<Attachement, String> parentIdColumn = new ColumnConfig<Attachement, String>(attachmentProps.parentIdStr(), 4, "parent_id");
		parentIdColumn.setHidden(true);
		ColumnConfig<Attachement, String> parentTypeColumn = new ColumnConfig<Attachement, String>(attachmentProps.parentTypeStr(), 4, "parent_type");
		parentTypeColumn.setHidden(true);
		ColumnConfig<Attachement, String> typeColumn = new ColumnConfig<Attachement, String>(attachmentProps.attachTypeName(), 5, "Тип документа");
		ColumnConfig<Attachement, String> titleColumn = new ColumnConfig<Attachement, String>(attachmentProps.title(), 10, "Описание документа");

		List<ColumnConfig<Attachement, ?>> l = new ArrayList<ColumnConfig<Attachement, ?>>();
		l.add(parentIdColumn);
		l.add(parentTypeColumn);
		l.add(typeColumn);
		l.add(titleColumn);
		ColumnModel<Attachement> cm = new ColumnModel<Attachement>(l);

		atchmGrid = new Grid<Attachement>(store, cm) {};
		atchmGrid.setLoadMask(true);
		atchmGrid.setLoader(attachementsLoader);
		atchmGrid.getView().setForceFit(true);
		atchmGrid.getView().setAutoExpandColumn(titleColumn);
		atchmGrid.getView().setStripeRows(true);
		atchmGrid.getView().setColumnLines(true);
		atchmGrid.setBorders(false);

		parentTypeFilter = new StringFilter<Attachement>(attachmentProps.parentTypeStr());
		parentIdFilter = new StringFilter<Attachement>(attachmentProps.parentIdStr());
		GridFilters<Attachement> filters = new GridFilters<Attachement>(attachementsLoader);
		filters.initPlugin(atchmGrid);
		filters.addFilter(parentIdFilter);
		filters.addFilter(parentTypeFilter);

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setBorders(false);
		con.add(atchmGrid, new VerticalLayoutData(1, 1));

		pagingToolBar = new PagingToolBar(10);
		pagingToolBar.bind(attachementsLoader);
		pagingToolBar.addStyleName(ThemeStyles.get().style().borderTop());
		pagingToolBar.getElement().getStyle().setProperty("borderBottom", "none");
		con.add(pagingToolBar, new VerticalLayoutData(1, 25));

		BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
		flex2.setFlex(5);
		this.add(con, flex2);
	}
	
	public void init(int parentId) {
		this.setData("parentId", parentId);
		
		TextField field = (TextField) parentIdFilter.getMenu().getWidget(0);
		parentIdFilter.setActive(false, false);
		field.setValue(String.valueOf(parentId), true);
		parentIdFilter.setActive(true, true);
		
		field = (TextField) parentTypeFilter.getMenu().getWidget(0);
		parentTypeFilter.setActive(false, false);
		field.setValue(String.valueOf(parentType), true);
		parentTypeFilter.setActive(true, false);
		
		tempAttachIDs.clear();
	}
	
	public void setEditMode(boolean editMode) {
		boolean isAttached;
		for (int idx=0; idx < buttons.size(); idx++) {
			isAttached = (buttons.get(idx) != null);
			if (isAttached)
				buttons.get(idx).removeFromParent();
		}
//		addDocBtn.setVisible(editMode);
//		editDocBtn.setVisible(editMode);
//		deleteDocBtn.setVisible(editMode);
		for (int idx=0; idx < buttons.size(); idx++) {
			if (editMode || buttons.get(idx) == openDocBtn)
				topToolBar.add(buttons.get(idx));
		}
	}
	
	public void cancelLastUploads() {
//		System.out.println("cancelling uploads, ids="+tempAttachIDs);
//		attachementService.deleteAttachments(tempAttachIDs, new AsyncCallback<Void>() {
//			@Override
//			public void onFailure(Throwable caught) {}
//			@Override
//			public void onSuccess(Void result) {}
//		});
	}
}
