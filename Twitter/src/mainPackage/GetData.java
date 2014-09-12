package mainPackage;

import java.net.UnknownHostException;

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
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class GetData {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MongoClient m = null;
		try {
			m = new MongoClient("localhost");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        DB db = m.getDB("market");
        int tweetCount = 22000;
 
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
