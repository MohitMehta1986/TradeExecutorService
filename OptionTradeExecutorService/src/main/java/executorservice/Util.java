package executorservice;

public class Util {

  public static double getDefaultStopLossPrice(double executionPrice, double defaultStopLossAmount, int lotSize, int numberOfLots)
  {


    double totalPrice = executionPrice * lotSize * numberOfLots;

    double stopLossPrice = totalPrice - defaultStopLossAmount;

    double stopLossForOneOption = stopLossPrice/(lotSize*numberOfLots);

    return stopLossForOneOption;

  }
}
