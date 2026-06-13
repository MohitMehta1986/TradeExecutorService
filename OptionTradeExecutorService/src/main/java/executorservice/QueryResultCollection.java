package executorservice;

import awesome.code.core.serialized.result.proto.SerializedResultOuterClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryResultCollection {
    public final List<SerializedResultOuterClass.StringField> strings;
    public final List<SerializedResultOuterClass.IntField> ints;
    public final List<SerializedResultOuterClass.LongField> longs;
    public final List<SerializedResultOuterClass.DoubleField> doubles;
    public final List<SerializedResultOuterClass.ByteArrayField> bytes;
    public final List<SerializedResultOuterClass.ObjectField> objects;
    public final List<SerializedResultOuterClass.NullField> nulls;
    public final List<SerializedResultOuterClass.NestedSerializedResult> nestedSerializedResults;
    public final List<SerializedResultOuterClass.BooleanField> booleanFields;
    public final List<SerializedResultOuterClass.TimestampField> timestampFields;

    public QueryResultCollection()
    {
        strings = new ArrayList<>();
        ints = new ArrayList<>();
        longs = new ArrayList<>();
        doubles = new ArrayList<>();
        bytes = new ArrayList<>();
        objects = new ArrayList<>();
        nulls = new ArrayList<>();
        nestedSerializedResults = new ArrayList<>();
        timestampFields = new ArrayList<>();
        booleanFields = new ArrayList<>();
    }

    public QueryResultCollection(SerializedResultOuterClass.LongField longField)
    {
        strings = Collections.emptyList();
        ints = Collections.emptyList();
        longs = Collections.emptyList();
        doubles = Collections.emptyList();
        bytes = Collections.emptyList();
        objects = Collections.emptyList();
        nulls = Collections.emptyList();
        nestedSerializedResults = Collections.emptyList();
        booleanFields = Collections.emptyList();
        timestampFields = Collections.emptyList();
    }

    public <T> QueryResultCollection(List<T> stringFields, Class<T> type)
    {
        if(SerializedResultOuterClass.ByteArrayField.class.equals(type))
        {
            bytes = (List<SerializedResultOuterClass.ByteArrayField>) stringFields;
        } else
        {
            bytes = Collections.emptyList();
        }

        strings = Collections.emptyList();
        ints = Collections.emptyList();
        longs = Collections.emptyList();
        doubles = Collections.emptyList();
        objects = Collections.emptyList();
        nulls = Collections.emptyList();
        nestedSerializedResults = Collections.emptyList();
        booleanFields = Collections.emptyList();
        timestampFields = Collections.emptyList();
    }

    public SerializedResultOuterClass.SerializedResult.Builder getSerializedResultBuilder()
    {
        SerializedResultOuterClass.SerializedResult.Builder result = SerializedResultOuterClass.SerializedResult.newBuilder();
        result.addAllStringField(strings);
        result.addAllIntField(ints);
        result.addAllLongField(longs);
        result.addAllDoubleField(doubles);
        result.addAllByteArrayField(bytes);
        result.addAllObjectField(objects);
        result.addAllNullField(nulls);
        result.addAllNestedSerializedResult(nestedSerializedResults);
        result.addAllBooleanField(booleanFields);
        result.addAllTimeStampField(timestampFields);
        return result;
    }

    public boolean isEmpty()
    {
        return strings.isEmpty() && ints.isEmpty()
                && longs.isEmpty() && doubles.isEmpty()
                && bytes.isEmpty() && objects.isEmpty()
                && nulls.isEmpty() && nestedSerializedResults.isEmpty()
                && booleanFields.isEmpty() && timestampFields.isEmpty();
    }
}
