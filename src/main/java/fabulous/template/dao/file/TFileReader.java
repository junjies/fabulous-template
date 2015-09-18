package fabulous.template.dao.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Wrapper of TFileReadDao, provides getObjectList API
 */
public class TFileReader<T> {

	private TFileReadDao<T> fileReadDao;
	private static final int LIST_COUNT = 10000;

	public TFileReader() {
		super();
	}

	public TFileReader(String filePath, Class<T> tClazz) throws IOException {
		fileReadDao = new TFileReadDao<T>(filePath, tClazz);
	}

	public List<T> getObjectList() throws IOException {
		T t = fileReadDao.current();

		if (t == null) {
			return null;
		}

		List<T> result = new ArrayList<T>(LIST_COUNT);
		int count = 0;
		while (count++ < LIST_COUNT && t != null) {
			result.add(t);
			t = fileReadDao.next();
		}

		return result;
	}

	public void close() {
		fileReadDao.close();
	}

}
