package com.example.md.gpwa;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

public class AlertManager
{
 // create dialog helper class
    public static void fCreateAlert(final Context pContext, final String pTitle, final String pMessage,
                                            final boolean bIsCancelable, final String pButtonText)
    {
        Handler mHandler = new Handler(pContext.getMainLooper());
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                new AlertDialog.Builder(pContext).setTitle(pTitle)
                    .setMessage(pMessage).setCancelable(bIsCancelable)
                    .setPositiveButton(pButtonText, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface pDialog, int pWhich)
                        {
                            pDialog.dismiss();
                        }
                    }).create().show();
            }
        });
    }

    public static void fCreateCallbackAlert(final Context pContext, final String pTitle, final String pMessage, final boolean bIsCancelable,
                                            final String pButtonText, final DialogInterface.OnClickListener pClickListener)
    {
        Handler mHandler = new Handler(pContext.getMainLooper());
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                new AlertDialog.Builder(pContext).setTitle(pTitle)
                    .setMessage(pMessage).setCancelable(bIsCancelable)
                    .setPositiveButton(pButtonText, pClickListener).create().show();
            }
        });
    }
}
