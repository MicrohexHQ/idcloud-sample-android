/**
 *
 * MIT License
 *
 * Copyright (c) 2019 Thales DIS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.gemalto.eziomobilesampleapp.helpers.ezio;


import com.gemalto.eziomobilesampleapp.Configuration;
import com.gemalto.eziomobilesampleapp.helpers.CMain;
import com.gemalto.eziomobilesampleapp.helpers.Protocols;
import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.authentication.AuthenticationModule;
import com.gemalto.idp.mobile.authentication.mode.biofingerprint.BioFingerprintAuthService;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthService;
import com.gemalto.idp.mobile.authentication.mode.pin.PinAuthInput;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.oath.OathDevice;
import com.gemalto.idp.mobile.otp.oath.OathFactory;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.OathToken;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathSettings;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;

// IMPORTANT: This source code is intended to serve training information purposes only. Please make sure to review our IdCloud documentation, including security guidelines.

/**
 * To enable all TOTP features like last OTP lifespan we need to keep instance of OATH device.
 * This class will help us keep everything on one place.
 */
public class TokenDevice {

    //region Defines

    /**
     * Keep all auth mode status in one struct.
     */
    public class TokenStatus {
        public boolean isTouchSupported = false;
        public boolean isFaceSupported = false;
        public boolean isTouchEnabled = false;
        public boolean isFaceEnabled = false;
    }

    private final OathToken mToken;
    private final OathDevice mDevice;

    //endregion

    //region Life Cycle

    /**
     * Creates a new {@code TokenDevice} object.
     * @param token
     * @throws IdpException
     */
    TokenDevice(final OathToken token) throws IdpException {
        final OathFactory factory = OathService.create(OtpModule.create()).getFactory();

        // Create device based on specific ocra suite.
        final SoftOathSettings oathSettings = factory.createSoftOathSettings();
        oathSettings.setOcraSuite(CMain.secureStringFromString(Configuration.C_CFG_OTP_OCRA_SUITE));
        mDevice = factory.createSoftOathDevice((SoftOathToken) token, oathSettings);
        mToken = token;
    }

    //endregion


    //region Public API

    /**
     * Retrieves the {@code OathDevice}.
     * @return {@code OathDevice}.
     */
    public OathDevice getDevice() {
        return mDevice;
    }

    /**
     * Retrieves the {@code OathToken}.
     * @return {@code OathToken}.
     */
    public OathToken getToken() {
        return mToken;
    }

    /**
     * Retrieves the {@code TokenStatus}.
     * @return {@code TokenStatus}.
     */
    public TokenStatus getTokenStatus() {
        final TokenStatus retValue = new TokenStatus();

        // Check all auth mode states so we can enable / disable proper buttons.
        final AuthenticationModule authMoule = AuthenticationModule.create();
        final BioFingerprintAuthService touchService = BioFingerprintAuthService.create(authMoule);
        final FaceAuthService faceService = FaceAuthService.create(authMoule);

        retValue.isTouchSupported = touchService.isSupported() && touchService.isConfigured();
        retValue.isFaceSupported = faceService.isInitialized() && faceService.isSupported() && faceService.isConfigured();
        try {
            retValue.isTouchEnabled = mToken.isAuthModeActive(touchService.getAuthMode());
        } catch (IdpException e) {
            retValue.isTouchEnabled = false;
        }
        try {
            retValue.isFaceEnabled = faceService.isInitialized() && mToken.isAuthModeActive(faceService.getAuthMode());
        } catch (IdpException e) {
            retValue.isFaceEnabled = false;
        }

        return retValue;
    }

    /**
     * Generates OTP with any supported {@code AuthInput}
     * @param authInput User authentication.
     * @param serverChallenge Server challenge.
     * @param handler Callback returned back to the application on completion.
     */
    public void totpWithAuthInput(final AuthInput authInput,
                                  final SecureString serverChallenge,
                                  final Protocols.OTPDelegate handler) {

        SecureString otp = null;
        String error = null;

        try {

            if (serverChallenge != null) {
                // Ocra does require multiauth enabled.
                if (!mToken.isMultiAuthModeEnabled()) {
                    mToken.upgradeToMultiAuthMode((PinAuthInput) authInput);
                }
                otp = mDevice.getOcraOtp(authInput, serverChallenge, null, null, null);
            } else {
                otp = mDevice.getTotp(authInput);

            }
        } catch (final IdpException exception) {
            error = exception.getLocalizedMessage();
        }

        // Notify listener
        if (handler != null) {
            handler.onOTPDelegateFinished(otp, error, authInput, serverChallenge);
        }

        // Wipe for security reasons.
        if (otp != null) {
            otp.wipe();
        }
    }

    //endregion
}
