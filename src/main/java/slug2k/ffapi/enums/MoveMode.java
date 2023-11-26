package slug2k.ffapi.enums;

public enum MoveMode {
    /**
     * The printer is currently busy<br>
     * Check MachineStatus, PrintStatus, or EndstopStatus for more info<br>
     * Some commands don't work in this state
     */
    MOVING,

    /**
     * An operation is currently being performed device-side<br>
     * No commands will work in this state
     */
    PAUSED,
    /**
     * The printer is idle and ready to start a job/accept any command
     */
    READY,

    /**
     * The printer is in an unknown state, this should never happen
     */
    DEFAULT

}
