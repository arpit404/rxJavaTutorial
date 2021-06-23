import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

public class Practice {

    @Test
    public void newThreadForEachObservableItem() {

        Observable<Integer> vals = Observable.range(1, 10);

        vals.flatMap(val -> Observable.just(val)
                .observeOn(Schedulers.computation())
                .map(i -> log(i))
        ).subscribe();
    }

    public Object log(Object x) {

        System.out.println("Thread : " + Thread.currentThread().getId() + "\t" + x);
        return x;
    }

    public void test2(){
        Observable
                .just(1,2)
                .flatMap(ticket ->
                        Observable.just(ticket)
                                .ignoreElements()
                                .doOnError(e -> System.out.println(e))
                                .onErrorReturn(err -> ticket)
                                .subscribeOn(Schedulers.io()));


    }
}
