package com.recording.trans.view.views

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.Resource
import com.recording.trans.callback.FuncCallback
import com.recording.trans.utils.AppUtil
import kotlinx.android.synthetic.main.item_heart_small.view.*


class ExtraFuncDialog(context: Context, callback: FuncCallback) : Dialog(context, R.style.app_dialog) {
    private val mContext: Context = context
    private val mCallback = callback
    private lateinit var recyclerView: RecyclerView

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(mContext).inflate(R.layout.d_extra_func, null)
        setContentView(dialogContent)
        setCancelable(true)

        recyclerView = dialogContent.findViewById(R.id.ry_func)
        val list = mutableListOf<Resource>()
        list.add(Resource("file_to_text", R.drawable.ic_fosd_to_text, "转文字"))
        list.add(Resource("rename", R.drawable.ic_fosd_rename, "重命名"))
        list.add(Resource("delete", R.drawable.ic_fosd_delete, "删除"))
        list.add(Resource("from", R.drawable.ic_fosd_share, "文件位置"))

        val adapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_heart_small)
            .addBindView { itemView, itemData ->
                itemView.iv_icon.setImageResource(itemData.icon)
                itemView.tv_name.text = itemData.name

                itemView.setOnClickListener {
                    mCallback.onSuccess(itemData.type)
                    cancel()
                }
            }
            .create()

        recyclerView.layoutManager = GridLayoutManager(mContext, 2)
        recyclerView.adapter = adapter
        adapter.notifyItemRangeInserted(0, list.size)

    }

    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 8 / 12
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }

    override fun cancel() {
        super.cancel()
        mCallback.onCancel()
    }


}