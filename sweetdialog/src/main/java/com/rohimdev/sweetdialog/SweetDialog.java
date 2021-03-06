package com.rohimdev.sweetdialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;


import java.util.Objects;

/**
 * @author TutorialsAndroid , tkdco
 */
public class SweetDialog extends AlertDialog implements View.OnClickListener {

    private final AnimationSet mModalInAnim, mModalOutAnim, mErrorXInAnim;//,mSuccessLayoutAnimSet;
    private final Animation mOverlayOutAnim, mImageAnim;//,mSuccessBowAnim;

    private TextView mTitleTextView, mContentTextView;
    private LinearLayout llCloseDialog;
    private ImageView mErrorX, mSuccessTick, mCustomImage, mShowCloseDialog;
    private LinearLayout llParent;

    private Drawable mCustomImgDrawable;
    private Button mConfirmButton, mCancelButton;
    public Drawable mColor, mCancelColor;
    private View mDialogView;//,mSuccessLeftMask,mSuccessRightMask;
    private View mCustomView;
    private FrameLayout mCustomViewContainer;

    private String mTitleText, mContentText, mCancelText, mConfirmText;

    private boolean mShowCancel, mShowContent, mShowTitleText, mCloseFromCancel, mShowConfirm, mShowCloseText, mShowCloseButtonCLose;
    private int contentTextSize = 0;

    private FrameLayout mErrorFrame, mSuccessFrame, mProgressFrame, mWarningFrame;
    //private SuccessTickView mSuccessTick;

    private final ProgressHelper mProgressHelper;
    public SweetDialog.SweetClickListener mCancelClickListener;
    public SweetDialog.SweetClickListener mConfirmClickListener;

    private int mAlertType;
    public static final int NORMAL_TYPE = 0;
    protected boolean mClose = false;
    // true = close muncul
    // false = close tidak muncul (default)

    public static final int ERROR_TYPE = 1;
    public static final int SUCCESS_TYPE = 2;
    public static final int WARNING_TYPE = 3;
    public static final int CUSTOM_IMAGE_TYPE = 4;
    public static final int PROGRESS_TYPE = 5;

    public static boolean DARK_STYLE = false;

    public interface SweetClickListener {
        void onClick(SweetDialog sweetDialog);
    }

    public static final int INPUT_TYPE = 6;
    private EditText mEditText;

    public SweetDialog(Context context) {
        this(context, NORMAL_TYPE);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);

        mDialogView = Objects.requireNonNull(getWindow()).getDecorView().findViewById(android.R.id.content);
        llParent = findViewById(R.id.ll_parent);
        mTitleTextView = findViewById(R.id.title_text);
        mContentTextView = findViewById(R.id.content_text);
        mShowCloseDialog = findViewById(R.id.iv_close);
        llCloseDialog = findViewById(R.id.ll_close_dialog);
        mErrorFrame = findViewById(R.id.error_frame);
        mErrorX = mErrorFrame.findViewById(R.id.error_x);
        mEditText = findViewById(R.id.edit_text);
        mSuccessFrame = findViewById(R.id.success_frame);
        mProgressFrame = findViewById(R.id.progress_dialog);
        mSuccessTick = mSuccessFrame.findViewById(R.id.success_x);
        //mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        //mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
        mCustomImage = findViewById(R.id.custom_image);
        mWarningFrame = findViewById(R.id.warning_frame);
        mCustomViewContainer = findViewById(R.id.custom_view_container);
        mProgressHelper.setProgressWheel(findViewById(R.id.progressWheel));

