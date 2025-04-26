package com.marinov.clearcache;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class QuickTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();

        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   // CLEAR_TOP não é necessário

        /* ---------- Opt‑in para BAL a partir da API 35 ---------- */
        // A constante oficial é PendingIntent.FLAG_ALLOW_BAL (API 35).
        // Use valor literal para compilações <35 onde a flag ainda não existe.
        final int FLAG_ALLOW_BAL =
                (Build.VERSION.SDK_INT >= 35) ? 0x40000000 /*FLAG_ALLOW_BAL*/ : 0x40000000;

        int flags = PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE
                | FLAG_ALLOW_BAL;        // opt‑in explícito

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);

        // Único caminho: APIs 34+ exigem PendingIntent; em versões mais antigas
        // startActivityAndCollapse(Intent) ainda é aceito, mas usar o PI é seguro.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE /*34*/) {
            startActivityAndCollapse(pi);
        } else {
            try {
                startActivityAndCollapse(intent);     // API <=33
            } catch (UnsupportedOperationException e) {
                // fallback se o método não aceitar Intent (casos raros)
                startActivity(intent);
            }
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel("Clear Cache");
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_clear_cache));
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }
}
