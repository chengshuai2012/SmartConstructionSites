package com.aoecloud.smartconstructionsites.camera

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aoecloud.smartconstructionsites.R
import com.othershe.calendarview.utils.CalendarUtil
import com.othershe.calendarview.weiget.CalendarView


class ChooseDayDialog private constructor(context: Context) :
    Dialog(context, R.style.CommonDialog) {
    var onDialogClickListener: (( Int) -> Unit)? = null

    companion object {
        fun getInstance(context: Context): ChooseDayDialog {
            return ChooseDayDialog(context)
        }
    }

    var title: TextView? = null
    var chooseDate: TextView? = null
    var calendarView:CalendarView? = null
    private val cDate = CalendarUtil.getCurrentDate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_day)
        title = findViewById(R.id.title)

        chooseDate = findViewById(R.id.choose_date)
        calendarView = findViewById(R.id.calendar)
        title?.text = cDate[0].toString() + "年" + cDate[1] + "月"
        calendarView?.run{
            this.setStartEndDate("2016.1", "2028.12")
            .setDisableStartEndDate("2016.10.10", "2028.10.10")
            .setInitDate(cDate.get(0).toString() + "." + cDate.get(1))
            .setSingleDate(cDate.get(0).toString() + "." + cDate.get(1) + "." + cDate.get(2))
            .init()
        }

    }
    fun lastMonth(view: View?) {
        calendarView?.lastMonth()
    }

    fun nextMonth(view: View?) {
        calendarView?.nextMonth()
    }

}