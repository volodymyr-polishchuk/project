package app.frame;

import app.DegreeProject;
import app.data.*;
import app.data.loading.GroupLoad;
import app.data.loading.LoadUnit;
import app.data.loading.SemesterLoad;
import app.lessons.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.*;
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
    private JComboBox<Lesson> lessonCBox;
    private JComboBox<Teacher> teacherCBox;
    private JComboBox<Auditory> auditoryCBox;
    private JLabel groupNameLabel;
    private JLabel workHourInWeekLabel;
    private JLabel studyPairInWeekLabel;
    private JButton exportButton;
    private JButton prevPeriodButton;
    private JButton nextPeriodButton;
    private JLabel periodLabel;
    private JButton rButton;
    private ButtonGroup buttonGroup;
    private LessonTableModel lessonTableModel;
    private StudyPair nowStudyPair;
    private PopupMenu tablePopupMenu = new PopupMenu();
    private Point cursor = new Point();

    private SemesterLoad semesterLoad = null;
    /**
     * Кількість пар в одному дні
     */
    private final int PAIR_IN_DAY = 5;
    /**
     * Кількість днів в тижні починаючи від понеділка, де 1 - Понеділок, 2 - Понеділок...Вівторок, 3 - Понеділок...Середа
     */
    private final int DAY_AT_WEEK = 5;

    /**
     * Кількість колонок, що виділяться під одну групу
     */
    private final int COLUMN_REPEAT = 5;
    // Константи, що позначають положення колонок в таблиці
    private final int DAY_NAME_NUMBER = 0;
    private final int PAIR_NUMBER = 1;
    private final int LESSONS_NAME_NUMBER = 2;
    private final int TEACHER_NAME_NUMBER = 3;
    private final int AUDITORY_NUMBER = 4;

    private LessonsPanel() {
        nowStudyPair = new EmptyStudyPair();
        setLayout(new GridLayout());
        add(contentPane);
        initialTable();
        initialGroupButton();
        settingButton.addActionListener(this::settingGroupClick);
        saveButton.addActionListener(this::saveButtonClick);
        boolean isExistData = initialData();
        if (!isExistData) JOptionPane.showMessageDialog(null, "В системі відсутні даної одної \n" +
                "з категорій (предмети/викладачі/аудиторії), \n" +
                "додайте хоча б по одному елементу до кожного, \n" +
                "щоб продовжити роботу", "Помилка", JOptionPane.WARNING_MESSAGE);
        button1.addActionListener(this::setButtonClick);
        button2.addActionListener(this::setButtonClick);
        button3.addActionListener(this::setButtonClick);
        auditoryCBox.addActionListener(this::setButtonClick);
        teacherCBox.addActionListener(this::setButtonClick);
        lessonCBox.addActionListener(this::setButtonClick);
        nextPeriodButton.addActionListener(this::nextPeriodButtonClick);
        prevPeriodButton.addActionListener(this::prevPeriodButtonClick);
        exportButton.addActionListener(this::exportButtonClick);
        rButton.addActionListener(e -> initialData());
    }

    LessonsPanel(String title) {
        this();
        setName(title);
    }

    LessonsPanel(String title, String period) {
        this(title);
        try {
            periodLabel.setText(period);

            String sql = "SELECT * FROM lessons_schedules WHERE period LIKE ?";
            PreparedStatement ps = DegreeProject.databaseData.getConnection().prepareStatement(sql);
            ps.setString(1, period);
            ResultSet rs = ps.executeQuery();
            int k = -1;
            if (rs.next()) {
                k = rs.getInt("k");
                int semesterLoadKey = rs.getInt("semester_load");
                if (!rs.wasNull()) {
                    semesterLoad = new SemesterLoad(semesterLoadKey);
                    settingButton.setEnabled(false);
                }
            } else {
                throw new SQLException("Не вдалося знайти жодного ключа за період " + period);
            }

            if (k == -1) throw new IllegalArgumentException("Period not found {" + period + "}");
            String sqlGetAll = "SELECT * FROM lessons_data INNER JOIN groups ON groups = groups.k INNER JOIN departments ON groups.department = departments.k \n" +
                    "INNER JOIN lessons ON lessons_data.lesson = lessons.k INNER JOIN teachers ON lessons_data.teacher = teachers.k\n" +
                    "INNER JOIN auditorys AS teachers_auditorys ON lessons.auditory = teachers_auditorys.k \n" +
                    "INNER JOIN auditorys AS lessons_auditory ON lessons_data.auditory = lessons_auditory.k WHERE lessons_data.lessons_schedule LIKE ?;";
            ps = DegreeProject.databaseData.getConnection().prepareStatement(sqlGetAll);
            ps.setInt(1, k);
            rs = ps.executeQuery();

            ArrayList<LessonsUnit> units = new ArrayList<>();

            while (rs.next()) {
                Group group = new Group(rs.getInt("groups.k"), new Department(rs.getInt("departments.k"), rs.getString("departments.name")), rs.getString("groups.name"));
                StudyPairLonely pairLonely = new StudyPairLonely(
                        new Lesson(rs.getInt("lessons.k"), rs.getString("lessons.name"), new Auditory(rs.getInt("teachers_auditorys.k"), rs.getString("teachers_auditorys.name"))),
                        new Teacher(rs.getInt("teachers.k"), rs.getString("teachers.name"), Preference.parsePreference(rs.getString("teachers.preferences"))),
                        new Auditory(rs.getInt("lessons_auditory.k"), rs.getString("lessons_auditory.name"))
                );
                StudyPairLonely pairEmpty = new StudyPairLonely(new Lesson(""), new Teacher(""), new Auditory(""));
                String[] line = rs.getString("pair_number").split("/");
                boolean flag = false;
                for (LessonsUnit unit : units) {

                    if (unit.getGroup().equals(group)) {
                        flag = true;
                        switch (line.length) {
                            case 1: {
                                int pair_number = Integer.parseInt(line[0]);
                                unit.setPair(pair_number, pairLonely);
                                break;
                            }
                            case 2: {
                                int pair_number = Integer.parseInt(line[0]);
                                int position = Integer.parseInt(line[1]);
                                switch (position) {
                                    case 1: {
                                        if (unit.getPair(pair_number) instanceof StudyPairDouble)
                                            unit.setPair(pair_number, StudyPairDouble.unite((StudyPairDouble) unit.getPair(pair_number), new StudyPairDouble(pairLonely, pairEmpty)));
                                        else unit.setPair(pair_number, new StudyPairDouble(pairLonely, pairEmpty));
                                        break;
                                    }
                                    case 2: {
                                        if (unit.getPair(pair_number) instanceof StudyPairDouble)
                                            unit.setPair(pair_number, StudyPairDouble.unite((StudyPairDouble) unit.getPair(pair_number), new StudyPairDouble(pairEmpty, pairLonely)));
                                        else unit.setPair(pair_number, new StudyPairDouble(pairEmpty, pairLonely));
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                if (flag) continue;
                LessonsUnit tUnit = new LessonsUnit(group, PAIR_IN_DAY, DAY_AT_WEEK);
                switch (line.length) {
                    case 1: {
                        int pair_number = Integer.parseInt(line[0]);
                        tUnit.setPair(pair_number, pairLonely);
                        break;
                    }
                    case 2: {
                        int pair_number = Integer.parseInt(line[0]);
                        int position = Integer.parseInt(line[1]);
                        switch (position) {
                            case 1:
                                tUnit.setPair(pair_number, new StudyPairDouble(pairLonely, pairEmpty));
                                break;
                            case 2:
                                tUnit.setPair(pair_number, new StudyPairDouble(pairEmpty, pairLonely));
                                break;
                        }
                        break;
                    }
                }
                units.add(tUnit);
            }
            lessonTableModel.units = units;
            lessonTableModel.fireTableStructureChanged();
            lessonTableModel.fireTableDataChanged();
            setButtonClick(null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }
    }

    public LessonsPanel(String title, SemesterLoad semesterLoad) {
        this(title);
        for (GroupLoad groupLoad : semesterLoad.getGroupLoads()) {
            lessonTableModel.units.add(new LessonsUnit(groupLoad.getGroup(), PAIR_IN_DAY, DAY_AT_WEEK));
            lessonTableModel.fireTableStructureChanged();
            lessonTableModel.fireTableDataChanged();
        }
        this.semesterLoad = semesterLoad;
        settingButton.setEnabled(false);
    }

    private void exportButtonClick(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("lessons_scheduler.xls"));
        if  (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (!chooser.getSelectedFile().getPath().endsWith(".xls")) {
                chooser.setSelectedFile(new File(chooser.getSelectedFile().getPath() + ".xls"));
            }
            try {
                lessonTableModel.export(chooser.getSelectedFile(), periodLabel.getText());
                int r = JOptionPane.showConfirmDialog(
                        null,
                        "Розклад занять збережено до файлу\n" + chooser.getSelectedFile().getPath() + "\nВідкрити розклад занять?",
                        "Повідомлення",
                        JOptionPane.YES_NO_CANCEL_OPTION
                );
                DegreeProject.mainForm.setStatusBar("Дані успішно збережено до файлу " + chooser.getSelectedFile().getPath());
                if (r == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(chooser.getSelectedFile());
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void prevPeriodButtonClick(ActionEvent event) {
        String periodLine = periodLabel.getText();
        String[] args = periodLine.split("/");
        if (args.length != 2) {
            periodLabel.setText("2017-2018/1");
            return;
        }
        if (Integer.parseInt(args[1]) == 2) {
            periodLabel.setText(args[0] + "/1");
        } else if (Integer.parseInt(args[1]) == 1) {
            String[] years = args[0].split("-");
            years[0] = String.valueOf(Integer.valueOf(years[0]) - 1);
            years[1] = String.valueOf(Integer.valueOf(years[1]) - 1);
            periodLabel.setText(years[0] + "-" + years[1] + "/2");
        }
    }

    private void nextPeriodButtonClick(ActionEvent event) {
        String periodLine = periodLabel.getText();
        String tPeriodLine = "";
        for (int i = 0; i < periodLine.length(); i++) {
            if (periodLine.charAt(i) != ' ')
                tPeriodLine += periodLine.charAt(i);
        }
        periodLine = tPeriodLine;
        String[] args = periodLine.split("/");
        if (args.length != 2) {
            periodLabel.setText("2017-2018/1");
            return;
        }
        if (Integer.parseInt(args[1]) == 1) {
            periodLabel.setText(args[0] + "/2");
        } else if (Integer.parseInt(args[1]) == 2) {
            String[] years = args[0].split("-");
            years[0] = String.valueOf(Integer.valueOf(years[0]) + 1);
            years[1] = String.valueOf(Integer.valueOf(years[1]) + 1);
            periodLabel.setText(years[0] + "-" + years[1] + "/1");
        }
    }

    private void saveButtonClick(ActionEvent event) {
        LessonTableModel lessonTableModel = ((LessonTableModel) jTable.getModel());
        ArrayList<LessonsUnit> units = lessonTableModel.units;
        try (Statement st = DegreeProject.databaseData.getConnection().createStatement()) {
            ResultSet resultSet = st.executeQuery("SELECT * FROM lessons_schedules WHERE period LIKE '" + periodLabel.getText() + "'");
            if (resultSet.next()) {
                int inputResult = JOptionPane.showConfirmDialog(null,
                        "За даний період уже є розклад занять!\nПерезаписати?",
                        "Попередження", JOptionPane.YES_NO_CANCEL_OPTION);
                if (inputResult != JOptionPane.YES_OPTION) {
                    resultSet.close();
                    return;
                }
                st.execute("DELETE FROM lessons_data WHERE lessons_schedule LIKE (SELECT k FROM lessons_schedules WHERE period LIKE '" + periodLabel.getText() + "')");
                st.execute("DELETE FROM lessons_schedules WHERE period LIKE '" + periodLabel.getText() + "'");
            }
            resultSet.close();
            String sql;
            if (semesterLoad != null) {
                sql = "INSERT INTO lessons_schedules(period, date_of_create, coments, semester_load) VALUE (?, ?, ?, ?)";
            } else {
                sql = "INSERT INTO lessons_schedules(period, date_of_create, coments) VALUE (?, ?, ?)";
            }
            PreparedStatement preparedStatement = DegreeProject.databaseData.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, periodLabel.getText());
            preparedStatement.setDate(2, new Date(System.currentTimeMillis()));
            String comment = JOptionPane.showInputDialog(null, "Введіть коментар", "Коментар", JOptionPane.QUESTION_MESSAGE);
            if (comment == null) {
                preparedStatement.close();
                return;
            }
            preparedStatement.setString(3, comment);
            if (semesterLoad != null) {
                preparedStatement.setInt(4, semesterLoad.getKey());
            }
            preparedStatement.execute();

            int k = -1;
            ResultSet rs = st.executeQuery("SELECT k FROM lessons_schedules WHERE period LIKE '" + periodLabel.getText() + "'");
            while (rs.next()) k = rs.getInt("k");
            if (k == -1) throw new SQLException("Значення ключа не змінилося, отже запис не було додано");

            String sqlLessonsData = "INSERT INTO lessons_data(lessons_schedule, groups, pair_number, lesson, teacher, auditory) VALUE (?, ?, ?, ?, ?, ?)";
            preparedStatement = DegreeProject.databaseData.getConnection().prepareStatement(sqlLessonsData);
            preparedStatement.setInt(1, k);
            for (LessonsUnit unit : units) {
                preparedStatement.setInt(2, unit.getGroup().getKey());
                StudyPair[] pairs = unit.getPairs();
                for (int i = 0; i < pairs.length; i++) {
                    StudyPair pair = pairs[i];
                    if (pair instanceof StudyPairLonely) {
//                      Якщо пара одиночна, тоді записуємо
                        StudyPairLonely pairLonely = (StudyPairLonely) pair;
                        preparedStatement.setString(3, String.valueOf(i));
                        preparedStatement.setInt(4, pairLonely.getLesson().getKey());
                        preparedStatement.setInt(5, pairLonely.getTeacher().getKey());
                        preparedStatement.setInt(6, pairLonely.getAuditory().getKey());
                        preparedStatement.execute();
                    } else if (pair instanceof StudyPairDouble) {
//                      Якщо пара двойна - перевіряємо який з елементів пари дійсний і записуємо
                        StudyPairDouble pairDouble = (StudyPairDouble) pair;
                        if (pairDouble.getNumerator() != null && !pairDouble.getNumerator().isEmpty()) {
                            preparedStatement.setString(3, String.valueOf(i) + "/1");
                            preparedStatement.setInt(4, pairDouble.getNumerator().getLesson().getKey());
                            preparedStatement.setInt(5, pairDouble.getNumerator().getTeacher().getKey());
                            preparedStatement.setInt(6, pairDouble.getNumerator().getAuditory().getKey());
                            preparedStatement.execute();
                        }
                        if (pairDouble.getDenominator() != null && !pairDouble.getDenominator().isEmpty()) {
                            preparedStatement.setString(3, String.valueOf(i) + "/2");
                            preparedStatement.setInt(4, pairDouble.getDenominator().getLesson().getKey());
                            preparedStatement.setInt(5, pairDouble.getDenominator().getTeacher().getKey());
                            preparedStatement.setInt(6, pairDouble.getDenominator().getAuditory().getKey());
                            preparedStatement.execute();
                        }
                    }
                }
            }
            JOptionPane.showMessageDialog(null, "Дані успішно збережено!", "Повідомлення з бази даних", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }
    }

    private void settingGroupClick(ActionEvent e) {
        DegreeProject.GROUPLIST.refresh();
        ArrayList<Group> groups = new ArrayList<>();
        lessonTableModel.units.forEach(lu -> groups.add(lu.getGroup()));
        ArrayList<Group> outlast = (ArrayList<Group>) new MultiChoiceDialog<>(DegreeProject.GROUPLIST.getList(), groups).showAndGetData();
        ArrayList<LessonsUnit> outlastLessonsUnits = new ArrayList<>();
        lessonTableModel.units.stream().filter(lessonsUnit -> outlast.remove(lessonsUnit.getGroup())).forEach(outlastLessonsUnits::add);
        outlast.forEach(item -> outlastLessonsUnits.add(new LessonsUnit(item, PAIR_IN_DAY, DAY_AT_WEEK)));
        Collections.sort(outlastLessonsUnits);
        lessonTableModel.units = outlastLessonsUnits;
        lessonTableModel.fireTableStructureChanged();
        lessonTableModel.fireTableDataChanged();
    }

    void showSetting() {
        settingButton.doClick();
    }

    private boolean initialData() {
        boolean check1 = false, check2 = false, check3 = false;
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
                        new Lesson(rs.getInt("lessons.k"), rs.getString("lessons.name"),
                                new Auditory(rs.getInt("auditorys.k"), rs.getString("auditorys.name")))
                );
                check1 = true;
            }

            DefaultComboBoxModel<Teacher> teacherModel = new DefaultComboBoxModel<>();
            teacherCBox.setModel(teacherModel);
            rs = s.executeQuery("SELECT * FROM teachers ORDER BY name");
            while (rs.next()) {
                teacherModel.addElement(
                        new Teacher(rs.getInt("k"), rs.getString("name"), Preference.parsePreference(rs.getString("preferences")))
                );
                check2 = true;
            }

            DefaultComboBoxModel<Auditory> auditoryModel = new DefaultComboBoxModel<>();
            auditoryCBox.setModel(auditoryModel);
            rs = s.executeQuery("SELECT * FROM auditorys ORDER BY name");
            while (rs.next()) {
                auditoryModel.addElement(new Auditory(rs.getInt("k"), rs.getString("name")));
                check3 = true;
            }
            rs.close();
            lessonTableModel.updateForbids(nowStudyPair);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check1 && check2 && check3;
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
        lessonTableModel.updateForbids(nowStudyPair);
    }

    private void initialGroupButton() {
        Border out = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);
        Border outCenter = BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY);
        Border in = BorderFactory.createEmptyBorder(3, 5, 3, 5);
        button1.setBorder(BorderFactory.createCompoundBorder(out, in));
        button1.addActionListener(e -> ((LessonTableModel)jTable.getModel()).fireTableDataChanged());
        button2.setBorder(BorderFactory.createCompoundBorder(outCenter, in));
        button2.addActionListener(e -> ((LessonTableModel)jTable.getModel()).fireTableDataChanged());
        button3.setBorder(BorderFactory.createCompoundBorder(out, in));
        button3.addActionListener(e -> ((LessonTableModel)jTable.getModel()).fireTableDataChanged());
        buttonGroup = new ButtonGroup();
        buttonGroup.add(button1);
        buttonGroup.add(button2);
        buttonGroup.add(button3);
    }

    private LessonTableModel getLessonTableModel() {
        return lessonTableModel;
    }

    private void initialTable() {
        lessonTableModel = new LessonTableModel();
        jTable.setModel(lessonTableModel);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable.setShowGrid(false);
        jTable.setIntercellSpacing(new Dimension(0, 0));
        jTable.setRowHeight((int) (jTable.getRowHeight() * 1.5));

        jTable.getTableHeader().setReorderingAllowed(false);
        jTable.getTableHeader().setResizingAllowed(false);
        jTable.getTableHeader().setDefaultRenderer(new TableHeaderCellRenderer());

        jTable.setDefaultRenderer(String.class, new TableCellDayNameRenderer(PAIR_IN_DAY));
        jTable.setDefaultRenderer(Integer.class, new TableCellPairNumberRenderer());
        jTable.setDefaultRenderer(StudyPair.class, new TableCellSubjectRenderer());

        jTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {mouseTableClick(e);
            }
        });

        lessonTableModel.addTableModelListener(this::tableModelChange);
        tablePopupMenu.add(new MenuItem("Копіювати")).addActionListener(e -> {
            Object o = lessonTableModel.getValueAt(jTable.rowAtPoint(cursor), jTable.columnAtPoint(cursor));
            if (o instanceof StudyPairLonely) {
                StudyPairLonely lonely = (StudyPairLonely)o;
                lessonCBox.setSelectedItem(lonely.getLesson());
                teacherCBox.setSelectedItem(lonely.getTeacher());
                auditoryCBox.setSelectedItem(lonely.getAuditory());
            }
        });
        tablePopupMenu.add(new MenuItem("Очистити")).addActionListener(
                e -> lessonTableModel.setValueAt(new EmptyStudyPair(), jTable.rowAtPoint(cursor), jTable.columnAtPoint(cursor)));
        jTable.add(tablePopupMenu);
    }

    private void tableModelChange(TableModelEvent event) {
        Enumeration<TableColumn> columns = jTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            switch (column.getModelIndex() % COLUMN_REPEAT) {
                case DAY_NAME_NUMBER:case PAIR_NUMBER: {
                    column.setMaxWidth(20); column.setMinWidth(20);
                } break;
                case LESSONS_NAME_NUMBER: {
                    column.setMinWidth(120); column.setMaxWidth(120);
                } break;
                case TEACHER_NAME_NUMBER: {
                    column.setMinWidth(100); column.setMaxWidth(100);
                } break;
                case AUDITORY_NUMBER: {
                    column.setMinWidth(30); column.setMaxWidth(30);
                } break;
            }
        }
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

    private class LessonTableModel extends AbstractTableModel {
        private ArrayList<LessonsUnit> units = new ArrayList<>();
        private HashMap<app.lessons.StudyPair.Forbidden, HashSet<Point>> fMap = new HashMap<>();

        void export(File file, String period) throws IOException {
            final String[] daysName = new String[]{
                "ПОНЕДІЛОК", "ВІВТОРОК", "СЕРЕДА", "ЧЕТВЕРГ", "ПЯТНИЦЯ", "СУБОТА", "НЕДІЛЯ"
            };
            final int trCell = 0;
            final int trRow = 3;
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Розклад занять за період " + period);

            HSSFCellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dataCellStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            HSSFFont defaultFont = workbook.createFont();
            defaultFont.setFontName("Calibri");
            dataCellStyle.setFont(defaultFont);
            dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataCellStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);

            HSSFCellStyle dataBoldCellStyle = workbook.createCellStyle();
            dataBoldCellStyle.cloneStyleFrom(dataCellStyle);
            dataBoldCellStyle.setBorderTop(BorderStyle.DOUBLE);

            HSSFCellStyle dataWithoutBorderCellStyle = workbook.createCellStyle();
            dataWithoutBorderCellStyle.cloneStyleFrom(dataCellStyle);
            dataWithoutBorderCellStyle.setBorderBottom(BorderStyle.NONE);

            HSSFCellStyle subscribeCellStyle = workbook.createCellStyle();
            subscribeCellStyle.cloneStyleFrom(dataCellStyle);
            subscribeCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subscribeCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

            HSSFCellStyle subscribeBoldCellStyle = workbook.createCellStyle();
            subscribeBoldCellStyle.cloneStyleFrom(subscribeCellStyle);
            subscribeBoldCellStyle.setBorderTop(BorderStyle.DOUBLE);

            HSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.cloneStyleFrom(dataCellStyle);
            headerCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            Font headerFont = workbook.createFont();
            headerFont.setFontHeightInPoints((short) 28);
            headerCellStyle.setFont(headerFont);

            HSSFCellStyle topHeaderCellStyle = workbook.createCellStyle();
            topHeaderCellStyle.cloneStyleFrom(headerCellStyle);
            Font topHeaderFont = workbook.createFont();
            topHeaderFont.setFontHeightInPoints((short) 32);
            topHeaderCellStyle.setFont(topHeaderFont);

            if (trRow >= 3) {
                sheet.addMergedRegion(new CellRangeAddress(trRow - 3, trRow - 2, trCell, trCell + COLUMN_REPEAT * units.size() - 1));
                Cell cell = sheet.createRow(trRow - 3).createCell(0);
                cell.setCellStyle(topHeaderCellStyle);
                String[] lines = periodLabel.getText().split("/");
                int s = Integer.parseInt(lines[1]);
                int year = Integer.parseInt(lines[0].split("-")[0]);
                cell.setCellValue("Розклад занять за " + (s == 1 ? "I" : "II") + " півріччя " + year + " - " + (year + 1));
            }

            HSSFRow headerRow = sheet.createRow(trRow - 1);
            for (int i = 0; i < units.size() * COLUMN_REPEAT; i++) {
                HSSFCell cell = headerRow.createCell(i);
                cell.setCellStyle(headerCellStyle);
                if (i % COLUMN_REPEAT == 0)
                    cell.setCellValue(units.get(i / COLUMN_REPEAT).getGroup().getName());
            }
            for (int i = 0; i < units.size(); i++) {
                sheet.addMergedRegion(new CellRangeAddress(trRow - 1, trRow - 1, i * COLUMN_REPEAT, i * COLUMN_REPEAT + COLUMN_REPEAT - 1));
            }

            HSSFCell[][] cells = new HSSFCell[DAY_AT_WEEK * PAIR_IN_DAY * 2][units.size() * COLUMN_REPEAT];
            for (int i = 0; i < cells.length; i++) {
                HSSFRow row = sheet.createRow(i + trRow);

                for (int j = 0; j < cells[0].length; j++) {
                    cells[i][j] = row.createCell(j + trCell);
                    boolean pos = true;
                    switch (j % COLUMN_REPEAT) {
                        case DAY_NAME_NUMBER:
                            if (i % (PAIR_IN_DAY * 2) == 0 && i != 0)
                                cells[i][j].setCellStyle(subscribeBoldCellStyle);
                            else
                                cells[i][j].setCellStyle(subscribeCellStyle);
                            if (i % (PAIR_IN_DAY * 2) < daysName[i / (DAY_AT_WEEK * 2)].length()) {
                                cells[i][j].setCellValue(String.valueOf(daysName[i / (DAY_AT_WEEK * 2)].charAt(i % (PAIR_IN_DAY * 2))));
                            } else {
                                cells[i][j].setCellValue(" ");
                            }
                            break;
                        case PAIR_NUMBER:
                            if (i % (PAIR_IN_DAY * 2) == 0 && i != 0)
                                cells[i][j].setCellStyle(subscribeBoldCellStyle);
                            else
                                cells[i][j].setCellStyle(subscribeCellStyle);
                            cells[i][j].setCellValue(((i % (PAIR_IN_DAY * 2)) / 2) + 1 == 0 ? PAIR_IN_DAY : ((i % (PAIR_IN_DAY * 2)) / 2) + 1);
                            if (i % 2 == 0)
                                sheet.addMergedRegion(new CellRangeAddress(i + trRow, i + 1 + trRow, j + trCell, j + trCell));
                            break;
                        default: {
                            if (i % (PAIR_IN_DAY * 2) == 0 && i != 0) cells[i][j].setCellStyle(dataBoldCellStyle);
                                                        else cells[i][j].setCellStyle(dataCellStyle);
                            switch (j % COLUMN_REPEAT) {
                                case LESSONS_NAME_NUMBER: {
                                    StudyPair studyPair = units.get(j / COLUMN_REPEAT).getPairs()[i / 2];
                                    if (studyPair instanceof StudyPairLonely) {
                                        cells[i][j].setCellValue(((StudyPairLonely) studyPair).getLesson().getName());
                                    } else if (studyPair instanceof StudyPairDouble) {
                                        if (i % 2 == 0) cells[i][j].setCellValue("Ч/" + ((StudyPairDouble) studyPair).getNumerator().getLesson().getName());
                                        else cells[i][j].setCellValue("З\\" + ((StudyPairDouble) studyPair).getDenominator().getLesson().getName());
                                        pos = false;
                                    }
                                } break;
                                case TEACHER_NAME_NUMBER: {
                                    StudyPair studyPair = units.get(j / COLUMN_REPEAT).getPairs()[i / 2];
                                    if (studyPair instanceof StudyPairLonely) {
                                        cells[i][j].setCellValue(((StudyPairLonely) studyPair).getTeacher().getName());
                                    }else if (studyPair instanceof StudyPairDouble) {
                                        if (i % 2 == 0) cells[i][j].setCellValue(((StudyPairDouble) studyPair).getNumerator().getTeacher().getName());
                                        else cells[i][j].setCellValue(((StudyPairDouble) studyPair).getDenominator().getTeacher().getName());
                                        pos = false;
                                    }
                                } break;
                                case AUDITORY_NUMBER: {
                                    StudyPair studyPair = units.get(j / COLUMN_REPEAT).getPairs()[i / 2];
                                    if (studyPair instanceof StudyPairLonely) {
                                        cells[i][j].setCellValue(((StudyPairLonely) studyPair).getAuditory().getName());
                                    } else if (studyPair instanceof StudyPairDouble) {
                                        if (i % 2 == 0) cells[i][j].setCellValue(((StudyPairDouble) studyPair).getNumerator().getAuditory().getName());
                                        else cells[i][j].setCellValue(((StudyPairDouble) studyPair).getDenominator().getAuditory().getName());
                                        pos = false;
                                    }
                                } break;
                            }
                            if (i % 2 == 0 && pos)
                                sheet.addMergedRegion(new CellRangeAddress(i + trRow, i + 1 + trRow, j + trCell, j + trCell));
                        }
                    }
                }
            }

            for (int i = 0; i < units.size() * COLUMN_REPEAT; i++) {
                sheet.autoSizeColumn(i, true);
            }

            workbook.write(file);
            workbook.close();
        }

        HashMap<StudyPair.Forbidden, HashSet<Point>> getfMap() {
            return fMap;
        }

        StudyPair.Forbidden getForbidden(LessonTableModel lessonTableModel, int row, int column) {
            StudyPair.Forbidden forbidden = StudyPair.Forbidden.UNKNOWN_FORBIDDEN;
            HashMap<StudyPair.Forbidden, HashSet<Point>> map = lessonTableModel.getfMap();
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

        void updateForbids(StudyPair studyPair) {
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
                }
            }
            updateForbids(nowStudyPair);
            fireTableDataChanged();
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
            StudyPair.Forbidden forbidden = ((LessonTableModel)table.getModel()).getForbidden(lessonTableModel, row, column);

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
        if (e.getButton() == MouseEvent.BUTTON1) {

            int row = jTable.rowAtPoint(e.getPoint());
            int column = jTable.columnAtPoint(e.getPoint());

            HashSet<StudyPair.Forbidden> forbiddenHashSet = new HashSet<>();
            HashMap<StudyPair.Forbidden, HashSet<Point>> hashMap = lessonTableModel.getfMap();
            for (HashMap.Entry<StudyPair.Forbidden, HashSet<Point>> item : hashMap.entrySet()) {
                forbiddenHashSet.addAll(item.getValue().stream().filter(point -> point.getX() == row).map(point -> item.getKey()).collect(Collectors.toList()));
            }
            if (!forbiddenHashSet.isEmpty()) {
                if (forbiddenHashSet.contains(StudyPair.Forbidden.ROW_FORBIDDEN)) {
                    int r = JOptionPane.showConfirmDialog(
                            null,
                            "Ви намагаєтеся встановити аудиторію або виклачадача, \nякий зайнятий у цей час. Продовжити?",
                            "Повідомлення", JOptionPane.YES_NO_OPTION
                    );
                    if (r == JOptionPane.NO_OPTION) return;
                } else if (forbiddenHashSet.contains(StudyPair.Forbidden.DAY_FORBIDDEN)) {
                    int r = JOptionPane.showConfirmDialog(
                            null,
                            "Викладач виявив бажання залишатися вільним в цей час. Продовжити?",
                            "Повідомлення", JOptionPane.YES_NO_OPTION
                    );
                    if (r == JOptionPane.NO_OPTION) return;
                }
            }
            if (semesterLoad != null) {
                LoadingCheckResult result = checkSemesterLoad(nowStudyPair, row, column);

                if (result.getResult() == LoadingCheckResults.GROUP_FAULT) {
                    JOptionPane.showMessageDialog(null, "Помилка роботи. Перезавантажте систему");
                } else if (result.getResult() == LoadingCheckResults.LESSON_NOT_USE_IN_THIS) {
                    JOptionPane.showMessageDialog(null, "Даний не може бути використаний для цієї групи");
                } else if (result.getResult() == LoadingCheckResults.MORE) {
                    int dialogResult = JOptionPane.showConfirmDialog(null, "Встановлення предмету перевищує заданий ліміт на тиждень.\n" +
                            "Рекомендовано: " + result.getRecommendLoad() + "; на даний момент: " + result.getNowLoad() + ";" +
                            "\nпісля встановлення буде: " + result.getNextLoad() + "\nПродовжити?", "Увага", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (dialogResult != JOptionPane.YES_OPTION) return;
                }
            }
            lessonTableModel.setValueAt(nowStudyPair, row, column);
            analyzeTable(row, column);

        } else if (e.getButton() == MouseEvent.BUTTON2) {

            int row = jTable.rowAtPoint(e.getPoint());
            int column = jTable.columnAtPoint(e.getPoint());
            Object o = jTable.getValueAt(row, column);
            if (o instanceof StudyPairLonely) {
                StudyPairLonely lonely = (StudyPairLonely) o;
                lessonCBox.setSelectedItem(lonely.getLesson());
                teacherCBox.setSelectedItem(lonely.getTeacher());
                auditoryCBox.setSelectedItem(lonely.getAuditory());
            } else if (o instanceof StudyPairDouble) {
                StudyPairDouble pairDouble = (StudyPairDouble) o;
                if (!pairDouble.getNumerator().equals(nowStudyPair) && !pairDouble.getNumerator().isEmpty()) {
                    lessonCBox.setSelectedItem(pairDouble.getNumerator().getLesson());
                    teacherCBox.setSelectedItem(pairDouble.getNumerator().getTeacher());
                    auditoryCBox.setSelectedItem(pairDouble.getNumerator().getAuditory());
                } else if (!pairDouble.getDenominator().equals(nowStudyPair) && !pairDouble.getDenominator().isEmpty()) {
                    lessonCBox.setSelectedItem(pairDouble.getDenominator().getLesson());
                    teacherCBox.setSelectedItem(pairDouble.getDenominator().getTeacher());
                    auditoryCBox.setSelectedItem(pairDouble.getDenominator().getAuditory());
                }
            }

        } else if (e.getButton() == MouseEvent.BUTTON3) {

            tablePopupMenu.show(jTable, e.getX(), e.getY());
            cursor = e.getPoint();

        }
    }

    private LoadingCheckResult checkSemesterLoad(StudyPair nowStudyPair, int row, int column) {
        LessonsUnit unit = getLessonTableModel().units.get(column / COLUMN_REPEAT);
        GroupLoad groupLoad = semesterLoad.getGroupLoadByGroup(unit.getGroup());
        if (groupLoad == null) return new LoadingCheckResult(LoadingCheckResults.GROUP_FAULT, -1, -1, -1);

        Lesson lesson = null;
        float modification = 0;
        if (nowStudyPair instanceof StudyPairLonely) {
            lesson = ((StudyPairLonely)nowStudyPair).getLesson();
            modification = 1;
        } else if (nowStudyPair instanceof StudyPairDouble) {
            StudyPairDouble pairDouble = (StudyPairDouble)nowStudyPair;
            if (!pairDouble.getNumerator().isEmpty()) {
                lesson = pairDouble.getNumerator().getLesson();
                modification = 0.5f;
            } else {
                lesson = pairDouble.getDenominator().getLesson();
                modification = 0.5f;
            }
        }

        LoadUnit loadUnit = groupLoad.getLoadUnitByLesson(lesson);
        if (loadUnit == null) return new LoadingCheckResult(LoadingCheckResults.LESSON_NOT_USE_IN_THIS, -1, -1, -1);

        float count = 0;
        for (StudyPair pair : unit.getPairs()) {
            if (pair instanceof StudyPairLonely) {
                StudyPairLonely lonely = (StudyPairLonely) pair;
                if (lonely.isEmpty()) continue;
                count += lonely.getLesson().equals(lesson) ? 1 : 0;
            } else if (pair instanceof StudyPairDouble) {
                StudyPairLonely numerator = ((StudyPairDouble)pair).getNumerator();
                StudyPairLonely denominator = ((StudyPairDouble)pair).getDenominator();
                if (!numerator.isEmpty()) {
                    count += numerator.getLesson().equals(lesson) ? 0.5 : 0;
                }
                if (!denominator.isEmpty()) {
                    count += denominator.getLesson().equals(lesson) ? 0.5 : 0;
                }
            }
        }

        if (count + modification > loadUnit.getWeekLoad()) return new LoadingCheckResult(LoadingCheckResults.MORE, loadUnit.getWeekLoad(), count, count + modification);
        return new LoadingCheckResult(LoadingCheckResults.GOOD, loadUnit.getWeekLoad(), count, count + modification);
    }

    private class LoadingCheckResult {
        private LoadingCheckResults result;
        private float recommendLoad;
        private float nowLoad;
        private float nextLoad;

        public LoadingCheckResult(LoadingCheckResults result, float recommendLoad, float nowLoad, float nextLoad) {
            this.result = result;
            this.recommendLoad = recommendLoad;
            this.nowLoad = nowLoad;
            this.nextLoad = nextLoad;
        }

        public LoadingCheckResults getResult() {
            return result;
        }

        public float getRecommendLoad() {
            return recommendLoad;
        }

        public float getNowLoad() {
            return nowLoad;
        }

        public float getNextLoad() {
            return nextLoad;
        }
    }

    private enum LoadingCheckResults {
        GOOD, MORE, GROUP_FAULT, LESSON_NOT_USE_IN_THIS
    }

    private void analyzeTable(int row, int column) {
        LessonsUnit unit = getLessonTableModel().units.get(column / COLUMN_REPEAT);
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
                "(Ч) " + (pairCountNumerator * 2) + " годин; (З) " + (pairCountDenominator * 2) + " годин; в середньому на день " + ((pairCountDenominator * 2 + pairCountNumerator * 2) / 10)
        );
        studyPairInWeekLabel.setText(pairCountNumerator == pairCountDenominator ?
                (pairCountNumerator) + " пар; в середньому на день " + ((pairCountDenominator + pairCountNumerator) / 10) :
                "(Ч) " + (pairCountNumerator) + " годин; (З) " + (pairCountDenominator) + " годин; в середньому на день " + ((pairCountDenominator + pairCountNumerator) / 10)
        );
    }
}