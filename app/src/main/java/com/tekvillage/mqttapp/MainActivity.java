package com.tekvillage.mqttapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private Messenger service = null;
    private final Messenger serviceHandler = new Messenger(new ServiceHandler());
    private IntentFilter intentFilter = null;
    private PushReceiver pushReceiver;
    TextView statuspanelField, logviewField;
    EditText subscribetopicField, publishMessageField;
    Button subscribeBtn, publishBtn;

    private String TAG = "MQTT MAINACTIVITY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statuspanelField = (TextView) findViewById(R.id.statuspanel);
        logviewField = (TextView) findViewById(R.id.logview);
        subscribetopicField = (EditText) findViewById(R.id.topic);
        publishMessageField = (EditText) findViewById(R.id.message);


        subscribeBtn = (Button) findViewById(R.id.subscribebtn);
        publishBtn = (Button) findViewById(R.id.publishbtn);
        subscribeBtn.setOnClickListener(onClickListener);
        publishBtn.setOnClickListener(onClickListener);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.tekvillage.mqttapp.PushReceived");
        pushReceiver = new PushReceiver();
        registerReceiver(pushReceiver, intentFilter, null, null);

        startService(new Intent(this, MQTTservice.class));
    }


    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder)
        {
            service = new Messenger(binder);
            Bundle data = new Bundle();
            //data.putSerializable(MQTTservice.CLASSNAME, MainActivity.class);
            data.putCharSequence(MQTTservice.INTENTNAME, "com.tekvillage.mqttapp.PushReceived");
            Message msg = Message.obtain(null, MQTTservice.REGISTER);
            msg.setData(data);
            msg.replyTo = serviceHandler;
            try
            {
                service.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String topic ="";
            int id = view.getId();
            switch (id){
                case R.id.subscribebtn:
                     topic = subscribetopicField.getText().toString();
                    if (topic != null && topic.isEmpty() == false)
                    {
                        statuspanelField.setText("");
                        Bundle data = new Bundle();
                        data.putCharSequence(MQTTservice.TOPIC, topic);
                        Message msg = Message.obtain(null, MQTTservice.SUBSCRIBE);
                        msg.setData(data);
                        msg.replyTo = serviceHandler;
                        try
                        {
                            service.send(msg);
                        }
                        catch (RemoteException e)
                        {
                            e.printStackTrace();
                            statuspanelField.setText("Subscribe failed with exception:" + e.getMessage());
                        }
                    }
                    else
                    {
                        statuspanelField.setText("Topic required.");
                    }
                    break;
                case R.id.publishbtn:
                     topic = subscribetopicField.getText().toString();
                    String message = publishMessageField.getText().toString();
                    if (topic != null && topic.isEmpty() == false && message != null && message.isEmpty() == false)
                    {
                        statuspanelField.setText("");
                        Bundle data = new Bundle();
                        data.putCharSequence(MQTTservice.TOPIC, topic);
                        data.putCharSequence(MQTTservice.MESSAGE, message);
                        Message msg = Message.obtain(null, MQTTservice.PUBLISH);
                        msg.setData(data);
                        msg.replyTo = serviceHandler;
                        try
                        {
                            Log.d(TAG,"Sending message.."+message);
                            service.send(msg);
                        }
                        catch (RemoteException e)
                        {
                            e.printStackTrace();
                            statuspanelField.setText("Publish failed with exception:" + e.getMessage());
                        }
                    }
                    else
                    {
                        statuspanelField.setText("Topic and message required.");
                    }
                    break;
            }
        }
    };

    public class PushReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent i)
        {
            String topic = i.getStringExtra(MQTTservice.TOPIC);
            String message = i.getStringExtra(MQTTservice.MESSAGE);
            Log.d(TAG,"Push recieved...."+topic+"  "+message);
            logviewField.append(System.getProperty("line.separator")+topic+": "+message);
            Toast.makeText(context, "Push message received - " + topic + ":" + message, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        bindService(new Intent(this, MQTTservice.class), serviceConnection, 0);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(pushReceiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(pushReceiver);
    }


    class ServiceHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            Log.d(TAG,"WHAT : "+msg.what);
            switch (msg.what)
            {


                case MQTTservice.SUBSCRIBE: 	break;
                case MQTTservice.PUBLISH:		break;
                case MQTTservice.REGISTER:		break;
                default:
                    super.handleMessage(msg);
                    return;
            }

            Bundle b = msg.getData();
            if (b != null)
            {
                TextView result = statuspanelField;//(TextView) findViewById(R.id.textResultStatus);
                Boolean status = b.getBoolean(MQTTservice.STATUS);
                if (status == false)
                {
                    result.setText("Fail");
                }
                else
                {
                    result.setText("Success");
                }
            }
        }
    }
}
