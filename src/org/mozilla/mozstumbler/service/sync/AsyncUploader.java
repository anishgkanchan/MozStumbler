package org.mozilla.mozstumbler.service.sync;

import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.AsyncTask;

/* Only one at a time may be uploading. If executed while another upload is in progress
* it will return immediately, and SyncResult is null. */
public class AsyncUploader extends AsyncTask<Void, Void, SyncResult> {
    public interface AsyncUploaderListener {
        public void onUploadComplete(SyncResult result);
    }

    private AsyncUploaderListener mListener;
    private SyncResult mSyncResult;
    private ContentResolver mContentResolver;

    private static boolean sIsUploading;

    // TODO: clarify how we want this accessed
    public boolean mShouldIgnoreWifiStatus = false;

    public AsyncUploader(AsyncUploaderListener listener, ContentResolver contentResolver) {
        mListener = listener;
        mContentResolver = contentResolver;
    }

    @Override
    protected SyncResult doInBackground(Void... voids) {
        if (sIsUploading)
            return null;

        sIsUploading = true;
        mSyncResult = new SyncResult();
        UploadReports uploadReports = new UploadReports(mContentResolver);
        uploadReports.uploadReports(mShouldIgnoreWifiStatus, mSyncResult);
        //TODO consider checking result for error, trying again after sleep

        return mSyncResult;
    }
    @Override
    protected void onPostExecute(SyncResult result) {
        if (mListener != null){
            mListener.onUploadComplete(result);
        }
        sIsUploading = false;
    }
    @Override
    protected void onCancelled(SyncResult result) {
        sIsUploading = false;
    }

    public SyncResult getSyncResult() { return mSyncResult; }
}