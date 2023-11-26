package slug2k.ffapi.commands.info;

import slug2k.ffapi.Logger;

/**
 * Class for handling printer specs
 */
// The original class was modified to work properly with the Adventurer 5 series, see https://github.com/Slugger2k/FlashForgePrinterApi/blob/main/src/main/java/de/slg/ddnss/printertool/clients/PrinterInfo.java
// The main difference is how temperature data is serialized
public class PrinterInfo {
	
	private String name;
	private String nickname;
	private String firmwareVersion;
	private String serialNumber;
	private String dimensions;
	private int toolCount;
	private String mac;

	/**
	 *
	 * Creates a PrinterInfo instance from a M115 command replay<br>
	 * @param replay The M115 command replay
	 * @author Slugger2k, upated by GhostTypes
	 */
	public PrinterInfo(String replay) {
		Logger.debug("PrinterInfo replay:\n" + replay);
		String[] split = replay.split("\\n");
		setName(split[1].split(":")[1].trim());
		setNickname(split[2].split(":")[1].trim());
		setFirmwareVersion(split[3].split(":")[1].trim());
		setSerialNumber(split[4].split(":")[1].trim());
		setDimensions(split[5].trim());
		setToolCount(Integer.parseInt(split[6].split(":")[1].trim()));
		setMac(split[7].trim());
	}

	/**
	 * Gets the 'actual' name of the printer, like Adventurer 5M Pro
	 */
	public String getName() {
		return name;
	}
	private void setName(String name) { this.name = name; }

	/**
	 * Gets the 'friendly' name of the printer, like CoolGuy's 3D Printer
	 */
	public String getNickname() {
		return nickname;
	}
	private void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * Gets the current firmware version of the printer
	 * @return String
	 */
	public String getFirmwareVersion() {
		return firmwareVersion;
	}
	private void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	/**
	 * Gets the serial number of the printer
	 * @return String
	 */
	public String getSerialNumber() {
		return serialNumber;
	}
	private void setSerialNumber(String seriesNr) {
		this.serialNumber = seriesNr;
	}

	/**
	 * Gets the print dimensions of the printer
	 * @return String
	 */
	public String getDimensions() {
		return dimensions;
	}
	private void setDimensions(String dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * Gets the tool count of the printer<br>
	 * This will always be 1 except the Creator Pro series (and other IDEX)
	 * @return Integer
	 */
	public int getToolCount() {
		return toolCount;
	}
	private void setToolCount(int toolCount) {
		this.toolCount = toolCount;
	}

	/**
	 * Gets the MAC Address of the printer
	 * @return String
	 */
	public String getMac() {
		return mac;
	}
	private void setMac(String mac) {
		this.mac = mac;
	}

	@Override
	public String toString() {
		return "PrinterInfo [machineType=" + name + ", machineName=" + nickname + ", firmwareVersion="
				+ firmwareVersion + ", serialNum=" + serialNumber + ", dimensions=" + dimensions + ", toolCount=" + toolCount
				+ ", mac=" + mac + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dimensions == null) ? 0 : dimensions.hashCode());
		result = prime * result + ((firmwareVersion == null) ? 0 : firmwareVersion.hashCode());
		result = prime * result + ((mac == null) ? 0 : mac.hashCode());
		result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
		result = prime * result + toolCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PrinterInfo other = (PrinterInfo) obj;
		if (dimensions == null) { if (other.dimensions != null) return false;
		} else if (!dimensions.equals(other.dimensions)) return false;
		if (firmwareVersion == null) { if (other.firmwareVersion != null) return false;
		} else if (!firmwareVersion.equals(other.firmwareVersion)) return false;
		if (mac == null) { if (other.mac != null) return false;
		} else if (!mac.equals(other.mac)) return false;
		if (nickname == null) { if (other.nickname != null) return false;
		} else if (!nickname.equals(other.nickname)) return false;
		if (name == null) { if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (serialNumber == null) { if (other.serialNumber != null) return false;
		} else if (!serialNumber.equals(other.serialNumber)) return false;
		return toolCount == other.toolCount;
	}
	
}
