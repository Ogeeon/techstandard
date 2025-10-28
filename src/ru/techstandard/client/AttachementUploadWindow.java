package ru.techstandard.client;

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.IUploader;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader.OnChangeUploaderHandler;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.Utils;
import gwtupload.client.SingleUploader;
import gwtupload.client.Uploader.FormFlowPanel;

import java.util.ArrayList;
import java.util.List;

import ru.techstandard.client.model.Attachement;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;
import ru.techstandard.shared.AttachementService;
import ru.techstandard.shared.AttachementServiceAsync;
import ru.techstandard.shared.DictionaryService;
import ru.techstandard.shared.DictionaryServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;

public class AttachementUploadWindow extends Window {
	private final DictionaryServiceAsync dictionaryService = GWT.create(DictionaryService.class);
	private final AttachementServiceAsync attachementService = GWT.create(AttachementService.class);
	
	TextButton uploadBtn;
	TextButton cancelBtn;
	Window theWindow;
	ComboBox<DictionaryRecord> attachType;
	TextField formAttachId;
	TextField formParentId;
	TextField formParentType;
	TextField attachTitle;
	TextField attachedFileName;
	CardLayoutContainer attachmentCardContainer;
	List<Widget> validityChecks;
	
	final SingleUploader uploader;
	SimpleContainer uploaderPanel;
	
	List<Integer> attachmentList;
	private int action;
	boolean isModified;
	
	public AttachementUploadWindow() {
		super();
		this.setPixelSize(600, 400);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Загрузка документов по контрагенту");
		
		theWindow = this;
		int labelColWidth = 180;
		isModified = false;

		FormFlowPanel form = new FormFlowPanel();
		 
		formAttachId = new TextField();
		formAttachId.setValue("0");
	    formAttachId.setName("attachId");
	    formAttachId.hide();
		form.add(formAttachId);
		
		formParentId = new TextField();
		formParentId.setName("parentId");
		formParentId.hide();
		form.add(formParentId);
		
		formParentType = new TextField();
		formParentType.setName("parentType");
		formParentType.hide();
		form.add(formParentType);
		
		final TextField formAttachTypeId = new TextField(); 
	    formAttachTypeId.setName("attachTypeId");
	    formAttachTypeId.hide();
		form.add(formAttachTypeId);
		
		final TextField formAttachTitle = new TextField(); 
		formAttachTitle.setName("attachTitle");
		formAttachTitle.hide();
		form.add(formAttachTitle);
		
		ContentPanel toplevelPanel = new FramedPanel();

		toplevelPanel.setHeaderVisible(false);
		VerticalLayoutContainer uploadFormContainer = new VerticalLayoutContainer();
		toplevelPanel.add(uploadFormContainer);
		
		DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> attachTypesStore = new ListStore<DictionaryRecord>(props.key());
	    
	    dictionaryService.getDictionaryContents(Constants.DICT_ATTACHTYPES,
				new AsyncCallback<List<DictionaryRecord>>() {
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены. AttachUW:135 - "+caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(List<DictionaryRecord> result) {
						attachTypesStore.addAll(result);
					}
				});
	    
