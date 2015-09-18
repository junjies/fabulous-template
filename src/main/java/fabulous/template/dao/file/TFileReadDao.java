package fabulous.template.dao.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;

/*
 * Map each line of file to object T.
 */
public class TFileReadDao<T> {
	private final static Log LOG = LogFactory.getLog(TFileReadDao.class);

	private String fileFullPath;
	private Class<T> clazz;
	private boolean hasInitRead = false;

	private Map<String, Integer> titleIndex = new HashMap<String, Integer>();
	private boolean isFileEnd = false;
	private BufferedReader br;
	private T current;

	public TFileReadDao(String fileFullPath, Class<T> clazz) throws IOException {
		this.fileFullPath = fileFullPath;
		this.clazz = clazz;
	}

	public boolean isFileEnd() {
		return isFileEnd;
	}

	public void setFileEnd(boolean isFileEnd) {
		this.isFileEnd = isFileEnd;
	}

	public String getFileFullPath() {
		return fileFullPath;
	}

	public void setFileFullPath(String fileFullPath) {
		this.fileFullPath = fileFullPath;
	}

	public T current() throws IOException {
		if (!hasInitRead)
			initRead();
		return this.current;
	}

	public T next() throws IOException {
		if (!hasInitRead)
			initRead();

		if (isFileEnd)
			return current;

		String line = br.readLine();
		if (line == null) {
			this.setFileEnd(true);
			current = null;
			return current;
		}
		String[] row = line.split("\t", -1);

		T t;
		try {
			t = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(String.format("Exception happened when get instance of %s", clazz.getName(), e));
		}

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			String name = field.getName();

			// turn field name to upper-case because file title what we cached
			// is upper-case
			String nameUppcased = name.toUpperCase();
			if (titleIndex.containsKey(nameUppcased)) {
				try {
					int idx = titleIndex.get(nameUppcased);
					String value = null;
					if (idx < row.length) {
						value = row[idx];
					}
					setField(t, field, value);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("useless case!", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("useless case!", e);
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new RuntimeException("useless case!", e);
				}
			}
		}

		current = t;
		return current;
	}

	public void close() {
		isFileEnd = true;
		if (this.br != null) {
			try {
				this.br.close();
			} catch (IOException e) {
				LOG.error(this.fileFullPath + " can't close");
				LOG.error(this, e);
			}
			this.br = null;
		}
	}

	private void setField(Object t, Field field, String str) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);

		if (str == null || str.length() <= 0) {
			field.set(t, null);
			return;
		}

		if (str.equals("null")) {
			field.set(t, null);
			return;
		}

		String type = field.getType().getSimpleName();
		if ("String".equals(type)) {
			field.set(t, str);
		} else if ("Integer".equals(type)) {
			field.set(t, Integer.parseInt(str));
		} else if ("Long".equals(type)) {
			field.set(t, Long.parseLong(str));
		} else if ("Short".equals(type)) {
			field.set(t, Short.parseShort(str));
		} else if ("BigDecimal".equals(type)) {
			field.set(t, new BigDecimal(str));
		} else if ("Double".equals(type)) {
			field.set(t, Double.parseDouble(str));
		} else if ("LocalDate".equals(type)) {
			field.set(t, new LocalDate(str));
		} else if ("Boolean".equals(type)) {
			field.set(t, new Boolean(str));
		} else {
			throw new RuntimeException("unsupport type:" + type);
		}
	}

	public String getFilePath() {
		return this.fileFullPath;
	}

	private void initRead() throws IOException {
		hasInitRead = true;
		File file = new File(fileFullPath);
		if (!file.exists()) {
			System.out.println(fileFullPath + " doesn't exist!");
			LOG.warn(fileFullPath + "doesn't exist!");
			RuntimeException e = new RuntimeException("file <" + fileFullPath + "> doesn't exist!");
			LOG.warn("TFileReadDAO: file " + fileFullPath + "doesnot exist!");
			throw e;
		}

		br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		if (line != null) {
			String[] row = line.split("\t");
			for (int i = 0; i < row.length; i++) {
				// cache the upper-case file title
				titleIndex.put(row[i].toUpperCase(), i);
			}
		} else {
			this.close();
		}

		this.next();
	}
}