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

package com.gemalto.idp.mobile.authentication.mode.face.ui.internal.manager;

import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthFrameEvent;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.ErrorMode;

// IMPORTANT: This source code is intended to serve training information purposes only. Please make sure to review our IdCloud documentation, including security guidelines.

/**
 * This wraps the onFrameReceived listener. After processing the frame, it will invoked 1 or more
 * of the following callbacks to update the UI
 */
public interface FaceVerifierUIListener {

    /**
     * when new frameEvent is available, use it to update the View for image captured by camera.
     * This is called on every frame event received
     * @param frameEvent
     */
    void onNewFrame(final FaceAuthFrameEvent frameEvent);

    /**
     * Updat UI on step changed, show the progress in UI
     * @param step
     * @param errorMode
     * @param surroudMode
     */
    void onStepChanged(int step, ErrorMode errorMode, ErrorMode surroudMode);

    /**
     * Update UI on Face Position changed
     * @param step
     * @param lastFrameFaceDetected
     */
    void onFacePositionChanged(int step, boolean lastFrameFaceDetected);
}