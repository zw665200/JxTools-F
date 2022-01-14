package com.recording.trans.controller;

import android.os.Handler;

import com.tencent.mm.opensdk.openapi.IWXAPI;

public class Constant {

    public static final String PRODUCT_ID = "1";
    public static boolean isDebug = true;
    public static Handler mHandler;
    public static Handler mSecondHandler;
    public static String ROM = "";
    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_OTHER = "OTHER";
    public static String CLIENT_TOKEN = "";
    public static String QUEST_TOKEN = "questToken";
    public static String USER_ID = "";
    public static String USER_NAME = "";
    public static String USER_ICON = "";
    public static String USER_VIP_STATUS = "";

    //official "5ccd4758a1115ff5"   1
    //007 "4180600c8466bd91"   7
    //007s "751ddd4941efa712"   7s
    //shop "" 10
    public static String CHANNEL_ID = "751ddd4941efa712";
    public static String CHANNEL_HUAWEI = "fbe809e8e87ea4ed";
    public static String CHANNEL_OPPO = "4d2d62abdc4e9545";
    public static String CHANNEL_XIAOMI = "4403c81f78f13618";
    public static String CHANNEL_VIVO = "bee99269e9042d8d";
    public static String CHANNEL_FLYME = "6b5accf8ed4d87d7";
    public static String CHANNEL_A007 = "4180600c8466bd91";
    public static String CHANNEL_A007_SHOP = "751ddd4941efa712";
    public static String CHANNEL_OFFICE = "5ccd4758a1115ff5";
    public static boolean ScanStop = false;
    public static boolean OCPC = false;
    public static IWXAPI api = null;
    public static String WEBSITE = "";
    public static int TEST = 0;

    //Baidu com.recording.trans
    public static long USER_ACTION_SET_ID = 14293;
    public static String APP_SECRET_KEY = "ef0c7a4d4dc499ded6bc6047f3b0419c";

    //service_code
    public static String REC = "rec";
    public static String COM = "com";
    public static String REPL = "repl";
    public static String VOICE = "voice";
    public static String VOICE_TIMES = "times";

    //service_expire
    public static String EXPIRE_TYPE_FOREVER = "2";
    public static String EXPIRE_TYPE_YEAR = "1";
    public static String EXPIRE_TYPE_MONTH = "3";

    public static String EXPORT_PATH = "/export/";
    public static String WX_HIGN_VERSION_PATH = "/Android/data/com.tencent.mm/";
    public static String MM_RESOURCE_PATH = "/Android/data/com.immomo.momo/";
    public static String SOUL_RESOURCE_PATH = "/Android/data/cn.soulapp.android/";
    public static String WX_PICTURE_PATH = "/Pictures/WeiXin/";
    public static String PICTURE_PATH = "/Pictures/";
    public static String DOWNLOAD_PATH = "/Download/";
    public static String DCIM_PATH = "/DCIM/";
    public static String QQ_PICTURE_PATH = "/Pictures/";
    public static String WX_DB_PATH = "/App/com.tencent.mm/MicroMsg/";
    public static String WX_ZIP_PATH = "APP/com.tencent.mm.zip";
    public static String WX_RESOURCE_PATH = "/tencent/";
    public static String WX_DOWNLOAD_PATH = "/Download/Weixin/";
    public static String QQ_RESOURCE_PATH = "/tencent/MobileQQ/";
    public static String QQ_HIGN_VERSION_PATH = "/Android/data/com.tencent.mobileqq/";
    public static String WX_BACKUP_NAME = "tencent";
    public static String FLYME_BACKUP_PATH = "/backup/";
    public static String HW_BACKUP_PATH = "/HuaweiBackup/backupFiles/";
    public static String HW_BACKUP_PATH2 = "/huawei/Backup/backupFiles/";
    public static String WX_PACK_NAME = "com.tencent.mm";

