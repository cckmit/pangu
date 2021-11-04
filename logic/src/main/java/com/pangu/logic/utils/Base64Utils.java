package  com.pangu.logic.utils;

import org.apache.commons.codec.binary.Base64;

/**
 * Base64工具类
 * @author qu.yy
 */
public class Base64Utils {
	/**
	 * 使用Base64加密
	 * @param plainText
	 * @return
	 */
	public static String encodeStr(String plainText) {
		byte[] b = plainText.getBytes();
		Base64 base64 = new Base64();
		b = base64.encode(b);
		String s = new String(b);
		return s;
	}

	/**
	 * 使用Base64解密
	 * @param encodeStr
	 * @return
	 */
	public static String decodeStr(String encodeStr) {
		byte[] b = encodeStr.getBytes();
		Base64 base64 = new Base64();
		b = base64.decode(b);
		String s = new String(b);
		return s;
	}
}
