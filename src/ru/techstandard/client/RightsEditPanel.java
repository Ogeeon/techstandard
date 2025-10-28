package ru.techstandard.client;

import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.AccessGroup.AccessGroupProps;
import ru.techstandard.client.model.AccessRule;
import ru.techstandard.client.model.AccessRule.AccessRuleProps;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.shared.AccessGroupService;
import ru.techstandard.shared.AccessGroupServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class RightsEditPanel extends DockLayoutPanel {
	EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	ListStore<Employee> emplStore;
	Grid<Employee> employeeGrid;
	ListStore<Employee> nonGrpEmplStore;
	AccessGroupServiceAsync accessGroupService = GWT.create(AccessGroupService.class);
	ListStore<AccessGroup> accGrpStore;
	Grid<AccessGroup> accessGroupGrid;
	ListStore<AccessRule> accRuleStore;
	Grid<AccessRule> accessRuleGrid;
	
	AccessGroupInfoWindow grpInfoWnd;
	AccessGroup group;

	public RightsEditPanel(Unit unit, AccessGroup accessGroup) {
		super(unit);
		group = accessGroup;
		
		grpInfoWnd = new AccessGroupInfoWindow();
		grpInfoWnd.addHideHandler(new HideHandler() {
			@Override
			public void onHide(HideEvent event) {
				Window wnd = (Window) event.getSource();
				if (wnd.getData("hideButton") == null)
					return;
				if (wnd.getData("hideButton").equals("save")) {
					fillGroupsStore();
				}
			}
		});
		
		ContentPanel northPanel = new ContentPanel();
		northPanel.setHeadingText("Группы доступа");
		
		VerticalLayoutContainer groupsVLC = new VerticalLayoutContainer();
	    groupsVLC.setBorders(false);
	    northPanel.add(groupsVLC);
	    
	    ToolBar groupsToolBar = new ToolBar();
	    groupsVLC.add(groupsToolBar, new VerticalLayoutData(1, -1));
	    
	    if (group.isAllowed(Constants.ACCESS_INSERT, "rights")) {
	    	TextButton addGroupBtn = new TextButton("Создать группу");
	    	addGroupBtn.setIcon(Images.INSTANCE.addRow());
	    	groupsToolBar.add(addGroupBtn);
	    	addGroupBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			final AccessGroup group = new AccessGroup();
	    			for (int idx = 0; idx < Constants.SECTION_KEYS.length; idx++) {
	    				group.setAllowed(Constants.ACCESS_READ, Constants.SECTION_KEYS[idx], true);
	    				group.setAllowed(Constants.ACCESS_UPDATE, Constants.SECTION_KEYS[idx], false);
	    				group.setAllowed(Constants.ACCESS_INSERT, Constants.SECTION_KEYS[idx], false);
	    				group.setAllowed(Constants.ACCESS_DELETE, Constants.SECTION_KEYS[idx], false);
	    				group.setAllowed(Constants.ACCESS_PRINT, Constants.SECTION_KEYS[idx], false);
	    			}
	    			accessGroupService.addAccessGroup(group, new AsyncCallback<Integer>() {
	    				@Override
	    				public void onSuccess(Integer result) {
	    					group.setId(result);
	    					grpInfoWnd.editInfo(group, true);						
	    				}
	    				@Override
	    				public void onFailure(Throwable caught) {
	    					AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись информацию о группе."+caught.getMessage());
	    					d.show();
	    				}
	    			});
	    		}
	    	});
	    }
	    
	    if (group.isAllowed(Constants.ACCESS_UPDATE, "rights")) {
	    	TextButton editGroupBtn = new TextButton("Редактировать группу");
	    	editGroupBtn.setIcon(Images.INSTANCE.editRow());
	    	groupsToolBar.add(editGroupBtn);
	    	editGroupBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			if (accessGroupGrid.getSelectionModel().getSelectedItem() == null) {
	    				MessageBox d = new MessageBox("Не выбрана группа", "Пожалуйста, выберите группу доступа, которую Вы хотите отредактировать.");
	    				d.setIcon(Images.INSTANCE.information());
	    				d.show();
	    				return;
	    			}
	    			grpInfoWnd.editInfo(accessGroupGrid.getSelectionModel().getSelectedItem(), false);
	    		}
	    	});
	    }

	    if (group.isAllowed(Constants.ACCESS_DELETE, "rights")) {
	    	TextButton deleteGroupBtn = new TextButton("Удалить группу");
	    	deleteGroupBtn.setIcon(Images.INSTANCE.delete());
	    	groupsToolBar.add(deleteGroupBtn);
	    	deleteGroupBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			if (accessGroupGrid.getSelectionModel().getSelectedItem() == null) {
	    				MessageBox d = new MessageBox("Не выбрана группа", "Пожалуйста, выберите группу, которую Вы хотите удалить.");
	    				d.setIcon(Images.INSTANCE.information());
	    				d.show();
	    				return;
	    			}
	    			ConfirmMessageBox box = new ConfirmMessageBox("Удаление группы", "Вы уверены, что хотите удалить выбранную группу?");
	    			box.setIcon(Images.INSTANCE.warning());
	    			box.addDialogHideHandler(new DialogHideHandler() {
	    				@Override
	    				public void onDialogHide(DialogHideEvent event) {
	    					if (event.getHideButton() == PredefinedButton.YES) {
	    						accessGroupService.deleteAccessGroup(accessGroupGrid.getSelectionModel().getSelectedItem().getId(), new AsyncCallback<Boolean>() {
	    							@Override
	    							public void onSuccess(Boolean result) {
	    								fillGroupsStore();						
	    							}
	    							@Override
	    							public void onFailure(Throwable caught) {
	    								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось выполнить удаление."+caught.getMessage());
	    								d.show();
	    							}
	    						});
	    					}
	    				}
	    			});
	    			box.show();
	    		}
	    	});
	    }
		
		final AccessGroupProps accGrpProps = GWT.create(AccessGroupProps.class);

		accGrpStore = new ListStore<AccessGroup>(new ModelKeyProvider<AccessGroup>() {
					@Override
					public String getKey(AccessGroup item) {
						return "" + item.getId();
					}
				});
		fillGroupsStore();
		
		ColumnConfig<AccessGroup, String> nameColumn = new ColumnConfig<AccessGroup, String>(accGrpProps.name(), 3, "Наименование группы");
		ColumnConfig<AccessGroup, String> descriptionColumn = new ColumnConfig<AccessGroup, String>(accGrpProps.description(), 7, "Описание");
		ColumnConfig<AccessGroup, String> creatorColumn = new ColumnConfig<AccessGroup, String>(accGrpProps.creator(), 2, "Создание заданий");
		ColumnConfig<AccessGroup, String> needApprColumn = new ColumnConfig<AccessGroup, String>(accGrpProps.needApproval(), 2, "Требуется согласование");
		ColumnConfig<AccessGroup, String> approverColumn = new ColumnConfig<AccessGroup, String>(accGrpProps.approver(), 2, "Согласование заданий");
		ColumnConfig<AccessGroup, String> deleteConfirmerColumn = new ColumnConfig<AccessGroup, String>(accGrpProps.deleteConfirmer(), 2, "Утверждение удалений");
		
		List<ColumnConfig<AccessGroup, ?>> l = new ArrayList<ColumnConfig<AccessGroup, ?>>();
		l.add(nameColumn);
		l.add(descriptionColumn);
		l.add(creatorColumn);
		l.add(needApprColumn);
		l.add(approverColumn);
		l.add(deleteConfirmerColumn);
		
		ColumnModel<AccessGroup> cm = new ColumnModel<AccessGroup>(l);
		
		accessGroupGrid = new Grid<AccessGroup>(accGrpStore, cm) {};
		
		accessGroupGrid.setLoadMask(true);
		accessGroupGrid.getView().setForceFit(true);
		accessGroupGrid.getView().setAutoExpandColumn(nameColumn);
		accessGroupGrid.getView().setStripeRows(true);
		accessGroupGrid.getView().setColumnLines(true);
		accessGroupGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		groupsVLC.add(accessGroupGrid, new VerticalLayoutData(1, 1));
		
		accessGroupGrid.getSelectionModel().addSelectionHandler(new SelectionHandler<AccessGroup>() {
			@Override
			public void onSelection(SelectionEvent<AccessGroup> event) {
				employeeService.getEmployeesByAccessGroup(event.getSelectedItem().getId(), false, new AsyncCallback<List<Employee>>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось загрузить список групп доступа.<br>"+caught.getLocalizedMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Employee> result) {
						emplStore.replaceAll(result);
					}
				});
				employeeService.getEmployeesByAccessGroup(event.getSelectedItem().getId(), true, new AsyncCallback<List<Employee>>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось загрузить список групп доступа.<br>"+caught.getLocalizedMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Employee> result) {
						nonGrpEmplStore.replaceAll(result);
					}
				});
				fillRightsStore(event.getSelectedItem());
			}
		});
		
		accessGroupGrid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
			@Override
			public void onCellClick(CellDoubleClickEvent event) {
				grpInfoWnd.editInfo(accessGroupGrid.getSelectionModel().getSelectedItem(), false);	
			}
		});
		
		// **********************  Пользователи  **********************************
		
		ContentPanel westPanel = new ContentPanel();
		westPanel.setHeadingText("Пользователи группы");
		
		VerticalLayoutContainer usersVLC = new VerticalLayoutContainer();
		usersVLC.setBorders(false);
	    westPanel.add(usersVLC);

    	final EmployeeProps nonGrpEmplProps = GWT.create(EmployeeProps.class);
    	nonGrpEmplStore = new ListStore<Employee>(nonGrpEmplProps.id());
	    
	    if (group.isAllowed(Constants.ACCESS_UPDATE, "rights")) {
	    	ToolBar usersToolBar = new ToolBar();
	    	usersVLC.add(usersToolBar, new VerticalLayoutData(1, -1));

	    	TextButton addUserBtn = new TextButton("Добавить пользователя в группу");
	    	addUserBtn.setIcon(Images.INSTANCE.addRow());
	    	usersToolBar.add(addUserBtn);
	    	addUserBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			if (accessGroupGrid.getSelectionModel().getSelectedItem() == null) {
	    				MessageBox d = new MessageBox("Не выбрана группа", "Пожалуйста, выберите группу доступа в верхней части окна.");
	    				d.setIcon(Images.INSTANCE.information());
	    				d.show();
	    				return;
	    			}
	    			final ComboMessageBox<Employee> newEmplCombo = new ComboMessageBox<Employee>("Добавление нового пользователя в группу", 
	    					"Выберите пользователя, которого нужно добавить в группу", nonGrpEmplStore, nonGrpEmplProps.nameLabel());
	    			newEmplCombo.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	    			newEmplCombo.addDialogHideHandler(new DialogHideHandler() {
	    				@Override
	    				public void onDialogHide(DialogHideEvent event) {
	    					if (event.getHideButton() == PredefinedButton.CANCEL) {
	    						return;
	    					}
	    					if (newEmplCombo.getValue() == null) {
	    						MessageBox d = new MessageBox("Недостаточно данных", "Пожалуйста, выберите пользователя.");
	    						d.setIcon(Images.INSTANCE.information());
	    						d.show();
	    						return;
	    					}
	    					final Employee addedEmpl = newEmplCombo.getValue();
	    					addedEmpl.setGroup(accessGroupGrid.getSelectionModel().getSelectedItem().getId());
	    					employeeService.updateEmployee(addedEmpl, new AsyncCallback<Boolean>() {
	    						@Override
	    						public void onFailure(Throwable caught) {
	    							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
	    							d.show();
	    						}
	    						@Override
	    						public void onSuccess(Boolean result) {
	    							emplStore.add(addedEmpl);
	    						}
	    					});
	    				}
	    			});
	    			newEmplCombo.show();
	    		}
	    	});

	    	TextButton changeUserGroupBtn = new TextButton("Сменить группу пользователя");
	    	changeUserGroupBtn.setIcon(Images.INSTANCE.editRow());
	    	usersToolBar.add(changeUserGroupBtn);
	    	changeUserGroupBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			if (employeeGrid.getSelectionModel().getSelectedItem() == null) {
	    				MessageBox d = new MessageBox("Не выбран пользователь", "Пожалуйста, выберите пользователя из списка текущих членов группы.");
	    				d.setIcon(Images.INSTANCE.information());
	    				d.show();
	    				return;
	    			}
	    			ListStore<AccessGroup> targetAccGrpStore = new ListStore<AccessGroup>(new ModelKeyProvider<AccessGroup>() {
	    				@Override
	    				public String getKey(AccessGroup item) {
	    					return "" + item.getId();
	    				}
	    			});
	    			targetAccGrpStore.addAll(accGrpStore.getAll());
	    			List<AccessGroup> targets = targetAccGrpStore.getAll();
	    			// убираем из списка текущую группу пользователя
	    			for (int idx = 0; idx < targets.size(); idx++) {
	    				if (targets.get(idx).getId() == employeeGrid.getSelectionModel().getSelectedItem().getGroup()) {
	    					targetAccGrpStore.remove(targets.get(idx));
	    					break;
	    				}
	    			}
	    			final ComboMessageBox<AccessGroup> newGroupCombo = new ComboMessageBox<AccessGroup>("Изменение группы для пользователя", 
	    					"Укажите новую группу доступ для выбранного пользователя", targetAccGrpStore, accGrpProps.nameLabel());
	    			newGroupCombo.addDialogHideHandler(new DialogHideHandler() {
	    				@Override
	    				public void onDialogHide(DialogHideEvent event) {
	    					if (event.getHideButton() == PredefinedButton.CANCEL) {
	    						return;
	    					}
	    					if (newGroupCombo.getValue() == null) {
	    						MessageBox d = new MessageBox("Недостаточно данных", "Пожалуйста, выберите группу.");
	    						d.setIcon(Images.INSTANCE.information());
	    						d.show();
	    						return;
	    					}
	    					final Employee removedEmpl = employeeGrid.getSelectionModel().getSelectedItem();
	    					AccessGroup newAccGrp = newGroupCombo.getValue();
	    					removedEmpl.setGroup(newAccGrp.getId());
	    					employeeService.updateEmployee(removedEmpl, new AsyncCallback<Boolean>() {
	    						@Override
	    						public void onFailure(Throwable caught) {
	    							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
	    							d.show();
	    						}
	    						@Override
	    						public void onSuccess(Boolean result) {
	    							emplStore.remove(removedEmpl);
	    						}
	    					});
	    				}
	    			});
	    			newGroupCombo.show();
	    		}
	    	});
	    }
		
		
		EmployeeProps employeeProps = GWT.create(EmployeeProps.class);

		emplStore = new ListStore<Employee>(new ModelKeyProvider<Employee>() {
					@Override
					public String getKey(Employee item) {
						return "" + item.getId();
					}
				});
		
		ColumnConfig<Employee, String> emplLoginColumn = new ColumnConfig<Employee, String>(employeeProps.login(), 3, "Логин пользователя");
		ColumnConfig<Employee, String> emplNameColumn = new ColumnConfig<Employee, String>(employeeProps.name(), 7, "ФИО пользователя");
		
		List<ColumnConfig<Employee, ?>> el = new ArrayList<ColumnConfig<Employee, ?>>();
		el.add(emplLoginColumn);
		el.add(emplNameColumn);
		
		ColumnModel<Employee> ecm = new ColumnModel<Employee>(el);
		
		employeeGrid = new Grid<Employee>(emplStore, ecm) {};
		
		employeeGrid.setLoadMask(true);
		employeeGrid.getView().setForceFit(true);
		employeeGrid.getView().setAutoExpandColumn(emplNameColumn);
		employeeGrid.getView().setStripeRows(true);
		employeeGrid.getView().setColumnLines(true);
		employeeGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		usersVLC.add(employeeGrid, new VerticalLayoutData(1, 1));
		
		// **********************  Права доступа  **********************************
		ContentPanel eastPanel = new ContentPanel();
		eastPanel.setHeadingText("Права доступа");
		
		VerticalLayoutContainer rightsVLC = new VerticalLayoutContainer();
		rightsVLC.setBorders(false);
	    eastPanel.add(rightsVLC);
	    
	    if (group.isAllowed(Constants.ACCESS_UPDATE, "rights")) {
	    	ToolBar rightsToolBar = new ToolBar();
	    	rightsVLC.add(rightsToolBar, new VerticalLayoutData(1, -1));

	    	TextButton applyRightsBtn = new TextButton("Сохранить права доступа");
	    	applyRightsBtn.setIcon(Images.INSTANCE.apply());
	    	rightsToolBar.add(applyRightsBtn);		
	    	applyRightsBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			if (accessGroupGrid.getSelectionModel().getSelectedItem() == null) {
	    				return;
	    			}
	    			AccessGroup grp = accessGroupGrid.getSelectionModel().getSelectedItem();
	    			accRuleStore.commitChanges();
	    			for (int idx = 0 ; idx < accRuleStore.getAll().size(); idx++) {
	    				AccessRule rule = accRuleStore.get(idx);
	    				grp.setAllowed(Constants.ACCESS_READ, rule.getId(), rule.isR());
	    				grp.setAllowed(Constants.ACCESS_UPDATE, rule.getId(), rule.isU());
	    				grp.setAllowed(Constants.ACCESS_INSERT, rule.getId(), rule.isI());
	    				grp.setAllowed(Constants.ACCESS_DELETE, rule.getId(), rule.isD());
	    				grp.setAllowed(Constants.ACCESS_PRINT, rule.getId(), rule.isP());
	    			}
	    			accessGroupService.updateAccessGroup(grp, false, new AsyncCallback<Boolean>() {
	    				@Override
	    				public void onFailure(Throwable caught) {
	    					AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
	    					d.show();
	    				}
	    				@Override public void onSuccess(Boolean result) {}
	    			});
	    		}
	    	});

	    	TextButton resetRightsBtn = new TextButton("Сброс");
	    	resetRightsBtn.setIcon(Images.INSTANCE.undo2());
	    	rightsToolBar.add(resetRightsBtn);
	    	resetRightsBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			accRuleStore.rejectChanges();
	    		}
	    	});
	    }
		
		AccessRuleProps accessRuleProps = GWT.create(AccessRuleProps.class);

		accRuleStore = new ListStore<AccessRule>(new ModelKeyProvider<AccessRule>() {
					@Override
					public String getKey(AccessRule item) {
						return "" + item.getId();
					}
				});

	    CheckBoxCell cbCell = new CheckBoxCell();
	    cbCell.setReadOnly(!group.isAllowed(Constants.ACCESS_UPDATE, "rights"));
