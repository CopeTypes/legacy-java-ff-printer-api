package de.slg.ddnss.printertool.clients;

import de.slg.ddnss.printertool.Logger;
import de.slg.ddnss.printertool.commands.*;
import de.slg.ddnss.printertool.enums.MachineStatus;
import de.slg.ddnss.printertool.exceptions.FlashForgePrinterException;
import de.slg.ddnss.printertool.exceptions.FlashForgePrinterTransferException;
import de.slg.ddnss.printertool.util.Util;

import java.util.List;

import static de.slg.ddnss.printertool.commands.AdventurerCommands.*;


public class AdventurerClient extends TcpPrinterClient {

	//Credit to the original author for this stuff
	public AdventurerClient(String hostname) {
		super(hostname);
	}

	public boolean print(String filename, byte[] readAllLines) throws FlashForgePrinterException {
		//Logger.log("File: {}/{} byte", filename, readAllLines.length);
		Logger.log("File " + filename + " size " + readAllLines.length);

		Logger.log(sendCommand(CMD_PREPARE_PRINT.replaceAll("%%size%%", "" + readAllLines.length)
				.replaceAll("%%filename%%", filename)));

		try {
			List<byte[]> gcode = Util.prepareRawData(readAllLines);
			sendRawData(gcode);
			Logger.log(sendCommand(CMD_SAVE_FILE));
			Logger.log(sendCommand(CMD_PRINT_START.replaceAll("%%filename%%", filename)));
			return true;
		} catch (FlashForgePrinterTransferException e) {
			Logger.error(e.getMessage());
		}

		return false;
	}

	public boolean setLed(boolean on) throws FlashForgePrinterException {
		String replay = sendCommand(on ? CMD_LED_ON : CMD_LED_OFF);
		Logger.log(replay);
		return replay.contentEquals("CMD M146 Received.\nok");
	}

	public PrinterInfo getPrinterInfo() throws FlashForgePrinterException {
		String replay = sendCommand(CMD_INFO_STATUS).trim();
		return new PrinterInfo(replay);
	}

	// The following code was added for/tested with the Adventurer 5M Pro

	/**
	 * A safe method to reliably stop prints
	 * @author GhostTypes
	 * @throws FlashForgePrinterException Communication error with the printer
	 */
	public void stopPrint() throws FlashForgePrinterException {
		Logger.log("Stopping current print");
		sendCommand(CMD_PRINT_STOP);
		EndstopStatus endstopStatus = getEndstopStatus();
		Logger.log("Ensuring print is stopped successfully");
		while (endstopStatus.machineStatus != MachineStatus.READY) {
			Logger.log("Print still running, sending M26 again.");
			sendCommand(CMD_PRINT_STOP);
			endstopStatus = getEndstopStatus();
			try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
		}
		Logger.log("Print stopped successfully");
	}

	public boolean isPrinting() throws FlashForgePrinterException {
		return getMachineStatus() == MachineStatus.BUILDING_FROM_SD;
	}

	public MachineStatus getMachineStatus() throws FlashForgePrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		return endstopStatus.machineStatus;
	}

	public TempInfo getTempInfo() throws FlashForgePrinterException {
		String replay = sendCommand(CMD_TEMP).trim();
		return new TempInfo(replay);
	}

	public LocationInfo getLocationInfo() throws FlashForgePrinterException {
		String replay = sendCommand(CMD_INFO_XYZAB).trim();
		return new LocationInfo(replay);
	}

	public PrintStatus getPrintStatus() throws FlashForgePrinterException {
		String replay = sendCommand(CMD_PRINT_STATUS).trim();
		return new PrintStatus(replay);
	}

	public EndstopStatus getEndstopStatus() throws FlashForgePrinterException {
		String replay = sendCommand(CMD_ENDSTOP_INFO).trim();
		return new EndstopStatus(replay);
	}

	public PrintReport getPrintReport() throws FlashForgePrinterException {
		EndstopStatus endstopStatus = getEndstopStatus();
		PrintStatus printStatus = getPrintStatus();
		TempInfo tempInfo = getTempInfo();
		return new PrintReport(endstopStatus, printStatus, tempInfo);
	}

}
