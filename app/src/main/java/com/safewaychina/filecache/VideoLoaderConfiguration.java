package com.safewaychina.filecache;

import android.content.Context;
import android.content.res.Resources;

import com.safewaychina.filecache.deque.QueueProcessingType;
import com.safewaychina.filecache.diskcache.DiskLruCache;
import com.safewaychina.filecache.naming.FileNameGenerator;
import com.safewaychina.filecache.naming.Md5FileNameGenerator;
import com.safewaychina.filecache.utils.VideoStorageUtils;

import java.io.File;
import java.io.IOException;


public final class VideoLoaderConfiguration {

    final Resources resources;
    final DiskLruCache diskCache;
    final FileNameGenerator fileNameGenerator;
    final int threadPoolSize;
    final int threadPriority;
    final File downloadCache;
    final QueueProcessingType queueProcessingType;

    private VideoLoaderConfiguration(final Builder builder) {
        resources = builder.context.getResources();
        diskCache = builder.diskCache;
        fileNameGenerator = builder.diskCacheFileNameGenerator;
        threadPoolSize = builder.threadPoolSize;
        threadPriority = builder.threadPriority;
        downloadCache = builder.downloadCache;
        queueProcessingType = builder.queueProcessingType;
    }

    public static VideoLoaderConfiguration createDefault(Context context) {
        return new Builder(context).build();
    }


    public static class Builder {

        public static final int DISK_CACHE_MAX_COUNT = 1024 * 1024 * 10;
        public static final int DEFAULT_THREAD_POOL_SIZE = 4;
        public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 2;
        public static final QueueProcessingType DEFAULT_TASK_PROCESSING_TYPE = QueueProcessingType.FIFO;


        private Context context;
        private DiskLruCache diskCache = null;
        private FileNameGenerator diskCacheFileNameGenerator = null;
        private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        private int threadPriority = DEFAULT_THREAD_PRIORITY;
        private File downloadCache;
        private QueueProcessingType queueProcessingType = DEFAULT_TASK_PROCESSING_TYPE;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder threadPriority(int threadPriority) {
            if (threadPriority < Thread.MIN_PRIORITY) {
                this.threadPriority = Thread.MIN_PRIORITY;
            } else {
                if (threadPriority > Thread.MAX_PRIORITY) {
                    this.threadPriority = Thread.MAX_PRIORITY;
                } else {
                    this.threadPriority = threadPriority;
                }
            }
            return this;
        }

        public Builder discCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
            return diskCacheFileNameGenerator(fileNameGenerator);
        }

        public Builder diskCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
            this.diskCacheFileNameGenerator = fileNameGenerator;
            return this;
        }

        public Builder downloadCache(File fileCache) {
            this.downloadCache = fileCache;
            return this;
        }

        public Builder discCache(DiskLruCache diskCache) {
            return diskCache(diskCache);
        }

        public Builder diskCache(DiskLruCache diskCache) {
            this.diskCache = diskCache;
            return this;
        }

        public Builder threadPoolSize(int threadSize) {
            this.threadPoolSize = threadSize;
            return this;
        }

        public Builder queueProcessingType(QueueProcessingType queueProcessingType) {
            this.queueProcessingType = queueProcessingType;
            return this;
        }


        public VideoLoaderConfiguration build() {
            initEmptyFieldsWithDefaultValues();
            return new VideoLoaderConfiguration(this);
        }

        private void initEmptyFieldsWithDefaultValues() {
            if (diskCache == null) {
                diskCache = createReserveDiskCacheDir(context);
            }
            if (diskCacheFileNameGenerator == null) {
                diskCacheFileNameGenerator = new Md5FileNameGenerator();
            }
            if (downloadCache == null) {
                downloadCache = createDefualtDownPath(context);
            }
        }
    }

    private static DiskLruCache createReserveDiskCacheDir(Context context) {
        try {
            File cacheDir = VideoStorageUtils.getCacheDirectory(context, true);
            File individualDir = new File(cacheDir, "file-caches");
            if (individualDir.exists() || individualDir.mkdir()) {
                cacheDir = individualDir;
            }
            return new DiskLruCache(context, cacheDir, 1024 * 1024 * 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File createDefualtDownPath(Context context) {
        try {
            File cacheDir = VideoStorageUtils.getCacheDirectory(context, true);
            String path = cacheDir + File.separator + "file-caches";
            File individualDir = new File(path, "down-caches");
            if (individualDir.exists() || individualDir.mkdir()) {
                return individualDir;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Resources getResources() {
        return resources;
    }

    public DiskLruCache getDiskCache() {
        return diskCache;
    }

    public FileNameGenerator getFileNameGenerator() {
        return fileNameGenerator;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public File getDownloadCache() {
        return downloadCache;
    }

    public QueueProcessingType getQueueProcessingType() {
        return queueProcessingType;
    }
}
