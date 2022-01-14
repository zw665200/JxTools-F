package com.recording.trans.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.view.View
import android.widget.*
import com.recording.trans.R
import com.recording.trans.controller.Constant
import com.recording.trans.utils.AppUtil
import com.recording.trans.utils.JLog
import com.recording.trans.view.base.BaseFragment
import com.recording.trans.view.base.BaseFragmentActivity
import com.recording.trans.view.fragment.*
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class MainActivity : BaseFragmentActivity(), View.OnClickListener {

    companion object {
        private const val FRAGMENT_HOME = 0

        private const val DEFAULT_INDEX = FRAGMENT_HOME

        val BOTTOM_ICON_CHECKED = arrayOf(
            R.drawable.ic_main_home_select,
            R.drawable.ic_main_file_select,
            R.drawable.ic_main_box_select,
            R.drawable.ic_main_mine_select
        )

        val BOTTOM_ICON_UNCHECKED = arrayOf(
            R.drawable.ic_main_home_unselect,
            R.drawable.ic_main_file_unselect,
            R.drawable.ic_main_box_unselect,
            R.drawable.ic_main_mine_unselect
        )

        val BOTTOM_TEXT_ARRAY = arrayOf("首页", "文件", "工具", "我的")

        const val BOTTOM_CHECKED_COLOR: Int = 0xff7087fc.toInt()
        const val BOTTOM_UNCHECKED_COLOR: Int = 0xffc0c0c0.toInt()

        val FRAGMENT_CLASS_ARRAY: Array<Class<out BaseFragment>> = arrayOf(
            FHome::class.java,
            FFile::class.java,
            FBox::class.java,
            FMine::class.java
        )

    }

    private var mCheckedFragmentID: Int = DEFAULT_INDEX


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.a_home)
        super.onCreate(savedInstanceState)
        regToWx()
    }

    override fun initView() {
        val view2 = this@MainActivity.findViewById<TextView>(R.id.divider)
        val different = AppUtil.getNavigationBarHeightIfRoom(this)
        JLog.i("different = $different")
        if (different != 0) {
            view2.height = different
        } else {
            view2.height = 0
        }
    }


    override fun onItemClick(item: View?, index: Int) {
        mCheckedFragmentID = index
    }


    override fun putFragments(): Array<Class<out BaseFragment>> {
        return FRAGMENT_CLASS_ARRAY
    }

    private val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
    override fun getBottomItemView(index: Int): View {
        val bottomView = bottomLayoutInflater.inflate(R.layout.l_home_bottom, null)

        val bottomLayout = bottomView.findViewById<LinearLayout>(R.id.home_page_bottom_layout)
        bottomLayout.layoutParams = params

        val bottomImage = bottomView.findViewById<ImageView>(R.id.home_page_bottom_image)
        bottomImage.setImageResource(BOTTOM_ICON_UNCHECKED[index])
        val buttonName = bottomView.findViewById<TextView>(R.id.home_page_bottom_btn_name)
        buttonName.text = BOTTOM_TEXT_ARRAY[index]
        return bottomView
    }

    override fun getFLid(): Int {
        return R.id.fl_home_body
    }

    override fun getBottomLayout(): LinearLayout? {
        val view = this@MainActivity.findViewById<LinearLayout>(R.id.ll_home_bottom)
        return view
    }

    override fun checkAllBottomItem(item: View?, position: Int, isChecked: Boolean) {
        (item?.findViewById<ImageView>(R.id.home_page_bottom_image))?.setImageResource(if (isChecked) BOTTOM_ICON_CHECKED[position] else BOTTOM_ICON_UNCHECKED[position])
        (item?.findViewById<TextView>(R.id.home_page_bottom_btn_name))?.setTextColor(if (isChecked) BOTTOM_CHECKED_COLOR else BOTTOM_UNCHECKED_COLOR)
    }

    override fun setTabSel(item: View?, index: Int) {
        super.setTabSel(item, index)

    }

    fun changeFragment(index: Int) {
        setTabSel(bottomLayout?.getChildAt(index), index)
    }


    private var mLastClick: Long = 0L
    override fun onBackPressed() {
        if (System.currentTimeMillis() - mLastClick < 2000) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory(Intent.CATEGORY_HOME)
            }
            startActivity(homeIntent)
        } else {
            Toast.makeText(this@MainActivity, "再按一下后退键退出程序", Toast.LENGTH_SHORT).show()
            mLastClick = System.currentTimeMillis()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.hook -> {

            }
        }
    }

    private fun regToWx() {
        Constant.api = WXAPIFactory.createWXAPI(this, Constant.TENCENT_APP_ID, true)
        Constant.api.registerApp(Constant.TENCENT_APP_ID)

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Constant.api.registerApp(Constant.TENCENT_APP_ID)
            }
        }, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP))
    }

}