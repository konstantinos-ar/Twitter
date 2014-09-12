package trends;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleTrendsClient {

	  private final GoogleAuthenticator authenticator;
	  private final HttpClient client;

	  /**
	   *
	   * @param authenticator
	   * @param client
	   */
	  public GoogleTrendsClient(GoogleAuthenticator authenticator, HttpClient client) {
	    this.authenticator = authenticator;
	    this.client = client;
	  }

	  /**
	   * Execute the request.
	   *
	   * @param request
	   * @return content The content of the response
	   * @throws GoogleTrendsClientException
	   */
	  public JSONObject execute(GoogleTrendsRequest request) throws GoogleTrendsClientException {
	    String html = null;
	    JSONObject jArray = null;
	    try {
	      if (!authenticator.isLoggedIn()) {
	        authenticator.authenticate();
	      }
	      Logger.getLogger(GoogleConfigurator.getLoggerPrefix()).log(Level.FINE, "Query: {0}", request.build().toString());

	      HttpPost httpRequest = request.build();
	      HttpResponse response = client.execute(httpRequest);
	      html = GoogleUtils.toString(response.getEntity().getContent());
	      System.out.println("Page is : " + html);
	      httpRequest.releaseConnection();

	      Pattern p = Pattern.compile(GoogleConfigurator.getConfiguration().getString("google.trends.client.reError"), Pattern.CASE_INSENSITIVE);
	      Matcher matcher = p.matcher(html);
	      //if (matcher.find()) {
	        //throw new GoogleTrendsClientException("*** You are running too fast man! Looks like you reached your quota limit. Wait a while and slow it down with the '-S' option! *** ");
	      //}
	    } catch (GoogleAuthenticatorException ex) {
	      throw new GoogleTrendsClientException(ex);
	    } catch (ClientProtocolException ex) {
	      throw new GoogleTrendsClientException(ex);
	    } catch (IOException ex) {
	      throw new GoogleTrendsClientException(ex);
	    } catch (ConfigurationException ex) {
	      throw new GoogleTrendsClientException(ex);
	    } catch (GoogleTrendsRequestException ex) {
	      throw new GoogleTrendsClientException(ex);
	    }

	    try{
	    	SimpleDateFormat df = new SimpleDateFormat("MM YYYY");
	    	Date d;
	    	int v, i = 0, j = 0, k = 0;
	    	ArrayList<Date> darray;
	    	ArrayList<Integer> varray;
	    	while (i < html.length())
	    	{
	    		i = html.indexOf("\"c\":");
	    		j = html.indexOf("\"f\":", 1);
	    		k = html.indexOf("\"f\":", 2);
	    		html = html.substring(html.indexOf("]}"));
	    		System.out.println("Chars are: " + i + ", " + j + ", " + k);
	    	}
        	if (!html.startsWith("{\"query\":{"))
        	{
        		html = html.replace("// Data table response", "");
        		html = html.replace("\ngoogle.visualization.Query.setResponse(", "");
        		html = html.replace(");", "");
        	}
            jArray = new JSONObject(html);           
        }catch(JSONException e){
               // Log.e("log_tag", "Error parsing data "+e.toString());
        }
	    
	    return jArray;
	  }
	}