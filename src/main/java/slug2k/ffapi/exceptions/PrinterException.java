package slug2k.ffapi.exceptions;

public class PrinterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PrinterException(String message) {
		super(message);
	}

	public PrinterException(String message, Throwable e) {
		super(message, e);
	}
}
