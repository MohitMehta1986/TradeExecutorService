
package transformers;

import awesome.code.base.converter.ITransformer;
import awesome.code.base.properties.IPropertiesProvider;
import awesome.code.base.service.exception.ServiceException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import computedocument.OptionTradeComputeDocument;
import options.trading.execution.proto.ComputeDocumentOuterClass;

public class OptionTradeRequestTransformer implements ITransformer<String, OptionTradeComputeDocument> {

    //ComputeDocumentOuterClass.ComputeDocument.Builder protoBuilder = ComputeDocumentOuterClass.ComputeDocument.newBuilder();

    @Override
    public OptionTradeComputeDocument transform(String request, IPropertiesProvider iPropertiesProvider) throws ServiceException {
        ComputeDocumentOuterClass.ComputeDocument.Builder protoBuilder = ComputeDocumentOuterClass.ComputeDocument.newBuilder();
        try {
            JsonFormat.parser().merge(request, protoBuilder);
        } catch (InvalidProtocolBufferException e) {
            throw new ServiceException("Error while parsing request json", e);
        }
        ComputeDocumentOuterClass.ComputeDocument proto = protoBuilder.build();
        OptionTradeComputeDocument optionTradeComputeDocument = new OptionTradeComputeDocument(proto.getChangeUserId(), proto.getComputeMapRequest().getComputeAttributesMap());
        return optionTradeComputeDocument;
    }
}

