package trends;

import java.io.DataInputStream;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.json.DataObjectFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class GetTrend {

	private static Date d;
	private static int v;
	private static ArrayList<Date> darray;
	private static ArrayList<Integer> varray;
	
	public static void main(String[] args) throws GoogleTrendsClientException, GoogleTrendsRequestException {
		// TODO Auto-generated method stub

		String u = "konstantinos.ar@gmail.com";
        String p = "arvanitis3210689";
		String url = "http://www.google.com/trends/fetchComponent?q=bank%20of%20america%20debt&cid=TIMESERIES_GRAPH_0&export=3&hl=en";
		JSONArray json_result = null;
		JSONArray json_result2 = null;
		DataInputStream dis = new DataInputStream(System.in);
		
		SimpleDateFormat df = new SimpleDateFormat("MM YYYY");
		
		MongoClient m = null;
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		/* Creates a new authenticator */
        GoogleAuthenticator authenticator = new GoogleAuthenticator(u, p, httpClient);
        /* Creates a new Google Trends Client */
        GoogleTrendsClient client = new GoogleTrendsClient(authenticator, httpClient);
        GoogleTrendsRequest request = new GoogleTrendsRequest("bank of america debt");

        /* Here the default request params can be modified with getter/setter methods */
        //JSONObject content = client.execute(request);

		
		try {
			m = new MongoClient("localhost");
			//DefaultHttpClient httpclient = new DefaultHttpClient();
			//JSONObject json_data = JSONfunctions.getJSONfromURL(url);
            //JSONObject json_query = json_data.getJSONObject("table");
			//json_result = json_query.getJSONArray("rows");
			JSONObject json_data = client.execute(request);
			JSONObject json_query = json_data.getJSONObject("table");
			json_result = json_query.getJSONArray("rows");
			
			if (json_result != null)
				for (int i = 0; i < json_result.length(); i++)
				{
					JSONObject vo = json_result.getJSONObject(i);
					JSONArray var = vo.getJSONArray("c");
						JSONObject v1 = var.getJSONObject(0);
						d = df.parse(v1.optString("f"));
						darray.add(d);
						JSONObject v2 = var.getJSONObject(1);
						v = v2.optInt("f");
						varray.add(v);
						System.out.println("Date is: " + d + "  ,  Value is: " + v);
				}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  /*      DB db = m.getDB("market");
 
        final DBCollection coll = db.getCollection("stock");
 
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                System.out.println(status.getUser().getName() + " : " + status.getText());
                String tweet = DataObjectFactory.getRawJSON(status);
                System.out.println(tweet);
                DBObject doc = (DBObject)JSON.parse(tweet);
                coll.insert(doc);
 
            }
 
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }
 
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }
 
            public void onScrubGeo(long l, long l1) {
            }
 
            public void onException(Exception ex) {
                ex.printStackTrace();
            }

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}
        };
        twitterStream.addListener(listener);
        FilterQuery q = new FilterQuery();
        String[] keywords = {"Apple Inc.","iPhone","iPad","macbook","iMac","ipad air","macbook pro","macbook air","appStore","iphone 6"};
        q.track(keywords);
        String[] lang = {"en"};
        q.language(lang);
        twitterStream.filter(q);
        //twitterStream.sample();
        while (coll.count() < tweetCount) {

        }
        twitterStream.shutdown();
/*
        DBCursor cursorDoc = coll.find();
 
        while (cursorDoc.hasNext()) {
            System.out.println(cursorDoc.next());
        }
        */
	}

}
