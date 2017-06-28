// Copyright (C) 2014 Guibing Guo
//
// This file is part of LibRec.
//
// LibRec is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// LibRec is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with LibRec. If not, see <http://www.gnu.org/licenses/>.
//
package librec.data;


import java.util.Set;

/**
 * Item-related Contextual Entry Information
 *
 * @author Keqiang Wang
 */
public class ItemContextEntry {
    /**
     * item name
     */
    private String itemName;
    /**
     * item price
     */
    private float price;
    /**
     * item category idx
     */
    private int categoryIdx;
    /**
     * item description
     */
    private String itemDescription;
    /**
     * item location
     */
    private Location location;

    /**
     * tag list
     */
    private Set<Integer> tagSet;

    /**
     * region or cluster of location
     */
    private int regionId;

    /**
     * @return the price
     */
    public float getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     * @return the text description of this item
     */
    public String getItemDescription() {
        return itemDescription;
    }

    /**
     * @param description the text description of this item to set
     */
    public void setItemDescription(String description) {
        this.itemDescription = description;
    }

    /**
     * @return the name of this item
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @param name the name of this item to set
     */
    public void setItemName(String name) {
        this.itemName = name;
    }

    /**
     * @return the category idx of this item
     */
    public int getCategoryIdx() {
        return categoryIdx;
    }

    /**
     * @param categoryIdx category idx of this item to set
     */
    public void setCategoryIdx(int categoryIdx) {
        this.categoryIdx = categoryIdx;
    }

    /**
     * @return the location of this item
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location location of this item to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the tags set of this item
     */
    public Set<Integer> getTagSet() {
        return tagSet;
    }

    /**
     * @param tagSet the tags set of this item to se
     */
    public void setTagSet(Set<Integer> tagSet) {
        this.tagSet = tagSet;
    }

    /**
     *@return  the region or cluster of location
     */
    public int getRegionId() {
        return regionId;
    }

    /**
     * @param regionId region or cluster of location to set
     */
    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }
}
