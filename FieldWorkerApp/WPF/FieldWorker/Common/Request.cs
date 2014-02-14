using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Net;
using System.Windows;
using Newtonsoft.Json.Linq;

namespace FieldWorker.Common
{
  public class HttpRequest
  {
    private readonly BackgroundWorker _worker = new BackgroundWorker();

    //TODO - private RequestMessageBox _mb = null;
    private string            _uri;
    private string            _requestMethod;
    private string            _contentType;
    private int               _timeoutMS;
    private string            _body = null;
    private NetworkCredential _credentials = null;


    public HttpResponse Response;

    public void SetCredentials(string username, string password)
    {
      _credentials = new NetworkCredential(username, password);
    }

    public HttpRequest(String uri, String requestMethod, String contentType, int timeoutMS, String body = null)
    {
      _uri           = uri;
      _requestMethod = requestMethod;
      _contentType   = contentType;
      _timeoutMS     = timeoutMS;
      _body          = body;
    }

    private void Init()
    {
      //if (_mb == null)
      //  _mb = new RequestMessageBox();
    }

    /*
        public void Execute(string title, string message, Window owner)
        {
          Init();

          _mb.Title   = title;
          _mb.Message = message;
          _mb.Owner   = owner;

          _mb.ContentRendered += ExecuteRequest;
          _mb.ShowDialog();
          _mb.ContentRendered -= ExecuteRequest;
        }
    */

    private void ExecuteRequest(object sender, EventArgs e)
    {
      Init();

      _worker.DoWork             += worker_DoWork;
      _worker.RunWorkerCompleted += worker_RunWorkerCompleted;
      _worker.RunWorkerAsync();
    }

    void worker_DoWork(object sender, DoWorkEventArgs e)
    {
      Response = ExecuteHttpRequestBackground();
    }

    void worker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
    {
      _worker.DoWork             -= worker_DoWork;
      _worker.RunWorkerCompleted -= worker_RunWorkerCompleted;
      //_mb.Close();
    }

    public HttpResponse ExecuteHttpRequest()
    {
      HttpResponse response = new HttpResponse("", HttpStatusCode.Unused);
      using (new WaitCursor())
      {
        response = ExecuteHttpRequestBackground();
      }
      return response;
    }


    private HttpResponse ExecuteHttpRequestBackground()
    {
      // see -
      //  http://stackoverflow.com/questions/4982765/json-call-with-c-sharp
      //  http://msdn.microsoft.com/query/dev11.query?appId=Dev11IDEF1&l=EN-US&k=k%28System.Net.HttpWebRequest.ContentType%29;k%28TargetFrameworkMoniker-.NETFramework,Version%3Dv4.5%29;k%28DevLang-csharp%29&rd=true

      Log.Trace("ExecuteHttpRequest - uri: " + _uri + " , RequestMethod: " + _requestMethod + " , ContentType: " + _contentType + " , TimeOutMS: " + _timeoutMS.ToString() + " , Body: " + _body);

      HttpResponse response = new HttpResponse("", HttpStatusCode.Unused);

      try
      {
        var webRequest = (HttpWebRequest)WebRequest.Create(_uri);
        webRequest.Method       = _requestMethod;
        webRequest.ContentType  = _contentType; // "text/xml" or "application/json" or "application/x-www-form-urlencoded"
        webRequest.Timeout      = _timeoutMS;
        if (_credentials != null)
          webRequest.Credentials  = _credentials;

        // POST or PUT with a body
        if ((_requestMethod.ToUpper() == "POST" || _requestMethod.ToUpper() == "PUT") &&
              !String.IsNullOrEmpty(_body))
        {
          //ASCIIEncoding encoding = new ASCIIEncoding();
          //Byte[] buffer = Encoding.UTF8.GetBytes(body);
          //webRequest.ContentLength = buffer.Length;
          //webRequest.UseDefaultCredentials = true;
          //Stream requestStream = webRequest.GetRequestStream();
          //requestStream.Write(buffer, 0, buffer.Length);

          using (var requestWriter = new StreamWriter(webRequest.GetRequestStream()))
          {
            requestWriter.Write(_body);
          }
        }

        var webResponse = (HttpWebResponse)webRequest.GetResponse();
        using (var streamReader = new StreamReader(webResponse.GetResponseStream()))
        {
          response.Text = streamReader.ReadToEnd();
          response.StatusCode = webResponse.StatusCode;
        }

        // close the Stream object
        webResponse.Close();
      }
      catch (Exception ex)
      {
        Log.TraceException("ExecuteHttpRequest", ex);
        response.Exception = ex.Message;
        return response;
      }

      return response;
    }
  }

  public struct HttpResponse
  {
    public String Text;
    public HttpStatusCode StatusCode;
    public String Exception;

    public HttpResponse(String text, HttpStatusCode statusCode)
    {
      this.Text = text;
      this.StatusCode = statusCode;
      this.Exception = "";
    }

    public bool ReportOnArcGISServerError(String caption)
    {
      if (StatusCode == HttpStatusCode.OK)
      {
        Log.Trace(Text);
        return false;
      }

      bool bError = false;
      if (!String.IsNullOrEmpty(Exception))
      {
        System.Windows.MessageBox.Show(Exception, caption);
        bError = true;
      }
      if (!String.IsNullOrEmpty(Text))
      {
        System.Windows.MessageBox.Show(Text, caption);
        bError = true;
      }

      if (!bError)
        Log.Trace(Text);

      return bError;
    }

    public bool ReportOnGeoEventProcessorError(String caption)
    {
      if (StatusCode == HttpStatusCode.OK)
      {
        if (String.IsNullOrEmpty(Text))
        {
          // received an empty respond
          return false;
        }

        try
        {
          JObject jobjText = JObject.Parse(Text);
          IDictionary<string, JToken> textDictionary = jobjText as IDictionary<string, JToken>;

          // extract the inner respond, if it ended up nested
          if (textDictionary.ContainsKey("reply"))
          {
            jobjText = textDictionary["reply"] as JObject;
            textDictionary = jobjText as IDictionary<string, JToken>;
          }

          if (textDictionary.ContainsKey("status"))
          {
            if (textDictionary["status"].ToString() == "error")
            {
              if (textDictionary.ContainsKey("messages"))
              {
                IList<JToken> messagesList = textDictionary["messages"] as IList<JToken>;
                var strBuilder = new System.Text.StringBuilder();
                foreach (var message in messagesList)
                {
                  strBuilder.Append(message.ToString());
                  strBuilder.Append(",");
                }
                string messages = strBuilder.ToString(0, strBuilder.Length - 1);
                System.Windows.MessageBox.Show(messages, caption);
              }
              return true;
            }
          }
        }
        catch (Exception)
        {
          // can't find a JSON error reported
          Log.Trace(Text);
          return false;
        }

        Log.Trace(Text);
        return false;
      }

      bool bError = false;
      if (!String.IsNullOrEmpty(Exception))
      {
        System.Windows.MessageBox.Show(Exception, caption);
        bError = true;
      }
      if (!String.IsNullOrEmpty(Text))
      {
        System.Windows.MessageBox.Show(Text, caption);
        bError = true;
      }

      if (!bError)
        Log.Trace(Text);

      return bError;
    }
  }


}
