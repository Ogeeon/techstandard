package ru.techstandard.server;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
	static Logger logger;
	
	private static void init() {
		logger = Logger.getLogger("MyLog");
		logger.setUseParentHandlers(false);
	    FileHandler fh;
	    try {
//	    	String path = System.getProperty("user.home");
	        if (System.getProperty("os.name").startsWith("Windows")) {
	            fh = new FileHandler("d:/temp/TechStandardLogFile.log");
	        } else {
	            fh = new FileHandler("/usr/share/tomcat7/logs/techstandard.log");
	        }
//	    	fh = new FileHandler(System.getProperty("user.home") + File.separator + "techstandard.log");
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);    

	    } catch (SecurityException e) {  
	    	System.out.println(e.getLocalizedMessage());
	        e.printStackTrace();  
	    } catch (IOException e) {
	    	System.out.println(e.getLocalizedMessage());
	        e.printStackTrace();  
	    }
	}
	
	public static void info (String data) {
		if (logger == null) {
			init();
		}
        logger.info(data);
	}
	
	public static void warning (String data) {
		if (logger == null) {
			init();
		}
        logger.warning(data);
	}
}
