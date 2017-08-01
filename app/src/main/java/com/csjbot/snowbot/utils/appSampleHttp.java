package com.csjbot.snowbot.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.activity.VidyoSampleActivity;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 */
public class appSampleHttp extends AsyncTask<appSampleHttp.Arguments, Integer, appSampleHttp.Arguments>
{
	private final String TAG = "appSampleHttp";
	private final int PROGRESS_STARTING = 100;
	private final int PROGRESS_READINGCERTIFICATES = 70;
	private final int PROGRESS_MERGINGCERTIFICATES = 40;
	private final int PROGRESS_INITNETWORK = 10;
	private final int PROGRESS_DONE = 0;

	@Override
	protected void onPreExecute () {
		// start the progress
		publishProgress(PROGRESS_STARTING);
	}

	@Override
	protected appSampleHttp.Arguments doInBackground(appSampleHttp.Arguments... params) {
		Csjlogger.info(TAG, "doInBackground Begin");
		String EntityID = "";

		String portal="http://";
		if(  VidyoSampleActivity.isHttps==true)
			portal="https://";

		appSampleHttp.Arguments args = params[0];
		String urlString = String.format("%1$s/services/v1_1/VidyoPortalUserService/", args.portalString);
		portal+=urlString;
		Csjlogger.debug(TAG, "Sending request to " + portal);

		String SOAPRequestXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">" +
				"<soapenv:Header/>" +
				"<soapenv:Body>" +
				"<v1:MyAccountRequest/>" +
				"</soapenv:Body>" +
				"</soapenv:Envelope>";
		Csjlogger.debug(TAG, "SOAP Request = " + SOAPRequestXML);

		String msgLength = String.format("%1$d", SOAPRequestXML.length());
		publishProgress(PROGRESS_INITNETWORK);

		try {
			HttpPost httppost = new HttpPost(portal);
			StringEntity se = new StringEntity(SOAPRequestXML, HTTP.UTF_8);

			se.setContentType("text/xml");
//			httppost.setHeader("Content-Type","application/soap+xml;charset=UTF-8");
			httppost.setHeader("Content-Type","text/xml;charset=UTF-8");

			httppost.setHeader("SOAPAction", "\"myAccount\"");
			//		httppost.setHeader("Accept-Encoding", "gzip,deflate");
			//		httppost.setHeader("Content-Length", msgLength);
			String auth = "Basic " + android.util.Base64.encodeToString((args.userString + ":" + args.passwordString).getBytes(), android.util.Base64.NO_WRAP);
			httppost.setHeader("Authorization", auth);


			httppost.setEntity(se);

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = (HttpResponse) httpclient.execute(httppost);

			Csjlogger.debug(TAG, httpResponse.getStatusLine().toString());
			Csjlogger.debug(TAG, "EntityID=" + httpResponse.getEntity().toString());


			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = httpResponse.getEntity().getContent();

			InputSource isrc = new InputSource();
			Document doc = db.parse(is);


			EntityID = getSoapValue(doc,"entityID");
			if (EntityID == null) {
				Log.e(TAG, "EntityID tag not found!");
				Toast mesg = Toast.makeText(args.context, "ENtityID tag not found!", Toast.LENGTH_LONG);
				mesg.show();
			}

			Csjlogger.debug(TAG, "EntityID = " + getSoapValue(doc, "entityID"));
			Csjlogger.debug(TAG, "OwnerID = " + getSoapValue(doc, "ownerID"));
			Csjlogger.debug(TAG, "DisplayName = " + getSoapValue(doc, "displayName"));
			Csjlogger.debug(TAG, "extension = " + getSoapValue(doc, "extension"));



		} catch (Exception e) {
			e.printStackTrace();
		}

		JoinConference (args, EntityID);


		Csjlogger.info(TAG, "doInBackground End");
		return(args);
	}


