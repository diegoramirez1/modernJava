package v1;

import java.util.List;
import java.util.function.Supplier;

public class BestPriceFinderMain {

  private static BestPriceFinder bestPriceFinder = new BestPriceFinder();

  public static void main(String[] args) {

    System.out.println("Procesors " + Runtime.getRuntime().availableProcessors());
    System.out.println();

    execute("sequential", () -> bestPriceFinder.findPricesSequential("myPhone27S"));
    System.out.println();

    execute("parallel", () -> bestPriceFinder.findPricesParallel("myPhone27S"));
    System.out.println();

    execute("CompletableFuture", () -> bestPriceFinder.findPricesFuture("myPhone27S"));
    System.out.println();

    execute("CompletableFutureExecutor", () -> bestPriceFinder.findPricesFutureExecutor("myPhone27S"));
    System.out.println();

    execute("combined USD CompletableFuture", () -> bestPriceFinder.findPricesInUSD("myPhone27S"));
    System.out.println();

    execute("combined USD CompletableFuture Executor", () -> bestPriceFinder.findPricesInUSDExecutor("myPhone27S"));

  }

  private static void execute(String msg, Supplier<List<String>> s) {

    long start = System.nanoTime();
    System.out.println(s.get());
    long duration = (System.nanoTime() - start) / 1_000_000;
    System.out.println(msg + " done in " + duration + " msecs");
  }

}
