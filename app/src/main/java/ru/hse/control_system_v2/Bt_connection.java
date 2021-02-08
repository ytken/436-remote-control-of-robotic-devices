package ru.hse.control_system_v2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Bt_connection extends Activity
{
    private final String LOG_TAG = "myLogs";

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket1;
    Context mContext;
    private static String MacAddress1;// MAC-адрес БТ модуля
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler h;
    private Thread_bt_data_receiver data_receiver1;
    private byte inputPacket[];                   // входящие данные от арудины
    private int[] my_data;

    private boolean ready_to_request;         // флаг готовности принятия данных: true - высылай новый пакет   false - не высылай пакет

    private void MyError(String title, String message, String client)
    {
        Toast.makeText(mContext, title + " - " + message, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra("message", client);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public Bt_connection(Context context, String address)
    {
        Log.d(LOG_TAG, "constructor");
        boolean is_enabled = false;
        MacAddress1 = address;
        mContext = context;

        Log.d(LOG_TAG, "получаем адаптер");
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        btSocket1 = null;

        inputPacket = new byte[12];
        my_data = new int[12];

        ready_to_request = true;


        Log.d(LOG_TAG, "проверяем включение");
        if (btAdapter != null)
        {
            if (!btAdapter.isEnabled())
            {

                Log.d(LOG_TAG, "блютуз не включен, включаем его");
                btAdapter.enable();
            }
            else
            {
                is_enabled = true;
            }


            Log.d(LOG_TAG, "проверяем включился ли блютуз");
            while (!is_enabled)
            {
                if (btAdapter.isEnabled())
                {
                    is_enabled = true;

                    Log.d(LOG_TAG, "блютуз включился");
                }
            }


            Log.d(LOG_TAG, "блютуз включен, продолжаем дальше");

            h = new Handler()
            {public void handleMessage(android.os.Message msg) {}};
        }
    }

    public void Connect(Timer bt_timer, TimerTask bt_timer_task) // для работы через определенные промежутки времени
    {

        Log.d(LOG_TAG, "получаем блютуз устройства");


        BluetoothDevice device1 = btAdapter.getRemoteDevice(MacAddress1);

        Log.d(LOG_TAG, "***Получили удаленный Device***" + device1.getName());

        try
        {
            btSocket1 = device1.createRfcommSocketToServiceRecord(MY_UUID);

            Log.d(LOG_TAG, "...Создали сокет...");
        } catch (IOException e)
        {
            MyError("Fatal Error", "В Connect() Не могу создать сокет: " + e.getMessage() + ".", "Не могу создать сокет.");
        }

        btAdapter.cancelDiscovery();


        Log.d(LOG_TAG, "***Отменили поиск других устройств***");

        Log.d(LOG_TAG, "***Соединяемся...***");

        try
        {
            btSocket1.connect();

            Log.d(LOG_TAG, "***Соединение успешно установлено***");
        } catch (IOException e)
        {
            try
            {
                btSocket1.close();

                Log.d(LOG_TAG, "***Соединение не установлено***");
            } catch (IOException e2)
            {
                MyError("Fatal Error", "В onResume() не могу закрыть сокет" + e2.getMessage() + ".", "***Соединение не установлено***");
            }
        }

        data_receiver1 = new Thread_bt_data_receiver(btSocket1);
        data_receiver1.start();

        bt_timer.schedule(bt_timer_task, 0, 300);
    }

    public void Connect() // при ручном управлении передачей пакетов
    {

        Log.d(LOG_TAG, "получаем блютуз устройства");

        BluetoothDevice device1 = btAdapter.getRemoteDevice(MacAddress1);

        if (device1.getName() == null){
            MyError("Fatal Error", "Устройство не найдено (проверьте правильность MAC адреса", "Устройство не найдено (проверьте правильность MAC адреса");
        }
        Log.d(LOG_TAG, "***Получили удаленный Device***" + device1.getName());


        try
        {
            btSocket1 = device1.createRfcommSocketToServiceRecord(MY_UUID);

            Log.d(LOG_TAG, "...Создали сокет...");
        } catch (IOException e)
        {
            MyError("Fatal Error", "В Connect() Не могу создать сокет: " + e.getMessage() + ".", "Не могу создать сокет.");
        }

        btAdapter.cancelDiscovery();

        Log.d(LOG_TAG, "***Отменили поиск других устройств***");

        Log.d(LOG_TAG, "***Соединяемся...***");

        try
        {
            btSocket1.connect();

            Log.d(LOG_TAG, "***Соединение успешно установлено***");
        } catch (IOException e)
        {
            try
            {
                btSocket1.close();

                Log.d(LOG_TAG, "***Соединение не установлено***");
            } catch (IOException e2)
            {
                MyError("Fatal Error", "В onResume() не могу закрыть сокет" + e2.getMessage() + ".", "***Соединение не установлено***");
            }
        }

        data_receiver1 = new Thread_bt_data_receiver(btSocket1);
        data_receiver1.start();
    }

    public void Disconnect(Timer bt_timer) // для работы через определенные промежутки времени
    {
        Log.d(LOG_TAG, "...In onPause()...");

        if (data_receiver1.status_OutStrem() != null)
        {
            data_receiver1.cancel();
            bt_timer.cancel();
        }

        try
        {
            btSocket1.close();

        } catch (IOException e2)
        {
            MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".", "Не могу закрыть сокет.");
        }

    }

    public void Disconnect() // при ручном управлении передачей пакетов
    {
        Log.d(LOG_TAG, "...In onPause()...");

        if (data_receiver1.status_OutStrem() != null)
        {
            data_receiver1.cancel();
        }

        try
        {
            btSocket1.close();

        } catch (IOException e2)
        {
            MyError("Fatal Error", "В onPause() Не могу закрыть сокет" + e2.getMessage() + ".", "Не могу закрыть сокет.");
        }
    }

    public void Shut_down_bt()
    {
        btAdapter.disable();
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

    public void Send_Data(String message) { data_receiver1.sendData(message);}

    public void Send_Data(byte message[]) { data_receiver1.sendData(message);}

    public class Thread_bt_data_receiver extends Thread // подкласс поток для приема и передачи данных
    {
        private final BluetoothSocket bt_socket;
        private final OutputStream OutStrem;
        private final InputStream InStrem;

        public Thread_bt_data_receiver(BluetoothSocket socket)
        {
            bt_socket = socket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try
            {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e)
            {
            }

            OutStrem = tmpOut;
            InStrem = tmpIn;
        }

        @Override
        public void run()
        {
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
                        Log.d(LOG_TAG, "Как то много всего пришло");
                        pacNum = 0;

                        byte[] message = new byte[]{0x30, 0x15, 0x0f, 0x37, 0x37, 0x37};
                        data_receiver1.sendData(message);
                    }

                    if (pacNum == 12) // все нормально
                    {
                        pacNum = 0;//здесь проверяем пакет и прочее

                        Log.d(LOG_TAG, "***Получаем данные: " +
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
            Log.d(LOG_TAG, "***Отправляем данные: " + message + "***");

            try
            {
                OutStrem.write(msgBuffer);
            } catch (IOException e)
            {
            }
        }

        public void sendData(byte message[])
        {
            String logMessage = "***Отправляем данные: ";
            for (int i=0; i < 32; i++)
                logMessage += message[i] + " ";
            Log.d(LOG_TAG, logMessage + "***");
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
                bt_socket.close();
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

}

//00:18:91:D7:99:9E
