package Domain;

public class UsersTimetable {

    public enum EmploymentState {BUSY, ALMOSTFREE, FREE}

    private EmploymentState[] employments;

    UsersTimetable(EmploymentState[] employmentStates) {
        employments = new EmploymentState[7];
        for (int i=0; i<employmentStates.length; i++) {
            employments[i] = employmentStates[i];
        }
    }

    public EmploymentState getStateByDayIndex(int day) {
        return employments[day];
    }
}
