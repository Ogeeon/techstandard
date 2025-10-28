package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.techstandard.client.model.Client;
import ru.techstandard.client.model.ClientProps;
import ru.techstandard.client.model.TemplateContract;
import ru.techstandard.shared.ClientService;
import ru.techstandard.shared.ClientServiceAsync;
import ru.techstandard.shared.ContractService;
import ru.techstandard.shared.ContractServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent.ParseErrorHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent.TriggerClickHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FormPanel.LabelAlign;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.SpinnerField;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class TemplateContractWindow extends Window {

	ComboBox<Client> client;
	TextField contractNum;
	DateField signedDate;
	TextField signer;
	TextField foundation;
	TextArea subject;
	SpinnerField<Integer> duration;
	SpinnerField<Integer> prePay;
	CheckBox multipleItems;
	TextField unitName;
	NumberField<Double> unitPrice;
	NumberField<Double> totalPrice;
	DateField expiryDate;
	List<Widget> validityChecks;

	Window theWindow;
	ContentPanel infoPanel;
	ContentPanel contractDocsPanel;
	ToolBar topToolBar;
	ToggleButton toggleEditModeBtn;
	TextButton printContratBtn;

	TextButton printBtn;
	TextButton cancelBtn;

	private final ClientServiceAsync clientService = GWT.create(ClientService.class);
	private final ContractServiceAsync contractService = GWT.create(ContractService.class);

	public TemplateContractWindow() {
		super();
		this.setPixelSize(500, 600);
		this.setModal(true);
		this.setBlinkModal(true);
		this.setResizable(false);
		this.setHeadingText("Печать типового договора");

		theWindow = this;

		VerticalLayoutContainer contractInfoContainer = new VerticalLayoutContainer();
		this.setWidget(contractInfoContainer);



		final ChangeHandler formChangedHandler = new ChangeHandler()  {
			@Override
			public void onChange(ChangeEvent event) {
				checkFormValidity();
			}
		};

		final KeyUpHandler fieldKeyUpHandler = new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				checkFormValidity();
			}
		};

		ClientProps clientProps = GWT.create(ClientProps.class);
		final ListStore<Client> clientStore = new ListStore<Client>(clientProps.id());

		client = new ComboBox<Client>(clientStore, clientProps.nameLabel());
		client.addValueChangeHandler(new ValueChangeHandler<Client>() {
			@Override
			public void onValueChange(ValueChangeEvent<Client> event) {
				checkFormValidity();
			}
		});
		client.addSelectionHandler(new SelectionHandler<Client>() {
			@Override
			public void onSelection(SelectionEvent<Client> event) {
				checkFormValidity();
			}
		});
		client.addKeyUpHandler(fieldKeyUpHandler);
		client.setAllowBlank(true);
		client.setForceSelection(true);
		client.setTriggerAction(TriggerAction.ALL);
		client.setMinListWidth(360);
		FieldLabel clientLabel = new FieldLabel(client, "Контрагент");
		clientLabel.setLabelWidth(100);
		contractInfoContainer.add(clientLabel, new VerticalLayoutData(1, -1, new Margins(10, 20, 10, 5)));

		contractNum = new TextField();
		contractNum.setAllowBlank(false);
		contractNum.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel numLabel = new FieldLabel(contractNum, "Номер договора");

		signedDate = new DateField();
		signedDate.setAllowBlank(false);
		signedDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
		signedDate.addParseErrorHandler(new ParseErrorHandler() {
			@Override
			public void onParseError(ParseErrorEvent event) {
				Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
			}
		});

		signedDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				checkFormValidity();
			}
		});
		signedDate.addKeyUpHandler(fieldKeyUpHandler);
		signedDate.addTriggerClickHandler(new TriggerClickHandler() {
			@Override
			public void onTriggerClick(TriggerClickEvent event) {
				checkFormValidity();
			}
		});
		FieldLabel signedDateLabel = new FieldLabel(signedDate, "Дата заключения");
		signedDateLabel.setLabelWidth(120);
		
		HorizontalLayoutContainer numDateHLC = new HorizontalLayoutContainer();
		numDateHLC.add(numLabel, new HorizontalLayoutData(190, 1, new Margins(0, 20, 0, 5)));
		numDateHLC.add(signedDateLabel, new HorizontalLayoutData(250, 1));
		contractInfoContainer.add(numDateHLC, new VerticalLayoutData(1, 25));
		
		signer = new TextField();
		signer.setAllowBlank(false);
		signer.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel signerLabel = new FieldLabel(signer, "Заказчик в лице");
		signerLabel.setLabelWidth(170);
		contractInfoContainer.add(signerLabel, new VerticalLayoutData(1, -1, new Margins(10, 20, 10, 5)));
		
		foundation = new TextField();
		foundation.setAllowBlank(false);
		foundation.addKeyUpHandler(fieldKeyUpHandler);
		FieldLabel foundationLabel = new FieldLabel(foundation, "Действующий на основании");
		foundationLabel.setLabelWidth(170);
		contractInfoContainer.add(foundationLabel, new VerticalLayoutData(1, -1, new Margins(0, 20, 10, 5)));

		subject = new TextArea();
		subject.setAllowBlank(true);
		FieldLabel subjectLabel = new FieldLabel(subject, "Предмет договора");
		subjectLabel.setLabelAlign(LabelAlign.TOP);
		contractInfoContainer.add(subjectLabel, new VerticalLayoutData(1, 110, new Margins(0, 20, 20, 5)));
		subject.addChangeHandler(formChangedHandler);

		duration = new SpinnerField<Integer>(new IntegerPropertyEditor());
		duration.setMinValue(1);
		duration.setAllowNegative(false);
		duration.setAllowBlank(false);
		duration.addKeyUpHandler(new KeyUpHandler() { public void onKeyUp(KeyUpEvent event) { checkFormValidity(); } });
		duration.addValueChangeHandler(new ValueChangeHandler<Integer>() { @Override public void onValueChange(ValueChangeEvent<Integer> event) { checkFormValidity(); } });
		duration.setValue(30);
		FieldLabel durationLabel = new FieldLabel(duration, "Срок выполнения работ, дней");
		durationLabel.setLabelWidth(200);
		contractInfoContainer.add(durationLabel, new VerticalLayoutData(1, -1, new Margins(0, 20, 15, 10)));
		
		prePay = new SpinnerField<Integer>(new IntegerPropertyEditor());
		prePay.setMinValue(0);
		prePay.setAllowNegative(false);
		prePay.setAllowBlank(false);
		prePay.addKeyUpHandler(new KeyUpHandler() { public void onKeyUp(KeyUpEvent event) { checkFormValidity(); } });
		prePay.addValueChangeHandler(new ValueChangeHandler<Integer>() { @Override public void onValueChange(ValueChangeEvent<Integer> event) { checkFormValidity(); } });
		prePay.setValue(50);
		FieldLabel prePayLabel = new FieldLabel(prePay, "Аванс, %");
		prePayLabel.setLabelWidth(75);
		contractInfoContainer.add(prePayLabel, new VerticalLayoutData(1, -1));
		
		HorizontalLayoutContainer durPrepHLC = new HorizontalLayoutContainer();
		durPrepHLC.add(durationLabel, new HorizontalLayoutData(250, -1, new Margins(0, 25, 0, 5)));
		durPrepHLC.add(prePayLabel, new HorizontalLayoutData(150, 1));
		contractInfoContainer.add(durPrepHLC, new VerticalLayoutData(1, 25));
		
		expiryDate = new DateField();
		expiryDate.setAllowBlank(false);
		expiryDate.setPropertyEditor(new DateTimePropertyEditor("dd.MM.yyyy"));
		expiryDate.addParseErrorHandler(new ParseErrorHandler() {
			@Override
			public void onParseError(ParseErrorEvent event) {
				Info.display("Неверный формат", event.getErrorValue() + " не является датой в допустимом формате.");
			}
		});

		FieldLabel expiryDateLabel = new FieldLabel(expiryDate, "Срок оплаты");
		expiryDateLabel.setLabelWidth(200);
		contractInfoContainer.add(expiryDateLabel, new VerticalLayoutData(1, -1, new Margins(10, 20, 10, 5)));
		expiryDate.addValueChangeHandler(new ValueChangeHandler<Date>() {	 
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				checkFormValidity();
			}
		});
		expiryDate.addKeyUpHandler(fieldKeyUpHandler);
		
		multipleItems = new CheckBox();
		FieldLabel multItmsLabel = new FieldLabel(multipleItems, "Требуется указание цены за единицу");
		multItmsLabel.setLabelWidth(220);
		multipleItems.setValue(true);
		contractInfoContainer.add(multItmsLabel, new VerticalLayoutData(1, -1, new Margins(0, 20, 10, 5)));
		multipleItems.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue()) {
					unitName.setEnabled(true);
					unitPrice.setEnabled(true);
				} else {
					unitName.setValue(null);
					unitName.setEnabled(false);
					unitName.clearInvalid();
					unitPrice.setValue(null);
					unitPrice.setEnabled(false);
					unitPrice.clearInvalid();
				}
				checkFormValidity();
			}
		});

		unitName = new TextField();
		unitName.setAllowBlank(false);
		unitName.addKeyUpHandler(fieldKeyUpHandler);
		unitName.setWidth(205);
		FieldLabel unitNameLabel = new FieldLabel(unitName, "Стоимость работ за 1 единицу (чего)");
		unitNameLabel.setLabelWidth(250);
		contractInfoContainer.add(unitNameLabel, new VerticalLayoutData(1, -1, new Margins(0, 20, 10, 5)));
		
		NumberFormat fmt = NumberFormat.getDecimalFormat();
        fmt.overrideFractionDigits(2, 2);
        
		unitPrice = new NumberField<Double>(new NumberPropertyEditor.DoublePropertyEditor());
		unitPrice.setAllowBlank(false);
		unitPrice.addKeyUpHandler(fieldKeyUpHandler);
		unitPrice.setFormat(fmt);
		unitPrice.setWidth(255);
		FieldLabel unitPriceLabel = new FieldLabel(unitPrice, "Составляет");
		unitPriceLabel.setLabelWidth(200);
