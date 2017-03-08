package com.adherence.adherence;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;

public class DeviceList implements Serializable
{

    ArrayAdapter<String> mDeviceAdapter;

    public DeviceList(ArrayAdapter<String> adapter, ListView view)
    {
        mDeviceAdapter = adapter;
        view.setAdapter(mDeviceAdapter);
        mDeviceAdapter.clear();
    }

    public void add(String name)
    {
        String deviceName = findDeviceWithName(name);

        if (deviceName == null)//only add if not already in the list
        {
            if (name != null)
            {
                mDeviceAdapter.add(name);
            }
        }
    }

    public String get(int position)
    {
        return mDeviceAdapter.getItem(position);
    }

    public void remove(String name)
    {
        String toRemove = findDeviceWithName(name);

        if (toRemove != null)
        {
            mDeviceAdapter.remove(toRemove);
        }
    }

    public void clear()
    {
        mDeviceAdapter.clear();
    }

    public String findDeviceWithName(String name)
    {
        String deviceName = null;
        int count = mDeviceAdapter.getCount();
        for (int i=0; i<count; i++)
        {
            String nextName = mDeviceAdapter.getItem(i);

            if (nextName.equals(name))
            {
                deviceName = nextName;
                break;
            }
        }
        return deviceName;
    }

    public int count () {
        int count = mDeviceAdapter.getCount();
        return count;
    }
}
