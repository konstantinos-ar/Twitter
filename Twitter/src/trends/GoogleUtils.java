package trends;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.http.client.methods.HttpRequestBase;

public class GoogleUtils {

	  private GoogleUtils() {
	  }

	  /**
	   * Setup the <code>HttpRequestBase</code> r with default headers and HTTP
	   * parameters.
	   *
	   * @param r <code>HttpRequestBase</code> to setup
	   * @throws org.apache.commons.configuration.ConfigurationException
	   */
	  public static void setupHttpRequestDefaults(HttpRequestBase r) throws ConfigurationException {
	    DataConfiguration config = GoogleConfigurator.getConfiguration();

	    //r.addHeader("Content-type", config.getString("request.default.content-type"));
	    r.addHeader("User-Agent", config.getString("request.default.user-agent"));
	    r.addHeader("Accept", config.getString("request.default.accept"));
	    r.addHeader("Accept-Language", config.getString("request.default.accept-language"));
	    r.addHeader("Accept-Encoding", config.getString("request.default.accept-encoding"));
	    r.addHeader("Connection", config.getString("request.default.connection"));
	    r.addHeader("Set-Encoding", "iso-8859-1");
	    
	  }

	  /**
	   * Eat the <code>InputStream</code> in and return a string.
	   *
	   * @param in <code>InputStream</code> to read from
	   * @return a <code>String</code> representation of the stream
	   * @throws java.io.IOException
	   */
	  public static String toString(InputStream in) throws IOException {
	    String string;
	    StringBuilder outputBuilder = new StringBuilder();
	    if (in != null) {
	      BufferedReader reader = new BufferedReader(new InputStreamReader(in,"iso-8859-1"),8);
	      while (null != (string = reader.readLine())) {
	        outputBuilder.append(string).append('\n');
	      }
	    }
	    return outputBuilder.toString();
	  }
	}
