package esri.mrm.mobile.activity;

import java.net.UnknownHostException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.esri.core.io.EsriSecurityException;
import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalUser;

import esri.mrm.mobile.AGSObjects;
import esri.mrm.mobile.R;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements OnSharedPreferenceChangeListener
{

  /**
   * Keep track of the login task to ensure we can cancel it if requested.
   */
  private UserLoginTask         mAuthTask         = null;

  // Values for email and password at the time of the login attempt.
  private String                mAccountName;
  private String                mPassword;

  // UI references.
  private EditText              mEmailView;
  private EditText              mPasswordView;
  private View                  mLoginFormView;
  private View                  mLoginStatusView;
  private TextView              mLoginStatusMessageView;
  private AGSObjects         agsObjects;
  private SharedPreferences  sharedPrefs;
  private TextView errorText;
  private String portalUrl;
  
  private static final String LOGIN_PREFERENCES = "login_preferences";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    PreferenceManager.setDefaultValues(this, R.xml.preference, false);
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    
    sharedPrefs.registerOnSharedPreferenceChangeListener(this);
    setContentView(R.layout.activity_login);

    portalUrl = sharedPrefs.getString("portal_url", null);
    agsObjects = ((AGSObjects) getApplicationContext());
    // Set up the login form.
    mEmailView = (EditText) findViewById(R.id.email);

    mPasswordView = (EditText) findViewById(R.id.password);
    mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
    {
      public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
      {
        if (id == R.id.login || id == EditorInfo.IME_NULL)
        {
          attemptLogin();
          return true;
        }
        return false;
      }
    });

    mLoginFormView = findViewById(R.id.login_form);
    mLoginStatusView = findViewById(R.id.login_status);
    mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
    
    errorText = (TextView) findViewById(R.id.login_error);

    findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        attemptLogin();
      }
    });
  }
  
  /**
   * Attempts to sign in or register the account specified by the login form. If
   * there are form errors (invalid email, missing fields, etc.), the errors are
   * presented and no actual login attempt is made.
   */
  public void attemptLogin()
  {
    if (mAuthTask != null)
    {
      return;
    }

    errorText.setText("");
    // Reset errors.
    mEmailView.setError(null);
    mPasswordView.setError(null);

    // Store values at the time of the login attempt.
    mAccountName = mEmailView.getText().toString();
    mPassword = mPasswordView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password.
    if (TextUtils.isEmpty(mPassword))
    {
      mPasswordView.setError(getString(R.string.error_field_required));
      focusView = mPasswordView;
      cancel = true;
    }
    // Check for a valid email address.
    if (TextUtils.isEmpty(mAccountName))
    {
      mEmailView.setError(getString(R.string.error_field_required));
      focusView = mEmailView;
      cancel = true;
    }

    if (cancel)
    {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView.requestFocus();
    }
    else
    {
      // Show a progress spinner, and kick off a background task to
      // perform the user login attempt.
      mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
      showProgress(true);
      mAuthTask = new UserLoginTask(this);
      mAuthTask.execute((Void) null);
    }
  }

  /**
   * Shows the progress UI and hides the login form.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
  private void showProgress(final boolean show)
  {
    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
    // for very easy animations. If available, use these APIs to fade-in
    // the progress spinner.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
    {
      int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

      mLoginStatusView.setVisibility(View.VISIBLE);
      mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
      {
        @Override
        public void onAnimationEnd(Animator animation)
        {
          mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
      });

      mLoginFormView.setVisibility(View.VISIBLE);
      mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
      {
        @Override
        public void onAnimationEnd(Animator animation)
        {
          mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
      });
    }
    else
    {
      // The ViewPropertyAnimator APIs are not available, so simply show
      // and hide the relevant UI components.
      mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
      mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate the
   * user.
   */
  public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
  {
    private Context mContext;
    private String errorMessage="";
    
    public UserLoginTask (Context context)
    {
      mContext = context;
    }
    @Override
    protected Boolean doInBackground(Void... params)
    {
      try
      {
        UserCredentials credentials = new UserCredentials();
        credentials.setUserAccount(mAccountName, mPassword);

        Portal portal = new Portal(portalUrl, credentials);
        PortalUser pu = portal.fetchUser();
        if(pu==null)
          return false;
        else
          agsObjects.setPortal(portal);
      }
      catch (InterruptedException e)
      {
        return false;
      }
      catch (Exception e)
      {
        if(e instanceof UnknownHostException)
          errorMessage = "Unable to access portal.";
        else if(e instanceof EsriSecurityException)
          errorMessage = "Invalid username or password.";
        return false;
      }
      return true;
    }

    @Override
    protected void onPostExecute(final Boolean success)
    {
      mAuthTask = null;
      showProgress(false);

      if (success)
      {
        agsObjects.setUsername(mAccountName);
        Intent myIntent = new Intent(mContext, EsriMrmActivity.class);
        finish();
        startActivity(myIntent);
      }
      else
      {
        errorText.setTextColor(Color.RED);
        errorText.setText("Log in failed.  " + errorMessage);
      }
    }

    @Override
    protected void onCancelled()
    {
      mAuthTask = null;
      showProgress(false);
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.mrm_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle item selection
    switch (item.getItemId())
    {
      case R.id.settings:
        Intent intent = new Intent(this, FieldWorkerAppPreference.class);
        intent.putExtra("settingsId", R.xml.preference);
        startActivity(intent);
        return true;
      default:
        return true;
    }
  }

  public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
  {
    portalUrl = sharedPrefs.getString("portal_url", null);
  }
}
