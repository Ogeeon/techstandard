package ru.techstandard.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.DefaultBroadcasterFactory;
import org.atmosphere.cpr.Serializer;
import org.atmosphere.gwt20.jackson.JacksonSerializerProvider;
import org.atmosphere.gwt20.server.SerializationException;
import org.atmosphere.gwt20.server.ServerSerializer;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;


public class ChatService extends AbstractReflectorAtmosphereHandler {
	ChatLoggerServiceImpl chatLoggerService = new ChatLoggerServiceImpl();
	
    @Override
    public void onRequest(AtmosphereResource ar) throws IOException {
      if (ar.getRequest().getMethod().equals("GET") ) {
        doGet(ar);
      } else if (ar.getRequest().getMethod().equals("POST") ) {
        doPost(ar);
      }
    }
    
    private ServerSerializer serializer = new JacksonSerializerProvider().getServerSerializer();
    
    @SuppressWarnings("deprecation")
	public void doGet(final AtmosphereResource ar) {
        
        ar.getResponse().setCharacterEncoding(ar.getRequest().getCharacterEncoding());
        ar.getResponse().setContentType("application/json");
        
        // lookup the broadcaster, if not found create it. Name is arbitrary
//        System.out.println("setting up broadcaster for client with header" + ar.getRequest().getHeader("username"));
        Broadcaster brc = DefaultBroadcasterFactory.getDefault().lookup("MyBroadcaster", true);
        ar.setBroadcaster(brc);
        
        ar.setSerializer(new Serializer() {
            Charset charset = Charset.forName(ar.getResponse().getCharacterEncoding());
            @Override
            public void write(OutputStream os, Object o) throws IOException {
                try {
                    String payload = serializer.serialize(o);
                    os.write(payload.getBytes(charset));
                    os.flush();
                } catch (SerializationException ex) {
                    throw new IOException("Failed to serialize object to JSON", ex);
                }
            }
        });
        
        ar.suspend();
    }
    
    @SuppressWarnings({ "unchecked", "deprecation" })
	public void doPost(AtmosphereResource ar) throws IOException {
        StringBuilder data = new StringBuilder();
        BufferedReader requestReader;
        try {
            requestReader = ar.getRequest().getReader();
            ar.getRequest().getHeader("s");
            char[] buf = new char[5120];
            int read = -1;
            while ((read = requestReader.read(buf)) > 0) {
              data.append(buf, 0, read);
            }
        
            Object message = serializer.deserialize(data.toString());
            
            Broadcaster brc = DefaultBroadcasterFactory.getDefault().lookup("MyBroadcaster");
            if (brc != null) {
            	Collection<AtmosphereResource> resources = brc.getAtmosphereResources();
            	List<String> users = new ArrayList<String>();
            	for (AtmosphereResource element :resources) {
            		users.add(URLDecoder.decode(element.getRequest().getHeader("username"), "UTF-8"));
            	}
            	LinkedHashMap<String, Object> msg = (LinkedHashMap<String, Object>) message;
            	msg.put("loggedUsers", users);

            	brc.broadcast(message);
            	
            	Boolean isSysMsg = msg.get("systemMessage")==null?false:((Boolean) msg.get("systemMessage"));
            	if (!isSysMsg)
            		chatLoggerService.saveLog(msg);
            }
            
        } catch (SerializationException ex) {
        }
        
    }
}
