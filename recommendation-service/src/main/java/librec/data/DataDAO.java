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

import com.google.common.collect.*;
import librec.util.*;
import service.Application;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A data access object (DAO) to a data file
 *
 * @author guoguibing
 */
public class DataDAO {

    // name of data file
    private String dataName;
    // directory of data file
    private String dataDir;
    // path to data file
    private String dataPath;
    // store rate data as {user, item, rate} matrix
    private SparseMatrix rateMatrix;
    // store time data as {user, item, timestamp} matrix
    private SparseMatrix timeMatrix;
    // store rate data as a sparse tensor
    private SparseTensor rateTensor;

    // is item type as user
    private boolean isItemAsUser;
    // is first head line
    private boolean isHeadline = false;

    // data scales
    private List<Double> ratingScale;
    // scale distribution
    private Multiset<Double> scaleDist;

    // number of rates
    private int numRatings;

    // user/item {raw id, inner id} map
    private BiMap<String, Integer> userIds, itemIds, categoryIds, tagIds, regionIds;

    // inverse views of userIds, itemIds, categoryIds
    private BiMap<Integer, String> idUsers, idItems, idCategories, idTags, idRegions;

    // time unit may depend on data sets, e.g. in MovieLens, it is unix seconds
    private TimeUnit timeUnit;

    // minimum/maximum rating timestamp
    private long minTimestamp, maxTimestamp;

    /**
     * user context entries
     */
    private UserContext userContext;

    /**
     * item context entries
     */
    private ItemContext itemContext;

    /**
     * rating(user-item pair) element context entries
     */
    private RatingContext ratingContext;

    /**
     * Constructor for a data DAO object
     *
     * @param path    path to data file
     * @param userIds user: {raw id, inner id} map
     * @param itemIds item: {raw id, inner id} map
     */
    public DataDAO(String path, BiMap<String, Integer> userIds, BiMap<String, Integer> itemIds) {
        dataPath = path;

        if (userIds == null)
            this.userIds = HashBiMap.create();
        else
            this.userIds = userIds;

        if (itemIds == null)
            this.itemIds = HashBiMap.create();
        else
            this.itemIds = itemIds;

        scaleDist = HashMultiset.create();

        isItemAsUser = this.userIds == this.itemIds;
        timeUnit = TimeUnit.SECONDS;
    }

    /**
     * Contructor for data DAO object
     *
     * @param path path to data file
     */
    public DataDAO(String path) {
        this(path, null, null);
    }

    /**
     * Contructor for data DAO object
     */
    public DataDAO(String path, BiMap<String, Integer> userIds) {
        this(path, userIds, userIds);
    }

    /**
     * Default relevant columns {0: user column, 1: item column, 2: rate column}; default recommendation task is rating
     * prediction;
     *
     * @return a sparse matrix storing all the relevant data
     */
    public SparseMatrix[] readData() throws Exception {
        return readData(new int[]{0, 1, 2}, -1);
    }

    /**
     * whether to construct CCS structures while reading data
     */
    public SparseMatrix[] readData(double binThold) throws Exception {
        return readData(new int[]{0, 1, 2}, binThold);
    }

