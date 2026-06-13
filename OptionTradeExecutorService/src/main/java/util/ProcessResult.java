package util;

public class ProcessResult<T> {

    public static final ProcessResult<Object> EVENT_PROCESS_RESULT_SUCCESS = new ProcessResult<>(ProcessResultType.SUCCESS, "Operation Succeded", null);
    private final ProcessResultType resultType;
    private final String description;
    private final T result;

    public ProcessResult(ProcessResultType resultType, String description, T result)
    {
        super();
        this.resultType = resultType;
        this.description = description;
        this.result = result;
    }

    public ProcessResultType getProcessResultType()
    {
        return resultType;
    }

    public String getDescription()
    {
        return description;
    }

    public T getResult()
    {
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProcessResult{");
        sb.append("resultType=").append(resultType);
        sb.append(", description='").append(description).append('\'');
        sb.append(", result=").append(result);
        sb.append('}');
        return sb.toString();
    }
}
