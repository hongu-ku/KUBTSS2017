package com.example.hongu.apaapa;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class SettingPrefActivity extends Activity {

    private static final String TAG = SettingPrefActivity.class.getSimpleName();

    static public final String PREF_KEY_LAPSE = "key_lapse";
    static public final String PREF_KEY_STANDARD = "key_standard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PrefFragmentの呼び出し
        getFragmentManager().beginTransaction().replace(
                android.R.id.content, new PrefFragment()).commit();
    }

    // 設定画面のPrefFragmentクラス
    public static class PrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.setting_pref);
            setSummaryLapse();
            setSummaryStandard();
        }

        // 設定値が変更されたときのリスナーを登録
        @Override
        public void onResume() {
            super.onResume();
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.registerOnSharedPreferenceChangeListener(listener);
        }

        // 設定値が変更されたときのリスナー登録を解除
        @Override
        public void onPause() {
            super.onPause();
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.unregisterOnSharedPreferenceChangeListener(listener);
        }

        // 設定変更時に、Summaryを更新
        private OnSharedPreferenceChangeListener listener
                = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                setSummaryLapse();
                setSummaryStandard();
            }
        };

        private void setSummaryLapse(){
            EditTextPreference edittext_preference = (EditTextPreference)getPreferenceScreen().findPreference(PREF_KEY_LAPSE);
            edittext_preference.setSummary(edittext_preference.getText());
        }

        private void setSummaryStandard(){
            EditTextPreference edittext_preference = (EditTextPreference)getPreferenceScreen().findPreference(PREF_KEY_STANDARD);
            edittext_preference.setSummary(edittext_preference.getText());
        }
    }
}