//		SimpleSafeHtmlCell<Boolean> cbCell = new SimpleSafeHtmlCell<Boolean>(new AbstractSafeHtmlRenderer<Boolean>() {
//	        @Override
//	        public SafeHtml render(Boolean object) {
//	          return SafeHtmlUtils.fromString(object ? "Да" : "Нет");
//	        }
//	      });
		
		ColumnConfig<AccessRule, String> sectionNameColumn = new ColumnConfig<AccessRule, String>(accessRuleProps.name(), 10, "Раздел программы");
		ColumnConfig<AccessRule, Boolean> rColumn = new ColumnConfig<AccessRule, Boolean>(accessRuleProps.r(), 2, "Чтение");
		ColumnConfig<AccessRule, Boolean> uColumn = new ColumnConfig<AccessRule, Boolean>(accessRuleProps.u(), 2, "Изменение");
		ColumnConfig<AccessRule, Boolean> iColumn = new ColumnConfig<AccessRule, Boolean>(accessRuleProps.i(), 2, "Создание");
		ColumnConfig<AccessRule, Boolean> dColumn = new ColumnConfig<AccessRule, Boolean>(accessRuleProps.d(), 2, "Удаление");
		ColumnConfig<AccessRule, Boolean> pColumn = new ColumnConfig<AccessRule, Boolean>(accessRuleProps.p(), 2, "Печать");
		rColumn.setCell(cbCell);
		uColumn.setCell(cbCell);
		iColumn.setCell(cbCell);
		dColumn.setCell(cbCell);
		pColumn.setCell(cbCell);
		
		List<ColumnConfig<AccessRule, ?>> rl = new ArrayList<ColumnConfig<AccessRule, ?>>();
		rl.add(sectionNameColumn);
		rl.add(rColumn);
		rl.add(uColumn);
		rl.add(iColumn);
		rl.add(dColumn);
		rl.add(pColumn);
		
		ColumnModel<AccessRule> rcm = new ColumnModel<AccessRule>(rl);
		
		accessRuleGrid = new Grid<AccessRule>(accRuleStore, rcm) {};
		
