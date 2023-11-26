package slug2k.ffapi.clients;

import slug2k.ffapi.Logger;
import slug2k.ffapi.exceptions.PrinterException;
import slug2k.ffapi.exceptions.PrinterTransferException;
import java.io.*;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * TCP Client for communicating with FlashForge printers
 * @author Slugger2k, updated by GhostTypes
 */
public class TcpPrinterClient implements Closeable {
	
	//private final static Logger log = LoggerFactory.getLogger(TcpPrinterClient.class);

	private Socket socket;
	private int port = 8899;
	private int timeout = 25000;
	private String hostname;

	/**
	 * Creates a new TcpPrinterClient with the default port and timeout<br>
	 * Port 8899, 25s timeout
	 * @param hostname The printer's ip
	 */
	public TcpPrinterClient(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Creates a news TcpPrinterClient with a custom port and timeout<br>
	 * All printers should be using port 8899...
	 * @param hostname The printer's ip
	 * @param port Which port to communicate with the printer on
	 * @param timeout General connection timeout in ms
	 */
	public TcpPrinterClient(String hostname, int port, int timeout) {
		this.hostname = hostname;
		this.port = port;
		this.timeout = timeout;
	}

	/**
	 * Sends a command to the printer
	 * @param cmd The Mcode command
	 * @return Mcode command replay
	 * @throws PrinterException Communication error with the printer
	 */
	public String sendCommand(String cmd) throws PrinterException {
		Logger.debug("sendCommand: " + cmd);
		try {
			checkSocket();
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println(cmd);
			return receiveMultiLineReplay(socket);
		} catch (NoRouteToHostException e) { throw new PrinterTransferException("Error while connecting. No route to host ["  + socket.getInetAddress().getHostAddress() + "].");
		} catch (UnknownHostException e) { throw new PrinterTransferException("Error while connecting. Unknown host ["  + socket.getInetAddress().getHostAddress() + "]."); }
		catch (IOException e) { throw new PrinterException("Error while building or writing output stream.", e); }
	}

	public void sendRawData(List<byte[]> rawData) throws PrinterException {
		try {
			checkSocket();
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			for (byte[] bs : rawData) {
				Logger.debug("Send data: " + bs.length + " bytes");
				dos.write(bs);
				String replay = receiveSingleLineReplay(socket);
				Logger.debug(replay);
				if (!replay.matches("N\\d{4,}\\sok.")) throw new PrinterTransferException("Error while transferring data.");
			}
		} catch (NoRouteToHostException e) { throw new PrinterTransferException("Error while connecting. No route to host ["  + socket.getInetAddress().getHostAddress() + "].");}
		catch (UnknownHostException e) { throw new PrinterTransferException("Error while connecting. Unknown host ["  + socket.getInetAddress().getHostAddress() + "]."); }
		catch (IOException e) { throw new PrinterException("Error while building or writing output stream.", e); }
	}

	private void checkSocket() throws IOException {
		boolean fix = false;
		if (socket == null) {
			fix = true;
			Logger.debug("TcpPrinterClient socket is null");
		} else if (socket.isClosed()) {
			fix = true;
			Logger.debug("TcpPrinterClient socket is closed");
		}

		if (fix) {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(timeout);
		}
	}

	public String receiveMultiLineReplay(Socket socket) throws PrinterException {
		var answer = new StringBuilder();
		BufferedReader reader;
		try {
			InputStream input = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));

			String line;
			while ((line = reader.readLine()) != null) {
				answer.append(line).append("\n");
				if (line.equalsIgnoreCase("ok")) {
					break;
				}
			}
		} catch (IOException e) { throw new PrinterException("Error while building or reading input stream.", e); }
		return answer.toString().trim();
	}

	public String receiveSingleLineReplay(Socket socket) throws PrinterException {
		BufferedReader reader;
		try {
			InputStream input = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));
			String line = reader.readLine();
			return line.trim();
		} catch (IOException e) { throw new PrinterException("Error while building or reading input stream.", e); }
	}

	@Override
	public void close() {
		try {
			Logger.debug("TcpPrinterClient closing socket");
			socket.close();			
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}
}