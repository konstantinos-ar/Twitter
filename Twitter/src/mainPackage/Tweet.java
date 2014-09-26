
package mainPackage;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.URLEntity;

/**
 *
 * @author ka
 */
public class Tweet {

    private Status status;
    private HashtagEntity[] hashtags;
    private URLEntity[] origUrls, origMedia;
    private final Version version = Version.LUCENE_48;
    private Analyzer analyzer;
    private StringReader sr;
    private TokenStream ts;
    private OffsetAttribute offsetAtt;
    private CharTermAttribute termAtt;
    private final HashMap<String, Integer> tokens;
    private int totalTokens = 0;
    private long id;

    /*
    Κλάση που αναπαριστά ένα tweet και εξάγει τη πληροφορία που χρειαζομαστε.
    */
    public Tweet(String rawJson) throws TwitterException, IOException {
        this.analyzer = new StopAnalyzer(version, new File("C:/Users/user/workspace/Twitter/ND_Stop_Words_Generic.txt"));
        status = TwitterObjectFactory.createStatus(rawJson);
        hashtags = status.getHashtagEntities();
        origUrls = status.getURLEntities();
        origMedia = status.getMediaEntities();
        id = status.getId();
        tokens = findTokens();
    }

    //To status ID του εκάστοτε tweet
    public long getId() {
        return id;
    }

    //Το ίδιο το αντικείμενο τυπου status
    public Status getStatus() {
        return status;
    }

    //Το κείμενο του status
    public String getStatusText() {
        return status.getText();
    }

    //λίστα με μοναδικά token του status
    public HashMap<String, Integer> getTokens() throws IOException {
        return tokens;
    }

    /*
    Αφαίρεση των URL απο το κείμενο του Tweet. Με τη μέθοδο expand βρίσκουμε τον
    συνδεσμο στον οποίο δείχνει το URL, οταν δεν είναι κατηγοριοποιημένο ως 
    media url.
    */
    private String removeUrls(String status) throws IOException {

        String origUrl;
        if (origUrls.length > 0) {
            for (int i = 0; i < origUrls.length; i++) {
                origUrl = origUrls[i].getURL();
                status = status.replace(origUrl, "");
                origUrl = expandShortURL(origUrl);
                status = status.replace(origUrl, "");
            }
        }
        String media;
        if (origMedia.length > 0) {
            for (int i = 0; i < origMedia.length; i++) {
                media = origMedia[i].getURL();
                status = status.replace(media, "");
            }
        }

        return status;
    }

    /*
    Διαδικασία ευρεσης των token του κειμένου. Με χρήση της Lucene εξάγουμε τα 
    tokens αφου πρωτα έχουμε κάνει ολα τα γράμματα μικρά και έχουμε αφαιρέσει τα
    stopword.
    */
    private HashMap<String, Integer> findTokens() throws IOException {
        String text = status.getText();
        HashMap<String, Integer> docTf = new HashMap<>();
        text = removeUrls(text);
        sr = new StringReader(text);
        ts = analyzer.tokenStream("irrelevant", sr);
        offsetAtt = ts.addAttribute(OffsetAttribute.class);
        termAtt = ts.addAttribute(CharTermAttribute.class);

        ts.reset();
        while (ts.incrementToken()) {
            int startOffset = offsetAtt.startOffset();
            int endOffset = offsetAtt.endOffset();

            String term = termAtt.toString();
            if (docTf.get(term) != null) {
                int value = docTf.get(term);
                value++;
                docTf.put(term, value);
            } else {
                docTf.put(term, 1);
            }
            totalTokens++;
        }
        ts.close();
        sr.close();
        return docTf;
    }
    
    /*
    Ενημέρωση του συνολικου DF για ολη τη συλλογή με το προς εξέταση tweet.
    */
    public void updateDf(HashMap<String, Integer> totaldf) throws IOException {

        Set<String> tokenKeySet = tokens.keySet();
        Iterator<String> tokenIt = tokenKeySet.iterator();
        String token;
        int docValue, oldValue;
        while (tokenIt.hasNext()) {
            token = tokenIt.next();
            docValue = tokens.get(token);
            if (totaldf.get(token) != null) {
                oldValue = totaldf.get(token);
                oldValue = oldValue + docValue;
                totaldf.put(token, oldValue);
            } else {
                totaldf.put(token, docValue);
            }
        }
    }

    //Μέθοδος "αναδίπλωσης" των URL.
    public String expandShortURL(String address) throws IOException {
        URL url = new URL(address);
        HttpURLConnection connection = 
                (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        String expandedURL = connection.getHeaderField("Location");
        connection.getInputStream().close();
        return expandedURL;
    }

}
