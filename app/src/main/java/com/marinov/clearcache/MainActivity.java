package com.marinov.clearcache;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.io.DataOutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Clear temporary files? (cache)")
                .setMessage("All apps on the device will be closed.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showProgressAndExecute();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
        // Remove escurecimento de fundo
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void showProgressAndExecute() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Clearing cache...");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());

                    os.writeBytes("for pkg in $(pm list packages | sed 's/^package://'); do\n");
                    os.writeBytes("  if [ \"$pkg\" != \"com.marinov.clearcache\" ] "
                            + "&& [ \"$pkg\" != \"com.android.systemui\" ]; then\n");
                    os.writeBytes("    am force-stop \"$pkg\"\n");
                    os.writeBytes("  fi\n");
                    os.writeBytes("done\n");
                    os.writeBytes("pm trim-caches 9999999999999\n");
                    os.writeBytes("exit\n");
                    os.flush();
                    su.waitFor();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        showRebootPrompt();
                    }
                });
            }
        }).start();
    }

    private void showRebootPrompt() {
        // Diálogo de reinício respeitando tema claro/escuro
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Reboot device?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        rebootDevice();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void rebootDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process su = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(su.getOutputStream());
                    os.writeBytes("reboot\n");
                    os.writeBytes("exit\n");
                    os.flush();
                    su.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}