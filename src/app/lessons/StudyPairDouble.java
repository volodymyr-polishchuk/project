package app.lessons;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created by Vladimir on 06/02/18.
 **/
public class StudyPairDouble extends StudyPair {

    private StudyPairLonely numerator;
    private StudyPairLonely denominator;

    public StudyPairDouble() {
        numerator = new StudyPairLonely();
        denominator = new StudyPairLonely();
    }

    public StudyPairDouble(StudyPairLonely numerator, StudyPairLonely denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public JComponent getRendererComponent(Query data) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        JLabel labelTop = new JLabel();
        JLabel labelBottom = new JLabel();
        labelTop.setHorizontalAlignment(SwingConstants.CENTER);
        labelBottom.setHorizontalAlignment(SwingConstants.CENTER);
        switch (data) {
            case LESSON: {
                labelTop.setText("Ч/ " + numerator.getLesson().getName());
                labelBottom.setText("З\\ " + denominator.getLesson().getName());
            } break;
            case TEACHER: {
                labelTop.setText(numerator.getTeacher().getName());
                labelBottom.setText(denominator.getTeacher().getName());
            } break;
            case AUDITORY: {
                labelTop.setText(numerator.getAuditory().getName());
                labelBottom.setText(denominator.getAuditory().getName());
            } break;
        }
        panel.add(labelTop);
        panel.add(labelBottom);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setBackground(Color.WHITE);
        return panel;
    }

    @Override
    public Forbidden[] getForbidden(StudyPair studyPair) {
        if (studyPair instanceof StudyPairDouble) {
            StudyPairDouble pairDouble = (StudyPairDouble) studyPair;
            HashSet<Forbidden> fb = new HashSet<>();
            Collections.addAll(fb, this.numerator.getForbidden(pairDouble.numerator));
            Collections.addAll(fb, this.denominator.getForbidden(pairDouble.numerator));
            Collections.addAll(fb, this.numerator.getForbidden(pairDouble.denominator));
            Collections.addAll(fb, this.denominator.getForbidden(pairDouble.denominator));
            Forbidden[] forbids = new Forbidden[fb.size()];
            int i = 0;
            for (Forbidden forbidden: fb) {
                forbids[i++] = forbidden;
            }
            return forbids;
        } else if (studyPair instanceof StudyPairLonely) {
            StudyPairLonely lonely = (StudyPairLonely) studyPair;
            HashSet<Forbidden> fb = new HashSet<>();
            Collections.addAll(fb, this.numerator.getForbidden(lonely));
            Collections.addAll(fb, this.denominator.getForbidden(lonely));
            Forbidden[] forbids = new Forbidden[fb.size()];
            int i = 0;
            for (Forbidden forbidden: fb) {
                forbids[i++] = forbidden;
            }
            return forbids;
        }
        return new Forbidden[] {Forbidden.UNKNOWN_FORBIDDEN};
    }

    @Override
    public Forbidden[] getForbidden(StudyPair studyPair, java.util.List<LessonsUnit> units, int row, int col, int pairPerDay, int dayPerWeek) {
        return getForbidden(studyPair);
    }

    @Override
    public Forbidden[] getSelfForbidden() {
        return new Forbidden[0];
    }
}