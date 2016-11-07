package de.iis.graphicaldisplayREST;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import de.iis.graphicaldisplayREST.R;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.app.Activity;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		
		TextView tv = (TextView) findViewById(R.id.testServlet);
		final String URL ="https://10.0.2.2:8443/admin/mainpage.html";
		final String testURL ="https://www.google.de";
		
		GraphViewSeries exmplseries = new GraphViewSeries(new GraphViewData[] {
				new GraphViewData(1, 2.0d), new GraphViewData(2, 1.5d),
				new GraphViewData(3, 2.5d), new GraphViewData(4, 1.0d)});
		
		GraphView graphView = new LineGraphView(
			    this // context
			    , "GraphView" // heading
			);
		
			graphView.getGraphViewStyle().setGridColor(Color.DKGRAY);
			graphView.getGraphViewStyle().setVerticalLabelsColor(Color.WHITE);
			graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.WHITE);
			graphView.setHorizontalLabels(new String[] {"2 days ago", "yesterday", "today", "tomorrow"});
			graphView.setVerticalLabels(new String[] {"high", "middle", "low"});
			graphView.getGraphViewStyle().setNumHorizontalLabels(4);
			graphView.getGraphViewStyle().setNumVerticalLabels(3);
			graphView.addSeries(exmplseries); // data
			
			graphView.setScrollable(true);
			// optional - activate scaling / zooming
			graphView.setScalable(true);
			 
			LinearLayout layout = (LinearLayout) findViewById(R.id.graphLayout);
			layout.addView(graphView);

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
