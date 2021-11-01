package com.pangu.framework.utils.codec;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class FileMd5Utils {

	/**
	 * 对一个文件获取md5值
	 * @return md5串
	 */
	public static String getMD5(File file) {
		// 输入流会自动关闭
		try (FileInputStream in = new FileInputStream(file)) {
			return getMd5(in);
		} catch (Exception e) {
			throw new RuntimeException("生成文件MD5异常", e);
		}
	}

	/**
	 * 对一个文件获取md5值
	 * @return md5串
	 */
	public static String getMD5(String url) {
		// 输入流会自动关闭
		try (InputStream in = url.startsWith("/") ? new FileInputStream(url) : FileMd5Utils.class
				.getResourceAsStream(url)) {
			return getMd5(in);
		} catch (Exception e) {
			throw new RuntimeException("生成文件MD5异常", e);
		}
	}

	/**
	 * 对一个文件获取md5值
	 * @return md5串
	 */
	public static String getMd5(InputStream in) {
		try {
			byte[] buffer = new byte[8192];
			int length;
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			while ((length = in.read(buffer)) != -1) {
				MD5.update(buffer, 0, length);
			}
			return CryptUtils.byte2hex(MD5.digest());
		} catch (Exception e) {
			throw new RuntimeException("生成文件MD5异常", e);
		}
	}

}
