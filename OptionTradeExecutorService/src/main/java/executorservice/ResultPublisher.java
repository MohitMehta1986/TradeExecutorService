package executorservice;

import awesome.code.base.result.IResultMetadata;
import awesome.code.base.result.IResultTable;
import awesome.code.base.result.ResultColumnType;
import awesome.code.base.result.ResultException;
import awesome.code.base.service.IMessagePublisher;
import awesome.code.base.service.exception.ServiceException;
import awesome.code.core.query.proto.QueryRequestOuterClass;
import awesome.code.core.query.proto.QueryResponseOuterClass;
import awesome.code.core.serialized.result.proto.SerializedResultOuterClass;
import awesome.code.exception.EndOfMessageException;
import awesome.code.server.query.select.proto.SelectColumnAttributeMapping;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResultPublisher {

    public static void publishResult(IResultTable resultTable,IMessagePublisher<QueryResponseOuterClass.QueryResponse> responseQueue) throws EndOfMessageException, ResultException, InterruptedException {
        publishMetadata(resultTable, responseQueue);
        publishRow(resultTable, responseQueue);
    }

    private static void publishMetadata(IResultTable resultTable, IMessagePublisher<QueryResponseOuterClass.QueryResponse> responseQueue) throws ResultException, EndOfMessageException, InterruptedException {
        IResultMetadata resultMetadata = resultTable.getMetaData();
        List<QueryResponseOuterClass.QueryResponse.MetaData> metaDataList = getMetaDataList(resultMetadata);
        QueryResponseOuterClass.QueryResponse.Builder responseBuilder = QueryResponseOuterClass.QueryResponse.newBuilder();
        responseBuilder.setMetaDataResponse(QueryResponseOuterClass.QueryResponse.MetaDataResponse.newBuilder().addAllMetaData(metaDataList));
        responseQueue.publish(responseBuilder.build());
    }

    private static List<QueryResponseOuterClass.QueryResponse.MetaData> getMetaDataList(IResultMetadata metadata)
    {
        List<QueryResponseOuterClass.QueryResponse.MetaData> metaDataList = new ArrayList<>();
        Set<String> columnNames = metadata.getColumnNames();
        int index = 0;

        for(String columnName : columnNames)
        {
            IResultMetadata resultMetadata = metadata.getNestedMetaData(columnName);
            if(resultMetadata!=null)
            {
                QueryResponseOuterClass.QueryResponse.MetaData.Builder metaDataBuilder = QueryResponseOuterClass.QueryResponse.MetaData.newBuilder().setIndex(++index).setLabel(columnName)
                        .setAttributeTypeDef(QueryResponseOuterClass.QueryResponse.AttributeTypeDef.newBuilder()
                                .setAttributeType(getAttributeType(metadata.getColumnType(columnName).getKey())).build());

                List<QueryResponseOuterClass.QueryResponse.MetaData> nestedMetaDataList = getMetaDataList(resultMetadata);

                metaDataList.add(metaDataBuilder.addAllMetaData(nestedMetaDataList).build());

            } else
            {
                metaDataList.add(QueryResponseOuterClass.QueryResponse.MetaData.newBuilder().setIndex(++index).setLabel(columnName)
                        .setAttributeTypeDef(QueryResponseOuterClass.QueryResponse.AttributeTypeDef.newBuilder()
                                .setAttributeType(getAttributeType(metadata.getColumnType(columnName).getKey()))).build());
            }
        }
        return metaDataList;
    }

    private static SelectColumnAttributeMapping.AttributeType getAttributeType(ResultColumnType type)
    {
        switch (type)
        {
            case INT:
                return SelectColumnAttributeMapping.AttributeType.INT_AT;
            case LONG:
                return SelectColumnAttributeMapping.AttributeType.LONG_AT;
            case DOUBLE:
                return SelectColumnAttributeMapping.AttributeType.DOUBLE_AT;
            case STRING:
                return SelectColumnAttributeMapping.AttributeType.STRING_AT;
            case BOOLEAN:
                return SelectColumnAttributeMapping.AttributeType.BOOLEAN_AT;
            case BYTES:
                return SelectColumnAttributeMapping.AttributeType.BYTES_AT;
            case NESTED:
                return SelectColumnAttributeMapping.AttributeType.NESTED_AT;
            case TIMESTAMP:
                return SelectColumnAttributeMapping.AttributeType.TIMESTAMP_AT;
            default:
                return SelectColumnAttributeMapping.AttributeType.UNKNOWN_AT;
        }
    }

    private static void publishRow(IResultTable table, IMessagePublisher<QueryResponseOuterClass.QueryResponse> responseQueue) throws ResultException, EndOfMessageException, InterruptedException {
        IResultMetadata metadata = table.getMetaData();
        Set<String> columnNames = metadata.getColumnNames();

        while(table.next())
        {
            QueryResultCollection queryResultCollection = new QueryResultCollection();
            updateRow(table, queryResultCollection, metadata, columnNames);
            SerializedResultOuterClass.SerializedResult.Builder resultBuilder = queryResultCollection.getSerializedResultBuilder();
            responseQueue.publish(QueryResponseOuterClass.QueryResponse.newBuilder().setResultResponse(resultBuilder).build());
        }

    }

    private static void updateRow(IResultTable resultTable, QueryResultCollection nestedQueryResultCollection, IResultMetadata metadata, Set<String> columnNames) throws ResultException {
        int index = 0;
        for(String columnName : columnNames)
        {
            ++index;
            ResultColumnType type = metadata.getColumnType(columnName).getKey();
            Object fieldValue = resultTable.getObject(columnName);
            if(fieldValue == null)
            {
                nestedQueryResultCollection.nulls.add(SerializedResultOuterClass.NullField.newBuilder().setIndex(index).build());
            } else {
                addResults(nestedQueryResultCollection, index, type, fieldValue);
            }
        }
    }

    private static void addResults(QueryResultCollection queryResultCollection, int index, ResultColumnType type, Object fieldValue) throws ResultException {
        switch (type)
        {
            case INT:
                queryResultCollection.ints.add(SerializedResultOuterClass.IntField.newBuilder().setIndex(index).setValue((Integer) fieldValue).build());
                break;
            case LONG:
                queryResultCollection.longs.add(SerializedResultOuterClass.LongField.newBuilder().setIndex(index).setValue((Long) fieldValue).build());
                break;
            case DOUBLE:
                queryResultCollection.doubles.add(SerializedResultOuterClass.DoubleField.newBuilder().setIndex(index).setValue((Double) fieldValue).build());
                break;
            case STRING:
                queryResultCollection.strings.add(SerializedResultOuterClass.StringField.newBuilder().setIndex(index).setValue((String) fieldValue).build());
                break;
            case BOOLEAN:
                queryResultCollection.booleanFields.add(SerializedResultOuterClass.BooleanField.newBuilder().setValue((Boolean) fieldValue).setIndex(index).build());
                break;
//            case BYTES:
//                queryResultCollection.bytes.add(SerializedResultOuterClass.ByteArrayField.newBuilder().setIndex(index).build());
//                break;
            case NESTED:
                IResultTable nestedResultTable = (IResultTable)fieldValue;
                queryResultCollection.nestedSerializedResults.add(SerializedResultOuterClass.NestedSerializedResult.newBuilder().addAllSerializedResult(getNestedResult(nestedResultTable)).setIndex(index).build());
                break;
            case TIMESTAMP:
                Instant instant = fieldValue instanceof Instant ? (Instant) fieldValue : Instant.ofEpochMilli((Long) fieldValue);
                Timestamp.Builder ts = Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano());
                queryResultCollection.timestampFields.add(SerializedResultOuterClass.TimestampField.newBuilder().setIndex(index).setTimestamp(ts).build());
                break;
            default:
                queryResultCollection.objects.add(SerializedResultOuterClass.ObjectField.newBuilder().setIndex(index).setValue(fieldValue + "").build());
                break;
        }
    }

    private static List<SerializedResultOuterClass.SerializedResult> getNestedResult(IResultTable resultTable) throws ResultException {
        List<SerializedResultOuterClass.SerializedResult> nestedSerializedResultRows = new ArrayList<>();
        IResultMetadata metadata = resultTable.getMetaData();
        Set<String> columnNames = metadata.getColumnNames();
        while(resultTable.next())
        {
            QueryResultCollection queryResultCollection = new QueryResultCollection();
            updateRow(resultTable, queryResultCollection, metadata, columnNames);
            nestedSerializedResultRows.add(queryResultCollection.getSerializedResultBuilder().build());
        }
        return nestedSerializedResultRows;
    }
}
