/**
 * /***** Changes ***********
 * Date			Edit				Bug					Description
 * 27-Nov-17		jmg0661																	Updated project for Android Studio 3.0 & latest plugins etc
 * 04-Mar-15    jmg0203             ST/WR/201           Created as base class for About & CreditsActivity.
 * 19-Jan-15    jmg0156                                 Changes to enable Material theming.
 */

package net.omnis.android.about;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Toast;

import net.omnis.android.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * @author jgissing
 * Very simple webview activity, to display a html file.
 * No loading bar etc really necessary as files are local, and should not be complex.
 */
public class SimpleWebActivity extends AppCompatActivity { // jmg0203 // jmg0661

    private WebView mWebView;

    protected void onCreate(Bundle savedInstanceState, int titleResId, int htmlResId, String assetsPath) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        mWebView = findViewById(R.id.webviewAbout);
        if (mWebView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mWebView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        //Set actionbar title:
        ActionBar actionBar = getSupportActionBar(); // jmg0156
        if (actionBar != null)
            actionBar.setTitle(titleResId);

        loadPage(htmlResId, assetsPath);
    }

    private void loadPage(int htmlResId, String assetsPath) {
        InputStream is = getResources().openRawResource(htmlResId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            mWebView.loadDataWithBaseURL("file:///android_asset/" + assetsPath,sb.toString(), "text/html", "UTF-8", "http://www.google.com");
        }
        catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            try {
                br.close();
                is.close();
            }
            catch (IOException ignore) {}
        }
    }

    @Override
    protected void onDestroy() {
        //jmg0026 - Make sure keyboard is closed:
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mWebView.getWindowToken(), 0);
        super.onDestroy();
    }
}
