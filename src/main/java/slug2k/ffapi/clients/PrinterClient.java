package slug2k.ffapi.clients;

import slug2k.ffapi.Logger;
import slug2k.ffapi.commands.extra.PrintReport;
import slug2k.ffapi.commands.info.LocationInfo;
import slug2k.ffapi.commands.info.PrinterInfo;
import slug2k.ffapi.commands.info.TempInfo;
import slug2k.ffapi.commands.status.EndstopStatus;
import slug2k.ffapi.commands.status.PrintStatus;
import slug2k.ffapi.enums.MachineStatus;
import slug2k.ffapi.enums.MoveMode;
import slug2k.ffapi.exceptions.PrinterException;
import slug2k.ffapi.exceptions.PrinterTransferException;
import slug2k.ffapi.util.Util;
import slug2k.ffapi.commands.PrinterCommands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Client for communicating with FlashForge printers
 * @author Slugger2k, updated by GhostTypes
 */
public class PrinterClient extends TcpPrinterClient {

	/**
	 * Creates a new PrinterClient
	 * @param hostname The printer's ip
	 */
	public PrinterClient(String hostname) {
		super(hostname);
	}


	/**
	 * Uploads a local print file to the printer, and starts printing it
	 * @param file The file to print
	 * @return boolean
	 */
	public boolean print(File file) {
		String filename = file.getName();
		if (!filename.endsWith(".gx") && !filename.endsWith(".gcode")) {
			Logger.error("print() invalid file provided: " + file.getPath());
			return false;
		}
		try {
			byte[] data = Files.readAllBytes(file.toPath());
			return print(filename, data);
		} catch (IOException e) {
			Logger.error("print() IOException while reading file: " + file.getPath());
			e.printStackTrace();
			return false;
		} catch (PrinterException e) {
			Logger.error("print() PrinterException while starting print with file: " + file.getPath());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Handles sending a file to the printer for printing.<br>
	 * Sends an M28 command with the file name, M29 with the file data, and M23 to start.
	 * @param filename The name of the file to print
	 * @param data The file data as byte[]
	 * @return boolean
	 * @throws PrinterException Communication error with the printer, or the printer's firmware rejected the file
	 */
	private boolean print(String filename, byte[] data) throws PrinterException {
		Logger.debug("print() File " + filename + " size " + data.length);
		Logger.debug(sendCommand(PrinterCommands.CMD_PREPARE_PRINT.replaceAll("%%size%%", "" + data.length).replaceAll("%%filename%%", filename)));

		try {
			List<byte[]> gcode = Util.prepareRawData(data);
			sendRawData(gcode);
			Logger.debug(sendCommand(PrinterCommands.CMD_SAVE_FILE));
			Logger.debug(sendCommand(PrinterCommands.CMD_PRINT_START.replaceAll("%%filename%%", filename)));
			return true;
		} catch (PrinterTransferException e) {
			Logger.error(e.getMessage());
		}

		return false;
	}

	/**
	 * Turns the printer's LEDs on/off
	 * @param on What state to set the LEDs to
	 * @return boolean
	 * @throws PrinterException Communication error
	 */
	public boolean setLed(boolean on) throws PrinterException {
		String replay = sendCommand(on ? PrinterCommands.CMD_LED_ON : PrinterCommands.CMD_LED_OFF);
		return replay.contentEquals("CMD M146 Received.\nok");
	}

	/**
	 * Gets the current PrinterInfo
	 * @return PrinterInfo instance
	 * @throws PrinterException Communication error
	 * @see slug2k.ffapi.commands.info.PrinterInfo
	 */
	public PrinterInfo getPrinterInfo() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_INFO_STATUS).trim();
		return new PrinterInfo(replay);
	}

	// The following code was added for/tested with the Adventurer 5M Pro

