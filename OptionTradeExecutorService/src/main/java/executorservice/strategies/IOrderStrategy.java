package executorservice.strategies;

import awesome.code.base.result.IResultTable;
import awesome.code.base.service.exception.ServiceException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface IOrderStrategy {
    List<Future<IResultTable>> executeOrder(Map<String,String> computeAttributes) throws ServiceException;
}
