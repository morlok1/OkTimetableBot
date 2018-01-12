package Domain;

/**
 * Реализует расписание одного человека - 7 дней с 3 возможными состояниями:
 * -Свободен
 * -Почти свободен
 * -Совсем занят
 */
public class UsersTimetable {

    public enum EmploymentState {BUSY, ALMOSTFREE, FREE}

    private EmploymentState[] employments;

    public UsersTimetable(EmploymentState[] employmentStates) {
        employments = new EmploymentState[7];
        for (int i=0; i<employmentStates.length; i++) {
            employments[i] = employmentStates[i];
        }
    }

    /**
     * Возвращает степень занятости по номеру дня
     * @param day
     * @return
     */
    public EmploymentState getStateByDayIndex(int day) {
        return employments[day];
    }

}
