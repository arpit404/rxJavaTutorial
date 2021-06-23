import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ExamplesTest {


    @Test
    public void test1() {
        Observable<Integer> ints = Observable
                .create(subscriber -> {
                    log("Create");

                    subscriber.onNext(5);

                    subscriber.onNext(6);

                    subscriber.onNext(7);
                    subscriber.onCompleted();
                    log("Completed");
                });

        log("Starting");
        ints.subscribe(i -> log("Element: " + i));
        log("Exit");

    }

    @Test
    public void test2() {
        Observable<Integer> ints =
                Observable.unsafeCreate(subscriber -> {
                            log("Create");
                            subscriber.onNext(42);
                            subscriber.onCompleted();
                            log("Completed");
                        }
                );

        log("Starting");
        ints.subscribe(i -> log("Element A: " + i),
                e -> e.printStackTrace(), () -> {
            System.out.println("Completion Event");
        });
        ints.subscribe(i -> log("Element B: " + i));
        log("Exit");
    }

    @Test
    public void test3() {
        Observable<Object> ints =
                Observable.unsafeCreate(subscriber -> {
                            log("Create");
                            subscriber.onNext(42);
                            subscriber.onCompleted();
                            log("Completed");
                        }
                ).cache();

        log("Starting");
        ints.subscribe(i -> log("Element A: " + i));
        ints.subscribe(i -> log("Element B: " + i));
        log("Exit");
    }

    @Test
    public void test4() {
        Observable<BigInteger> naturalNumbers = Observable.create(
                subscriber -> {
                    BigInteger i = BigInteger.ZERO;
                    while (true) {
                        subscriber.onNext(i);
                        i = i.add(BigInteger.ONE);
                    }
                });
        Subscription subscription = naturalNumbers.subscribe(x -> log(x + ""));
        subscription.unsubscribe();
    }

    @Test
    public void test5() throws InterruptedException {
        log("Started");
        Observable<BigInteger> naturalNumbers = Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        BigInteger i = BigInteger.ZERO;
                        while (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(i);
                            i = i.add(BigInteger.ONE);
                        }
                    };
                    new Thread(r).start();
                });

        Subscription subscription = naturalNumbers.subscribe(x -> log(x + ""));
        log("Continue");
        Thread.sleep(10);
        subscription.unsubscribe();
        log("Exit");
    }

    @Test
    public void test6() {
        Observable<Integer> N = Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        sleep(10);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(1);
                            subscriber.onCompleted();
                        }
                    };
                    Thread th = new Thread(r);
                    th.start();
                    subscriber.add(Subscriptions.create(th::interrupt));
                    subscriber.add(Subscriptions.create(() -> System.out.println("Ending")));
                    subscriber.add(Subscriptions.create(th::interrupt));
                });
        Subscription s = N.subscribe(x -> log(x));
        sleep(1);
        s.unsubscribe();

    }

    public void sleep(int x) {
        try {
            TimeUnit.SECONDS.sleep(x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test7() {
        Observable<Integer> N = Observable.fromCallable(() -> 1 / 0);
        N.subscribe(x -> log(x), e -> System.out.println("Error"));
    }


    @Test
    public void test8() throws InterruptedException {
        Observable
                .timer(1, TimeUnit.SECONDS)
                .subscribe((Long zero) -> log(zero));
        Thread.sleep(5000);
    }

    @Test
    public void test9() throws InterruptedException {
        Observable
                .interval(1_000_000 / 60, TimeUnit.MICROSECONDS)
                .subscribe((Long zero) -> log(zero));
        Thread.sleep(2000);
    }

    @Test
    public void test10() {
        System.out.println(Thread.currentThread().getName());
//        Observable.defer(() ->
                Observable.from(getInts());
//                .subscribeOn(Schedulers.computation())
//                .subscribe(n -> System.out.println(n));
    }

    public List<Integer> getInts(){
        System.out.println(Thread.currentThread().getName());
        return Arrays.asList(1,2,3);
    }

    @Test
    public void test11_1() throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        Observable<Integer> flight = Observable.from(getInts()).subscribeOn(Schedulers.io());
        Observable<Integer> passenger = Observable.from(getInts()).subscribeOn(Schedulers.io());

        flight.zipWith(passenger, (f, p) -> f * p)
                .subscribe(e -> System.out.println(Thread.currentThread().getName() + "\t" + e));
        Thread.sleep(1000);
    }

    @Test
    public void test11_2() throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        Observable<Integer> flight = Observable.defer(() -> Observable.from(getInts())).subscribeOn(Schedulers.io());
        Observable<Integer> passenger = Observable.defer(() -> Observable.from(getInts())).subscribeOn(Schedulers.io());

        flight.zipWith(passenger, (f, p) -> f * p)
                .subscribe(e -> System.out.println(Thread.currentThread().getName() + "\t" + e));
        Thread.sleep(1000);
    }

    @Test
    public void test12() throws InterruptedException {
        log("Starting");
        final Observable<String> obs = Observable.just("A","B");
        log("Created");
        final Observable<String> obs2 = obs
                .map(x -> x)
                .filter(x -> true);
        log("Transformed");

        obs2
                .subscribeOn(Schedulers.newThread())
                .subscribe(
                        x -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            log("Got " + x);
                        },
                        Throwable::printStackTrace,
                        () -> log("Completed")
                );
        log("Exiting");
        Thread.sleep(3000);
    }

    @Test
    public void newThreadForEachObservableItem() {

        Observable<Integer> vals = Observable.range(1, 10);

        vals.flatMap(val -> Observable.just(val)
                .observeOn(Schedulers.computation())
                .map(i -> log(i))
        ).subscribe();
    }


    public Object log(Object x) {

        System.out.println("Thread : " + Thread.currentThread().getName() + "\t" + x);
        return x;
    }

    @Test
    public void test13(){
        PublishSubject<Integer> source = PublishSubject.<Integer>create();

        source.observeOn(Schedulers.computation())
                .subscribe(System.out::println, Throwable::printStackTrace);

        IntStream.range(1, 20000).forEach(source::onNext);

    }

    @Test
    public void buffering() throws InterruptedException {
        PublishSubject<Integer> source = PublishSubject.<Integer>create();

        source.buffer(1024)
                .observeOn(Schedulers.computation())
                .subscribe(System.out::println, Throwable::printStackTrace);

        IntStream.range(1, 200000).forEach(source::onNext);
        Thread.sleep(1000);
    }

    @Test
    public void batchEmitting() throws InterruptedException {
        PublishSubject<Integer> source = PublishSubject.<Integer>create();

        source.window(500)
                .observeOn(Schedulers.computation())
                .subscribe(System.out::println, Throwable::printStackTrace);

        IntStream.range(1, 200000).forEach(source::onNext);
        Thread.sleep(1000);
    }

    @Test
    public void skippingElements() throws InterruptedException {
        PublishSubject<Integer> source = PublishSubject.<Integer>create();

        source.sample(1,TimeUnit.MICROSECONDS)
                .observeOn(Schedulers.computation())
                .subscribe(System.out::println, Throwable::printStackTrace);

        IntStream.range(1, 200000).forEach(source::onNext);
        Thread.sleep(1000);
    }

}