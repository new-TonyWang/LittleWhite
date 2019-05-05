package com.littlewhite;

import android.support.v7.app.AppCompatActivity;

public abstract class SendReceive<T> extends AppCompatActivity {
    protected T handler;

    public T getHandler() {
        return handler;
    }
}
