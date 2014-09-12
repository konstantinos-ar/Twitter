package trends;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

public class GoogleConfigurator {

	  private static DataConfiguration config = null;
	  private static final String CONFIG_FILE = "config.properties";

	  private GoogleConfigurator() {
	  }

	  public static DataConfiguration getConfiguration() throws ConfigurationException {
	    if (config == null) {
	      config = new DataConfiguration(new PropertiesConfiguration(CONFIG_FILE));
	    }
	    return config;
	  }
	  
	  public static String getLoggerPrefix() {
	    try {
	      return (String) getConfiguration().getProperty("defaultLoggerPrefix");
	    } catch (ConfigurationException ex) {
	      Logger.getLogger(GoogleConfigurator.class.getName()).
	        log(Level.WARNING, 
	        "Cannot find prefix for logger, messages might not be displayed. Please check config.properties", ex);
	      return "";
	    }
	  }
	}
