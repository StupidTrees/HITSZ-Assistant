package com.stupidtree.hita.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RefreshBroadcastReceiver extends BroadcastReceiver {
    private ActionListener listener;

    public void setListener(ActionListener l) {
        listener = l;
    }

    public interface ActionListener {
        void receive(Context context, Intent intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.receive(context, intent);
    }
}
