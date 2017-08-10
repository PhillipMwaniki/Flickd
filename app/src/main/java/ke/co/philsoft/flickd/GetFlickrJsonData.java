package ke.co.philsoft.flickd;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phillip Mwaniki on 10-Aug-17.
 */

class GetFlickrJsonData implements GetRawData.OnDownloadComplete{
    private static final String TAG = "GetFlickrJsonData";

    private List<Photo> mPhotoList = null;
    private String mBaseURL;
    private String mLanguage;
    private boolean mMatchAll;

    private final OnDataAvailable mCallBack;

    interface OnDataAvailable {
        void onDataAvailable(List<Photo> data, DownloadStatus status);
    }

    public GetFlickrJsonData(String baseURL, String language, boolean matchAll, OnDataAvailable callBack) {
        Log.d(TAG, "GetFlickrJsonData: called");
        mBaseURL = baseURL;
        mLanguage = language;
        mMatchAll = matchAll;
        mCallBack = callBack;
    }
    
    void executeOnSameThread(String searchCritera) {
        Log.d(TAG, "executeOnSameThread: starts");
        String destinationUri = createUri(searchCritera, mLanguage, mMatchAll);
        
        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(destinationUri);
        Log.d(TAG, "executeOnSameThread: ends");
    }

    private String createUri(String searchCriteria, String lang, boolean matchAll) {
        Log.d(TAG, "createUri: starts");

        return Uri.parse(mBaseURL).buildUpon()
                    .appendQueryParameter("tags", searchCriteria)
                    .appendQueryParameter("tagmode", matchAll ? "ALL" : "ANY")
                    .appendQueryParameter("lang", lang)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                .build().toString();
    }
    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        Log.d(TAG, "onDownloadComplete: starts. Status = " + status);

        if (status == DownloadStatus.OK) {
            mPhotoList = new ArrayList<>();
            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("items");

                for (int i=0; i < itemsArray.length(); i++) {
                    JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                    String title = jsonPhoto.getString("title");
                    String author = jsonPhoto.getString("author");
                    String authorId = jsonPhoto.getString("author_id");
                    String tags = jsonPhoto.getString("tags");

                    JSONObject jsonMedia = jsonPhoto.getJSONObject("media");
                    String photoUrl = jsonMedia.getString("m");

                    String link = photoUrl.replaceFirst("_m.", "_b."); // getting the big photo size by replacing the _m in the URL with _b

                    Photo photoObject = new Photo(title, author, authorId, link, tags, photoUrl);
                    mPhotoList.add(photoObject);

                    Log.d(TAG, "onDownloadComplete: " + photoObject.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Error parsing JSON: " + e.getMessage());
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }

        if (mCallBack != null) {
            //now inform the caller that processing is done - possibly returning null if there was
            //an error
            mCallBack.onDataAvailable(mPhotoList, status);
        }
    }
}
