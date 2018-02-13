package com.segway.loomo.depthimagedemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.frame.Frame;
import com.segway.robot.sdk.vision.stream.StreamType;

import java.nio.ByteBuffer;

/**
 * @author jacob
 */
public class MainActivity extends AppCompatActivity {

    private static final int SHOW_IMAGE = 0;

    private ImageView mDepthImage;
    private Vision mVision;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {


            mDepthImage.setImageBitmap(convertBmp(depthToGrey(mDepthBitmap)));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDepthImage = (ImageView) findViewById(R.id.depthImage);


        mVision = Vision.getInstance();
        mVision.bindService(this, mBindStateListener);

    }

    private ServiceBinder.BindStateListener mBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            mVision.startListenFrame(StreamType.DEPTH,mFrameListener);
        }

        @Override
        public void onUnbind(String reason) {

        }


    };


    private Bitmap mDepthBitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.RGB_565);
    private Vision.FrameListener mFrameListener = new Vision.FrameListener() {
        @Override
        public void onNewFrame(int streamType, Frame frame) {

            ByteBuffer in = frame.getByteBuffer();
            ByteBuffer out;
            int bytes = in.capacity();
            out = ByteBuffer.allocate(bytes);

            short [] segments = new short[4];

            for (int i = 0; i < bytes /8; i++) {
                long l = in.getLong();

                long l1 = l & 0x00ff00ff00ff00ffL;
                l1 = l1 << 8;
                long l2 = l & 0xff00ff00ff00ff00L;
                l2 = l2 >> 8 & 0x00ffffffffffffffL;
                l = l1 | l2;

                segments[3] = (short) (0x000000000000ffff & l);
                segments[2] = (short) (l >> 16 & 0x000000000000ffff);
                segments[1] = (short) (l >> 16 & 0x000000000000ffff);
                segments[0] = (short) (l >> 16 & 0x000000000000ffff);

                for(int j= 0; j<segments.length;j++){
                    if (segments[j] < 0) {
                        segments[j] = 0;
                    } else if (segments[j] > 500 && segments[j] < 1000) {
                        Log.d("jacob", "onNewFrame() called with: segment[j] = [" + segments[j] + "]");
                        //inside the interval
                        segments[j] = (short) ((segments[j] - 750) / 4);

                    } else {
                        segments[j] = 0;
                    }
                    out.putShort(segments[j]);
                }
            }
            out.rewind();

            mDepthBitmap.copyPixelsFromBuffer(out);

            mHandler.sendEmptyMessage(SHOW_IMAGE);
        }
    };


    private Bitmap convertBmp(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
        return convertBmp;
    }

    private Bitmap depthToGrey(Bitmap img) {
        int width, height;
        height = img.getHeight();
        width = img.getWidth();

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmp);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(img, 0, 0, paint);
        return bmp;
    }



}
