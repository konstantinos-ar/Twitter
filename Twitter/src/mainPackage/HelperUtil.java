package mainPackage;

import com.aliasi.classify.LMClassifier;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

/**
 * Βοηθητική κλάση με λειτουργίες που θεωρήσαμε οτι δεν χρειαζοτανε να
 * δημιουργήσουμε ξεχωριστές κλάσεις για τη κάθε μια, οποτε και τις
 * συγκεντρωσαμε εδω.
 */
public class HelperUtil
{

	static final long ONE_MINUTE_IN_MILLIS = 60000;

	public HelperUtil()
	{
	}

	/**
	 * Μέθοδος που μας δίνει cursor με αποτελέσματα απο το χρονικο διάστημα
	 * start μέχρι end. Το πεδίο _id της βάσης κρατά την πληροφορία του ποτε
	 * αποθηκευτηκε ένα στοιχείο. Ετσι θεωρησαμε οτι αφου τα αποτελέσματα μας
	 * είναι realtime είναι σωστο να πάρουμε αυτο. Στη πραγματικοτητα
	 * παρατηρήσαμε οτι δεν υπάρχει καθολου διαφορα.
	 * @throws ParseException 
	 */

	public DBCursor getCursorInRange(DBCollection coll, String start, String end) throws ParseException
	{

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		//SimpleDateFormat formatter2 = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
		//String d, d2, s1, s2;
		//Date dd = null, dd2 = null;
		//DBObject dbo = null;
		//dbo = cursorDoc.next();
		
		//String year1 = start.toString().substring(start.toString().length()-5);
		//String year2 = end.toString().substring(end.toString().length()-5);
		//String date1 = start.toString().substring(4, 10);
		//String date2 = end.toString().substring(4, 10);
		//s1 = date1 + "," + year1;
		//s2 = date2 + "," + year2;
		//try {
		//	dd = formatter2.parse(start.toString());
		//	dd2 = formatter2.parse(end.toString());
		//} catch (ParseException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		//d = formatter.format(dd);
		//d2 = formatter.format(dd2);
		//ObjectId startId = new ObjectId(start);
		//ObjectId endId = new ObjectId(end);
		BasicDBObject dateQuery = new BasicDBObject("pub_date", new BasicDBObject("$gte", formatter.parseObject(start)).append("$lte", formatter.parseObject(end)));//"pub_date",
				//new BasicDBObject("$gte", startId).append("$lt", endId));
		//dateQuery.put("pub_date", new BasicDBObject("$gt", Integer.parseInt(d)).append("$lt", Integer.parseInt(d2)));
		return coll.find(dateQuery);
	}

