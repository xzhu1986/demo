/* $Id: IOUtils.java 9540 2009-02-23 00:06:21Z brucez $ */
package au.com.isell.common.io;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: iSell Pty Ltd
 * </p>
 * 
 * @author Kieren Dowding
 * @version 1.0
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IOUtils {
	public static final String DEFAULT_URL_ENCODING = "ISO-8859-1";

	public static final int ZIPOUT_BUFFER = 10240;

	private IOUtils() {
	}

	public static void write(String text, File file) throws IOException {
		IOException ioException = null;
		FileOutputStream out = null;
		ByteArrayInputStream bin = null;
		file.getParentFile().mkdirs();
		try {
			out = new FileOutputStream(file);
			bin = new ByteArrayInputStream(text.getBytes());
		} catch (Throwable t) {
			if (t instanceof IOException) {
				ioException = (IOException) t;
			} else {
				ioException = (IOException) (new IOException(t.getMessage())).initCause(t);
			}
			if (out != null) {
				try {
					out.close();
				} catch (Throwable t1) { /* do nothing */
				}
			}
			// no need to close bin
			throw ioException;
		}
		transfer(bin, out, 10240); // all errors are handled here and files
		// closed
	}

	public static byte[] load(final File file) throws IOException {
		IOException ioException = null;
		FileInputStream in = null;
		byte[] data = null;
		try {
			in = new FileInputStream(file);
			long len = file.length();
			if (len > (long) Integer.MAX_VALUE) {
				throw new IOException("File to big to read into memory");
			}

			data = new byte[(int) len];
			byte[] buffer = new byte[4 * 1024];
			int read;
			int total = 0;

			while ((read = in.read(buffer)) != -1) {
				System.arraycopy(buffer, 0, data, total, read);
				total = total + read;
			}
		} catch (Throwable t) {
			if (t instanceof IOException) {
				ioException = (IOException) t;
			} else {
				ioException = (IOException) (new IOException(t.getMessage())).initCause(t);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable t1) {
					if (ioException == null) {
						// this is the first error report it
						if (t1 instanceof IOException) {
							ioException = (IOException) t1;
						} else {
							ioException = (IOException) (new IOException(t1.getMessage())).initCause(t1);
						}
					}
				}
			}
		}
		if (ioException != null) {
			// got some error report it
			throw ioException;
		} // else {
		return data;
	}

	public static void transfer(final InputStream in, final OutputStream out, final int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];
		int read;
		IOException ioException = null;
		try {
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				out.flush();
			}
		} catch (Throwable t) {
			if (t instanceof IOException) {
				ioException = (IOException) t;
			} else {
				ioException = (IOException) (new IOException(t.getMessage())).initCause(t);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable t1) {
					if (ioException == null) {
						// this is the first error report it
						if (t1 instanceof IOException) {
							ioException = (IOException) t1;
						} else {
							ioException = (IOException) (new IOException(t1.getMessage())).initCause(t1);
						}
					}
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Throwable t2) {
					if (ioException == null) {
						// this is the first error report it
						if (t2 instanceof IOException) {
							ioException = (IOException) t2;
						} else {
							ioException = (IOException) (new IOException(t2.getMessage())).initCause(t2);
						}
					}
				}
			}
		}
		if (ioException != null) {
			// got some error report it
			throw ioException;
		}
	}

	public static long transferWithoutClose(final InputStream in, final OutputStream out, final int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];
		int read;
		long count = 0;
		IOException ioException = null;
		try {
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
				count += read;
			}
		} catch (Throwable t) {
			if (t instanceof IOException) {
				ioException = (IOException) t;
			} else {
				ioException = (IOException) (new IOException(t.getMessage())).initCause(t);
			}
		}
		if (ioException != null) {
			// got some error report it
			throw ioException;
		}
		return count;
	}

	public static long transferWithoutClose(final InputStream in, final WritableByteChannel out, final int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int read;
		long count = 0;
		IOException ioException = null;
		try {
			while ((read = in.read(buffer, 0, bufferSize)) != -1) {
				out.write(ByteBuffer.wrap(buffer, 0, read));
				count += read;
			}
		} catch (Throwable t) {
			if (t instanceof IOException) {
				ioException = (IOException) t;
			} else {
				ioException = (IOException) (new IOException(t.getMessage())).initCause(t);
			}
		}
		if (ioException != null) {
			// got some error report it
			throw ioException;
		}
		return count;
	}

	public static long transferWithoutClose(final ReadableByteChannel in, final WritableByteChannel out, final int bufferSize) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		int read;
		long count = 0;
		IOException ioException = null;
		try {
			while ((read = in.read(buffer)) != -1) {
				buffer.flip();
				out.write(buffer);
				count += read;
				buffer.clear();
			}
		} catch (Throwable t) {
			if (t instanceof IOException) {
				ioException = (IOException) t;
			} else {
				ioException = (IOException) (new IOException(t.getMessage())).initCause(t);
			}
		}
		if (ioException != null) {
			// got some error report it
			throw ioException;
		}
		return count;
	}

	public static String getErrorMsgFromHtmlErrStream(InputStream in) throws IOException {

		String msg = "";
		String str = "";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transfer(in, out, 4096);
		str = out.toString();
		int start = str.indexOf('%') + 1;
		msg = str.substring(start, str.indexOf('%', start));
		return msg;
	}

	public static void writeInt(OutputStream out, int value) throws IOException {
		out.write(value >> 24);
		out.write(value >> 16);
		out.write(value >> 8);
		out.write(value);
	}

	public static int readInt(InputStream in) throws IOException {
		int val = 0;
		for (int i = 0; i < 4; i++) {
			int v = in.read();
			if (v == -1)
				break;
			val = (val << 8) + v;
		}
		return val;
	}

	public static void writeLong(OutputStream out, long value) throws IOException {
		out.write((int) (value >> 56));
		out.write((int) (value >> 48));
		out.write((int) (value >> 40));
		out.write((int) (value >> 32));
		out.write((int) (value >> 24));
		out.write((int) (value >> 16));
		out.write((int) (value >> 8));
		out.write((int) value);
	}

	public static long readLong(InputStream in) throws IOException {
		long val = 0;
		for (int i = 0; i < 8; i++) {
			int v = in.read();
			if (v == -1)
				break;
			val = (val << 8) + v;
		}
		return val;
	}

	public static void writeBoolean(OutputStream out, boolean value) throws IOException {
		out.write(value ? 1 : 2);
	}

	public static boolean readBoolean(InputStream in) throws IOException {
		return in.read() == 1;
	}

	public static String serialize(int a) {
		return String.valueOf(a);
	}

	public static int unserializeInt(String s) {
		return unserializeInt(s, -1);
	}

	public static int unserializeInt(String s, int defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static String serialize(long a) {
		return String.valueOf(a);
	}

	public static long unserializeLong(String s) {
		return unserializeLong(s, -1);
	}

	public static long unserializeLong(String s, long defaultValue) {
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static String serialize(double a) {
		return String.valueOf(a);
	}

	public static double unserializeDouble(String s) {
		return unserializeDouble(s, -1);
	}

	public static double unserializeDouble(String s, double defaultValue) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static String serialize(boolean a) {
		return a ? "1" : "0";
	}

	public static boolean unserializeBool(String s) {
		return "1".equals(s.trim());
	}

	public static String serialize(Date a) {
		if (a == null)
			return "";
		return String.valueOf(a.getTime());
	}

	public static Date unserializeDate(String s) {
		if (s.length() == 0)
			return null;
		return new Date(Long.parseLong(s));
	}

	public static boolean deleteDir(File dir, boolean deleteRoot) {
		return deleteDir(dir, deleteRoot, null);
	}

	public static boolean deleteDir(File dir, boolean deleteRoot, FilenameFilter filter) {
		if (dir == null)
			return false;
		if (!dir.exists())
			return true;
		if (!dir.isDirectory()) {
			return dir.delete();
		}
		File[] files = null;
		if (filter != null)
			files = dir.listFiles(filter);
		else
			files = dir.listFiles();
		boolean result = true;
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					result = deleteDir(file, true, filter);
				} else {
					result = file.delete();
				}
				if (result == false) {
					return false;
				}
			}
		}
		if (deleteRoot) {
			return dir.delete();
		} else {
			return true;
		}
	}

	public static void copyDir(File source, File target, FilenameFilter filter) throws IOException {
		if (source == null)
			return;
		if (!source.exists())
			return;
		if (target.exists()) {
			if (!deleteDir(target, true))
				throw new IOException("Target exists.");
		}
		if (!source.isDirectory()) {
			copyFile(source, target);
			return;
		}
		target.mkdirs();
		File[] files = null;
		if (filter != null)
			files = source.listFiles(filter);
		else
			files = source.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					copyDir(file, new File(target, file.getName()), filter);
				} else {
					copyFile(file, new File(target, file.getName()));
				}
			}
		}
	}

	public static void copyFile(File source, File target) throws IOException {
		if (target.exists()) {
			if (!target.delete())
				throw new IOException("Target exists.");
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(target);
			transferWithoutClose(in, out, 102400);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception ex) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception ex) {
				}
			}
		}

	}

	public static File matchFileIgnoreCase(File file) {
		FileNameMatcher matcher = new FileNameMatcher();
		File f = file.getAbsoluteFile();
		List<File> path = new ArrayList<File>();
		for (File parent = f.getParentFile(); parent != null; parent = parent.getParentFile()) {
			path.add(0, parent);
		}
		path.add(f);
		int i = 0;
		File result = path.get(i);
		i++;
		for (; i < path.size(); i++) {
			matcher.setExpectName(path.get(i).getName());
			File[] files = result.listFiles(matcher);
			if (files != null && files.length > 0) {
				result = files[0];
			} else {
				result = new File(result, path.get(i).getName());
			}
		}
		return result;
	}

	private static class FileNameMatcher implements FilenameFilter {

		private String expectName;

		@Override
		public boolean accept(File dir, String name) {
			if (name.equalsIgnoreCase(expectName))
				return true;
			return false;
		}

		public void setExpectName(String expectName) {
			this.expectName = expectName.toLowerCase();
		}
	}

	public static void main(String[] args) {
		System.out.println(matchFileIgnoreCase(new File("//riddick/idata/apollo imports/altech computers/specific1/aaaaaaaaaa.xml")));
	}
}
