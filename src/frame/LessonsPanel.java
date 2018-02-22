package frame;

import app.DegreeProject;
import app.data.*;
import app.lessons.*;
import javafx.collections.transformation.SortedList;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Vladimir on 31/01/18.
 **/
public class LessonsPanel extends JPanel{
    private JTable jTable;
    private JButton settingButton;
    private JButton saveButton;
    private JPanel contentPane;
    private JToggleButton button1;
    private JToggleButton button2;
    private JToggleButton button3;
    private JButton setButton;
    private JComboBox<Lesson> lessonCBox;
    private JComboBox<Teacher> teacherCBox;
    private JComboBox<Auditory> auditoryCBox;
    private JLabel groupNameLabel;
    private JLabel workHourInWeekLabel;
    private JLabel studyPairInWeekLabel;
    private JButton exportButton;
    private ButtonGroup buttonGroup;
    private TableModel tableModel;
    private StudyPair nowStudyPair;
    /**
     * Кількість пар в одному дні
     */
    private final int PAIR_IN_DAY = 5;
    private final int COLUMN_REPEAT = 5;
    // Константи, що позначають положення колонок в таблиці
    private final int DAY_NAME_NUMBER = 0;
    private final int PAIR_NUMBER = 1;
    private final int LESSONS_NAME_NUMBER = 2;
    private final int TEACHER_NAME_NUMBER = 3;
    private final int AUDITORY_NUMBER = 4;
    /**
     * Кількість днів в тижні починаючи від понеділка, де 1 - Понеділок, 2 - Понеділок...Вівторок, 3 - Понеділок...Середа
     */
    private final int DAY_AT_WEEK = 6;

    public LessonsPanel() {
        nowStudyPair = new EmptyStudyPair();
        setLayout(new GridLayout());
        add(contentPane);
        InitialTable();
        InitialGroupButton();
        setButton.addActionListener(this::setButtonClick);
        settingButton.addActionListener(this::settingGroupClick);
        InitialData();
    }

    public LessonsPanel(String title) {
        this();
        setName(title);
    }

    private void settingGroupClick(ActionEvent e) {
        DegreeProject.GROUPLIST.refresh();
        int[] choice = new int[DegreeProject.GROUPLIST.GetAllWeek().size()];
        int count = 0;
        ArrayList<Group> tList = DegreeProject.GROUPLIST.GetAllWeek();
        for (int i = 0; i < tList.size(); i++) {
            for (int j = 0; j < tableModel.units.size(); j++) {
                if (tList.get(i).equals(tableModel.units.get(j).getGroup())) {
                    choice[count++] = i;
                }
            }
        }
        choice = Arrays.copyOf(choice, count);
        new GroupChoiceDialog(DegreeProject.GROUPLIST.GetAllWeek(), choice, this::afterSettingGroup);
//        saveButton.setEnabled(true);
    }

    private void afterSettingGroup(ArrayList<Group> list) {
        ArrayList<LessonsUnit> listFromTable = new ArrayList<>(tableModel.units);
        LessonsUnit tLessonUnit;

        for (int i = tableModel.units.size() - 1; i >= 0; i--) {
            tLessonUnit = tableModel.units.remove(i);
            for (Group group : list) {
                if (tLessonUnit.getGroup().equals(group)) {
                    listFromTable.add(tLessonUnit);
                }
            }
        }

        Group tGroup;
        boolean b = false;
        for (Group group : list) {
            tGroup = group;
            for (LessonsUnit lessonsUnit : listFromTable) {
                if (lessonsUnit.getGroup().equals(tGroup)) {
                    b = true;
                }
            }
            if (!b) listFromTable.add(new LessonsUnit(tGroup, PAIR_IN_DAY, DAY_AT_WEEK));
            b = false;
        }
        Collections.sort(listFromTable, (o1, o2) -> o1.getGroup().getName().compareTo(o2.getGroup().getName()));
        tableModel.units.addAll(listFromTable);
        tableModel.fireTableStructureChanged();
        tableModel.fireTableDataChanged();
    }

