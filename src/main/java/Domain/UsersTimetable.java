package Domain;

import Data.ConstantManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Реализует расписание одного человека - 7 дней с 3 возможными состояниями:
 * -Свободен
 * -Почти свободен
 * -Совсем занят
 */
public class UsersTimetable {

    private static final Logger log = LoggerFactory.getLogger(UsersTimetable.class);

    public enum EmploymentState {BUSY, ALMOSTFREE, FREE}

    private EmploymentState[] employments;

    public UsersTimetable(EmploymentState[] employmentStates) {
        employments = Arrays.copyOf(employmentStates, employmentStates.length);
    }

    /**
     * Возвращает степень занятости по номеру дня
     * @param day
     * @return
     */
    public EmploymentState getStateByDayIndex(int day) {
        return employments[day];
    }

    public void setStateByDayIndex(int index, String state) {
        switch (state) {
            case "+":
                employments[index] = getEmploymentStateByIndex(2);
                break;
            case "=":
                employments[index] = getEmploymentStateByIndex(1);
                break;
            case "-":
                employments[index] = getEmploymentStateByIndex(0);
                break;
                default:
                throw new IllegalStateException();
        }

        //employments[index] = getEmploymentStateByIndex(state);
    }

    private EmploymentState getEmploymentStateByIndex(int index) {
        switch (index) {
            case 0:
                return EmploymentState.BUSY;
            case 1:
                return EmploymentState.ALMOSTFREE;
            case 2:
                return EmploymentState.FREE;
        }

        return EmploymentState.FREE;
    }

    public String getTimetableString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < employments.length; i++) {
            builder.append(ConstantManager.getDayOfWeekByIndex(i));

            switch (employments[i]) {
                case FREE:
                    builder.append(": Свободен \n");
                    break;
                case ALMOSTFREE:
                    builder.append(": Опоздаю \n");
                    break;
                case BUSY:
                    builder.append(": Занят \n");
                    break;
            }
        }

        return builder.toString();
    }
}
