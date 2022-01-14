package com.recording.trans.view.views

import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.Resource
import com.recording.trans.utils.AppUtil
import kotlinx.android.synthetic.main.item_anchor_select.view.*

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/10/26 10:39
 */
class ChangeAnchorDialog(activity: Activity, var value: Int, handler: Handler) : Dialog(activity, R.style.app_dialog) {
    private val mContext = activity
    private val mHandler = handler

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataAdapter<Resource>
    private lateinit var cancel: TextView
    private lateinit var finish: TextView

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.p_anchor_change_setting, null)
        setContentView(view)
        setCancelable(true)

        finish = view.findViewById(R.id.finish)
        cancel = view.findViewById(R.id.cancel)
        recyclerView = view.findViewById(R.id.recy_from)

        val list = arrayListOf<Resource>()
        list.add(Resource("传统", 0, "普通女声"))
        list.add(Resource("传统", 1, "普通男声"))
        list.add(Resource("情感男声", 3, "情感男声<度逍遥>"))
        list.add(Resource("情感儿童声", 4, "情感儿童声<度丫丫>"))
        list.add(Resource("情感女声", 5, "度小娇"))
        list.add(Resource("情感女声", 103, "度米朵"))
        list.add(Resource("情感男声", 106, "度博文"))
        list.add(Resource("情感儿童声", 110, "度小童"))
        list.add(Resource("情感女声", 111, "度小萌"))

        adapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_anchor_select)
            .addBindView { itemView, itemData ->
                itemView.tv_item_nickname.text = itemData.name
                itemView.tv_item_wxId.text = itemData.type

                if (value == itemData.icon) {
                    itemView.choose.visibility = View.VISIBLE
                } else {
                    itemView.choose.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    itemView.choose.visibility = View.VISIBLE
                    value = itemData.icon
                    adapter.notifyItemRangeChanged(0, list.size)
                }
            }
            .create()

        recyclerView.layoutManager = LinearLayoutManager(mContext)
        recyclerView.adapter = adapter
        adapter.notifyItemRangeChanged(0, list.size)

        finish.setOnClickListener { finish() }
        cancel.setOnClickListener { dismiss() }

    }

    override fun show() {
        val h = AppUtil.getScreenHeight(mContext)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER

            width = WindowManager.LayoutParams.MATCH_PARENT
            height = h / 2
        }
        super.show()
    }

    private fun finish() {
        val message = Message()
        message.what = 0x2
        message.arg1 = value
        mHandler.sendMessage(message)
        dismiss()
    }
}