package Frontend.InstructorPages;

import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.Instructor;
import Backend.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class InstructorProfilePanel extends JPanel {

    private static final Color ColorBackground     = new Color(0x121212);
    private static final Color ColorPanel          = new Color(0x1E1E1E);
    private static final Color ColorTextPrimary    = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary  = new Color(0xB0B0B0);
    private static final Color ColorAccent         = new Color(0x3F51B5);

    private final int InstructorID;

    private Instructor CurrentInstructor;
    private User CurrentUser;

    private JLabel LabelValueInstructorId;
    private JLabel LabelValueDepartment;
    private JLabel LabelValueQualification;
    private JLabel LabelValueJoiningDate;
    private JLabel LabelValueUsername;

    private JTextField TextFieldName;
    private JTextField TextFieldEmail;
    private JPasswordField TextFieldNewPassword;
    private JPasswordField TextFieldConfirmPassword;

    private JButton ButtonSaveChanges;

    public InstructorProfilePanel(int CurrentInstructorID) {
        this.InstructorID = CurrentInstructorID;

        setOpaque(true);
        setBackground(ColorBackground);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(ColorPanel);
        CardPanel.setLayout(new BorderLayout(20, 20));
        CardPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel HeaderLabel = new JLabel("Instructor Profile");
        HeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        HeaderLabel.setForeground(ColorTextPrimary);
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
            CurrentInstructor = InstructorHandler.FindInstructorByInstructorID(InstructorID);
            if (CurrentInstructor == null) {
                ShowError("Instructor not found for ID: " + InstructorID);
                return;
            }

            int UserId = CurrentInstructor.GetUserID();
            CurrentUser = UserAuthenticationHandler.FindUserID(UserId);
            if (CurrentUser == null) {
                ShowError("User account not found for UserID: " + UserId);
                return;
            }

            PopulateUI();

        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
            ShowError("Failed to load profile data: " + ExceptionObject.getMessage());
        }
    }

    private void PopulateUI() {
        if (CurrentInstructor == null || CurrentUser == null) {
            return;
        }

        LabelValueInstructorId.setText(String.valueOf(CurrentInstructor.GetInstructorID()));
        LabelValueDepartment.setText(CurrentInstructor.GetDepartment());
        LabelValueQualification.setText(CurrentInstructor.GetQualification());
        LabelValueJoiningDate.setText(CurrentInstructor.GetJoiningDate().toString());
        LabelValueUsername.setText(CurrentUser.GetUserName());

        TextFieldName.setText(CurrentInstructor.GetName());
        TextFieldEmail.setText(CurrentInstructor.GetEmail());

        TextFieldNewPassword.setText("");
        TextFieldConfirmPassword.setText("");
    }

    private JPanel CreateReadOnlyInfoPanel() {
        JPanel ContentPanel = new JPanel();
        ContentPanel.setOpaque(false);
        ContentPanel.setLayout(new GridBagLayout());
        ContentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0x333333)), "Professional Details (Read-Only)", 0, 0, new Font("SansSerif", Font.BOLD, 14), ColorTextSecondary));

        GridBagConstraints GridConstraints = new GridBagConstraints();
        GridConstraints.insets = new Insets(8, 8, 8, 8);
        GridConstraints.anchor = GridBagConstraints.WEST;
        GridConstraints.gridx = 0;
        GridConstraints.gridy = 0;

        ContentPanel.add(CreateSecondaryLabel("Instructor ID:"), GridConstraints);
        GridConstraints.gridx = 1;
        LabelValueInstructorId = CreatePrimaryLabel("-");
        ContentPanel.add(LabelValueInstructorId, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        ContentPanel.add(CreateSecondaryLabel("Department:"), GridConstraints);
        GridConstraints.gridx = 1;
        LabelValueDepartment = CreatePrimaryLabel("-");
        ContentPanel.add(LabelValueDepartment, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        ContentPanel.add(CreateSecondaryLabel("Qualification:"), GridConstraints);
        GridConstraints.gridx = 1;
        LabelValueQualification = CreatePrimaryLabel("-");
        ContentPanel.add(LabelValueQualification, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        ContentPanel.add(CreateSecondaryLabel("Joining Date:"), GridConstraints);
        GridConstraints.gridx = 1;
        LabelValueJoiningDate = CreatePrimaryLabel("-");
        ContentPanel.add(LabelValueJoiningDate, GridConstraints);

        return ContentPanel;
    }

    private JPanel CreateEditableSettingsPanel() {
        JPanel AccountPanel = new JPanel();
        AccountPanel.setOpaque(false);
        AccountPanel.setLayout(new GridBagLayout());
        AccountPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x333333)),
                "Personal Settings (Editable)",
                0, 0,
                new Font("SansSerif", Font.BOLD, 14),
                ColorTextSecondary
        ));

        GridBagConstraints GridConstraints = new GridBagConstraints();
        GridConstraints.insets = new Insets(8, 8, 8, 8);
        GridConstraints.anchor = GridBagConstraints.WEST;
        GridConstraints.fill = GridBagConstraints.HORIZONTAL;
        GridConstraints.weightx = 1.0;
        GridConstraints.gridx = 0;
        GridConstraints.gridy = 0;

        AccountPanel.add(CreateSecondaryLabel("Username (Login):"), GridConstraints);
        GridConstraints.gridx = 1;
        LabelValueUsername = CreatePrimaryLabel("-");
        AccountPanel.add(LabelValueUsername, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        AccountPanel.add(CreateSecondaryLabel("Full Name:"), GridConstraints);
        GridConstraints.gridx = 1;
        TextFieldName = new JTextField(20);
        StyleTextField(TextFieldName);
        AccountPanel.add(TextFieldName, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        AccountPanel.add(CreateSecondaryLabel("Email Address:"), GridConstraints);
        GridConstraints.gridx = 1;
        TextFieldEmail = new JTextField(20);
        StyleTextField(TextFieldEmail);
        AccountPanel.add(TextFieldEmail, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        GridConstraints.gridwidth = 2;
        JSeparator Separator = new JSeparator();
        Separator.setBackground(new Color(0x333333));
        Separator.setForeground(new Color(0x333333));
        AccountPanel.add(Separator, GridConstraints);
        GridConstraints.gridwidth = 1;

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        AccountPanel.add(CreateSecondaryLabel("New Password:"), GridConstraints);
        GridConstraints.gridx = 1;
        TextFieldNewPassword = new JPasswordField(20);
        StyleTextField(TextFieldNewPassword);
        AccountPanel.add(TextFieldNewPassword, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        AccountPanel.add(CreateSecondaryLabel("Confirm Password:"), GridConstraints);
        GridConstraints.gridx = 1;
        TextFieldConfirmPassword = new JPasswordField(20);
        StyleTextField(TextFieldConfirmPassword);
        AccountPanel.add(TextFieldConfirmPassword, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        GridConstraints.gridwidth = 2;
        GridConstraints.anchor = GridBagConstraints.CENTER;
        ButtonSaveChanges = new JButton("Save Changes");
        StyleButton(ButtonSaveChanges);
        ButtonSaveChanges.addActionListener(new SaveChangesListener());
        AccountPanel.add(ButtonSaveChanges, GridConstraints);

        return AccountPanel;
    }

    private JLabel CreatePrimaryLabel(String Text) {
        JLabel Label = new JLabel(Text);
        Label.setForeground(ColorTextPrimary);
        Label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return Label;
    }

    private JLabel CreateSecondaryLabel(String Text) {
        JLabel Label = new JLabel(Text);
        Label.setForeground(ColorTextSecondary);
        Label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return Label;
    }

    private void StyleTextField(JTextField TextField) {
        TextField.setBackground(new Color(0x2A2A2A));
        TextField.setForeground(ColorTextPrimary);
        TextField.setCaretColor(ColorTextPrimary);
        TextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x444444)),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void StyleButton(JButton Button) {
        Button.setBackground(ColorAccent);
        Button.setForeground(Color.WHITE);
        Button.setFocusPainted(false);
        Button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        Button.setFont(new Font("SansSerif", Font.BOLD, 14));
        Button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void ShowError(String Message) {
        JOptionPane.showMessageDialog(this, Message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class SaveChangesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent EventObject) {
            if (CurrentInstructor == null) {
                return;
            }

            String NewName = TextFieldName.getText().trim();
            String NewEmail = TextFieldEmail.getText().trim();
            String PasswordFirst = new String(TextFieldNewPassword.getPassword());
            String PasswordSecond = new String(TextFieldConfirmPassword.getPassword());

            boolean ShouldUpdateDetails = false;
            boolean ShouldUpdatePassword = false;

            if (NewName.isEmpty() || NewEmail.isEmpty()) {
                JOptionPane.showMessageDialog(InstructorProfilePanel.this,
                        "Name and Email cannot be empty.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!NewName.equals(CurrentInstructor.GetName()) || !NewEmail.equals(CurrentInstructor.GetEmail())) {
                ShouldUpdateDetails = true;
            }

            if (!PasswordFirst.isEmpty() || !PasswordSecond.isEmpty()) {
                if (!PasswordFirst.equals(PasswordSecond)) {
                    JOptionPane.showMessageDialog(InstructorProfilePanel.this,
                            "Passwords do not match.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ShouldUpdatePassword = true;
            }

            if (!ShouldUpdateDetails && !ShouldUpdatePassword) {
                JOptionPane.showMessageDialog(InstructorProfilePanel.this,
                        "No changes detected.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try {
                boolean IsSuccess = true;

                if (ShouldUpdateDetails) {
                    CurrentInstructor.SetName(NewName);
                    CurrentInstructor.SetEmail(NewEmail);

                    boolean UpdateInstructorOk = InstructorHandler.UpdateInstructor(CurrentInstructor);
                    IsSuccess = IsSuccess && UpdateInstructorOk;
                }

                if (ShouldUpdatePassword) {
                    boolean UpdatePasswordOk = UserAuthenticationHandler.UpdateUserPassword(CurrentUser, PasswordFirst);
                    IsSuccess = IsSuccess && UpdatePasswordOk;
                }

                if (IsSuccess) {
                    JOptionPane.showMessageDialog(InstructorProfilePanel.this,
                            "Profile updated successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    TextFieldNewPassword.setText("");
                    TextFieldConfirmPassword.setText("");
                }
                else {
                    ShowError("Failed to save changes to the database.");
                }

            }
            catch (SQLException ExceptionObject) {
                ExceptionObject.printStackTrace();
                ShowError("Database Error: " + ExceptionObject.getMessage());
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
        protected void paintComponent(Graphics GraphicsContext) {
            Graphics2D Graphics2DContext = (Graphics2D) GraphicsContext.create();
            Graphics2DContext.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Graphics2DContext.setColor(getBackground());
            Graphics2DContext.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CornerRadius, CornerRadius));
            Graphics2DContext.dispose();
            super.paintComponent(GraphicsContext);
        }
    }
}
