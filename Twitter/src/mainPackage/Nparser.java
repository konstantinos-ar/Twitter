package mainPackage;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class Nparser
{

	//private static ArrayList<HashMap<String, String>> arraylist;
	//private static ArrayList<String> sym = new ArrayList<String>();
	//private static final String TITLE = "title";
	//private static final String DESC = "description";
	//private static final String THUMB = "thumbnail";
	private static String urlin = "http://api.nytimes.com/svc/search/v2/articlesearch.json?q=apple&fq=(%27aapl%27%20%27apple%20inc%27)&begin_date=20040101&end_date=20140914&fq=news_desk%3A%28%22Business/Financial%20Desk%22%20%22Business%22%29&sort=oldest&api-key=932411dde075fd16337547bd13fdb616%3A11%3A69757573";

	public static void main(String[] args)
	{
		stream();
		//getLast();
		//PreProcessing p = new PreProcessing();
	}

	private static void stream()
	{
		//while (getLast() != "20140914")
		{
			//arraylist = new ArrayList<HashMap<String, String>>();
			JSONArray json_result = null;
			MongoClient m = null;
			JSONObject vo = null;
			DBObject doc = null;
			String url = urlin + "&page=";
			int exx = 0;
			//DataInputStream dis = new DataInputStream(System.in);

			try
			{
				m = new MongoClient("localhost");
				DB db = m.getDB("times");
				final DBCollection coll = db.getCollection("aapl");

				// Retrieve JSON Objects from the given URL in JSONfunctions.class
				for (int j = 0; j < 101; j++)
				{
					JSONObject json_data = JSONfunctions.getJSONfromURL(url+j);
					JSONObject json_query = json_data.getJSONObject("response");
					json_result = json_query.getJSONArray("docs");

					if (json_result != null)
						for (int i = 0; i < json_result.length(); i++) 
						{
							vo = json_result.getJSONObject(i);
							doc = (DBObject) JSON.parse(vo.toString());
							try
							{
								coll.insert(doc);
								//System.out.println(doc);
							}
							catch (Exception e)
							{
								System.out.println(e.toString());
								++exx;
							}

							//System.out.print("\rURL: " + vo.optString("web_url").replace("\"", "") + "\nSource: " + vo.optString("source") + "\nHeadline: " + vo.optJSONObject("headline").optString("main") + "\nDate: " + vo.optString("pub_date") + "\nNews_Desk: " + vo.optString("news_desk") + "\ntype_of_material: " + vo.optString("type_of_material"));
							//System.out.println("\n");


						}
					//System.out.println("\r");

					Thread.sleep(100);



				}
				System.out.println("exceptions: " + exx);
				urlin = "http://api.nytimes.com/svc/search/v2/articlesearch.json?q=apple&fq=(%27aapl%27%20%27apple%20inc%27)&begin_date="+getLast()+"&end_date=20140101&fq=news_desk%3A%28%22Business/Financial%20Desk%22%20%22Business%22%29&sort=oldest&api-key=932411dde075fd16337547bd13fdb616%3A11%3A69757573";
			}
			//catch (JSONException | UnknownHostException | InterruptedException e)
			catch (Exception e) 
			{
				//Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/*
    DBCursor cursorDoc = coll.find();
	DBObject dbo = null;

	while (cursorDoc.hasNext())
	{
		dbo = cursorDoc.next();
		System.out.println(dbo.get("headline"));
	}
	 */

	public static String getLast()
	{
		MongoClient m = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
		String d;
		Date dd = null;
		try
		{
			m = new MongoClient("localhost");
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DB db = m.getDB("times");
		final DBCollection coll = db.getCollection("aapl");
		DBCursor cursorDoc = coll.find().sort(new BasicDBObject( "pub_date", -1 )).limit(1);
		DBObject dbo = null;

		//while (cursorDoc.hasNext())
		//{
			dbo = cursorDoc.next();
			//System.out.println(dbo.get("pub_date"));
			System.out.println(dbo.get("pub_date").toString().substring(0, 10));
			try
			{
				dd = formatter2.parse(dbo.get("pub_date").toString().substring(0, 10));
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			d = formatter.format(dd);
			return d;
		//}
	}

}
