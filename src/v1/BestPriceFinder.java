package v1;

import services.ExchangeService;
import services.ExchangeService.Money;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BestPriceFinder {

  private final List<Shop> shops = Arrays.asList(
      new Shop("BestPrice"),
      new Shop("LetsSaveBig"),
      new Shop("MyFavoriteShop"),
      new Shop("BuyItAll")
     ,new Shop("ShopEasy")
  );


  private final Executor executor = Executors.newFixedThreadPool(shops.size(), (Runnable r) -> {
    Thread t = new Thread(r);
    t.setDaemon(true);
    return t;
  });


  public List<String> findPricesSequential(String product) {
    return shops.stream()
        .map(shop -> shop.getName() + " price is " + shop.getPrice(product))
        .collect(Collectors.toList());
  }

  public List<String> findPricesParallel(String product) {
    return shops.parallelStream()
        .map(shop -> shop.getName() + " price is " + shop.getPrice(product))
        .collect(Collectors.toList());
  }

  public List<String> findPricesFuture(String product) {

    List<CompletableFuture<String>> priceFutures =
            shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(() -> shop.getName() + " price is " + shop.getPrice(product)))
            .collect(Collectors.toList());

    //Join separated because
    List<String> prices = priceFutures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());

    return prices;
  }


  public List<String> findPricesFutureExecutor(String product) {

    List<CompletableFuture<String>> priceFutures =
            shops.stream()
                    .map(shop -> CompletableFuture.supplyAsync(() -> shop.getName() + " price is " + shop.getPrice(product), executor))
                    .collect(Collectors.toList());

    //Join separated because
    List<String> prices = priceFutures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

    return prices;
  }

  public List<String> findPricesInUSD(String product) {
    // Here, the for loop has been replaced by a mapping function...
    Stream<CompletableFuture<String>> priceFuturesStream = shops.stream()
        .map(shop -> CompletableFuture
            .supplyAsync(() -> shop.getPrice(product))
            .thenCombine(
                CompletableFuture.supplyAsync(() -> ExchangeService.getRate(Money.EUR, Money.USD)),
                (price, rate) -> price * rate)
            .thenApply(price -> shop.getName() + " price is " + price));


    // However, we should gather the CompletableFutures into a List so that the asynchronous
    // operations are triggered before being "joined."
    List<CompletableFuture<String>> priceFutures = priceFuturesStream.collect(Collectors.toList());
    List<String> prices = priceFutures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
    return prices;
  }

  public List<String> findPricesInUSDExecutor(String product) {
    // Here, the for loop has been replaced by a mapping function...
    Stream<CompletableFuture<String>> priceFuturesStream = shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice(product),executor)
                    .thenCombine(
                            CompletableFuture.supplyAsync(() -> ExchangeService.getRate(Money.EUR, Money.USD),executor), (price, rate) -> price * rate)
                          //.completeOnTimeout(DEFAULT_RATE, 1, TimeUnit.SECONDS),
                    .thenApply(price -> shop.getName() + " price is " + price));
                    //.orTimeout(3, TimeUnit.SECONDS);

    // However, we should gather the CompletableFutures into a List so that the asynchronous
    // operations are triggered before being "joined."
    List<CompletableFuture<String>> priceFutures = priceFuturesStream.collect(Collectors.toList());
    List<String> prices = priceFutures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    return prices;
  }

}
