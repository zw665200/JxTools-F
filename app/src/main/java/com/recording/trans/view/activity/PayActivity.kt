package com.recording.trans.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mobads.action.ActionParam
import com.baidu.mobads.action.ActionType
import com.baidu.mobads.action.BaiduAction
import com.bumptech.glide.Glide
import com.tencent.mmkv.MMKV
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.FileBean
import com.recording.trans.bean.Price
import com.recording.trans.bean.Resource
import com.recording.trans.callback.Callback
import com.recording.trans.callback.DialogCallback
import com.recording.trans.callback.PayCallback
import com.recording.trans.controller.Constant
import com.recording.trans.controller.LogReportManager
import com.recording.trans.controller.PayManager
import com.recording.trans.http.loader.OrderDetailLoader
import com.recording.trans.http.response.ResponseTransformer
import com.recording.trans.http.schedulers.SchedulerProvider
import com.recording.trans.utils.AppUtil
import com.recording.trans.utils.JLog
import com.recording.trans.utils.RomUtil
import com.recording.trans.utils.ToastUtil
import com.recording.trans.view.base.BaseActivity
import com.recording.trans.view.views.DiscountDialog
import com.recording.trans.view.views.PaySuccessDialog
import com.recording.trans.view.views.QuitDialog
import kotlinx.android.synthetic.main.heart_small.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*


class PayActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var pay: Button
    private lateinit var userAgreement: AppCompatCheckBox
    private lateinit var wechatPay: AppCompatCheckBox
    private lateinit var aliPay: AppCompatCheckBox
    private lateinit var titleName: TextView

    private lateinit var firstLayout: FrameLayout
    private lateinit var secondLayout: FrameLayout

    private lateinit var firstPriceView: TextView
    private lateinit var secondPriceView: TextView
    private lateinit var firstOriginPriceView: TextView
    private lateinit var secondOriginPriceView: TextView

    private lateinit var discount: TextView
    private lateinit var discountWx: TextView
    private lateinit var menuBox: RecyclerView

    private var currentServiceId = 0
    private var firstServiceId = 0
    private var secondServiceId = 0

    private var lastClickTime: Long = 0L

    private var mPrice = 0f
    private var firstPrice = 0f
    private var secondPrice = 0f

    private lateinit var firstDailyPrice: TextView
    private lateinit var secondDailyPrice: TextView
    private lateinit var customerAgreement: TextView

    private var kv: MMKV? = MMKV.defaultMMKV()
    private var orderSn = ""
    private var startPay = false
    private var isBack = 0

    override fun setLayout(): Int {
        return R.layout.a_recovery_pay
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        back = findViewById(R.id.iv_back)
        pay = findViewById(R.id.do_pay)
        wechatPay = findViewById(R.id.do_wechat_pay)
        aliPay = findViewById(R.id.do_alipay_pay)
        titleName = findViewById(R.id.pay_content)

        customerAgreement = findViewById(R.id.customer_agreement)
        userAgreement = findViewById(R.id.user_agreement)
        discount = findViewById(R.id.discount)
        discountWx = findViewById(R.id.discount_wx)
        menuBox = findViewById(R.id.menu_box)

        firstLayout = findViewById(R.id.fl_1)
        secondLayout = findViewById(R.id.fl_2)
        firstPriceView = findViewById(R.id.price1)
        secondPriceView = findViewById(R.id.price2)
        firstOriginPriceView = findViewById(R.id.origin_price1)
        secondOriginPriceView = findViewById(R.id.origin_price2)
        firstDailyPrice = findViewById(R.id.price1_per_day)
        secondDailyPrice = findViewById(R.id.price2_per_day)

        //原价删除线
        firstOriginPriceView.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
        secondOriginPriceView.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG

        back.setOnClickListener { onBackPressed() }
        pay.setOnClickListener { checkPay(this) }
        firstLayout.setOnClickListener { chooseMenu(1) }
        secondLayout.setOnClickListener { chooseMenu(2) }
        customerAgreement.setOnClickListener { toAgreementPage() }
        menuBox.setOnTouchListener { _, _ ->
            firstLayout.performClick()
            false
        }


        //选择微信支付
        wechatPay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                aliPay.isChecked = false
            }
        }

        //选择支付宝支付
        aliPay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                wechatPay.isChecked = false
            }
        }


        chooseMenu(1)
        kv = MMKV.defaultMMKV()


    }

    override fun onResume() {
        super.onResume()
        if (startPay) {
            checkPayResult()
        }
    }

    override fun initData() {
        getServicePrice()
        loadMenuBox()

        LogReportManager.logReport("付费", "访问页面", LogReportManager.LogType.OPERATION)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun loadMenuBox() {
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
            .setLayoutId(R.layout.heart_small)
            .addBindView { itemView, itemData ->
                Glide.with(this).load(itemData.icon).into(itemView.iv_icon)
                itemView.tv_name.text = itemData.name
            }
            .create()

        menuBox.layoutManager = GridLayoutManager(this, 4)
        menuBox.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
    }


    private fun chooseMenu(index: Int) {
        val width = AppUtil.getScreenWidth(this)
        when (index) {
            1 -> {
                firstLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_select, null)
                secondLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_unselect, null)
                currentServiceId = firstServiceId

                val firstParams = firstLayout.layoutParams
                firstParams.width = width * 14 / 32
                firstLayout.layoutParams = firstParams

                val secondParams = secondLayout.layoutParams
                secondParams.width = width * 11 / 32
                secondLayout.layoutParams = secondParams

            }

            2 -> {
                firstLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_unselect, null)
                secondLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_corner_select, null)
                currentServiceId = secondServiceId

                val firstParams = firstLayout.layoutParams
                firstParams.width = width * 11 / 32
                firstLayout.layoutParams = firstParams

                val secondParams = secondLayout.layoutParams
                secondParams.width = width * 14 / 32
                secondLayout.layoutParams = secondParams
            }
        }

    }


    private fun toAgreementPage() {
        val intent = Intent()
        intent.setClass(this, AgreementActivity::class.java)
        startActivity(intent)
    }

    private fun getServicePrice() {

        PayManager.get().getServiceList(this) {
            val packDetails = arrayListOf<Price>()
            for (child in it) {
                if (child.server_code == Constant.VOICE) {
                    packDetails.add(child)
                }
            }

            if (packDetails.isEmpty()) {
                ToastUtil.showShort(this, "已付费")
                finish()
                return@getServiceList
            }

            if (packDetails.size >= 2) {
                for (child in packDetails) {
                    if (child.expire_type == "1") {
                        firstServiceId = child.id
                        firstPrice = child.sale_price.toFloat()
                        currentServiceId = firstServiceId
                    }

                    if (child.expire_type == "3") {
                        secondServiceId = child.id
                        secondPrice = child.sale_price.toFloat()
                    }
                }
            }

            //刷新价格
            changeDescription()
        }
    }


    private fun changeDescription() {
        pay.visibility = View.VISIBLE
        val firstText = "￥${String.format("%.0f", firstPrice)}一年"
        val secondText = "￥${String.format("%.0f", secondPrice)}一月"
        val firstPercentText = "${String.format("%.2f", firstPrice / 365)}/天"
        val secondPercentText = "${String.format("%.2f", secondPrice / 30)}/天"
        firstPriceView.text = firstText
        secondPriceView.text = secondText
        firstDailyPrice.text = firstPercentText
        secondDailyPrice.text = secondPercentText
    }

    private fun checkPay(c: Activity) {
        if (!userAgreement.isChecked) {
            ToastUtil.show(this, "请阅读并勾选《会员须知》")
            return
        }

        if (!wechatPay.isChecked && !aliPay.isChecked) {
            ToastUtil.show(this, "请选择付款方式")
            return
        }

        if (Constant.CLIENT_TOKEN == "") {
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        if (lastClickTime == 0L) {
            lastClickTime = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - lastClickTime < 2 * 1000) {
            ToastUtil.showShort(c, "请不要频繁发起支付")
            return
        }

        lastClickTime = System.currentTimeMillis()


        if (wechatPay.isChecked) {
            startPay = true
            doPay(c, 2)
        } else {
            startPay = false
            doPay(c, 1)
        }
    }

    /**
     *  index = 0快速支付 1支付宝支付 2微信支付
     */
    private fun doPay(c: Activity, index: Int) {
        when (index) {
            0 -> PayManager.get().doFastPay(c, currentServiceId, object : PayCallback {
                override fun success() {
                }

                override fun progress(orderId: String) {
                    orderSn = orderId
                }

                override fun failed(msg: String) {
                    launch(Dispatchers.Main) {
                        ToastUtil.showShort(c, msg)
                    }
                }
            })

            1 -> PayManager.get().doAliPay(c, currentServiceId, isBack, object : PayCallback {
                override fun success() {
                    launch(Dispatchers.Main) {

                        //pay upload
                        if (!RomUtil.isOppo() && Constant.OCPC) {
                            val actionParam = JSONObject()
                            actionParam.put(ActionParam.Key.PURCHASE_MONEY, mPrice * 100)
                            BaiduAction.logAction(ActionType.PURCHASE, actionParam)
                        }

                        //返回支付结果
                        ToastUtil.showShort(c, "支付成功")

                        LogReportManager.logReport("付费", "付费成功", LogReportManager.LogType.OPERATION)

                        openPaySuccessDialog()
                    }
                }

                override fun progress(orderId: String) {
                    orderSn = orderId
                }

                override fun failed(msg: String) {
                    launch(Dispatchers.Main) {
                        ToastUtil.showShort(c, msg)
                        openDiscountDialog()

                        LogReportManager.logReport("付费", "付费取消", LogReportManager.LogType.OPERATION)
                    }
                }
            })

            2 -> PayManager.get().doWechatPay(c, currentServiceId, object : PayCallback {
                override fun success() {
                }

                override fun progress(orderId: String) {
                    JLog.i("orderId = $orderId")
                    orderSn = orderId
                }

                override fun failed(msg: String) {
                }
            })
        }

    }

    private fun checkPayResult() {
        JLog.i("orderSn = $orderSn")
        if (orderSn == "") return
        launch(Dispatchers.IO) {
            OrderDetailLoader.getOrderStatus(orderSn)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    JLog.i("order_sn = ${it.order_sn}")
                    if (it.order_sn != orderSn) {
                        return@subscribe
                    }

                    when (it.status) {
                        "1" -> {

                            //pay upload
                            if (!RomUtil.isOppo() && Constant.OCPC) {
                                val actionParam = JSONObject()
                                actionParam.put(ActionParam.Key.PURCHASE_MONEY, mPrice * 100)
                                BaiduAction.logAction(ActionType.PURCHASE, actionParam)
                            }

                            openPaySuccessDialog()

                            //返回支付结果
                            ToastUtil.showShort(this@PayActivity, "支付成功")
                        }

                        else -> {
                            ToastUtil.show(this@PayActivity, "未支付")
                        }
                    }

                }, {
                    ToastUtil.show(this@PayActivity, "查询支付结果失败")
                })
        }

    }

    private fun toPaySuccessPage() {
        val intent = Intent(this, PaySuccessActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openDiscountDialog() {
        DiscountDialog(this@PayActivity, object : Callback {
            override fun onSuccess() {
                isBack = 1
                checkPay(this@PayActivity)
            }

            override fun onCancel() {
            }
        }).show()
    }

    private fun openPaySuccessDialog() {
        PaySuccessDialog(this@PayActivity, object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                setResult(0x100)
                finish()
            }

            override fun onCancel() {
            }
        }).show()
    }


    override fun onBackPressed() {
        QuitDialog(this, getString(R.string.quite_title), object : DialogCallback {
            override fun onSuccess(file: FileBean) {
                finish()
            }

            override fun onCancel() {
            }
        }).show()
    }


}