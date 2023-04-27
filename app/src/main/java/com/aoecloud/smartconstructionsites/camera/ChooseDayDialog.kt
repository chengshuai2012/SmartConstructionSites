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
import com.othershe.calendarview.listener.OnPagerChangeListener
import com.videogo.util.DateTimeUtil
import java.util.*


class ChooseDayDialog private constructor(context: Context,val date: Date) :
    Dialog(context, R.style.CommonDialog) {
    var onDialogClickListener: (( String) -> Unit)? = null

    companion object {
        fun getInstance(context: Context, data:Date): ChooseDayDialog {
            return ChooseDayDialog(context,data)
        }
    }

    var title: TextView? = null
    var chooseDate: TextView? = null
    var next: ImageView? = null
    var last: ImageView? = null
    var calendarView:CalendarView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_day)
        title = findViewById(R.id.title)
        val formatDateToString = DateTimeUtil.formatDateToString(date, DateTimeUtil.DAY_FORMAT)
        val cDate = formatDateToString.split("-")
        chooseDate = findViewById(R.id.choose_date)
        calendarView = findViewById(R.id.calendar)
        next = findViewById(R.id.next)
        last = findViewById(R.id.last)
        next?.setOnClickListener {
            calendarView?.nextMonth()
        }
        last?.setOnClickListener {
            calendarView?.lastMonth()
        }
        calendarView?.setOnPagerChangeListener(OnPagerChangeListener { date -> title?.text = date[0].toString() + "年" + date[1] + "月" })
        title?.text = cDate[0].toString() + "年" + cDate[1] + "月"
        calendarView?.run{
            this.setStartEndDate("2016.1", "2028.12")
            .setDisableStartEndDate("2016.10.10", "2028.10.10")
            .setInitDate(cDate.get(0) + "." + cDate.get(1))
            .setSingleDate(cDate.get(0) + "." + cDate.get(1) + "." + cDate.get(2))
            .init()
        }
        calendarView?.setOnSingleChooseListener { view, date ->
            title?.text = date.solar[0].toString() + "年" + date.solar[1] + "月"
            onDialogClickListener?.invoke("${date.solar[0]}-${date.solar[1]}-${date.solar[2]}")
            dismiss()
        }

    }
}