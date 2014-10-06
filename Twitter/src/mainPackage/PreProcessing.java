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

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Κύρια κλάση για την επεξεργασία των δεδομένων, ανάγνωση από και προς τη
 * βάση και αποθήκευση καθώς και υπολογισμός και εξαγωγή των στατιστικών στοιχείων,
 * οπως η εκπαίδευση του classifier.
 */
public class PreProcessing
{

	//Το υπολογισμένο IDF για ολη τη συλλογή των λέξεων.
	HashMap<String, Double> idf = new HashMap<String, Double>();
	//Λίστα με τον αριθμο εμφανίσεων κάθε λέξης για κάθε μοναδικο article.
	HashMap<String, HashMap<String, Integer>> postingMap = new HashMap<String, HashMap<String, Integer>>();
	@SuppressWarnings("rawtypes")
	//Ο Classifier που χρησιμοποιείται για binary classification.
	LMClassifier classifier;
	//MaxentTagger tagger = new MaxentTagger("C:/Users/user/git/Twitter/Twitter/models/wsj-0-18-left3words-distsim.tagger");

	public HashMap<String, Double> getIdf()
	{
		return idf;
	}

	public HashMap<String, HashMap<String, Integer>> getPostings()
	{
		return postingMap;
	}

	@SuppressWarnings("rawtypes")
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
	 * Σύνδεση με τη βάση, λήψη όλων των αποθηκευμένων στοιχείων και εξαγωγή
	 * στατιστικών καθώς και δειγματοληψία αρνητικών/ουδέτερων/θετικών
	 * παραδειγμάτων για εκπαίδευση του classifier. Στη συνέχεια αποθήκευσή τους
	 * στο δίσκο με serialization.
	 *
	 */
	private void mineDatabase() throws UnknownHostException, IOException, ClassNotFoundException
	{

		//Συνδεση στη βάση και δημιουργία φορμά ωρας
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
		dateFormat.setLenient(false);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String[] cols = {"news","nasdaq","aapl","msft"};
		MongoClient mongoClient = new MongoClient("localhost");
		DB db = mongoClient.getDB("times");
		DBCollection collection1;
		DBCursor cursor;
		HashMap<String, Integer> tokenMap = new HashMap<>();
		ArrayList<Article> posList = new ArrayList<Article>();
		ArrayList<Article> negList = new ArrayList<Article>();
		ArrayList<Article> netList = new ArrayList<Article>();
		SentiLexicon lexicon = new SentiLexicon();
		Article currDoc;
		for ( int i = 0; i < cols.length; i++)
		{
		collection1 = db.getCollection(cols[i]);
		cursor = collection1.find();
		//int counter = 0;
		double sentiScore;
		
		try
		{
			while (cursor.hasNext())
			{
				//counter++;
				currDoc = new Article(cursor.next(), null);
				/*
                 Aποθήκευση της λίστας των λέξεων του συγκεκριμένου article. Για κάθε
                 articke αποθηκευονται τα tokens του χρησιμοποιωντας ως κλειδί το ID
                 του article.
				 */
				//postingMap.put(currDoc.getId(), currDoc.getTokens());
				//Ενημέρωση του πίνακα των συνολικων μοναδικων λέξεων, για τη συλλογή
				//currDoc.updateDf(tokenMap);
				/*
                 Λήψη του sentiment score για το κείμενο του article. Μέ βάση αυτο
                 το score βρίσκουμε αν ένα παράδειγμα είναι θετικο ή αρνητικο και
                 το κατατάσσουμε αναλογως στο συνολο παραδειγμάτων εκπαίδευσης του
                 classifier.
				 */
				sentiScore = lexicon.getScore(currDoc.getStemmed());
				if (sentiScore > 1)
				{
					if (posList.size() < 10000)
					{
						posList.add(currDoc);
					}
				}
				else if (sentiScore < -1)
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
		}}
		/*
         Ο τελικος υπολογισμος του IDF αφου πλέον ξέρουμε τις λέξεις της συλλογής
         μετά την επεξεργασία, αποθηκευουμε τα στοιχεία στο δίσκο.
		 */
		//idf = normalizeDf(tokenMap, counter);
		//SerializationUtil.serialize(idf, "idf.dat");
		tokenMap.clear();
		classifier = train(posList, negList, netList);
		SerializationUtil.serialize(classifier, "classifier.dat");
		//SerializationUtil.serialize(postingMap, "postings.dat");
	}

	//Εκπαίδευση του Classifier με το training set
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LMClassifier train(ArrayList<Article> pos, ArrayList<Article> neg, ArrayList<Article> neu) throws IOException, ClassNotFoundException
	{
		String[] categories = new String[3];
		LMClassifier classi;
		categories[0] = "pos";
		categories[1] = "neg";
		categories[2] = "neu";
		ArrayList<Iterator<Article>> itList = new ArrayList<Iterator<Article>>(3);
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
				System.out.println("Stemmed is: " + text);
				//else
				//	text = temp.getSnippet();
				Classified classified = new Classified(text, classification);
				try
				{
					((ObjectHandler) classi).handle(classified);
				}
				catch(Exception e){}
			}
		}
		return classi;
	}

	/*private static HashMap<String, Double> normalizeDf(HashMap<String, Integer> df, int docCounter)
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

	}*/

	/*public static void main()
	{
		PreProcessing p = new PreProcessing();
	}*/
}
