package ru.techstandard.client;

import ru.techstandard.client.model.ChatMessage;
import ru.techstandard.client.model.Notification;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

public interface MyBeanFactory extends AutoBeanFactory {
    AutoBean<ChatMessage> event(ChatMessage event);
    AutoBean<Notification> notif(Notification notif);
}
