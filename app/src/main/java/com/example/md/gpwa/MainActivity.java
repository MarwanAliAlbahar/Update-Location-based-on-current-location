package com.example.md.gpwa;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
	private final String RESTAURANT_FETCH_URL = "http://apk-download.tk/distance/getRestaurant.php";
	private MainActivity mActivity;
	private RelativeLayout mContainer;
	private CustomAdapter customAdapter;
	private LocationManager mLocationManager;
	private Dialog mWaitDialog;

	private ArrayList<DataHolder> mRestaurantsList = new ArrayList<>();
	private ArrayList<Float> mRestaurantDistance = new ArrayList<>();
	private ArrayList<String> mRestaurantDuration = new ArrayList<>();
	private LocationListener mLocationListener = new LocationListener()
	{
		@Override
		public void onLocationChanged(final Location currentLocation)
		{
			Log.e("Location", "Update");
			mActivity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					fFindDistance(currentLocation);
				}
			});
		}

		@Override
		public void onStatusChanged(String s, int i, Bundle bundle){}

		@Override
		public void onProviderEnabled(String s){}

		@Override
		public void onProviderDisabled(String s){}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mActivity = this;

		mContainer = (RelativeLayout) findViewById(R.id.activity_main);
		ListView mListView = (ListView) findViewById(R.id.listview);
		customAdapter = new CustomAdapter(mActivity);
		mListView.setAdapter(customAdapter);

		if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 10011);

		mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
		if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			AlertManager.fCreateCallbackAlert(mActivity, "Location Services Disabled", "Please enable location services.", true, "Enable Location", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface pDialog, int which)
				{
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			});
		}

		//Millisecond and Meters
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10*1000, 0, mLocationListener);

		mWaitDialog = new Dialog(mActivity);
		mWaitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mWaitDialog.setContentView(R.layout.wait_dialog_layout);
		mWaitDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mWaitDialog.setCancelable(false);
	}

	@Override
	protected void onDestroy()
	{
		if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 10011);
		mLocationManager.removeUpdates(mLocationListener);
		super.onDestroy();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch(requestCode)
		{
			case 10011:
			{
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){}
				else
					Snackbar.make(mContainer, "Pls grant permission for the app to work", Snackbar.LENGTH_SHORT).show();
				return;
			}
		}
	}

	private void fFindDistance(final Location currentLocation)
	{
		//Fetch the Restuarants from Mysql and finds the distance

		if(!NetworkManager.fIsConnectionAvailable(mActivity))
		{
			Snackbar.make(mContainer, "Network Error. Check network and try again", Snackbar.LENGTH_LONG).show();
			return;
		}

		String city = "";
		try
		{
			if(Geocoder.isPresent()) {
				Geocoder geocoder;
				List<Address> addresses;
				geocoder = new Geocoder(this, Locale.getDefault());

				addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);

				city = addresses.get(0).getLocality();
			}
			//Snackbar.make(mContainer, "Device does not have geocoder", 1000).show();
		}
		catch(Exception pE)
		{
			Log.e("Error", "Geocoder Error", pE);
			AlertManager.fCreateAlert(mActivity, "Cannot Find City", "Unable to get the city using gps coordinates", true, "OK");
		}

		//Only for testing because the emulator has not Geocoder
		city = "Kuopio";
		Log.e("City", city);
		if(city.length() > 0)
		{
			final String[] mKeys = new String[]{"city"};
			final String[] mValues = new String[]{city};

			mWaitDialog.show();

			mRestaurantsList.clear();
			ServerTaskAsync mRegisterTask = new ServerTaskAsync(RESTAURANT_FETCH_URL);
			mRegisterTask.fExcute(mKeys, mValues, new ServerTaskAsyncCallback()
			{
				@Override
				public void onTaskComplete(JSONObject pJSONResponse)
				{
					try
					{
						if(Integer.parseInt(pJSONResponse.get("Code").toString()) == 0)
						{
							mWaitDialog.dismiss();
							AlertManager.fCreateAlert(mActivity, "Error", "Failed to get Restaurants", true, "OK");
						}
						else
						{
							JSONArray mJsonArray = pJSONResponse.getJSONArray("Data");
							for(int i = 0; i < mJsonArray.length(); i++)
							{
								JSONArray json = mJsonArray.getJSONArray(i);
								DataHolder data = new DataHolder();
								data.setName(json.getString(0));
								data.setAddress(json.getString(1));
								data.setLat(json.getDouble(2));
								data.setLng(json.getDouble(3));
								mRestaurantsList.add(data);
							}
							mActivity.runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									mRestaurantDistance.clear();
									for(int i = 0; i < mRestaurantsList.size(); i++)
									{
										String str_origin = "origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
										String str_dest = "destination=" + mRestaurantsList.get(i).getLat() + "," + mRestaurantsList.get(i).getLng();
										String sensor = "sensor=false";
										String parameters = str_origin + "&" + str_dest + "&" + sensor;
										String output = "json";
										String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
										new FindRouteTask().execute(url);
									}
								}
							});
							mWaitDialog.dismiss();
						}
					}
					catch(Exception pE)
					{
						mWaitDialog.dismiss();
						AlertManager.fCreateAlert(mActivity, "Error", "Failed to get Restaurants", true, "OK");
						Log.e("Error", Log.getStackTraceString(pE));
					}
				}

				@Override
				public void onTaskFailed()
				{
					mWaitDialog.dismiss();
					AlertManager.fCreateAlert(mActivity, "Error", "Failed to get Restaurants from Mysql", true, "OK");
				}
			});
		}

	}

	private class FindRouteTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... pUrls)
		{
			String data = "";
			HttpURLConnection urlConnection = null;
			InputStream iStream = null;
			try
			{
				URL url = new URL(pUrls[0]);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.connect();
				iStream = urlConnection.getInputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
				StringBuffer sb = new StringBuffer();
				String line;
				while((line = br.readLine()) != null)
				{
					sb.append(line);
				}
				data = sb.toString();
				br.close();
			}
			catch(Exception pE)
			{
				Snackbar.make(mContainer, "Error generating distance.", Snackbar.LENGTH_SHORT).show();
				Log.e("Generating distance", Log.getStackTraceString(pE));
			}
			finally
			{
				try
				{
					iStream.close();
					urlConnection.disconnect();
				}
				catch(Exception pE)
				{
					Log.e("Error closing Stream", Log.getStackTraceString(pE));
				}
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			try
			{
				JSONObject jObject = new JSONObject(result);
				JSONArray jRoutes = jObject.getJSONArray("routes");
				JSONArray jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
				JSONObject jDistance = ((JSONObject) jLegs.get(0)).getJSONObject("distance");
				String distance = jDistance.getString("text");
				JSONObject jDuration = ((JSONObject) jLegs.get(0)).getJSONObject("duration");
				String duration = jDuration.getString("text");
                 // add distance in the array
				if(distance.length() > 0)
					mRestaurantDistance.add(Float.parseFloat(distance.substring(0, distance.length()-3)));
				else
					mRestaurantDistance.add(0f);
				mRestaurantDuration.add(duration);
				if(mRestaurantDistance.size() == mRestaurantsList.size())
					customAdapter.notifyDataSetChanged();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public class CustomAdapter extends BaseAdapter
	{
		private LayoutInflater mInflater;

		private CustomAdapter(Context pContext)
		{
			mInflater = (LayoutInflater) pContext.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount()
		{
			return mRestaurantDistance.size();
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int pPosition, View pView, ViewGroup pParent)
		{
			pView = mInflater.inflate(R.layout.list_content, null);

			final int pos = findDistanceIndex(pPosition);
			TextView textView = (TextView) pView.findViewById(R.id.restaurantName);
			textView.setText(mRestaurantsList.get(pos).getName());

			TextView textView1 = (TextView) pView.findViewById(R.id.restaurantAddress);
			textView1.setText(mRestaurantsList.get(pos).getAddress());

			TextView textView2 = (TextView) pView.findViewById(R.id.restaurantDistance);
			textView2.setText(mRestaurantDistance.get(pos) + " km Approx " + mRestaurantDuration.get(pos));

			ImageView imageView = (ImageView) pView.findViewById(R.id.restaurantMap);
			imageView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					Intent intent = new Intent(mActivity, MapActivity.class);
					intent.putExtra("lat", mRestaurantsList.get(pos).getLat());
					intent.putExtra("lng", mRestaurantsList.get(pos).getLng());
					startActivity(intent);
				}
			});

			return pView;
		}
	}

	private int findDistanceIndex(int index)
	{
		//Method for sorting before displaying
		int size = mRestaurantsList.size();
		float[] values = new float[size];
		int[] indexes = new int[size];
		for(int z = 0; z < size; z++)
		{
			values[z] = mRestaurantDistance.get(z);
			indexes[z] = z;
		}
		for(int j = 1; j < size; j++)
		{
			float key = values[j];
			int ind = j;
			int i = j - 1;
			while((i > -1) && (values[i] > key))
			{
				values[i + 1] = values[i];
				indexes[i + 1] = indexes[i];
				i--;
			}
			values[i + 1] = key;
			indexes[i + 1] = ind;
		}
		return indexes[index];
	}
}