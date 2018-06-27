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



public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private OscilloManager mOscilloManager;
    private FrameProcessor mFrameProcessor;

    public BluetoothManager getmBluetoothManager() {
        return mBluetoothManager;
    }

    public OscilloManager getmOscilloManager() {
        return mOscilloManager;
    }

    public FrameProcessor getmFrameProcessor() {
        return mFrameProcessor;
    }

    private Slider mSlider;
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


    private ChannelFragment ch1Fragment;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.mSlider = findViewById(R.id.mSlider);

       FragmentManager fragmentManager = getFragmentManager();
       ch1Fragment= (ChannelFragment) fragmentManager.findFragmentById(R.id.chFragment1);
       ch1Fragment.setChannelRef(1);


        this.mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case MESSAGE_TOAST:
                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        byte[] ff=mFrameProcessor.fromFrame(readBuf);
                        byte[] data = new byte[1024];
                        for(int i=6;i<ff.length;i++){
                            data[i-6]=ff[i];
                        }
                        float[] dataread = new float[byteRaw(data).length];
                        for(int i=0;i<dataread.length;i++){
                            dataread[i]=(float)byteRaw(data)[i];
                        }
                        mOGView.set_data(dataread);
                        break;
                }

            }

        };
        this.mBluetoothManager = new BluetoothManager(this, mHandler);
        this.mOscilloManager= OscilloManager.getOscilloManager();
        this.mFrameProcessor= new FrameProcessor();
        mTextViewString = findViewById(R.id.stringSlider);
        mOGView=findViewById(R.id.oscilloView);




        mSlider.setSliderListener(new Slider.SliderListener() {
            @Override
            public void onValueChanged(View view, float value) {
                if(view.getId()==R.id.mSlider){
                    //envoyer la commande
                        if(mBluetoothManager.getBluetoothState()==mBluetoothManager.STATE_CONNECTED){
                        byte[] trame=mFrameProcessor.toFrame(mOscilloManager.setCalibrationDutyCycle(value));
                        mBluetoothManager.write(trame);
                        Toast.makeText(MainActivity.this,"envoi de la trame",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onDoubleClick(View view,float value) {
                if(view.getId()==R.id.mSlider){

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

    /**Vérifie si l'utilisateur possède un adaptateur blueetooth et s'il peut l'utiliser. S'il en possède un  mais que l'application n'a pas l'autorisation de l'utiliser, une reqûete sera faite à l'utiisateur.
     * Renvoie des constantes définies plus haut en fonction des situations
     * **/
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

    /**Permet de vérifier si l' utilisateur possède les autorisations passées en paramètres sous la forme de String. Dés qu'une des autorisations n'est pas accordées, la méthode renvoie False. Renvoie True sinon*/
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

    /**
     * Cette méthode eprmet à partir d'un tableau de byte de grouper deux par deux chacun des bytes afin d'en faire des shorts. Elle renvoie le tableau de short obtenu.
     * **/
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

    /**
     * Permet de concaténer deux bytes afin d'obtenir un short
     * **/
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