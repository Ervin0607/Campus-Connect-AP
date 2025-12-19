import Backend.AdminUtils.UserRoleFactory;
import Frontend.ui.LoginWindow;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }


        SwingUtilities.invokeLater(() -> {
            new LoginWindow().setVisible(true);
        });


    }
}
