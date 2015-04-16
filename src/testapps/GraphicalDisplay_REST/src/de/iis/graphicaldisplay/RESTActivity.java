package de.iis.graphicaldisplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


// WARNING	!!!: Do not use this in production code you are ever going to use on a network you do not entirely trust,
// especially anything going over the public internet.
// at present all server certificates are accepted !!!


public class RESTActivity extends Activity {
	
	private String toggle1Status = "";
	private String toggle2Status = "";
	private String toggle3Status = "";
	private Handler handler;
	private boolean isrunning = true;
	private boolean doRestIsRunning = true;
	private volatile Object toggleEventLock; 
	private volatile boolean flag1On = false;
	private volatile boolean flag1Off = false;
	private boolean flag2On = false;
	private boolean flag2Off = false;
	private boolean flag3On = false;
	private boolean flag3Off = false;
	private String url;
	private String ipAdr;
	private DefaultHttpClient client;
	private Thread threadREST;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_rest);
		
		Button button = (Button) findViewById(R.id.button1);
		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton toggle2 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton toggle3 = (ToggleButton) findViewById(R.id.toggleButton3);
	
		toggleEventLock = new Object();
        button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) 
             {
            	client = getNewHttpClient();
            	getIPAddressFromTextField();
            	handler = new Handler(Looper.getMainLooper());
         		handler.postDelayed(runnableCode, 2000);
//         		DoRESTRequest thread = new DoRESTRequest();
//         		Thread threadREST = new Thread(thread);
//         		threadREST.start();
         	
             }
         });
     
        // 1: Lava-Lampe 
		toggle.setOnClickListener(new OnClickListener(){
	        @Override
	        public void onClick(View v) {
	        	
	        	System.out.println("begin toggle + flag1on:" + flag1On + flag1Off);
	            boolean checked = ((ToggleButton)v).isChecked();
	            ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
	            Context context = getApplicationContext();
	            Toast toast;
				
	            if(checked)
	            {
//		    		new DoRESTRequestOn().execute("HM_ES_PMSw1_Pl_PowerMeter_274155/onOffSwitch/stateControl");
//		    		toggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
//		    		toggle1Status = "on";
	            	flag1On = true;
	            	new Thread(new DoRESTRequest()).run();
		        	toast = Toast.makeText(context, "Sie schalten die Lava-Lampe ein.", 2000);
		    		toast.show();
		    	
	            } else
	            {
//		    		new DoRESTRequestOff().execute("HM_ES_PMSw1_Pl_PowerMeter_274155/onOffSwitch/stateControl");
//		    		toggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
//		    		toggle1Status = "off";
	            	flag1Off = true;
	            	new Thread(new DoRESTRequest()).run();
		    		toast = Toast.makeText(context, "Sie schalten die Lava-Lampe aus.", 2000);
		    		toast.show();
	            }
	        }
	    });
		
		// 2: LED-Streifen
		toggle2.setOnClickListener(new OnClickListener(){
	        @Override
	        public void onClick(View v) {
	            boolean checked = ((ToggleButton)v).isChecked();
	            ToggleButton toggle2 = (ToggleButton) findViewById(R.id.toggleButton2);
	            Context context = getApplicationContext();
	            Toast toast;
				
	            if(checked)
	            {
//		    		new DoRESTRequestOn().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//		    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
//		    		toggle2Status = "on";
	            	flag2On = true;
	            	new Thread(new DoRESTRequest()).run();
		        	toast = Toast.makeText(context, "Sie schalten den LED-Streifen ein.", 2000);
		    		toast.show();
	            } else
	            {
//		    		new DoRESTRequestOff().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//		    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
//		    		toggle2Status = "off";
	            	flag2Off = true;
	            	new Thread(new DoRESTRequest()).run();
		    		toast = Toast.makeText(context, "Sie schalten den LED-Streifen aus.", 2000);
		    		toast.show();
	            }
	        }
	    });
		
		// resource 3
				toggle3.setOnClickListener(new OnClickListener(){
			        @Override
			        public void onClick(View v) {
			            boolean checked = ((ToggleButton)v).isChecked();
			            ToggleButton toggle3 = (ToggleButton) findViewById(R.id.toggleButton3);
			            Context context = getApplicationContext();
			            Toast toast;
						
			            if(checked)
			            {
//				    		new DoRESTRequestOn().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//				    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
//				    		toggle2Status = "on";
			            	flag3On = true;
			            	new Thread(new DoRESTRequest()).run();
				        	toast = Toast.makeText(context, "Sie schalten den LED-Streifen ein.", 2000);
				    		toast.show();
			            } else
			            {
//				    		new DoRESTRequestOff().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//				    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
//				    		toggle2Status = "off";
			            	flag3Off = true;
			            	new Thread(new DoRESTRequest()).run();
				    		toast = Toast.makeText(context, "Sie schalten den LED-Streifen aus.", 2000);
				    		toast.show();
			            }
			        }
			    });
	}

		
	
	@Override
	protected void onStop()
	{
	    super.onStop();  
	    handler.removeCallbacks(runnableCode);
	    doRestIsRunning = false;
	    isrunning = false;
	    finish();
	}
	
	
	private Runnable runnableCode = new Runnable() {
	    @Override
	    public void run()
	    {
		    	new DoRESTRequestUpdateToggles().execute(getPathFromTextField(1), "1");
		    	new DoRESTRequestUpdateToggles().execute(getPathFromTextField(2), "2");
		    	new DoRESTRequestUpdateToggles().execute(getPathFromTextField(3), "3");
		    	 
		    	handler.postDelayed(this, 1000);
		    	
		    	setToggles();
	    }
	};
	
	
	public void getIPAddressFromTextField() {
		
		EditText edit = (EditText) findViewById(R.id.editText1);
		ipAdr = edit.getText().toString();
	}
	
	
	public String getPathFromTextField(int number) {
		
		EditText edit = null;
		
		if(number == 1) edit = (EditText) findViewById(R.id.editText2);
		if(number == 2) edit = (EditText) findViewById(R.id.editText3);
		if(number == 3) edit = (EditText) findViewById(R.id.editText4);
		
		return edit.getText().toString();
	}
	
	
	private void setToggles() {
		
		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton toggle2 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton toggle3 = (ToggleButton) findViewById(R.id.toggleButton3);
      	
		 // 1: Lava-Lampe 
      	if(toggle1Status == "on")
   		{
   			toggle.setChecked(true);
   			toggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
   		}
   		else if(toggle1Status == "off")
   		{
   			toggle.setChecked(false);
   			toggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
   		} 
      	
      	// 2: LED-Streifen
      	if(toggle2Status == "on")
   		{
   			toggle2.setChecked(true);
   			toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
   		}
   		else if(toggle2Status == "off")
   		{
   			toggle2.setChecked(false);
   			toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
   		} 
      	
      	// 3: Ventilator
      	if(toggle3Status == "on")
   		{
   			toggle3.setChecked(true);
   			toggle3.setBackgroundDrawable(getResources().getDrawable(R.drawable.ventilator_on));
   		}
   		else if(toggle3Status == "off")
   		{
   			toggle3.setChecked(false);
   			toggle3.setBackgroundDrawable(getResources().getDrawable(R.drawable.ventilator_off));
   		} 
	}
	
	// ToggleButtons mit Geräten sync
	 private class DoRESTRequestUpdateToggles extends AsyncTask<String, Void, String> {
		 
	        protected String doInBackground(String... urls) {
	              
	        	DefaultHttpClient client = getNewHttpClient();
	    		HttpGet httpGet = new HttpGet("https://" +  ipAdr + ":8443/rest/resources/" + urls[0] + "?user=mobile&pw=mobile&depth=100");
	    		
	    		String result = "";
	    		String returnResult = "";
	    		
	    		try {
		    			HttpResponse response = client.execute(httpGet);
		    			if(response != null)
		    			{
		    			HttpEntity entity = response.getEntity();
		    			InputStream in = entity.getContent();
		    			BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    			String line;
		    			while((line = br.readLine()) != null) result = result + line;
		    			in.close();
		    	
						JSONObject json = new JSONObject(result);
						
						if(json.getBoolean("value") == true) 
						{
							returnResult = urls[1] + "on";
						} 
						else 
						{
							returnResult = urls[1] + "off";
						}
		    			}
					
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				}
	    		
	    		return returnResult;
	        }
	        
	        @Override
	        protected void onPostExecute(String result) {
	        	
	        	if(result.equals("1on")) toggle1Status = "on";
	        	if(result.equals("1off")) toggle1Status = "off";
	        	if(result.equals("2on")) toggle2Status = "on";
	        	if(result.equals("2off")) toggle2Status = "off";
	        	if(result.equals("3on")) toggle3Status = "on";
	        	if(result.equals("3off")) toggle3Status = "off";
	       }
	    }
	
	
	 private class DoRESTRequest implements Runnable {
		
	       public void run() {
	    	   
	    	boolean requestForOn = true;
	              
	    		   if(flag1On == true)
		    		{
		    			url = getPathFromTextField(1);
		    			requestForOn = true;
		    			flag1On = false;
		    		}else
		    		
		    		if(flag1Off == true)
		    		{
		    			url = getPathFromTextField(1);
		    			requestForOn = false;
		    			flag1Off = false;
		    		}   
	    		   
	    		   if(flag2On == true)
		    		{
		    			url = getPathFromTextField(2);
		    			requestForOn = true;
		    			flag2On = false;
		    		}else
		    		
		    		if(flag2Off == true)
		    		{
		    			url = getPathFromTextField(2);
		    			requestForOn = false;
		    			flag2Off = false;
		    		}   
	    		   
	    		   if(flag3On == true)
		    		{
		    			url = getPathFromTextField(3);
		    			requestForOn = true;
		    			flag3On = false;
		    		}else
		    		
		    		if(flag3Off == true)
		    		{
		    			url = getPathFromTextField(3);
		    			requestForOn = false;
		    			flag3Off = false;
		    		}   
		    		else { 
								
//		    			Thread.yield();
//		    			continue;
		    		}
	  
	    		HttpGet httpGet = new HttpGet("https://" +  ipAdr + ":8443/rest/resources/" + url + "?user=mobile&pw=mobile&depth=100");
	    		
	    		String result = "";
	    		String result2 = "";
	    		
	    		try {
	    
	    			HttpResponse response = client.execute(httpGet);
	    			HttpEntity entity = response.getEntity();
	    			InputStream in = entity.getContent();
	    			BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    			String line;
	    			while((line = br.readLine()) != null) result = result + line;
	    			in.close();
	    	
					JSONObject json = new JSONObject(result);
					
					if(json.has("resourcelink"))
					{
						JSONObject jsonTemp = json.getJSONObject("resourcelink");
					    
						HttpGet httpGet2 = new HttpGet("https://" +  ipAdr + ":8443/rest/resources/" + jsonTemp.get("link") + "?user=mobile&pw=mobile&depth=100");
			
						System.out.println("exe before"+ System.currentTimeMillis());	
						HttpResponse response2 = client.execute(httpGet2);
						System.out.println("exe after"+ System.currentTimeMillis());	
		    			HttpEntity entity2 = response2.getEntity();
		    			InputStream in2 = entity2.getContent();
		    			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		    			String line2;
		    			while((line2 = br2.readLine()) != null) result2 = result2 + line2;
		    			in.close();
		    			json = new JSONObject(result2);
		    			
		    			json.remove("value");
						
		    			if(requestForOn) json.put("value", true);
		    			else json.put("value", false);
						
						HttpPut httpPut = new HttpPut("https://" + ipAdr + ":8443/rest/resources/" + jsonTemp.getString("link") + "?user=mobile&pw=mobile&depth=100");
					
						httpPut.addHeader("Content-Type", "application/json");
						httpPut.addHeader("Accept", "application/json");
						httpPut.setEntity(new StringEntity(json.toString()));
						
						client.execute(httpPut);
					}
					else
					{
						json.remove("value");
						
						if(requestForOn) json.put("value", true);
		    			else json.put("value", false);
						
						HttpPut httpPut = new HttpPut("https://" + ipAdr + ":8443/rest/resources/" + url + "?user=mobile&pw=mobile&depth=100");
					
						httpPut.addHeader("Content-Type", "application/json");
						httpPut.addHeader("Accept", "application/json");
						httpPut.setEntity(new StringEntity(json.toString()));
						
						client.execute(httpPut);
					
					}
					
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				} 	   
	       } 
	    }
	 
	
	public DefaultHttpClient getNewHttpClient() {
		
	     DefaultHttpClient client=null; 
	     
	     try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 8080));
	        registry.register(new Scheme("https", sf, 8443));

	        ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(params, registry);
	        
	       client = new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return client;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}