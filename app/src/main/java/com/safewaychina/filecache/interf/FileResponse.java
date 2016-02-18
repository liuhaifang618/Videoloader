package com.safewaychina.filecache.interf;

import com.safewaychina.filecache.core.FileHttpRequest;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public abstract class FileResponse {


    /**
     * Returns data whether request completed successfully
     *
     * @param response HttpResponse object with data
     * @throws java.io.IOException if retrieving data from response fails
     */
    public abstract void sendResponseMessage(FileHttpRequest request,HttpResponse response) throws IOException;

    /**
     * Notifies callback, that request started execution
     */
    public abstract void sendStartMessage();

    /**
     * Notifies callback, that request was completed and is being removed from thread pool
     */
    public abstract void sendFinishMessage();

    /**
     * Notifies callback, that request (mainly uploading) has progressed
     *
     * @param bytesWritten number of written bytes
     * @param bytesTotal   number of total bytes to be written
     */
    public abstract void sendProgressMessage(int bytesWritten, int bytesTotal);

    /**
     * Notifies callback, that request was cancelled
     */
    public abstract void sendCancelMessage();

    /**
     * Notifies callback, that request was handled successfully
     *
     * @param statusCode   HTTP status code
     * @param headers      returned headers
     * @param responseBody returned data
     */
    public abstract void sendSuccessMessage(int statusCode, Header[] headers, byte[] responseBody);

    /**
     * Returns if request was completed with error code or failure of implementation
     *
     * @param statusCode   returned HTTP status code
     * @param headers      returned headers
     * @param responseBody returned data
     * @param error        cause of request failure
     */
    public abstract void sendFailureMessage(int statusCode, Header[] headers, byte[] responseBody, Throwable error);

    /**
     * Notifies callback of retrying request
     *
     * @param retryNo number of retry within one request
     */
    public abstract void sendRetryMessage(int retryNo);

    /**
     * Returns URI which was used to request
     *
     * @return uri of origin request
     */
    public abstract URI getRequestURI();

    /**
     * Returns Header[] which were used to request
     *
     * @return headers from origin request
     */
    public abstract Header[] getRequestHeaders();

    /**
     * Helper for handlers to receive Request URI info
     *
     * @param requestURI claimed request URI
     */
    public abstract void setRequestURI(URI requestURI);

    /**
     * Helper for handlers to receive Request Header[] info
     *
     * @param requestHeaders Headers, claimed to be from original request
     */
    public abstract void setRequestHeaders(Header[] requestHeaders);

    /**
     * Can set, whether the handler should be asynchronous or synchronous
     *
     * @param useSynchronousMode whether data should be handled on background Thread on UI Thread
     */
    public abstract void setUseSynchronousMode(boolean useSynchronousMode);

    /**
     * Returns whether the handler is asynchronous or synchronous
     *
     * @return boolean if the ResponseHandler is running in synchronous mode
     */
    public abstract boolean getUseSynchronousMode();


    /**
     * Method to be overriden, receives as much of response as possible
     *
     * @param file       file in which the response is stored
     */
    public abstract void onSuccess(File file);


    /**
     * Method to be overriden, receives as much of file as possible Called when the file is
     * considered failure or if there is error when retrieving file
     *
     * @param throwable  returned throwable
     */
    public abstract void onFailure(Throwable throwable);



}
