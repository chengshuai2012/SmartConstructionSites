package com.aoecloud.smartconstructionsites.camera

import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import com.aoecloud.smartconstructionsites.R
import java.util.*


object CustomDialogManager {

    fun showHowToUpTVDialog(context: Context, date: Date, callBack: (String) -> Unit) {
        val howToUpTVDialog = ChooseDayDialog.getInstance(context,date)
        howToUpTVDialog.onDialogClickListener = {
            callBack.invoke(it)
        }
        howToUpTVDialog.show()
        val window = howToUpTVDialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams
        howToUpTVDialog.window?.setGravity(Gravity.BOTTOM)
        howToUpTVDialog.window?.setWindowAnimations(R.style.BottomDialog_Animation)
    }

}