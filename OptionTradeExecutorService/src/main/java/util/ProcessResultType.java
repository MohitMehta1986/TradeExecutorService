package util;

public enum ProcessResultType {
    SUCCESS(true, false),
    ERROR(false, true),
    STALE(false, true),
    LOCK_TIMEOUT(false, true),
    FATAL(false, false)
    ;

    private boolean isSuccess;
    private boolean isRecoverable;

    private ProcessResultType(boolean isSuccess, boolean isRecoverable)
    {
        this.isRecoverable = isRecoverable;
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess()
    {
        return isSuccess;
    }

    public boolean isRecoverable()
    {
        return isRecoverable;
    }
}
