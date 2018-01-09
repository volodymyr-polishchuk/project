package Frame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;

/**
 * Created by Vladimir on 09/01/18.
 **/
public class mainForm extends JFrame {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JLabel statusBar;
    private JToolBar toolBar;

    public mainForm() {
        setJMenuBar(new myMenuBar());

        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 600);
        setExtendedState(MAXIMIZED_BOTH);
        setTitle("Система автоматизації складання розкладу занять");
        InitJToolBar(toolBar);
        setVisible(true);
    }

    public void addTab(JPanel jPanel) {
        tabbedPane.add(jPanel);
    }

    public void setStatusBar(String s) {
        statusBar.setText(s);
    }

    private void InitJToolBar(JToolBar jToolBar) {
        try {
            jToolBar.add(new JToggleButton(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/resource/cloud-computing.png")))));
//            jToolBar.add(new JToggleButton(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/resource/cloud-computing.png")))));
            //Додання панелі інструментів
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class myMenuBar extends JMenuBar {
    myMenuBar() {
// Створення головного меню програми
        JMenu m1 = new JMenu("Програма");
        m1.add(new JMenuItem("Виконати підключення до іншого сервера"));
        m1.add(new JMenuItem("Налаштування"));
        m1.add(new JPopupMenu.Separator());
        m1.add(new JMenuItem("Вихід"));
        add(m1);
        JMenu m2 = new JMenu("Навчальний графік");
        m2.add(new JMenuItem("Створити навчальний графік"));
        m2.add(new JMenuItem("Редагувати існуючий графік"));
        m2.add(new JMenuItem("Видалити або архівувати графік"));
        add(m2);
        JMenu m3 = new JMenu("Розклад занять");
        m3.add(new JMenuItem("Створити розклад занять"));
        m3.add(new JMenuItem("Редагувати існуючих розклад"));
        m3.add(new JMenuItem("Видалити або архівувати розклад"));
        add(m3);
        JMenu m4 = new JMenu("Дані");
        m4.add(new JMenuItem("Аудиторії"));
        m4.add(new JMenuItem("Викладачі"));
        m4.add(new JMenuItem("Предмети"));
        m4.add(new JMenuItem("Навчальний предмет"));
        add(m4);

    }
}

class Test000 {
    public static void main(String[] args) {
        mainForm form = new mainForm();
        form.setStatusBar("Всі дані завантажені до програми");
        JPanel panel = new JPanel();
        String[][] data = {{"Завірюха В. П.", "402", "Основи програмування"},
                {"Харченко О. О.", "403", "ІСТО"},
                {"Заболотній Ю. Л.", "407", "WEB"}};
        String[] col = {"Викладач", "Аудиторія", "Предмет"};
        JTable table = new JTable(data, col);
        panel.add(table);
        panel.setName("Викладачі");
        form.addTab(panel);
    }
}