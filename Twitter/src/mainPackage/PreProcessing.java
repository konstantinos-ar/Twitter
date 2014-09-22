package mainPackage;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.corpus.ObjectHandler;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import twitter4j.TwitterException;

/**
 * Bοηθητική κλάση για την επεξεργασία των δεδομένων, ανάγνωση απο και προς τη
 * βάση και αποθήκευση καθως και υπολογισμος των στατιστικων στοιχείων, οπως
 * IDF.
 *
 * Στα πειράματά μας, χρησιμοποιήσαμε αυτή τη κλάση για ολα τα παραπάνω αλλα
 * θεωρήσαμε οτι ήδη έχουμε βγάλει τα αποτελέσματα και αντι για να περιέχει μια
 * main μέθοδο την παραδίδουμε ως έχει. Μπορεί να δημιουργηθεί αντικείμενο απο
 * αυτη και να γίνει η κατάλληλη προεπεξεργασία.
 */
public class PreProcessing
{

	//Το υπολογισμένο IDF για ολη τη συλλογή των λέξεων.
	HashMap<String, Double> idf = new HashMap<String, Double>();
	//Λίστα με τον αριθμο εμφανίσεων κάθε λέξης για κάθε μοναδικο tweet.
	HashMap<String, HashMap<String, Integer>> postingMap = new HashMap<String, HashMap<String, Integer>>();
	//Ο Classifier που χρησιμοποιουμε για binary classification.
	LMClassifier classifier;

	public HashMap<String, Double> getIdf()
	{
		return idf;
	}

	public HashMap<String, HashMap<String, Integer>> getPostings()
	{
		return postingMap;
	}

	public LMClassifier getClassifier()
	{
		return classifier;
	}

	public PreProcessing()
	{
		try
		{
			mineDatabase();
		}
		catch (IOException ex)
		{
			Logger.getLogger(PreProcessing.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (ClassNotFoundException ex)
		{
			Logger.getLogger(PreProcessing.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Συνδεση με τη βαση, λήψη ολων των αποθηκευμενων στοιχείων και εξαγωγη
	 * στατιστικων καθως και δειγματοληψία αρνητικων/ουδέτερων/θετικων
	 * παραδειγμάτων για εκπαίδευση του classifier. Στη συνέχεια αποθήκευσή τους
	 * στο δίσκο με serialization.
	 *
	 */
	private void mineDatabase() throws UnknownHostException, IOException, ClassNotFoundException
	{

		//Συνδεση στη βάση και δημιουργία φορμά ωρας με βάση το Greenwich
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
		dateFormat.setLenient(false);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		MongoClient mongoClient = new MongoClient("localhost");
		DB db = mongoClient.getDB("times");
		final DBCollection collection1 = db.getCollection("news");
		HashMap<String, Integer> tokenMap = new HashMap<>();
		ArrayList<Article> posList = new ArrayList<Article>();
		ArrayList<Article> negList = new ArrayList<Article>();
		ArrayList<Article> netList = new ArrayList<Article>();
		Article currDoc;
		DBCursor cursor = collection1.find();
		int counter = 0;
		double sentiScore;
		SentiLexicon lexicon = new SentiLexicon();
		try
		{
			while (cursor.hasNext())
			{
				counter++;
				currDoc = new Article(cursor.next());
				/*
                 Aποθήκεση της λίστας των λέξεων του συγκεκριμένου tweet. Για κάθε
                 tweet αποθηκευουμε τα tokens της χρησιμοποιωντας ως κλειδί το ID
                 του tweet (το Twitter εγγυάται οτι είναι μοναδικο).
				 */
				//postingMap.put(currDoc.getId(), currDoc.getTokens());
				//Ενημέρωση του πίνακα των συνολικων μοναδικων λέξεων, για τη συλλογή
				//currDoc.updateDf(tokenMap);
				/*
                 Λήψη του sentiment score για το κείμενο του tweet. Μέ βάση αυτο
                 το score βρίσκουμε αν ένα παράδειγμα είναι θετικο ή αρνητικο και
                 το κατατάσσουμε αναλογως στο συνολο παραδειγμάτων εκπαίδευσης του
                 classifier.
				 */
				sentiScore = lexicon.extractScore(currDoc.getStemmed());
				if (sentiScore > 0)
				{
					if (posList.size() < 10000)
					{
						posList.add(currDoc);
					}
				}
				else if (sentiScore < 0)
				{
					if (negList.size() < 10000)
					{
						negList.add(currDoc);
					}
				}
				else
				{
					if (netList.size() < 20000)
					{
						netList.add(currDoc);
					}
				}
			}
		}
		finally
		{
			cursor.close();
		}
		/*
         Ο τελικος υπολογισμος του IDF αφου πλέον ξέρουμε τις λέξεις της συλλογής
         μετά την επεξεργασία, αποθηκευουμε τα στοιχεία στο δίσκο.
		 */
		idf = normalizeDf(tokenMap, counter);
		SerializationUtil.serialize(idf, "idf.dat");
		tokenMap.clear();
		classifier = train(posList, negList, netList);
		SerializationUtil.serialize(classifier, "classifier.dat");
		SerializationUtil.serialize(postingMap, "postings.dat");
	}

	//Εκπαίδευση του Classifier με το training set
	private LMClassifier train(ArrayList<Article> pos, ArrayList<Article> neg, ArrayList<Article> neu) throws IOException, ClassNotFoundException
	{
		String[] categories = new String[3];
		LMClassifier classi;
		categories[0] = "pos";
		categories[1] = "neg";
		categories[2] = "neu";
		ArrayList<Iterator> itList = new ArrayList(3);
		itList.add(pos.iterator());
		itList.add(neg.iterator());
		itList.add(neu.iterator());
		Iterator<Article> itCurr;
		String text;
		int nGram = 7;

		classi = DynamicLMClassifier.createNGramProcess(categories, nGram);
		Article temp;

		for (int i = 0; i < categories.length; ++i)
		{
			String category = categories[i];
			Classification classification = new Classification(category);
			itCurr = itList.get(i);
			while (itCurr.hasNext())
			{
				temp = itCurr.next();
				//if (temp.getAbstract() != null)
					text = temp.getStemmed();
				//else
				//	text = temp.getSnippet();
				Classified classified = new Classified(text, classification);
				((ObjectHandler) classi).handle(classified);
			}
		}
		return classi;
	}

	private static HashMap<String, Double> normalizeDf(HashMap<String, Integer> df, int docCounter)
	{

		HashMap<String, Double> idf = new HashMap<String, Double>();
		Set keys = df.keySet();
		Iterator<String> it = keys.iterator();
		double oldValue;
		String currKey;
		while (it.hasNext())
		{
			currKey = it.next();
			oldValue = (double) df.get(currKey);
			idf.put(currKey, Math.log(((double) docCounter) / oldValue));
		}
		return idf;

	}

	public static void main()
	{
		PreProcessing p = new PreProcessing();
	}
}