//		if (group.isAllowed(Constants.ACCESS_UPDATE, "rights")) {
//			final GridEditing<AccessRule> editing = new GridInlineEditing<AccessRule>(accessRuleGrid);
//			CheckBox checkField = new CheckBox();
//			editing.addEditor(rColumn, checkField);
//			editing.addEditor(uColumn, checkField);
//			editing.addEditor(iColumn, checkField);
//			editing.addEditor(dColumn, checkField);
//			editing.addEditor(pColumn, checkField);
//			editing.addCompleteEditHandler(new CompleteEditHandler<AccessRule>() {
//				@Override
//				public void onCompleteEdit(CompleteEditEvent<AccessRule> event) {
//					//				System.out.println("edited cell at ["+event.getEditCell().getCol()+":"+event.getEditCell().getRow()+"] ->"+accessRuleGrid.getSelectionModel().getSelectedItem().getId());
//					//				accRuleStore.getModifiedRecords().iterator()
//					//				accRuleStore.getRecord(null).getValue(property)
//				}
//			});
//		}
	    
		accessRuleGrid.setLoadMask(true);
		accessRuleGrid.getView().setForceFit(true);
		accessRuleGrid.getView().setAutoExpandColumn(sectionNameColumn);
		accessRuleGrid.getView().setStripeRows(true);
		accessRuleGrid.getView().setColumnLines(true);
		accessRuleGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		rightsVLC.add(accessRuleGrid, new VerticalLayoutData(1, 1));

		this.addNorth(northPanel, 35);
		this.addEast(eastPanel, 50);
		this.add(westPanel);
	}

	private void fillRightsStore(AccessGroup group) {
		accRuleStore.clear();
		for (int k = 0; k < Constants.SECTION_KEYS.length; k++) {
			AccessRule rule = new AccessRule(Constants.SECTION_KEYS[k], Constants.SECTION_NAMES.get(Constants.SECTION_KEYS[k]));
			rule.setR(group.isAllowed(Constants.ACCESS_READ, Constants.SECTION_KEYS[k]));
			rule.setU(group.isAllowed(Constants.ACCESS_UPDATE, Constants.SECTION_KEYS[k]));
			rule.setI(group.isAllowed(Constants.ACCESS_INSERT, Constants.SECTION_KEYS[k]));
			rule.setD(group.isAllowed(Constants.ACCESS_DELETE, Constants.SECTION_KEYS[k]));
			rule.setP(group.isAllowed(Constants.ACCESS_PRINT, Constants.SECTION_KEYS[k]));
			accRuleStore.add(rule);
		}
	}
	
	private void fillGroupsStore() {
		accGrpStore.clear();
		accessGroupService.getAccessGroups(new AsyncCallback<List<AccessGroup>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось загрузить список групп доступа.<br>"+caught.getLocalizedMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<AccessGroup> result) {
				accGrpStore.addAll(result);
			}
		});		
	}
}
