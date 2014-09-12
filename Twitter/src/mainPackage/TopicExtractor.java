package mainPackage;

import com.mongodb.DBCursor;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.URLEntity;

/**
 *
 * @author dgi
 */
public class TopicExtractor {

    /*
     Mέθοδος ευρεσης των Κ πιο σημαντικων λέξεων για το χρονικο παράθυρο που
     ορισε ο χρήστης ως πεδίο αναζήτησης. Μαζί με τις λέξεις, βρίσκουμε τα Κ πιο
     συχνά URL και τα K πιο retweeted tweets.
     */
    public static String[] topWordsInRange(DBCursor cursor,
            int topKwors, int topKUrls, int topKTweets, HashMap<Long, HashMap<String, Integer>> postingMap,
            HashMap<String, Double> idf) throws TwitterException {

        HashMap<String, Integer> postings;
        HashMap<String, Integer> urls;
        HashMap<String, Double> total = new HashMap<>();
        HashMap<String, Integer> totalUrls = new HashMap();
        HashMap<String, Integer> totalRetweet = new HashMap();
        String word = "";
        Iterator it;
        Iterator itUrl;
        Iterator itRetweet;
        double num, oldNum;
        int urlCounter;
        Long id;
        Status status;
        URLEntity[] urlArray;
        String url;
        while (cursor.hasNext()) {
            //για κάθε στοιχείο του χρονικου παραθυρου
            status = TwitterObjectFactory.createStatus(cursor.next().toString());
            totalRetweet.put(status.getText(), status.getRetweetCount());
            id = status.getId();
            urlArray = status.getURLEntities();
            postings = postingMap.get(id);

            it = postings.keySet().iterator();
            //δημιουργία πίνακα εμφανίσεων κάθε λέξης για το χρονικο παράθυρο
            while (it.hasNext()) {
                word = (String) it.next();
                num = postings.get(word);
                if (total.get(word) != null) {
                    oldNum = total.get(word);
                    num = num + oldNum;
                    total.put(word, num);
                } else {
                    total.put(word, num);
                }
            }
            for (int i = 0; i < urlArray.length; ++i) {
                url = urlArray[i].getURL();
                if (totalUrls.get(url) != null) {
                    urlCounter = totalUrls.get(url);
                    urlCounter++;
                    totalUrls.put(url, urlCounter);
                } else {
                    totalUrls.put(url, 1);
                }
            }

        }
        cursor.close();

        it = total.keySet().iterator();

        while (it.hasNext()) {
            /*
             Ανανέωση των τιμων του πίνακα των λέξεων με το IDF score που είχε
             προϋπολογιστεί απο τη συνολική συλλογή. Μας ενδιαφέρει να βρουμε 
             score που να υπολογίζει τη σημαντικοτητα μιας λέξης στο χρονικο
             παράθυρο μας αλλα και να περιλαμβάνει τη σημαντικοτητα απο το
             συνολικο dataset, για αυτο και ξαναυπολογίζουμε το TF. Η μέθοδος αυτή
             περιγράφεται και στο paper του OPAD. Αντιμετωπίζουμε το παράθυρο ως
             ένα μοναδικο έγγραφο.
             */
            word = (String) it.next();
            oldNum = total.get(word);
            num = oldNum * idf.get(word);
            total.put(word, num);
        }
        if (total.size() < topKwors) {
            topKwors = total.size();
        }
        if (totalUrls.size() < topKUrls) {
            topKUrls = totalUrls.size();
        }
        if (totalRetweet.size() < topKTweets) {
            topKTweets = totalRetweet.size();
        }
        /*
        Κατάταξη των score των λέξεων, των url και των πιο retweeted tweets.
        */
        total = sortByDouble(total);
        totalUrls = sortByInteger(totalUrls);
        totalRetweet = sortByInteger(totalRetweet);
        it = total.keySet().iterator();
        itUrl = totalUrls.keySet().iterator();
        itRetweet = totalRetweet.keySet().iterator();
        
        //Δημιουργία αποτελεσμάτων για πέρασμα στο JSP.
        String[] results = new String[3];
        String retweets = "<p/>" + (String) itRetweet.next();
        String words = (String) it.next();
        String temp = (String) itUrl.next();
        String urlResult = "<a href=\"" + temp + "\">" + temp + "</a>";
        for (int i = 1; i < topKwors; ++i) {
            words = words + ", " + (String) it.next();

        }
        for (int i = 1; i < topKUrls; i++) {
            temp = (String) itUrl.next();
            urlResult = urlResult + ", " + "<a href=\"" + temp + "\">" + temp + "</a>";
        }
        for (int i = 1; i < topKTweets; i++) {
            retweets = retweets + "<p/>" + (String) itRetweet.next();
        }
        results[0] = words;
        results[1] = urlResult;
        results[2] = retweets;

        return results;
    }

    private static HashMap<String, Double> sortByDouble(HashMap<String, Double> unsortMap) {

        List list = new LinkedList(unsortMap.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap<String, Double> sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put((String) entry.getKey(), (double) entry.getValue());
        }
        list.clear();
        return sortedMap;
    }

    private static HashMap<String, Integer> sortByInteger(HashMap<String, Integer> unsortMap) {

        List list = new LinkedList(unsortMap.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap<String, Integer> sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put((String) entry.getKey(), (int) entry.getValue());
        }
        list.clear();
        return sortedMap;
    }

}
