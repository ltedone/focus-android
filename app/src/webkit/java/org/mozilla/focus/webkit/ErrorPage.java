/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.focus.webkit;

import android.support.annotation.StringRes;
import android.util.SparseArray;
import android.webkit.WebView;

import org.mozilla.focus.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ErrorPage {

    private static final SparseArray<Integer> errorDescriptionMap;

    static {
        errorDescriptionMap = new SparseArray<>();

        errorDescriptionMap.put(TrackingProtectionWebViewClient.ERROR_HOST_LOOKUP, R.string.error_host_lookup);
        errorDescriptionMap.put(TrackingProtectionWebViewClient.ERROR_TIMEOUT, R.string.error_timeout);
    }

    public static boolean supportsErrorCode(final int errorCode) {
        return (errorDescriptionMap.get(errorCode) != null);
    }

    // Negative values correspond to WebViewClient.ERROR_*
    // Positive values correspond to HTTP error codes
    public static void loadErrorPage(final WebView webView, final String desiredURL, final int errorCode) {
        BufferedReader errorPageReader = null;
        @StringRes final Integer errorDescriptionID = errorDescriptionMap.get(errorCode);

        if (errorDescriptionID == null) {
            throw new IllegalArgumentException("Cannot load error description for unsupported errorcode=" + errorCode);
        }

        try {
            final InputStream errorPageStream = webView.getContext().getResources().openRawResource(R.raw.errorpage);
            errorPageReader = new BufferedReader(new InputStreamReader(errorPageStream));

            final StringBuilder outputBuffer = new StringBuilder();

            String line;
            while ((line = errorPageReader.readLine()) != null) {
                line = line.replace("%messageLong%", webView.getContext().getResources().getString(errorDescriptionID));
                line = line.replace("%messageShort%", desiredURL);
                line = line.replace("%button%", webView.getContext().getResources().getString(R.string.errorpage_refresh));

                outputBuffer.append(line);
            }

            webView.loadDataWithBaseURL(desiredURL, outputBuffer.toString(), "text/html", "UTF8", desiredURL);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to load error page data");
        } finally {
            try {
                if (errorPageReader != null) {
                    errorPageReader.close();
                }
            } catch (IOException e) {
                // There's pretty much nothing we can do here...
            }
        }
    }
}
