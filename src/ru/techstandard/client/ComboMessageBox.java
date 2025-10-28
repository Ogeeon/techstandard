package ru.techstandard.client;

import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ComponentHelper;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;

public class ComboMessageBox<T>  extends MessageBox {
	private ComboBox<T> field;
	
	public ComboMessageBox(String title, String message, ListStore<T> store, LabelProvider<? super T> labelProvider) {
		super(title, message);
		
		field = new ComboBox<T>(store, labelProvider);
		
		ComponentHelper.setParent(this, field);

	    setFocusWidget(field);

	    contentAppearance.getContentElement(getElement()).appendChild(field.getElement());

		field.setWidth(347);
	    setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
	}

	public  ComboBox<T> getField() {
		return field;
	}
	
	public T getValue() {
		return field.getValue();
	}

	@Override
	protected void doAttachChildren() {
		super.doAttachChildren();
		ComponentHelper.doAttach(field);
	}

	@Override
	protected void doDetachChildren() {
		super.doDetachChildren();
		ComponentHelper.doDetach(field);
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		field.setWidth(getContainerTarget().getWidth(true));
	}
}
