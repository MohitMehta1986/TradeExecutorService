package executorservice.commands;

import executorservice.ITradeExecutionResult;

public interface ICommand<T> {
    ITradeExecutionResult execute();
    boolean canExecute();
}
