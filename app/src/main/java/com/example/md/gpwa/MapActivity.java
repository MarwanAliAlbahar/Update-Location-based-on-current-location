package com.example.md.gpwa;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity
{
	private static MapActivity mActivity;
	private GoogleMapFragment mMapFragment;
	public LatLng mLatLng;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);

		mActivity = this;

		Intent intent = getIntent();
		mLatLng = new LatLng(intent.getDoubleExtra("lat", 0), intent.getDoubleExtra("lng", 0));

		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				mMapFragment = new GoogleMapFragment();
				FragmentManager mFragmentManager = mActivity.getSupportFragmentManager();
				mFragmentManager.beginTransaction().replace(R.id.mapFragmentContainer, mMapFragment).commit();
				return null;
			}
		}.execute();
	}

	private static GoogleMap mMap;
	public static class GoogleMapFragment extends Fragment implements OnMapReadyCallback
	{
		private View mContainer;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			mContainer = inflater.inflate(R.layout.map_fragment, null, false);

			SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);

			return mContainer;
		}

		private float mDensity;
		@Override
		public void onMapReady(GoogleMap googleMap)
		{
			mMap = googleMap;
			mDensity = getResources().getDisplayMetrics().density;
			//mMap.setPadding(0, (int)(mDensity * 100), 0, (int)(mDensity * 90));

			if(ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			{
				ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 11111);
			}
			if(Build.VERSION.SDK_INT >= 23)
			{
				if(ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
						ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				{
					mMap.setMyLocationEnabled(true);
				}
			}
			else
				mMap.setMyLocationEnabled(true);
			UiSettings mUiSettings = mMap.getUiSettings();
			mUiSettings.setRotateGesturesEnabled(false);
			mUiSettings.setTiltGesturesEnabled(false);
			mUiSettings.setScrollGesturesEnabled(true);
			mUiSettings.setZoomGesturesEnabled(true);

			mUiSettings.setMapToolbarEnabled(false);
			mUiSettings.setZoomControlsEnabled(false);
			mUiSettings.setCompassEnabled(false);

			LocationManager mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
			if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				AlertManager.fCreateCallbackAlert(mActivity, "Location Services Disabled", "Please enable location services.",
						true, "Enable Location", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface pDialog, int which)
							{
								startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						});
			}
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mActivity.mLatLng, 11.0f));
			int height = (int)(40*mDensity);
			int width = (int)(25*mDensity);
			BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.sourcemarker);
			Bitmap b = bitmapdraw.getBitmap();
			Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
			mMap.addMarker(new MarkerOptions()
					.position(mActivity.mLatLng)
					.icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mActivity.mLatLng, 13));

			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(mActivity.mLatLng)
					.zoom(17)
					.build();
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}
	}
}
