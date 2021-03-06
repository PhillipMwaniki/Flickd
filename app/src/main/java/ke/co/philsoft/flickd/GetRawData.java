package ke.co.philsoft.flickd;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}

/**
 * Created by mwani on 10-Aug-17.
 */

class GetRawData extends AsyncTask<String, Void, String> {
    private static final String TAG = "GetRawData";

    private DownloadStatus mDownloadStatus;
    private final OnDownloadComplete mCallback;

    interface OnDownloadComplete {
        /**
         * @Description: Called after download has been completed
         * @param data
         * @param status
         */
        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete mCallback) {
        this.mCallback = mCallback;
        this.mDownloadStatus = DownloadStatus.IDLE;
    }

    void runInSameThread(String s) {
        Log.d(TAG, "runInSameThread: starts");
        onPostExecute(doInBackground(s));

        Log.d(TAG, "runInSameThread: ends");
    }

    @Override
    protected void onPostExecute(String s) {
        if (mCallback != null) {
            mCallback.onDownloadComplete(s, mDownloadStatus);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (params == null) {
            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {

            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(params[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: The response code was " + response);

            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

//            String line;
//            while(null != (line = reader.readLine())) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append("\n");
            }

            mDownloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: Security exception. Needs permission ?: " + e.getMessage());
            Log.e(TAG, "doInBackground: IO Exception reading data: " + e.getMessage());
        } catch (SecurityException e) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream: " + e.getMessage());
                }
            }
        }
        return null;
    }
}
