package trends;

public class GoogleTrendsClientException extends Exception {

	public GoogleTrendsClientException() {
	  }

	  /**
	   * Constructs an instance of <code>GoogleTrendsClientException</code> with the
	   * specified detail message.
	   *
	   * @param msg the detail message.
	   */
	  public GoogleTrendsClientException(String msg) {
	    super(msg);
	  }

	  /**
	   * Constructs an instance of <code>GoogleTrendsClientException</code> with the
	   * specified exception.
	   *
	   * @param e the detail message.
	   */
	  public GoogleTrendsClientException(Exception e) {
	    super(e);
	  }
	
}
