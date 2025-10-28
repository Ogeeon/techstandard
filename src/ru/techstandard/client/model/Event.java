package ru.techstandard.client.model;

import java.io.Serializable;
import java.util.Date;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public class Event implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public interface EventProps extends PropertyAccess<Event> {
		ModelKeyProvider<Event> id();

		ValueProvider<Event, String> title();
		ValueProvider<Event, Date> created();
		ValueProvider<Event, String> description();
	}

	private int id;
	private Date created;
	private int recepientId;
	private String title;
	private String description;
	
	public Event() {}
	
	public Event(Date created, int recepientId, String title, String description) {
		this.created = created;
		this.setRecepientId(recepientId);
		this.setTitle(title);
		this.setDescription(description);
	}
	
	public Event(int id, Date created, int recepientId, String title, String description) {
		this.id = id;
		this.created = created;
		this.setRecepientId(recepientId);
		this.setTitle(title);
		this.setDescription(description);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getRecepientId() {
		return recepientId;
	}

	public void setRecepientId(int recepientId) {
		this.recepientId = recepientId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		return "["+id+"] "+title+": "+description;
	}
}
