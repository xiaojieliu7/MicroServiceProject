package librec.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * item-related context entries information
 *
 * @author Keqiang Wang
 */
public class ItemContext {
    /**
     * item context element data list
     */
    private List<ItemContextEntry> itemContextEntries;
    /**
     * element data index of itemIdx
     */
    private int[] indexOfItemIdx;
    /**
     * the maximum item idx
     */
    private int maxItemIdx;
    /**
     * the number of items
     */
    private int size;

    /**
     * the number of region
     */
    private int numRegions;

    /**
     * the number of tag
     */
    private int numTags;

    /**
     * the number of catrgory
     */
    private int numCategorys;

    /**
     * Constructs item context list by a map of userContextEntryMap
     * and element index array of itemIdx with the specified maximum itemIdx
     *
     * @param maxItemIdx          the maximum itemIdx
     * @param itemContextEntryMap item context entry map to set
     * @throws IllegalArgumentException if the maximum itemIdx is negative
     */
    public ItemContext(int maxItemIdx, Map<Integer, ItemContextEntry> itemContextEntryMap) {
        if (maxItemIdx < 0) {
            throw new IllegalArgumentException("Illegal Maximum itemIdx: " + maxItemIdx);
        } else {
            this.maxItemIdx = maxItemIdx;
            this.indexOfItemIdx = new int[this.maxItemIdx + 1];
            Arrays.fill(indexOfItemIdx, -1);
            this.size = 0;
            this.itemContextEntries = new ArrayList<>(itemContextEntryMap.size());
            for (Map.Entry<Integer, ItemContextEntry> tempMapEntry : itemContextEntryMap.entrySet()) {
                int itemIdx = tempMapEntry.getKey();
                ItemContextEntry itemContextEntry = tempMapEntry.getValue();
                setItemContextEntry(itemIdx, itemContextEntry);
            }
        }
    }

    /**
     * Constructs an empty item context list with the specified initial capacity
     * and element index array of userIdx with the specified maximum itemIdx
     *
     * @param maxItemIdx the maximum itemIdx
     * @throws IllegalArgumentException if the maximum itemIdx is negative
     */
    public ItemContext(int maxItemIdx) {
        this(maxItemIdx, 0);
    }

    /**
     * Constructs an empty item context list with the specified initial capacity
     * and element index array of userIdx with the specified maximum itemIdx
     *
     * @param maxItemIdx      the maximum itemIdx
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity is negative
     * @throws IllegalArgumentException if the maximum itemIdx is negative
     */
    public ItemContext(int maxItemIdx, int initialCapacity) {
        if (maxItemIdx < 0) {
            throw new IllegalArgumentException("Illegal Maximum itemIdx: " + maxItemIdx);
        } else if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        } else {
            this.maxItemIdx = maxItemIdx;
            indexOfItemIdx = new int[this.maxItemIdx + 1];
            Arrays.fill(indexOfItemIdx, -1);
            itemContextEntries = new ArrayList<>(initialCapacity);
            this.size = 0;
        }
    }

    /**
     * @param itemIdx the specified item itemIdx is to be tested
     * @throws IndexOutOfBoundsException if the specified item itemIdx is larger than the maximum itemIdx.
     */
    public void rangeCheck(int itemIdx) {
        if (itemIdx > maxItemIdx) {
            throw new IndexOutOfBoundsException("the itemIdx is out of the item index range!");
        }
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified item itemIdx.
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param itemIdx item itemIdx is to be tested
     * @return <tt>true</tt> if this set contains the specified element item itemIdx
     */
    public boolean contains(int itemIdx) {
        rangeCheck(itemIdx);
        return indexOfItemIdx[itemIdx] >= 0;
    }

    /**
     * return the item context entry of the item itemIdx if the itemContext contain item itemIdx,
     * else throw a IndexOutOfBoundsException
     *
     * @param itemIdx item itemIdx
     * @return the item context entry of the item userIdx
     * @throws IndexOutOfBoundsException if the item context do not contain item itemIdx
     */
    public ItemContextEntry getItemContextEntry(int itemIdx) {
        if (!contains(itemIdx)) {
            throw new IndexOutOfBoundsException("do not contain the context of item itemIdx!");
        } else {
            return itemContextEntries.get(indexOfItemIdx[itemIdx]);
        }
    }

    /**
     * set the item context entry of itemIdx with itemContextEntry
     *
     * @param itemIdx          item  itemIdx to set
     * @param itemContextEntry item context entry to set of item itemIdx
     */
    public void setItemContextEntry(int itemIdx, ItemContextEntry itemContextEntry) {
        rangeCheck(itemIdx);
        if (!contains(itemIdx)) {
            indexOfItemIdx[itemIdx] = itemContextEntries.size();
            itemContextEntries.add(itemContextEntry);
            size++;
        } else {
            itemContextEntries.set(indexOfItemIdx[itemIdx], itemContextEntry);
        }
    }

    /**
     * @return the number of items
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the number of regions
     */
    public int getNumRegions() {
        return numRegions;
    }

    /**
     * @param numRegions the number of regions to set
     */
    public void setNumRegions(int numRegions) {
        this.numRegions = numRegions;
    }

    /**
     * @return the number of tags
     */
    public int getNumTags() {
        return numTags;
    }

    /**
     * @param numTags the number of tags to set
     */
    public void setNumTags(int numTags) {
        this.numTags = numTags;
    }

    /**
     * @return the number of tags
     */
    public int getNumCategorys() {
        return numCategorys;
    }

    /**
     * @param numCategorys the number of tags to set
     */
    public void setNumCategorys(int numCategorys) {
        this.numCategorys = numCategorys;
    }
}
