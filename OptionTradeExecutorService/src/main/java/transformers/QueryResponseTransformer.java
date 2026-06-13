
package transformers;

import awesome.code.base.converter.ITransformer;
import awesome.code.base.properties.IPropertiesProvider;
import awesome.code.base.service.exception.ServiceException;
import awesome.code.core.query.proto.QueryResponseOuterClass;

public class QueryResponseTransformer implements ITransformer<QueryResponseOuterClass.QueryResponse, QueryResponseOuterClass.QueryResponse> {

    @Override
    public QueryResponseOuterClass.QueryResponse transform(QueryResponseOuterClass.QueryResponse queryResponse, IPropertiesProvider iPropertiesProvider) throws ServiceException {
        return queryResponse;
    }
}

