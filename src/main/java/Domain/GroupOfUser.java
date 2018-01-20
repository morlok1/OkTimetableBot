package Domain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Реализует сущность расписания группы
 * Осуществляет расчёт результирующего расписания
 */
public class GroupOfUser {

    //private ArrayList<UsersTimetable> users;
    private HashMap<String, UsersTimetable> users;

    private String adminID;

    public GroupOfUser(String chatID) {
        this.adminID = chatID;
        users = new HashMap<>();
    }

    /**
     * Осуществляет добавление нового расписание в группу
     * @param employmentStates - расписание
     */
    public void addUser(UsersTimetable.EmploymentState[] employmentStates, String userID) {
        users.put(userID ,new UsersTimetable(employmentStates));
    }

    public UsersTimetable getUserById(String chatID) {
        if (users.containsKey(chatID)) {
            return users.get(chatID);
        }

        return null;
    }

    /**
     * Осуществляет расчёт результирующего расписания
     * @return
     */
    public UsersTimetable.EmploymentState[] getGeneralTimetable() {
        UsersTimetable.EmploymentState[] generalTimetable = new UsersTimetable.EmploymentState[7];
        for (int i=0; i<generalTimetable.length; i++) {
            //Изначально все дни свободны
            generalTimetable[i] = UsersTimetable.EmploymentState.FREE;
        }

        Set<String> keys = users.keySet();
        Iterator<String> iterator;
        UsersTimetable user;
        for (int i=0; i<7; i++) {
            iterator = keys.iterator();
            while (iterator.hasNext()) {
                user = users.get(iterator.next());
                if (user.getStateByDayIndex(i) == UsersTimetable.EmploymentState.ALMOSTFREE) {
                    //Индексируем только ухудшения
                    generalTimetable[i] = UsersTimetable.EmploymentState.ALMOSTFREE;
                }
                if (user.getStateByDayIndex(i) == UsersTimetable.EmploymentState.BUSY) {
                    //После этого продолжать нет смысла, не даем возможности
                    //поднять степень доступности дня до среднего - выходим из цикла
                    generalTimetable[i] = UsersTimetable.EmploymentState.BUSY;
                    break;
                }
            }
        }

        /*for (int i=0; i<generalTimetable.length; i++) {
            for (UsersTimetable user : users) {
                if (user.getStateByDayIndex(i) == UsersTimetable.EmploymentState.ALMOSTFREE) {
                    //Индексируем только ухудшения
                    generalTimetable[i] = UsersTimetable.EmploymentState.ALMOSTFREE;
                }
                if (user.getStateByDayIndex(i) == UsersTimetable.EmploymentState.BUSY) {
                    //После этого продолжать нет смысла, не даем возможности
                    //поднять степень доступности дня до среднего - выходим из цикла
                    generalTimetable[i] = UsersTimetable.EmploymentState.BUSY;
                    break;
                }
            }
        }*/

        return generalTimetable;
    }

    public String getAdminID() {
        return adminID;
    }

    public boolean contain(String chatId) {
        if (users.containsKey(chatId)) {
            return true;
        }

        return false;
    }
}
