package com.example.skillverse_android.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import com.example.skillverse_android.R;

public class LoadingUtils {
    private static Dialog dialog;

    public static void showLoading(Context context) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_loading_dialog);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.5f);
        }
        
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void hideLoading() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                     
                }
            }
            dialog = null;
        }
    }
}
