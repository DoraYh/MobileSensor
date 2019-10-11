package com.qianzuncheng.sensors.Network;


import com.qianzuncheng.sensors.MyApplication;
import com.qianzuncheng.sensors.Storage.Cache;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPostTask {
    public static final String TAG = "HttpPostTask";

    public void doPostFileAsync(String URL, final Cache.FilePack filePack, final String logDetail) {
        /*
        // post only file
        OkHttpClient client = new OkHttpClient();
        MediaType fileType = MediaType.parse("File/*");
        File file = new File(filePath);
        RequestBody body = RequestBody.create(fileType , file );
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();
        */
        File file = new File(filePack.filePath);
        OkHttpClient client = new OkHttpClient();
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("title","title")
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("file/*"), file)   )
                .build();

        Request.Builder requestBuilder = new Request.Builder();
        Request request = null;
        try {
            request = requestBuilder
                    .url(URL)
                    .post(multipartBody)
                    .build();
        } catch (Exception e) {
            // TODO: what to do here?
            //Log.uploadFailed(e.toString(), MyApplication.getContext());
            android.util.Log.d(TAG, "doPostFile: " + e.toString());
            return;
        }

        final long startPostTime = System.currentTimeMillis();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                /*
                Cache.getInstance(MyApplication.getContext()).append(
                        Cache.CATEGORY.UPLOAD_QUEUE,
                        filePack
                );
                */
                Log.uploadFailed(logDetail, MyApplication.getContext());
                android.util.Log.d(TAG, "onFailure: " + filePack.filePath);
                // regard as small bandwidth
                UploadQueue.getInstance(MyApplication.getContext()).addBandwidthSample(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    File postedFile = new File(filePack.filePath);
                    long fileLength = postedFile.length();
                    Cache.getInstance(MyApplication.getContext()).clear(postedFile);
                    Log.uploadSuccess(logDetail, MyApplication.getContext());
                    // android.util.Log.d(TAG, "onResponse: success " + filePack.filePath);

                    long totalTime = startPostTime - System.currentTimeMillis();
                    float bandwidth = (float) (fileLength * 1.0 / totalTime);
                    UploadQueue.getInstance(MyApplication.getContext()).addBandwidthSample(bandwidth);
                } else {
                    /*
                    Cache.getInstance(MyApplication.getContext()).append(
                            Cache.CATEGORY.UPLOAD_QUEUE,
                            filePack
                    );
                    */
                    Log.uploadFailed(logDetail, MyApplication.getContext());
                    // android.util.Log.d(TAG, "onResponse: failed " + filePack.filePath);
                    // regard as small bandwidth
                    UploadQueue.getInstance(MyApplication.getContext()).addBandwidthSample(0);
                }
            }
        });
    }

    public void doPostFileSync(String URL, final Cache.FilePack filePack, final String logDetail) {
        File file = new File(filePack.filePath);
        OkHttpClient client = new OkHttpClient();
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("title","title")
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("file/*"), file))
                .build();

        Request.Builder requestBuilder = new Request.Builder();
        Request request = null;
        try {
            request = requestBuilder
                    .url(URL)
                    .post(multipartBody)
                    .build();
        } catch (Exception e) {
            // TODO: what to do here?
            //Log.uploadFailed(e.toString(), MyApplication.getContext());
            android.util.Log.d(TAG, "doPostFile: " + e.toString());
            return;
        }

        final long startPostTime = System.currentTimeMillis();
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            Log.uploadFailed(logDetail, MyApplication.getContext());
            android.util.Log.d(TAG, "onFailure: " + filePack.filePath);
            // regard as small bandwidth
            UploadQueue.getInstance(MyApplication.getContext()).addBandwidthSample(0);
            return;
        }

        if(response.code() == 200) {
            File postedFile = new File(filePack.filePath);
            long fileLength = postedFile.length();
            Cache.getInstance(MyApplication.getContext()).clear(postedFile);
            Log.uploadSuccess(logDetail, MyApplication.getContext());

            long totalTime = startPostTime - System.currentTimeMillis();
            float bandwidth = (float) (fileLength * 1.0 / totalTime);
            UploadQueue.getInstance(MyApplication.getContext()).addBandwidthSample(bandwidth);
        } else {
            Log.uploadFailed(logDetail, MyApplication.getContext());
            // regard as small bandwidth
            UploadQueue.getInstance(MyApplication.getContext()).addBandwidthSample(0);
        }
    }
}

/*
public class HttpPostTask extends AsyncTask<String, String, String> {
    public static final String TAG = "HttpPostTask";
    private static final String FAILED_FLAG = "failed";
    private String logDetail;

    public HttpPostTask(){
        //set context variables if required
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String requestURL = params[0];
        String filePath = params[1];
        String charset = "UTF-8";
        logDetail = params[2];

        com.qianzuncheng.sensors.Network.Log.uploading(logDetail, MyApplication.getContext());

        MultipartPost multipartPost = null;
        try {
            multipartPost = new MultipartPost(requestURL, charset);

            // not adding form data so ignore this
            // This is to add parameter values

            multipartPost.addFormField(
                    myFormDataArray.get(i).getParamName(),
                    myFormDataArray.get(i).getParamValue()
            );

            //add file here.

            Thread.sleep(3000);
            File f = new File(filePath);
            Log.d(TAG, "doInBackground1: " + f.length());
            multipartPost.addFilePart("file", f);

            List<String> response = multipartPost.finish();
            Log.d(TAG, "doInBackground: server replied:");
            for (String line : response) {
                Log.d(TAG, "Upload Files Response:::" + line);
                // get your server response here.
                //responseString = line;
            }
        //} catch (IOException e) {
        //    Log.e(TAG, "doInBackground: " + e.toString());
        //    return FAILED_FLAG;
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: " + e.toString());
        }

        return filePath;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s != FAILED_FLAG) {
            // clear uploaded file
            Cache.getInstance(MyApplication.getContext()).clear(new File(s));
            com.qianzuncheng.sensors.Network.Log.uploadSuccess(logDetail, MyApplication.getContext());
        } else {
            com.qianzuncheng.sensors.Network.Log.uploadFailed(logDetail, MyApplication.getContext());
        }
        com.qianzuncheng.sensors.Network.Log.idle(MyApplication.getContext()); // for display purpose
    }
*/
