package com.jianpan.myrecord;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.jianpan.myrecord.util.AuditRecorderConfiguration;
import com.jianpan.myrecord.util.ExtAudioRecorder;
import com.jianpan.myrecord.util.FailRecorder;
import com.jianpan.myrecord.view.RecordHintDialog;
import com.jianpan.myrecord.view.RippleDiffuse;


public class MainActivity extends AppCompatActivity {

    private RippleDiffuse rippleDiffuse;
    private RecordHintDialog recordHintDialog;

    // 获取类的实例
    ExtAudioRecorder recorder;
    //录音地址
    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "media.wav";
    //dialog显示的图片
    private Drawable[] micImages;

    private int touchSlop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        touchSlop = ViewConfiguration.get(MainActivity.this).getScaledTouchSlop();
        micImages = getRecordAnimPic(getResources());
        recordHintDialog = new RecordHintDialog(this, R.style.DialogStyle);

        AuditRecorderConfiguration configuration = new AuditRecorderConfiguration.Builder()
                .recorderListener(listener)
                .handler(handler)
                .uncompressed(true)
                .builder();

        recorder = new ExtAudioRecorder(configuration);

        initViews();
    }
    /** 设置Dialog的图片 */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            recordHintDialog.setImage(micImages[msg.what]);
        }
    };
    /** 录音失败的提示 */
    ExtAudioRecorder.RecorderListener listener = new ExtAudioRecorder.RecorderListener() {
        @Override
        public void recordFailed(FailRecorder failRecorder) {
            if (failRecorder.getType() == FailRecorder.FailType.NO_PERMISSION) {
                Toast.makeText(MainActivity.this, "录音失败，可能是没有给权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "发生了未知错误", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void initViews() {
        rippleDiffuse = (RippleDiffuse) findViewById(R.id.rd);

        rippleDiffuse.setBtnOnTouchListener(new View.OnTouchListener() {
            float downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = event.getY();
                        //检查sdcard
                        if (!isExitsSdcard()) {
                            String needSd = getResources().getString(R.string.Send_voice_need_sdcard_support);
                            Toast.makeText(MainActivity.this, needSd, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        // 设置输出文件
                        recorder.setOutputFile(filePath);
                        recorder.prepare();
                        recorder.start();
                        //弹出dialog
                        if (recorder.getState() != ExtAudioRecorder.State.ERROR) {
                            recordHintDialog.show();
                            recordHintDialog.moveUpToCancel();
                            return true;
                        }
                        return false;

                    case MotionEvent.ACTION_MOVE: {
                        if (recorder.getState() != ExtAudioRecorder.State.RECORDING) {
                            return false;
                        }
                        float offsetY = downY - event.getY();
                        if (offsetY > touchSlop) {
                            recordHintDialog.releaseToCancel();
                        } else {
                            recordHintDialog.moveUpToCancel();
                        }
                        return true;
                    }

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        recordHintDialog.dismiss();
                        if (recorder.getState() != ExtAudioRecorder.State.RECORDING) {
                            return false;
                        }
                        float offsetY = downY - event.getY();
                        if (offsetY > touchSlop) {
                            //删除录音
                            recorder.discardRecording();
                        } else {
                            //录音成功
                            int time = recorder.stop();
                            if (time > 0) {
                                //成功的处理
                            } else {
                                String st2 = getResources().getString(R.string.The_recording_time_is_too_short);
                                Toast.makeText(MainActivity.this, st2, Toast.LENGTH_SHORT).show();
                            }
                        }
                        recorder.reset();
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Sdcard是否存在
     */
    public static boolean isExitsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取录音时动画效果的图片
     */
    public static Drawable[] getRecordAnimPic(Resources res) {
        return new Drawable[]{res.getDrawable(R.mipmap.record_animate_01),
                res.getDrawable(R.mipmap.record_animate_02), res.getDrawable(R.mipmap.record_animate_03),
                res.getDrawable(R.mipmap.record_animate_04), res.getDrawable(R.mipmap.record_animate_05),
                res.getDrawable(R.mipmap.record_animate_06), res.getDrawable(R.mipmap.record_animate_07),
                res.getDrawable(R.mipmap.record_animate_08), res.getDrawable(R.mipmap.record_animate_09),
                res.getDrawable(R.mipmap.record_animate_10), res.getDrawable(R.mipmap.record_animate_11),
                res.getDrawable(R.mipmap.record_animate_12), res.getDrawable(R.mipmap.record_animate_13),
                res.getDrawable(R.mipmap.record_animate_14)};
    }

}
