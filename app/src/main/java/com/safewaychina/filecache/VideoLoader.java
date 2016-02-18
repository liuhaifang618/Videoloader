package com.safewaychina.filecache;

import android.content.Context;
import android.text.TextUtils;

import com.safewaychina.filecache.core.FileHttpRequest;
import com.safewaychina.filecache.core.FileLoaderEngine;
import com.safewaychina.filecache.core.RequestQueue;
import com.safewaychina.filecache.interf.FileResponseListener;
import com.safewaychina.filecache.utils.PathUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;


public class VideoLoader {


    private final AtomicBoolean paused = new AtomicBoolean(false);
    private VideoLoaderConfiguration configuration;
    private volatile static VideoLoader instance;
    private RequestQueue requestQueue;
    private FileLoaderEngine fileLoaderEngine;


    public static VideoLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (VideoLoader.class) {
                if (instance == null) {
                    instance = new VideoLoader(context);
                }
            }
        }
        return instance;
    }

    protected VideoLoader(Context context) {
        this.fileLoaderEngine = new FileLoaderEngine();
    }

    public void displayVideoView(String uri,VideoViewAware videoViewAware) {
        displayVideoView(uri,videoViewAware, defaultListener);
    }

    public void displayVideoView(String uri, VideoViewAware videoViewAware, FileResponseListener listener) {
        checkConfiguration();
        boolean isPause = paused.get();
        if (isPause) {
            return;
        }
        if (videoViewAware == null) {
            throw new IllegalArgumentException("VideoViewAware not be null");
        }

        if (TextUtils.isEmpty(uri)) {
            listener.onFailure(new RuntimeException("url not empty"));
            return;
        }

        FileHttpRequest request = fileLoaderEngine.get(uri, listener);
        String path = PathUtil.createPath(uri, configuration.downloadCache, configuration.fileNameGenerator);
        request.setFileAbsPath(path);
        request.setUrl(uri);
        requestQueue.add(request);

    }

    @Deprecated
    public void clearDiscCache() {
        clearDiskCache();
    }

    public void clearDiskCache() {
    }


    public void cancelDisplayTask(VideoViewAware imageAware) {
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
    }

    public void stop() {
    }

    private FileResponseListener defaultListener = new FileResponseListener() {
        @Override
        public void onFailure(Throwable throwable) {

        }

        @Override
        public void onSuccess(File file) {
        }
    };

    public synchronized void init(VideoLoaderConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("VideoLoaderConfiguration not be null");
        }
        if (this.configuration == null) {
            this.configuration = configuration;
            requestQueue = new RequestQueue(configuration.diskCache, configuration.threadPoolSize,
                    configuration.queueProcessingType, configuration.threadPriority);
            requestQueue.start();
        }
    }

    private void checkConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException("VideoLoaderConfiguration not be null");
        }
    }

    public VideoLoaderConfiguration getConfiguration() {
        return configuration;
    }

}
