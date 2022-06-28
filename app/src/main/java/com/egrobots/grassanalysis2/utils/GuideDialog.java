package com.egrobots.grassanalysis2.utils;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.egrobots.grassanalysis2.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GuideDialog extends AppCompatDialogFragment {

    private GuidelineCallback guidelineCallback;

    public GuideDialog(GuidelineCallback guidelineCallback) {
        this.guidelineCallback = guidelineCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.guide_dialog);
        dialog.setCanceledOnTouchOutside(false);
        ButterKnife.bind(this, dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);

        return dialog;
    }

    @OnClick(R.id.start_now_button)
    public void onStartNowButtonClicked() {
        guidelineCallback.onStartNowClicked();
        dismiss();
    }

    public interface GuidelineCallback {
        void onStartNowClicked();
    }
}
