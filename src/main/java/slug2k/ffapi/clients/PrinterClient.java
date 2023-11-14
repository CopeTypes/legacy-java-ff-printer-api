package slug2k.ffapi.clients;

import slug2k.ffapi.Logger;
import slug2k.ffapi.commands.extra.PrintReport;
import slug2k.ffapi.commands.info.LocationInfo;
import slug2k.ffapi.commands.info.PrinterInfo;
import slug2k.ffapi.commands.info.TempInfo;
import slug2k.ffapi.commands.status.EndstopStatus;
import slug2k.ffapi.commands.status.PrintStatus;
import slug2k.ffapi.enums.MachineStatus;
import slug2k.ffapi.exceptions.PrinterException;
import slug2k.ffapi.exceptions.PrinterTransferException;
import slug2k.ffapi.util.Util;
import slug2k.ffapi.commands.PrinterCommands;

import java.util.List;


public class PrinterClient extends TcpPrinterClient {

	//Credit to the original author for this stuff
	public PrinterClient(String hostname) {
		super(hostname);
	}

	public boolean print(String filename, byte[] readAllLines) throws PrinterException {
		//Logger.log("File: {}/{} byte", filename, readAllLines.length);
		Logger.log("File " + filename + " size " + readAllLines.length);

		Logger.log(sendCommand(PrinterCommands.CMD_PREPARE_PRINT.replaceAll("%%size%%", "" + readAllLines.length)
				.replaceAll("%%filename%%", filename)));

		try {
			List<byte[]> gcode = Util.prepareRawData(readAllLines);
			sendRawData(gcode);
			Logger.log(sendCommand(PrinterCommands.CMD_SAVE_FILE));
			Logger.log(sendCommand(PrinterCommands.CMD_PRINT_START.replaceAll("%%filename%%", filename)));
			return true;
		} catch (PrinterTransferException e) {
			Logger.error(e.getMessage());
		}

		return false;
	}

	public boolean setLed(boolean on) throws PrinterException {
		String replay = sendCommand(on ? PrinterCommands.CMD_LED_ON : PrinterCommands.CMD_LED_OFF);
		Logger.log(replay);
		return replay.contentEquals("CMD M146 Received.\nok");
	}

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

	private boolean isPrintingStopped() throws PrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		return endstopStatus.machineStatus == MachineStatus.READY || endstopStatus.machineStatus == MachineStatus.BUILDING_COMPLETED;
	}

	public boolean isPrinting() throws PrinterException {
		return getMachineStatus() == MachineStatus.BUILDING_FROM_SD;
	}


	public MachineStatus getMachineStatus() throws PrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		return endstopStatus.machineStatus;
	}

	public TempInfo getTempInfo() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_TEMP).trim();
		return new TempInfo(replay);
	}

	public LocationInfo getLocationInfo() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_INFO_XYZAB).trim();
		return new LocationInfo(replay);
	}

	public PrintStatus getPrintStatus() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_PRINT_STATUS).trim();
		return new PrintStatus(replay);
	}

	public EndstopStatus getEndstopStatus() throws PrinterException {
		String replay = sendCommand(PrinterCommands.CMD_ENDSTOP_INFO).trim();
		return new EndstopStatus(replay);
	}

	public PrintReport getPrintReport() throws PrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		PrintStatus printStatus = getPrintStatus();
		TempInfo tempInfo = getTempInfo();
		return new PrintReport(endstopStatus, printStatus, tempInfo);
	}

}
