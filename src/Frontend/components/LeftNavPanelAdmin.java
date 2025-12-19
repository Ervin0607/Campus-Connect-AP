package Frontend.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LeftNavPanelAdmin extends JPanel {

    public static class Theme {
        public final Color PanelColor;
        public final Color AccentColor;
        public final Color TextPrimaryColor;
        public final Color TextSecondaryColor;

        public Theme(Color PanelColor, Color AccentColor, Color TextPrimaryColor, Color TextSecondaryColor) {
            this.PanelColor = PanelColor;
            this.AccentColor = AccentColor;
            this.TextPrimaryColor = TextPrimaryColor;
            this.TextSecondaryColor = TextSecondaryColor;
        }
    }

    private final Theme CurrentTheme;
    private final Map<String, JButton> NavigationButtonsMap = new LinkedHashMap<>();
    private String ActiveLabel;
    private Consumer<String> SelectionHandler;

    private static final String[] TopMenuItems = {
            "Overview", "Users", "Offerings",
            "Courses"
    };

    private static final String[] BottomMenuItems = {
            "System Health", "Settings", "Log Out"
    };

    public LeftNavPanelAdmin(Theme CurrentTheme, String InitialActiveLabel) {
        this.CurrentTheme = CurrentTheme;
        this.ActiveLabel = (InitialActiveLabel == null) ? TopMenuItems[0] : InitialActiveLabel;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CurrentTheme.PanelColor);
        setPreferredSize(new Dimension(240, 0));
        setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel LogoLabel = new JLabel("ERP Admin");
        LogoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        LogoLabel.setForeground(CurrentTheme.TextPrimaryColor);
        LogoLabel.setBorder(new EmptyBorder(0, 20, 30, 20));
        LogoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(LogoLabel);

        for (String MenuItem : TopMenuItems) {
            AddNavItem(MenuItem);
        }

        add(Box.createVerticalGlue());

        for (String MenuItem : BottomMenuItems) {
            AddNavItem(MenuItem);
        }

        ApplyActiveStyles();
    }

    private void AddNavItem(String ItemText) {
        JButton NavButton = new JButton(ItemText);
        NavButton.setHorizontalAlignment(SwingConstants.LEFT);
        NavButton.setBorder(new EmptyBorder(12, 25, 12, 25));
        NavButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        NavButton.setFocusPainted(false);
        NavButton.setContentAreaFilled(false);
        NavButton.setOpaque(false);
        NavButton.setForeground(CurrentTheme.TextSecondaryColor);

        JPanel ButtonContainerPanel = new JPanel(new BorderLayout());
        ButtonContainerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, NavButton.getPreferredSize().height));
        ButtonContainerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonContainerPanel.setBackground(CurrentTheme.PanelColor);
        ButtonContainerPanel.add(NavButton, BorderLayout.CENTER);
        add(ButtonContainerPanel);

        NavButton.addActionListener(ActionEventObject -> {
            SetActive(ItemText);
            if (SelectionHandler != null) {
                SelectionHandler.accept(ItemText);
            }
        });

        NavigationButtonsMap.put(ItemText, NavButton);
    }

    public void SetOnSelect(Consumer<String> SelectionHandler) {
        this.SelectionHandler = SelectionHandler;
    }

    public void SetActive(String LabelText) {
        if (LabelText == null || LabelText.equals(ActiveLabel)) {
            return;
        }
        ActiveLabel = LabelText;
        ApplyActiveStyles();
    }

    private void ApplyActiveStyles() {
        for (Map.Entry<String, JButton> Entry : NavigationButtonsMap.entrySet()) {
            boolean IsActive = Entry.getKey().equals(ActiveLabel);
            JButton NavButton = Entry.getValue();
            if (IsActive) {
                NavButton.setContentAreaFilled(true);
                NavButton.setOpaque(true);
                NavButton.setBackground(CurrentTheme.AccentColor);
                NavButton.setForeground(Color.BLACK);
                NavButton.getParent().setBackground(CurrentTheme.AccentColor);
            } else {
                NavButton.setContentAreaFilled(false);
                NavButton.setOpaque(false);
                NavButton.setBackground(CurrentTheme.PanelColor);
                NavButton.setForeground(CurrentTheme.TextSecondaryColor);
                NavButton.getParent().setBackground(CurrentTheme.PanelColor);
            }
        }
        revalidate();
        repaint();
    }
}
