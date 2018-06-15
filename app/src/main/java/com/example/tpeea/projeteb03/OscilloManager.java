package com.example.tpeea.projeteb03;

import android.util.Log;

/**
 * Created by Steph on 05/05/2018.
 */

public class OscilloManager implements Transceiver.TransceiverDataListener, Transceiver.TransceiverEventListener{
    private static OscilloManager instance =null;

    private OscilloManager(){

    }

    //Singleton
    public static OscilloManager getOscilloManager(){
        if(instance == null){
            instance = new OscilloManager();
        }
        return instance;
    }

    public byte[] setVerticalScale(int channel, int index){
        byte[] commande = {2,0,0};
        if(channel==1){
            commande[1]=0x01;
        }
       if(index<=16 && index >=0){
            commande[2]=(byte)index;
       }
       return commande;
    }

    public byte[] setVerticalOffset(int channel,int value){
        // Value est sur deux octets doc doit être compris entre
        byte[] commande = {3,0,0};

        if(channel==1){
            commande[1]=0x01;
        }
        String hex = hexString(value);
        byte[] valueBytes = getLsbMsb(hex);
        commande[1]=valueBytes[1];
        commande[2]=valueBytes[0];
       return commande;
    }


    public byte[] setHorizontalScale(int index){
        byte[] commande = {7,0};
        if(index<=16 && index >=0){
            commande[1]=(byte)index;
        }

        return commande;
    }
    public byte[] setChannel(int channel ,boolean state){
        byte[] commande = {0x0B,0,0};
        if(channel==1){
            commande[1]=0x01;
        }else if (channel==2){
            commande[1]=0x02;
        }
        if(state){
            commande[2]=0x01;
            Log.i("OSCILLOMANAGER","channel 1 ouvert");
        }else if(!state){
            commande[2]=0x00;
            Log.i("OSCILLOMANAGER","channel 1 ferme");
        }
        return commande;
    }

    public byte[] setCalibrationDutyCycle(float dutyCycle){
        byte[] commande = {0x0A,0};
        if(dutyCycle>=0 && dutyCycle<=1){
            commande[1] = (byte)(dutyCycle*100);
        }else{

            commande[1]=(byte)(dutyCycle);
        }
        return commande;
    }

    public byte[] setTriggerChannel(int channel){
        byte[] commande = {0x08,(byte)channel};
        return commande;
    }

    public byte[] setTrigggerLevel(int type){
        byte[] commande = {0x0C,(byte)type};
        return commande;
    }

    public String hexString(int i){
        String hex = Integer.toHexString(i);
        StringBuilder result = new StringBuilder("0000");
        if(hex.length()<4){
            for(int k=0;k<hex.length()-1;k++){
                result.setCharAt(3-k,hex.charAt(0));
            }
        }else{
            result = new StringBuilder(hex);
        }
        return result.toString();
    }

    public byte[] getLsbMsb(String hex){
        byte[] result = {0,0};
        if(hex.length()<=4){
            result[0]=(byte)(Integer.parseInt(Character.toString(hex.charAt(0)),16)+Integer.parseInt(Character.toString(hex.charAt(1)),16)*16);
           // result[0] = (byte) unsignedByteToInt(result[0]);
            result[1]=(byte)(Integer.parseInt(Character.toString(hex.charAt(2)),16)+Integer.parseInt(Character.toString(hex.charAt(3)),16)*16);
            //result[1] = (byte) unsignedByteToInt(result[1]);
        }
        return result;
    }
    public int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }


    interface OscilloEventListener{

    }
    public static void main (String[] args){
        // Test
        int a = 0xFA51;
        OscilloManager om = OscilloManager.getOscilloManager();
        String hex =om.hexString(a);
        System.out.println(hex);
        byte[] result =om.getLsbMsb(hex);
        System.out.print(result);
    }
}
