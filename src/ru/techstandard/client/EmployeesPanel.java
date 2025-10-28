package ru.techstandard.client;

import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.Department;
import ru.techstandard.client.model.Employee;
import ru.techstandard.client.model.DeptTreeNode;
import ru.techstandard.client.model.DeptTreeNode.TreeNodeProps;
import ru.techstandard.client.model.EmployeeProps;
import ru.techstandard.shared.DepartmentService;
import ru.techstandard.shared.DepartmentServiceAsync;
import ru.techstandard.shared.EmployeeService;
import ru.techstandard.shared.EmployeeServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreAddEvent.StoreAddHandler;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.dnd.core.client.DND.Feedback;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent.DndDragStartHandler;
import com.sencha.gxt.dnd.core.client.TreeDragSource;
import com.sencha.gxt.dnd.core.client.TreeDropTarget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;

public class EmployeesPanel extends BorderLayoutContainer {
	private final DepartmentServiceAsync departmentService = GWT.create(DepartmentService.class);
	private final EmployeeServiceAsync employeeService = GWT.create(EmployeeService.class);
	private final Images IMAGES = GWT.create(Images.class);
	private BorderLayoutContainer emplPanel;
	CardLayoutContainer cardLayout;
	ContentPanel podrViewPanel;
	CheckBox showFiredEmpls;
	PagingLoader<PagingLoadConfig, PagingLoadResult<Employee>> employeeGridLoader;
	Grid<Employee> employeeGrid;
	PagingToolBar pagingToolBar;
	ContentPanel emplViewPanel;
	int emplViewPanelActon = Constants.ACTION_UPDATE;
	EmployeeInfoPanel emplInfoPanel;

	TreeStore<DeptTreeNode> deptTreeStore;
	Tree<DeptTreeNode, String> organizationTree;
	private List<Department> departments;
	private List<Employee> employees;
	
	private boolean isInit=true;
	AccessGroup group;
	
