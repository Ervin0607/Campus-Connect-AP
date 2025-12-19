package Frontend.components.Tables;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import Backend.domain.StudentGradeRecord;
import Backend.DataBaseHandler.StudentGradeRecordHandler;
import Backend.domain.Offerings;
import Backend.DataBaseHandler.OfferingHandler;

public class ViewStatsDialog extends JDialog {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);
    private static final Color ColorAccent = new Color(0xB0F2B4);

    private int TotalStudents = 0;
    private double ClassAverage = 0.0;
    private String ClassAverageGrade = "N/A";
    private Map<String, Double> ComponentAverages = new HashMap<>();
    private Map<String, Integer> ComponentMaxMarks;

    public ViewStatsDialog(Frame Owner, String CourseName, Offerings Offering) {
        super(Owner, "Class Statistics - " + CourseName, true);
        setSize(900, 700);
        setLocationRelativeTo(Owner);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(ColorBackground);

        CalculateStats(Offering);

        JLabel TitleLabel = new JLabel("Class Performance Dashboard", SwingConstants.CENTER);
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        TitleLabel.setForeground(ColorTextPrimary);
        TitleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));

        JPanel SummaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        SummaryPanel.setOpaque(false);
        SummaryPanel.setBorder(new EmptyBorder(0, 20, 0, 20));

        SummaryPanel.add(CreateStatCard("Registered Students", String.valueOf(TotalStudents)));
        SummaryPanel.add(CreateStatCard("Class Average", String.format("%.2f", ClassAverage)));
        SummaryPanel.add(CreateStatCard("Average Grade", ClassAverageGrade));

        JPanel TopContainer = new JPanel(new BorderLayout(0, 20));
        TopContainer.setOpaque(false);
        TopContainer.add(TitleLabel, BorderLayout.NORTH);
        TopContainer.add(SummaryPanel, BorderLayout.CENTER);
        add(TopContainer, BorderLayout.NORTH);

        BarChartPanel ChartPanel = new BarChartPanel(ComponentAverages, ComponentMaxMarks);
        ChartPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(ChartPanel, BorderLayout.CENTER);

        JButton CloseButton = new JButton("Close");
        CloseButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        CloseButton.addActionListener(e -> dispose());
        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonPanel.setOpaque(false);
        ButtonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        ButtonPanel.add(CloseButton);
        add(ButtonPanel, BorderLayout.SOUTH);
    }

    private void CalculateStats(Offerings Offering) {
        this.ComponentMaxMarks = Offering.GetGradingComponents();

        try {
            List<StudentGradeRecord> Records = StudentGradeRecordHandler.FindByOfferingID(Offering.GetOfferingID());
            this.TotalStudents = Records.size();

            if (TotalStudents == 0) return;

            double TotalClassScore = 0;
            Map<String, Double> ComponentSum = new HashMap<>();

            for (String ComponentKey : ComponentMaxMarks.keySet()) {
                ComponentSum.put(ComponentKey, 0.0);
            }

            for (StudentGradeRecord Record : Records) {
                Map<String, Integer> Grades = Record.getGrade();
                double StudentTotal = 0;

                for (String Key : ComponentMaxMarks.keySet()) {
                    int Score = 0;
                    if (Grades != null && Grades.containsKey(Key)) {
                        Score = Grades.get(Key);
                    }

                    ComponentSum.put(Key, ComponentSum.get(Key) + Score);
                    StudentTotal += Score;
                }
                TotalClassScore += StudentTotal;
            }

            this.ClassAverage = TotalClassScore / TotalStudents;

            for (String Key : ComponentSum.keySet()) {
                ComponentAverages.put(Key, ComponentSum.get(Key) / TotalStudents);
            }

            this.ClassAverageGrade = CalculateGrade(ClassAverage, Offering.GetGradingSlabs());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String CalculateGrade(double Score, HashMap<String, Integer> Slabs) {
        String BestGrade = "F";
        int BestThreshold = -1;

        for (Map.Entry<String, Integer> Entry : Slabs.entrySet()) {
            int Threshold = Entry.getValue();
            if (Score >= Threshold && Threshold > BestThreshold) {
                BestThreshold = Threshold;
                BestGrade = Entry.getKey();
            }
        }
        return BestGrade;
    }

    private JPanel CreateStatCard(String Title, String Value) {
        JPanel Card = new JPanel(new BorderLayout());
        Card.setBackground(ColorPanel);
        Card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel ValueLabel = new JLabel(Value, SwingConstants.CENTER);
        ValueLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        ValueLabel.setForeground(ColorAccent);

        JLabel TitleLabel = new JLabel(Title, SwingConstants.CENTER);
        TitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        TitleLabel.setForeground(ColorTextSecondary);

        Card.add(ValueLabel, BorderLayout.CENTER);
        Card.add(TitleLabel, BorderLayout.SOUTH);
        return Card;
    }

    private static class BarChartPanel extends JPanel {
        private final Map<String, Double> Data;
        private final Map<String, Integer> MaxMarks;

        public BarChartPanel(Map<String, Double> Data, Map<String, Integer> MaxMarks) {
            this.Data = Data;
            this.MaxMarks = MaxMarks;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics GraphicsObject) {
            super.paintComponent(GraphicsObject);
            Graphics2D Graphics2DObject = (Graphics2D) GraphicsObject;
            Graphics2DObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int Width = getWidth();
            int Height = getHeight();
            int Padding = 50;
            int ChartBottom = Height - Padding;
            int ChartLeft = Padding;

            Graphics2DObject.setColor(ColorTextPrimary);
            Graphics2DObject.setFont(new Font("SansSerif", Font.BOLD, 16));
            Graphics2DObject.drawString("Average Component Scores", Width / 2 - 100, 20);

            if (Data.isEmpty()) return;

            int NumberOfBars = Data.size();
            int BarWidth = (Width - 2 * Padding) / (NumberOfBars * 2);
            int Space = BarWidth;

            int CurrentX = ChartLeft + Space / 2;

            double MaxScale = 0;
            for (int Value : MaxMarks.values()) MaxScale = Math.max(MaxScale, Value);
            if (MaxScale == 0) MaxScale = 100;

            for (String Key : Data.keySet()) {
                double Value = Data.get(Key);
                double MaxValue = MaxMarks.getOrDefault(Key, 100);

                int BarHeight = (int) ((Value / MaxScale) * (ChartBottom - 50));

                Graphics2DObject.setColor(ColorAccent);
                Graphics2DObject.fillRoundRect(CurrentX, ChartBottom - BarHeight, BarWidth, BarHeight, 10, 10);

                Graphics2DObject.setColor(ColorTextPrimary);
                Graphics2DObject.setFont(new Font("SansSerif", Font.BOLD, 12));
                String ValueString = String.format("%.1f", Value);
                FontMetrics FontMetricsObject = Graphics2DObject.getFontMetrics();
                Graphics2DObject.drawString(
                        ValueString,
                        CurrentX + (BarWidth - FontMetricsObject.stringWidth(ValueString)) / 2,
                        ChartBottom - BarHeight - 5
                );

                Graphics2DObject.setColor(ColorTextSecondary);
                Graphics2DObject.setFont(new Font("SansSerif", Font.PLAIN, 12));
                String Label = Key;
                if (Label.length() > 10) Label = Label.substring(0, 8) + "..";

                FontMetricsObject = Graphics2DObject.getFontMetrics();
                Graphics2DObject.drawString(Label, CurrentX + (BarWidth - FontMetricsObject.stringWidth(Label)) / 2, ChartBottom + 20);

                int MaxBarHeight = (int) ((MaxValue / MaxScale) * (ChartBottom - 50));
                Graphics2DObject.setColor(new Color(255, 255, 255, 30));
                Graphics2DObject.drawRoundRect(CurrentX, ChartBottom - MaxBarHeight, BarWidth, MaxBarHeight, 10, 10);

                CurrentX += BarWidth + Space;
            }

            Graphics2DObject.setColor(ColorTextSecondary);
            Graphics2DObject.drawLine(ChartLeft, ChartBottom, Width - Padding, ChartBottom);
        }
    }
}
