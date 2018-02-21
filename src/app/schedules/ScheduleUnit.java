package app.schedules;

import app.data.Group;
import app.data.Week;
import app.data.WeekList;

/**
 * Created by Vladimir on 03/01/18.
 **/
public class ScheduleUnit extends Group {
    private Week[] weeks = new Week[52];

    public ScheduleUnit(Group group) {
        super(group.getKey(), group.getDepartment(), group.getName());
        for (int i = 0; i < 52; i++) {
            weeks[i] = new Week();
        }
    }

    public Week getWeek(int index) {
        return weeks[index];
    }

    public void setWeek(int index, Week week) {
        this.weeks[index] = week;
    }

    public String decode() {
        String line = "";
        for (Week week :
                weeks) {
            line += week.getMark();
        }
        return line;
    }

    public boolean encode(WeekList list, String line) {
        if (line.length() > 52) return false;
        for (int i = 0; i < 52; i++) {
            try {
                weeks[i] = list.getWeekByMark(line.charAt(i));
            } catch (ArrayIndexOutOfBoundsException e) {
                weeks[i] = new Week();
            }
        }
        return true;
    }

    @Override
    public int compareTo(Group o) {
        return this.getName().compareTo(o.getName());
    }
}
