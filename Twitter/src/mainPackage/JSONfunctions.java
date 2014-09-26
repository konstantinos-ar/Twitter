package mainPackage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class JSONfunctions
{

	public static JSONObject getJSONfromURL(String url)
	{
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;
		DefaultHttpClient httpclient = null;

		// Download JSON data from URL
		try
		{
			httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		}
		catch(Exception e)
		{
			System.out.println("Error in http connection "+e.toString());
		}

		// Convert response to string
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
			is.close();
			httpclient.close();
			result=sb.toString();

		}
		catch(Exception e)
		{
			System.out.println("Error converting result "+e.toString());
		}

		try
		{

			/*if (!result.startsWith("{\"query\":{"))
        	{
        		result = result.replace("// Data table response", "");
        		result = result.replace("\ngoogle.visualization.Query.setResponse(", "");
        		result = result.replace("\"version\":\"0.6\",\"status\":\"ok\",\"sig\":\"253350858\",", "");
        		result = result.replace(");", "");
        	}*/

			jArray = new JSONObject(result);  

		}
		catch(JSONException e)
		{
			System.out.println("Error parsing data "+e.toString());
		}

		return jArray;
	}
}