	public void  JoinConference (appSampleHttp.Arguments args, String EntityId)
	{

		String portal="http://";
		if(VidyoSampleActivity.isHttps==true)
			portal="https://";


		String urlString = String.format("%1$s/services/v1_1/VidyoPortalUserService/", args.portalString);
		portal+=urlString;
		String SOAPRequestXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">" +
				"<soapenv:Header/>" +
				"<soapenv:Body>" +
				"<v1:JoinConferenceRequest>" +

				"<v1:conferenceID>" + EntityId +
				"</v1:conferenceID>"+
				"</v1:JoinConferenceRequest>"+
				"</soapenv:Body>" +
				"</soapenv:Envelope>";
		Csjlogger.debug(TAG, "SOAP Request = " + SOAPRequestXML);

		String msgLength = String.format("%1$d", SOAPRequestXML.length());
		publishProgress(PROGRESS_INITNETWORK);

		try {
			HttpPost httppost = new HttpPost(portal);
			StringEntity se = new StringEntity(SOAPRequestXML, HTTP.UTF_8);

			se.setContentType("text/xml");

			httppost.setHeader("Content-Type","text/xml;charset=UTF-8");

			httppost.setHeader("SOAPAction", "\"JoinConference\"");

			String auth = "Basic " + android.util.Base64.encodeToString((args.userString + ":" + args.passwordString).getBytes(), android.util.Base64.NO_WRAP);
			httppost.setHeader("Authorization", auth);


			httppost.setEntity(se);

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = (HttpResponse) httpclient.execute(httppost);

			StatusLine status = httpResponse.getStatusLine();
			Csjlogger.debug(TAG, "Join status code = " + status.getStatusCode());

			Csjlogger.debug(TAG, httpResponse.getStatusLine().toString());
			Csjlogger.debug(TAG, "Staus=" + httpResponse.getEntity().toString());


			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = httpResponse.getEntity().getContent();

			InputSource isrc = new InputSource();
			Document doc = db.parse(is);

			//   Csjlogger.debug(TAG, "EntityID = "+getSoapValue(doc,"ns1:entityID"));
			//   Csjlogger.debug(TAG, "OwnerID = "+getSoapValue(doc,"ns1:ownerID"));
			//  Csjlogger.debug(TAG, "DisplayName = "+getSoapValue(doc,"ns1:displayName"));
			//  Csjlogger.debug(TAG, "extension = "+getSoapValue(doc,"ns1:extension"));



		} catch (Exception e) {
			e.printStackTrace();
		}


		Csjlogger.info(TAG, "JoinConference End");
	}


	public String getSoapValue(Document doc, String name) {
		NodeList nodes = doc.getElementsByTagNameNS("*", name);
		if (nodes.getLength() > 0) {
			Element element = (Element) nodes.item(0);

			NodeList entityIDs = element.getChildNodes();

			Node entityID = entityIDs.item(0);
			String nodevalue = entityID.getNodeValue();
			return nodevalue;
		}
		return null;
	}


	@Override
	protected void onCancelled() {
		// stop the progress
		publishProgress(PROGRESS_DONE);
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		switch (progress[0]) {
			case PROGRESS_DONE:
			case PROGRESS_READINGCERTIFICATES:
			case PROGRESS_MERGINGCERTIFICATES:
			case PROGRESS_INITNETWORK:
				break;
			case PROGRESS_STARTING:
				break;
		}
	}

	@Override
	protected void onPostExecute(appSampleHttp.Arguments result) {
		publishProgress(PROGRESS_DONE);

		if (result == null)
			return;

//		Conference.start(result.context);

		Csjlogger.info(TAG, "onPostExecute End");
	}

	public static class Arguments
	{
		String portalString;
		String userString;
		String passwordString;
		Context context;
		public Arguments(String portal, String user, String password, Context context) {
			portalString = portal;
			userString = user;
			passwordString = password;
			this.context = context;
		}
	}

}
