package ru.hse.control_system_v2;

import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;

import ru.hse.control_system_v2.list_devices.DeviceItem;

public class SocketHandler {
    private static BluetoothSocket socket;
    private static ArrayList<BluetoothSocket> socketList;
    private static ArrayList<Boolean> resultOfConnection;
    private static ArrayList<DeviceItem> devicesList;

    public static synchronized BluetoothSocket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(BluetoothSocket socket){
        SocketHandler.socket = socket;
    }

    public static synchronized ArrayList<BluetoothSocket> getSocketList(){
        return socketList;
    }

    public static synchronized void setSocketList(ArrayList<BluetoothSocket> socketList){
        SocketHandler.socketList = socketList;
    }

    public static synchronized ArrayList<Boolean> getResultOfConnection(){
        return resultOfConnection;
    }

    public static synchronized void setResultOfConnection(ArrayList<Boolean> resultOfConnection){
        SocketHandler.resultOfConnection = resultOfConnection;
    }

    public static synchronized ArrayList<DeviceItem> getDevicesList(){
        return devicesList;
    }

    public static synchronized void setDevicesList(ArrayList<DeviceItem> devicesList){
        SocketHandler.devicesList = devicesList;
    }
}