    private void InitialData() {
        lessonCBox.addActionListener(e -> {
            if(lessonCBox.getSelectedItem() instanceof Lesson) {
                Lesson lesson = (Lesson) lessonCBox.getSelectedItem();
                Auditory auditory = lesson.getAuditory();
                if (auditory != null) {
                    auditoryCBox.setSelectedItem(auditory);
                }
            }
        });
        try (Statement s = DegreeProject.databaseData.getConnection().createStatement()) {
            DefaultComboBoxModel<Lesson> lessonModel = new DefaultComboBoxModel<>();
            lessonCBox.setModel(lessonModel);
            ResultSet rs = s.executeQuery("SELECT * FROM lessons INNER JOIN auditorys ON lessons.auditory = auditorys.k ORDER BY lessons.name");
            while (rs.next()) {
                lessonModel.addElement(
                        new Lesson(
                                rs.getInt("lessons.k"),
                                rs.getString("lessons.name"),
                                new Auditory(rs.getInt("auditorys.k"), rs.getString("auditorys.name")))
                );
            }
            DefaultComboBoxModel<Teacher> teacherModel = new DefaultComboBoxModel<>();
            teacherCBox.setModel(teacherModel);
            rs = s.executeQuery("SELECT * FROM teachers ORDER BY name");
            while (rs.next()) {
                teacherModel.addElement(
                        new Teacher(rs.getInt("k"), rs.getString("name"), Preference.parsePreference(rs.getString("preferences")))
                );
            }

            DefaultComboBoxModel<Auditory> auditoryModel = new DefaultComboBoxModel<>();
            auditoryCBox.setModel(auditoryModel);
            rs = s.executeQuery("SELECT * FROM auditorys ORDER BY name");
            while (rs.next()) {
                auditoryModel.addElement(new Auditory(rs.getInt("k"), rs.getString("name")));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void setButtonClick(ActionEvent event) {
        switch (buttonGroup.getSelection().getActionCommand()) {
            case "BOTH": nowStudyPair = new StudyPairLonely(
                    (Lesson) lessonCBox.getModel().getSelectedItem(),
                    (Teacher) teacherCBox.getModel().getSelectedItem(),
                    (Auditory) auditoryCBox.getModel().getSelectedItem()
            ); break;
            case "NUMERATOR": nowStudyPair = new StudyPairDouble(
                    new StudyPairLonely(
                            (Lesson) lessonCBox.getModel().getSelectedItem(),
                            (Teacher) teacherCBox.getModel().getSelectedItem(),
                            (Auditory) auditoryCBox.getModel().getSelectedItem()),
                    new StudyPairLonely()
            ); break;
            case "DENOMINATOR": nowStudyPair = new StudyPairDouble(
                    new StudyPairLonely(),
                    new StudyPairLonely(
                            (Lesson) lessonCBox.getModel().getSelectedItem(),
                            (Teacher) teacherCBox.getModel().getSelectedItem(),
                            (Auditory) auditoryCBox.getModel().getSelectedItem())
            ); break;
        }
        tableModel.updateForbids(nowStudyPair);
    }

    private void InitialGroupButton() {
        Border out = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);
        Border outCenter = BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY);
        Border in = BorderFactory.createEmptyBorder(3, 5, 3, 5);
        button1.setBorder(BorderFactory.createCompoundBorder(out, in));
        button1.addActionListener(e -> ((TableModel)jTable.getModel()).fireTableDataChanged());
        button2.setBorder(BorderFactory.createCompoundBorder(outCenter, in));
        button2.addActionListener(e -> ((TableModel)jTable.getModel()).fireTableDataChanged());
        button3.setBorder(BorderFactory.createCompoundBorder(out, in));
        button3.addActionListener(e -> ((TableModel)jTable.getModel()).fireTableDataChanged());
        buttonGroup = new ButtonGroup();
        buttonGroup.add(button1);
        buttonGroup.add(button2);
        buttonGroup.add(button3);
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    private void InitialTable() {
        tableModel = new TableModel();
        jTable.setModel(tableModel);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable.setShowGrid(false);
        jTable.setIntercellSpacing(new Dimension(0, 0));
        jTable.setRowHeight(jTable.getRowHeight() * 2);

        jTable.getTableHeader().setReorderingAllowed(false);
        jTable.getTableHeader().setResizingAllowed(false);
        jTable.getTableHeader().setDefaultRenderer(new TableHeaderCellRenderer());

        jTable.setDefaultRenderer(String.class, new TableCellDayNameRenderer());
        jTable.setDefaultRenderer(Integer.class, new TableCellPairNumberRenderer());
        jTable.setDefaultRenderer(StudyPair.class, new TableCellSubjectRenderer());

        jTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {mouseTableClick(e);
            }
        });

        tableModel.addTableModelListener(e -> {
            Enumeration<TableColumn> columns = jTable.getColumnModel().getColumns();
            while (columns.hasMoreElements()) {
                TableColumn column = columns.nextElement();
                switch (column.getModelIndex() % COLUMN_REPEAT) {
                    case DAY_NAME_NUMBER:case PAIR_NUMBER: {
                        column.setMaxWidth(25); column.setMinWidth(25);
                    } break;
                    case LESSONS_NAME_NUMBER:case TEACHER_NAME_NUMBER: {
                        column.setMinWidth(130); column.setMaxWidth(130);
                    } break;
                    case AUDITORY_NUMBER: {
                        column.setMinWidth(40); column.setMaxWidth(40);
                    } break;
                }
            }
        });
    }

