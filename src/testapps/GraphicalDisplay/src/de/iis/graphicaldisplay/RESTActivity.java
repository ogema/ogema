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
import org.json.JSONArray;
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
	private String toggle4Status = "";
	private Handler handler;
	private boolean isrunning = true;
	private boolean doRequestIsRunning = true;
	private volatile Object toggleEventLock; 
	private volatile boolean flag1On = false;
	private volatile boolean flag1Off = false;
	private boolean flag2On = false;
	private boolean flag2Off = false;
	private boolean flag3On = false;
	private boolean flag3Off = false;
	private boolean flag4On = false;
	private boolean flag4Off = false;
	private String url;
	private String ipAdr; 
	private DefaultHttpClient client;
	private Thread threadREST;
	private int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_rest);
		
		Button button = (Button) findViewById(R.id.button1);
		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton toggle2 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton toggle3 = (ToggleButton) findViewById(R.id.toggleButton3);
		ToggleButton toggle4 = (ToggleButton) findViewById(R.id.toggleButton4);
	
	
        button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) 
             {
            	ipAdr = getIPAddressFromTextField();
            	client = getNewHttpClient();
            	handler = new Handler(Looper.getMainLooper());
         		handler.postDelayed(runnableCode, 3000);
         	
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
//	            	new Thread(new DoRESTRequest()).run();
		        	toast = Toast.makeText(context, "Sie schalten die Lava-Lampe ein.", 2000);
		    		toast.show();
		    	
	            } else
	            {
//		    		new DoRESTRequestOff().execute("HM_ES_PMSw1_Pl_PowerMeter_274155/onOffSwitch/stateControl");
//		    		toggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
//		    		toggle1Status = "off";
	            	flag1Off = true;
//	            	new Thread(new DoRESTRequest()).run();
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
		        	toast = Toast.makeText(context, "Sie schalten den LED-Streifen ein.", 2000);
		    		toast.show();
	            } else
	            {
//		    		new DoRESTRequestOff().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//		    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
//		    		toggle2Status = "off";
	            	flag2Off = true;
		    		toast = Toast.makeText(context, "Sie schalten den LED-Streifen aus.", 2000);
		    		toast.show();
	            }
	        }
	    });
		
		// 3: Ventilator
		toggle3.setOnClickListener(new OnClickListener(){
	        @Override
	        public void onClick(View v) {
	            boolean checked = ((ToggleButton)v).isChecked();
	            ToggleButton toggle3 = (ToggleButton) findViewById(R.id.toggleButton3);
	            Context context = getApplicationContext();
	            Toast toast;
				
	            if(checked)
	            {
//		    		new DoRESTRequestOn().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//		    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
//		    		toggle2Status = "on";
	            	flag3On = true;
		        	toast = Toast.makeText(context, "Sie schalten den Ventilator ein.", 2000);
		    		toast.show();
	            } else
	            {
//		    		new DoRESTRequestOff().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//		    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
//		    		toggle2Status = "off";
	            	flag3Off = true;
		    		toast = Toast.makeText(context, "Sie schalten den Ventilator aus.", 2000);
		    		toast.show();
	            }
	        }
	    });
		
		// 3: Z wave lamp
				toggle4.setOnClickListener(new OnClickListener(){
			        @Override
			        public void onClick(View v) {
			            boolean checked = ((ToggleButton)v).isChecked();
			            ToggleButton toggle4 = (ToggleButton) findViewById(R.id.toggleButton4);
			            Context context = getApplicationContext();
			            Toast toast;
						
			            if(checked)
			            {
//				    		new DoRESTRequestOn().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//				    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
//				    		toggle2Status = "on";
			            	flag4On = true;
				        	toast = Toast.makeText(context, "Sie schalten die Z-Wave-Lampe ein.", 2000);
				    		toast.show();
			            } else
			            {
//				    		new DoRESTRequestOff().execute("Light_00212effff003dc6_0b/onOffSwitch/stateControl");
//				    		toggle2.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
//				    		toggle2Status = "off";
			            	flag4Off = true;
				    		toast = Toast.makeText(context, "Sie schalten die Z-Wave-Lampe aus.", 2000);
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
	    doRequestIsRunning = false;
	    isrunning = false;
	    finish();
	}
	
	
	private Runnable runnableCode = new Runnable() {
	    @Override
	    public void run()
	    {
	    		
	    		new DoRESTRequest().execute();
	    		
//	    		if(counter++ == 50)
//	    		{
//			    	new DoRESTRequestUpdateToggles().execute("HM_ES_PMSw1_Pl_PowerMeter_274155.onOffSwitch.stateFeedback", "1");
//			    	setToggles();
//	    		}
//	    		
//	    		if(counter == 100)
//	    		{
//			    	new DoRESTRequestUpdateToggles().execute("Light_00212effff003dc6_0b.onOffSwitch.stateFeedback", "2");
//			    	setToggles();
//	    		}
//	    		
//	    		if(counter >= 150)
//	    		{
//			    	new DoRESTRequestUpdateToggles().execute("Develco_Smart_Plug.onOffSwitch.stateFeedback", "3");
//			    	setToggles();
//			    	counter = 0;
//	    		}
		    	 
		    	handler.postDelayed(this, 1111);	
	}
	};
	
	
public String getIPAddressFromTextField() {
		
		EditText edit = (EditText) findViewById(R.id.editText1);
		return edit.getText().toString();
	}
	
	
	private void setToggles() {
		
		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
		ToggleButton toggle2 = (ToggleButton) findViewById(R.id.toggleButton2);
		ToggleButton toggle3 = (ToggleButton) findViewById(R.id.toggleButton3);
		ToggleButton toggle4 = (ToggleButton) findViewById(R.id.toggleButton4);
      	
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
      	
     // 3: Z-Wave lamp
      	if(toggle4Status == "on")
   		{
   			toggle4.setChecked(true);
   			toggle4.setBackgroundDrawable(getResources().getDrawable(R.drawable.lighton));
   		}
   		else if(toggle4Status == "off")
   		{
   			toggle4.setChecked(false);
   			toggle4.setBackgroundDrawable(getResources().getDrawable(R.drawable.lightoff));
   		} 
	}
	
	// ToggleButtons mit Geräten sync
//	 private class DoRESTRequestUpdateToggles extends AsyncTask<String, Void, String> {
//		 
//	        protected String doInBackground(String... urls) {
//	              
//	        	DefaultHttpClient client = getNewHttpClient();
//	    		HttpGet httpGet = new HttpGet("https://" +  ipAdr + ":8443/servletAndroid/read?resource=" + urls[0]);
//	    		
//	    		String result = "";
//	    		String returnResult = "";
//	    		
//	    		try {
//		    			HttpResponse response = client.execute(httpGet);
//		    			if(response != null)
//		    			{
//		    			HttpEntity entity = response.getEntity();
//		    			InputStream in = entity.getContent();
//		    			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		    			String line;
//		    			while((line = br.readLine()) != null) result = result + line;
//		    			in.close();
//		    	
//						JSONObject json = new JSONObject(result);
//						
//						if(json.getBoolean("value") == true) 
//						{
//							returnResult = urls[1] + "on";
//						} 
//						else 
//						{
//							returnResult = urls[1] + "off";
//						}
//		    			}
//					
//				} catch (JSONException | IOException e) {
//					e.printStackTrace();
//				}
//	    		
//	    		return returnResult;
//	        }
//	        
//	        @Override
//	        protected void onPostExecute(String result) {
//	        	
//	        	if(result.equals("1on")) toggle1Status = "on";
//	        	if(result.equals("1off")) toggle1Status = "off";
//	        	if(result.equals("2on")) toggle2Status = "on";
//	        	if(result.equals("2off")) toggle2Status = "off";
//	        	if(result.equals("3on")) toggle3Status = "on";
//	        	if(result.equals("3off")) toggle3Status = "off";
//	        	
//	        	
//	       }
//	    }
	
	 
	 
	
	 private class DoRESTRequest extends AsyncTask<String, Void, String> {
		
		 protected String doInBackground(String... urls) {
             
	    
			   HttpResponse response = null;
			 
	    	   try {
	    		   
	    	   
	    		   if(flag1On == true)
		    		{
	    			   flag1On = false;
	    		
	    			   response = client.execute(new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
			                      "HM_ES_PMSw1_Pl_PowerMeter_274155.onOffSwitch.stateControl" + "&body=" + "true"));
		    			
		    		}else
		    		
		    		if(flag1Off == true)
		    		{
		    			flag1Off = false;
		    			HttpGet httpGet = new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
		    			"HM_ES_PMSw1_Pl_PowerMeter_274155.onOffSwitch.stateControl" + "&body=" + "false");
		    			response = client.execute(httpGet);
		    			
		    		}  
	    		   
	    		   if(flag2On == true)
		    		{
		    		
	    			   flag2On = false;
	    			   response = client.execute(new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
			                      "Light_00212effff003dc6_0b.onOffSwitch.stateControl" + "&body=" + "true"));
		    			
		    		}else
		    		
		    		if(flag2Off == true)
		    		{
		    			flag2Off = false;
		    			HttpGet httpGet = new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
		    				 "Light_00212effff003dc6_0b.onOffSwitch.stateControl" + "&body=" + "false");
		    			response = client.execute(httpGet);
		    			
		    		}  
	    		   
	    		   if(flag3On == true)
		    		{
	    			   flag3On = false;
	    			   response = client.execute(new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
			                      "Develco_Smart_Plug.onOffSwitch.stateControl" + "&body=" + "true"));
		    			
		    		}else
		    		
		    		if(flag3Off == true)
		    		{
		    			flag3Off = false;
		    			HttpGet httpGet = new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
		    					"Develco_Smart_Plug.onOffSwitch.stateControl" + "&body=" + "false");
		    			response = client.execute(httpGet);
		    			
		    		}   
	    		   
	    		   if(flag4On == true)
		    		{
	    			   flag4On = false;
	    			   response = client.execute(new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
			                      "ZWave_Switch_Box_2.onOffSwitch.stateControl" + "&body=" + "true"));
		    			
		    		}else
		    		
		    		if(flag4Off == true)
		    		{
		    			flag4Off = false;
		    			HttpGet httpGet = new HttpGet("https://" + ipAdr + ":8443/servletAndroid/write" + "?resource=" + 
		    					"ZWave_Switch_Box_2.onOffSwitch.stateControl" + "&body=" + "false");
		    			response = client.execute(httpGet);
		    			
		    		}   
	    		   
	    		   
	    		   if(response != null)
		    		{
	    			    String result = "";
		    			HttpEntity entity = response.getEntity();
		    			InputStream in = entity.getContent();
		    			BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    			String line;
		    			while((line = br.readLine()) != null) result = result + line;
		    			in.close();
		    	
						JSONObject jsonObj = new JSONObject(result);
						
						if(jsonObj != null) {
							
							JSONArray jsonArr = jsonObj.getJSONArray("values");
						
							if(!jsonArr.isNull(0))
							{
								if(jsonArr.getBoolean(0) == true) toggle1Status = "on";
								else toggle1Status = "off";
							}
							
							if(!jsonArr.isNull(1))
							{
								if(jsonArr.getBoolean(1) == true) toggle2Status = "on";
								else toggle2Status = "off";
							}
							
							if(!jsonArr.isNull(2))
							{
								if(jsonArr.getBoolean(2) == true) toggle3Status = "on";
								else toggle3Status = "off";
							}
							
							if(!jsonArr.isNull(3))
							{
								if(jsonArr.getBoolean(3) == true) toggle4Status = "on";
								else toggle4Status = "off";
							}
						}
						
						
		    		}
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	   
	    	  
	    	   
			return ""; 	
		 }
		 @Override
	        protected void onPostExecute(String result) {
	            	
			 setToggles();
	       }
	        
	    }
	 
	 
	
