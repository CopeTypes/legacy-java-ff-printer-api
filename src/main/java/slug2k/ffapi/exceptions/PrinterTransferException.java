package slug2k.ffapi.exceptions;

import java.io.Serial;

public class PrinterTransferException extends PrinterException {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Error with transferring a file/files to the printer
	 * @param message The error message
	 */
	public PrinterTransferException(String message) {
		super(message);
	}
	
}
