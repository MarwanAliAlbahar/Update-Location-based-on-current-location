package com.example.md.gpwa;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkManager
{
    public static boolean fIsConnectionAvailable(Context pContext)
    {
        ConnectivityManager mConnectivity = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mConnectivity == null)
            return false;
        else
        {
            NetworkInfo[] mNetworkInfo = mConnectivity.getAllNetworkInfo();
            if(mNetworkInfo != null)
            {
                for(int i = 0; i < mNetworkInfo.length; i++)
                {
                    if(mNetworkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}