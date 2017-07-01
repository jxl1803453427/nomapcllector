package com.example.signalcollection.qinniu;

import com.google.gson.Gson;
import com.qiniu.android.utils.UrlSafeBase64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hehe on 2016/4/23.
 */
public class GenearateToke {
    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";
    private static final String KEY_AK = "yEpW7Q5gCb4OTJywJHc8mcyCf9JrBkwguIHl5fQY";
    private static final String KEY_SK = "OnLJVS3IjIOOPeo5QQ_bARtiAw56OStZjwpT2LgR";
    public static final String KEY_SCOPE = "jmtool3";//j6ch

    public static String getToken(ReturnBody returnBody) {
        PutPolicy putPolicy = new PutPolicy();
        putPolicy.setScope(KEY_SCOPE);// +":" + returnBody.getName()
        putPolicy.setDeadline(System.currentTimeMillis() / 1000 + 60 * 60);
        //    putPolicy.setReturnBody(returnBody);
        String putPolicyStr = new Gson().toJson(putPolicy);


        String _encodedPutPolicy = UrlSafeBase64.encodeToString(putPolicyStr.toString().getBytes());
        byte[] _sign = new byte[0];
        try {
            _sign = HmacSHA1Encrypt(_encodedPutPolicy, KEY_SK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String _encodedSign = UrlSafeBase64.encodeToString(_sign);
        String _uploadToken = KEY_AK + ':' + _encodedSign + ':'
                + _encodedPutPolicy;

        return _uploadToken;
    }


    /**
     * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
     *
     * @param encryptText 被签名的字符串
     * @param encryptKey  密钥
     * @return
     * @throws Exception
     */
    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);

        byte[] text = encryptText.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }


}
