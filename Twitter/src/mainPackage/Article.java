package mainPackage;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DBObject;

public class Article {
	
	private String _url;
	private String _snippet;
	private String _lead_paragraph;
	private String _abstract;
	private String _source;
	private String _headline_main;
	private String _headline_kicker;
	private ArrayList<String> _keywords = new ArrayList<String>();
	private String _pubdate;
	private String _doctype;
	private String _newsdesk;
	private String _section;
	private String _subsection;
	private String _typeof;
	private String _id;
	private int _wordcount;
	
	private final Version version = Version.LUCENE_48;
	private Analyzer analyzer;
    private StringReader sr;
    private TokenStream ts;
    private OffsetAttribute offsetAtt;
    private CharTermAttribute termAtt;
    private final HashMap<String, Integer> tokens;
    private int totalTokens = 0;
    private long id;
	
	public Article (DBObject o) throws IOException
	{
		JSONObject vo = null;
		JSONObject vo2 = null;
		JSONArray ar = null;
		//BSONObject bo = null;
		//BasicBSONDecoder decoder = new BasicBSONDecoder();

		try {
			//bo.putAll(o);
			vo = new JSONObject(o.toString());
		
			_url = vo.optString("web_url").replace("\"", "");
			_snippet = vo.get("snippet").toString();
			_lead_paragraph = vo.optString("lead_paragraph");
			_abstract = vo.optString("abstract");
			_source = vo.optString("source");
			_headline_main = vo.optJSONObject("headline").optString("main");
			_headline_kicker = vo.optJSONObject("headline").optString("kicker");
			for (int i = 0; i < vo.getJSONArray("keywords").length(); i++)
				_keywords.add(vo.getJSONArray("keywords").getJSONObject(i).optString("value"));
			_pubdate = vo.opt("pub_date").toString();
			_doctype = vo.optString("document_type");
			_newsdesk = vo.optString("news_desk");
			_section = vo.optString("section_name");
			_subsection = vo.optString("subsection_name");
			_typeof = vo.optString("type_of_material");
			_id = vo.optString("_id");
			_wordcount = Integer.parseInt(vo.optString("word_count"));
			
			this.analyzer = new StopAnalyzer(version, new File("C:/Users/user/git/Twitter/Twitter/ND_Stop_Words_Generic.txt"));
			
			
			
		
/*		_url = o.get("web_url").toString();
		_snippet = o.get("snippet").toString();
		_lead_paragraph = o.get("lead_paragraph").toString();
		_abstract = o.get("abstract").toString();
		_source = o.get("source").toString();
		vo = new JSONObject(o.get("headline"));
		_headline_main = vo.optString("main");//vo.optJSONObject("headline").optString("main");
		_headline_main = vo.optString("kicker");
		//vo2 = new JSONObject(o.get("headline"));
		ar = (JSONArray) o.get("keywords");
		for (int i = 0; i < ar.length(); i++)
			_keywords.add(ar.getJSONObject(i).optString("value"));
		_pubdate = (Date) o.get("pub_date");
		_doctype = o.get("doc_type").toString();
		_newsdesk = o.get("news_desk").toString();
		_section = o.get("section").toString();
		_subsection = o.get("subsection").toString();
		_typeof = o.get("type_of").toString();
		_id = (long) vo.get("_id");
		_wordcount = (int) o.get("wordcount");
*/		
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tokens = findTokens();
	}
	
	public String getId()
	{
		return _id;
	}
	
	public String getUrl()
	{
		return _url;
	}
	
	public String getSnippet()
	{
		return _snippet.toLowerCase();
	}
	
	public String getLeadparagraph()
	{
		return _lead_paragraph.toLowerCase();
	}
	
	public String getAbstract()
	{
		return _abstract.toLowerCase();
	}
	
	public String getText()
	{
		return !_abstract.equals("") ? _abstract.toLowerCase() : _lead_paragraph.toLowerCase();
	}
	
	public String getSource()
	{
		return _source;
	}
	
	public String getHeadlinemain()
	{
		return _headline_main;
	}
	
	public String getHeadlinekicker()
	{
		return _headline_kicker;
	}
	
	public ArrayList<String> getKeywords()
	{
		return _keywords;
	}
	
	public String getDate()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
		Date dd = null;
		try {
				dd = formatter2.parse(_pubdate.substring(10, 34));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String d = formatter.format(dd);
				//d2 = formatter.format(dd2);
		
		return d;
	}

	public String getDoctype()
	{
		return _doctype;
	}
	
	public String getNewsdesk()
	{
		return _newsdesk;
	}
	
	public String getSection()
	{
		return _section;
	}
	
	public String getSubsection()
	{
		return _subsection;
	}
	
	public String getType()
	{
		return _typeof;
	}
	
	public int getWordcount()
	{
		return _wordcount;
	}
	
	public HashMap<String, Integer> getTokens() throws IOException {
        return tokens;
    }
	
	private HashMap<String, Integer> findTokens() throws IOException {
        String text = _abstract;
        HashMap<String, Integer> docTf = new HashMap<>();
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
	
}
