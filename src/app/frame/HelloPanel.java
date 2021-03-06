package app.frame;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Vladimir on 22/02/18.
 **/
public class HelloPanel extends JPanel {
    private JPanel contentPane;
    private JButton createScheduleButton;
    private JButton viewLessonsButton;
    private JButton createLessonsButton;
    private JButton viewScheduleButton;
    private JButton addGroupButton;
    private JButton addAuditoryButton;
    private JButton addTeacherButton;
    private JButton addLessonButton;
    private JButton aboutButton;
    private JButton userHelpButton;
    private MainForm owner;

    HelloPanel(String title, MainForm owner) {
        this.owner = owner;
        setLayout(new GridLayout());
        setName(title);
        add(contentPane);
        initialButtons();
    }

    private void initialButtons() {
        createScheduleButton.addActionListener(owner.getMainFormMenuBar()::MenuItemCreateSchedule);
        viewScheduleButton.addActionListener(owner.getMainFormMenuBar()::MenuItemViewSchedule);
        createLessonsButton.addActionListener(owner.getMainFormMenuBar()::MenuItemCreateLessons);
        viewLessonsButton.addActionListener(owner.getMainFormMenuBar()::MenuItemViewLessons);
        addGroupButton.addActionListener(owner.getMainFormMenuBar()::MenuItemDataGroup);
        addAuditoryButton.addActionListener(owner.getMainFormMenuBar()::MenuItemDataAuditory);
        addTeacherButton.addActionListener(owner.getMainFormMenuBar()::MenuItemDataTeacher);
        addLessonButton.addActionListener(owner.getMainFormMenuBar()::MenuItemDataLesson);
        aboutButton.addActionListener(owner.getMainFormMenuBar()::MenuItemAbout);
        userHelpButton.addActionListener(owner.getMainFormMenuBar()::MenuItemHelp);
    }
}
