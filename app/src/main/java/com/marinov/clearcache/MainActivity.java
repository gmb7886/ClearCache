package com.marinov.clearcache;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear temporary files? (cache)")
                .setMessage("All apps on the device will be closed.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    showProgressAndExecute();
                })
                .setNegativeButton("No", (dialog, which) -> finish())
                .setCancelable(false)
                .create()
                .show();
    }

    private void showProgressAndExecute() {
        // --- novo diálogo de progresso ---
        View progressView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_progress, null, false);

        // garante que o indicador comece animado
        CircularProgressIndicator cpi =
                progressView.findViewById(R.id.progress);
        if (cpi != null) {
            cpi.setIndeterminate(true);
            cpi.show();
        }

        AlertDialog progressDialog = new MaterialAlertDialogBuilder(this)
                .setView(progressView)
                .setCancelable(false)
                .create();

        // remover escurecimento de fundo
        progressDialog.setOnShowListener(d -> {
            if (progressDialog.getWindow() != null) {
                progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        });

        progressDialog.show();

        new Thread(() -> {
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream os = getDataOutputStream(su);
                os.flush();
                su.waitFor();
            } catch (Exception e) {
                Log.e(TAG, "Error while clearing cache", e);
            }

            runOnUiThread(() -> {
                progressDialog.dismiss();
                showRebootPrompt();
            });
        }).start();
    }

    @NonNull
    private static DataOutputStream getDataOutputStream(Process su) throws IOException {
        DataOutputStream os = new DataOutputStream(su.getOutputStream());
        os.writeBytes("for pkg in $(pm list packages | sed 's/^package://'); do\n");
        os.writeBytes("  if [ \"$pkg\" != \"com.marinov.clearcache\" ] "
                + "&& [ \"$pkg\" != \"com.android.systemui\" ]; then\n");
        os.writeBytes("    am force-stop \"$pkg\"\n");
        os.writeBytes("  fi\n");
        os.writeBytes("done\n");
        os.writeBytes("pm trim-caches 9999999999999\n");
        os.writeBytes("exit\n");
        return os;
    }

    private void showRebootPrompt() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Reboot device?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    rebootDevice();
                })
                .setNegativeButton("No", (dialog, which) -> finish())
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void rebootDevice() {
        new Thread(() -> {
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(su.getOutputStream());
                os.writeBytes("reboot\n");
                os.writeBytes("exit\n");
                os.flush();
                su.waitFor();
            } catch (Exception e) {
                Log.e(TAG, "Error while rebooting the device", e);
            }
        }).start();
    }
}
