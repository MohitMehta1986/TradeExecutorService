package publisher;

import awesome.code.result.Context;
import awesome.code.result.CoreClientImpl;
import changedocument.ChangeDocumentResponse;
import com.google.gson.Gson;
import fetchrequest.ContextName;
import optiontrading.common.changeDocuments.OrderChangeDocument;
import optiontrading.common.changeDocuments.Status;

import java.util.concurrent.Callable;

public class OrderWriter implements Callable<ChangeDocumentResponse> {

    private final CoreClientImpl coreClient;
    private final OrderChangeDocument orderChangeDocument;
    private final Gson gson;

    public OrderWriter(CoreClientImpl coreClient, OrderChangeDocument orderChangeDocument)
    {
        this.coreClient = coreClient;
        this.orderChangeDocument = orderChangeDocument;
        this.gson = new Gson();
    }

    @Override
    public ChangeDocumentResponse call() {
        if(this.orderChangeDocument!=null) {
            String computeRequest = gson.toJson(this.orderChangeDocument, OrderChangeDocument.class);
            ChangeDocumentResponse response = coreClient.publishChangeDocument(new Context(ContextName.OFFICIAL), "OrderChangeDocument", computeRequest);
            if (response.getStatus().equals(Status.SUCCESS)) {
                System.out.println("Order saved successfully " + orderChangeDocument.toString());
            } else {
                System.out.println("Order not saved " + orderChangeDocument.toString());
            }
            return response;
        }
        return null;
    }
}
