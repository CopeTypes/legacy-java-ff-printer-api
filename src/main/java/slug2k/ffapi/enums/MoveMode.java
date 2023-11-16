package slug2k.ffapi.enums;

public enum MoveMode {
    //todo there's like 2-3 more move status states we need to document
    //one might be ready?

    /**
     * The printer is currently moving.<br>
     * Check MachineStatus, PrintStatus, or EndstopStatus for more info
     */
    MOVING,

    /**
     * The printer is currently paused.<br>
     * Check MachineStatus, PrintStatus, or EndstopStatus for more info
     */
    PAUSED,
    DEFAULT

}
