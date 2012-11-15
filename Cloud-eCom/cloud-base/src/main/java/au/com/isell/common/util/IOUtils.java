/* $Id: IOUtils.java 9540 2009-02-23 00:06:21Z brucez $ */
package au.com.isell.common.util;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

public class IOUtils {
	public static final String DEFAULT_URL_ENCODING = "ISO-8859-1";
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IOUtils.class);

	public static final int ZIPOUT_BUFFER = 10240;

	private IOUtils() {
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
					logger.warn("########Can't delete file ,check if locked. Path : {}", file.getAbsoluteFile());
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

}
