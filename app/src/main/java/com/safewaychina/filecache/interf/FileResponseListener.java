package com.safewaychina.filecache.interf;

import com.safewaychina.filecache.core.FileLoaderEngine;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class FileResponseListener extends FileResponseImpl {

    private static final String LOG_TAG = "FileAsyncHttpResponseHandler";

    /**
     * Obtains new FileAsyncHttpResponseHandler and stores response in passed file
     *
     */
    public FileResponseListener() {
        super();
    }


    /**
     * Attempts to delete file with stored response
     *
     * @return false if the file does not exist or is null, true if it was successfully deleted
     */
    public boolean deleteTargetFile() {
        return getTargetFile() != null && getTargetFile().delete();
    }


    /**
     * Retrieves File object in which the response is stored
     *
     * @return File file in which the response is stored
     */
    protected File getTargetFile() {
        return new File(fileAbsPath);
    }

    @Override
    public final void onFailure(int statusCode, Header[] headers, byte[] responseBytes, Throwable throwable) {
        onFailure(throwable);
    }


    @Override
    public final void onSuccess(int statusCode, Header[] headers, byte[] responseBytes) {
        onSuccess(getTargetFile());
    }


    @Override
    protected byte[] getResponseData(HttpEntity entity) throws IOException {
        if (entity != null) {
            InputStream instream = entity.getContent();
            long contentLength = entity.getContentLength();
            FileOutputStream buffer = new FileOutputStream(getTargetFile());
            if (instream != null) {
                try {
                    byte[] tmp = new byte[BUFFER_SIZE];
                    int l, count = 0;
                    // do not send messages if request has been cancelled
                    while ((l = instream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted()) {
                        count += l;
                        buffer.write(tmp, 0, l);
                        sendProgressMessage(count, (int) contentLength);
                    }
                } finally {
                    FileLoaderEngine.silentCloseInputStream(instream);
                    buffer.flush();
                    FileLoaderEngine.silentCloseOutputStream(buffer);
                }
            }
        }
        return null;
    }



}
