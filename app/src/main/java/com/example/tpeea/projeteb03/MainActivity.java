package com.example.tpeea.projeteb03;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private OscilloManager mOscilloManager;
    private FrameProcessor mFrameProcessor;
    private Slider mSlider;
    //private Handler mHandler;
    private static Handler mHandler;
    private TextView mTextViewValue;
    private TextView mTextViewString;
    private OscilloGraphView mOGView;
    private final static int NO_ADAPTER = 0;
    private final static String[] PERMISSIONS = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};
    private final int PERMISSIONS_REQUEST_CODE = 1;
    private final int USER_REQUEST = 2;
    private final int PERMISSION_GRANTED = 3;


    public static final int MESSAGE_READ = 4;
    public static final int MESSAGE_WRITE = 5;
    public static final int MESSAGE_TOAST = 6;

    //bails étranges
    private static final int MAX_SAMPLES = 640;
    private static final int  MAX_LEVEL	= 240;
    private static final int  DATA_START = (MAX_LEVEL + 1);
    private static final int  DATA_END = (MAX_LEVEL + 2);

    private static final byte  REQ_DATA = 0x00;
    private static final byte  ADJ_HORIZONTAL = 0x01;
    private static final byte  ADJ_VERTICAL = 0x02;
    private static final byte  ADJ_POSITION = 0x03;

    private static final byte  CHANNEL1 = 0x01;
    private static final byte  CHANNEL2 = 0x02;

    private int[] ch1_data = new int[MAX_SAMPLES/2];
    private int[] ch2_data = new int[MAX_SAMPLES/2];

    private int dataIndex=0, dataIndex1=0, dataIndex2=0;
    private boolean bDataAvailable=false;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.mSlider = findViewById(R.id.mSlider);
        //this.mHandler = new Handler();
        this.mHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case MESSAGE_TOAST:
                    //Toast.makeText(getApplicationContext(), msg.getData().getString(),Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_READ:
                        int raw, data_length, x;
                        byte[] readBuf = (byte[]) msg.obj;
                        data_length = msg.arg1;
                        for(x=0; x<data_length; x++){
                            raw = UByte(readBuf[x]);
                            if( raw>MAX_LEVEL ){
                                if( raw==DATA_START ){
                                    bDataAvailable = true;
                                    dataIndex = 0; dataIndex1=0; dataIndex2=0;
                                }
                                else if( (raw==DATA_END) || (dataIndex>=MAX_SAMPLES) ){
                                    bDataAvailable = false;
                                    dataIndex = 0; dataIndex1=0; dataIndex2=0;
                                    mOGView.set_data(ch1_data, ch2_data);
                                    /*if(bReady){ // send "REQ_DATA" again
                                        MainActivity.this.sendMessage( new String(new byte[] {REQ_DATA}) );
                                    }*/
                                    break;
                                }
                            }
                            else if( (bDataAvailable) && (dataIndex<(MAX_SAMPLES)) ){ // valid data
                                if((dataIndex++)%2==0) ch1_data[dataIndex1++] = raw;	// even data
                                else ch2_data[dataIndex2++] = raw;	// odd data
                            }

                        }
                        break;
                }

            }
            private int UByte(byte b){
                if(b<0) // if negative
                    return (int)( (b&0x7F) + 128 );
                else
                    return (int)b;
            }
        };
        this.mBluetoothManager = new BluetoothManager(this, mHandler);
        this.mOscilloManager= OscilloManager.getOscilloManager();
        this.mFrameProcessor= new FrameProcessor();
        //mTextViewValue = findViewById(R.id.valueSlider);
        mTextViewString = findViewById(R.id.stringSlider);
        mOGView=findViewById(R.id.oscilloView);




        mSlider.setSliderListener(new Slider.SliderListener() {
            @Override
            public void onValueChanged(View view, float value) {
                if(view.getId()==R.id.mSlider){
                    //envoyer la commande
                    //mTextViewValue.setText(String.valueOf((int)value));

                    if(mBluetoothManager.getBluetoothState()==mBluetoothManager.STATE_CONNECTED){
                        byte[] trame=mFrameProcessor.toFrame(mOscilloManager.setCalibrationDutyCycle(value));
                        mTextViewString.setText(mFrameProcessor.str);
                        mBluetoothManager.write(trame);
                        Toast.makeText(MainActivity.this,"envoi de la trame",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onDoubleClick(View view,float value) {
                Toast.makeText(MainActivity.this,"Double Click",Toast.LENGTH_SHORT).show();
                if(view.getId()==R.id.mSlider){
                    //mTextViewValue.setText(String.valueOf((int)value));
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.connect:
                //Toast.makeText(this, "ultracoolos", Toast.LENGTH_SHORT).show();
                switch (BluetoothRights()) {
                    case NO_ADAPTER: {
                        Toast.makeText(this, "Il faut un adaptateur Bluetooth", Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case PERMISSION_GRANTED: { //si toutes les permissions sont filées, on check si le bt est activé et on crée une nouvelle activité
                        if (!mBluetoothAdapter.isEnabled()) {
                            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
                        } else {
                            startActivityForResult(new Intent(this, BluetoothConnectActivity.class), 2);
                        }
                    }
                    break;
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private int BluetoothRights() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            return NO_ADAPTER;
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkMultiplePermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return USER_REQUEST;
            }
        }
        return PERMISSION_GRANTED;
    }

    private boolean checkMultiplePermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PERMISSION_DENIED) {
                return false;
            }
        }
        return true;

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                BluetoothDevice btDevice = data.getParcelableExtra("btDevice");
                Toast.makeText(this, btDevice.getName(), Toast.LENGTH_SHORT).show();
                //connection à btdevice:
                mBluetoothManager.connect(btDevice);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "retour cancelled", Toast.LENGTH_SHORT).show();
            }
        }

    }
}