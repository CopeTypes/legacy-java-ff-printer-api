package slug2k.ffapi.enums;

public enum MachineStatus {
    /**
     * The printer is currently printing a job
     */
    BUILDING_FROM_SD,
    /**
     * The printer has finished a job and is in a 'waiting' state
     */
    BUILDING_COMPLETED,
    /**
     * The printer is ready to start a job
     */
    READY,

    /**
     * An operation is currently being performed device-side<br>
     * No commands will work in this state
     */
    // I haven't encountered this on the 5M series, but older printers may use this in place of
    // BUILDING_COMPLETED
    PAUSED,

    /**
     * The printer is in an unknown state, this should never happen
     */
    DEFAULT
}
