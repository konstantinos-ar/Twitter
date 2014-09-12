package mainPackage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class Nparser
{

	private static ArrayList<HashMap<String, String>> arraylist;
	private static ArrayList<String> sym = new ArrayList<String>();
	private static final String TITLE = "title";
	private static final String DESC = "description";
	private static final String THUMB = "thumbnail";
	private static final String urlin = "http://api.nytimes.com/svc/search/v2/articlesearch.json?q=S&P500&begin_date=20040101&end_date=20140101&fq=news_desk%3A%28%22Business/Financial%20Desk%22%29&sort=oldest&api-key=932411dde075fd16337547bd13fdb616%3A11%3A69757573";

	public static void main(String[] args)
	{
		//stream();
		PreProcessing p = new PreProcessing();
	}

	private static void stream()
	{
		arraylist = new ArrayList<HashMap<String, String>>();
		JSONArray json_result = null;
		MongoClient m = null;
		JSONObject vo = null;
		DBObject doc = null;
		String url = urlin + "&page=";
		//DataInputStream dis = new DataInputStream(System.in);

		try
		{
			m = new MongoClient("localhost");
			DB db = m.getDB("times");
			final DBCollection coll = db.getCollection("news");

			// Retrieve JSON Objects from the given URL in JSONfunctions.class
			for (int j = 0; j < 10; j++)
			{
				JSONObject json_data = JSONfunctions.getJSONfromURL(url+j);
				JSONObject json_query = json_data.getJSONObject("response");
				json_result = json_query.getJSONArray("docs");

				if (json_result != null)
					for (int i = 0; i < json_result.length(); i++) 
					{
						vo = json_result.getJSONObject(i);
						doc = (DBObject) JSON.parse(vo.toString());
						coll.insert(doc);

						System.out.print("\rURL: " + vo.optString("web_url").replace("\"", "") + "\nSource: " + vo.optString("source") + "\nHeadline: " + vo.optJSONObject("headline").optString("main") + "\nDate: " + vo.optString("pub_date") + "\nNews_Desk: " + vo.optString("news_desk") + "\ntype_of_material: " + vo.optString("type_of_material"));
						System.out.println("\n");


					}
				System.out.println("\r");

				Thread.sleep(100);



			}
		}
		catch (JSONException | UnknownHostException | InterruptedException e) 
		{
			//Log.e("Error", e.getMessage());
			e.printStackTrace();
		}


	}

	/*
    DBCursor cursorDoc = coll.find();
	DBObject dbo = null;

	while (cursorDoc.hasNext())
	{
		dbo = cursorDoc.next();
		System.out.println(dbo.get("lead_paragraph") + "\n");
	}
	*/

}