	    final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				isModified = true;
				checkFormValidity();
			}
		};
		
	    attachType = new ComboBox<DictionaryRecord>(attachTypesStore, props.nameLabel());
	    attachType.setAllowBlank(false);
	    attachType.setForceSelection(true);
	    attachType.setTriggerAction(TriggerAction.ALL);
	    attachType.addKeyUpHandler(fieldKeyUpHandler);
	    
	    TextButton addAttachTypeBtn = new TextButton("");
	    addAttachTypeBtn.setIcon(Images.INSTANCE.add());
	    addAttachTypeBtn.setToolTip("Добавить тип документа");
	    addAttachTypeBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final PromptMessageBox box = new PromptMessageBox("Тип документа", "Введите новое значение для справочника<br>типов документов:");
	          box.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	          box.getField().focus();
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
	            	if (event.getHideButton() == PredefinedButton.CANCEL) {
						return;
					}
	            	if (box.getValue() == null || box.getValue().trim().isEmpty()) {
	            		MessageBox d = new MessageBox("Недостаточно данных","Пожалуйста, введите краткую формулировку названия типа документа.");
	            		d.setIcon(Images.INSTANCE.information());
	            		d.show();
						return;
	            	}
	            	dictionaryService.addRecord(Constants.DICT_ATTACHTYPES, box.getValue().trim(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getMessage());
							d.show();
						}
						@Override
						public void onSuccess(Integer result) {
							DictionaryRecord rec = new DictionaryRecord(result, box.getValue().trim());
							attachTypesStore.add(rec);
							attachType.setValue(rec);
							formAttachTypeId.setValue(String.valueOf(rec.getId()));
						}
					});
	            }
	          });
	          box.show();
	        }
	      });
	    
	    HorizontalLayoutContainer attachTypeHLC = new HorizontalLayoutContainer();
	    attachTypeHLC.add(attachType, new HorizontalLayoutData(1, 1));
	    attachTypeHLC.add(addAttachTypeBtn, new HorizontalLayoutData(-1, -1));
	    
		FieldLabel attachTypeLabel = new FieldLabel(attachTypeHLC, "Тип документа");
		attachTypeLabel.setLabelWidth(labelColWidth);
		attachTypeLabel.setHeight(23);
		uploadFormContainer.add(attachTypeLabel, new VerticalLayoutData(1, -1));
		attachType.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				int id = event.getSelectedItem().getId();
				formAttachTypeId.setValue(String.valueOf(id));
				isModified = true;
				checkFormValidity();
			}
		});

		attachTitle = new TextField();
		attachTitle.setAllowBlank(false);
		attachTitle.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel workNumLabel = new FieldLabel(attachTitle, "Описание документа");
		workNumLabel.setLabelWidth(labelColWidth);
		uploadFormContainer.add(workNumLabel, new VerticalLayoutData(1, -1));
		
		attachmentCardContainer = new CardLayoutContainer();
		attachedFileName = new TextField();
		attachedFileName.setValue("filename.ext");
		attachedFileName.setEnabled(false);
		TextButton deleteAttachmentBtn = new TextButton("");
		deleteAttachmentBtn.setIcon(Images.INSTANCE.delete());
		deleteAttachmentBtn.setToolTip("Удалить документ");
		deleteAttachmentBtn.addSelectHandler(new SelectHandler() {
	        @Override
	        public void onSelect(SelectEvent event) {
	          final ConfirmMessageBox box = new ConfirmMessageBox("Удаление файла", "Вы уверены, что хотите удалить приложенный файл?");
	          box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
	          box.addDialogHideHandler(new DialogHideHandler() {
	            @Override
	            public void onDialogHide(DialogHideEvent event) {
	            	if (event.getHideButton() == PredefinedButton.YES) {
	            		attachmentCardContainer.setActiveWidget(uploaderPanel);
	            		uploadBtn.setText("Закачать");
	            		uploadBtn.setEnabled(false);
	            		action = Constants.ACTION_ADD;
	            	}
	            }
	          });
	          box.show();
	        }
	      });
		
		HorizontalLayoutContainer attachmentNameHLC = new HorizontalLayoutContainer();
	    attachmentNameHLC.add(attachedFileName, new HorizontalLayoutData(1, 1));
	    attachmentNameHLC.add(deleteAttachmentBtn, new HorizontalLayoutData(-1, -1));
	    attachmentNameHLC.setItemId("attachmentNameHLC");
		
		uploaderPanel = new SimpleContainer();
		HorizontalLayoutContainer vlc = new HorizontalLayoutContainer();
		uploaderPanel.setWidget(vlc);
		uploaderPanel.setItemId("uploaderPanel");
		
		uploader = new SingleUploader(FileInputType.BROWSER_INPUT, new BaseUploadStatus(), uploadBtn, form);
		uploader.setMultipleSelection(false);
		uploader.setAvoidRepeatFiles(false);
		
		uploader.setServletPath("uploader.fileUpload");
		uploader.addOnChangeUploadHandler(new OnChangeUploaderHandler() {
			@Override
			public void onChange(IUploader uploader) {
				isModified = true;
				checkFormValidity();
			}
		});
		
		uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
			public void onFinish(IUploader uploader) {
 
				if (uploader.getStatus() == Status.SUCCESS) {
 
					String response = uploader.getServerRawResponse();
 
					if (response != null) {
						Document doc = XMLParser.parse(response);
						String message = Utils.getXmlNodeValue(doc, "message");
						// добавляем вложение в список только если это было новое вложение
						if (formAttachId.getValue().equals("0"))
							attachmentList.add(Integer.valueOf(message));
						uploader.reset();
						theWindow.setData("hideButton", "save");
						theWindow.hide();
						
					} else {
						new AlertMessageBox("Exception", "Unaccessible server response").show();
					}
				} else {
					new AlertMessageBox("Exception", "Uploader Status: \n" + uploader.getStatus()).show();
				}
				
				uploadBtn.setEnabled(false);
			}
		});
		vlc.add(uploader);
		
		attachmentCardContainer.add(attachmentNameHLC);
		attachmentCardContainer.add(uploaderPanel);
		attachmentCardContainer.setActiveWidget(uploaderPanel);
		
		FieldLabel documentLabel = new FieldLabel(attachmentCardContainer, "Документ");
		documentLabel.setLabelWidth(labelColWidth);
		
		uploadFormContainer.add(documentLabel, new VerticalLayoutData(1, -1));
		
		this.add(toplevelPanel);
		
		validityChecks = new ArrayList<Widget>();
	    validityChecks.add(attachTitle);
	    validityChecks.add(attachType);
		
		uploadBtn = new TextButton("Закачать");
		uploadBtn.setEnabled(false);
		this.addButton(uploadBtn);
		uploadBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				if (action == Constants.ACTION_ADD) {
					if (!formAttachId.getValue().equals("0")) {
						// Закачивается новый файл: нужно удалить старый, и потом прописать информацию о новом
						attachementService.deleteFile(Integer.valueOf(formAttachId.getValue()), new AsyncCallback<Void>() {
							@Override public void onSuccess(Void result) {}
							@Override public void onFailure(Throwable caught) {}
						});
					}
					formAttachTitle.setValue(attachTitle.getValue());
					uploadBtn.setEnabled(false);
					uploader.submit();
				} else if (action == Constants.ACTION_UPDATE) {
					Attachement att = new Attachement(Integer.valueOf(formAttachId.getValue()));
					att.setParentIdStr(formParentId.getCurrentValue());
					att.setParentTypeStr(formParentType.getCurrentValue());
					att.setTitle(attachTitle.getCurrentValue());
					att.setAttachType(attachType.getCurrentValue().getId());
					attachementService.updateAttachment(att, new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							AlertMessageBox d = new AlertMessageBox("Ошибка", "Не удалось сохранить данные.<br>"+caught.getMessage());
							d.show();
						}
						@Override
						public void onSuccess(Boolean result) {
							theWindow.setData("hideButton", "save");
							theWindow.hide();
						}
					});
				}
				
			}
		});
		
		cancelBtn = new TextButton("Отмена");
		this.addButton(cancelBtn);
		cancelBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				theWindow.hide();
			}
		});
	}
	
	public void addAttachment(int parentId, int parentType, List<Integer> attachments) {
		isModified = false;
		action = Constants.ACTION_ADD;
		attachmentList = attachments;
		formAttachId.setValue("0");
		formParentId.setValue(String.valueOf(parentId));
		formParentType.setValue(String.valueOf(parentType));
		attachmentCardContainer.setActiveWidget(attachmentCardContainer.getItemByItemId("uploaderPanel"));
		String what = "";
		switch (parentType) {
			case Constants.ACT_ATTACHMENTS: what = ""; break;
			case Constants.TASK_ATTACHMENTS: what = ""; break;
			case Constants.DEVICE_ATTACHMENTS: what = "устройству"; break;
			case Constants.CONTRACT_ATTACHMENTS: what = "договору"; break;
		}
		this.setHeadingText("Загрузка документов по "+what);
		attachType.clear();
		attachTitle.setValue(null);
		attachmentCardContainer.setActiveWidget(uploaderPanel);
		uploader.reset();
		uploadBtn.setText("Закачать");
		uploadBtn.setEnabled(false);
		this.show();
	}
	
	public void editAttachment(Attachement att) {
		isModified = false;
		action = Constants.ACTION_UPDATE;
		formAttachId.setValue(String.valueOf(att.getId()));
		formParentId.setValue(att.getParentIdStr());
		formParentType.setValue(att.getParentTypeStr());
		attachmentCardContainer.setActiveWidget(attachmentCardContainer.getItemByItemId("attachmentNameHLC"));
		String what = "";
		switch (att.getParentType()) {
			case Constants.ACT_ATTACHMENTS: what = ""; break;
			case Constants.TASK_ATTACHMENTS: what = ""; break;
			case Constants.DEVICE_ATTACHMENTS: what = "устройству"; break;
			case Constants.CONTRACT_ATTACHMENTS: what = "договору"; break;
		}
		this.setHeadingText("Редактирование документов по "+what);
		
		List<DictionaryRecord> types = attachType.getStore().getAll();
		attachType.clear();
		for (int i=0; i<types.size(); i++) {
			if(types.get(i).getId() == att.getAttachType()) {
				attachType.setValue(types.get(i));
				break;
			}
		}
		attachTitle.setValue(att.getTitle());
		if (att.getFilename().isEmpty())
			attachmentCardContainer.setActiveWidget(uploaderPanel);
		else
			attachedFileName.setValue(att.getFilename());
		
		uploadBtn.setText("Сохранить");
		uploadBtn.setEnabled(false);
		this.show();
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
		if (action == Constants.ACTION_ADD) {
			isFormValid = isFormValid && (!uploader.getFileName().isEmpty());
		}
		
		uploadBtn.setEnabled(isModified && isFormValid);
	}
}
