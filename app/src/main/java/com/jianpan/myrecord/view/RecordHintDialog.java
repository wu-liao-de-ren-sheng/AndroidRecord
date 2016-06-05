package com.jianpan.myrecord.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jianpan.myrecord.R;

/**
 * Created by 键盘 on 2016/5/23.
 */
public class RecordHintDialog extends Dialog{

    /** 录音提示图片 */
    private ImageView micImage;
    /** 录音提示文本 */
    private TextView recordingHint;

    public RecordHintDialog(Context context) {
        this(context, 0);
    }

    public RecordHintDialog(Context context, int themeResId) {
        super(context, themeResId);

        init();
    }

    private void init(){
        LinearLayout dialogView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.record_dialog, null, false);
        setContentView(dialogView);

        micImage = (ImageView) dialogView.findViewById(R.id.record_dialog_img);
        recordingHint = (TextView) dialogView.findViewById(R.id.record_dialog_txt);

        micImage.setImageResource(R.mipmap.record_animate_01);
        recordingHint.setTextSize(14);

    }

    public void moveUpToCancel(){
        recordingHint.setText(getContext().getResources().getString(R.string.move_up_to_cancel));
        recordingHint.setBackgroundColor(Color.TRANSPARENT);
    }

    public void releaseToCancel(){
        recordingHint.setText(getContext().getResources().getString(R.string.release_to_cancel));
        recordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg);
    }

    public void setImage(Drawable drawable){
        micImage.setImageDrawable(drawable);
    }

    @Override
    public void dismiss() {
        if (isShowing()){
            super.dismiss();
        }
    }
}
