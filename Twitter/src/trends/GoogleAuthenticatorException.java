package trends;

public class GoogleAuthenticatorException extends Exception {

	public GoogleAuthenticatorException() {
	  }

	  /**
	   * Constructs an instance of <code>GoogleAuthenticatorException</code> with
	   * the specified detail message.
	   *
	   * @param msg the detail message.
	   */
	  public GoogleAuthenticatorException(String msg) {
	    super(msg);
	  }

	  /**
	   * Constructs an instance of <code>GoogleAuthenticatorException</code> with
	   * the specified exception.
	   *
	   * @param e the detail message.
	   */
	  public GoogleAuthenticatorException(Exception e) {
	    super(e);
	  }
	
}
