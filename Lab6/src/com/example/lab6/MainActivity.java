package com.example.lab6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

public class MainActivity extends Activity {

	EditText searchbox;
	WebView webview;
	EditText stockname, stockprice, updatetime;
   	Handler mHandler = new Handler();
   	Thread myThread;
   	boolean running = false;
   	private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
   	String name;
   	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
	   	webview = (WebView) findViewById(R.id.browser);
	   	searchbox =  (EditText) findViewById(R.id.addressbar);
	   	stockname =  (EditText) findViewById(R.id.stockName);
	   	stockprice =  (EditText) findViewById(R.id.stockPrice);
	   	updatetime = (EditText) findViewById(R.id.updateTime);
	   	
	   	
    }
    
    public void updateStock(){
    	
    	new RetrieveCode().execute("http://finance.yahoo.com/webservice/v1/symbols/" + name + "/quote?format=json");
    	
    }
    
    public void goButtonClick(View view) {
    	name = searchbox.getText().toString().replaceAll("\\s+","");
    	    	
    	updateStock();
        myThread = new Thread(new Runnable() {
	        @Override
	        public void run() {
	            // TODO Auto-generated method stub
	            String prevName = name;
	        	while (prevName == name) {
	                try {
	                		                	
	                    Thread.sleep(10000);
	                    mHandler.post(new Runnable() {

	                        @Override
	                        public void run() {
	                            updateStock();
	                            System.out.println("updated stock: ");

	                        }
	                    });
	                } catch (Exception e) {
	                  System.out.println("ERror occured here " + e);
	                }
	            }
	        }
	    });
        myThread.start();
        running = true;
    }
    
    private class RetrieveCode extends AsyncTask<String, Void, String> {
    	@Override
    	protected String doInBackground(String... urls) {
    		
    		String result = "";

			try {
				HttpGet httpGet = new HttpGet(urls[0]);
				HttpClient client = new DefaultHttpClient();
				
				HttpResponse response = client.execute(httpGet);
				
				int statusCode = response.getStatusLine().getStatusCode();
				
				if(statusCode == 200) {
					InputStream inputstream = response.getEntity().getContent();
					BufferedReader in = new BufferedReader(new InputStreamReader(inputstream));			
			
					String line;
					while ((line = in.readLine()) != null){
						result +=line;
					}
				}
				
	    	} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			return result;
    	}
    	
    	protected void onPostExecute(String result) {
    		   // execution of result of Long time consuming operation
    		try {
				updateWebView(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	private void updateWebView(String result) throws JSONException { 
    	   	
    	   	webview.setWebChromeClient(new WebChromeClient());
    		webview.loadData(result, "text/html", "UTF-8");
    		
    		JSONObject mainObject = new JSONObject(result);
    		JSONObject list = mainObject.getJSONObject("list");
    		JSONArray resources = list.getJSONArray("resources");
    		JSONObject resource1 = resources.getJSONObject(0);
    		JSONObject resource = resource1.getJSONObject("resource");
    		JSONObject fields = resource.getJSONObject("fields");
    		stockname.setText(fields.getString("name"));
    		stockprice.setText(fields.getString("price"));
    		updatetime.setText(sdf.format(new Date(System.currentTimeMillis())));
    	}	
    }   
}

/*
{
	"list" : 
	{ 
		"meta" : 
		{ 
		"type" : "resource-list",
		"start" : 0,
		"count" : 1
		},
		"resources" : 
		[ 
			{
				"resource" : 
				{ 
					"classname" : "Quote",
					"fields" : 
					{ 
						"name" : "Google Inc.",
						"price" : "572.500000",
						"symbol" : "GOOG",
						"ts" : "1412798400",
						"type" : "equity",
						"utctime" : "2014-10-08T20:00:00+0000",
						"volume" : "1984767"
					}
				}
			}
	
		]
	}
}
*/