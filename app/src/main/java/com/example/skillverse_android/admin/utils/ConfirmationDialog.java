package com.example.skillverse_android.admin.utils;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
public class ConfirmationDialog {
    public interface OnConfirmListener {
        void onConfirm();
    }
    public static void show(Context context, String title, String message, OnConfirmListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    public static void showDeleteConfirmation(Context context, String itemName, OnConfirmListener listener) {
        show(context,
             "Delete " + itemName,
             "Are you sure you want to delete this " + itemName.toLowerCase() + "? This action cannot be undone.",
             listener);
    }
}
