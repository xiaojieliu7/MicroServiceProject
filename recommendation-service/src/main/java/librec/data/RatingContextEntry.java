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

import java.util.ArrayList;
import java.util.List;

/**
 * Rating-related Context Entry Information
 *
 * @author Keqiang Wang
 */
public class RatingContextEntry {
    /**
     * rating time stamp, we prefer long to Date or Timestamp for computational convenience
     */
    private long timestamp;

    /**
     * rating time stamp list
     */
    private List<Long> timestampList;

    /**
     * mood when giving rating
     */
    private String mood;

    public RatingContextEntry() {

    }

    /**
     * @return the mood
     */
    public String getMood() {
        return mood;
    }

    /**
     * @param mood the mood to set
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * @return the timestamp in million seconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp in million seconds
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return timestamp list
     */
    public List<Long> getTimestampList() {
        if (timestampList == null) {
            timestampList = new ArrayList<>();
        }
        return timestampList;
    }

    /**
     * @param timestampList timestamp list of this rating to set
     */
    public void setTimestampList(List<Long> timestampList) {
        this.timestampList = timestampList;
    }
}
