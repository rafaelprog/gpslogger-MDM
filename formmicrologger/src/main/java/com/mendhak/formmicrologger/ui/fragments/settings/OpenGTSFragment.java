/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of Formmicro for Android.
 *
 * Formmicro for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Formmicro for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Formmicro for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.formmicro.gpslogger.ui.fragments.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.webkit.URLUtil;
import com.afollestad.materialdialogs.prefs.MaterialEditTextPreference;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.formmicro.gpslogger.R;
import com.formmicro.gpslogger.common.PreferenceHelper;
import com.formmicro.gpslogger.common.Strings;
import com.formmicro.gpslogger.common.network.Networks;
import com.formmicro.gpslogger.common.network.ServerType;
import com.formmicro.gpslogger.senders.PreferenceValidator;
import com.formmicro.gpslogger.ui.Dialogs;
import com.formmicro.gpslogger.ui.components.CustomSwitchPreference;
import com.formmicro.gpslogger.ui.fragments.PermissionedPreferenceFragment;

public class OpenGTSFragment extends PermissionedPreferenceFragment implements
        PreferenceValidator,
        OnPreferenceChangeListener,
        OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.opengtssettings);

        findPreference("autoopengts_enabled").setOnPreferenceChangeListener(this);
        findPreference("opengts_server").setOnPreferenceChangeListener(this);
        findPreference("opengts_server_port").setOnPreferenceChangeListener(this);
        findPreference("opengts_server_communication_method").setOnPreferenceChangeListener(this);
        findPreference("autoopengts_server_path").setOnPreferenceChangeListener(this);
        findPreference("opengts_device_id").setOnPreferenceChangeListener(this);
        findPreference("opengts_validatecustomsslcert").setOnPreferenceClickListener(this);

    }

    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("opengts_validatecustomsslcert")){
            Networks.beginCertificateValidationWorkflow(
                    getActivity(),
                    PreferenceHelper.getInstance().getOpenGTSServer(),
                    Strings.toInt(PreferenceHelper.getInstance().getOpenGTSServerPort(),443),
                    ServerType.HTTPS
            );
        }
        if (!isFormValid()) {
            Dialogs.alert(getString(R.string.autoftp_invalid_settings),
                    getString(R.string.autoftp_invalid_summary),
                    getActivity());
            return false;
        }
        return true;
    }

    private boolean isFormValid() {

        CustomSwitchPreference chkEnabled = (CustomSwitchPreference) findPreference("autoopengts_enabled");
        if(!chkEnabled.isChecked()) {
            return true;
        }

        MaterialEditTextPreference txtOpenGTSServer = (MaterialEditTextPreference) findPreference("opengts_server");
        MaterialEditTextPreference txtOpenGTSServerPort = (MaterialEditTextPreference) findPreference("opengts_server_port");
        MaterialListPreference txtOpenGTSCommunicationMethod = (MaterialListPreference) findPreference("opengts_server_communication_method");
        MaterialEditTextPreference txtOpenGTSServerPath = (MaterialEditTextPreference) findPreference("autoopengts_server_path");
        MaterialEditTextPreference txtOpenGTSDeviceId = (MaterialEditTextPreference) findPreference("opengts_device_id");

        return  txtOpenGTSServer.getText() != null && txtOpenGTSServer.getText().length() > 0
                && txtOpenGTSServerPort.getText() != null && isNumeric(txtOpenGTSServerPort.getText())
                && txtOpenGTSCommunicationMethod.getValue() != null && txtOpenGTSCommunicationMethod.getValue().length() > 0
                && txtOpenGTSDeviceId.getText() != null && txtOpenGTSDeviceId.getText().length() > 0
                && URLUtil.isValidUrl("http://" + txtOpenGTSServer.getText() + ":" + txtOpenGTSServerPort.getText() + txtOpenGTSServerPath.getText());

    }

    private static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }


    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    @Override
    public boolean isValid() {
        return isFormValid();
    }
}
