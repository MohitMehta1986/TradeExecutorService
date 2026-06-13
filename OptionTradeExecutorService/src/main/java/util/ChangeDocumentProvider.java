package util;

import executorservice.ITradeExecutionResult;
import executorservice.commands.*;
import optiontrading.common.WorkFlowStatus;
import optiontrading.common.changeDocuments.LatchOrderChangeDocument;
import optiontrading.common.changeDocuments.Operation;
import optiontrading.common.changeDocuments.OrderChangeDocument;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChangeDocumentProvider {

    private ChangeDocumentProvider()
    {}

    public static OrderChangeDocument getOrderChangeDocument(ITradeExecutionResult tradeExecutionResult, ICommand commandToExecute, ClientOrder clientOrder)
    {
        if(tradeExecutionResult!=null && tradeExecutionResult.getOrder()!=null)
        {
            OrderChangeDocument orderChangeDocument = new OrderChangeDocument(Operation.UPSERT, commandToExecute.getClass().getSimpleName(),commandToExecute.getClass().getSimpleName());
            orderChangeDocument.setIndexSymbol(clientOrder.getIndexSymbol());
            orderChangeDocument.setOptionSymbol(clientOrder.getTradingSymbol());
            orderChangeDocument.setQuantity(clientOrder.getLots() * clientOrder.getLotSize());
            orderChangeDocument.setSource(clientOrder.getClientOrderOriginator());

            if(commandToExecute instanceof ExecuteAtMarketCommand)
            {
                orderChangeDocument.setOrderId(tradeExecutionResult.getOrder().getOrderId());
                orderChangeDocument.setTradeOrderId(tradeExecutionResult.getOrder().getOrderId());
                orderChangeDocument.setTradeDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
                orderChangeDocument.setWorkflowStatus("PENDING_COMPLETE");
                orderChangeDocument.setProcessStatus("NEW");
                orderChangeDocument.setBuyTime(Instant.now().toEpochMilli());
                orderChangeDocument.setOrderAction(WorkFlowStatus.EXECUTE_AT_MARKET.getStatus());
            }
            if(commandToExecute instanceof ExecuteAtLimitCommand)
            {
                orderChangeDocument.setOrderId(tradeExecutionResult.getOrder().getOrderId());
                orderChangeDocument.setTradeOrderId(tradeExecutionResult.getOrder().getOrderId());
                orderChangeDocument.setTradeDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
                orderChangeDocument.setWorkflowStatus("PENDING_COMPLETE");
                orderChangeDocument.setProcessStatus("NEW");
                orderChangeDocument.setBuyTime(Instant.now().toEpochMilli());
                orderChangeDocument.setOrderAction(WorkFlowStatus.EXECUTE_AT_LIMIT.getStatus());
            }
            if(commandToExecute instanceof ExitAtMarketCommand)
            {

                orderChangeDocument.setProcessStatus("COMPLETED");
                orderChangeDocument.setSellTime(Instant.now().toEpochMilli());
                orderChangeDocument.setOrderId(clientOrder.getOrderId());
                orderChangeDocument.setTradeOrderId(clientOrder.getTradeOrderId());
                orderChangeDocument.setWorkflowStatus("COMPLETED");
                orderChangeDocument.setOrderAction(WorkFlowStatus.EXIT_AT_MARKET.getStatus());

            }
            if(commandToExecute instanceof CancelOrderCommand)
            {
                orderChangeDocument.setProcessStatus("CANCELED");
                orderChangeDocument.setOrderId(clientOrder.getOrderId());
                orderChangeDocument.setTradeOrderId(clientOrder.getTradeOrderId());
                orderChangeDocument.setWorkflowStatus("PENDING_CANCEL");
                orderChangeDocument.setOrderAction(WorkFlowStatus.CANCELED.getStatus());
            }
            if (commandToExecute instanceof ModifyLimitPriceOrderCommand)
            {
                orderChangeDocument.setOrderId(clientOrder.getOrderId());
                orderChangeDocument.setProcessStatus("NEW");
                orderChangeDocument.setTradeOrderId(clientOrder.getTradeOrderId());
                orderChangeDocument.setWorkflowStatus("PENDING_UPDATE");
                orderChangeDocument.setOrderAction(WorkFlowStatus.MODIFY_STOP_LOSS.getStatus());
            }
            return orderChangeDocument;
        }
        return null;
    }

    public static LatchOrderChangeDocument getLatchOrderChangeDocument(ITradeExecutionResult tradeExecutionResult,
                                                                       ICommand commandToExecute, ClientOrder clientOrder, boolean isMockTrade)
    {
        if(tradeExecutionResult!=null && tradeExecutionResult.getOrder()!=null)
        {
            LatchOrderChangeDocument latchOrderChangeDocument = new LatchOrderChangeDocument(Operation.UPSERT, commandToExecute.getClass().getSimpleName(),commandToExecute.getClass().getSimpleName());
            latchOrderChangeDocument.setSource(clientOrder.getClientOrderOriginator());
            if(commandToExecute instanceof ExecuteAtMarketCommand)
            {
                latchOrderChangeDocument.setOrderId(tradeExecutionResult.getOrder().getOrderId());
                latchOrderChangeDocument.setTradeOrderId(tradeExecutionResult.getOrder().getOrderId());
                latchOrderChangeDocument.setOrderOperation("MARKET");
                latchOrderChangeDocument.setOperationStatus("IN_PROGRESS");
            }
            if(commandToExecute instanceof ExecuteAtLimitCommand)
            {
                latchOrderChangeDocument.setOrderId(tradeExecutionResult.getOrder().getOrderId());
                latchOrderChangeDocument.setTradeOrderId(tradeExecutionResult.getOrder().getOrderId());
                latchOrderChangeDocument.setOrderOperation("LIMIT");
                latchOrderChangeDocument.setOperationStatus("IN_PROGRESS");

            }
            if(commandToExecute instanceof ExitAtMarketCommand)
            {
                latchOrderChangeDocument.setOrderId(clientOrder.getOrderId());
                latchOrderChangeDocument.setTradeOrderId(clientOrder.getTradeOrderId());
                latchOrderChangeDocument.setOrderOperation("EXIT");
                latchOrderChangeDocument.setOperationStatus("IN_PROGRESS");
            }
            if(commandToExecute instanceof CancelOrderCommand)
            {
                latchOrderChangeDocument.setOrderId(clientOrder.getOrderId());
                latchOrderChangeDocument.setTradeOrderId(clientOrder.getTradeOrderId());
                latchOrderChangeDocument.setOrderOperation("CANCEL");
                latchOrderChangeDocument.setOperationStatus("IN_PROGRESS");

            }
            if (commandToExecute instanceof ModifyLimitPriceOrderCommand)
            {
                latchOrderChangeDocument.setOrderId(clientOrder.getOrderId());
                latchOrderChangeDocument.setTradeOrderId(clientOrder.getTradeOrderId());
                latchOrderChangeDocument.setOrderOperation("UPDATE");
                latchOrderChangeDocument.setOperationStatus("IN_PROGRESS");
            }

            if(isMockTrade)
            {
                latchOrderChangeDocument.setOperationStatus("COMPLETE");
            }
            return latchOrderChangeDocument;
        }
        return null;
    }
}
