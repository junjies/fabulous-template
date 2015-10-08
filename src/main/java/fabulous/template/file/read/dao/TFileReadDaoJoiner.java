package fabulous.template.file.read.dao;

import java.io.IOException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import fabulous.template.util.KeyAttsComparable;

public class TFileReadDaoJoiner<T1 extends KeyAttsComparable<T2>, T2> {
	private final TFileReadDao<T1> t1Dao;
	private final TFileReadDao<T2> t2Dao;

	public TFileReadDaoJoiner(TFileReadDao<T1> t1Dao, TFileReadDao<T2> t2Dao) {
		this.t1Dao = t1Dao;
		this.t2Dao = t2Dao;
	}

	public ImmutablePair<T1, T2> getPair() throws IOException {
		if (t1Dao == null || t2Dao == null) {
			return null;
		}

		T1 t1 = t1Dao.current();
		T2 t2 = t2Dao.current();

		while (true) {
			if (t1 == null || t2 == null) {
				return null;
			}

			int compareResult = t1.compare(t2);
			if (compareResult > 0) {
				t2Dao.next();
				t2 = t2Dao.current();
				continue;
			} else if (compareResult < 0) {
				t1Dao.next();
				t1 = t1Dao.current();
				continue;
			} else {
				t1Dao.next();
				t2Dao.next();
				return new ImmutablePair<T1, T2>(t1, t2);
			}
		}
	}

	public void close() {
		t1Dao.close();
		t2Dao.close();
	}
}
