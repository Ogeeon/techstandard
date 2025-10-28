package ru.techstandard.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Images extends ClientBundle {
	 public Images INSTANCE = GWT.create(Images.class);

	 @Source("images/logo.jpg")
	 ImageResource logo();
	 
	 @Source("images/details.png")
	 ImageResource view();
	  
	  @Source("images/information.png")
	  ImageResource information();
	  
	  @Source("images/warning.png")
	  ImageResource warning();
	  
	  @Source("images/cross_flat.png")
	  ImageResource deleteRow();
	  
	  @Source("images/plus_flat.png")
	  ImageResource addRow();
	  
	  @Source("images/edit.png")
	  ImageResource editRow();
	  
	  @Source("images/edit.png")
	  ImageResource edit();
	  
	  @Source("images/plus_flat.png")
	  ImageResource add();
	  
	  @Source("images/plus-3.png")
	  ImageResource addRed();
	  
	  @Source("images/cross_flat.png")
	  ImageResource delete();
	  
	  @Source("images/binders.png")
	  ImageResource documents();
	  
	  @Source("images/upload.png")
	  ImageResource upload();
	  
	  @Source("images/download.png")
	  ImageResource download();
	  
	  @Source("images/print.png")
	  ImageResource print();
	  
	  @Source("images/checked.png")
	  ImageResource checked();
	  
	  @Source("images/undo.png")
	  ImageResource undo();
	  
	  @Source("images/undo-2.png")
	  ImageResource undo2();
	  
	  @Source("images/apply.png")
	  ImageResource apply();
	  
	  @Source("images/apply2.png")
	  ImageResource apply2();
	  
	  @Source("images/leftarrow.png")
	  ImageResource left();
	  
	  @Source("images/rightarrow.png")
	  ImageResource right();
	  
	  @Source("images/conference-call.png")
	  ImageResource dept();
	  
	  @Source("images/conference-call-open.png")
	  ImageResource dept_open();
	  
	  @Source("images/user.png")
	  ImageResource empl();
	  
	  @Source("images/rename.png")
	  ImageResource rename();
	  
	  @Source("images/signature.png")
	  ImageResource signature();
	  
	  @Source("images/trash.png")
	  ImageResource trash();
	  
	  @Source("images/inbox.png")
	  ImageResource inbox();
	  
	  @Source("images/refresh.png")
	  ImageResource refresh();
}
