package com.example.finalproject.Utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class MyUtils {
    private static MyUtils instance;
    private Context context;

    private MyUtils(Context context) {
        this.context = context.getApplicationContext();
    }

    public static MyUtils getInstance(Context context) {
        if (instance == null) {
            instance = new MyUtils(context);
        }
        return instance;
    }

    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
