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

package com.formmicro.gpslogger.ui.fragments;


import android.preference.PreferenceFragment;
import com.canelmas.let.DeniedPermission;
import com.canelmas.let.Let;
import com.canelmas.let.RuntimePermissionListener;
import com.canelmas.let.RuntimePermissionRequest;
import com.formmicro.gpslogger.R;
import com.formmicro.gpslogger.ui.Dialogs;

import java.util.List;

public class PermissionedPreferenceFragment extends PreferenceFragment implements RuntimePermissionListener {
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Let.handle(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onShowPermissionRationale(List<String> list, final RuntimePermissionRequest runtimePermissionRequest) {
        Dialogs.alert(getString(R.string.formmicrologger_permissions_rationale_title), getString(R.string.formmicrologger_permissions_rationale_message_basic), getActivity(), new Dialogs.MessageBoxCallback() {
            @Override
            public void messageBoxResult(int which) {
                runtimePermissionRequest.retry();
            }
        });
    }

    @Override
    public void onPermissionDenied(List<DeniedPermission> list) {
        boolean anyPermanentlyDeniedPermissions = false;
        for(DeniedPermission denial :list){
            if(denial.isNeverAskAgainChecked()){
                anyPermanentlyDeniedPermissions = true;
            }
        }

        if(anyPermanentlyDeniedPermissions){
            Dialogs.alert(getString(R.string.formmicrologger_permissions_rationale_title), getString(R.string.formmicrologger_permissions_permanently_denied), getActivity());
        }

    }


}
