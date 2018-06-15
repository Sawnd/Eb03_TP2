package com.example.tpeea.projeteb03;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;


//TODO gerer onsaveinstancestate pour les changements d'orientation des fragments et de l'activité
//TODO passer le resultat de fromframe dans ubyte

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private OscilloManager mOscilloManager;
    private FrameProcessor mFrameProcessor;

    public BluetoothManager getmBluetoothManager() {
        return mBluetoothManager;
    }

    public void setmBluetoothManager(BluetoothManager mBluetoothManager) {
        this.mBluetoothManager = mBluetoothManager;
    }

    public OscilloManager getmOscilloManager() {
        return mOscilloManager;
    }

    public void setmOscilloManager(OscilloManager mOscilloManager) {
        this.mOscilloManager = mOscilloManager;
    }

    public FrameProcessor getmFrameProcessor() {
        return mFrameProcessor;
    }

    public void setmFrameProcessor(FrameProcessor mFrameProcessor) {
        this.mFrameProcessor = mFrameProcessor;
    }

    private Slider mSlider;
    //private Handler mHandler;
    private static Handler mHandler;
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

    private ChannelFragment ch1Fragment;
    private ChannelFragment ch2Fragment;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.mSlider = findViewById(R.id.mSlider);

       FragmentManager fragmentManager = getFragmentManager();
        /*FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction ();
// work here to change Activity fragments (add, remove, etc.).  Example here of adding.
        fragmentTransaction.add (R.id.main,new ChannelFragment());
        fragmentTransaction.add (R.id.main,new TimeDivFragment());
        fragmentTransaction.commit ();*/

       ch1Fragment= (ChannelFragment) fragmentManager.findFragmentById(R.id.chFragment1);
       ch2Fragment= (ChannelFragment) fragmentManager.findFragmentById(R.id.chFragment2);
       ch1Fragment.setChannelRef(1);
       //ch2Fragment.setChannelRef(2);


        this.mHandler = new Handler(){
            //private StringBuilder stbb = new StringBuilder();
            @Override
            public void handleMessage(Message msg) {
                int cmptErreur=0;
                switch(msg.what){
                    case MESSAGE_TOAST:
                    //Toast.makeText(getApplicationContext(), msg.getData().getString(),Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_READ:
                        int data_length, x;
                        byte[] readBuf = (byte[]) msg.obj;
                        byte[] ff=mFrameProcessor.fromFrame(readBuf);
                        byte[] param = new byte[6];
                        byte[] donnees = new byte[500];
                        for(int k=0;k<donnees.length+6;k++){

                            if(k<6){
                                param[k]=ff[k];
                            }else{
                               donnees[k-6]=ff[k];
                            }

                        }


                        //data_length = msg.arg1;

                        StringBuilder stb = new StringBuilder();
                        if (stb.length() > donnees.length) {
                            stb.setLength(0);
                        }
                        for (byte a : donnees) {
                            stb.append(String.format("%02X ", a));
                        }

                        Log.i("DONNEES",stb.toString());

                        short[] resultConcat = byteRaw(donnees);

                        if (stb.length() > resultConcat.length) {
                            stb.setLength(0);
                        }
                        for (short a : resultConcat) {
                            stb.append(String.format("%02X ", a));
                        }

                        Log.i("resultcoNCAT",stb.toString());

                        data_length=resultConcat.length; //on prend la longueur car fromFrame enleve des caractères de readBuf
                        for(x=0; x<data_length; x++){
                            try {
                                int raw = UShort(resultConcat[x]); //valeurs des tensions en fonction du temps

                                Log.i("URAW",String.valueOf(raw));

                                if (raw > MAX_LEVEL) {
                                    if (raw == DATA_START) {
                                        bDataAvailable = true;
                                        dataIndex = 0;
                                        dataIndex1 = 0;
                                        dataIndex2 = 0;
                                    } else if ((raw == DATA_END) || (dataIndex >= MAX_SAMPLES)) {
                                        bDataAvailable = false;
                                        dataIndex = 0;
                                        dataIndex1 = 0;
                                        dataIndex2 = 0;
                                        mOGView.set_data(ch1_data, ch2_data);
                                    /*if(bReady){ // send "REQ_DATA" again
                                        MainActivity.this.sendMessage( new String(new byte[] {REQ_DATA}) );
                                    }*/
                                        break;
                                    }
                                } else if ((bDataAvailable) && (dataIndex < (MAX_SAMPLES))) { // valid data
                                    if ((dataIndex++) % 2 == 0)
                                        ch1_data[dataIndex1++] = raw;    // even data
                                    else ch2_data[dataIndex2++] = raw;    // odd data
                                }
                            }catch (ArrayIndexOutOfBoundsException e){
                                System.out.println("Erreur à l'index : "+String.valueOf(x));
                                cmptErreur++;
                                System.out.println("Nombre d'erreurs : "+String.valueOf(cmptErreur));
                                e.printStackTrace();
                            }
                        }
                        break;
                }

            }
            private  int UShort(short b){
                if(b<0) // if negative
                    return (int)( b & 0xFFFF);
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

    public static short[] byteRaw(byte[] entree){
        byte bFort;
        byte bFaible;
        short[] result = new short[entree.length/2];
        int k=0;
        for(int i=0;i<entree.length;i+=2){
            bFort=entree[i];
            bFaible=entree[i+1];
            result[k]=concatBytes(bFort,bFaible);
            k++;
        }
        return result;
    }
    public static short concatBytes(byte b1,byte b2){
        return (short)((b1 <<8) |(b2 & 0xFF));
    }

    public static void main(String[] args) {
        // Test
        StringBuilder str =new StringBuilder();
        byte[] b = {0x05, 0x00, 0x02, 0x07, 0x06, 0x0C,(byte)0xF1,0x04};
      short[] result=  MainActivity.byteRaw(b);
        if (str.length() > result.length) {
            str.setLength(0);
        }
        for (short a : result) {
            str.append(String.format("%02X ", a));
        }

        System.out.print(str);
    }
}