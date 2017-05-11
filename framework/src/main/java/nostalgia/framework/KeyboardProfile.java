package nostalgia.framework;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.KeyEvent;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import nostalgia.framework.base.Migrator;
import nostalgia.framework.controllers.KeyboardController;
import nostalgia.framework.ui.preferences.PreferenceUtil;
import nostalgia.framework.utils.NLog;

public class KeyboardProfile implements Serializable {
    public static final String[] DEFAULT_PROFILES_NAMES = new String[]{
            "default", "ps3", "wiimote"
    };
    private static final long serialVersionUID = 5817859819275903370L;
    private static final String KEYBOARD_PROFILES_SETTINGS = "keyboard_profiles_pref";
    private static final String KEYBOARD_PROFILE_POSTFIX = "_keyboard_profile";
    private static final String TAG = "KeyboardProfile";
    public static String[] BUTTON_NAMES = null;
    public static String[] BUTTON_DESCRIPTIONS = null;
    public static int[] BUTTON_KEY_EVENT_CODES = null;
    public String name;
    public SparseIntArray keyMap = new SparseIntArray();

    public static KeyboardProfile createDefaultProfile() {
        KeyboardProfile profile = new KeyboardProfile();
        profile.name = "default";
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_LEFT,
                EmulatorController.KEY_LEFT);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_RIGHT,
                EmulatorController.KEY_RIGHT);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_UP, EmulatorController.KEY_UP);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_DOWN,
                EmulatorController.KEY_DOWN);
        profile.keyMap
                .put(KeyEvent.KEYCODE_ENTER, EmulatorController.KEY_START);
        profile.keyMap.put(KeyEvent.KEYCODE_SPACE,
                EmulatorController.KEY_SELECT);
        profile.keyMap.put(KeyEvent.KEYCODE_Q, EmulatorController.KEY_A);
        profile.keyMap.put(KeyEvent.KEYCODE_W, EmulatorController.KEY_B);
        profile.keyMap.put(KeyEvent.KEYCODE_A, EmulatorController.KEY_A_TURBO);
        profile.keyMap.put(KeyEvent.KEYCODE_S, EmulatorController.KEY_B_TURBO);
        return profile;
    }

    @SuppressLint("InlinedApi")
    public static KeyboardProfile createPS3Profile() {
        KeyboardProfile profile = new KeyboardProfile();
        profile.name = "ps3";
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_LEFT,
                EmulatorController.KEY_LEFT);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_RIGHT,
                EmulatorController.KEY_RIGHT);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_UP, EmulatorController.KEY_UP);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_DOWN,
                EmulatorController.KEY_DOWN);

        if (Build.VERSION.SDK_INT > 8) {
            profile.keyMap.put(KeyEvent.KEYCODE_BUTTON_START,
                    EmulatorController.KEY_START);
            profile.keyMap.put(KeyEvent.KEYCODE_BUTTON_SELECT,
                    EmulatorController.KEY_SELECT);
            profile.keyMap.put(KeyEvent.KEYCODE_BUTTON_B,
                    EmulatorController.KEY_A);
            profile.keyMap.put(100, EmulatorController.KEY_B);
            profile.keyMap.put(KeyEvent.KEYCODE_BUTTON_A,
                    EmulatorController.KEY_A_TURBO);
            profile.keyMap.put(99, EmulatorController.KEY_B_TURBO);
            profile.keyMap.put(KeyEvent.KEYCODE_BUTTON_R2,
                    KeyboardController.KEY_MENU);
            profile.keyMap.put(KeyEvent.KEYCODE_BUTTON_L2,
                    KeyboardController.KEY_BACK);
            profile.keyMap.put(KeyEvent.KEYCODE_BUTTON_L1,
                    KeyboardController.KEY_FAST_FORWARD);
        }

        return profile;
    }

    public static KeyboardProfile createWiimoteProfile() {
        KeyboardProfile profile = new KeyboardProfile();
        profile.name = "wiimote";
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_LEFT,
                EmulatorController.KEY_LEFT);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_RIGHT,
                EmulatorController.KEY_RIGHT);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_UP, EmulatorController.KEY_UP);
        profile.keyMap.put(KeyEvent.KEYCODE_DPAD_DOWN,
                EmulatorController.KEY_DOWN);
        profile.keyMap.put(KeyEvent.KEYCODE_P, EmulatorController.KEY_START);
        profile.keyMap.put(KeyEvent.KEYCODE_M, EmulatorController.KEY_SELECT);
        profile.keyMap.put(KeyEvent.KEYCODE_1, EmulatorController.KEY_B);
        profile.keyMap.put(KeyEvent.KEYCODE_2, EmulatorController.KEY_A);
        profile.keyMap.put(23, KeyboardController.KEY_MENU);
        profile.keyMap.put(KeyEvent.KEYCODE_H, KeyboardController.KEY_BACK);
        profile.keyMap.put(KeyEvent.KEYCODE_O, EmulatorController.KEY_LEFT
                + KeyboardController.PLAYER2_OFFSET);
        profile.keyMap.put(KeyEvent.KEYCODE_J, EmulatorController.KEY_RIGHT
                + KeyboardController.PLAYER2_OFFSET);
        profile.keyMap.put(KeyEvent.KEYCODE_I, EmulatorController.KEY_UP
                + KeyboardController.PLAYER2_OFFSET);
        profile.keyMap.put(KeyEvent.KEYCODE_K, EmulatorController.KEY_DOWN
                + KeyboardController.PLAYER2_OFFSET);
        profile.keyMap.put(KeyEvent.KEYCODE_PLUS, EmulatorController.KEY_START
                + KeyboardController.PLAYER2_OFFSET);
        profile.keyMap.put(KeyEvent.KEYCODE_MINUS,
                EmulatorController.KEY_SELECT
                        + KeyboardController.PLAYER2_OFFSET);
        profile.keyMap.put(KeyEvent.KEYCODE_COMMA, EmulatorController.KEY_B
                + KeyboardController.PLAYER2_OFFSET);
        profile.keyMap.put(KeyEvent.KEYCODE_PERIOD, EmulatorController.KEY_A
                + KeyboardController.PLAYER2_OFFSET);
        return profile;
    }

    public static KeyboardProfile getSelectedProfile(String gameHash,
                                                     Context context) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        String name = pref.getString("pref_game_keyboard_profile", "default");
        return load(context, name);
    }

    public static KeyboardProfile load(Context context, String name) {
        if (name != null) {
            SharedPreferences pref = context.getSharedPreferences(name
                    + KEYBOARD_PROFILE_POSTFIX, Context.MODE_PRIVATE);

            if (pref.getAll().size() != 0) {
                KeyboardProfile profile = new KeyboardProfile();
                profile.name = name;

                for (Entry<String, ?> entry : pref.getAll().entrySet()) {
                    String key = entry.getKey();
                    Integer value = (Integer) entry.getValue();
                    int nkey = Integer.parseInt(key);
                    int nvalue = value;
                    profile.keyMap.put(nkey, nvalue);
                }

                return profile;

            } else {
                NLog.i(TAG, "empty " + name + KEYBOARD_PROFILE_POSTFIX);

                if (name.equals("ps3")) {
                    return createPS3Profile();

                } else if (name.equals("wiimote")) {
                    return createWiimoteProfile();

                } else {
                    return createDefaultProfile();
                }
            }

        } else {
            return createDefaultProfile();
        }
    }

    public static ArrayList<String> getProfilesNames(Context context) {
        SharedPreferences pref = context.getSharedPreferences(
                KEYBOARD_PROFILES_SETTINGS, Context.MODE_PRIVATE);
        Set<String> prefNames = pref.getAll().keySet();
        ArrayList<String> names = new ArrayList<String>();

        for (String defName : DEFAULT_PROFILES_NAMES) {
            if (!prefNames.contains(defName))
                names.add(defName);
        }

        names.addAll(prefNames);
        return names;
    }

    public static boolean isDefaultProfile(String name) {
        boolean defProf = false;

        for (String defName : KeyboardProfile.DEFAULT_PROFILES_NAMES) {
            if (defName.equals(name)) {
                defProf = true;
            }
        }

        return defProf;
    }

    public static void restoreDefaultProfile(String name, Context context) {
        KeyboardProfile prof = null;

        if (name.equals("ps3")) {
            prof = createPS3Profile();

        } else if (name.equals("default")) {
            prof = createDefaultProfile();

        } else if (name.equals("wiimote")) {
            prof = createWiimoteProfile();
        }

        if (prof != null) {
            prof.save(context);

        } else {
            NLog.e(TAG, "Keyboard profile " + name + " is unknown!!");
        }
    }

    public boolean delete(Context context) {
        NLog.i(TAG, "delete profile " + name);
        SharedPreferences pref = context.getSharedPreferences(
                name + ".keyprof", Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        pref = context.getSharedPreferences(KEYBOARD_PROFILES_SETTINGS,
                Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.remove(name);
        editor.apply();
        return true;
    }

    public boolean save(Context context) {
        SharedPreferences pref = context.getSharedPreferences(name
                + KEYBOARD_PROFILE_POSTFIX, Context.MODE_PRIVATE);
        NLog.i(TAG, "save profile " + name + " " + keyMap);
        Editor editor = pref.edit();
        editor.clear();

        for (int i = 0; i < BUTTON_NAMES.length; i++) {
            int value = BUTTON_KEY_EVENT_CODES[i];
            int idx = keyMap.indexOfValue(value);
            int key = idx == -1 ? 0 : keyMap.keyAt(idx);

            if (key != 0) {
                NLog.i(TAG, "save " + BUTTON_NAMES[i] + " " + key + "->" + value);
                editor.putInt(key + "", value);
            }
        }

        editor.apply();

        if (!name.equals("default")) {
            pref = context.getSharedPreferences(KEYBOARD_PROFILES_SETTINGS,
                    Context.MODE_PRIVATE);
            editor = pref.edit();
            editor.putBoolean(name, true);
            editor.remove("default");
            editor.apply();
        }

        return true;
    }

    public static class PreferenceMigrator implements Migrator {

        @Override
        public void doExport(Context context, String baseDir) {
            migrate(PreferenceUtil.EXPORT, context, baseDir);
        }

        @Override
        public void doImport(Context context, String baseDir) {
            migrate(PreferenceUtil.IMPORT, context, baseDir);
        }

        private void migrate(int type, Context context, String baseDir) {
            File file = new File(baseDir, KEYBOARD_PROFILES_SETTINGS);
            SharedPreferences pref = context.getSharedPreferences(
                    KEYBOARD_PROFILES_SETTINGS, Context.MODE_PRIVATE);
            PreferenceUtil.migratePreferences(type, pref, file,
                    PreferenceUtil.NotFoundHandling.IGNORE);
            ArrayList<String> names = getProfilesNames(context);

            for (String name : names) {
                SharedPreferences keyPref = context.getSharedPreferences(name
                        + KEYBOARD_PROFILE_POSTFIX, Context.MODE_PRIVATE);
                PreferenceUtil.migratePreferences(type, keyPref, new File(
                                baseDir, name + KEYBOARD_PROFILE_POSTFIX),
                        PreferenceUtil.NotFoundHandling.IGNORE);
            }
        }

    }

}
