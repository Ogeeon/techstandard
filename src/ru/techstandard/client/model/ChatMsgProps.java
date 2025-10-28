package ru.techstandard.client.model;

import java.util.Date;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface ChatMsgProps extends PropertyAccess<ChatMsg> {
		ModelKeyProvider<ChatMsg> id();
		
		ValueProvider<ChatMsg, String> author();
		ValueProvider<ChatMsg, String> room();
		ValueProvider<ChatMsg, String> message();
		ValueProvider<ChatMsg, Date> date();
	}