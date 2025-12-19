package Frontend.StudentPages;

import Backend.DataBaseHandler.StudentHandler;
import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.Student;
import Backend.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class StudentProfilePanel extends JPanel {

    private static final Color COLOR_BACKGROUND     = new Color(0x121212);
    private static final Color COLOR_PANEL          = new Color(0x1E1E1E);
    private static final Color COLOR_TEXT_PRIMARY   = new Color(0xFFFFFF);
    private static final Color COLOR_TEXT_SECONDARY = new Color(0xB0B0B0);
    private static final Color COLOR_ACCENT         = new Color(0x3F51B5);

    private final int StudentRollNumber;

    private Student CurrentStudent;
    private User CurrentUser;

    private JLabel RollValueLabel;
    private JLabel NameValueLabel;
    private JLabel ProgramValueLabel;
    private JLabel YearValueLabel;

    private JTextField UserNameField;
    private JPasswordField NewPasswordField;
    private JPasswordField ConfirmPasswordField;
    private JButton SaveChangesButton;

    public StudentProfilePanel(int StudentRollNumber) {
        this.StudentRollNumber = StudentRollNumber;

        setOpaque(true);
        setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(COLOR_PANEL);
        CardPanel.setLayout(new BorderLayout(20, 20));
        CardPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel HeaderLabel = new JLabel("Student Profile");
        HeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        HeaderLabel.setForeground(COLOR_TEXT_PRIMARY);
        CardPanel.add(HeaderLabel, BorderLayout.NORTH);

        JPanel CenterPanel = new JPanel();
        CenterPanel.setOpaque(false);
        CenterPanel.setLayout(new BoxLayout(CenterPanel, BoxLayout.Y_AXIS));

        CenterPanel.add(CreateStudentInfoPanel());
        CenterPanel.add(Box.createVerticalStrut(20));
        CenterPanel.add(CreateAccountPanel());

        CardPanel.add(CenterPanel, BorderLayout.CENTER);

        add(CardPanel, BorderLayout.CENTER);

        LoadDataFromDatabase();
    }

    private void LoadDataFromDatabase() {
        try {
            CurrentStudent = StudentHandler.FindStudentByRollNumber(StudentRollNumber);
            if (CurrentStudent == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Student not found for Roll Number: " + StudentRollNumber,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            int UserId = CurrentStudent.GetUserID();
            CurrentUser = UserAuthenticationHandler.FindUserID(UserId);
            if (CurrentUser == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "User account not found for UserID: " + UserId,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            PopulateStudentInfo();
            PopulateAccountInfo();

        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load profile data: " + ExceptionObject.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void PopulateStudentInfo() {
        if (CurrentStudent == null) {
            return;
        }

        RollValueLabel.setText(String.valueOf(CurrentStudent.GetRollNumber()));
        NameValueLabel.setText(CurrentStudent.GetName());
        ProgramValueLabel.setText(CurrentStudent.GetProgram());
        YearValueLabel.setText("Year " + CurrentStudent.GetYear());
    }

    private void PopulateAccountInfo() {
        if (CurrentUser == null) {
            return;
        }

        UserNameField.setText(CurrentUser.GetUserName());
        NewPasswordField.setText("");
        ConfirmPasswordField.setText("");
    }

    private JPanel CreateStudentInfoPanel() {
        JPanel ContentPanel = new JPanel();
        ContentPanel.setOpaque(false);
        ContentPanel.setLayout(new GridBagLayout());

        GridBagConstraints GridConstraints = new GridBagConstraints();
        GridConstraints.insets = new Insets(8, 8, 8, 8);
        GridConstraints.anchor = GridBagConstraints.WEST;
        GridConstraints.gridx = 0;
        GridConstraints.gridy = 0;

        JLabel RollLabel = CreateSecondaryLabel("Roll Number:");
        ContentPanel.add(RollLabel, GridConstraints);

        GridConstraints.gridx = 1;
        RollValueLabel = CreatePrimaryLabel("-");
        ContentPanel.add(RollValueLabel, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        JLabel NameLabel = CreateSecondaryLabel("Name:");
        ContentPanel.add(NameLabel, GridConstraints);

        GridConstraints.gridx = 1;
        NameValueLabel = CreatePrimaryLabel("-");
        ContentPanel.add(NameValueLabel, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        JLabel ProgramLabel = CreateSecondaryLabel("Program:");
        ContentPanel.add(ProgramLabel, GridConstraints);

        GridConstraints.gridx = 1;
        ProgramValueLabel = CreatePrimaryLabel("-");
        ContentPanel.add(ProgramValueLabel, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        JLabel YearLabel = CreateSecondaryLabel("Current Year:");
        ContentPanel.add(YearLabel, GridConstraints);

        GridConstraints.gridx = 1;
        YearValueLabel = CreatePrimaryLabel("-");
        ContentPanel.add(YearValueLabel, GridConstraints);

        return ContentPanel;
    }

    private JPanel CreateAccountPanel() {
        JPanel AccountPanel = new JPanel();
        AccountPanel.setOpaque(false);
        AccountPanel.setLayout(new GridBagLayout());
        AccountPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0x333333)), "Account Settings", 0, 0, new Font("SansSerif", Font.BOLD, 14), COLOR_TEXT_SECONDARY));

        GridBagConstraints GridConstraints = new GridBagConstraints();
        GridConstraints.insets = new Insets(8, 8, 8, 8);
        GridConstraints.anchor = GridBagConstraints.WEST;
        GridConstraints.fill = GridBagConstraints.HORIZONTAL;
        GridConstraints.weightx = 1.0;
        GridConstraints.gridx = 0;
        GridConstraints.gridy = 0;

        JLabel UserNameLabel = CreateSecondaryLabel("Username:");
        AccountPanel.add(UserNameLabel, GridConstraints);

        GridConstraints.gridx = 1;
        UserNameField = new JTextField(20);
        StyleTextField(UserNameField);
        AccountPanel.add(UserNameField, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        JLabel NewPasswordLabel = CreateSecondaryLabel("New Password:");
        AccountPanel.add(NewPasswordLabel, GridConstraints);

        GridConstraints.gridx = 1;
        NewPasswordField = new JPasswordField(20);
        StyleTextField(NewPasswordField);
        AccountPanel.add(NewPasswordField, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        JLabel ConfirmPasswordLabel = CreateSecondaryLabel("Confirm Password:");
        AccountPanel.add(ConfirmPasswordLabel, GridConstraints);

        GridConstraints.gridx = 1;
        ConfirmPasswordField = new JPasswordField(20);
        StyleTextField(ConfirmPasswordField);
        AccountPanel.add(ConfirmPasswordField, GridConstraints);

        GridConstraints.gridx = 0;
        GridConstraints.gridy++;
        GridConstraints.gridwidth = 2;
        GridConstraints.anchor = GridBagConstraints.CENTER;
        SaveChangesButton = new JButton("Save Changes");
        StyleButton(SaveChangesButton);
        SaveChangesButton.addActionListener(new SaveChangesListener());
        AccountPanel.add(SaveChangesButton, GridConstraints);

        return AccountPanel;
    }

    private JLabel CreatePrimaryLabel(String Text) {
        JLabel LabelObject = new JLabel(Text);
        LabelObject.setForeground(COLOR_TEXT_PRIMARY);
        LabelObject.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return LabelObject;
    }

    private JLabel CreateSecondaryLabel(String Text) {
        JLabel LabelObject = new JLabel(Text);
        LabelObject.setForeground(COLOR_TEXT_SECONDARY);
        LabelObject.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return LabelObject;
    }

    private void StyleTextField(JTextField Field) {
        Field.setBackground(new Color(0x2A2A2A));
        Field.setForeground(COLOR_TEXT_PRIMARY);
        Field.setCaretColor(COLOR_TEXT_PRIMARY);
        Field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x444444)),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    private void StyleButton(JButton ButtonObject) {
        ButtonObject.setBackground(COLOR_ACCENT);
        ButtonObject.setForeground(Color.WHITE);
        ButtonObject.setFocusPainted(false);
        ButtonObject.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        ButtonObject.setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    private class SaveChangesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent EventObject) {
            if (CurrentUser == null) {
                JOptionPane.showMessageDialog(
                        StudentProfilePanel.this,
                        "No user loaded.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String NewUserName = UserNameField.getText().trim();
            String CurrentUserName = CurrentUser.GetUserName();

            String NewPassword = new String(NewPasswordField.getPassword());
            String ConfirmPassword = new String(ConfirmPasswordField.getPassword());

            boolean ShouldUpdateUserName = false;
            boolean ShouldUpdatePassword = false;

            if (!NewUserName.equals(CurrentUserName)) {
                if (NewUserName.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            StudentProfilePanel.this,
                            "Username cannot be empty.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                ShouldUpdateUserName = true;
            }

            if (!NewPassword.isEmpty() || !ConfirmPassword.isEmpty()) {
                if (!NewPassword.equals(ConfirmPassword)) {
                    JOptionPane.showMessageDialog(
                            StudentProfilePanel.this,
                            "Passwords do not match.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                ShouldUpdatePassword = true;
            }

            if (!ShouldUpdateUserName && !ShouldUpdatePassword) {
                JOptionPane.showMessageDialog(
                        StudentProfilePanel.this,
                        "No changes to save.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            try {
                boolean UpdateSuccess = true;

                if (ShouldUpdateUserName) {
                    boolean UserNameUpdated = UserAuthenticationHandler.UpdateUser(CurrentUser, NewUserName);
                    UpdateSuccess = UpdateSuccess && UserNameUpdated;
                    if (UserNameUpdated) {
                        CurrentUser.SetUserName(NewUserName);
                    }
                }

                if (ShouldUpdatePassword) {
                    boolean PasswordUpdated = UserAuthenticationHandler.UpdateUserPassword(CurrentUser, NewPassword);
                    UpdateSuccess = UpdateSuccess && PasswordUpdated;
                }

                if (UpdateSuccess) {
                    JOptionPane.showMessageDialog(
                            StudentProfilePanel.this,
                            "Changes saved successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    NewPasswordField.setText("");
                    ConfirmPasswordField.setText("");
                }
                else {
                    JOptionPane.showMessageDialog(
                            StudentProfilePanel.this,
                            "Failed to save some/all changes.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            }
            catch (SQLException ExceptionObject) {
                ExceptionObject.printStackTrace();
                JOptionPane.showMessageDialog(
                        StudentProfilePanel.this,
                        "Database error while saving changes: " + ExceptionObject.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
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
        protected void paintComponent(Graphics GraphicsObject) {
            Graphics2D Graphics2DObject = (Graphics2D) GraphicsObject.create();
            Graphics2DObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Graphics2DObject.setColor(getBackground());
            Graphics2DObject.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CornerRadius, CornerRadius));
            Graphics2DObject.dispose();
            super.paintComponent(GraphicsObject);
        }
    }
}
