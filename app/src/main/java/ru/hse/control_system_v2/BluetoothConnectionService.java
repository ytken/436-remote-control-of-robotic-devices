package ru.hse.control_system_v2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.UUID;

public class BluetoothConnectionService extends Service {
    public static BluetoothDevice device;
    String selectedDevice;
    private static final String TAG = "SendDataActivity";
    // SPP UUID сервиса
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothAdapter btAdapter;
    public boolean stateOfConnection = false;
    static BluetoothSocket clientSocket;
    private byte inputPacket[];
    OutputStream OutStrem;
    InputStream InStrem;
    private int[] my_data;
    private BluetoothConnectionThread data_receiver1;
    private boolean ready_to_request;         // флаг готовности принятия данных: true - высылай новый пакет   false - не высылай пакет


    public BluetoothConnectionService(String mac) {
        selectedDevice = mac;
        BluetoothConnectionServiceVoid();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle arguments = intent.getExtras();
        selectedDevice = arguments.get("idOfDevice").toString();
        BluetoothConnectionServiceVoid();
        return Service.START_STICKY;
    }


    public void BluetoothConnectionServiceVoid() {
        my_data = new int[12];

            btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btIsEnabledFlagVoid()) {
                device = btAdapter.getRemoteDevice(selectedDevice);
                // Попытка подключиться к устройству
                // В новом потоке, чтобы Main Activity не зависал
                try {
                    clientSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord", UUID.class).invoke(device, MY_UUID);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    Log.d("BLUETOOTH", e.getMessage());
                    stateOfConnection = false;
                }
                try {
                    clientSocket.connect();
                    // Отключаем поиск устройств для сохранения заряда батареи
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
                    stateOfConnection = true;
                } catch (IOException e) {
                    stateOfConnection = false;
                    try {
                        // В случае ошибки пытаемся закрыть соединение
                        clientSocket.close();
                    } catch (IOException closeException) {
                        Log.d("BLUETOOTH", e.getMessage());
                    }
                    Log.d("BLUETOOTH", e.getMessage());
                }

                if (stateOfConnection) {
                    // Передаём данные о устройстве в Main Activity
                    //MainActivity.clientSocket = clientSocket;
                    //MainActivity.device = device;
                    data_receiver1 = new BluetoothConnectionThread();
                    data_receiver1.start();
                    //try {
                        // Решение ошибки, зависящей от версии Android - даём время на установку полного подключения
                        //Thread.sleep(2000);
                    //} catch (InterruptedException e) {
                        //e.printStackTrace();
                    //}
                    resultOfConnection();
                }

            } else {
                stateOfConnection = false;
            }
            if (!stateOfConnection) {
                resultOfConnection();
                onDestroy();
            }

    }


    public class BluetoothConnectionThread extends Thread // подкласс поток для приема и передачи данных
    {


        @Override
        public void run()
        {
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try
            {
                tmpOut = clientSocket.getOutputStream();
                tmpIn = clientSocket.getInputStream();
            } catch (IOException e)
            {
            }

            OutStrem = tmpOut;
            InStrem = tmpIn;
            inputPacket = new byte[12];
            byte[] buffer = new byte[12]; // 12 должно быть: 2 - префикс 7 - данные 3 - контр сумма
            int bufNum;
            int pacNum = 0;

            while (true) // ну а дальше тоже 12
            {
                try
                {
                    bufNum = InStrem.read(buffer);
                    if (pacNum + bufNum < 13) // нормально считываем данные
                    {
                        System.arraycopy(buffer, 0, inputPacket, pacNum, bufNum);
                        pacNum = pacNum + bufNum;
                    } else // если неправильые данные, то переслать
                    {
                        ready_to_request = false;
                        Log.d(TAG, "Как то много всего пришло");
                        pacNum = 0;

                        byte[] message = new byte[]{0x30, 0x15, 0x0f, 0x37, 0x37, 0x37};
                        data_receiver1.sendData(message);
                    }

                    if (pacNum == 12) // все нормально
                    {
                        pacNum = 0;//здесь проверяем пакет и прочее

                        Log.d(TAG, "***Получаем данные: " +
                                buffer[0] + " " +
                                buffer[1] + " " +
                                buffer[2] + " " +
                                buffer[3] + " " +
                                buffer[4] + " " +
                                buffer[5] + " " +
                                buffer[6] + " " +
                                buffer[7] + " " +
                                buffer[8] + " " +
                                buffer[9] + " " +
                                buffer[10] + " " +
                                buffer[11] + " ");

                        ready_to_request = true;
                    }
                } catch (IOException e)
                {
                    break;
                }
            }
        }
        public void sendData(String message)
        {
            byte[] msgBuffer = message.getBytes();
            Log.d(TAG, "***Отправляем данные: " + message + "***");

            try
            {
                OutStrem.write(msgBuffer);
            } catch (IOException e)
            {
            }
        }

        public void sendData(byte[] message)
        {
            String logMessage = "***Отправляем данные: ";
            for (int i=0; i < 32; i++)
                logMessage += message[i] + " ";
            Log.d(TAG, logMessage + "***");
            try
            {
                OutStrem.write(message);
            } catch (IOException e)
            {
            }
        }

        public void cancel()
        {
            try
            {
                clientSocket.close();
            } catch (IOException e)
            {
            }
        }

        public Object status_OutStrem()
        {
            if (OutStrem == null)
            {
                return null;
            }
            return OutStrem;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //возвращает true, если bluetooth включён
    public boolean btIsEnabledFlagVoid() {
        return btAdapter.isEnabled();
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            // В случае остановки сервиса завершаем соединение
            clientSocket.close();
        } catch (RuntimeException | IOException e) {
            Log.d("BLUETOOTH", e.getMessage());
        }
    }

    // Передаём данные о статусе соединения в Main Activity
    public void resultOfConnection() {
        Intent resultOfConnectionIntent;
        if (!stateOfConnection) {
            resultOfConnectionIntent = new Intent("not_success");
            Log.d("stateOfConnection", "not_success");
        } else {
            resultOfConnectionIntent = new Intent("success");
            Log.d("stateOfConnection", "success");
        }
        sendBroadcast(resultOfConnectionIntent);
    }

    public void Send_Data(String message) { data_receiver1.sendData(message);}

    public void Send_Data(byte message[]) { data_receiver1.sendData(message);}
    public void Disconnect(Timer bt_timer) // для работы через определенные промежутки времени
    {
        Log.d(TAG, "...In onPause()...");

        if (data_receiver1.status_OutStrem() != null)
        {
            data_receiver1.cancel();
            bt_timer.cancel();
        }

        try
        {
            clientSocket.close();

        } catch (IOException e2)
        {
            //MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".", "Не могу закрыть сокет.");
        }

    }
    public void Disconnect() // при ручном управлении передачей пакетов
    {
        Log.d(TAG, "...In onPause()...");

        if (data_receiver1.status_OutStrem() != null)
        {
            data_receiver1.cancel();
        }

        try
        {
            clientSocket.close();

        } catch (IOException e2)
        {
            //MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".", "Не могу закрыть сокет.");
        }
    }

    public boolean isReady_to_request()
    {
        return ready_to_request;
    }

    public void setReady_to_request(boolean ready_to_request)
    {
        this.ready_to_request = ready_to_request;
    }

    public int[] getMy_data()
    {
        for (int i = 0; i < 5; i++) // 12 должно быть: 2 - префикс 7 - данные 3 - контр сумма.  ... ну или 7 - только данные
        {
            my_data[i] = (int) inputPacket[i +2];
            my_data[i] = my_data[i]*5;
        }
        return my_data;
    }

}