    private class TableHeaderCellRenderer extends DefaultTableCellHeaderRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int row, int col) {
            JLabel label = (JLabel)super.getTableCellRendererComponent(jTable, o, b, b1, row, col);
            label.setHorizontalAlignment(LEFT);
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            int r = col % COLUMN_REPEAT;
            if (r == 4 || r == 0 || r == 1) {
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            }
            return label;
        }
    }

    private class TableModel extends AbstractTableModel {
        private ArrayList<LessonsUnit> units = new ArrayList<>();
        private HashMap<app.lessons.StudyPair.Forbidden, HashSet<Point>> fMap = new HashMap<>();

        public TableModel() {
        }

        public HashMap<StudyPair.Forbidden, HashSet<Point>> getfMap() {
            return fMap;
        }

        public StudyPair.Forbidden getForbidden(TableModel tableModel, int row, int column) {
            StudyPair.Forbidden forbidden = StudyPair.Forbidden.UNKNOWN_FORBIDDEN;
            HashMap<StudyPair.Forbidden, HashSet<Point>> map = tableModel.getfMap();
            if (map.get(StudyPair.Forbidden.ROW_FORBIDDEN) != null) {
                for (Point point: map.get(StudyPair.Forbidden.ROW_FORBIDDEN)) {
                    if (point.getX() == row) {
                        forbidden = StudyPair.Forbidden.ROW_FORBIDDEN;
                        break;
                    }
                }
            }
            for (StudyPair.Forbidden f: map.keySet()) {
                if (!map.get(f).contains(new Point(row, column / COLUMN_REPEAT))) continue;
                switch (f) {
                    case SELF_FORBIDDEN: forbidden = StudyPair.Forbidden.SELF_FORBIDDEN;
                        break;
                    case NON_FORBIDDEN: forbidden = StudyPair.Forbidden.NON_FORBIDDEN;
                        break;
                }
            }
            return forbidden;
        }

        public void updateForbids(StudyPair studyPair) {
            fMap.clear();
            StudyPair.Forbidden[] forbidsArr;
            for (int col = 0; col < units.size(); col++) {
                LessonsUnit unit = units.get(col);
                for (int row = 0; row < unit.getPairPerDay() * unit.getDayPerWeek(); row++) {
                    forbidsArr = unit.getPair(row).getForbidden(studyPair, units, row, col, unit.getPairPerDay(), unit.getDayPerWeek());
                    for (StudyPair.Forbidden f: forbidsArr) {
                        HashSet<Point> set = fMap.get(f) == null ? new HashSet<>() : fMap.get(f);
                        set.add(new Point(row, col));
                        fMap.put(f, set);
                    }
                }
            }
            fireTableDataChanged();
        }



        @Override
        public int getRowCount() {
            return DAY_AT_WEEK * PAIR_IN_DAY;
        }

        @Override
        public String getColumnName(int column) {
            if (column % COLUMN_REPEAT == TEACHER_NAME_NUMBER) return units.get(column / COLUMN_REPEAT).getGroup().getName();
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex % COLUMN_REPEAT) {
                case DAY_NAME_NUMBER: return String.class;
                case PAIR_NUMBER: return Integer.class;
                case LESSONS_NAME_NUMBER:case TEACHER_NAME_NUMBER:case AUDITORY_NUMBER: return StudyPair.class;
                default: return Object.class;
            }
        }

        @Override
        public int getColumnCount() {
            return units == null ? 2 : units.size() * COLUMN_REPEAT;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex % COLUMN_REPEAT) {
                case LESSONS_NAME_NUMBER:case TEACHER_NAME_NUMBER:case AUDITORY_NUMBER:
                    return units.get(columnIndex / COLUMN_REPEAT).getPair(rowIndex);
            }
            return "";
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex % COLUMN_REPEAT) {
                case LESSONS_NAME_NUMBER:case TEACHER_NAME_NUMBER:case AUDITORY_NUMBER: {
                    if (units.get(columnIndex / COLUMN_REPEAT).getPair(rowIndex) instanceof StudyPairDouble
                            && aValue instanceof StudyPairDouble) {
                        units.get(columnIndex / COLUMN_REPEAT).setPair(rowIndex,
                                StudyPairDouble.unite(
                                        (StudyPairDouble)units.get(columnIndex / COLUMN_REPEAT).getPair(rowIndex),
                                        (StudyPairDouble)aValue));
                    } else {
                        units.get(columnIndex / COLUMN_REPEAT).setPair(rowIndex, (StudyPair)aValue);
                    }
//                    units.get(columnIndex / COLUMN_REPEAT).setPair(rowIndex, (StudyPair)aValue);
                }
            }
            updateForbids(nowStudyPair);
            fireTableDataChanged();
        }
    }

    private class TableCellDayNameRenderer extends DefaultTableCellRenderer {
        private final String[] daysName = new String[] {
            "ПОНЕДІЛОК", "ВІВТОРОК", "СЕРЕДА", "ЧЕТВЕРГ", "ПЯТНИЦЯ", "СУБОТА", "НЕДІЛЯ"
        };
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel labelTop = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            labelTop.setBackground(new Color(242, 242, 242));
            labelTop.setFont(new Font(labelTop.getFont().getName(), Font.BOLD, labelTop.getFont().getSize()));
            labelTop.setHorizontalAlignment(CENTER);
            if ((row * 2) % (PAIR_IN_DAY * 2) < daysName[row / PAIR_IN_DAY].length()) {
                labelTop.setText(String.valueOf(
                        daysName[row / PAIR_IN_DAY].charAt((row * 2) % (PAIR_IN_DAY * 2))
                ));
            }

            JLabel labelBottom = new JLabel();

            if (((row * 2) + 1) % (PAIR_IN_DAY * 2) < daysName[row / PAIR_IN_DAY].length()) {
                labelBottom.setText(String.valueOf(
                        daysName[row / PAIR_IN_DAY].charAt(((row * 2) + 1) % (PAIR_IN_DAY * 2))
                ));
            }
            labelBottom.setHorizontalAlignment(CENTER);
            labelBottom.setFont(new Font(labelBottom.getFont().getName(), Font.BOLD, labelBottom.getFont().getSize()));

            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(labelTop);
            panel.add(labelBottom);
            panel.setBorder(row % PAIR_IN_DAY == PAIR_IN_DAY - 1 ?
                    BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY)
                    :
                    BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY)
            );
            if (row % PAIR_IN_DAY == PAIR_IN_DAY - 1) panel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, Color.LIGHT_GRAY));
            return panel;
        }
    }

    private class TableCellPairNumberRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            label.setHorizontalAlignment(CENTER);
            label.setText(String.valueOf((row + 1) % (PAIR_IN_DAY) == 0 ? PAIR_IN_DAY : (row + 1) % (PAIR_IN_DAY)));
            label.setBackground(new Color(242, 242, 242));
            if (row % PAIR_IN_DAY == PAIR_IN_DAY - 1) label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, Color.LIGHT_GRAY));
            return label;
        }
    }

    private class TableCellSubjectRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent component = ((StudyPair) value).getRendererComponent(StudyPair.Query.values()[(column % COLUMN_REPEAT) - 2]);
            component.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            if (row % PAIR_IN_DAY == PAIR_IN_DAY - 1)
                component.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, Color.LIGHT_GRAY));
            component.setBackground(new Color(0xC5DCA0));
            component.setOpaque(true);
            for (StudyPair.Forbidden forbidden : nowStudyPair.getSelfForbidden(row, column, PAIR_IN_DAY, DAY_AT_WEEK)) {
                if (forbidden == StudyPair.Forbidden.DAY_FORBIDDEN) {
                    component.setBackground(new Color(0xBBBAB8));
                    return component;
                }
            }
            StudyPair.Forbidden forbidden = ((TableModel)table.getModel()).getForbidden(tableModel, row, column);

            switch (forbidden) {
                case SELF_FORBIDDEN: component.setBackground(new Color(0x818AA3));
                    break;
                case ROW_FORBIDDEN: component.setBackground(new Color(0xF9DAD0));
                    break;
                case NON_FORBIDDEN: component.setBackground(new Color(0xF5F2B8));
                    break;
                case UNKNOWN_FORBIDDEN: component.setBackground(new Color(0xC5DCA0));
            }
            return component;
        }
    }

    private void mouseTableClick(MouseEvent e) {
        int row = jTable.rowAtPoint(e.getPoint());
        int column = jTable.columnAtPoint(e.getPoint());
        tableModel.setValueAt(nowStudyPair, row, column);
        analyzeTable(row, column);
    }

    private void analyzeTable(int row, int column) {
        LessonsUnit unit = getTableModel().units.get(column / COLUMN_REPEAT);
        int pairCountNumerator = 0;
        int pairCountDenominator = 0;
        for (StudyPair pair : unit.getPairs()) {
            if (pair instanceof StudyPairLonely) {
                StudyPairLonely lonely = (StudyPairLonely) pair;
                if (lonely.isEmpty()) continue;
                pairCountNumerator++;
                pairCountDenominator++;
            } else if (pair instanceof StudyPairDouble) {
                StudyPairLonely numerator = ((StudyPairDouble)pair).getNumerator();
                StudyPairLonely denominator = ((StudyPairDouble)pair).getDenominator();
                if (!numerator.isEmpty()) pairCountNumerator++;
                if (!denominator.isEmpty()) pairCountDenominator++;
            }
        }
        groupNameLabel.setText(unit.getGroup().getName());
        workHourInWeekLabel.setText(pairCountNumerator == pairCountDenominator ?
                (pairCountNumerator * 2) + " годин; в середньому на день " + ((pairCountDenominator * 2 + pairCountNumerator * 2) / 10) :
                "(Ч) " + (pairCountNumerator * 2) + " годин; (З) " + (pairCountDenominator * 2) + " годин;  в середньому на день " + ((pairCountDenominator * 2 + pairCountNumerator * 2) / 10)
        );
        studyPairInWeekLabel.setText(pairCountNumerator == pairCountDenominator ?
                (pairCountNumerator) + " пар; в середньому на день " + ((pairCountDenominator + pairCountNumerator) / 10) :
                "(Ч) " + (pairCountNumerator) + " годин; (З) " + (pairCountDenominator) + " годин;  в середньому на день " + ((pairCountDenominator + pairCountNumerator) / 10)
        );
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setLayout(new GridLayout());
        frame.add(new LessonsPanel());
        frame.setVisible(true);
    }
}


