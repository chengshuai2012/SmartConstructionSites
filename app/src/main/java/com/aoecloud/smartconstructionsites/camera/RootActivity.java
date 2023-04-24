package com.aoecloud.smartconstructionsites.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aoecloud.smartconstructionsites.base.BaseActivity;
import com.aoecloud.smartconstructionsites.utils.TaskManager;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("Registered")
public class RootActivity extends BaseActivity {

    public final static String TAG = RootActivity.class.getSimpleName();

    protected static Context mContext;

    private Toast mToast = null;

    private boolean mIsTip = true;
    private static TaskManager mTaskManager = null;
    protected synchronized static TaskManager getTaskManager(){
        if (mTaskManager == null){
            mTaskManager = new TaskManager();
        }
        return mTaskManager;
    }
    protected int pageKey = -1;
    protected void showToast(int id) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }
        String text = getString(id);
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(int id, int errCode) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        String text = getString(id);
        if (errCode != 0) {
            int errorId = getErrorId(errCode);
        if (errorId != 0) {
            text = getString(errorId);
        } else {
            text = text + " (" + errCode + ")";
        }
    }
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(int id, String msg) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        String text = getString(id);
        if (!TextUtils.isEmpty(msg)) {
            text = text + " (" + msg + ")";
        }
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsTip) {
                    return;
                }

                if (isFinishing()) {
                    return;
                }
                if (text != null && !text.equals("")) {
                    if (mToast == null) {
                        mToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
                        mToast.setGravity(Gravity.CENTER, 0, 0);
                    } else {
                        mToast.setText(text);
                    }
                    mToast.show();
                }
            }
        });
    }

    protected int getErrorId(int errorCode) {
        int errorId = this.getResources().getIdentifier("error_code_" + errorCode, "string", this.getPackageName());
        /*
         * Field fieldError; int errorId = 0; try { fieldError =
         * R.string.class.getDeclaredField("error_code_" + errorCode);
         * fieldError.setAccessible(true); R.string string = new R.string(); try { errorId =
         * fieldError.getInt(string); } catch (IllegalAccessException e) { // TODO Auto-generated
         * catch block e.printStackTrace(); } catch (IllegalArgumentException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); } } catch (NoSuchFieldException e) { //
         * TODO Auto-generated catch block e.printStackTrace(); }
         */

        return errorId;
    }

    protected void showToast(int res1, int res2, int errCode) {
        String text = res1 != 0 ? getString(res1) : "";
        if (res2 != 0) {
            text = text + ", " + getString(res2);
        }
        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                text = getString(errorId);
            } else {
                text = text + " (" + errCode + ")";
            }
        }
        if (text != null) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void setPageKey(int argPageKey) {
        this.pageKey = argPageKey;
    }


    /**
     * @return the isResumed
     */
    // public static boolean isResumed() {
    // return isResumed;
    // }

    // public static void setResumed(boolean isResumed) {
    // RootActivity.isResumed = isResumed;
    // }

    protected String getErrorTip(int id, int errCode) {
        StringBuffer errorTip = new StringBuffer();

        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                errorTip.append(getString(errorId));
            } else {
                errorTip.append(getString(id)).append(" (").append(errCode).append(")");
            }
        } else {
            errorTip.append(getString(id));
        }
        return errorTip.toString();
    }

    protected void hideInputMethod() {
        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    protected void removeHandler(Handler handler) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    private static List<Activity> mActivityList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mActivityList.add(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityList.remove(this);
    }

    protected void toast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected static void toastMsg(final String msg){
        if (mContext == null){
            return;
        }
        if (mContext instanceof RootActivity){
            final RootActivity rootActivity = (RootActivity) mContext;
            rootActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rootActivity.toast(msg);
                }
            });
        }
    }

    private static int mNotificationId = 1;
    protected static int getUniqueNotificationId(){
        return mNotificationId++;
    }

    protected void initUi(){}


    public static void exitApp(){
        for (Activity activity: mActivityList){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
        System.exit(0);
    }

    protected void initPresenter(){}

}
