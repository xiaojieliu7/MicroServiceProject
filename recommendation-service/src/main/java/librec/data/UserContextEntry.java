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

import java.util.HashSet;
import java.util.Set;

/**
 * User-related Contextual Entry Information
 *
 * @author Keqiang Wang
 */
public class UserContextEntry {
    /**
     * user's social relations
     */
    private Set<Integer> socialSet;

    /**
     * user name
     */
    private String userName;

    /**
     * user age
     */
    private int age;

    /**
     * user gender 1 for man, 0 for female
     */
    private int gender;

    /**
     * user description
     */
    private String userDescription;

    /**
     * add social relations
     *
     * @param userIdx user id socially related with
     */
    public void addSocial(int userIdx) {
        if (socialSet == null)
            socialSet = new HashSet<>();

        socialSet.add(userIdx);
    }

    /**
     * Returns {@code true} if friends set of user contains the friend userIdx
     *
     * @param userIdx of user social friends to search for
     */
    public boolean socialContains(int userIdx) {
        return socialSet.contains(userIdx);
    }

    /**
     * @return the social friends set of user.
     */
    public Set<Integer> getSocialSet() {
        return socialSet;
    }

    /**
     * @return user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * set userName as the name of this user context entry
     *
     * @param userName user name of this user context entry
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the age of this user
     */
    public int getAge() {
        return age;
    }

    /**
     * set the age of this user
     *
     * @param age the age of this user
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @return the gender of this user
     */
    public int getGender() {
        return gender;
    }

    /**
     * set the gender of this user
     *
     * @param gender the gender of this user
     */
    public void setGender(int gender) {
        this.gender = gender;
    }

    /**
     * @return the text description of this user
     */
    public String getUserDescription() {
        return userDescription;
    }

    /**
     * set the text description of this user
     *
     * @param userDescription the text description of this user
     */
    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

}
