package nostalgia.framework.ui.preferences;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nostalgia.framework.KeyboardProfile;
import nostalgia.framework.R;
import nostalgia.framework.base.EmulatorHolder;
import nostalgia.framework.remote.VirtualDPad;

public class GeneralPreferenceActivity extends PreferenceActivity {

    static String[] keyboardProfileNames = null;
    static ListPreference selProfile;
    static Preference edProfile;
    private static String NEW_PROFILE = null;

    static void initProPreference(Preference pref, final Activity activity) {
    }

    static void initDDPAD(CheckBoxPreference ddpad, final Activity activity) {
    }

    static void initScreenSettings(Preference screenSettings, final Activity activity) {
    }

    static void initInputMethodPreference(Preference imPreference, final Activity activity) {
        imPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        InputMethodManager imeManager = (InputMethodManager) activity
                                .getApplicationContext().getSystemService(
                                        INPUT_METHOD_SERVICE);

                        if (imeManager != null) {
                            imeManager.showInputMethodPicker();

                        } else {
                            Toast.makeText(
                                    activity,
                                    R.string.pref_keyboard_cannot_change_input_method,
                                    Toast.LENGTH_LONG).show();
                        }

                        return false;
                    }
                });
    }

    static void initFastForward(CheckBoxPreference ff, final Activity activity) {
    }

    static void initQuality(PreferenceCategory cat, Preference pref) {
        if (EmulatorHolder.getInfo().getNumQualityLevels() == 0) {
            cat.removePreference(pref);
        }
    }

    public static void setNewProfile(ListPreference listProfile,
                                     Preference editProfile, String name) {
        listProfile.setSummary(name);
        editProfile.setSummary(name);
        editProfile.setTitle(R.string.key_profile_edit);
        editProfile.getIntent().putExtra(
                KeyboardSettingsActivity.EXTRA_PROFILE_NAME, name);
    }

    public static void initProfiles(final Activity context,
                                    final ListPreference selectProfile, final Preference editProfile) {
        selProfile = selectProfile;
        edProfile = editProfile;
        selectProfile.setEntries(keyboardProfileNames);
        selectProfile.setEntryValues(keyboardProfileNames);
        selectProfile.setDefaultValue("default");

        if (selectProfile.getValue() == null) {
            selectProfile.setValue("default");
        }

        selectProfile
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        if (newValue.equals(NEW_PROFILE)) {
                            Intent i = new Intent(context,
                                    KeyboardSettingsActivity.class);
                            i.putExtra(
                                    KeyboardSettingsActivity.EXTRA_PROFILE_NAME,
                                    "default");
                            i.putExtra(KeyboardSettingsActivity.EXTRA_NEW_BOOL,
                                    true);
                            context.startActivityForResult(i, 0);
                            return false;

                        } else {
                            setNewProfile(selectProfile, editProfile,
                                    (String) newValue);
                            return true;
                        }
                    }
                });
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NEW_PROFILE = getText(R.string.key_profile_new).toString();
        initKeyboardProfiles();

    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.general_preferences_header, target);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VirtualDPad.getInstance().onResume(getWindow());
        initKeyboardProfiles();
        boolean found = false;

        for (String keyboardProfileName : keyboardProfileNames) {
            if (keyboardProfileName.equals(selProfile.getValue())) {
                found = true;
                break;
            }
        }

        if (!found) {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(this);
            Editor edit = pref.edit();
            edit.putString("pref_game_keyboard_profile", "default");
            edit.commit();
            setNewProfile(selProfile, edProfile, "default");
            selProfile.setValue("default");
            selProfile.setEntries(keyboardProfileNames);
            selProfile.setEntryValues(keyboardProfileNames);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        VirtualDPad.getInstance().onPause();
    }

    private void initKeyboardProfiles() {
        ArrayList<String> names = KeyboardProfile.getProfilesNames(this);
        keyboardProfileNames = new String[names.size() + 1];
        int i = 0;

        for (String name : names) {
            keyboardProfileNames[i++] = name;
        }

        keyboardProfileNames[names.size()] = NEW_PROFILE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != KeyboardSettingsActivity.RESULT_NAME_CANCEL) {
            ArrayList<String> profileNames = KeyboardProfile
                    .getProfilesNames(this);
            keyboardProfileNames = new String[profileNames.size() + 1];
            int i = 0;

            for (String name : profileNames) {
                keyboardProfileNames[i++] = name;
            }

            keyboardProfileNames[profileNames.size()] = NEW_PROFILE;
            String name = null;
            name = data
                    .getStringExtra(KeyboardSettingsActivity.EXTRA_PROFILE_NAME);
            selProfile.setValue(name);
            setNewProfile(selProfile, edProfile, name);
            initProfiles(this, selProfile, edProfile);
        }
    }
}
