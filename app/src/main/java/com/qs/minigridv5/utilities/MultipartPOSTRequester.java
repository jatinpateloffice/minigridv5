package com.qs.minigridv5.utilities;

import android.util.Log;
import com.qs.minigridv5.misc.C;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

public class MultipartPOSTRequester {

    private final        String            boundary;
    private static final String            LINE_FEED = "\r\n";
    private              HttpURLConnection httpConn;
    private              String            charset;
    private final        DataOutputStream  out;
    private              VideoUploader     videoUploader;
    public boolean isCanceled = false;

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     */
    public MultipartPOSTRequester(VideoUploader videoUploader, String requestURL, String charset, String auth_key)
            throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        Log.e("URL", "URL : " + requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        httpConn.setRequestProperty("ENCTYPE", "multipart/form-data");
        httpConn.setRequestProperty("authorization", auth_key);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        httpConn.setChunkedStreamingMode(-1);
        out = new DataOutputStream(httpConn.getOutputStream());

        this.videoUploader = videoUploader;
    }

    /**
     * Adds a form field to the request
     */
    public void addFormField(String name, String value) throws IOException {

        final StringBuilder builder = new StringBuilder();
        builder
                .append("--" + boundary)
                .append(LINE_FEED)
                .append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED)
                .append("Content-Type: text/plain; charset=" + charset)
                .append(LINE_FEED)
                .append(LINE_FEED)
                .append(value)
                .append(LINE_FEED);

        out.writeBytes(builder.toString());
        out.flush();
        Log.i(C.T, builder.toString());
    }

    /**
     * Adds a upload file section to the request
     */
    public int addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();

        final StringBuilder builder = new StringBuilder();
        builder
                .append("--" + boundary)
                .append(LINE_FEED)
                .append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED)
                .append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED)
                .append("Content-Transfer-Encoding: binary")
                .append(LINE_FEED)
                .append(LINE_FEED);

        out.writeBytes(builder.toString());
        out.flush();
        Log.i(C.T, builder.toString());

        int progress = 0;
        final int totalSize = (int) uploadFile.length();
        int             bytesRead   = -1;
        byte[]          buffer      = new byte[1024];

        FileInputStream inputStream = new FileInputStream(uploadFile);
        while ((bytesRead = inputStream.read(buffer)) != -1) {

            if(isCanceled)
                break;

            out.write(buffer, 0, bytesRead);
            try {
                out.flush();
            } catch (SocketException se) {
                // internet closed
                inputStream.close();
                return videoUploader.ERROR_INTERNET_LOST;

            } catch (InterruptedIOException se){
                //  cancelled
                inputStream.close();
                return videoUploader.ERROR_UPLOAD_CANCELLED;
            }
            progress += bytesRead;
            videoUploader.onProgressUpdate((progress / 1000000)+ " MB / " + (totalSize / 1000000) + " MB", progress);


        }
        out.flush();
        inputStream.close();
        Log.e(C.T, "done writing the file to outputstream. ");

        return 0;

    }

    /**
     * Adds a header field to the request.
     */
    public void addHeaderField(String name, String value) throws IOException {

        final String string = name + ": " + value + LINE_FEED;
        out.writeBytes(string);
        out.flush();
        Log.i(C.T, string);

    }

    /**
     * Completes the request and receives response from the server.
     */
    public String finish() throws IOException {

        final String tail = LINE_FEED + "--" + boundary + "--" + LINE_FEED;
        out.writeBytes(tail);
        out.flush();
        out.close();


        Log.e(C.T, "checks server's status code first");
        videoUploader.onProgressUpdate("finishing upload...", -1);

        // checks server's status code first
        int          status   = httpConn.getResponseCode();
        StringBuffer response = new StringBuffer();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String         line   = null;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            videoUploader.onProgressUpdate("failed", -1);
            return videoUploader.errorResponseString;
        }

        return response.toString();
    }

    public void terminateUpload() throws IOException {

        isCanceled = true;
        if (httpConn != null) {
            out.close();
            httpConn.disconnect();
        }

    }


}
