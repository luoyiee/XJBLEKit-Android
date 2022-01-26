package cc.xiaojiang.lib.ble.utils;

import android.text.TextUtils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class SignUtil {


    //加密
    public static byte[] AES128(String content, String key) {
        return AES128(content.getBytes(), key);
    }

    public static byte[] AES128(byte[] content, String key) {
        if (TextUtils.isEmpty(key)) {
            BleLog.e("call AES128 with key empty!");
            return null;
        }
        BleLog.d("BleKey: " + key);
        String IV = "1ci5crnda6ojzgtr";//向量iv
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = null;//"算法/模式/补码方式"
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            //使用CBC模式，需要一个向量iv，可增加加密算法的强度
            IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ips);
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