//	 private class DoRESTRequestOff extends AsyncTask<String, Void, String> {
//		 
//	        protected String doInBackground(String... urls) {
//	              
//	        	DefaultHttpClient client = getNewHttpClient();
//	    		HttpGet httpGet = new HttpGet("https://" + getIPAddressFromTextField() + ":8443/rest/resources/" + urls[0] + "?user=mobile&pw=mobile&depth=100");
//	    		
//	    		String result = "";
//	    		String result2 = "";
//	    		
//	    		try {
//	    			HttpResponse response = client.execute(httpGet);
//	    			HttpEntity entity = response.getEntity();
//	    			InputStream in = entity.getContent();
//	    			BufferedReader br = new BufferedReader(new InputStreamReader(in));
//	    			String line;
//	    			while((line = br.readLine()) != null) result = result + line;
//	    			in.close();
//	    	
//					JSONObject json = new JSONObject(result);
//					
//					if(json.has("resourcelink"))
//					{
//						JSONObject jsonTemp = json.getJSONObject("resourcelink");
//					    
//						HttpGet httpGet2 = new HttpGet("https://" +  getIPAddressFromTextField() + ":8443/rest/resources/" + jsonTemp.get("link") + "?user=mobile&pw=mobile&depth=100");
//						
//						HttpResponse response2 = client.execute(httpGet2);
//		    			HttpEntity entity2 = response2.getEntity();
//		    			InputStream in2 = entity2.getContent();
//		    			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
//		    			String line2;
//		    			while((line2 = br2.readLine()) != null) result2 = result2 + line2;
//		    			in.close();
//		    			json = new JSONObject(result2);
//		    			
//		    			json.remove("value");
//						json.put("value", false);
//						
//						HttpPut httpPut = new HttpPut("https://" + getIPAddressFromTextField() + ":8443/rest/resources/" + jsonTemp.getString("link") + "?user=mobile&pw=mobile&depth=100");
//					
//						httpPut.addHeader("Content-Type", "application/json");
//						httpPut.addHeader("Accept", "application/json");
//						httpPut.setEntity(new StringEntity(json.toString()));
//						
//						client.execute(httpPut);
//					}
//					else
//					{
//						json.remove("value");
//						json.put("value", false);
//						
//						HttpPut httpPut = new HttpPut("https://" + getIPAddressFromTextField() + ":8443/rest/resources/" + urls[0] + "?user=mobile&pw=mobile&depth=100");
//					
//						httpPut.addHeader("Content-Type", "application/json");
//						httpPut.addHeader("Accept", "application/json");
//						httpPut.setEntity(new StringEntity(json.toString()));
//						
//						client.execute(httpPut);
//					
//					}
//		    			
//							
//				} catch (JSONException | IOException e) {
//					e.printStackTrace();
//				}
//	    		
//	    		return result;
//	        }
//	        
//	        @Override
//	        protected void onPostExecute(String result) {
//	          
//	       }
//	    }
	 
	
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