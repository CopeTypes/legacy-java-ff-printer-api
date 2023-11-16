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

    DEFAULT
}
