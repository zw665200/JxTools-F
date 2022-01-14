package com.recording.trans.view.fragment

import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.Resource
import com.recording.trans.bean.UserInfo
import com.recording.trans.callback.Callback
import com.recording.trans.controller.Constant
import com.recording.trans.controller.DBManager
import com.recording.trans.controller.PayManager
import com.recording.trans.http.loader.AccountLoader
import com.recording.trans.http.response.ResponseTransformer
import com.recording.trans.http.schedulers.SchedulerProvider
import com.recording.trans.utils.*
import com.recording.trans.view.activity.AgreementActivity
import com.recording.trans.view.activity.CustomerServiceActivity
import com.recording.trans.view.activity.LoginActivity
import com.recording.trans.view.activity.PayActivity
import com.recording.trans.view.base.BaseFragment
import com.recording.trans.view.views.AccountDeleteDialog
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_function.view.*
import kotlinx.android.synthetic.main.item_mine_heart_small.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class FMine : BaseFragment() {
    private lateinit var title: TextView
    private lateinit var level: TextView
    private lateinit var phone: TextView
    private lateinit var vipFunction: RecyclerView
    private lateinit var customer: RecyclerView
    private lateinit var openPay: ImageView
    private lateinit var avatar: ImageView
    private lateinit var logout: Button

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_mine, container, false)
        title = rootView.findViewById(R.id.tv_mine_nick)
        level = rootView.findViewById(R.id.tv_mine_vip)
        phone = rootView.findViewById(R.id.tv_mine_phone)
        customer = rootView.findViewById(R.id.function)
        vipFunction = rootView.findViewById(R.id.vip_function)
        openPay = rootView.findViewById(R.id.open_pay)
        logout = rootView.findViewById(R.id.logout)
        avatar = rootView.findViewById(R.id.user_avatar)

        return rootView
    }

    override fun initData() {

        loadVipFunction()
        loadFunction()
        loadDeviceInfo()
        initHandler()
        loadUserInfo()

        title.setOnClickListener { checkLogin() }
        openPay.setOnClickListener { openPayPage() }
        logout.setOnClickListener { logOut() }

    }


    private fun initHandler() {
        Constant.mSecondHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0x1000 -> {
                        JLog.i("handler")
                        loadUserInfo()
                    }
                }
            }
        }

    }

    private fun checkLogin() {
        if (Constant.USER_NAME == "") {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        } else {
            title.text = Constant.USER_NAME
        }
    }

    private fun openPayPage() {
        PayManager.get().checkPay(requireActivity()) {
            if (!it) {
                val intent = Intent(activity, PayActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun click(v: View?) {
    }

    private fun loadVipFunction() {
        val list = arrayListOf<Resource>()
        list.add(Resource("1", R.drawable.mine_vip_1, getString(R.string.mine_vip_1)))
        list.add(Resource("2", R.drawable.mine_vip_2, getString(R.string.mine_vip_2)))
        list.add(Resource("3", R.drawable.mine_vip_3, getString(R.string.mine_vip_3)))
        list.add(Resource("4", R.drawable.mine_vip_4, getString(R.string.mine_vip_4)))
        list.add(Resource("5", R.drawable.mine_vip_5, getString(R.string.mine_vip_5)))
        list.add(Resource("6", R.drawable.mine_vip_6, getString(R.string.mine_vip_6)))
        list.add(Resource("7", R.drawable.mine_vip_7, getString(R.string.mine_vip_7)))
        list.add(Resource("8", R.drawable.mine_vip_8, getString(R.string.mine_vip_8)))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_mine_heart_small)
            .addBindView { itemView, itemData ->
                itemView.iv_icon.setImageResource(itemData.icon)
                itemView.tv_name.text = itemData.name
            }
            .create()

        vipFunction.layoutManager = GridLayoutManager(requireActivity(), 4)
        vipFunction.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, list.size)
    }

    private fun loadFunction() {
        val list = arrayListOf<Resource>()
        list.add(Resource("website", R.drawable.mine_website, getString(R.string.mine_website)))
        list.add(Resource("service", R.drawable.mine_feedback, getString(R.string.mine_service)))
        list.add(Resource("privacy", R.drawable.mine_help, getString(R.string.mine_privacy)))
        list.add(Resource("feedback", R.drawable.mine_ask_services, getString(R.string.mine_help)))
        list.add(Resource("about", R.drawable.mine_aboutus, getString(R.string.setting_about_us)))
        list.add(Resource("about", R.drawable.mine_delete_account, getString(R.string.setting_account_delete)))
        list.add(Resource("clear", R.drawable.mine_clear, getString(R.string.setting_clear_cache)))

        val mAdapter = DataAdapter.Builder<Resource>()
            .setData(list)
            .setLayoutId(R.layout.item_function)
            .addBindView { itemView, itemData, position ->
                itemView.function_icon.setImageResource(itemData.icon)
                itemView.function_name.text = itemData.name

                itemView.setOnClickListener {
                    when (position) {
                        0 -> openWebsite()
                        1 -> openUserAgreement()
                        2 -> openPrivacyAgreement()
                        3 -> openFeedback()
                        4 -> aboutUs()
                        5 -> accountDelete()
                        6 -> clearCache()
                    }
                }
            }
            .create()

        customer.layoutManager = LinearLayoutManager(requireActivity())
        customer.adapter = mAdapter
        mAdapter.notifyItemRangeChanged(0, list.size)
    }

    private fun loadUserInfo() {
        if (Constant.USER_NAME != "") {
            title.text = Constant.USER_NAME
            level.text = Constant.USER_VIP_STATUS
            if (Constant.USER_ICON != "") {
                Glide.with(this).load(Constant.USER_ICON).apply(RequestOptions.bitmapTransform(CircleCrop())).into(avatar)
            }

            logout.visibility = View.VISIBLE
            PayManager.get().getPayList(requireActivity()) {
                if (it.isNotEmpty()) {
                    val builder = StringBuilder()
                    for (order in it) {

                        if (order.server_code == Constant.VOICE) {
                            when (order.expire_type) {
                                "1" -> builder.append("年度会员 ")
                                "3" -> builder.append("月度会员 ")
                            }

                        }
                    }

                    val text = builder.toString()
                    if (text != "") {
                        level.text = text
                        openPay.setImageResource(R.drawable.bg_opened_vip)
                    } else {
                        level.text = getString(R.string.mine_level_after)
                        openPay.setImageResource(R.drawable.bg_open_vip)
                    }
                } else {
                    level.text = getString(R.string.mine_level_after)
                    openPay.setImageResource(R.drawable.bg_open_vip)
                }
            }
        } else {
            val userInfo = MMKV.defaultMMKV()?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                Constant.USER_NAME = userInfo.nickname
                title.text = Constant.USER_NAME
            }
        }
    }


    private fun loadDeviceInfo() {
        if (Build.BRAND == "HUAWEI" || Build.BRAND == "HONOR") {
            val name = Dict.getHUAWEIName(Build.MODEL)
            if (name.isNullOrEmpty()) {
                val b = "${Build.BRAND} ${Build.MODEL}"
                phone.text = b
            } else {
                phone.text = name
            }
        } else {
            val b = "${Build.BRAND} ${Build.MODEL}"
            phone.text = b
        }
    }


    private fun openWebsite() {
        if (Constant.WEBSITE == "") return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constant.WEBSITE))
        startActivity(intent)
    }


    private fun openFeedback() {
        val intent = Intent(requireActivity(), CustomerServiceActivity::class.java)
        startActivity(intent)
    }

    private fun openUserAgreement() {
        val intent = Intent(requireActivity(), AgreementActivity::class.java)
        intent.putExtra("index", 0)
        startActivity(intent)
    }

    private fun openPrivacyAgreement() {
        val intent = Intent(requireActivity(), AgreementActivity::class.java)
        intent.putExtra("index", 1)
        startActivity(intent)
    }

    private fun clearCache() {
        launch(Dispatchers.IO) {
            DBManager.deleteFiles(requireActivity())
            FileUtil.clearAllCache(requireActivity())
        }

        launch(Dispatchers.Main) {
            ToastUtil.showShort(requireActivity(), "清除成功")
        }

    }

    private fun aboutUs() {
        val packName = AppUtil.getPackageVersionName(requireActivity(), requireActivity().packageName)
        val appName = getString(R.string.app_name)
        ToastUtil.show(requireActivity(), "$appName $packName")
    }

    private fun accountDelete() {
        if (Constant.USER_NAME != "") {
            AccountDeleteDialog(requireActivity(), object : Callback {
                override fun onSuccess() {
                    delete()
                }

                override fun onCancel() {
                }
            }).show()
        } else {
            checkLogin()
        }
    }

    private fun delete() {
        launch {
            AccountLoader.delete()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    logOut()
                }, {
                })
        }
    }

    private fun logOut() {
        if (Constant.USER_NAME != "") {
            Constant.USER_NAME = ""
            Constant.CLIENT_TOKEN = ""
            title.text = getString(R.string.mine_login)
            level.text = getString(R.string.mine_level_before)
            logout.visibility = View.GONE

            val mmkv = MMKV.defaultMMKV()
            val userInfo = mmkv?.decodeParcelable("userInfo", UserInfo::class.java)
            if (userInfo != null) {
                mmkv.removeValueForKey("userInfo")
            }
        }
    }


}