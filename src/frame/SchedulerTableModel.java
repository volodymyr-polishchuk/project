package frame;

import app.Period;
import app.ScheduleUnit;
import app.Week;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Vladimir on 14/01/18.
 **/
class SchedulerTableModel extends AbstractTableModel {
    private ArrayList<Period> periods = Period.GetWeekList(new Date(System.currentTimeMillis()));
    private ArrayList<ScheduleUnit> units = new ArrayList<>();
    private Calendar c = Calendar.getInstance();

    @Override
    public int getRowCount() {
        return units == null ? 3 : 3 + units.size();
    }

    @Override
    public int getColumnCount() {
        return 53;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Група";
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return String.class;
            default: return Week.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        switch (columnIndex) {
            case 0: switch (rowIndex) {
                case 0: return "<html><b>Період</b></html>";
                case 1: return "<html><b>Робочих днів</b></html>";
                case 2: return "<html><b>Тиждень</b></html>";
                default: return units.get(rowIndex - 3).getGroup().getGroupName();
            }
            default: switch (rowIndex) {
                case 0:  {
                    String line = "<html>";
                    c.setTime(periods.get(columnIndex - 1).getStartDate());
                    line += AddZeroBefore(c.get(Calendar.DATE)) + "<br><u>";
                    line += AddZeroBefore(c.get(Calendar.MONTH) + 1) +  "</u><br>";
                    c.setTime(periods.get(columnIndex - 1).getLastDate());
                    line += AddZeroBefore(c.get(Calendar.DATE)) + "<br>";
                    line += AddZeroBefore(c.get(Calendar.MONTH) + 1) + "</html>";
                    return line;
                }
                case 1: return periods.get(columnIndex - 1).getWorkDay();
                case 2: return columnIndex;
                default: return units.get(rowIndex - 3).getWeek(columnIndex - 1);
            }
        }
    }



    /**
     * Перетвоює одноцифрове число у форма двох цифрового
     * @param i число 0, 1, 2, ..., 9, 10, 11, ...
     * @return повертає число у форматі 00, 01, 02, ..., 09, 10, 11, ...
     */
    private String AddZeroBefore(int i) {
        return i<10?"0"+i:""+i;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= 3) {
            if (columnIndex >= 1) {
                units.get(rowIndex - 3).setWeek(columnIndex - 1, (Week)aValue);
                fireTableDataChanged();
            }
        }
    }

    public void addScheduleUnit(ScheduleUnit scheduleUnit) {
        units.add(scheduleUnit);
        fireTableDataChanged();
    }
}
