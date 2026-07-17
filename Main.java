import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) {
        System.out.println("Оконное приложение запущено");

        // устанавливаем глобальный шрифт
        setGlobalFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    // установка глобального шрифта для swing-компонентов
    public static void setGlobalFont(Font font) {
        Enumeration<?> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
            }
        }
    }
}