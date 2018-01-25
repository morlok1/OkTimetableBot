package Domain;

import java.util.*;

import static Data.ConstantManager.DAYS_IN_WEEK;
import static Domain.UsersTimetable.EmploymentState.FREE;

/**
 * Реализует сущность расписания группы
 * Осуществляет расчёт результирующего расписания
 */
public class GroupOfUser {

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
        return users.get(chatID);
    }

    /**
     * Осуществляет расчёт результирующего расписания
     * @return
     */
    public UsersTimetable.EmploymentState[] getGeneralTimetable() {
        UsersTimetable.EmploymentState[] generalTimetable = new UsersTimetable.EmploymentState[DAYS_IN_WEEK];
        Arrays.fill(generalTimetable, FREE);

        for (int i=0; i<DAYS_IN_WEEK; i++) {

            for (Map.Entry<String, UsersTimetable> entry : users.entrySet()) {
                switch (users.get(entry.getKey()).getStateByDayIndex(i)) {
                    case ALMOSTFREE:
                        //Индексируем только ухудшения
                        generalTimetable[i] = UsersTimetable.EmploymentState.ALMOSTFREE;
                        break;
                    case BUSY:
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

    public boolean contains(String chatId) {
        return users.containsKey(chatId);
    }
}
