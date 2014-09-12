package trends;

public class GoogleTrendsRequestException extends Exception {

	public GoogleTrendsRequestException() {
	  }

	  /**
	   * Constructs an instance of <code>GoogleTrensRequestException</code> with the
	   * specified detail message.
	   *
	   * @param msg the detail message.
	   */
	  public GoogleTrendsRequestException(String msg) {
	    super(msg);
	  }

	  /**
	   * Constructs an instance of <code>GoogleTrensRequestException</code> with the
	   * specified exception.
	   *
	   * @param e the detail message.
	   */
	  public GoogleTrendsRequestException(Exception e) {
	    super(e);
	  }
}
