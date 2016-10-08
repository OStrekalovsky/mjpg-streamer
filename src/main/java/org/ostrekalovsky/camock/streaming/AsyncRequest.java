package org.ostrekalovsky.camock.streaming;

import javax.servlet.*;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Oleg Strekalovsky on 23.08.2016.
 */
public class AsyncRequest {

    private static final int MAX_BUFFERED_FRAMES = 1;

    private final AsyncContext context;

    private final ServletOutputStream out;

    private volatile WriteListener listener;


    private final LinkedBlockingQueue<byte[]> queue;

    public final AtomicBoolean isDone = new AtomicBoolean(false);

    public AsyncRequest(AsyncContext context) throws IOException {
        this.context = context;
        this.out = this.getContext().getResponse().getOutputStream();
        this.context.addListener(new AsyncListener() {

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                isDone.getAndSet(true);
                AsyncRequest.this.context.complete();
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                isDone.getAndSet(true);
            }

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                isDone.getAndSet(true);
            }
        });

        queue = new LinkedBlockingQueue<>(MAX_BUFFERED_FRAMES);

    }

    public void onWriteWhenPossible(byte[] pckg) {
        if (!isDone.get()) {
            if (queue.offer(pckg) && listener == null) {
                listener = new WriteListener() {

                    @Override
                    public void onWritePossible() throws IOException {
                        try {
                            while (out.isReady()) {
                                try {
                                    byte pckg[] = queue.take();
                                    if (!out.isReady()) {
                                        return;
                                    }
                                    out.write(pckg, 0, pckg.length);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    isDone.getAndSet(true);
                                }
                            }
                        } catch (InterruptedException e) {
                            System.err.print("Interrupted exception on writing output to client.");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (t instanceof EOFException) {
                            AsyncRequest.this.getContext().complete();
                        } else {
                            AsyncRequest.this.getContext().complete();
                        }
                    }
                };
                out.setWriteListener(listener);
            }
        }
    }

    public AsyncContext getContext() {
        return context;
    }
}