package com.example.tpeea.projeteb03;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
//import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

/*public class OscilloGraphView extends SurfaceView implements SurfaceHolder.Callback{

    private class DessinThread extends Thread {
        

    }

    private DessinThread dessin_thread;

    private final int width = 320;
    private final int heigth = 240;

    private static int[] ch1_data = new int[320];
    private static int[] ch2_data = new int[320];
    private static int ch1_pos = 120, ch2_pos = 120;

    private Paint ch1_color = new Paint();
    private Paint ch2_color = new Paint();
    private Paint grid_paint = new Paint();
    private Paint cross_paint = new Paint();
    private Paint outline_paint = new Paint();

    public OscilloGraphView(Context context, AttributeSet attrs) {

        super(context, attrs);
        //super(context);
        getHolder().addCallback(this);

        int i;
        for(i=0; i<width; i++){
            ch1_data[i] = ch1_pos;
            ch2_data[i] = ch2_pos;
        }

       // plot_thread = new WaveformPlotThread(getHolder(), this);
        //setFocusable(true);
        ch1_color.setColor(Color.YELLOW);
        ch2_color.setColor(Color.RED);
        grid_paint.setColor(Color.rgb(100, 100, 100));
        cross_paint.setColor(Color.rgb(70, 100, 70));
        outline_paint.setColor(Color.GREEN);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

    }


   @Override
    public void surfaceCreated(SurfaceHolder holder) {
        plot_thread.setRunning(true);
        plot_thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        plot_thread.setRunning(false);
        while (retry){
            try{
                plot_thread.join();
                retry = false;
            }catch(InterruptedException e){

            }
        }

    }



}
*/