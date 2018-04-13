package com.formmicro.gpslogger.common;

import android.telephony.PhoneStateListener;;
import 	android.telephony.SignalStrength;

public class myPhoneStateListener extends PhoneStateListener {
    private PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();

    public int signalStrengthValue;

    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        if (signalStrength.isGsm()) {
            if (signalStrength.getGsmSignalStrength() != 99)
                signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
            else
                signalStrengthValue = signalStrength.getGsmSignalStrength();
        } else {
            signalStrengthValue = signalStrength.getCdmaDbm();
        }
        preferenceHelper.setSignalStrenght(Integer.toString(signalStrengthValue));
    }
}
