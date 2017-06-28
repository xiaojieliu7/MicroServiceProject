package librec.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;

/**
 * rating(user-item pair) context entries information
 *
 * @author Keqiang Wang
 */
public class RatingContext {
    Table<Integer, Integer, RatingContextEntry> ratingContextEntryTable;

    /**
     * Constructs rating context table by a table of ratingContextEntryTable
     *
     * @param ratingContextEntryTable
     */
    public RatingContext(Table<Integer, Integer, RatingContextEntry> ratingContextEntryTable) {
        this.ratingContextEntryTable = ratingContextEntryTable;
    }

    /**
     * Constructs a empty rating context table
     */
    public RatingContext() {
        this.ratingContextEntryTable = HashBasedTable.create();
    }

    /**
     * Returns {@code true} if the table contains a mapping with the specified
     * row key.
     *
     * @param userIdx key of row to search for
     */
    public boolean containsUser(int userIdx) {
        return ratingContextEntryTable.containsRow(userIdx);
    }

    /**
     * Returns {@code true} if the table contains a mapping with the specified
     * column.
     *
     * @param itemIdx key of column to search for
     */
    public boolean containsItem(int itemIdx) {
        return ratingContextEntryTable.containsColumn(itemIdx);
    }

    /**
     * Returns {@code true} if the table contains a mapping with the specified
     * row and column keys.
     *
     * @param userIdx key of row to search for
     * @param itemIdx key of column to search for
     */
    public boolean containsRating(int userIdx, int itemIdx) {
        return ratingContextEntryTable.contains(userIdx, itemIdx);
    }

    /**
     * Returns a view of all mappings that have the given row key. For each row
     * key / column key / value mapping in the table with that row key, the
     * returned map associates the column key with the value. If no mappings in
     * the table have the provided row key, an empty map is returned.
     * <p>
     * <p>Changes to the returned map will update the underlying table, and vice
     * versa.
     *
     * @param userIdx key of row to search for in the table
     * @return the corresponding map from column keys to values
     */
    public Map<Integer, RatingContextEntry> getItemRatingContextByUser(int userIdx) {
        return ratingContextEntryTable.row(userIdx);
    }

    /**
     * Returns a view of all mappings that have the given column key. For each row
     * key / column key / value mapping in the table with that column key, the
     * returned map associates the row key with the value. If no mappings in the
     * table have the provided column key, an empty map is returned.
     * <p>
     * <p>Changes to the returned map will update the underlying table, and vice
     * versa.
     *
     * @param itemIdx key of column to search for in the table
     * @return the corresponding map from row keys to values
     */
    public Map<Integer, RatingContextEntry> getUserRatingContextByItem(int itemIdx) {
        return ratingContextEntryTable.column(itemIdx);
    }

    /**
     * Returns the value corresponding to the given row and column keys, or
     * {@code null} if no such mapping exists.
     *
     * @param userIdx key of row to search for
     * @param itemIdx key of column to search for
     */
    public RatingContextEntry getRatingContextEntry(int userIdx, int itemIdx) {
        return ratingContextEntryTable.get(userIdx, itemIdx);
    }

    /**
     * Associates the specified value with the specified keys. If the table
     * already contained a mapping for those keys, the old value is replaced with
     * the specified value.
     *
     * @param userIdx            row key that the value should be associated with
     * @param itemIdx            column key that the value should be associated with
     * @param ratingContextEntry value to be associated with the specified keys
     * @return the value previously associated with the keys, or {@code null} if
     * no mapping existed for the keys
     */
    public void putRatingContextEntry(int userIdx, int itemIdx, RatingContextEntry ratingContextEntry) {
        ratingContextEntryTable.put(userIdx, itemIdx, ratingContextEntry);
    }
}
