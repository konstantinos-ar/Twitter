package mainPackage;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DBObject;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

public class Article
{
	
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
	private String _stemmed;
	private ArrayList<String> _stopList = new ArrayList<String>();
	//MaxentTagger tagger;
	
	private final Version version = Version.LUCENE_48;
	private Analyzer analyzer;
    private StringReader sr;
    private TokenStream ts;
    //private OffsetAttribute offsetAtt;
    private CharTermAttribute termAtt;
    //private final HashMap<String, Integer> tokens;
    //private final PorterStemmer stemmer = new PorterStemmer();
    //private int totalTokens = 0;
    //private long id;
	
	public Article (DBObject o, MaxentTagger tagger) throws IOException
	{
		JSONObject vo = null;
		//JSONObject vo2 = null;
		//JSONArray ar = null;
		//BSONObject bo = null;
		//BasicBSONDecoder decoder = new BasicBSONDecoder();

		try
		{
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
			
			//this.analyzer = new StopAnalyzer(version, new File("C:/Users/user/git/Twitter/Twitter/ND_Stop_Words_Generic.txt"));
			
			_stopList.add("C:/Users/user/git/Twitter/Twitter/stop.txt");
			//_stopList.add("C:/Users/user/git/Twitter/Twitter/ND_Stop_Words_Generic.txt");
			_stopList.add("C:/Users/user/git/Twitter/Twitter/ND_Stop_Words_Currencies.txt");
			_stopList.add("C:/Users/user/git/Twitter/Twitter/ND_Stop_Words_Geographic.txt");
			_stopList.add("C:/Users/user/git/Twitter/Twitter/ND_Stop_Words_Names.txt");
			_stopList.add("C:/Users/user/git/Twitter/Twitter/ND_Stop_Words_DatesandNumbers.txt");
			
			//this.tagger = tagger;
			_stemmed = calcStemmed();
			
		
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
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//tokens = findTokens();
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
	
	private String getText()
	{
		//return _abstract.length()>_lead_paragraph.length() ? _abstract.toLowerCase() : _lead_paragraph.toLowerCase();
		if (_abstract.length()>_lead_paragraph.length())
			return _abstract.toLowerCase();
		else if (_abstract.length()<_lead_paragraph.length())
			return _lead_paragraph.toLowerCase();
		else if (_snippet.length() > 10)
			return _snippet.toLowerCase();
		else
			return _headline_main.toLowerCase();
	}
	
	public String getStemmed()
	{
		if (_stemmed == null)
			return null;
		return _stemmed.trim();
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
		try
		{
			dd = formatter2.parse(_pubdate.substring(10, 34));
		}
		catch (ParseException e)
		{
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
	
/*	public HashMap<String, Integer> getTokens() throws IOException {
        return tokens;
    }
*/	
/*	private HashMap<String, Integer> findTokens() throws IOException {
        String text = getText();
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
 */   
    /*
    Ενημέρωση του συνολικου DF για ολη τη συλλογή με το προς εξέταση tweet.
    */
/*    public void updateDf(HashMap<String, Integer> totaldf) throws IOException {

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
*/    
    public String calcStemmed()
    {
    	String text = getText();
    	String s = null, t = null;
    	sr = new StringReader(text);
        
    	try
    	{
    		System.out.println("Orig is : " + text);

    		for (int i = 0; i < _stopList.size(); i++)
    		{
    			this.analyzer = new StopAnalyzer(version, new File(_stopList.get(i)));
    			ts = analyzer.tokenStream("irrelevant", sr);

    			//offsetAtt = ts.addAttribute(OffsetAttribute.class);
    			termAtt = ts.addAttribute(CharTermAttribute.class);

    			ts.reset();
    			s = null;

    			while (ts.incrementToken())
    			{
    				//int startOffset = offsetAtt.startOffset();
    				//int endOffset = offsetAtt.endOffset();

    				String term = termAtt.toString();
    				//stemmer.setCurrent(term);
    				//stemmer.stem();
    				//term = stemmer.getCurrent();

    				if ( s == null)
    					s = term + " ";
    				else
    					s = s + term + " ";
    			}

    			sr = new StringReader(s.trim());
    		}
    		//System.out.println("Stem is : " + s);
    		Properties props = new Properties(); 
    		props.put("annotators", "tokenize, ssplit, pos, lemma"); 
    		StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
    		String text2 = s.trim();
    		s = null;
    		Annotation document = pipeline.process(text2);  
    		for(CoreMap sentence: document.get(SentencesAnnotation.class))
    		{    
    			for(CoreLabel token: sentence.get(TokensAnnotation.class))
    			{       
    				//String word = token.get(TextAnnotation.class);      
    				String lemma = token.get(LemmaAnnotation.class); 
    				if ( s == null)
    					s = lemma + " ";
    				else
    					s = s + lemma + " ";
    				 //System.out.println("lemmatized version :" + lemma);
    			}
    		}
    		/*List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(s.trim()));
    		s = null;
			for(List<HasWord> sentence: sentences)
			{ 
				ArrayList<TaggedWord> tagged = tagger.tagSentence(sentence); 
				for(TaggedWord word: tagged)
				{ 
					if ( word.tag().startsWith("V"))
						t = "v";
					else if ( word.tag().startsWith("N"))
						t = "n";
					else if ( word.tag().startsWith("R"))
						t = "r";
					else if ( word.tag().startsWith("J"))
						t = "a";
					else
						t = "w";
					if ( s == null)
    					s = word.value() + "#" + t + " ";
    				else
    					s = s + word.value() + "#" + t + " ";
					System.out.println("Tag: " + t); 
					System.out.println("Value: " + word.value()); 
				}
			}*/
    		ts.close();

    	}
    	catch (IOException e)
    	{
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	sr.close();
    	return s.trim();
    }
	
}
