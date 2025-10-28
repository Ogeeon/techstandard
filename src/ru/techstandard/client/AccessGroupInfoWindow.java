package ru.techstandard.client;

import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.shared.AccessGroupService;
import ru.techstandard.shared.AccessGroupServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;

public class AccessGroupInfoWindow extends Window {
	
	TextField groupId;
	TextField name;
	TextArea description;
	CheckBox canCreateTasks;
	CheckBox needTasksAprroved;
	CheckBox canApproveTasks;
	CheckBox canConfirmDeletes;
	List<Widget> validityChecks;
	
	Window theWindow;
	
	TextButton saveBtn;
	TextButton cancelBtn;
	
	private  boolean isModified = false;
	private int action=0;
	private int wndHeight = 315;
	private boolean canClose=false;
	
	AccessGroupServiceAsync accessGroupService = GWT.create(AccessGroupService.class);
	
	public AccessGroupInfoWindow() {
		super();
		this.setPixelSize(500, wndHeight);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Редактирование информации о группе доступа");
		
		theWindow = this;
		
		VerticalLayoutContainer accGrpInfoTopContainer = new VerticalLayoutContainer();
		this.setWidget(accGrpInfoTopContainer);
		
		final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};

		int labelColWidth = 150;
		int cbLabelColWidth = 200;
		groupId = new TextField();
		
		name = new TextField();
		name.setAllowBlank(false);
		FieldLabel nameLabel = new FieldLabel(name, "Название группы");
		nameLabel.setLabelWidth(labelColWidth);
		accGrpInfoTopContainer.add(nameLabel, new VerticalLayoutData(1, -1, new Margins(10, 5, 5, 5)));
		name.addKeyUpHandler(fieldKeyUpHandler);
		
		description = new TextArea();
		description.setAllowBlank(false);
		FieldLabel descrLabel = new FieldLabel(description, "Описание группы");
		descrLabel.setLabelWidth(labelColWidth);
		accGrpInfoTopContainer.add(descrLabel, new VerticalLayoutData(1, 70, new Margins(10, 5, 5, 5)));
		description.addKeyUpHandler(fieldKeyUpHandler);
		
		canCreateTasks = new CheckBox();
		FieldLabel creatorLabel = new FieldLabel(canCreateTasks, "Создание заданий");
		creatorLabel.setLabelWidth(cbLabelColWidth);
		accGrpInfoTopContainer.add(creatorLabel, new VerticalLayoutData(1, -1, new Margins(10, 5, 5, 5)));
		canCreateTasks.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				isModified = true;
				checkFormValidity();
			}
		});
		
		needTasksAprroved = new CheckBox();
		FieldLabel needApprovalLabel = new FieldLabel(needTasksAprroved, "Задания требуют согласования");
		needApprovalLabel.setLabelWidth(cbLabelColWidth);
		accGrpInfoTopContainer.add(needApprovalLabel, new VerticalLayoutData(1, -1, new Margins(10, 5, 5, 5)));
		needTasksAprroved.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				isModified = true;
				checkFormValidity();
			}
		});
		
		canApproveTasks = new CheckBox();
		FieldLabel approverLabel = new FieldLabel(canApproveTasks, "Согласование заданий");
		approverLabel.setLabelWidth(cbLabelColWidth);
		accGrpInfoTopContainer.add(approverLabel, new VerticalLayoutData(1, -1, new Margins(10, 5, 5, 5)));
		canApproveTasks.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				isModified = true;
				checkFormValidity();
			}
		});

		canConfirmDeletes = new CheckBox();
		FieldLabel deleteConfirmerLabel = new FieldLabel(canConfirmDeletes, "Утверждение удалений");
		deleteConfirmerLabel.setLabelWidth(cbLabelColWidth);
		accGrpInfoTopContainer.add(deleteConfirmerLabel, new VerticalLayoutData(1, -1, new Margins(10, 5, 5, 5)));
		canConfirmDeletes.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				isModified = true;
				checkFormValidity();
			}
		});
		
	    validityChecks = new ArrayList<Widget>();
	    validityChecks.add(name);
	    validityChecks.add(description);

	    this.setButtonAlign(BoxLayoutPack.END);
	    
	    saveBtn = new TextButton("Сохранить");
	    saveBtn.addSelectHandler(new SelectHandler() {
	    	@Override
	    	public void onSelect(SelectEvent event) {
	    		AccessGroup group = new AccessGroup(Integer.valueOf(groupId.getValue()), name.getValue());
	    		group.setDescription(description.getValue());
	    		group.setTaskCreator(canCreateTasks.getValue());
	    		group.setNeedApproval(needTasksAprroved.getValue());
	    		group.setTaskApprover(canApproveTasks.getValue());
	    		group.setDeleteConfirmer(canConfirmDeletes.getValue());
	    		// При добавлении пустая запись уже была создана, её нужно только обновить.
	    		accessGroupService.updateAccessGroup(group, true, new AsyncCallback<Boolean>() {
	    			@Override
	    			public void onSuccess(Boolean result) {
						action = -1; // для предотвращения удаления при выходе, если было Constants.ACTION_ADD
	    				canClose=true;
	    				theWindow.setData("hideButton", "save");
	    				theWindow.hide();						
	    			}
	    			@Override
	    			public void onFailure(Throwable caught) {
	    				AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить информацию о группе.");
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
	    this.setClosable(true);
	    this.setOnEsc(true);
	    
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
			accessGroupService.deleteAccessGroup(Integer.valueOf(groupId.getValue()), new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(Boolean result) {}
			});
		}
	}
	
	
	private void checkFormValidity() {
		boolean isFormValid = true;
		Widget w;
		for (int i=0; i < validityChecks.size(); i++) {
			w = validityChecks.get(i);
			if (((ValueBaseField<?>) w).getCurrentValue() == null) {
				((ValueBaseField<?>) w).forceInvalid("Поле не должно быть пустым");
				isFormValid = false;
			} else {
				((ValueBaseField<?>) w).clearInvalid();
			}
		}
		saveBtn.setEnabled(isModified && isFormValid);
	}
	
	private void fillWindowFields(AccessGroup group) {
		theWindow.setData("hideButton", null);
		groupId.setValue(String.valueOf(group.getId()));
		
		name.setValue(group.getName());
		description.setValue(group.getDescription());
		canCreateTasks.setValue(group.isTaskCreator());
		needTasksAprroved.setValue(group.isNeedApproval());
		canApproveTasks.setValue(group.isTaskApprover());
		canConfirmDeletes.setValue(group.isDeleteConfirmer());
		checkFormValidity();
	}

	public void editInfo(AccessGroup group, boolean newGroup) {
		action = newGroup ? Constants.ACTION_ADD : Constants.ACTION_UPDATE;
		canClose=false;
		
		fillWindowFields(group);

		isModified = false;
		saveBtn.setEnabled(false);
		theWindow.forceLayout();
		theWindow.show();
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	        @Override
	        public void execute() {
	        	cancelBtn.focus();
	        }
	    });
		theWindow.setPixelSize(500, wndHeight);
	}
}
