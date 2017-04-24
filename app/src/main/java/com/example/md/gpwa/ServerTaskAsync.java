package com.example.md.gpwa;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerTaskAsync
{
    //   working with online task making call to php in order to get data
    final String URL_STRING;

    public ServerTaskAsync(String pUrl)
    {
        URL_STRING = pUrl;
    }

    public void fExcute(final String[] pKeys, final String[] pValues, final ServerTaskAsyncCallback pCallback)
    {
       // take the restauramt data from php file
        new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground(Void... v)
            {
                JSONObject mJSONData = new JSONObject();
                try
                {
                    for(int i = 0; i < pKeys.length; i++)
                        mJSONData.put(pKeys[i], pValues[i]);

                    URL mUrl = new URL(URL_STRING);
                    HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
                    mConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    mConnection.setRequestMethod("POST");
                    mConnection.setConnectTimeout(60000);

                    OutputStreamWriter mWriter = new OutputStreamWriter(mConnection.getOutputStream());
                    mWriter.write(mJSONData.toString());
                    mWriter.flush();
                    mWriter.close();

                    BufferedReader mReader = new BufferedReader(new InputStreamReader(mConnection.getInputStream()));
                    String text;
                    StringBuffer mResponse = new StringBuffer();
                    while((text = mReader.readLine()) != null)
                        mResponse.append(text);
                    mReader.close();

                    Log.e("Response", mResponse.toString());
                    if(mResponse.length() == 0)
                        pCallback.onTaskFailed();
                    else
                        pCallback.onTaskComplete(new JSONObject(mResponse.toString()));
                }
                catch(Exception e)
                {
	                Log.e("Exception", e.getMessage(), e);
                    pCallback.onTaskFailed();
                }
                return null;
            }
        }.execute();
    }
}