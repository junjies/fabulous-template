package fabulous.template.util;

/*
 * object T comparison on key attributes
 *  if return value is less than zero, current instance is smaller than obj on key attributes
 *  if return value is larger than zero, current instance is larger than obj on key attributes 
 *  if return value equals zero, current instance equals obj on key attributes
 */
public interface KeyAttsComparable<T> {
	public int compare(T obj);
}
