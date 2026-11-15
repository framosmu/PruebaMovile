

/***** Changes ***********
 * Date			Edit				Bug					Description
 * 04-Mar-15    jmg0203             ST/WR/201           Created.
 */

package net.omnis.android.about;

import android.os.Bundle;

import net.omnis.android.R;

/**
 * @author jgissing
 * Very simple webview activity, to display the credits.html file.
 * No loading bar etc really necessary as files are local, and should not be complex.
 */

public class CreditsActivity extends SimpleWebActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.string.credits, R.raw.credits, "");
    }
}
