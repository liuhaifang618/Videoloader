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


import com.safewaychina.filecache.deque.LIFOLinkedBlockingDeque;
import com.safewaychina.filecache.deque.LinkedBlockingDeque;
import com.safewaychina.filecache.deque.QueueProcessingType;
import com.safewaychina.filecache.diskcache.DiskLruCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class RequestQueue {

    /**
     * Callback interface for completed requests.
     */
    public static interface RequestFinishedListener<T> {
        /**
         * Called when a request has finished processing.
         */
        public void onRequestFinished(FileHttpRequest request);
    }

    /**
     * Used for generating monotonically-increasing sequence numbers for requests.
     */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    /**
     * Staging area for requests that already have a duplicate request in flight.
     * <p/>
     * <ul>
     * <li>containsKey(cacheKey) indicates that there is a request in flight for the given cache
     * key.</li>
     * <li>get(cacheKey) returns waiting requests for the given cache key. The in flight request
     * is <em>not</em> contained in that list. Is null if no requests are staged.</li>
     * </ul>
     */
    private final Map<String, Queue<FileHttpRequest>> mWaitingRequests =
            new HashMap<String, Queue<FileHttpRequest>>();

    /**
     * The set of all requests currently being processed by this RequestQueue. A Request
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
    private final Set<FileHttpRequest> mCurrentRequests = new HashSet<FileHttpRequest>();

    /**
     * The cache triage queue.
     */
    private final BlockingQueue<FileHttpRequest> mCacheQueue ;


    /**
     * The queue of requests that are actually going out to the network.
     */
    private final BlockingQueue<FileHttpRequest> mNetworkQueue ;


    /**
     * Number of network request dispatcher threads to start.
     */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    /**
     * Cache interface for retrieving and storing responses.
     */
    private final DiskLruCache mCache;


    /**
     * The network dispatchers.
     */
    private NetworkDispatcher[] mDispatchers;

    /**
     * The cache dispatcher.
     */
    private CacheDispatcher mCacheDispatcher;

    private List<RequestFinishedListener> mFinishedListeners =
            new ArrayList<RequestFinishedListener>();

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache          A Cache to use for persisting responses to disk
     * @param threadPoolSize Number of network dispatcher threads to create
     */
    public RequestQueue(DiskLruCache cache, int threadPoolSize,QueueProcessingType queueProcessingType,int threadPriority) {
        mCache = cache;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        if (queueProcessingType == QueueProcessingType.LIFO){
            mNetworkQueue = new LIFOLinkedBlockingDeque<>();
            mCacheQueue = new LIFOLinkedBlockingDeque<>();
        }else{
            mNetworkQueue = new LinkedBlockingDeque<>();
            mCacheQueue = new LinkedBlockingDeque<>();
        }
    }


    /**
     * Starts the dispatchers in this queue.
     */
    public void start() {
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create the cache dispatcher and start it.
        mCacheDispatcher = new CacheDispatcher(mCacheQueue, mNetworkQueue, mCache);
        mCacheDispatcher.start();

        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < mDispatchers.length; i++) {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(mNetworkQueue, mCache);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    /**
     * Stops the cache and network dispatchers.
     */
    public void stop() {
        if (mCacheDispatcher != null) {
            mCacheDispatcher.quit();
        }
        for (int i = 0; i < mDispatchers.length; i++) {
            if (mDispatchers[i] != null) {
                mDispatchers[i].quit();
            }
        }
    }

    /**
     * Gets a sequence number.
     */
    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    public DiskLruCache getCache() {
        return mCache;
    }

    /**
     * A simple predicate or filter interface for Requests, for use by
     * {@link RequestQueue#cancelAll(RequestFilter)}.
     */
    public interface RequestFilter {
        public boolean apply(FileHttpRequest request);
    }

    /**
     * Cancels all requests in this queue for which the given filter applies.
     *
     * @param filter The filtering function to use
     */
    public void cancelAll(RequestFilter filter) {
        synchronized (mCurrentRequests) {
            for (FileHttpRequest request : mCurrentRequests) {
                if (filter.apply(request)) {
                    request.cancel(true);
                }
            }
        }
    }


    /**
     * Adds a Request to the dispatch queue.
     *
     * @param request The request to service
     * @return The passed-in request
     */
    public FileHttpRequest add(FileHttpRequest request) {
        // Tag the request as belonging to this queue and add it to the set of current requests.
        request.setRequestQueue(this);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(request);
        }

        // If the request is uncacheable, skip the cache queue and go straight to the network.
        if (!request.isShouldFromCache()) {
            mNetworkQueue.add(request);
            return request;
        }

        // Insert request into stage if there's already a request with the same cache key in flight.
        synchronized (mWaitingRequests) {
            String cacheKey = request.getUrl();
            if (mWaitingRequests.containsKey(cacheKey)) {
                // There is already a request in flight. Queue up.
                Queue<FileHttpRequest> stagedRequests = mWaitingRequests.get(cacheKey);
                if (stagedRequests == null) {
                    stagedRequests = new LinkedList<FileHttpRequest>();
                }
                stagedRequests.add(request);
                mWaitingRequests.put(cacheKey, stagedRequests);
            } else {
                // Insert 'null' queue for this cacheKey, indicating there is now a request in
                // flight.
                mWaitingRequests.put(cacheKey, null);
                mCacheQueue.add(request);
            }
            return request;
        }
    }

    public void finish(FileHttpRequest request) {
        // Remove from the set of requests currently being processed.
        synchronized (mCurrentRequests) {
            mCurrentRequests.remove(request);
        }
        synchronized (mFinishedListeners) {
            for (RequestFinishedListener listener : mFinishedListeners) {
                listener.onRequestFinished(request);
            }
        }
        if (request.isShouldCache()){
            synchronized (mWaitingRequests) {
                String cacheKey = request.getUrl();
                Queue<FileHttpRequest> waitingRequests = mWaitingRequests.remove(cacheKey);
                if (waitingRequests != null) {
                    // Process all queued up requests. They won't be considered as in flight, but
                    // that's not a problem as the cache has been primed by 'request'.
                    mCacheQueue.addAll(waitingRequests);
                }
            }
        }

    }

    public <T> void addRequestFinishedListener(RequestFinishedListener listener) {
        synchronized (mFinishedListeners) {
            mFinishedListeners.add(listener);
        }
    }

    /**
     * Remove a RequestFinishedListener. Has no effect if listener was not previously added.
     */
    public void removeRequestFinishedListener(RequestFinishedListener listener) {
        synchronized (mFinishedListeners) {
            mFinishedListeners.remove(listener);
        }
    }
}
