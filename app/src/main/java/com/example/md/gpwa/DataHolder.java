package com.example.md.gpwa;

public class DataHolder
{
	//holds all the data for restaurant in a single object for easy management
	private String Name, Address, Duration;
	private double Lat, Lng;

	public String getName()
	{
		return Name;
	}

	public void setName(String name)
	{
		Name = name;
	}

	public String getAddress()
	{
		return Address;
	}

	public void setAddress(String address)
	{
		Address = address;
	}

	public double getLat()
	{
		return Lat;
	}

	public void setLat(double lat)
	{
		Lat = lat;
	}

	public double getLng()
	{
		return Lng;
	}

	public void setLng(double lng)
	{
		Lng = lng;
	}
}