package com.example.md.gpwa;

import org.json.JSONObject;

public interface ServerTaskAsyncCallback
{
   // handel the returned data
    public void onTaskComplete(JSONObject pJSONResponse);
    public void onTaskFailed();
}
