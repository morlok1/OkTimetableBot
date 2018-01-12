package Domain;

import java.util.ArrayList;

public class GroupOfUser {

    private ArrayList<UsersTimetable> users;

    private String chatID;

    public GroupOfUser(String chatID) {
        this.chatID = chatID;
        users = new ArrayList<>();
    }

    public void addUser(UsersTimetable.EmploymentState[] employmentStates) {
        System.out.println("WHAT");
        users.add(new UsersTimetable(employmentStates));
    }

    public UsersTimetable.EmploymentState[] getGeneralTimetable() {
        System.out.println("Всего пользователей: " + users.size());
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

    public void clear() {
        users.clear();
    }
}
