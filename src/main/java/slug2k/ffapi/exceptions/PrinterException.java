package slug2k.ffapi.exceptions;

import java.io.Serial;

public class PrinterException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Generic communication error with the printer
	 * @param message The error message
	 */
	public PrinterException(String message) {
		super(message);
	}

	public PrinterException(String message, Throwable e) {
		super(message, e);
	}
}