	/**
	 * Η μέθοδος μας επιστρέφει ένα array με 3 String τα οποία είναι
	 * επεξεργασμένα για το συναίσθημα και έτοιμα να αναπαρασταθουν στο google 
	 * chart api και στο google maps api
	 *
	 */
	public String[] sentiStream(Date start, Date end,
			LMClassifier classifier) throws UnknownHostException, Exception
	{
		HashMap<String, Integer> countries = new HashMap<String, Integer>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat formatter2 = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);
		SimpleDateFormat form = new SimpleDateFormat("MMM dd yyyy");
		String d, d2, s1, s2;
		Date dd = null, dd2 = null;
	//	MaxentTagger tagger =  new MaxentTagger("C:/Users/user/git/Twitter/Twitter/models/wsj-0-18-left3words-distsim.tagger");
		try {
			dd = formatter2.parse(start.toString());
			dd2 = formatter2.parse(end.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		d = formatter.format(dd);
		d2 = formatter.format(dd2);
		String country = "[";
		String[] result = new String[3];
		//long BuckSize = ONE_MINUTE_IN_MILLIS * Duration;
		MongoClient mongoClient = new MongoClient("localhost");
		DB db = mongoClient.getDB("times");
		DBObject obj;
		final DBCollection collection = db.getCollection("news");
		Article status;
		int sumNeg, sumPos, sumNeu, sumNegAll, sumPosAll, sumNeuAll;
		String stringTimeline = "[['Date', 'Sentiment'],";
		//SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-YYYY");
		sumNegAll = 0;
		sumPosAll = 0;
		sumNeuAll = 0;
		Date dn, dx;
		//for (long time = start.getTime(); time <= end.getTime(); time += BuckSize)
		//{
			sumNeg = 0;
			sumPos = 0;
			sumNeu = 0;
			String pubdate = d;
			DBCursor temp = getCursorInRange(collection, d, d2);
			double fscore = 0;


			while (temp.hasNext())
			{
				obj = temp.next();

				status = new Article(obj);
				if (!status.getDate().equals(pubdate))
				{
					stringTimeline = stringTimeline + "['" + form.format(formatter.parse(pubdate)) + "',"
							+ (sumPos - sumNeg) + "],";
					sumNeg = 0;
					sumPos = 0;
					sumNeu = 0;
				}
				pubdate = status.getDate();
				Integer score;
				System.out.println("Category Stemmed: " + status.getStemmed());
		//		String tagged = tagger.tagString(status.getStemmed());
		//		System.out.println(tagged);
				/*String s = null;
				Properties props = new Properties(); 
				  props.put("annotators", "tokenize, ssplit, pos, lemma"); 
				  StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
				  String text = status.getStemmed(); 
				  s = null;
				  Annotation document = pipeline.process(text);  
				  for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
				    for(CoreLabel token: sentence.get(TokensAnnotation.class)) {       
				    String word = token.get(TextAnnotation.class);      
				    String lemma = token.get(LemmaAnnotation.class); 
				    if ( s == null)
						s = lemma + " ";
					else
						s = s + lemma + " ";
				    //System.out.println("lemmatized version :" + lemma);
				    } }
				  System.out.println("lemmatized version :" + s);*/
				
				String sentiTest = classifier.classify(status.getStemmed()).bestCategory();
				//δημιουργουμε το χάρτη με τα θετικά,αρνητικά και ουδέτερα tweets
				if ("neu".equals(sentiTest))
				{
					sumNeu++;
					//if (status.getGeoLocation() != null) {
					//    country = country + "['Neutral'," + status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude() + ",'yellow'],";
					//}
				} 
				else if ("pos".equals(sentiTest))
				{
					//if (status.getGeoLocation() != null) {
					//    country = country + "['Positive'," + status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude() + ",'blue'],";
					//}
					sumPos++;
				}
				else if ("neg".equals(sentiTest))
				{
					//if (status.getGeoLocation() != null) {
					//    country = country + "['Negative'," + status.getGeoLocation().getLatitude() + "," + status.getGeoLocation().getLongitude() + ",'red'],";
					//}
					sumNeg++;
				}
				sumNegAll += sumNeg;
				sumPosAll += sumPos;
				sumNeuAll += sumNeu;

			}
			//sumNegAll += sumNeg;
			//sumPosAll += sumPos;
			//sumNeuAll += sumNeu;
			//δημιουργουμε το ιστογραμμα του συναισθήματος των tweets
			//stringTimeline = stringTimeline + "['" + formatter.format(end) + "',"
			//		+ sumPos + "," + sumNeg + "," + sumNeu + "],";

		//}
		stringTimeline = stringTimeline + "]";
		result[0] = stringTimeline;
		String stingMap = "[['Country', 'Sentiment'],";

		result[2] = country + "]";
		result[1] = "[['Sentiment','number of Articles'],"//δημιουργουμε τη πίτα του συναισθήματος των tweets
				+ "['Positive'," + sumPosAll + "],"
				+ "['Negative'," + sumNegAll + "],"
				+ "['Neutral'," + sumNeuAll + "]]";

		return result;
	}

	/**
	 * Η μέθοδος μας επιστρέφει ένα array με 2 Strings τα οποία είναι
	 * επεξεργασμένα για να αναπαρασταθουν στο google chart api για την αναπαράσταση
	 * περιοχων ενδιαφέροντος
	 *
	 */
	/*public String[] stringOfPeaks(Date start, Date end, Integer topWords, Integer topURLs, Integer topRetweets, Integer Duration, HashMap<String, Double> idf,
			HashMap<Long, HashMap<String, Integer>> postingMap) throws UnknownHostException, IOException, TwitterException
			{

		MongoClient mongoClient = new MongoClient("localhost");

		int i = 0;
		long BuckSize = ONE_MINUTE_IN_MILLIS * Duration;
		DB db = mongoClient.getDB("times");
		ArrayList<Date> buckets = new ArrayList<Date>();
		ArrayList<Integer> timeLine = new ArrayList<Integer>();
		final DBCollection collection = db.getCollection("news");
		boolean t = true;
		//δημιουργία ιστογράμματος των tweets
		for (long time = start.getTime(); time <= end.getTime(); time += BuckSize)
		{
			buckets.add(new Date(time));
			int k = getCursorInRange(collection, new Date(time), new Date(time + BuckSize)).count();

			timeLine.add(k);

		}
		//αρχικοποιήση ArrayList
		ArrayList<String> peakAreas = new ArrayList<String>();
		for (i = 0; i < buckets.size(); i++)
		{
			peakAreas.add("null");
		}
		ArrayList<String> peakAreas2 = new ArrayList<String>();
		for (i = 0; i < buckets.size(); i++)
		{
			peakAreas2.add("false,");
		}

		Integer l = 1;
		Integer[] timeLineArray = timeLine.toArray(new Integer[0]);
		//εκτελουμε την μέθοδο ευρεσης peaks
		ArrayList<String> peaks = peakFinding.peakFinder(timeLineArray, 0.125, 5, 3);

		final DBCollection collection1 = db.getCollection("news");

		String resultString = "";
		DBCursor cursor;
		for (String key : peaks)
		{
			String[] tempAreas = key.split(" ");
			if (Integer.parseInt(tempAreas[1]) >= buckets.size())
			{
				tempAreas[1] = Integer.toString(buckets.size() - 1);
			}
			//για κάθε peak βρίσκουμε τις πιο σημαντικές λέξεις με TF/IDF
			cursor = getCursorInRange(collection, buckets.get(
					Integer.parseInt(tempAreas[0])), 
					buckets.get(Integer.parseInt(tempAreas[1])));
			String[] results = TopicExtractor.topWordsInRange(cursor,
					topWords, topURLs, topRetweets, postingMap, idf);
			cursor.close();

			//επεξεργασία για την εμφάνιση στο google-charts
			int max = 0, index = -1;
			for (i = Integer.parseInt(tempAreas[0]); i <= Integer.parseInt(tempAreas[1]); i++)
			{
				if (i < peakAreas.size())
				{
					peakAreas2.set(i, "true,");
					if (max < timeLineArray[i])
					{
						index = i;
						max = timeLineArray[i];
					}
				}

			}
			//επεξεργασία για την εμφάνιση στο google-charts
			peakAreas.set(index, "'Peak" + Integer.toString(l) + "'"); 
			resultString += "<p><div class=\"alert alert-info\"><h4><span class=\"label label-primary\">Peak" 
					+ Integer.toString(l) + "</span></h4><span class=\"label label-danger\">Words:</span> " + results[0] + " <p><span class=\"label label-info\">URLs:</span> " + results[1] + "<p><span class=\"label label-success\">Most Retweeted tweets:</span> " + results[2] + "</div>";
			l++;
		}

		String sum = "[";
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
		//επεξεργασία για την εμφάνιση στο google-charts
		for (i = 0; i < buckets.size(); i++)
		{

			sum = sum + "['" + formatter.format(buckets.get(i)) + "'," + timeLine.get(i) + ",";

			sum = sum + peakAreas2.get(i);

			sum = sum + peakAreas.get(i) + "],";
		}
		sum = sum + "]";
		String[] fin = new String[2];
		fin[0] = sum;
		fin[1] = resultString;
		return fin;
	}*/
}
