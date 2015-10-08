package fabulous.template.file.read.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fabulous.template.util.KeyAttsComparable;

/**
 * read T list with same key attributes from multiple files
 */
public class MultiFilesReadDao<T extends KeyAttsComparable<T>> {
	private final static Log LOG = LogFactory.getLog(MultiFilesReadDao.class);

	private List<TListFileReadDao<T>> daoList;

	public MultiFilesReadDao(List<String> fileFullPaths, Class<T> clazz) throws IOException {
		daoList = new ArrayList<TListFileReadDao<T>>();
		for (String fileFullPath : fileFullPaths) {
			this.daoList.add(new TListFileReadDao<T>(fileFullPath, clazz));
		}
	}

	private T getMinT() throws IOException {
		T minT = daoList.get(0).current();
		if (daoList.size() == 1)
			return minT;

		for (TListFileReadDao<T> tListFileReadDao : daoList) {
			if (minT.compare(tListFileReadDao.current()) > 0) {
				minT = tListFileReadDao.current();
			}
		}

		return minT;
	}

	public List<T> readTListForCurrentAsin() {
		if (daoList == null || daoList.size() == 0)
			return null;

		List<T> result = new ArrayList<T>();
		try {
			T minT = getMinT();
			for (int i = 0; i < daoList.size(); ++i) {
				if (minT.compare(daoList.get(i).current()) != 0)
					continue;

				result.addAll(daoList.get(i).getTListWithSameKey());
				if (daoList.get(i).isFileEnd()) {
					daoList.remove(i);
				}
			}
		} catch (IOException e) {
			LOG.error(this, e);
			throw new RuntimeException("Exception happened when read objects from files: " + e.getMessage());
		}

		return result;
	}
}