//		unitPriceLabel.setLabelAlign(LabelAlign.TOP);
		contractInfoContainer.add(unitPriceLabel, new VerticalLayoutData(1, -1, new Margins(0, 20, 10, 5)));
		
		totalPrice = new NumberField<Double>(new NumberPropertyEditor.DoublePropertyEditor());
		totalPrice.setAllowBlank(false);
		totalPrice.addKeyUpHandler(fieldKeyUpHandler);
		totalPrice.setFormat(fmt);
		FieldLabel totalPriceLabel = new FieldLabel(totalPrice, "Стоимость работ итого");
		totalPriceLabel.setLabelWidth(200);
//		totalPriceLabel.setLabelAlign(LabelAlign.TOP);
		contractInfoContainer.add(totalPriceLabel, new VerticalLayoutData(1, -1, new Margins(0, 20, 10, 5)));

		validityChecks = new ArrayList<Widget>();
		validityChecks.add(client);
		validityChecks.add(contractNum);
		validityChecks.add(signedDate);
		validityChecks.add(signer);
		validityChecks.add(foundation);
		validityChecks.add(subject);
		validityChecks.add(duration);
		validityChecks.add(prePay);
		validityChecks.add(unitName);
		validityChecks.add(unitPrice);
		validityChecks.add(totalPrice);
		validityChecks.add(expiryDate);


		this.setButtonAlign(BoxLayoutPack.END);

		printBtn = new TextButton("Сформировать");
		printBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				TemplateContract tc = new TemplateContract();
				tc.setClientId(client.getValue().getId());
				tc.setNum(contractNum.getValue());
				tc.setSigned(signedDate.getValue());
				tc.setSigner(signer.getValue());
				tc.setFoundation(foundation.getValue());
				tc.setSubject(subject.getValue());
				tc.setDuration(duration.getValue());
				tc.setMultipleItems(multipleItems.getValue());
				tc.setPrePay(prePay.getValue());
				tc.setUnitName(unitName.getValue());
				tc.setUnitPrice(unitPrice.getValue());
				tc.setTotalPrice(totalPrice.getValue());
				tc.setDueDate(expiryDate.getValue());
				
				
				contractService.storeTemplate(tc, new AsyncCallback<Integer>() {
					@Override
					public void onFailure(Throwable caught) {
						AlertMessageBox d = new AlertMessageBox("Ошибка", caught.getMessage());
						d.show();
					}
					@Override
					public void onSuccess(Integer result) {
						String url = GWT.getModuleBaseURL() + "downloadService?type=contract&id="+result;
						com.google.gwt.user.client.Window.open(url, "_self", "");
						
						theWindow.hide();
					}
				});
			}
		});
		this.addButton(printBtn);
		this.setData("saveBtn", printBtn);

		cancelBtn = new TextButton("Отмена");
		cancelBtn.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				theWindow.hide();
			}
		});
		this.addButton(cancelBtn);
		this.setFocusWidget(this.getButtonBar().getWidget(0));
		this.setClosable(true);
		this.setOnEsc(true);
	}

	private void checkFormValidity() {
		boolean isFormValid = true;
		Widget w;
		for (int i=0; i < validityChecks.size(); i++) {
			w = validityChecks.get(i);
			// пропускаем проверку полей, которые не задействованы, если стоимость работ по договору - одной суммой
			if (!multipleItems.getValue()) {
				if (w == unitPrice || w == unitName) {
					((ValueBaseField<?>) w).clearInvalid();
					continue;
				}
			}
			if (((ValueBaseField<?>) w).getCurrentValue() == null) {
				((ValueBaseField<?>) w).forceInvalid("Поле не должно быть пустым");
				isFormValid = false;
			} else {
				((ValueBaseField<?>) w).clearInvalid();
			}
		}
		printBtn.setEnabled(isFormValid);
	}

	public void displayWindow() {
		client.setValue(null);
		clientService.getAllClients(new AsyncCallback<List<Client>>() {
			public void onFailure(Throwable caught) {
				AlertMessageBox d = new AlertMessageBox("Ошибка", "Данные не получены.<br>"+caught.getMessage());
				d.show();
			}
			@Override
			public void onSuccess(List<Client> result) {
				client.getStore().replaceAll(result);
			}
		});
		
		contractNum.setValue(null);
		signedDate.setValue(null);
		signer.setValue(null);
		foundation.setValue(null);
		subject.setValue(null);
		duration.setValue(30);
		prePay.setValue(50);
		multipleItems.setValue(true);
		unitName.setValue(null);
		unitPrice.setValue(null);
		totalPrice.setValue(null);
		expiryDate.setValue(null);
		checkFormValidity();

		theWindow.show();
		theWindow.setPixelSize(500, 510);
		theWindow.forceLayout();
	}
}
