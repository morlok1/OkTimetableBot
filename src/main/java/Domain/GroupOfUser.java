package Domain;

import java.util.ArrayList;

public class GroupOfUser {

    private ArrayList<UsersTimetable> users;

    private String hashNumber;

    public GroupOfUser(String hashNumber) {
        this.hashNumber = hashNumber;
        users = new ArrayList<>();
    }

    public void addUser(UsersTimetable.EmploymentState[] employmentStates) {
        users.add(new UsersTimetable(employmentStates));
    }

    public UsersTimetable.EmploymentState[] getGeneralTimetable() {

        UsersTimetable.EmploymentState[] generalTimetable = new UsersTimetable.EmploymentState[7];
        for (int i=0; i<generalTimetable.length; i++) {
            generalTimetable[i] = UsersTimetable.EmploymentState.FREE;
        }

        for (int i=0; i<generalTimetable.length; i++) {
            for (UsersTimetable user : users) {
                if (user.getStateByDayIndex(i) == UsersTimetable.EmploymentState.ALMOSTFREE) {
                    generalTimetable[i] = UsersTimetable.EmploymentState.ALMOSTFREE;
                }
                if (user.getStateByDayIndex(i) == UsersTimetable.EmploymentState.BUSY) {
                    generalTimetable[i] = UsersTimetable.EmploymentState.BUSY;
                    break;
                }
            }
        }

        return generalTimetable;
    }
}