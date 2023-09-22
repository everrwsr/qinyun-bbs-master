package com.qinyun.bbs.common;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;

/**
 * aes加密、解密
 */
public class AESUtils {

	// 指定AES加密解密所用的密钥
	private static Key key;

	private static final String AES = "AES";
	
	/**
	 * 加密key为空, 默认为类名
	 */
	public AESUtils() {
		setkey(this.getClass().getName());
	}

	/**
	 * 设置加密key
	 * 
	 * @param keyStr
	 *            加密key值
	 */
	public AESUtils(String keyStr) {
		setkey(keyStr);
	}
	
	/**
	 * 设置加密的校验码
	 */
	private void setkey(String keyStr) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance(AES);  
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	        random.setSeed(keyStr.getBytes());
            kgen.init(128, random);  
            SecretKey secretKey = kgen.generateKey();  
            byte[] enCodeFormat = secretKey.getEncoded();
			key = new SecretKeySpec(enCodeFormat, AES);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// 对字符串进行AES加密，返回BASE64编码的加密字符串
	public final String encryptString(String str) {
		try {
	            Cipher cipher = Cipher.getInstance(AES);// 创建密码器  
	            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
	            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化  
	            byte[] result = cipher.doFinal(bytes);  
	            return Base64.encodeBase64URLSafeString(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// 对BASE64编码的加密字符串进行解密，返回解密后的字符串
	public final String decryptString(String str) {
		try {
				byte[] bytes = Base64.decodeBase64(str);
	            Cipher cipher = Cipher.getInstance(AES);// 创建密码器  
	            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化  
	            byte[] result = cipher.doFinal(bytes);  
	            return new String(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
