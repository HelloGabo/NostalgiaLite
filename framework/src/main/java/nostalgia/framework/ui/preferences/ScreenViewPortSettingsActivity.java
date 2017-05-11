package nostalgia.framework.ui.preferences;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import nostalgia.framework.GfxProfile;
import nostalgia.framework.R;
import nostalgia.framework.SlotInfo;
import nostalgia.framework.base.EmulatorUtils;
import nostalgia.framework.base.GameMenu;
import nostalgia.framework.base.GameMenu.GameMenuItem;
import nostalgia.framework.base.GameMenu.OnGameMenuListener;
import nostalgia.framework.base.SlotUtils;
import nostalgia.framework.ui.gamegallery.GameDescription;
import nostalgia.framework.ui.multitouchbutton.MultitouchLayer;
import nostalgia.framework.ui.multitouchbutton.MultitouchLayer.EDIT_MODE;
import nostalgia.framework.utils.DatabaseHelper;

public class ScreenViewPortSettingsActivity extends AppCompatActivity implements
        OnGameMenuListener {

    MultitouchLayer mtLayer;
    String gameHash = "";
    DatabaseHelper dbHelper;
    Bitmap lastGameScreenshot;
    private GameMenu gameMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controler_layout);
        gameMenu = new GameMenu(this, this);
        mtLayer = (MultitouchLayer) findViewById(R.id.touch_layer);
        dbHelper = new DatabaseHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mtLayer.setEditMode(EDIT_MODE.SCREEN);
        GameDescription games = dbHelper.selectObjFromDb(GameDescription.class,
                "where lastGameTime!=0 ORDER BY lastGameTime DESC LIMIT 1");
        GfxProfile gfxProfile = null;
        lastGameScreenshot = null;

        if (games != null) {
            SlotInfo info = SlotUtils.getSlot(EmulatorUtils.getBaseDir(this),
                    games.checksum, 0);
            lastGameScreenshot = info.screenShot;
        }

        gfxProfile = PreferenceUtil.getLastGfxProfile(this);
        mtLayer.setLastgameScreenshot(lastGameScreenshot,
                gfxProfile == null ? null : gfxProfile.name);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mtLayer.saveScreenElement();
        mtLayer.stopEditMode();

        if (lastGameScreenshot != null) {
            lastGameScreenshot.recycle();
            lastGameScreenshot = null;
        }
    }


    @Override
    public void onGameMenuCreate(GameMenu menu) {
        menu.add(R.string.act_tcs_reset, R.drawable.ic_restart);
    }

    @Override
    public void onGameMenuPrepare(GameMenu menu) {
    }

    @Override
    public void onGameMenuOpened(GameMenu menu) {
    }

    @Override
    public void onGameMenuClosed(GameMenu menu) {
    }

    @Override
    public void onGameMenuItemSelected(GameMenu menu, GameMenuItem item) {
        runOnUiThread(new Runnable() {
            public void run() {
                mtLayer.resetScreenElement();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openGameMenu();
            return true;

        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void openGameMenu() {
        gameMenu.open();
    }

    @Override
    public void openOptionsMenu() {
        gameMenu.open();
    }

}