	public EmployeesPanel(AccessGroup accessGroup) {
		super();
		emplPanel = this;
		emplPanel.setBorders(false);
		group = accessGroup;
		
	    final ContentPanel structPanel = new ContentPanel();
	    structPanel.setBorders(false);
	    structPanel.setHeadingText("Структура организации");
	    BorderLayoutData structPanelData = new BorderLayoutData(150);
	    structPanelData.setSplit(true);
	    structPanelData.setMargins(new Margins(0, 5, 0, 2));
	    structPanelData.setMinSize(200);
	    structPanelData.setSize(300);
	    
	    VerticalLayoutContainer structVLC = new VerticalLayoutContainer();
	    structVLC.setBorders(false);
	    structPanel.add(structVLC);
	    
	    ToolBar structToolBar = new ToolBar();
	    structVLC.add(structToolBar, new VerticalLayoutData(1, -1));
	    
	    if (group.isAllowed(Constants.ACCESS_INSERT, "employees")) {
	    	TextButton addNodeBtn = new TextButton("Добавить");
	    	addNodeBtn.setIcon(IMAGES.addRow());
	    	structToolBar.add(addNodeBtn);

	    	addNodeBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			DeptTreeNode updNode = organizationTree.getSelectionModel().getSelectedItem();
	    			if (updNode == null) {
	    				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите подразделение, в котором будет создано подчинённое подразделение.");
	    				d.setIcon(IMAGES.information());
	    				d.show();
	    				return;
	    			}
	    			final DeptTreeNode updNodeParent;
	    			if (updNode.isLeaf()) 
	    				updNodeParent = deptTreeStore.getParent(updNode);
	    			else
	    				updNodeParent = updNode;
	    			final PromptMessageBox newDeptPrompt = new PromptMessageBox("Ввод наименования подразделения", "Введите наименование нового подразделения:");
	    			newDeptPrompt.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	    			newDeptPrompt.addDialogHideHandler(new DialogHideHandler() {
	    				@Override
	    				public void onDialogHide(DialogHideEvent event) {
	    					if (event.getHideButton() == PredefinedButton.CANCEL) {
	    						return;
	    					}
	    					if (newDeptPrompt.getValue() == null || newDeptPrompt.getValue().trim().isEmpty()) {
	    						MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите наименования подразделения.");
	    						d.setIcon(Images.INSTANCE.information());
	    						d.show();
	    						return;
	    					}

	    					String[] parentNodeIdParts = updNodeParent.getId().split("_");
	    					final Department newDept = new Department(newDeptPrompt.getValue());
	    					newDept.setParentId(Integer.valueOf(parentNodeIdParts[1]));
	    					departmentService.addDepartment(newDept, new AsyncCallback<Integer>() {
	    						@Override
	    						public void onFailure(Throwable caught) {
	    							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
	    							d.show();
	    						}
	    						@Override 
	    						public void onSuccess(Integer result) {
	    							if (result == 0) {
	    								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.");
	    								d.show();
	    								return;
	    							}
	    							newDept.setId(result);
	    							departments.add(newDept);
	    							DeptTreeNode newDeptNode = new DeptTreeNode("d_"+result, newDept.getName());
	    							DeptTreeNode parentDeptNode = dept("d_"+newDept.getParentId());
	    							deptTreeStore.add(parentDeptNode, newDeptNode);
	    							organizationTree.refresh(newDeptNode);
	    						}
	    					});
	    				}
	    			});
	    			newDeptPrompt.show();
	    			newDeptPrompt.getTextField().focus();
	    		}
	    	});
	    }
		
	    if (group.isAllowed(Constants.ACCESS_UPDATE, "employees")) {
	    	TextButton renameNodeBtn = new TextButton("Переименовать");
	    	renameNodeBtn.setIcon(IMAGES.rename());
	    	structToolBar.add(renameNodeBtn);
	    	renameNodeBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			final DeptTreeNode updNode = organizationTree.getSelectionModel().getSelectedItem();
	    			if (updNode == null) {
	    				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите подразделение, которое Вы хотите переименовать.");
	    				d.setIcon(IMAGES.information());
	    				d.show();
	    				return;
	    			}
	    			if (updNode.isLeaf()) {
	    				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите подразделение. Для редактирования данных сотрудников используйте элементы управления в правой части.");
	    				d.setIcon(IMAGES.information());
	    				d.show();
	    				return;
	    			}
	    			final PromptMessageBox renameDeptPrompt = new PromptMessageBox("Изменение наименования подразделения", "Введите новое значение наименования<br/>подразделения:");
	    			renameDeptPrompt.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	    			renameDeptPrompt.getField().setValue(updNode.getName());
	    			renameDeptPrompt.addDialogHideHandler(new DialogHideHandler() {
	    				@Override
	    				public void onDialogHide(DialogHideEvent event) {
	    					if (event.getHideButton() == PredefinedButton.CANCEL) {
	    						return;
	    					}
	    					if (renameDeptPrompt.getValue() == null || renameDeptPrompt.getValue().trim().isEmpty()) {
	    						MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите наименования подразделения.");
	    						d.setIcon(Images.INSTANCE.information());
	    						d.show();
	    						return;
	    					}

	    					String[] nodeIdParts = updNode.getId().split("_");
	    					String[] parentIdParts = deptTreeStore.getParent(updNode).getId().split("_");
	    					Department updDept = new Department(Integer.valueOf(nodeIdParts[1]), renameDeptPrompt.getValue());
	    					updDept.setParentId(Integer.valueOf(parentIdParts[1]));
	    					departmentService.updateDepartment(updDept, new AsyncCallback<Boolean>() {
	    						@Override
	    						public void onFailure(Throwable caught) {
	    							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
	    							d.show();
	    						}
	    						@Override 
	    						public void onSuccess(Boolean result) {
	    							updNode.setName(renameDeptPrompt.getValue());
	    							organizationTree.refresh(updNode);
	    						}
	    					});
	    				}
	    			});
	    			renameDeptPrompt.show();
	    			renameDeptPrompt.getTextField().focus();
	    		}
	    	});
	    }
		
	    if (group.isAllowed(Constants.ACCESS_DELETE, "employees")) {
	    	TextButton deleteNodeBtn = new TextButton("Удалить");
	    	deleteNodeBtn.setIcon(IMAGES.delete());
	    	structToolBar.add(deleteNodeBtn);
	    	deleteNodeBtn.addSelectHandler(new SelectHandler() {
	    		@Override
	    		public void onSelect(SelectEvent event) {
	    			final DeptTreeNode delNode = organizationTree.getSelectionModel().getSelectedItem();
	    			if (delNode == null) {
	    				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите подразделение, которое Вы хотите удалить.");
	    				d.setIcon(IMAGES.information());
	    				d.show();
	    				return;
	    			}
	    			if (delNode.isLeaf()) {
	    				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите подразделение. Для редактирования данных сотрудников используйте элементы управления в правой части.");
	    				d.setIcon(IMAGES.information());
	    				d.show();
	    				return;
	    			}
	    			if (deptTreeStore.getChildCount(delNode) != 0) {
	    				MessageBox d = new MessageBox("Внимание","Удалить можно только подразделение, в котором нет сотрудников и подчинённых подразделений.");
	    				d.setIcon(IMAGES.information());
	    				d.show();
	    				return;
	    			}

	    			final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
	    				@Override
	    				public void onDialogHide(DialogHideEvent event) {
	    					if (event.getHideButton() == PredefinedButton.YES) {
	    						String[] nodeIdParts = delNode.getId().split("_");
	    						departmentService.deleteDepartment(Integer.valueOf(nodeIdParts[1]), new AsyncCallback<Boolean>() {
	    							@Override
	    							public void onFailure(Throwable caught) {
	    								AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось выполнить удаление.<br>"+caught.getLocalizedMessage());
	    								d.show();
	    							}
	    							@Override 
	    							public void onSuccess(Boolean result) {
	    								if(result)
	    									deptTreeStore.remove(delNode);
	    								else {
	    									AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось выполнить удаление.<br>Проверьте наличие в подразделении уволенных сотрудников.");
		    								d.show();	
	    								}
	    							}
	    						});
	    					}
	    				}
	    			};

	    			ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранное подразделение?");
	    			box.setIcon(IMAGES.warning());
	    			box.addDialogHideHandler(deleteHideHandler);
	    			box.show();
	    		}
	    	});
	    }
		
		TreeNodeProps nodeProps = GWT.create(TreeNodeProps.class);
		
		deptTreeStore = new TreeStore<DeptTreeNode>(nodeProps.id());
