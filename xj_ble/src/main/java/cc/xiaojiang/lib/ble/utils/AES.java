package cc.xiaojiang.lib.ble.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by facexxyz on 4/29/21.
 */
public class AES {
    /**
     * AES128 算法
     * CBC 模式
     * PKCS7Padding 填充模式
     * CBC模式需要添加一个参数iv
     * 介于java 不支持PKCS7Padding，只支持PKCS5Padding 但是PKCS7Padding 和 PKCS5Padding 没有什么区别
     * 要实现在java端用PKCS7Padding填充，需要用到bouncycastle组件来实现
     */
    // 加解密算法/模式/填充方式
    private static String algorithmStr = "AES/CBC/PKCS7Padding";
    private static String iv = "0123456789ABCDEF";

//
//    /**
//     * 加密方法
//     */
//    public static byte[] encrypt2(byte[] content, String key) {
//        byte[] encryptedText = null;
//        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
//        try {
//            Cipher cipher = Cipher.getInstance(algorithmStr);
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes()));
//            encryptedText = cipher.doFinal(content);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return encryptedText;
//    }

    public static byte[] encrypt(byte[] content, String key) {
        byte[] bleKeyArray = ByteUtils.hexStrToBytes(key);
        byte[] encryptedText = null;
        SecretKeySpec skeySpec = new SecretKeySpec(bleKeyArray, "AES");
        try {
            Cipher cipher = Cipher.getInstance(algorithmStr);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes()));
            encryptedText = cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedText;
    }

    /**
     * 解密方法
     */
    public static byte[] decrypt(byte[] content, String key) {
        byte[] bleKeyArray = ByteUtils.hexStrToBytes(key);
        byte[] encryptedText = null;
        SecretKeySpec skeySpec = new SecretKeySpec(bleKeyArray, "AES");
        try {
            Cipher cipher = Cipher.getInstance(algorithmStr);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv.getBytes()));
            encryptedText = cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedText;
    }


    public static void main(String[] args) {





    }


}
