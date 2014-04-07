/**
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.futureconverter.java8rx;

import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static net.javacrumbs.futureconverter.java8rx.FutureConverter.toCompletableFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToCompletableFutureConverterTest {

    public static final String VALUE = "test";

    @Test
    public void testConvertToCompletableCompleted() throws ExecutionException, InterruptedException {
        Observable<String> observable = Observable.from(VALUE);
        CompletableFuture<String> completable = toCompletableFuture(observable);
        Consumer<String> consumer = mock(Consumer.class);

        CountDownLatch latch = new CountDownLatch(1);
        completable.thenAccept(consumer).thenRun(() -> latch.countDown());

        assertEquals(VALUE, completable.get());
        assertEquals(true, completable.isDone());
        assertEquals(false, completable.isCancelled());
        latch.await();
        verify(consumer).accept(VALUE);

    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        CountDownLatch waitLatch = new CountDownLatch(1);
        Observable<String> observable = createWaitingTask(waitLatch);
        CompletableFuture<String> completable = toCompletableFuture(observable);

        Consumer<String> consumer = mock(Consumer.class);
        assertEquals(false, completable.isDone());
        assertEquals(false, completable.isCancelled());

        CountDownLatch latch = new CountDownLatch(1);
        completable.thenAccept(consumer).thenRun(() -> latch.countDown());
        waitLatch.countDown();

        //wait for the result
        assertEquals(VALUE, completable.get());
        assertEquals(true, completable.isDone());
        assertEquals(false, completable.isCancelled());

        latch.await();
        verify(consumer).accept(VALUE);
    }

    private Observable<String> createWaitingTask(CountDownLatch waitLatch) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        waitLatch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return VALUE;
                });
                completableFuture.thenAccept(subscriber::onNext);
                subscriber.add(Subscriptions.from(completableFuture));
            }
        });
    }


    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        CountDownLatch waitLatch = new CountDownLatch(1);
        Observable<String> observable = createWaitingTask(waitLatch);

        CompletableFuture<String> completable = toCompletableFuture(observable);
        assertTrue(completable.cancel(true));

        try {
            completable.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, completable.isDone());
        assertEquals(true, completable.isCancelled());
//        assertEquals(true, observable.);
//        assertEquals(true, observable.isCancelled());
    }
//
//    @Test
//    public void testCancelNew() throws ExecutionException, InterruptedException {
//        CountDownLatch waitLatch = new CountDownLatch(1);
//        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(() -> {
//            waitLatch.await();
//            return VALUE;
//        });
//        executorService.execute(listenable);
//
//        CompletableFuture<String> completable = toCompletableFuture(listenable);
//        assertTrue(completable.cancel(true));
//
//        try {
//            completable.get();
//            fail("Exception expected");
//        } catch (CancellationException e) {
//            //ok
//        }
//        assertEquals(true, completable.isDone());
//        assertEquals(true, completable.isCancelled());
//        assertEquals(true, listenable.isDone());
//        assertEquals(true, listenable.isCancelled());
//    }
//
//    @Test
//    public void testCancelCompleted() throws ExecutionException, InterruptedException {
//        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(() -> VALUE);
//        executorService.execute(listenable);
//
//        CompletableFuture<String> completable = toCompletableFuture(listenable);
//        Consumer<String> consumer = mock(Consumer.class);
//
//        CountDownLatch latch = new CountDownLatch(1);
//        completable.thenAccept(consumer).thenRun(() -> latch.countDown());
//
//        assertEquals(VALUE, completable.get());
//        assertEquals(true, completable.isDone());
//        assertEquals(false, completable.isCancelled());
//        latch.await();
//        verify(consumer).accept(VALUE);
//
//        assertFalse(completable.cancel(true));
//    }
//
//    @Test
//    public void testConvertToCompletableException() throws ExecutionException, InterruptedException {
//        doTestException(new RuntimeException("test"));
//    }
//
////    @Test
////    public void testConvertToCompletableIOException() throws ExecutionException, InterruptedException {
////        Throwable exception = new IOException("test");
////        doTestException(exception);
////    }
//
//
//    private void doTestException(RuntimeException exception) throws InterruptedException {
//        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(() -> {
//            throw exception;
//        });
//        executorService.execute(listenable);
//
//        CompletableFuture<String> completable = toCompletableFuture(listenable);
//        Function<Throwable, ? extends String> fn = mock(Function.class);
//        CountDownLatch latch = new CountDownLatch(1);
//        completable.exceptionally(fn).thenRun(() -> latch.countDown());
//        try {
//            completable.get();
//            fail("Exception expected");
//        } catch (ExecutionException e) {
//            assertEquals(exception, e.getCause());
//        }
//        assertEquals(true, completable.isDone());
//        assertEquals(false, completable.isCancelled());
//
//        latch.await();
//        verify(fn).apply(exception);
//    }

}
