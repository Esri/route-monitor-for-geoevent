package esri.mrm.mobile.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import esri.mrm.mobile.R;
import esri.mrm.mobile.WorkOrder;
import esri.mrm.mobile.activity.StopTypeActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class SaveStopTask extends AsyncTask<String, Void, Boolean>
{
  private WorkOrder      workOrder;
  private Activity       activity;
  private Context        context;
  private ProgressDialog progressDialog;
  private CompleteListener listener;
  String                 msg = "";

  public SaveStopTask(WorkOrder workOrder, Activity activity, Context context, CompleteListener listener)
  {
    this.workOrder = workOrder;
    this.activity = activity;
    this.context = context;
    this.listener = listener;
  }

  @Override
  protected Boolean doInBackground(String... uri)
  {
    msg = "";
    activity.runOnUiThread(new Runnable()
    {
      public void run()
      {
        showProgressDialog();
      }
    });

    
    try
    {
      String jsonString = workOrder.getJsonString(workOrder.getRouteName());
      System.out.println(jsonString);
      StringEntity input;
      input = new StringEntity(jsonString);
      input.setContentType("application/json");
      HttpPost postRequest = new HttpPost(uri[0]);
      postRequest.setEntity(input);
      HttpClient httpclient = new DefaultHttpClient();
      httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
      httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
      HttpResponse response = httpclient.execute(postRequest);
      StatusLine statusLine = response.getStatusLine();
      if (statusLine.getStatusCode() == HttpStatus.SC_OK)
      {
        return true;
      }
      else
      {
        msg = "HTTP Status code is " + statusLine.getStatusCode();
        return false;
      }
    }
    catch (UnsupportedEncodingException e)
    {
      msg = e.getMessage();
      return false;
    }
    catch (ClientProtocolException e)
    {
      msg = e.getMessage();
      return false;
    }
    catch (IOException e)
    {
      msg = "Unable to access server.  This may be caused by the lack of network coverage.  Please try again when you have coverage.";
      return false;
    }
    catch (Exception e)
    {
      msg = e.getMessage();
      return false;
    }
  }

  @Override
  protected void onPostExecute(Boolean result)
  {
    progressDialog.dismiss();
    if (result)
    {
      showUpdateResult(true, msg);
    }
    else
    {
      showUpdateResult(false, msg);
    }
  }

  private void showProgressDialog()
  {
    progressDialog = new ProgressDialog(context);
    progressDialog.setTitle("Processing...");
    progressDialog.setMessage("Please wait.");
    progressDialog.setCancelable(false);
    progressDialog.setIndeterminate(true);
    progressDialog.show();
  }

  private void showUpdateResult(final boolean successful, final String msg)
  {
    activity.runOnUiThread(new Runnable()
    {

      public void run()
      {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set title
        alertDialogBuilder.setTitle(R.string.updateResult);

        String newMsg = msg;
        if (newMsg.length() > 0)
          newMsg = " - " + msg;

        // set dialog message
        alertDialogBuilder.setMessage((successful ? activity.getResources().getString(R.string.updateSucceeded) : activity.getResources().getString(R.string.updateFailed)) + " " + newMsg).setCancelable(false).setPositiveButton(R.string.okButtonLabel, new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int id)
          {
            dialog.cancel();
            if(listener != null)
              listener.resultCallback(successful);
          }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
      }
    });
  }
}
