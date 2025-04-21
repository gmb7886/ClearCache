package com.marinov.clearcache;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class QuickTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent); // Usa startActivity diretamente, sem startActivityAndCollapse
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        tile.setLabel("Clear Cache");
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_clear_cache));
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}