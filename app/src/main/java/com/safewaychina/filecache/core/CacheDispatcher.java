/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.safewaychina.filecache.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import com.safewaychina.filecache.diskcache.DiskLruCache;
import com.safewaychina.filecache.interf.FileResponse;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class CacheDispatcher extends Thread {

    /**
     * The queue of requests coming in for triage.
     */
    private final BlockingQueue<FileHttpRequest> mCacheQueue;

    /**
     * The queue of requests going out to the network.
     */
    private final BlockingQueue<FileHttpRequest> mNetworkQueue;


    /**
     * The cache to read from.
     */
    private final DiskLruCache mCache;

    /**
     * Used for telling us to die.
     */
    private volatile boolean mQuit = false;

    /**
     * Creates a new cache triage dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param cacheQueue   Queue of incoming requests for triage
     * @param networkQueue Queue to post requests that require network to
     */
    public CacheDispatcher(
            BlockingQueue<FileHttpRequest> cacheQueue, BlockingQueue<FileHttpRequest> networkQueue,
            DiskLruCache cache) {
        mCacheQueue = cacheQueue;
        mNetworkQueue = networkQueue;
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

        // Make a blocking call to initialize the cache.

        while (true) {
            try {
                // Get a request from the cache triage queue, blocking until
                // at least one is available.
                final FileHttpRequest request = mCacheQueue.take();
                String key = request.getUrl();
                final FileResponse responseHandler = request.getResponseHandler();
                // If the request has been canceled, don't bother dispatching it.
                if (request.isCancelled()) {
                    responseHandler.sendCancelMessage();
                    request.setFinished();
                    continue;
                }
                // Attempt to retrieve this item from cache.

                final String filePath = mCache.getAsString(key);
                boolean checkFile = checkFilePath(filePath);
                if (checkFile){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            responseHandler.onSuccess(new File(filePath));
                            request.setFinished();
                        }
                    });
                }else{
                    mCache.remove(key);
                    mNetworkQueue.put(request);
                }

            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }
        }
    }

    private boolean checkFilePath(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File videoFile = new File(filePath);
            if (videoFile.exists() && videoFile.isFile()) {
                return true;
            }
        }
        return false;
    }
}