    /**
     * Read data from the data file. Note that we didn't take care of the duplicated lines.
     *
     * @param cols     the indexes of the relevant columns in the data file: {user, item, [rating, timestamp] (optional)}
     * @param binThold the threshold to binarize a rating. If a rating is greater than the threshold, the value will be 1;
     *                 otherwise 0. To disable this feature, i.e., keep the original rating value, set the threshold a
     *                 negative value
     * @return a sparse matrix storing all the relevant data
     */
    public SparseMatrix[] readData(int[] cols, double binThold) throws Exception {

        Logs.info(String.format("Dataset: %s", Strings.last(dataPath, 38)));

        // Table {row-id, col-id, rate}
        Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
        // Table {row-id, col-id, timestamp}
        Table<Integer, Integer, Long> timeTable = null;
        // Map {col-id, multiple row-id}: used to fast build a rating matrix
        Multimap<Integer, Integer> colMap = HashMultimap.create();

        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(dataPath)));
        String line = null;
        minTimestamp = Long.MAX_VALUE;
        maxTimestamp = Long.MIN_VALUE;
        while ((line = br.readLine()) != null) {
            if (isHeadline()) {
                setHeadline(false);
                continue;
            }

            String[] data = line.trim().split("[ \t,]+");

            String user = data[cols[0]];
            String item = data[cols[1]];
            Double rate = (cols.length >= 3 && data.length >= 3) ? Double.valueOf(data[cols[2]]) : 1.0;

            // binarize the rating for item recommendation task
            if (binThold >= 0)
                rate = rate > binThold ? 1.0 : 0.0;

            scaleDist.add(rate);

            // inner id starting from 0
            int row = userIds.containsKey(user) ? userIds.get(user) : userIds.size();
            userIds.put(user, row);

            int col = itemIds.containsKey(item) ? itemIds.get(item) : itemIds.size();
            itemIds.put(item, col);

            dataTable.put(row, col, rate);
            colMap.put(col, row);

            // record rating's issuing time
            if (cols.length >= 4 && data.length >= 4) {
                if (timeTable == null)
                    timeTable = HashBasedTable.create();

                // convert to million-seconds
                long mms = 0L;
                try {
                    mms = Long.parseLong(data[cols[3]]); // cannot format "9.7323480e+008"
                } catch (NumberFormatException e) {
                    mms = (long) Double.parseDouble(data[cols[3]]);
                }
                long timestamp = timeUnit.toMillis(mms);

                if (minTimestamp > timestamp)
                    minTimestamp = timestamp;

                if (maxTimestamp < timestamp)
                    maxTimestamp = timestamp;

                timeTable.put(row, col, timestamp);
            }

        }
        br.close();

        numRatings = scaleDist.size();
        ratingScale = new ArrayList<>(scaleDist.elementSet());
        Collections.sort(ratingScale);

        int numRows = numUsers(), numCols = numItems();

        // if min-rate = 0.0, shift upper a scale
        double minRate = ratingScale.get(0).doubleValue();
        double epsilon = minRate == 0.0 ? ratingScale.get(1).doubleValue() - minRate : 0;
        if (epsilon > 0) {
            // shift upper a scale
            for (int i = 0, im = ratingScale.size(); i < im; i++) {
                double val = ratingScale.get(i);
                ratingScale.set(i, val + epsilon);
            }
            // update data table
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    if (dataTable.contains(row, col))
                        dataTable.put(row, col, dataTable.get(row, col) + epsilon);
                }
            }
        }

        String dateRange = "";
        if (cols.length >= 4)
            dateRange = String.format(", Timestamps = {%s, %s}", Dates.toString(minTimestamp),
                    Dates.toString(maxTimestamp));

        Logs.debug("With Specs: {Users, {}} = {{}, {}, {}}, Scale = {{}}{}", (isItemAsUser ? "Users, Links"
                : "Items, Ratings"), numRows, numCols, numRatings, Strings.toString(ratingScale), dateRange);

        // build rating matrix
        rateMatrix = new SparseMatrix(numRows, numCols, dataTable, colMap);

        if (timeTable != null)
            timeMatrix = new SparseMatrix(numRows, numCols, timeTable, colMap);

        // release memory of data table
        dataTable = null;
        timeTable = null;

        return new SparseMatrix[]{rateMatrix, timeMatrix};
    }

    /**
     * get the social information of users
     *
     * @param cols       the indexes of the relevant columns in the data file: {user, user}
     * @param socialPath social data path
     * @return social  information
     * @throws IOException
     */
    public UserContext readUserSocial(int[] cols, String socialPath) throws IOException {
        if (userContext == null) {
            userContext = new UserContext(numUsers() - 1);
            System.out.println("numu:" + numUsers());
        }
        BufferedReader br = FileIO.getReader(socialPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split("[ \t,]+");
            if (data.length < 2) {
                continue;
            }
            String user = data[cols[0]];
            String socialUser = data[cols[1]];
            if (userIds.containsKey(user) && userIds.containsKey(socialUser)) {
                int userIdx = userIds.get(user);
                UserContextEntry userContextEntry;
                if (userContext.contains(userIdx)) {
                    userContextEntry = userContext.getUserContextEntry(userIdx);
                } else {
                    userContextEntry = new UserContextEntry();
                }
                int socialUserIdx = userIds.get(socialUser);
                userContextEntry.addSocial(socialUserIdx);
                userContext.setUserContextEntry(userIdx, userContextEntry);
            }
        }
        return userContext;
    }

    /**
     * get the text description of users
     *
     * @param cols            the indexes of the relevant columns in the data file: {user,description}
     * @param descriptionPath the text description data path
     * @return user the text description information
     * @throws IOException
     */
    public UserContext readUserDescription(int[] cols, String descriptionPath) throws IOException {
        if (userContext == null) {
            userContext = new UserContext(numItems() - 1);
        }
        BufferedReader br = FileIO.getReader(descriptionPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split(" ## ");
            if (data.length < 2) {
                continue;
            }
            String user = data[cols[0]];
            if (userIds.containsKey(user)) {
                int userIdx = userIds.get(user);
                UserContextEntry userContextEntry;
                if (userContext.contains(userIdx)) {
                    userContextEntry = userContext.getUserContextEntry(userIdx);
                } else {
                    userContextEntry = new UserContextEntry();
                }

                userContextEntry.setUserDescription(data[cols[1]]);
                userContext.setUserContextEntry(userIdx, userContextEntry);
            }
        }
        return userContext;
    }

    /**
     * get the location information of items
     *
     * @param cols             the indexes of the relevant columns in the data file: {item, latitude, longitude}
     * @param locationDataPath location data path
     * @return item location information(latitude, longitude)
     * @throws IOException
     */
    public ItemContext readItemLocation(int[] cols, String locationDataPath) throws IOException {
        if (itemContext == null) {
            itemContext = new ItemContext(numItems() - 1);
        }
        BufferedReader br = FileIO.getReader(locationDataPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split("[ \t,]+");
            if (data.length != 3) {
                throw new IOException("the location(latitude, longitude) error");
            }
            String item = data[cols[0]];//item是raw id
            double latitude = Double.parseDouble(data[cols[1]]);
            double longitude = Double.parseDouble(data[cols[2]]);
            Location location = new Location(latitude, longitude);
            if (itemIds.containsKey(item)) {//itemIds是rawid 到innerid的映射
                int itemIdx = itemIds.get(item);//itemIdx是innerid
                ItemContextEntry itemContextEntry;
                if (itemContext.contains(itemIdx)) {
                    itemContextEntry = itemContext.getItemContextEntry(itemIdx);
                } else {
                    itemContextEntry = new ItemContextEntry();
                }
                itemContextEntry.setLocation(location);
                itemContext.setItemContextEntry(itemIdx, itemContextEntry);
            }
        }
        return itemContext;
    }

    /**
     * get the tags information of items
     *
     * @param cols     the indexes of the relevant columns in the data file: {item}
     * @param tagsPath tags data path
     * @return item tags information
     * @throws IOException
     */
    public ItemContext readItemTags(int[] cols, String tagsPath) throws IOException {
        if (itemContext == null) {
            itemContext = new ItemContext(numItems() - 1);
        }
        tagIds = HashBiMap.create();

        BufferedReader br = FileIO.getReader(tagsPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split(" ## ");
            if (data.length < 2) {
                continue;
            }
            String item = data[cols[0]];

            if (itemIds.containsKey(item)) {
                int itemIdx = itemIds.get(item);
                ItemContextEntry itemContextEntry;
                if (itemContext.contains(itemIdx)) {
                    itemContextEntry = itemContext.getItemContextEntry(itemIdx);
                } else {
                    itemContextEntry = new ItemContextEntry();
                }

                Set<Integer> tagSet = new HashSet<>();
                for (int dataIdx = 1; dataIdx < data.length; ++dataIdx) {
                    int tagIdx = tagIds.containsKey(data[dataIdx]) ? tagIds.get(data[dataIdx]) : tagIds.size();
                    tagIds.putIfAbsent(data[dataIdx], tagIdx);
                    tagSet.add(tagIdx);
                }
                itemContextEntry.setTagSet(tagSet);
                itemContext.setItemContextEntry(itemIdx, itemContextEntry);
            }
        }
        itemContext.setNumTags(tagIds.size());
        return itemContext;
    }

    /**
     * get the text description of items
     *
     * @param cols            the indexes of the relevant columns in the data file: {item,description}
     * @param descriptionPath the text description data path
     * @return item the text description information
     * @throws IOException
     */
    public ItemContext readItemDescription(int[] cols, String descriptionPath) throws IOException {
        if (itemContext == null) {
            itemContext = new ItemContext(numItems() - 1);
        }

        BufferedReader br = FileIO.getReader(descriptionPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split(" ## ");
            if (data.length < 2) {
                continue;
            }
            String item = data[cols[0]];

            if (itemIds.containsKey(item)) {
                int itemIdx = itemIds.get(item);
                ItemContextEntry itemContextEntry;
                if (itemContext.contains(itemIdx)) {
                    itemContextEntry = itemContext.getItemContextEntry(itemIdx);
                } else {
                    itemContextEntry = new ItemContextEntry();
                }

                itemContextEntry.setItemDescription(data[cols[1]]);
                itemContext.setItemContextEntry(itemIdx, itemContextEntry);
            }
        }
        return itemContext;
    }

    /**
     * get the category of items
     *
     * @param cols         the indexes of the relevant columns in the data file: {item,category}
     * @param categoryPath the region data path
     * @return category information of items
     * @throws IOException
     */
    public ItemContext readItemCategory(int[] cols, String categoryPath) throws IOException {
        if (itemContext == null) {
            itemContext = new ItemContext(numItems() - 1);
        }

        categoryIds = HashBiMap.create();

        BufferedReader br = FileIO.getReader(categoryPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split(" ## ");
            if (data.length < 2) {
                continue;
            }
            String item = data[cols[0]];

            if (itemIds.containsKey(item)) {
                int itemIdx = itemIds.get(item);
                ItemContextEntry itemContextEntry;
                if (itemContext.contains(itemIdx)) {
                    itemContextEntry = itemContext.getItemContextEntry(itemIdx);
                } else {
                    itemContextEntry = new ItemContextEntry();
                }
                String category;
                if (data.length > 4)
                    category = data[3];
                else
                    category = data[data.length - 1];
                int categoryIdx = categoryIds.containsKey(category) ? categoryIds.get(category) : categoryIds.size();
                categoryIds.putIfAbsent(category, categoryIdx);
                itemContextEntry.setCategoryIdx(categoryIdx);
                itemContext.setItemContextEntry(itemIdx, itemContextEntry);
            }
        }
        itemContext.setNumCategorys(categoryIds.size());
        return itemContext;
    }


    /**
     * get the region information of items
     *
     * @param cols       the indexes of the relevant columns in the data file: {item,region}
     * @param regionPath the region data path
     * @return region information of items
     * @throws IOException
     */
    public ItemContext readItemRegionInfo(int[] cols, String regionPath) throws IOException {
        if (itemContext == null) {
            itemContext = new ItemContext(numItems() - 1);
        }

        regionIds = HashBiMap.create();

        BufferedReader br = FileIO.getReader(regionPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split("[ \t,]+");
            if (data.length < 2) {
                continue;
            }
            String item = data[cols[0]];

            if (itemIds.containsKey(item)) {
                int itemIdx = itemIds.get(item);
                ItemContextEntry itemContextEntry;
                if (itemContext.contains(itemIdx)) {
                    itemContextEntry = itemContext.getItemContextEntry(itemIdx);
                } else {
                    itemContextEntry = new ItemContextEntry();
                }
                String region = data[cols[1]];
                int regionIdx = regionIds.containsKey(region) ? regionIds.get(region) : regionIds.size();
                regionIds.putIfAbsent(region, regionIdx);
                itemContextEntry.setRegionId(regionIdx);
                itemContext.setItemContextEntry(itemIdx, itemContextEntry);
            }
        }
        itemContext.setNumRegions(regionIds.size());
        return itemContext;
    }


    /**
     * get the timestamp list information of user-item pair
     *
     * @param cols              the indexes of the relevant columns in the data file: {user, item, timestamp}
     * @param timestampListPath timestamp file path
     * @return rating(user-item pair) context information
     * @throws IOException
     */
    public RatingContext readRatingsTimestampList(int[] cols, String timestampListPath) throws IOException {
        if (ratingContext == null) {
            ratingContext = new RatingContext();
        }

        BufferedReader br = FileIO.getReader(timestampListPath);
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.trim().split("[ \t,]+");
            if (data.length < 2) {
                continue;
            }
            String user = data[cols[0]];
            String item = data[cols[1]];

            if (itemIds.containsKey(item) && userIds.containsKey(user)) {
                int userIdx = userIds.get(user);
                int itemIdx = itemIds.get(item);

                RatingContextEntry ratingContextEntry;
                if (ratingContext.containsRating(userIdx, itemIdx)) {
                    ratingContextEntry = ratingContext.getRatingContextEntry(userIdx, itemIdx);
                } else {
                    ratingContextEntry = new RatingContextEntry();
                }
                long time = Long.parseLong(data[cols[3]]);
                ratingContextEntry.getTimestampList().add(time);
            }
        }
        return ratingContext;
    }


    /**
     * Read data from the data file. Note that we didn't take care of the duplicated lines.
     *
     * @param cols     the indexes of the relevant columns in the data file: {user, item, rating}, other columns are treated
     *                 as features
     * @param binThold the threshold to binarize a rating. If a rating is greater than the threshold, the value will be 1;
     *                 otherwise 0. To disable this feature, i.e., keep the original rating value, set the threshold a
     *                 negative value
     * @return a sparse tensor storing all the relevant data
     */
    @SuppressWarnings("unchecked")
    public SparseMatrix[] readTensor(int[] cols, double binThold) throws Exception {

        if (cols.length < 3)
            throw new Exception("Column length cannot be smaller than 3. Usage: user, item, rating columns.");

        Logs.info(String.format("Dataset: %s", Strings.last(dataPath, 38)));

        int[] dims = null;
        int numDims = 0;
        List<Integer>[] ndLists = null;
        Set<Integer>[] ndSets = null;
        List<Double> vals = new ArrayList<Double>();

        BufferedReader br = FileIO.getReader(dataPath);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (isHeadline()) {
                setHeadline(false);
                continue;
            }

            String[] data = line.trim().split("[ \t,]+");

            // initialization
            if (dims == null) {
                numDims = data.length - 1;
                dims = new int[numDims];
                ndLists = (List<Integer>[]) new List<?>[numDims];
                ndSets = (Set<Integer>[]) new Set<?>[numDims];
                for (int d = 0; d < numDims; d++) {
                    ndLists[d] = new ArrayList<Integer>();
                    ndSets[d] = new HashSet<Integer>();
                }
            }

            // set data
            for (int d = 0; d < data.length; d++) {
                String val = data[d];
                int feature = -1;

                if (d == cols[0]) {
                    // user
                    feature = userIds.containsKey(val) ? userIds.get(val) : userIds.size();
                    userIds.put(val, feature);

                } else if (d == cols[1]) {
                    // item
                    feature = itemIds.containsKey(val) ? itemIds.get(val) : itemIds.size();
                    itemIds.put(val, feature);

                } else if (d == cols[2]) {
                    // rating
                    double rate = Double.parseDouble(val);

                    // binarize the rating for item recommendation task
                    if (binThold >= 0)
                        rate = rate > binThold ? 1.0 : 0.0;

                    vals.add(rate);
                    scaleDist.add(rate);

                    continue;
                } else {
                    // other: val as feature value
                    feature = val.equalsIgnoreCase("na") ? 0 : Integer.parseInt(val);
                }

                int dim = d > cols[2] ? d - 1 : d;
                ndLists[dim].add(feature);
                ndSets[dim].add(feature);
            }
        }
        br.close();

        numRatings = scaleDist.size();
        ratingScale = new ArrayList<>(scaleDist.elementSet());
        Collections.sort(ratingScale);

        // if min-rate = 0.0, shift upper a scale
        double minRate = ratingScale.get(0).doubleValue();
        double epsilon = minRate == 0.0 ? ratingScale.get(1).doubleValue() - minRate : 0;
        if (epsilon > 0) {
            // shift upper a scale
            for (int i = 0, im = ratingScale.size(); i < im; i++) {
                double val = ratingScale.get(i);
                ratingScale.set(i, val + epsilon);
            }
            // update rating values
            for (int i = 0; i < vals.size(); i++) {
                vals.set(i, vals.get(i) + epsilon);
            }
        }

        // get dimensions
        int numRows = numUsers(), numCols = numItems();
        for (int d = 0; d < numDims; d++) {
            dims[d] = ndSets[d].size();
        }

        // debug info
        Logs.debug("With Specs: {Users, Items, Ratings, Features} = {{}, {}, {}, {}}, Scale = {{}}", numRows, numCols,
                numRatings, (numDims - 2), Strings.toString(ratingScale));

        rateTensor = new SparseTensor(dims, ndLists, vals);
        rateTensor.setUserDimension(cols[0]);
        rateTensor.setItemDimension(cols[1]);

        return new SparseMatrix[]{rateTensor.rateMatrix(), null};
    }

    /**
     * @param toPath the data file to write to
     * @param sep    the sparator of the written data file
     */
    public void writeData(String toPath, String sep) throws Exception {
        FileIO.deleteFile(toPath);

        List<String> lines = new ArrayList<>(1500);
        for (MatrixEntry me : rateMatrix) {
            String line = Strings.toString(new Object[]{me.row() + 1, me.column() + 1, (float) me.get()}, sep);
            lines.add(line);

            if (lines.size() >= 1000) {
                FileIO.writeList(toPath, lines, null, true);
                lines.clear();
            }
        }

        if (lines.size() > 0)
            FileIO.writeList(toPath, lines, null, true);

        Logs.debug("Data has been exported to {}", toPath);
    }

    /**
     * Default sep=" " is adopted
     */
    public void writeData(String toPath) throws Exception {
        writeData(toPath, " ");
    }

    /**
     * Write rate matrix to a data file with format ".arff" which can be used by the PREA toolkit
     *
     * @param relation relation name of dataset
     * @param toPath   data file path
     */
    public void writeArff(String relation, String toPath) throws Exception {
        FileIO.deleteFile(toPath);

        BufferedWriter bw = FileIO.getWriter(toPath);

        bw.write("@RELATION " + relation + "\n\n");
        bw.write("@ATTRIBUTE UserId NUMERIC\n\n");
        bw.write("@DATA\n");

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int u = 0, um = numUsers(); u < um; u++) {
            sb.append("{0 " + (u + 1));

            for (int j = 0, jm = numItems(); j < jm; j++) {
                double rate = rateMatrix.get(u, j);
                if (rate != 0)
                    sb.append(", " + (j + 1) + " " + rate);

                if (j == jm - 1)
                    sb.append("}\n");
            }

            if (count++ >= 500) {
                bw.write(sb.toString());
                count = 0;
                sb = new StringBuilder();
            }
        }

        if (count > 0)
            bw.write(sb.toString());

        bw.close();

        Logs.debug("Data has been exported to {}", toPath);
    }

    /**
     * print out specifications of the dataset
     */
    public void printSpecs() throws Exception {
        if (rateMatrix == null)
            readData();

        List<String> sps = new ArrayList<>();

        int users = numUsers();
        int items = numItems();
        int numRates = rateMatrix.size();

        sps.add(String.format("Dataset: %s", dataPath));
        sps.add("User amount: " + users + ", " + FileIO.formatSize(users));
        if (!isItemAsUser)
            sps.add("Item amount: " + items + ", " + FileIO.formatSize(items));
        sps.add("Rate amount: " + numRates + ", " + FileIO.formatSize(numRates));
        sps.add(String.format("Data density: %.4f%%", (numRates + 0.0) / users / items * 100));
        sps.add("Scale distribution: " + scaleDist.toString());

        // user/item mean
        double[] data = rateMatrix.getData();
        float mean = (float) (Stats.sum(data) / numRates);
        float std = (float) Stats.sd(data);
        float mode = (float) Stats.mode(data);
        float median = (float) Stats.median(data);

        sps.add("");
        sps.add(String.format("Average value of all ratings: %f", mean));
        sps.add(String.format("Standard deviation of all ratings: %f", std));
        sps.add(String.format("Mode of all rating values: %f", mode));
        sps.add(String.format("Median of all rating values: %f", median));

        List<Integer> userCnts = new ArrayList<>();
        int userMax = 0, userMin = Integer.MAX_VALUE;
        for (int u = 0, um = numUsers(); u < um; u++) {
            int size = rateMatrix.rowSize(u);
            if (size > 0) {
                userCnts.add(size);

                if (size > userMax)
                    userMax = size;
                if (size < userMin)
                    userMin = size;
            }
        }

        sps.add("");
        sps.add(String.format("Max number of ratings per user: %d", userMax));
        sps.add(String.format("Min number of ratings per user: %d", userMin));
        sps.add(String.format("Average number of ratings per user: %f", (float) Stats.mean(userCnts)));
        sps.add(String.format("Standard deviation of number of ratings per user: %f", (float) Stats.sd(userCnts)));

        if (!isItemAsUser) {
            List<Integer> itemCnts = new ArrayList<>();
            int itemMax = 0, itemMin = Integer.MAX_VALUE;
            for (int j = 0, jm = numItems(); j < jm; j++) {
                int size = rateMatrix.columnSize(j);
                if (size > 0) {
                    itemCnts.add(size);

                    if (size > itemMax)
                        itemMax = size;
                    if (size < itemMin)
                        itemMin = size;
                }
            }

            sps.add("");
            sps.add(String.format("Max number of ratings per item: %d", itemMax));
            sps.add(String.format("Min number of ratings per item: %d", itemMin));
            sps.add(String.format("Average number of ratings per item: %f", (float) Stats.mean(itemCnts)));
            sps.add(String.format("Standard deviation of number of ratings per item: %f", (float) Stats.sd(itemCnts)));
        }

        Logs.info(Strings.toSection(sps));
    }

    /**
     * print out distributions of the dataset <br/>
     * <p>
     * <ul>
     * <li>#users (y) -- #ratings (x) (that are issued by each user)</li>
     * <li>#items (y) -- #ratings (x) (that received by each item)</li>
     * </ul>
     */
    public void printDistr(boolean isWriteOut) throws Exception {
        if (rateMatrix == null)
            readData();

        // count how many users give the same number of ratings
        Multiset<Integer> numURates = HashMultiset.create();

        // count how many items recieve the same number of ratings
        Multiset<Integer> numIRates = HashMultiset.create();

        for (int r = 0, rm = rateMatrix.numRows; r < rm; r++) {
            int numRates = rateMatrix.rowSize(r);
            numURates.add(numRates);
        }

        for (int c = 0, cm = rateMatrix.numColumns; c < cm; c++) {
            int numRates = rateMatrix.columnSize(c);
            numIRates.add(numRates);
        }

        String ustrs = Strings.toString(numURates);
        String istrs = Strings.toString(numIRates);

        if (isWriteOut) {
            FileIO.writeString(FileIO.desktop + "user-distr.txt", ustrs);
            FileIO.writeString(FileIO.desktop + "item-distr.txt", istrs);
        } else {
            Logs.debug("#ratings (x) ~ #users (y): \n" + ustrs);
            Logs.debug("#ratings (x) ~ #items (y): \n" + istrs);
        }

        Logs.debug("Done!");

    }

    /**
     * @return number of users
     */
    public int numUsers() {
        return userIds.size();
    }

    /**
     * @return number of items
     */
    public int numItems() {
        return itemIds.size();
    }

    /**
     * @return number of rates
     */
    public int numRatings() {
        return numRatings;
    }

    /**
     * @return number of days
     */
    public int numDays() {
        return (int) TimeUnit.MILLISECONDS.toDays(maxTimestamp - minTimestamp);
    }

    /**
     * @param rawId raw user id as String
     * @return inner user id as int
     */
    public int getUserId(String rawId) {
        return userIds.get(rawId);
    }

    /**
     * @param innerId inner user id as int
     * @return raw user id as String
     */
    public String getUserId(int innerId) {

        if (idUsers == null)
            idUsers = userIds.inverse();

        return idUsers.get(innerId);
    }

    /**
     * @param rawId raw item id as String
     * @return inner item id as int
     */
    public int getItemId(String rawId) {
        return itemIds.get(rawId);
    }

    /**
     * @param innerId inner user id as int
     * @return raw item id as String
     */
    public String getItemId(int innerId) {

        if (idItems == null)
            idItems = itemIds.inverse();

        return idItems.get(innerId);
    }

    /**
     * @return the path to the dataset file
     */
    public String getDataPath() {
        return dataPath;
    }

    /**
     * @return the rate matrix
     */
    public SparseMatrix getRateMatrix() {
        return rateMatrix;
    }

    /**
     * @return whether "items" are users, useful for social reltions
     */
    public boolean isItemAsUser() {
        return isItemAsUser;
    }

    /**
     * @return rating scales
     */
    public List<Double> getRatingScale() {
        return ratingScale;
    }

    /**
     * @return user {rawid, inner id} mappings
     */
    public BiMap<String, Integer> getUserIds() {
        return userIds;
    }

    /**
     * @return item {rawid, inner id} mappings
     */
    public BiMap<String, Integer> getItemIds() {
        return itemIds;
    }

    /**
     * @return name of the data file with file type extension
     */
    public String getDataName() {
        if (dataName == null) {
            dataName = dataPath.substring(dataPath.lastIndexOf(File.separator) + 1, dataPath.lastIndexOf("."));
        }

        return dataName;
    }

    /**
     * @return directory of the data file
     */
    public String getDataDirectory() {
        if (dataDir == null) {
            int pos = dataPath.lastIndexOf(File.separator);
            dataDir = pos > 0 ? dataPath.substring(0, pos + 1) : "." + File.separator;
        }

        return dataDir;
    }

    /**
     * set the time unit of the data file
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * @return the minimum timestamp
     */
    public long getMinTimestamp() {
        return minTimestamp;
    }

    /**
     * @return the maximum timestamp
     */
    public long getMaxTimestamp() {
        return maxTimestamp;
    }

    public SparseTensor getRateTensor() {
        return rateTensor;
    }

    public boolean isHeadline() {
        return isHeadline;
    }

    public void setHeadline(boolean isHeadline) {
        this.isHeadline = isHeadline;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public ItemContext getItemContext() {
        return itemContext;
    }

    public void setItemContext(ItemContext itemContext) {
        this.itemContext = itemContext;
    }

    public RatingContext getRatingContext() {
        return ratingContext;
    }

    public void setRatingContext(RatingContext ratingContext) {
        this.ratingContext = ratingContext;
    }
}