    public static String BACKUP_PATH = "/aA123456在此/";
    public static String XM_BACKUP_PATH = "/MIUI/backup/AllBackup/";
    public static String OPPO_BACKUP_PATH = "/backup/App/";
    public static String HW_BACKUP_NAME_DB = "com.tencent.mm.db";
    public static String HW_BACKUP_NAME_TAR = "com.tencent.mm.tar";
    public static String HW_BACKUP_APP_DATA_TAR = "com.tencent.mm_appDataTar";
    public static String MZ_BACKUP_NAME_TAR = "com.tencent.mm.zip";
    public static String XM_BACKUP_NAME_BAK = "微信(com.tencent.mm).bak";
    public static String OPPO_BACKUP_NAME_TAR = "com.tencent.mm.tar";
    public static String VIVO_BACKUP_NAME_TAR = "5a656b0891e6321126f9b7da9137994c72220ce7";
    public static String HW_BACKUP_NAME_XML = "info.xml";
    public static String JX_BACKUP_PATH = "/backup/";

    public static String DB_NAME = "EnMicroMsg.db";
    public static String DB_FTS_NAME = "FTS5IndexMicroMsg_encrypt.db";

    public static String UIN_CONFIG_NAME = "system_config_prefs.xml";
    public static String SYSTEM_INFO_NAME = "systemInfo.cfg";
    public static String COMPATIBLE_INFO_NAME = "CompatibleInfo.cfg";
    public static String HISTORY_INFO_NAME = "app_brand_global_sp.xml";
    public static String DENGTA_META_NAME = "DENGTA_META.xml";
    public static String AUTH_INFO_KEY_NAME = "auth_info_key_prefs.xml";
    public static String ZIP_FILE_SUFFIX = ".zip";
    public static String SPECIAL_FILE = "MicroMsg";
    public static Long CURRENT_BACKUP_TIME = 0L;
    public static String CURRENT_BACKUP_PATH = "";

    public static int PERMISSION_CAMERA_REQUEST_CODE = 0x00000011;
    public static int CAMERA_REQUEST_CODE = 0x00000012;

    //File
    public static int FILE_NOT_FOUND = 0;
    public static int FILE_UNZIP_FAILED = 1;
    public static int FILE_DAMAGE = 2;
    public static int FILE_CREATE_FAILED = 3;
    public static int FOLDER_CREATE_FAILED = 4;

    //db
    public static String NOT_FOUND_ACCOUNT = "没有找到账户";
    public static String NOT_FOUND_CONTACT = "没有找到联系人";
    public static String NOT_FOUND_MESSAGE = "没有找到聊天记录";

    //Realm
    public static String ROOM_DB_NAME = "EnMicroMsg";

    //IM
    public static String SDK_APP_ID = "132041";
    public static String SDK_APP_KEY = "1482210305025478#kefuchannelapp90871";
    public static String SDK_SERVICE_ID = "kefuchannelimid_451993";
    public static String SDK_DEFAULT_PASSWORD = "123456";

    //Notification
    public static String Notification_title = "消息提醒";
    public static String Notification_content = "您有新的客服消息";

    //Bugly
    public static String BUGLY_APPID = "e3efc56c14";

    //oss
    public static String END_POINT = "http://oss-cn-shenzhen.aliyuncs.com";
    public static String END_POINT_WITHOUT_HTTP = "oss-cn-shenzhen.aliyuncs.com";
    public static String BUCKET_NAME = "qlrecovery";

    //tencent Pay
    public static String TENCENT_APP_ID = "wxc844e9bf2d98a144";
    public static String TENCENT_MINI_PROGRAM_APP_ID = "gh_72629534a52d";
    public static String TENCENT_PARTNER_ID = "1605572449";

    //voice key
    public static String BAIDU_SPEECH_APP_ID = "25038796";
    public static String BAIDU_SPEECH_APP_KEY = "bOAQ1Abvn91rm9scAUHoQkAi";
    public static String BAIDU_SPEECH_APP_SECRET = "Q0RrSXQdjSD5TFoerCYquGoVDybqmi75";
}
