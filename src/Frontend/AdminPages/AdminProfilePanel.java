package Frontend.AdminPages;

import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class AdminProfilePanel extends JPanel {

    private static final Color COLOR_BACKGROUND      = new Color(0x121212);
    private static final Color COLOR_PANEL           = new Color(0x1E1E1E);
    private static final Color COLOR_TEXT_PRIMARY    = new Color(0xFFFFFF);
    private static final Color COLOR_TEXT_SECONDARY  = new Color(0xB0B0B0);
    private static final Color COLOR_ACCENT          = new Color(0xB0F2B4);

    private final int UserID;

    private User CurrentUser;

    private JLabel LabelValueID;
    private JLabel LabelValueRole;
    private JLabel LabelValueStatus;

    private JTextField TextUserName;
    private JPasswordField TextNewPassword;
    private JPasswordField TextConfirmPassword;

    private JButton ButtonSaveChanges;

    public AdminProfilePanel(int UserID) {
        this.UserID = UserID;

        setOpaque(true);
        setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(COLOR_PANEL);
        CardPanel.setLayout(new BorderLayout(20, 20));
        CardPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel HeaderLabel = new JLabel("Admin Profile");
        HeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        HeaderLabel.setForeground(COLOR_TEXT_PRIMARY);
        CardPanel.add(HeaderLabel, BorderLayout.NORTH);

        JPanel CenterPanel = new JPanel();
        CenterPanel.setOpaque(false);
        CenterPanel.setLayout(new BoxLayout(CenterPanel, BoxLayout.Y_AXIS));
        CenterPanel.add(CreateReadOnlyInfoPanel());
        CenterPanel.add(Box.createVerticalStrut(20));
        CenterPanel.add(CreateEditableSettingsPanel());
        CardPanel.add(CenterPanel, BorderLayout.CENTER);

        add(CardPanel, BorderLayout.CENTER);

        LoadDataFromDatabase();
    }

    private void LoadDataFromDatabase() {
        try {
            CurrentUser = UserAuthenticationHandler.FindUserID(UserID);

            if (CurrentUser == null) {
                ShowError("User account not found for ID: " + UserID);
                return;
            }

            PopulateUI();

        }
        catch (SQLException SqlException) {
            SqlException.printStackTrace();
            ShowError("Failed to load profile data: " + SqlException.getMessage());
        }
    }

    private void PopulateUI() {
        if (CurrentUser == null) return;

        LabelValueID.setText(String.valueOf(CurrentUser.GetUserID()));
        LabelValueRole.setText(CurrentUser.GetRole());
        LabelValueStatus.setText(CurrentUser.GetStatus());

        TextUserName.setText(CurrentUser.GetUserName());

        TextNewPassword.setText("");
        TextConfirmPassword.setText("");
    }

    private JPanel CreateReadOnlyInfoPanel() {
        JPanel ContentPanel = new JPanel();
        ContentPanel.setOpaque(false);
        ContentPanel.setLayout(new GridBagLayout());
        ContentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x333333)),
                "System Details (Read-Only)",
                0, 0, new Font("SansSerif", Font.BOLD, 14), COLOR_TEXT_SECONDARY
        ));

        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.insets = new Insets(8, 8, 8, 8);
        Constraints.anchor = GridBagConstraints.WEST;
        Constraints.gridx = 0;
        Constraints.gridy = 0;

        ContentPanel.add(CreateSecondaryLabel("User ID:"), Constraints);
        Constraints.gridx = 1;
        LabelValueID = CreatePrimaryLabel("-");
        ContentPanel.add(LabelValueID, Constraints);
        Constraints.gridx = 0;
        Constraints.gridy++;
        ContentPanel.add(CreateSecondaryLabel("Role:"), Constraints);
        Constraints.gridx = 1;
        LabelValueRole = CreatePrimaryLabel("-");
        ContentPanel.add(LabelValueRole, Constraints);

        Constraints.gridx = 0;
        Constraints.gridy++;
        ContentPanel.add(CreateSecondaryLabel("Account Status:"), Constraints);
        Constraints.gridx = 1;
        LabelValueStatus = CreatePrimaryLabel("-");
        ContentPanel.add(LabelValueStatus, Constraints);

        return ContentPanel;
    }

    private JPanel CreateEditableSettingsPanel() {
        JPanel AccountPanel = new JPanel();
        AccountPanel.setOpaque(false);
        AccountPanel.setLayout(new GridBagLayout());
        AccountPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x333333)),
                "Account Settings",
                0, 0, new Font("SansSerif", Font.BOLD, 14), COLOR_TEXT_SECONDARY
        ));

        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.insets = new Insets(8, 8, 8, 8);
        Constraints.anchor = GridBagConstraints.WEST;
        Constraints.fill = GridBagConstraints.HORIZONTAL;
        Constraints.weightx = 1.0;
        Constraints.gridx = 0;
        Constraints.gridy = 0;

        AccountPanel.add(CreateSecondaryLabel("Username (Login):"), Constraints);
        Constraints.gridx = 1;
        TextUserName = new JTextField(20);
        StyleTextField(TextUserName);
        AccountPanel.add(TextUserName, Constraints);

        Constraints.gridx = 0;
        Constraints.gridy++;
        Constraints.gridwidth = 2;
        JSeparator Separator = new JSeparator();
        Separator.setBackground(new Color(0x333333));
        Separator.setForeground(new Color(0x333333));
        AccountPanel.add(Separator, Constraints);
        Constraints.gridwidth = 1;
        Constraints.gridx = 0;
        Constraints.gridy++;
        AccountPanel.add(CreateSecondaryLabel("New Password:"), Constraints);
        Constraints.gridx = 1;
        TextNewPassword = new JPasswordField(20);
        StyleTextField(TextNewPassword);
        AccountPanel.add(TextNewPassword, Constraints);
        Constraints.gridx = 0;
        Constraints.gridy++;
        AccountPanel.add(CreateSecondaryLabel("Confirm Password:"), Constraints);
        Constraints.gridx = 1;
        TextConfirmPassword = new JPasswordField(20);
        StyleTextField(TextConfirmPassword);
        AccountPanel.add(TextConfirmPassword, Constraints);

        Constraints.gridx = 0;
        Constraints.gridy++;
        Constraints.gridwidth = 2;
        Constraints.anchor = GridBagConstraints.CENTER;
        ButtonSaveChanges = new JButton("Save Changes");
        StyleButton(ButtonSaveChanges);
        ButtonSaveChanges.addActionListener(new SaveChangesListener());
        AccountPanel.add(ButtonSaveChanges, Constraints);

        return AccountPanel;
    }

    private JLabel CreatePrimaryLabel(String Text) {
        JLabel Label = new JLabel(Text);
        Label.setForeground(COLOR_TEXT_PRIMARY);
        Label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return Label;
    }

    private JLabel CreateSecondaryLabel(String Text) {
        JLabel Label = new JLabel(Text);
        Label.setForeground(COLOR_TEXT_SECONDARY);
        Label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return Label;
    }

    private void StyleTextField(JTextField TextFieldInstance) {
        TextFieldInstance.setBackground(new Color(0x2A2A2A));
        TextFieldInstance.setForeground(COLOR_TEXT_PRIMARY);
        TextFieldInstance.setCaretColor(COLOR_TEXT_PRIMARY);
        TextFieldInstance.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x444444)),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void StyleButton(JButton ButtonInstance) {
        ButtonInstance.setBackground(COLOR_ACCENT);
        ButtonInstance.setForeground(Color.BLACK);
        ButtonInstance.setFocusPainted(false);
        ButtonInstance.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        ButtonInstance.setFont(new Font("SansSerif", Font.BOLD, 14));
        ButtonInstance.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void ShowError(String Message) {
        JOptionPane.showMessageDialog(this, Message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class SaveChangesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent Event) {
            if (CurrentUser == null) return;

            String NewUserName = TextUserName.getText().trim();
            String NewPassword = new String(TextNewPassword.getPassword());
            String ConfirmPassword = new String(TextConfirmPassword.getPassword());

            boolean ShouldUpdateUserName = false;
            boolean ShouldUpdatePassword = false;

            if (NewUserName.isEmpty()) {
                JOptionPane.showMessageDialog(AdminProfilePanel.this, "Username cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!NewUserName.equals(CurrentUser.GetUserName())) {
                ShouldUpdateUserName = true;
            }

            if (!NewPassword.isEmpty()) {
                if (!NewPassword.equals(ConfirmPassword)) {
                    JOptionPane.showMessageDialog(AdminProfilePanel.this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ShouldUpdatePassword = true;
            }

            if (!ShouldUpdateUserName && !ShouldUpdatePassword) {
                JOptionPane.showMessageDialog(AdminProfilePanel.this, "No changes detected.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try {
                boolean IsSuccessful = true;

                if (ShouldUpdateUserName) {
                    boolean IsUserNameUpdated = UserAuthenticationHandler.UpdateUser(CurrentUser, NewUserName);
                    IsSuccessful = IsSuccessful && IsUserNameUpdated;
                    if (IsUserNameUpdated) CurrentUser.SetUserName(NewUserName);
                }

                if (ShouldUpdatePassword) {
                    boolean IsPasswordUpdated = UserAuthenticationHandler.UpdateUserPassword(CurrentUser, NewPassword);
                    IsSuccessful = IsSuccessful && IsPasswordUpdated;
                }

                if (IsSuccessful) {
                    JOptionPane.showMessageDialog(AdminProfilePanel.this, "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    TextNewPassword.setText("");
                    TextConfirmPassword.setText("");
                } else {
                    ShowError("Failed to save changes. Please try again.");
                }

            } catch (SQLException SqlException) {
                SqlException.printStackTrace();
                ShowError("Database Error: " + SqlException.getMessage());
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private int CornerRadius = 15;
        public RoundedPanel() {
            super();
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics GraphicsInstance) {
            Graphics2D Graphics2DInstance = (Graphics2D) GraphicsInstance.create();
            Graphics2DInstance.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Graphics2DInstance.setColor(getBackground());
            Graphics2DInstance.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CornerRadius, CornerRadius));
            Graphics2DInstance.dispose();
            super.paintComponent(GraphicsInstance);
        }
    }
}
