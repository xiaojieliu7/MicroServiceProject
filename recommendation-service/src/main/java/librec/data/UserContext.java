package librec.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * user-related context entries information
 *
 * @author Keqiang Wang
 */
public class UserContext {
    /**
     * user context entry element data list
     */
    private List<UserContextEntry> userContextEntries;
    /**
     * element data index of userIdx
     */
    private int[] indexOfUserIdx;
    /**
     * the max idx of userIdx
     */
    private int maxUserIdx;
    /**
     * the number of users
     */
    private int size;

    /**
     * Constructs user context list by a map of userContextEntryMap
     * and element index array of userIdx with the specified maximum userIdx
     *
     * @param maxUserIdx          the maximum userIdx
     * @param userContextEntryMap
     * @throws IllegalArgumentException if the maximum userIdx is negative
     */
    public UserContext(int maxUserIdx, Map<Integer, UserContextEntry> userContextEntryMap) {
        if (maxUserIdx < 0) {
            throw new IllegalArgumentException("Illegal maximum userIdx: " + maxUserIdx);
        } else {
            this.maxUserIdx = maxUserIdx;
            indexOfUserIdx = new int[this.maxUserIdx + 1];
            Arrays.fill(indexOfUserIdx, -1);
            userContextEntries = new ArrayList<>(userContextEntryMap.size());
            for (Map.Entry<Integer, UserContextEntry> tempMapEntry : userContextEntryMap.entrySet()) {
                int userIdx = tempMapEntry.getKey();
                UserContextEntry userContextEntry = tempMapEntry.getValue();
                indexOfUserIdx[userIdx] = userContextEntries.size();
                userContextEntries.add(userContextEntry);
            }
        }
    }

    /**
     * Constructs an empty user context list with 0 initial capacity
     * and element index array of userIdx with the specified maximum userIdx
     *
     * @param maxUserIdx the maximum userIdx
     * @throws IllegalArgumentException if the maximum userIdx is negative
     */
    public UserContext(int maxUserIdx) {
        this(maxUserIdx, 0);
    }

    /**
     * Constructs an empty user context list with the specified initial capacity
     * and element index array of userIdx with the specified maximum userIdx
     *
     * @param maxUserIdx      the maximum userIdx
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity is negative
     * @throws IllegalArgumentException if the maximum userIdx is negative
     */
    public UserContext(int maxUserIdx, int initialCapacity) {
        if (maxUserIdx < 0) {
            throw new IllegalArgumentException("Illegal Maximum userIdx: " + maxUserIdx);
        } else if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        } else {
            this.maxUserIdx = maxUserIdx;
            indexOfUserIdx = new int[this.maxUserIdx + 1];
            Arrays.fill(indexOfUserIdx, -1);
            userContextEntries = new ArrayList<>(initialCapacity);
            this.size = 0;
        }
    }

    /**
     * @param userIdx the specified user userIdx is to be tested
     * @throws IndexOutOfBoundsException if the specified user userIdx is larger than the maximum userIdx
     */
    public void rangeCheck(int userIdx) {
        if (userIdx > maxUserIdx) {
            throw new IndexOutOfBoundsException("the userIdx is out of the user index range!");
        }
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified user userIdx.
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param userIdx user userIdx is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     */
    public boolean contains(int userIdx) {
//        System.out.println("uid:"+userIdx);
//        System.out.println("uuuid:"+indexOfUserIdx[userIdx]);
        return indexOfUserIdx[userIdx] >= 0;
    }

    /**
     * return the user context entry of the user userIdx if the userContext contain user userIdx,
     * else throw a IndexOutOfBoundsException
     *
     * @param userIdx user userIdx
     * @return the user context entry of the user userIdx
     * @throws IndexOutOfBoundsException if the user context do not contain user userIdx
     */
    public UserContextEntry getUserContextEntry(int userIdx) {
        if (!contains(userIdx)) {
            throw new IndexOutOfBoundsException("do not contain the context of user userIdx!");
        } else {
            return userContextEntries.get(indexOfUserIdx[userIdx]);
        }
    }

    /**
     * set the user context entry of userIdx
     *
     * @param userIdx          user userIdx
     * @param userContextEntry user context entry
     */
    public void setUserContextEntry(int userIdx, UserContextEntry userContextEntry) {
        rangeCheck(userIdx);
        if (!contains(userIdx)) {
            indexOfUserIdx[userIdx] = userContextEntries.size();
            userContextEntries.add(userContextEntry);
            size++;
        } else {
            userContextEntries.set(indexOfUserIdx[userIdx], userContextEntry);
        }
    }

    /**
     * @return the number of users
     */
    public int getSize() {
        return size;
    }

}
