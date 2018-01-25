package Domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static Data.ConstantManager.DAYS_IN_WEEK;
import static Domain.UsersTimetable.EmploymentState.FREE;

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
        UsersTimetable.EmploymentState[] generalTimetable = new UsersTimetable.EmploymentState[DAYS_IN_WEEK];
        Arrays.fill(generalTimetable, FREE);

        Set<String> keys = users.keySet();
        Iterator<String> iterator;
        UsersTimetable user;
        for (int i=0; i<DAYS_IN_WEEK; i++) {
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
