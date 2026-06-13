package computedocument;

import java.util.Map;

public abstract class ComputeDocument {

    private final String userId;
    private Map<String, String> computeAttributes;
    private byte[] computeAttributeRequest;

    protected ComputeDocument(String computeUserId, Map<String,String> computeAttributes)
    {
        this.userId = computeUserId;
        this.computeAttributes = computeAttributes;
    }

    protected ComputeDocument(String computeUserId, byte[] computeAttributeRequest)
    {
        this.userId = computeUserId;
        this.computeAttributeRequest = computeAttributeRequest;
    }

    public Map<String, String> getComputeAttributes() {
        return computeAttributes;
    }

    public String getUserId() {
        return userId;
    }

    public byte[] getComputeAttributeRequest() {
        return computeAttributeRequest;
    }
}
