/* $Header: $ */

/***** Changes ***********
 Date			Edit				Bug					Description
 04-Mar-15      jmg0203             ST/WR/201           Adapted to use SimpleWebActivity (allows localisation of about page).
 19-Jan-15      jmg0156                                 Changes to enable Material theming.

 ************************/

package net.omnis.android.about;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.omnis.android.R;

/**
 * 
 * @author jgissing
 * Very simple webview activity, to display the about.htm file.
 * No loading bar etc really necessary as files are local, and should not be complex.
 *
 */

public class About extends SimpleWebActivity { // jmg0203
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.string.abouttitle, R.raw.about, "about/"); // jmg0203
	}

    // Start jmg0203
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_credits) {
            Intent creditsIntent = new Intent(this, CreditsActivity.class);
            startActivity(creditsIntent);
            return true;
        }

        return false;
    }
    // End jmg0203
}
