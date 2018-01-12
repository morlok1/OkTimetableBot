package Domain;

import java.util.ArrayList;

/**
 * Реализует сущность расписания группы
 * Осуществляет расчёт результирующего расписания
 */
public class GroupOfUser {

    private ArrayList<UsersTimetable> users;

    private String chatID;

    public GroupOfUser(String chatID) {
        this.chatID = chatID;
        users = new ArrayList<>();
    }

    /**
     * Осуществляет добавление нового расписание в группу
     * @param employmentStates - расписание
     */
    public void addUser(UsersTimetable.EmploymentState[] employmentStates) {
        users.add(new UsersTimetable(employmentStates));
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

        for (int i=0; i<generalTimetable.length; i++) {
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
        }

        return generalTimetable;
    }
}