	/**
	 * A safe method to reliably stop prints
	 * @author GhostTypes
	 * @throws PrinterException Communication error with the printer
	 */
	public void stopPrint() throws PrinterException {
		Logger.log("Stopping current print");
		sendCommand(PrinterCommands.CMD_PRINT_STOP);
		Logger.log("Ensuring print is stopped successfully");
		while (!isPrintingStopped()) {
			Logger.log("Print still running, sending M26 again.");
			sendCommand(PrinterCommands.CMD_PRINT_STOP);
			try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
		}
		Logger.log("Print stopped successfully");
	}

	/**
	 * Checks if the printer is stopped (after a print job)<br>
	 * Use isReady() to see if it's ready for a new job
	 * @return boolean
	 * @throws PrinterException Communication error with the printer
	 */
	private boolean isPrintingStopped() throws PrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		return endstopStatus.machineStatus == MachineStatus.READY || endstopStatus.machineStatus == MachineStatus.BUILDING_COMPLETED;
	}

	/**
	 * Checks if the printer is working on a job
	 * @return boolean
	 * @throws PrinterException Communication error with the printer
	 */
	public boolean isPrinting() throws PrinterException {
		return getMachineStatus() == MachineStatus.BUILDING_FROM_SD;
	}

	/**
	 * Checks if the printer is ready to start a new job
	 * @return boolean
	 * @throws PrinterException Communication error
	 */
	public boolean isReady() throws PrinterException {
		EndstopStatus es = getEndstopStatus();
		return es.moveMode == MoveMode.READY && es.machineStatus == MachineStatus.READY;
	}

	/**
	 * Gets the current MachineStatus
	 * @return MachineStatus
	 * @throws PrinterException Communication error
	 * @see slug2k.ffapi.enums.MachineStatus
	 */
	public MachineStatus getMachineStatus() throws PrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		return endstopStatus.machineStatus;
	}

	/**
	 * Gets the current MoveMode
	 * @return MoveMode
	 * @throws PrinterException Communication error
	 * @see slug2k.ffapi.enums.MoveMode
	 */
	public MoveMode getMoveStatus() throws PrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		return endstopStatus.moveMode;
	}

	/**
	 * Gets the printer's temps
	 * @return TempInfo instance
	 * @throws PrinterException Communication error
	 * @see slug2k.ffapi.commands.info.TempInfo
	 */
	public TempInfo getTempInfo() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_TEMP).trim();
		return new TempInfo(replay);
	}

	/**
	 * Gets the printer's current coordinates
	 * @return LocationInfo instance
	 * @throws PrinterException Communication error
	 * @see slug2k.ffapi.commands.info.LocationInfo
	 */
	public LocationInfo getLocationInfo() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_INFO_XYZAB).trim();
		return new LocationInfo(replay);
	}

	/**
	 * Gets the status of the current print
	 * @return PrintStatus instance
	 * @throws PrinterException Communication error
	 * @see slug2k.ffapi.commands.status.PrintStatus
	 */
	public PrintStatus getPrintStatus() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_PRINT_STATUS).trim();
		return new PrintStatus(replay);
	}


	private EndstopStatus lastEndstopStatus = null;
	/**
	 * Gets the current Endstop status, MachineStatus, MoveMode, Status, Led (on/off)
	 * and current file name
	 * @return EndstopStatus instance
	 * @throws PrinterException Communication error
	 * @see slug2k.ffapi.commands.status.EndstopStatus
	 */
	public EndstopStatus getEndstopStatus() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_ENDSTOP_INFO).trim();
		try {
			EndstopStatus status = new EndstopStatus(replay);
			lastEndstopStatus = status;
			return status;
		} catch (ArrayIndexOutOfBoundsException e) {
			Logger.log("getEndstopStatus error: " + e.getMessage());
			return lastEndstopStatus; // not sure if this is a *good* fix
		}
	}

	/**
	 * Generates a PrintReport<br>
	 * Used for sending status updates to Discord
	 * @return PrintReport instance
	 * @throws PrinterException Communication error
	 */
	public PrintReport getPrintReport() throws PrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		PrintStatus printStatus = getPrintStatus();
		TempInfo tempInfo = getTempInfo();
		return new PrintReport(endstopStatus, printStatus, tempInfo);
	}

}
