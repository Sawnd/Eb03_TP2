package com.example.tpeea.projeteb03;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;


public class OscilloGraphView extends SurfaceView implements SurfaceHolder.Callback {

    private class DessinThread extends Thread {
        private SurfaceHolder holder;
        private OscilloGraphView plot_area;
        private boolean _run = false;

        public DessinThread(SurfaceHolder surfaceHolder, OscilloGraphView view) {
            holder = surfaceHolder;
            plot_area = view;
        }

        public void setRunning(boolean run) {
            _run = run;
        }

        @Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = holder.lockCanvas(null);
                    synchronized (holder) {
                        if (c != null) {
                            plot_area.PlotPoints(c);
                        }
                    }
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

    }

    private DessinThread dessinThread;

    private int width = 640;
    private int heigth = 480;

    private static float[] ch1_data = new float[640];
    private static int[] ch2_data = new int[640];
    private static int ch1_pos = 240, ch2_pos = 240;

    private Paint ch1_color = new Paint();
    private Paint ch2_color = new Paint();
    private Paint grid_paint = new Paint();
    private Paint cross_paint = new Paint();
    private Paint outline_paint = new Paint();

    public OscilloGraphView(Context context, AttributeSet attrs) {

        super(context, attrs);
        getHolder().addCallback(this);

        int i;
        for (i = 0; i < width; i++) {
            ch1_data[i] = ch1_pos;
            ch2_data[i] = ch2_pos;
        }

        dessinThread = new DessinThread(getHolder(), this);
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
        dessinThread.setRunning(true);
        dessinThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        dessinThread.setRunning(false);
        while (retry) {
            try {
                dessinThread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }

    }

    public void set_data(float[] data1) {

        dessinThread.setRunning(true);

        for (int x = 0; x < width; x++) {
            if (x < data1.length) {
                ch1_data[x] = heigth / 2 - data1[x];
            } else {
                ch1_data[x] = ch1_pos;
            }
        }

        dessinThread.setRunning(true);

    }

    public void PlotPoints(Canvas canvas) {

        canvas.drawColor(Color.rgb(20, 20, 20));

        for (int vertical = 1; vertical < 10; vertical++) {
            canvas.drawLine(
                    vertical * (width / 10) + 1, 1,
                    vertical * (width / 10) + 1, heigth + 1,
                    grid_paint);
        }
        for (int horizontal = 1; horizontal < 10; horizontal++) {
            canvas.drawLine(
                    1, horizontal * (heigth / 10) + 1,
                    width + 1, horizontal * (heigth / 10) + 1,
                    grid_paint);
        }

        // draw center cross
        canvas.drawLine(0, (heigth / 2) + 1, width + 1, (heigth / 2) + 1, cross_paint);
        canvas.drawLine((width / 2) + 1, 0, (width / 2) + 1, heigth + 1, cross_paint);

        // draw outline
        canvas.drawLine(0, 0, (width + 1), 0, outline_paint);    // top
        canvas.drawLine((width + 1), 0, (width + 1), (heigth + 1), outline_paint); //right
        canvas.drawLine(0, (heigth + 1), (width + 1), (heigth + 1), outline_paint); // bottom
        canvas.drawLine(0, 0, 0, (heigth + 1), outline_paint); //left

        // plot data
        for (int x = 0; x < (width - 1); x++) {
            canvas.drawLine(x + 1, ch2_data[x], x + 2, ch2_data[x + 1], ch2_color);
            canvas.drawLine(x + 1, ch1_data[x], x + 2, ch1_data[x + 1], ch1_color);
        }
    }


}
