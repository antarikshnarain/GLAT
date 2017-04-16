package com.lasser.play.geomania.AsyncJava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.lasser.play.geomania.Login;

/**
 * Created by antar on 04-Feb-17.
 */

public class IncomingSms extends BroadcastReceiver {
    public static final String SMS_BUNDLE = "pdus";
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            String smsBody = "";
            String smsAddress = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                smsBody = smsMessage.getMessageBody().toString();
                smsAddress = smsMessage.getOriginatingAddress();
            }
            Log.d("MYAPP","Live Cap:"+smsAddress);
            //this will update the UI with message
            Login inst = Login.instance();
            inst.updateList(smsBody,smsAddress);
        }
    }
}
