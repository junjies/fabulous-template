package fabulous.template.file.read.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fabulous.template.util.KeyAttsComparable;

/*
 * Read the list of T with same key fields, it's better that the file is ordered by key attributes
 */
public class TListFileReadDao<T extends KeyAttsComparable<T>> extends TFileReadDao<T> {

	public TListFileReadDao(String fileFullPath, Class<T> clazz) throws IOException {
		super(fileFullPath, clazz);
	}

	public List<T> getTListWithSameKey() throws IOException {
		List<T> list = new ArrayList<T>();

		T index = current();
		if (index == null) {
			return list;
		}

		T currentIns = index;
		while (true) {
			if (index == null || index.compare(currentIns) != 0) {
				break;
			}

			list.add(currentIns);

			currentIns = next();
		}

		return list;
	}
}
