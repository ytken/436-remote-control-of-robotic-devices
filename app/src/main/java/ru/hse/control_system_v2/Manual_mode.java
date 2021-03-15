package ru.hse.control_system_v2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import androidx.appcompat.widget.SwitchCompat;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;
import ru.hse.control_system_v2.dbprotocol.ProtocolRepo;

public class Manual_mode extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private boolean is_hold_command;

    private DataThread dataThreadForArduino;                  // устройство, с которого буду получаю получать данные
    //TODO:переделать в массив
    private Timer arduino_timer;            // таймер для arduino

    private String[] pre_str_sens_data;             // форматирование вывода данных с сенсоров
    private int[] sens_data;                        // непосредственно данные с сенсоров
    private final byte[] message= new byte[32];      // комманда посылаемая на arduino
    private byte prevCommand = 0;
    String MAC;
    String classDevice;
    private TextView text_sens_data;
    private byte inputPacket[];
    OutputStream OutStrem;
    InputStream InStrem;
    private int[] my_data;
    private boolean ready_to_request;         // флаг готовности принятия данных: true - высылай новый пакет   false - не высылай пакет
    BluetoothSocket clientSocket;

    public void setSocket(BluetoothSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    ProtocolRepo getDevicesID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_mode);
        showToast("Started Manual mode!");
        findViewById(R.id.button_stop).setEnabled(false);

        clientSocket = SocketHandler.getSocket();

        //много устройств, но сейчас одно
        Bundle b = getIntent().getExtras();
        MAC = b.get("MAC").toString();
        classDevice = b.get("protocol").toString();
        dataThreadForArduino = new DataThread();
        dataThreadForArduino.setSelectedDevice(MAC);
        dataThreadForArduino.setSocket(clientSocket);
        dataThreadForArduino.setProtocol(classDevice);
        dataThreadForArduino.start();

        pre_str_sens_data = new String[5];
        pre_str_sens_data[0] = "     0º \t\t-\t\t ";
        pre_str_sens_data[1] = " -45º \t\t-\t\t ";
        pre_str_sens_data[2] = "  45º \t\t-\t\t ";
        pre_str_sens_data[3] = " -90º \t\t-\t\t ";
        pre_str_sens_data[4] = "  90º \t\t-\t\t ";

        sens_data = new int[5];
        my_data = new int[12];

        is_hold_command = false;
        boolean is_sens_data = false;
        boolean is_fixed_angel = false;
        getDevicesID = new ProtocolRepo(getApplicationContext(), b.getString("protocol"));
        MAC = b.getString("MAC");

        //String MAC = DeviceRepository.getInstance(getApplicationContext()).item(b.getInt("id")).getMAC();

        if (!BluetoothAdapter.checkBluetoothAddress(MAC)) {
            showToast("Wrong MAC address");
            Manual_mode.this.finish();
        }

        TextView deviceInfo = findViewById(R.id.textViewNameManual);
        deviceInfo.setText("Устройство: " + b.getString("name") + "\n MAC: " + MAC);

        arduino_timer = new Timer();
        // функция выполняющаяся при тике таймера для arduino
        TimerTask arduino_timer_task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Data_request();
                    }
                });
            }
        };

        Button button_left_45 = findViewById(R.id.button_left_45);
        button_left_45.setVisibility(View.INVISIBLE);
        Button button_right_45 = findViewById(R.id.button_right_45);
        button_right_45.setVisibility(View.INVISIBLE);
        Button button_left_90 = findViewById(R.id.button_left_90);
        button_left_90.setVisibility(View.INVISIBLE);
        Button button_right_90 = findViewById(R.id.button_right_90);
        button_right_90.setVisibility(View.INVISIBLE);


        findViewById(R.id.button_stop).setOnClickListener(this);
        findViewById(R.id.button_left_45).setOnClickListener(this);
        findViewById(R.id.button_right_45).setOnClickListener(this);
        findViewById(R.id.button_left_90).setOnClickListener(this);
        findViewById(R.id.button_right_90).setOnClickListener(this);

        findViewById(R.id.button_up).setOnTouchListener(touchListener);
        findViewById(R.id.button_down).setOnTouchListener(touchListener);
        findViewById(R.id.button_left).setOnTouchListener(touchListener);
        findViewById(R.id.button_right).setOnTouchListener(touchListener);

        Switch hold_command = findViewById(R.id.switch_hold_command_mm);
        hold_command.setOnCheckedChangeListener(this);

        Arrays.fill(message, (byte) 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data==null) return;
        String message = data.getStringExtra("message");
        Intent intent = new Intent();
        intent.putExtra("message", message);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //arduino.BluetoothConnectionServiceVoid();     // соединяемся с bluetooth
        //TODO - вызывает вылет приложения
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        message[4] = getDevicesID.get("new_command");
        message[5] = getDevicesID.get("type_move");
        message[6] = getDevicesID.get("STOP");
        dataThreadForArduino.Send_Data(message);

        if(arduino_timer != null)
        {
            arduino_timer.cancel();
            arduino_timer = null;
        }

        try
        {
            dataThreadForArduino.Disconnect();                 // отсоединяемся от bluetooth
            //arduino.Shut_down_bt();               // и выключаем  bluetooth на cubietruck
        }
        catch (Exception e)
        {}
    }

    @Override
    public void onClick(View v)
    {
        message[0] = getDevicesID.get("class_android"); message[1] = getDevicesID.get("type_computer"); // класс и тип устройства отправки
        message[2] = getDevicesID.get("class_arduino"); // класс и тип устройства приема
        switch (v.getId())
        {
            case R.id.button_stop:
                //Toast.makeText(getApplicationContext(), "Стоп всех комманд", Toast.LENGTH_SHORT).show();
                message[4] = (prevCommand == getDevicesID.get("STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                message[5] = getDevicesID.get("type_move");
                message[6] = prevCommand = getDevicesID.get("STOP");
                dataThreadForArduino.Send_Data(message);
                break;
        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            message[0] = getDevicesID.get("class_android");
            message[1] = getDevicesID.get("type_computer"); // класс и тип устройства отправки
            message[2] = getDevicesID.get("class_arduino"); // класс и тип устройства приема
            message[5] = getDevicesID.get("type_move");
            if(event.getAction() == MotionEvent.ACTION_DOWN)                        // если нажали на кнопку и не важно есть удержание команд или нет
            {
                switch (v.getId())
                {
                    case R.id.button_up:
                        //Toast.makeText(getApplicationContext(), "Вперед поехали", Toast.LENGTH_SHORT).show();
                        Log.d("Вперед поехали", "********************************************");
                        message[4] = (prevCommand == getDevicesID.get("FORWARD"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("FORWARD");
                        dataThreadForArduino.Send_Data(message);
                        break;
                    case R.id.button_down:
                        Log.d("Назад поехали", "********************************************");
                        //Toast.makeText(getApplicationContext(), "Назад поехали", Toast.LENGTH_SHORT).show();
                        message[4] = (prevCommand == getDevicesID.get("BACK"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("BACK");
                        dataThreadForArduino.Send_Data(message);
                        break;
                    case R.id.button_left:
                        //Toast.makeText(getApplicationContext(), "Влево поехали", Toast.LENGTH_SHORT).show();
                        Log.d("Влево поехали", "********************************************");
                        message[4] = (prevCommand == getDevicesID.get("LEFT"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("LEFT");
                        dataThreadForArduino.Send_Data(message);
                        break;
                    case R.id.button_right:
                        //Toast.makeText(getApplicationContext(), "Вправо поехали", Toast.LENGTH_SHORT).show();
                        Log.d("Вправо поехали", "********************************************");
                        message[4] = (prevCommand == getDevicesID.get("RIGHT"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                        message[6] = prevCommand = getDevicesID.get("RIGHT");
                        dataThreadForArduino.Send_Data(message);
                        break;
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP)             // если отпустили кнопку
            {
                if(!is_hold_command)    // и нет удержания команд то все кнопки отправляют команду стоп
                {
                    switch (v.getId())
                    {
                        case R.id.button_up:
                            message[4] = (prevCommand == getDevicesID.get("FORWARD_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("FORWARD_STOP");
                            dataThreadForArduino.Send_Data(message);
                            break;
                        case R.id.button_down:
                            message[4] = (prevCommand == getDevicesID.get("BACK_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("BACK_STOP");
                            dataThreadForArduino.Send_Data(message);
                            break;
                        case R.id.button_left:
                            message[4] = (prevCommand == getDevicesID.get("LEFT_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("LEFT_STOP");
                            dataThreadForArduino.Send_Data(message);
                            break;
                        case R.id.button_right:
                            message[4] = (prevCommand == getDevicesID.get("RIGHT_STOP"))? getDevicesID.get("redo_command"): getDevicesID.get("new_command");
                            message[6] = prevCommand = getDevicesID.get("RIGHT_STOP");
                            dataThreadForArduino.Send_Data(message);
                            break;
                    }
                }
            }
            return false;
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch (buttonView.getId())
        {
            case R.id.switch_hold_command_mm:
                is_hold_command = isChecked;
                if(is_hold_command)
                {
                    Toast.makeText(this, "Удерживание комманды включено: ", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.button_stop).setEnabled(true);
                }
                else
                {
                    Toast.makeText(this, "Удерживание комманды отключено: ", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.button_stop).setEnabled(false);
                }
                break;
        }
    }

    private void Data_request()
    {
        if (dataThreadForArduino.isReady_to_request()) // если готовы принимать данные, таймер действует
        {
            sens_data = dataThreadForArduino.getMy_data();
            text_sens_data.setText( pre_str_sens_data[0] + sens_data[0] + "\n" +
                    pre_str_sens_data[1] + sens_data[1] + "\n" +
                    pre_str_sens_data[2] + sens_data[2] + "\n" +
                    pre_str_sens_data[3] + sens_data[3] + "\n" +
                    pre_str_sens_data[4] + sens_data[4]);

            dataThreadForArduino.Send_Data(message);
            dataThreadForArduino.setReady_to_request(false); // как только отправили запрос, то так сказать приостанавливаем таймер
        } else // если не готовы получать данные то просто ничего не делаем
        {
            Log.d("qwerty", "******************************************** ошибка");
        }
    }

    // Метод для вывода всплывающих данных на экран
    public void showToast(String outputInfoString) {
        Toast outputInfoToast = Toast.makeText(this, outputInfoString, Toast.LENGTH_SHORT);
        outputInfoToast.show();
    }










}