//		Comparator<DeptTreeNode> cmp = new Comparator<DeptTreeNode>() {
//			@Override
//			public int compare(DeptTreeNode o1, DeptTreeNode o2) {
//				if (o1.getId().startsWith("d_") && o2.getId().startsWith("e_"))
//					return 0;
//				if (o1.getId().startsWith("e_") && o2.getId().startsWith("d_"))
//					return 1;
//				
//				return o1.getName().compareTo(o2.getName());
//			}
//		};
//		deptTreeStore.addSortInfo(new StoreSortInfo<DeptTreeNode>(cmp, SortDir.DESC));
		deptTreeStore.setAutoCommit(false);
		
		deptTreeStore.addStoreAddHandler(new StoreAddHandler<DeptTreeNode>() {
			@Override
			public void onAdd(StoreAddEvent<DeptTreeNode> event) {
				// событие срабатывает при первоначальном построении дерева и при перетаскивании. в первом случае обрабатывать его не надо
				if (isInit)
					return;
				for (int idx=0; idx<event.getItems().size(); idx++) {
					final DeptTreeNode addedNode = event.getItems().get(idx);
					if (deptTreeStore.getParent(addedNode) != null) {
						final String[] nodeIdParts = addedNode.getId().split("_");
						String[] parentIdParts = deptTreeStore.getParent(addedNode).getId().split("_");
						// первая часть id - d для подразделений и e для сотрудников, третьего не дано
						if (nodeIdParts[0].equals("d")) {
							Department updDept = new Department(Integer.valueOf(nodeIdParts[1]), addedNode.getName());
							updDept.setParentId(Integer.valueOf(parentIdParts[1]));
							departmentService.updateDepartment(updDept, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									deptTreeStore.rejectChanges();
									AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
									d.show();
									deptTreeStore.commitChanges();
								}
								// В случае успешного сохранения ничего предпринимать не нужно, кроме разве что...
								// TODO Выполнить сортировку
								@Override public void onSuccess(Boolean result) {}
							});
						} else {
							Employee updEmpl = getEmpl(Integer.valueOf(nodeIdParts[1]));
							if (updEmpl != null) {
								updEmpl.setDepartmentId(Integer.valueOf(parentIdParts[1]));
								employeeService.updateEmployee(updEmpl, new AsyncCallback<Boolean>() {
									@Override
									public void onFailure(Throwable caught) {
										deptTreeStore.rejectChanges();
										AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getLocalizedMessage());
										d.show();
										deptTreeStore.commitChanges();
									}
									// В случае успешного сохранения ничего предпринимать не нужно, кроме разве что...
									// TODO Выполнить сортировку
									@Override public void onSuccess(Boolean result) {
										organizationTree.getSelectionModel().select(addedNode, false);
										emplInfoPanel.init(Integer.valueOf(nodeIdParts[1]), false, group.isAllowed(Constants.ACCESS_UPDATE, "employees"));
									}
								});
							}
						}
						deptTreeStore.commitChanges();
					} else {
						deptTreeStore.rejectChanges();
						organizationTree.refresh(addedNode);
						structPanel.forceLayout();
						return;
					}
				}
			}
		});
		
		departmentService.getAllDepartments(new AsyncCallback<List<Department>>() {
			@Override
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Employees:378 - "+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<Department> result) {
				departments = result;
				Department parent = null;
				for (Department d: result) {
					if (d.getParentId() == 0) {
						parent = d;
						break;
					}
				}
				if (parent != null) {
					DeptTreeNode node = new DeptTreeNode("d_"+parent.getId(), parent.getName());
					node.setLeaf(false);
					deptTreeStore.add(node);
					addSubNodes(node);
				} else {
					AlertMessageBox d = new AlertMessageBox("Ошибка", "Не найдено корневое подразделение.");
					d.show();
				}
				
				employeeService.getAllEmployees(new AsyncCallback<List<Employee>>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. Employees:404 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<Employee> result) {
						employees = result;
						for (Employee e: result) {
							DeptTreeNode node = new DeptTreeNode("e_"+e.getId(), e.getName());
						    node.setLeaf(true);
							DeptTreeNode parent = dept("d_"+e.getDepartmentId()); 
							if (parent != null) {
								deptTreeStore.add(parent, node);
							}
						}
					    organizationTree.expandAll();
					    isInit = false;
					}
				});
			}
		});
	    
		
		organizationTree = new Tree<DeptTreeNode, String>(deptTreeStore, nodeProps.name()){
			@Override
			  protected boolean hasChildren(DeptTreeNode model) {
				return model.hasChildren();
			  }
			};
			
		organizationTree.getStyle().setNodeOpenIcon(IMAGES.dept_open());
		organizationTree.getStyle().setNodeCloseIcon(IMAGES.dept());
		organizationTree.getStyle().setLeafIcon(IMAGES.empl());
	    organizationTree.setBorders(false);
	    organizationTree.getSelectionModel().addSelectionHandler(new SelectionHandler<DeptTreeNode>() {
			@Override
			public void onSelection(SelectionEvent<DeptTreeNode> event) {
				if (event.getSelectedItem().isLeaf()) {
					cardLayout.setActiveWidget(emplViewPanel);
					String[] nodeIdParts = event.getSelectedItem().getId().split("_");
					emplInfoPanel.init(Integer.valueOf(nodeIdParts[1]), false, group.isAllowed(Constants.ACCESS_UPDATE, "employees"));
					emplViewPanelActon = Constants.ACTION_UPDATE;
				}
				else {
					cardLayout.setActiveWidget(podrViewPanel);
					employeeGridLoader.load();
				}
			}
		});

	    structVLC.add(organizationTree, new VerticalLayoutData(1, 1));
	    
	    final TreeDragSource<DeptTreeNode> source = new TreeDragSource<DeptTreeNode>(organizationTree);
	    source.addDragStartHandler(new DndDragStartHandler() {
	      @Override
	      public void onDragStart(DndDragStartEvent event) {
	    	  DeptTreeNode sel = organizationTree.getSelectionModel().getSelectedItem();
//	    	  Нельзя перетаскивать корневой узел (директорат) и любой при отсутствии прав на изменение
	        if ((sel != null && sel == deptTreeStore.getRootItems().get(0)) || !group.isAllowed(Constants.ACCESS_UPDATE, "employees")) {
	          event.setCancelled(true);
	          event.getStatusProxy().setStatus(false);
	        }
	      }
	    });
	    
	    TreeDropTarget<DeptTreeNode> target = new TreeDropTarget<DeptTreeNode>(organizationTree);
	    target.setAllowSelfAsSource(true);
	    target.setFeedback(Feedback.BOTH);

	    MarginData centerData = new MarginData();
	    cardLayout = new CardLayoutContainer();

	    podrViewPanel = new ContentPanel();
	    podrViewPanel.setHeadingText("Подразделение");
	    
	    VerticalLayoutContainer podrContainer = new VerticalLayoutContainer();
	    podrViewPanel.add(podrContainer);
	    
	    ToolBar podrToolBar = new ToolBar();
        podrContainer.add(podrToolBar, new VerticalLayoutData(1, -1));
        
        if (group.isAllowed(Constants.ACCESS_INSERT, "employees")) {
        	TextButton addEmplBtn = new TextButton("Создать сотрудника");
        	addEmplBtn.setIcon(IMAGES.addRow());
        	podrToolBar.add(addEmplBtn);
        	addEmplBtn.addSelectHandler(new SelectHandler() {
        		@Override
        		public void onSelect(SelectEvent event) {
        			DeptTreeNode selectedDept = organizationTree.getSelectionModel().getSelectedItem();
        			if (selectedDept == null) {
        				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите подразделение, в котором Вы хотите создать нового сотрудника.");
        				d.setIcon(IMAGES.information());
        				d.show();
        				return;
        			}
        			String[] deptIdParts = selectedDept.getId().split("_");
        			int dept = Integer.valueOf(deptIdParts[1]);

        			final Employee tmpEmpl = new Employee();
        			tmpEmpl.setDepartmentId(dept);
        			employeeService.addEmployee(tmpEmpl, new AsyncCallback<Integer>() {
        				@Override
        				public void onFailure(Throwable caught) {
        					AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось подготовить запись о новом сотруднике.<br>"+caught.getLocalizedMessage());
        					d.show();
        				}
        				@Override
        				public void onSuccess(Integer result) {
        					tmpEmpl.setId(result);
        					cardLayout.setActiveWidget(emplViewPanel);
        					emplInfoPanel.init(tmpEmpl.getId(), true, group.isAllowed(Constants.ACCESS_UPDATE, "employees"));
        					emplViewPanelActon = Constants.ACTION_ADD;
        				}
        			});
        		}
        	});
        }
		
        if (group.isAllowed(Constants.ACCESS_UPDATE, "employees")) {
        	TextButton editEmplBtn = new TextButton("Редактировать сотрудника");
        	editEmplBtn.setIcon(IMAGES.editRow());
        	podrToolBar.add(editEmplBtn);

        	editEmplBtn.addSelectHandler(new SelectHandler() {
        		@Override
        		public void onSelect(SelectEvent event) {
        			Employee empl = employeeGrid.getSelectionModel().getSelectedItem();
        			if (empl == null) {
        				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите сотрудника, которого Вы хотите отредактировать.");
        				d.setIcon(IMAGES.information());
        				d.show();
        				return;
        			}
        			DeptTreeNode node = dept("e_"+empl.getId());
        			organizationTree.getSelectionModel().select(node, false);
        			cardLayout.setActiveWidget(emplViewPanel);
        			emplInfoPanel.init(empl.getId(), false, group.isAllowed(Constants.ACCESS_UPDATE, "employees"));
        			emplViewPanelActon = Constants.ACTION_UPDATE;
        		}
        	});
        }
		
        if (group.isAllowed(Constants.ACCESS_DELETE, "employees")) {
        	final DialogHideHandler deleteHideHandler = new DialogHideHandler() {
        		@Override
        		public void onDialogHide(DialogHideEvent event) {
        			if (event.getHideButton() == PredefinedButton.YES) {
        				final Employee empl = employeeGrid.getSelectionModel().getSelectedItem();
        				if (empl == null) {
        					MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите сотрудника, которого Вы хотите удалить.");
        					d.setIcon(IMAGES.information());
        					d.show();
        					return;
        				}
        				employeeService.deleteEmployee(empl.getId(), new AsyncCallback<Boolean>() {
        					@Override public void onFailure(Throwable caught) {
        						AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось выполнить удаление.<br>"+caught.getLocalizedMessage());
        						d.show();
        					}
        					@Override public void onSuccess(Boolean result) {
        						DeptTreeNode node = dept("e_"+empl.getId());
        						// уволенные не показываются в дереве слева
        						if (node != null)
        							deptTreeStore.remove(node);
        						pagingToolBar.refresh();
        					}
        				});				
        			}
        		}
        	};

        	TextButton deleteEmplBtn = new TextButton("Удалить сотрудника");
        	deleteEmplBtn.setIcon(IMAGES.delete());
        	podrToolBar.add(deleteEmplBtn);
        	deleteEmplBtn.addSelectHandler(new SelectHandler() {
        		@Override
        		public void onSelect(SelectEvent event) {
        			Employee empl = employeeGrid.getSelectionModel().getSelectedItem();
        			if (empl == null) {
        				MessageBox d = new MessageBox("Внимание","Пожалуйста, выберите сотрудника, которого Вы хотите удалить.");
        				d.setIcon(IMAGES.information());
        				d.show();
        				return;
        			}
        			ConfirmMessageBox box = new ConfirmMessageBox("Внимание", "Вы уверены, что хотите удалить выбранного сотрудника?");
        			box.setIcon(IMAGES.warning());
        			box.addDialogHideHandler(deleteHideHandler);
        			box.show();
        		}
        	});
        }
		
		podrToolBar.add(new FillToolItem());
		
		showFiredEmpls = new CheckBox();
		showFiredEmpls.setBoxLabel("Отображать уволенных");
	    showFiredEmpls.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				pagingToolBar.refresh();
			}
		});
		podrToolBar.add(showFiredEmpls);
		
		RpcProxy<PagingLoadConfig, PagingLoadResult<Employee>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<Employee>>() {
			@Override
			public void load(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Employee>> callback) {
				DeptTreeNode selectedDept = organizationTree.getSelectionModel().getSelectedItem();
				int dept = 0;
				if (selectedDept != null) {
					String[] deptIdParts = selectedDept.getId().split("_");
					dept = Integer.valueOf(deptIdParts[1]);
				}
				employeeService.getEmployeesByDepartment(dept, showFiredEmpls.getValue(), loadConfig, callback);
			}
		};
		
		EmployeeProps props = GWT.create(EmployeeProps.class);

		ListStore<Employee> store = new ListStore<Employee>(new ModelKeyProvider<Employee>() {
					@Override
					public String getKey(Employee item) {
						return "" + item.getId();
					}
				});
		
		employeeGridLoader = new PagingLoader<PagingLoadConfig, PagingLoadResult<Employee>>(proxy);
		employeeGridLoader.setRemoteSort(true);
		employeeGridLoader.addLoadHandler(new LoadResultListStoreBinding<PagingLoadConfig, Employee, PagingLoadResult<Employee>>(store));
		
		ColumnConfig<Employee, String> nameColumn = new ColumnConfig<Employee, String>(props.name(), 20, "ФИО сотрудника");
		ColumnConfig<Employee, String> loginColumn = new ColumnConfig<Employee, String>(props.login(), 5, "Логин");
		ColumnConfig<Employee, String> passwordColumn = new ColumnConfig<Employee, String>(props.password(), 5, "Пароль");
		ColumnConfig<Employee, String> emailColumn = new ColumnConfig<Employee, String>(props.email(), 5, "e-mail");
		ColumnConfig<Employee, String> positionColumn = new ColumnConfig<Employee, String>(props.positionName(), 10, "Должность");
		ColumnConfig<Employee, String> groupColumn = new ColumnConfig<Employee, String>(props.groupName(), 10, "Группа доступа");
		ColumnConfig<Employee, String> bossColumn = new ColumnConfig<Employee, String>(props.deptLeader(), 10, "Начальник подразделения");
		ColumnConfig<Employee, String> firedColumn = new ColumnConfig<Employee, String>(props.firedStr(), 5, "Уволенный");
		
		List<ColumnConfig<Employee, ?>> l = new ArrayList<ColumnConfig<Employee, ?>>();
		l.add(nameColumn);
		l.add(loginColumn);
		l.add(passwordColumn);
		l.add(emailColumn);
		l.add(positionColumn);
		l.add(groupColumn);
		l.add(bossColumn);
		l.add(firedColumn);
		
		ColumnModel<Employee> cm = new ColumnModel<Employee>(l);
		
		employeeGrid = new Grid<Employee>(store, cm) {};
		
		employeeGrid.setLoadMask(true);
		employeeGrid.setLoader(employeeGridLoader);
		employeeGrid.getView().setForceFit(true);
		employeeGrid.getView().setAutoExpandColumn(nameColumn);
		employeeGrid.getView().setStripeRows(true);
		employeeGrid.getView().setColumnLines(true);
		employeeGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		pagingToolBar = new PagingToolBar(30);
		pagingToolBar.setHeight(25);
		pagingToolBar.addStyleName(ThemeStyles.get().style().borderTop());
		pagingToolBar.getElement().getStyle().setProperty("borderBottom", "none");
		pagingToolBar.bind(employeeGridLoader);
		
		podrContainer.add(employeeGrid, new VerticalLayoutData(1, 1));
		podrContainer.add(pagingToolBar, new VerticalLayoutData(1, -1));
		
		emplViewPanel = new ContentPanel();
	    emplViewPanel.setHeadingText("Сотрудник");
	    emplInfoPanel = new EmployeeInfoPanel();
	    emplInfoPanel.addValueChangeHandler(new ValueChangeHandler<Employee>() {
			@Override
			public void onValueChange(ValueChangeEvent<Employee> event) {
				// редактировали, но нажали отмену - можно ничего не делать
				if (event.getValue() == null) {
					// Если создавали сотрудника, при отмене лучше вернуться к просмотру подразделения
					if (emplViewPanelActon == Constants.ACTION_ADD)
						cardLayout.setActiveWidget(podrViewPanel);
					return;
				}
				DeptTreeNode node;
				if (emplViewPanelActon == Constants.ACTION_UPDATE) {
					// редактировали и что-то изменили, нужно обновить фио на всякий случай
					node = dept("e_"+event.getValue().getId());
					node.setName(event.getValue().getName());
					organizationTree.refresh(node);
				} else {
					node = new DeptTreeNode("e_"+event.getValue().getId(), event.getValue().getName());
					node.setLeaf(true);
					DeptTreeNode parentNode = dept("d_"+event.getValue().getDepartmentId());
					deptTreeStore.add(parentNode, node);
				}
				pagingToolBar.refresh();
				organizationTree.getSelectionModel().select(node, false);
			}
		});
	    emplViewPanel.add(emplInfoPanel);
		
		cardLayout.add(podrViewPanel);
		cardLayout.add(emplViewPanel);
		cardLayout.setActiveWidget(podrViewPanel);
	    
	    emplPanel.setWestWidget(structPanel, structPanelData);
	    emplPanel.setCenterWidget(cardLayout, centerData);
	}

	private void addSubNodes(DeptTreeNode toNode) {
		for (Department dept: departments) {
			if (("d_"+dept.getParentId()).equals(toNode.getId())) {
				DeptTreeNode node = new DeptTreeNode("d_"+dept.getId(), dept.getName());
				node.setLeaf(false);
				deptTreeStore.add(toNode, node);
				addSubNodes(node);
			}
		}
	}
	
	private DeptTreeNode dept(String id) {
		List<DeptTreeNode> nodes = deptTreeStore.getAll();
		for (DeptTreeNode node: nodes) {
			if (node.getId().equals(id))
				return node;
		}
		return null;
	}
	
	private Employee getEmpl(int id) {
		for (Employee empl: employees) {
			if (empl.getId() == id)
				return empl;
		}
		return null;
	}
}
