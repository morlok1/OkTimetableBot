package Data;


import Domain.GroupOfUser;

import java.util.HashMap;

public class DataManager {

    private static DataManager theInstance;

    HashMap<String, GroupOfUser> groups;

    public static DataManager getInstance() {
        if (theInstance == null) {
            theInstance = new DataManager();
        }

        return theInstance;
    }

    private DataManager() {
        groups = new HashMap<>();
    }

    public GroupOfUser getGroupByHash(String hash) {
        return groups.get(hash);
    }

}
