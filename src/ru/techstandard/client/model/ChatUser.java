package ru.techstandard.client.model;

import java.io.Serializable;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public class ChatUser implements Serializable {
	private static final long serialVersionUID = 1L;
		private boolean active=true; // reserve
		private String name="";
		public ChatUser(){}
		public ChatUser(String name) {
			this.setName(name);
		}
		public ChatUser(boolean active, String name) {
			this.setActive(active);
			this.setName(name);
		}
		public boolean isActive() {
			return active;
		}
		public void setActive(boolean state) {
			this.active = state;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	
	public interface ChatUserProps extends PropertyAccess<ChatUser> {
		@Path("name")
		LabelProvider<ChatUser> nameLabel();
		ValueProvider<ChatUser, String> name();
		ValueProvider<ChatUser, Boolean> active();
	}
}
