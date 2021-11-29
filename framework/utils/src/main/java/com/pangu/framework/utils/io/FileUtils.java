package com.pangu.framework.utils.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * 文件系统工具类
 * @author author
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

	/**
	 * 清除指定目录内的全部内容
	 * @param targetDirectory 被清除的文件目录
	 */
	public static void clearDirectory(File targetDirectory) {
		final Stack<File> directories = new Stack<File>();
		final Stack<File> files = new Stack<File>();
		final FileFilter directoryFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				files.push(file);
				return false;
			}
		};
		directories.push(targetDirectory);
		while (!directories.isEmpty()) {
			final File directory = directories.pop();
			files.push(directory);
			Collections.addAll(directories, directory.listFiles(directoryFilter));
		}
		for (File file : files) {
			file.delete();
		}
	}

	/**
	 * 拷贝文件
	 * @param cacheSize
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static long copyFile(int cacheSize, File from, File to) throws IOException {
		final long time = new Date().getTime();

		final FileInputStream in = new FileInputStream(from);
		final FileOutputStream out = new FileOutputStream(to);
		final FileChannel inChannel = in.getChannel();
		final FileChannel outChannel = out.getChannel();

		int length;
		while (true) {
			if (inChannel.position() == inChannel.size()) {
				inChannel.close();
				outChannel.close();
				return new Date().getTime() - time;
			}
			if ((inChannel.size() - inChannel.position()) < cacheSize) {
				length = (int) (inChannel.size() - inChannel.position());
			} else {
				length = cacheSize;
			}
			inChannel.transferTo(inChannel.position(), length, outChannel);
			inChannel.position(inChannel.position() + length);
		}
	}

	/**
	 * 关闭文件流
	 * @param reader
	 */
	public static void close(Closeable reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 * @param file the file to read, must not be <code>null</code>
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @return the file contents, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
	 */
	public static String readFileToString(InputStream file, String encoding) throws IOException {
		try {
			return IOUtils.toString(file, encoding);
		} finally {
			IOUtils.closeQuietly(file);
		}
	}

	/**
	 * Reads the contents of a file into a String using the default encoding for the VM. The file is always closed.
	 * @param file the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @since Commons IO 1.3.1
	 */
	public static String readFileToString(InputStream file) throws IOException {
		return readFileToString(file, null);
	}

	/**
	 * Reads the contents of a file into a byte array. The file is always closed.
	 * @param file the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @since Commons IO 1.1
	 */
	public static byte[] readFileToByteArray(File file) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toByteArray(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Reads the contents of a file line by line to a List of Strings. The file is always closed.
	 * @param file the file to read, must not be <code>null</code>
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @return the list of Strings representing each line in the file, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
	 * @since Commons IO 1.1
	 */
	@SuppressWarnings("unchecked")
	public static List<String> readLines(InputStream file, String encoding) throws IOException {
		try {
			return IOUtils.readLines(file, encoding);
		} finally {
			IOUtils.closeQuietly(file);
		}
	}

	/**
	 * Reads the contents of a file line by line to a List of Strings using the default encoding for the VM. The file is
	 * always closed.
	 * @param file the file to read, must not be <code>null</code>
	 * @return the list of Strings representing each line in the file, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @since Commons IO 1.3
	 */
	public static List<String> readLines(InputStream file) throws IOException {
		return readLines(file, null);
	}

	/**
	 * Return an Iterator for the lines in a <code>File</code>.
	 * <p>
	 * This method opens an <code>InputStream</code> for the file. When you have finished with the iterator you should
	 * close the stream to free internal resources. This can be done by calling the {@link LineIterator#close()} or
	 * {@link LineIterator#closeQuietly(LineIterator)} method.
	 * <p>
	 * The recommended usage pattern is:
	 * 
	 * <pre>
	 * LineIterator it = FileUtils.lineIterator(file, &quot;UTF-8&quot;);
	 * try {
	 * 	while (it.hasNext()) {
	 * 		String line = it.nextLine();
	 * 		// / do something with line
	 * 	}
	 * } finally {
	 * 	LineIterator.closeQuietly(iterator);
	 * }
	 * </pre>
	 * <p>
	 * If an exception occurs during the creation of the iterator, the underlying stream is closed.
	 * @param file the file to open for input, must not be <code>null</code>
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @return an Iterator of the lines in the file, never <code>null</code>
	 * @throws IOException in case of an I/O error (file closed)
	 * @since Commons IO 1.2
	 */
	public static LineIterator lineIterator(InputStream file, String encoding) throws IOException {
		try {
			return IOUtils.lineIterator(file, encoding);
		} catch (IOException ex) {
			IOUtils.closeQuietly(file);
			throw ex;
		} catch (RuntimeException ex) {
			IOUtils.closeQuietly(file);
			throw ex;
		}
	}

	/**
	 * Return an Iterator for the lines in a <code>File</code> using the default encoding for the VM.
	 * @param file the file to open for input, must not be <code>null</code>
	 * @return an Iterator of the lines in the file, never <code>null</code>
	 * @throws IOException in case of an I/O error (file closed)
	 * @since Commons IO 1.3
	 * @see #lineIterator(File, String)
	 */
	public static LineIterator lineIterator(InputStream file) throws IOException {
		return lineIterator(file, null);
	}

}
