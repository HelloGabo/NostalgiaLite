package nostalgia.framework.ui.gamegallery;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nostalgia.framework.Emulator;
import nostalgia.framework.R;
import nostalgia.framework.base.EmulatorActivity;
import nostalgia.framework.remote.ControllableActivity;
import nostalgia.framework.ui.gamegallery.RomsFinder.OnRomsFinderListener;
import nostalgia.framework.utils.DatabaseHelper;
import nostalgia.framework.utils.DialogUtils;
import nostalgia.framework.utils.FileUtils;
import nostalgia.framework.utils.NLog;

@SuppressLint({"HandlerLeak"})
abstract public class BaseGameGalleryActivity extends ControllableActivity
        implements OnRomsFinderListener {

    private static final String TAG = "BaseGameGalleryActivity";

    protected Set<String> exts;
    protected Set<String> inZipExts;
    protected boolean reloadGames = true;
    protected boolean reloading = false;
    private RomsFinder romsFinder = null;
    private DatabaseHelper dbHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashSet<String> exts = new HashSet<String>(getRomExtensions());
        exts.addAll(getArchiveExtensions());
        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("android50comp",
                Context.MODE_PRIVATE);
        String androidVersion = Build.VERSION.RELEASE;

        if (!pref.getString("androidVersion", "").equals(androidVersion)) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.onUpgrade(db, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
            db.close();
            Editor editor = pref.edit();
            editor.putString("androidVersion", androidVersion);
            editor.apply();
            NLog.i(TAG, "Reinit DB " + androidVersion);
        }
        reloadGames = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!FileUtils.isSDCardRWMounted()) {
            showSDcardFailed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (romsFinder != null) {
            romsFinder.stopSearch();
        }
    }

    protected void reloadGames(boolean searchNew, File selectedFolder) {
        if (romsFinder == null) {
            reloadGames = false;
            reloading = searchNew;
            romsFinder = new RomsFinder(exts, inZipExts, this, this, searchNew,
                    selectedFolder);
            romsFinder.start();
        }
    }

    @Override
    public void onRomsFinderFoundGamesInCache(ArrayList<GameDescription> oldRoms) {
        setLastGames(oldRoms);
    }

    @Override
    public void onRomsFinderNewGames(ArrayList<GameDescription> roms) {
        setNewGames(roms);
    }

    @Override
    public void onRomsFinderEnd(boolean searchNew) {
        romsFinder = null;
        reloading = false;
    }

    @Override
    public void onRomsFinderCancel(boolean searchNew) {
        romsFinder = null;
        reloading = false;
    }

    protected void stopRomsFinding() {
        if (romsFinder != null) {
            romsFinder.stopSearch();
        }
    }

    public void showSDcardFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Builder builder = new Builder(BaseGameGalleryActivity.this);
                builder.setTitle(R.string.error);
                builder.setMessage(R.string.gallery_sd_card_not_mounted);
                builder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                builder.setPositiveButton(R.string.exit, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                DialogUtils.show(builder.create(), true);
            }
        });
    }


    public abstract Class<? extends EmulatorActivity> getEmulatorActivityClass();

    abstract public void setLastGames(ArrayList<GameDescription> games);

    abstract public void setNewGames(ArrayList<GameDescription> games);

    abstract protected Set<String> getRomExtensions();

    public abstract Emulator getEmulatorInstance();

    protected Set<String> getArchiveExtensions() {
        HashSet<String> set = new HashSet<String>();
        set.add("zip");
        return set;
    }

}
