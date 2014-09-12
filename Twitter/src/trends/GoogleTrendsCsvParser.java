package trends;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;

public class GoogleTrendsCsvParser {

	  private static final String SECTION_TOP_SEARCHES_FOR = "Top searches for";
	  private final String _csv;
	  private String _separator;

	  /**
	   *
	   * @param csv
	   * @throws org.apache.commons.configuration.ConfigurationException
	   */
	  public GoogleTrendsCsvParser(String csv) throws ConfigurationException {
	    this._csv = csv;
	    this._separator = GoogleConfigurator.getConfiguration().getString("google.csv.separator");
	  }

	  /**
	   * Gets the specified section from the CSV.
	   *
	   * @param section Section of the CSV to retrieve
	   * @param header If the section has a header. If <code>true</code> the first
	   * line will be skipped.
	   * @return content The content of the section
	   */
	  public String getSectionAsString(String section, boolean header) {
	    String ret = null;
	    Logger.getLogger(GoogleConfigurator.getLoggerPrefix()).log(Level.FINE, "Parsing CSV for section: {0}", section);

	    Pattern startSectionPattern = Pattern.compile("^" + section + ".*$", Pattern.MULTILINE);
	    Matcher matcher = startSectionPattern.matcher(_csv);

	    if (matcher.find()) {
	      ret = _csv.subSequence(matcher.start(), _csv.length()).toString();

	      int end = ret.length();

	      Pattern endSectionPattern = Pattern.compile("\n\n", Pattern.MULTILINE);
	      matcher = endSectionPattern.matcher(ret);
	      if (matcher.find()) {
	        end = matcher.start();
	      }

	      ret = ret.subSequence(0, end).toString().substring(ret.indexOf('\n') + 1);

	      if (header) {
	        ret = ret.substring(ret.indexOf('\n') + 1);
	      }
	    } else {
	      Logger.getLogger(GoogleConfigurator.getLoggerPrefix()).log(Level.WARNING, "Writing the full CSV file. Section not found: #{0}", section);
	      return "";
	    }

	    return ret;
	  }

	  /**
	   * Gets the specified section from the CSV as list of <code>String[]</code>.
	   *
	   * @param section Section of the CSV to retrieve
	   * @param header If the section has a header. If <code>true</code> the first
	   * line will be skipped.
	   * @param fieldSep Field _separator for each line
	   * @return An <code>ArrayList</code> of <code>String[]</code> where each
	   * element in the <code>ArrayList</code> is a row and each <code>String</code>
	   * is a column
	   * @throws IOException
	   */
	  public List<String[]> getSectionAsStringArrayList(String section, boolean header, String fieldSep) throws IOException {
	    List<String[]> ret = new ArrayList<String[]>();
	    BufferedReader r = new BufferedReader(new StringReader(getSectionAsString(section, header)));

	    String line;
	    while ((line = r.readLine()) != null) {
	      ret.add(line.split(fieldSep));
	    }

	    return ret;
	  }

	  /**
	   * Retrieve the CSV section as list of <code>String</code>.
	   *
	   * @param section Section of the CSV to retrieve
	   * @param header If the section has a header. If <code>true</code> the first
	   * line will be skipped.
	   * @param fieldSep Field _separator for each line
	   * @return An <code>ArrayList</code> of <code>String</code> where each
	   * <code>String</code> element in the <code>ArrayList</code> is a row
	   * @throws IOException
	   */
	  public List<String> getSectionAsStringList(String section, boolean header, String fieldSep) throws IOException {
	    List<String> ret = new ArrayList<String>();
	    BufferedReader r = new BufferedReader(new StringReader(getSectionAsString(section, header)));

	    String line;
	    while ((line = r.readLine()) != null) {
	      ret.add(line);
	    }

	    return ret;
	  }

	  /**
	   * @return the _separator
	   */
	  public String getSeparator() {
	    return _separator;
	  }

	  /**
	   * @param separator the _separator to set
	   */
	  public void setSeparator(String separator) {
	    this._separator = separator;
	  }

	  /**
	   * Retrieve the whole CSV document.
	   *
	   * @return The CSV file.
	   */
	  public String getCsv() {
	    return this._csv;
	  }

	  public LinkedList<String> getTopSearches(int max) {
	    LinkedList<String> res = new LinkedList<String>();
	    List<String> topSearches;
	    try {
	      topSearches = getSectionAsStringList(SECTION_TOP_SEARCHES_FOR, false, _separator);
	    } catch (IOException ex) {
	      Logger.getLogger(GoogleTrendsCsvParser.class.getName()).log(Level.WARNING, "Cannot find Top Search section", ex);
	      return res;
	    }
	    for (String col : topSearches) {
	      res.add(col.split(_separator)[0]);
	      if (res.size() >= max){
	        return res;
	      }
	    }
	    return res;
	  }
	}
