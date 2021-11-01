package com.pangu.framework.utils.codec;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密解密工具
 */
public class CryptUtils {

	private final static String DES = "DES";
	private final static String CHARSET = "UTF-8";

	/**
	 * BASE64 编码
	 *
	 * @param s
	 * @return
	 */
	@SuppressWarnings("restriction")
	public static String base64Encode(String s) {
		if (s == null) {
			return null;
		}
		try {
			return (new sun.misc.BASE64Encoder()).encode(s.getBytes(CHARSET));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("MD5 decrypt", e);
		}
	}

	/**
	 * BASE64 编码
	 *
	 * @param s
	 * @return
	 */
	@SuppressWarnings("restriction")
	public static String base64Encode(byte[] s) {
		if (s == null) {
			return null;
		}
		return (new sun.misc.BASE64Encoder()).encode(s);
	}

	/**
	 * BASE 解码
	 *
	 * @param s
	 * @return
	 */
	@SuppressWarnings("restriction")
	public static String base64Decode(String s) {
		if (s == null) {
			return null;
		}
		sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(s);
			return new String(b);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * MD5 摘要计算(byte[]).
	 * 
	 * @param src
	 *            byte[]
	 * @throws Exception
	 * @return byte[] 16 bit digest
	 */
	public static byte[] md5(byte[] src) {
		try {
			MessageDigest alg = MessageDigest.getInstance("MD5");
			return alg.digest(src);
		} catch (Exception e) {
			throw new RuntimeException("MD5", e);
		}
	}

	/**
	 * MD5 摘要计算(String).
	 * 
	 * @param src
	 *            String
	 * @throws Exception
	 * @return String
	 */
	public static String md5(String src) {
		try {
			return byte2hex(md5(src.getBytes(CHARSET)));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("MD5 decrypt", e);
		}
	}

	/**
	 * MD5 摘要计算(String) <per>base64方式转换字符串.</per>
	 *
	 * @param src
	 *            String
	 * @throws Exception
	 * @return String
	 */
	public static String base64Md5(String src) {
		try {
			return byte2Base64(md5(src.getBytes(CHARSET)));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("MD5 decrypt", e);
		}
	}

	/**
	 * 加密
	 * 
	 * @param src
	 *            数据源
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * @return 返回加密后的数据
	 * @throws Exception
	 */
	public static byte[] encryptDES(byte[] src, byte[] key) {
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			// 从原始密匙数据创建DESKeySpec对象
			DESKeySpec dks = new DESKeySpec(key);
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			// 一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
			SecretKey securekey = keyFactory.generateSecret(dks);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance(DES);
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
			// 现在，获取数据并加密
			// 正式执行加密操作
			return cipher.doFinal(src);
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException("DES encrypt", e);
		}
	}

	public static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("长度[" + b.length + "]不是偶数");
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	/**
	 * 解密
	 * 
	 * @param src
	 *            数据源
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * @return 返回解密后的原始数据
	 * @throws Exception
	 */
	public static byte[] decryptDES(byte[] src, byte[] key) {
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			// 从原始密匙数据创建一个DESKeySpec对象
			DESKeySpec dks = new DESKeySpec(key);
			// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
			// 一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
			SecretKey securekey = keyFactory.generateSecret(dks);
			// Cipher对象实际完成解密操作
			Cipher cipher = Cipher.getInstance(DES);
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
			// 现在，获取数据并解密
			// 正式执行解密操作
			return cipher.doFinal(src);
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException("DES decrypt", e);
		}
	}

	/**
	 * 密码解密
	 * 
	 * @param data
	 *            密文
	 * @param key
	 *            密匙
	 * @return
	 * @throws Exception
	 */
	public final static String decryptPassword(String data, String key) {
		if (data != null) {
			try {
				return new String(decryptDES(hex2byte(data.getBytes(CHARSET)), key.getBytes(CHARSET)));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("DES decrypt", e);
			}
		}
		return null;
	}

	public final static String HMACSHA256(String data, String key) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			return byte2hex(mac.doFinal(data.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("HMACSHA256 decrypt", e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("DES decrypt", e);
		}
	}

	/**
	 * 密码加密
	 * 
	 * @param password
	 *            明文
	 * @param key
	 *            密匙
	 * @return
	 * @throws Exception
	 */
	public final static String encryptPassword(String password, String key) {
		if (password != null)
			try {
				return byte2hex(encryptDES(password.getBytes(CHARSET), key.getBytes(CHARSET)));
			} catch (Exception e) {
				throw new RuntimeException("DES encrypt", e);
			}
		return null;
	}

	/**
	 * 二行制转字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		StringBuilder sb = new StringBuilder();
		String stmp = "";
		for (int n = 0; b != null && n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				sb.append("0").append(stmp);
			else
				sb.append(stmp);
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * 字节数组转换Base64字符串
	 *
	 * @param b
	 * @return
	 */
	public static String byte2Base64(byte[] b) {
		return base64Encode(b);
	}

	/**
	 * 二行制转页面字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2webhex(byte[] b) {
		return byte2hex(b, "%");
	}

	/**
	 * 二行制转字符串
	 * 
	 * @param b
	 * @param elide
	 *            分隔符
	 * @return
	 */
	public static String byte2hex(byte[] b, String elide) {
		StringBuilder sb = new StringBuilder();
		String stmp = "";
		elide = elide == null ? "" : elide;
		for (int n = 0; b != null && n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				sb.append(elide).append("0").append(stmp);
			else
				sb.append(elide).append(stmp);
		}
		return sb.toString().toUpperCase();
	}

	public static String md5String(byte[] source) {
		return byte2hex(md5(source));
	}

	/**
	 * Blowfish加密
	 * 
	 * @param key
	 *            加密KEY
	 * @param source
	 *            原始数据
	 * @return 加密数据
	 */
	public static byte[] encrypt(final byte[] source, final byte[] key) {
		Blowfish bf = new Blowfish();
		bf.init(true, key);
		return bf.encrypt(source);
	}

	/**
	 * Blowfish解密
	 * 
	 * @param key
	 *            加密KEY
	 * @param source
	 *            加密数据
	 * @return 原始数据
	 */
	public static byte[] decrypt(final byte[] source, final byte[] key) {
		Blowfish bf = new Blowfish();
		bf.init(true, key);
		return bf.decryptBytes(source);
	}

}
