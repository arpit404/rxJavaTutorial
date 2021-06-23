import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class RefCountTest {

    @Test
    public void test() {
        Observable<Integer> observable = Observable.unsafeCreate(subscriber -> {
            // establish connection
            System.out.println("Establishing connection");

            //do the needful

            // disconnect connection on unsubscribe
            subscriber.add(Subscriptions.create(() -> {
                System.out.println("Disconnecting");
            }));
        });

        Subscription sub1 = observable.subscribe();
        System.out.println("Subscribed 1");
        Subscription sub2 = observable.subscribe();
        System.out.println("Subscribed 2");
        sub1.unsubscribe();
        System.out.println("Unsubscribed 1");
        sub2.unsubscribe();
        System.out.println("Unsubscribed 2");
    }

    @Test
    public void lazyTest(){
        Observable<Object> observable = Observable.unsafeCreate(subscriber -> {
            // establish connection
            System.out.println("Establishing connection");

            //do the needful

            // disconnect connection on unsubscribe
            subscriber.add(Subscriptions.create(() -> {
                System.out.println("Disconnecting");
            }));
        }).publish().refCount();

        Subscription sub1 = observable.subscribe();
        System.out.println("Subscribed 1");
        Subscription sub2 = observable.subscribe();
        System.out.println("Subscribed 2");
        sub1.unsubscribe();
        System.out.println("Unsubscribed 1");
        sub2.unsubscribe();
        System.out.println("Unsubscribed 2");
    }
}
