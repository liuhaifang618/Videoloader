package com.safewaychina.filecache.core;

import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.safewaychina.filecache.diskcache.DiskLruCache;
import com.safewaychina.filecache.interf.FileResponse;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;


public class NetworkDispatcher extends Thread {
    /**
     * The queue of requests to service.
     */
    private final BlockingQueue<FileHttpRequest> mQueue;
    /**
     * The cache to write to.
     */
    private final DiskLruCache mCache;
    /**
     * For posting responses and errors.
     */
    private volatile boolean mQuit = false;

    /**
     * Creates a new network dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue    Queue of incoming requests for triage
     * @param cache    Cache interface to use for writing responses to cache
     */
    public NetworkDispatcher(BlockingQueue<FileHttpRequest> queue, DiskLruCache cache) {
        mQueue = queue;
        mCache = cache;
    }

    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }


    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            long startTimeMs = SystemClock.elapsedRealtime();
            FileHttpRequest request;
            try {
                // Take a request from the queue.
                request = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }
            final FileResponse responseHandler = request.getResponseHandler();
            if (request.isCancelled()) {
                return;
            }

            if (responseHandler != null) {
                responseHandler.sendStartMessage();
            }
            if (request.isCancelled()) {
                request.setFinished();
                return;
            }
            try {
                request.makeRequestWithRetries();
            } catch (IOException e) {
                if (!request.isCancelled() && responseHandler != null) {
                    responseHandler.sendFailureMessage(0, null, null, e);
                } else {
                    Log.e("AsyncHttpRequest", "makeRequestWithRetries returned error, but handler is null", e);
                }
            }

            if (request.isCancelled()) {
                return;
            }

            if (responseHandler != null) {
                responseHandler.sendFinishMessage();
            }

            mCache.put(request.getUrl(), request.getFileAbsPath());

            request.setFinished();

        }
    }
}