        mConfirmButton = findViewById(R.id.custom_confirm_button);
        mCancelButton = findViewById(R.id.cancel_button);
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        changeAlertType(mAlertType, true);
        setCustomView(mCustomView);
        setTitleText(mTitleText);
        setCloseDialog(mShowCloseButtonCLose);
        setContentText(mContentText);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        setConfirmBackground(mColor);
        setCancelBackground(mCancelColor);
//        rlParent.setBackgroundColor(Color.TRANSPARENT);
    }

    public SweetDialog(Context context, int alertType) {
        super(context, DARK_STYLE ? R.style.alert_dialog_dark : R.style.alert_dialog_light);

        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mProgressHelper = new ProgressHelper(context);
        mAlertType = alertType;
        mImageAnim = AnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
        mErrorXInAnim = (AnimationSet) AnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);
        //mSuccessBowAnim = AnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
        //mSuccessLayoutAnimSet = (AnimationSet) AnimationLoader.loadAnimation(getContext(), R.anim.success_mask_layout);
        mModalInAnim = (AnimationSet) AnimationLoader.loadAnimation(getContext(), R.anim.modal_in);
        mModalOutAnim = (AnimationSet) AnimationLoader.loadAnimation(getContext(), R.anim.modal_out);
        Objects.requireNonNull(mModalOutAnim).setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDialogView.setVisibility(View.GONE);
                mDialogView.post(() -> {
                    if (mCloseFromCancel) {
                        SweetDialog.super.cancel();
                    } else {
                        SweetDialog.super.dismiss();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mOverlayOutAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                WindowManager.LayoutParams wlp = Objects.requireNonNull(getWindow()).getAttributes();
                wlp.alpha = 1 - interpolatedTime;
                getWindow().setAttributes(wlp);
            }
        };
        mOverlayOutAnim.setDuration(120);

        Objects.requireNonNull(this.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void restore() {
        mCustomImage.setVisibility(View.GONE);
        mErrorFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mWarningFrame.setVisibility(View.GONE);
        mProgressFrame.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);

        mConfirmButton.setBackgroundResource(R.drawable.button_blue_background);
        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        //mSuccessLeftMask.clearAnimation();
        //mSuccessRightMask.clearAnimation();
    }

    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {
            mErrorFrame.startAnimation(mImageAnim);
            mErrorX.startAnimation(mErrorXInAnim);
        } else if (mAlertType == SUCCESS_TYPE) {
            mSuccessTick.startAnimation(mImageAnim);
            mSuccessFrame.startAnimation(mImageAnim);
        }
    }

    private void changeAlertType(int alertType, boolean fromCreate) {
        mAlertType = alertType;
        if (mDialogView != null) {
            if (!fromCreate) {
                restore();
            }
            switch (mAlertType) {
                case ERROR_TYPE:
                    mErrorFrame.setVisibility(View.VISIBLE);
                    setConfirmBackground(mColor);
                    break;
                case SUCCESS_TYPE:
                    mSuccessFrame.setVisibility(View.VISIBLE);
                    //mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                    //mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                    setConfirmBackground(mColor);
                    break;
                case WARNING_TYPE:
                    mWarningFrame.setVisibility(View.VISIBLE);
                    setConfirmBackground(mColor);
                    break;
                case CUSTOM_IMAGE_TYPE:
                    setCustomImage(mCustomImgDrawable);
                    setConfirmBackground(mColor);
                    break;
                case PROGRESS_TYPE:
                    mProgressFrame.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.GONE);
                    setConfirmBackground(mColor);
                    llParent.setBackgroundColor(Color.TRANSPARENT);
                    break;
                case INPUT_TYPE:
                    mEditText.requestFocus();
                    //mEditTextFrame.setVisibility(View.VISIBLE);
                    setConfirmBackground(mColor);
                    break;
            }
            if (!fromCreate) {
                playAnimation();
            }
        }
    }

    public void changeAlertType(int alertType) {
        changeAlertType(alertType, false);
    }

    public SweetDialog setTitleText(String text) {

        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            showTitleText();
            //mTitleTextView.setText(mTitleText);
            mTitleTextView.setText(Html.fromHtml(mTitleText));
        }
        return this;
    }

    private void showTitleText() {
        mShowTitleText = true;
        if (mTitleTextView != null) {
            mTitleTextView.setVisibility(View.VISIBLE);
        }
    }

    public SweetDialog setCloseDialog(Boolean flag) {
        mShowCloseButtonCLose = flag;
        if (llCloseDialog != null && mShowCloseDialog != null && flag) {
            showCloseDialog();
        }
        return this;
    }

    private void showCloseDialog() {
        mShowCloseText = true;
        if (mShowCloseDialog != null) {
            llCloseDialog.setVisibility(View.VISIBLE);
            mShowCloseDialog.setVisibility(View.VISIBLE);
            mShowCloseDialog.setOnClickListener(v->{
                SweetDialog.super.dismiss();
            });
        }
    }

    public SweetDialog setCustomImage(int resourceId) {
        return setCustomImage(getContext().getResources().getDrawable(resourceId));
    }

    private SweetDialog setCustomImage(Drawable drawable) {
        mCustomImgDrawable = drawable;
        if (mCustomImage != null && mCustomImgDrawable != null) {
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageDrawable(mCustomImgDrawable);
        }
        return this;
    }

    public SweetDialog setContentText(String text) {
 /*
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText();
            //mContentTextView.setText(mContentText);
            mContentTextView.setText(Html.fromHtml(mContentText));
        }
        return this;
*/
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText();
            if (contentTextSize != 0) {
                mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, spToPx(contentTextSize, getContext()));
            }
            mContentTextView.setText(Html.fromHtml(mContentText));
            mCustomViewContainer.setVisibility(View.GONE);
        }
        return this;
    }

    public SweetDialog showCancelButton (boolean isShow){

        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public SweetDialog showConfirmButton (boolean isShow){

        mShowConfirm = isShow;
        if (mConfirmButton != null) {
            mConfirmButton.setVisibility(mShowConfirm ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    private void showContentText () {
        mShowContent = true;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(View.VISIBLE);
        }
    }

    public SweetDialog setCancelClickListener (SweetClickListener listener){
        mCancelClickListener = listener;
        return this;
    }

    public SweetDialog setConfirmClickListener (SweetClickListener listener){
        mConfirmClickListener = listener;
        return this;
    }

    protected void onStart () {
        mDialogView.startAnimation(mModalInAnim);
        playAnimation();
    }

    @Override
    public void cancel () {
        dismissWithAnimation(true);
    }

    public SweetDialog setConfirmBackground (Drawable background){
        mColor = background;
        if (mConfirmButton != null && mColor != null) {
            mConfirmButton.setBackground(mColor);
        }
        return this;
    }

    public SweetDialog setConfirmColor(String warna){
        if (mConfirmButton != null && mColor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mConfirmButton.setTextColor(Color.parseColor(warna));
            }
        }
        return this;
    }

    public SweetDialog setCancelBackground (Drawable background){
        mCancelColor = background;
        if (mCancelButton != null && mCancelColor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mCancelButton.setBackground(mCancelColor);
            }
        }
        return this;
    }

    public SweetDialog setCancelColor(String color){
        if (mCancelButton != null && mCancelColor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mCancelButton.setTextColor(Color.parseColor(color));
            }
        }
        return this;
    }

    public void dismissWithAnimation () {
        dismissWithAnimation(false);
    }

    public void dismissWithAnimation ( boolean fromCancel){
        mCloseFromCancel = fromCancel;
        mConfirmButton.startAnimation(mOverlayOutAnim);
        mDialogView.startAnimation(mModalOutAnim);
    }

    public static int spToPx ( float sp, Context context){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public int getAlertType () {
        return mAlertType;
    }

    public String getTitleText () {
        return mTitleText;
    }

    public boolean isShowTitleText () {
        return mShowTitleText;
    }

    public String getContentText () {
        return mContentText;
    }

    public String getInputText () {
        return mEditText.getText().toString();
    }

    public void setInputText (String text){
        mEditText.setText(text);
    }

    public boolean isShowCancelButton () {
        return mShowCancel;
    }

    public boolean isShowConfirmButton () {
        return mShowConfirm;
    }

    public boolean isShowContentText () {
        return mShowContent;
    }

    public String getCancelText () {
        return mCancelText;
    }

    public SweetDialog setCancelText (String text){
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public String getConfirmText () {
        return mConfirmText;
    }

    public SweetDialog setConfirmText (String text){
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            showConfirmButton(true);
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    public SweetDialog setConfirmTextSuccess(String text){
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            showConfirmButton(true);
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    public SweetDialog setContentTextSize (int value){
        this.contentTextSize = value;
        return this;
    }

    public int getContentTextSize () {
        return contentTextSize;
    }

    public SweetDialog setCustomView (View view){
        mCustomView = view;
        if (mCustomView != null && mCustomViewContainer != null) {
            mCustomViewContainer.addView(view);
            mCustomViewContainer.setVisibility(View.VISIBLE);
            //mContentTextView.setVisibility(View.GONE);
        }
        return this;
    }

    @Override
    public void onClick (View v){
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(SweetDialog.this);
            } else {
                dismissWithAnimation();
            }
        } else if (v.getId() == R.id.custom_confirm_button) {
            if (mConfirmClickListener != null) {
                mConfirmClickListener.onClick(SweetDialog.this);
            } else {
                dismissWithAnimation();
            }
        }
    }

    public ProgressHelper getProgressHelper () {
            return mProgressHelper;
        }
}