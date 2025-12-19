package Frontend.StudentPages;

import Backend.DataBaseHandler.CourseHandler;
import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.RegistrationHandler;
import Backend.domain.Course;
import Backend.domain.Offerings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class TimeTable extends JPanel {
    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorText = new Color(0xFFFFFF);
    private static final Color ColorTextDim = new Color(0xB0B0B0);
    private static final Color ColorGridLine = new Color(0x2A2A2A);

    private static final Color[] CourseColors = {new Color(0xFF6B6B), new Color(0x4ECDC4), new Color(0xFFE66D), new Color(0xA29BFE), new Color(0xFF9F1C), new Color(0x2ECC71), new Color(0xE84393), new Color(0x00CEC9), new Color(0xF5B7B1), new Color(0x7FB3D5)
    };

    private static final int StartHour = 8;
    private static final int EndHour = 20;
    private static final int SlotMinutes = 30;
    private static final int SlotsPerDay = (EndHour - StartHour) * 60 / SlotMinutes;

    private final int StudentRollNumber;

    private JComboBox<String> SemesterBox;
    private JComboBox<String> YearBox;
    private JPanel TimetableContainer;
    private JLabel StatusLabel;
    private JLabel[][] CellLabels;
    private SwingWorker<?, ?> ReloadWorker;

    public TimeTable(int studentRollNumber) {
        super(new BorderLayout(10, 10));
        this.StudentRollNumber = studentRollNumber;
        setOpaque(true);
        setBackground(ColorBackground);
        setBorder(new EmptyBorder(10, 15, 15, 15));
        add(BuildHeader(), BorderLayout.NORTH);
        add(BuildCenter(), BorderLayout.CENTER);
        ReloadTimetableAsync();
    }


    private JComponent BuildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("My Timetable");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(ColorText);
        title.setBorder(new EmptyBorder(0, 5, 6, 0));
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        SemesterBox = new JComboBox<>(new String[]{"All Semesters", "SPRING", "SUMMER", "FALL", "WINTER"});
        StyleCombo(SemesterBox);

        YearBox = new JComboBox<>();
        StyleCombo(YearBox);

        int currentYear = LocalDate.now().getYear();
        YearBox.addItem("All Years");
        for (int y = currentYear; y >= currentYear - 4; y--) YearBox.addItem(String.valueOf(y));

        SemesterBox.setSelectedItem(DetectCurrentSemester());
        YearBox.setSelectedItem(String.valueOf(currentYear));

        SemesterBox.addActionListener(e -> ReloadTimetableAsync());
        YearBox.addActionListener(e -> ReloadTimetableAsync());

        right.add(LabelChip("Semester"));
        right.add(SemesterBox);
        right.add(LabelChip("Year"));
        right.add(YearBox);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JComponent BuildCenter() {
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);

        TimetableContainer = new JPanel(new BorderLayout(6, 6));
        TimetableContainer.setOpaque(false);

        StatusLabel = new JLabel("Loading timetable...");
        StatusLabel.setForeground(ColorTextDim);
        StatusLabel.setBorder(new EmptyBorder(8, 8, 8, 8));

        center.add(TimetableContainer, BorderLayout.CENTER);
        center.add(StatusLabel, BorderLayout.SOUTH);
        return center;
    }

    private JLabel LabelChip(String text) {
        JLabel label = new JLabel(" " + text + " ");
        label.setForeground(ColorTextDim);
        return label;
    }

    private void StyleCombo(JComboBox<?> combo) {
        combo.setBackground(ColorPanel);
        combo.setForeground(ColorText);
        combo.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    }

    private String DetectCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        if (month <= 4) return "SPRING";
        if (month <= 6) return "SUMMER";
        if (month <= 10) return "FALL";
        return "WINTER";
    }

    private String GetSelectedSemester() {
        return String.valueOf(SemesterBox.getSelectedItem());
    }

    private Integer GetSelectedYearNullable() {
        String v = String.valueOf(YearBox.getSelectedItem());
        if ("All Years".equalsIgnoreCase(v)) return null;
        try {
            return Integer.parseInt(v);
        }
        catch (Exception e) {
            return null;
        }
    }

    public void ReloadTimetableAsync() {
        if (ReloadWorker != null && !ReloadWorker.isDone()) ReloadWorker.cancel(true);

        String sem = GetSelectedSemester();
        Integer yr = GetSelectedYearNullable();

        if ("All Semesters".equalsIgnoreCase(sem) || yr == null) {
            RenderMessage("Select a specific semester and year to view your timetable.");
            return;
        }

        SetControlsEnabled(false);
        StatusLabel.setText("Loading timetable for " + sem + " " + yr + "...");

        ReloadWorker = new SwingWorker<TimetableData, Void>() {
            @Override
            protected TimetableData doInBackground() {
                TimetableData data = new TimetableData();
                try {
                    Set<Integer> ids = RegistrationHandler.GetRegisteredOfferingIds(StudentRollNumber, sem, yr);
                    if (ids.isEmpty()) return data;
                    List<Integer> offeringIds = new ArrayList<>(ids);
                    data.offerings = OfferingHandler.FindOfferingsByIDs(offeringIds);
                    for (Offerings off : data.offerings) {
                        Course c = CourseHandler.FindCourseByID(off.GetCourseID());
                        if (c != null) data.courseMap.put(off.GetCourseID(), c);
                    }
                }
                catch (SQLException e) {
                    data.error = e.getMessage();
                }
                return data;
            }

            @Override
            protected void done() {
                SetControlsEnabled(true);
                try {
                    TimetableData data = get();
                    if (data.error != null) RenderMessage("Error loading timetable: " + data.error);
                    else if (data.offerings.isEmpty()) RenderMessage("You have no registrations for this term.");
                    else RenderTimetable(data.offerings, data.courseMap);
                }
                catch (Exception e) {
                    RenderMessage("Failed to load timetable: " + e.getMessage());
                }
            }
        };
        ReloadWorker.execute();
    }

    private void SetControlsEnabled(boolean enabled) {
        SemesterBox.setEnabled(enabled);
        YearBox.setEnabled(enabled);
    }

    private void RenderMessage(String msg) {
        TimetableContainer.removeAll();
        TimetableContainer.revalidate();
        TimetableContainer.repaint();
        StatusLabel.setText(msg);
    }

    private void RenderTimetable(List<Offerings> offerings, Map<Integer, Course> courseMap) {
        TimetableContainer.removeAll();
        JPanel grid = BuildBaseGrid();
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        legend.setOpaque(false);

        Map<Integer, Color> courseColorMap = new HashMap<>();
        final int[] colorIndex = {0};

        for (Offerings off : offerings) {
            Course course = courseMap.get(off.GetCourseID());
            if (course == null) continue;

            Color color = courseColorMap.computeIfAbsent(off.GetCourseID(), id -> {
                Color base = CourseColors[colorIndex[0] % CourseColors.length];
                colorIndex[0]++;
                return base;
            });

            String code = Safe(course.GetCode());
            String title = Safe(course.GetTitle());
            String fullLabel = code.isEmpty() ? title : (code + " - " + title);

            String lecJson = NormalizeJson(off.GetLectureSchedule());
            String labJson = NormalizeJson(off.GetLabSchedule());

            List<ScheduleEntry> entries = new ArrayList<>();
            entries.addAll(ParseScheduleJson(lecJson, false));
            entries.addAll(ParseScheduleJson(labJson, true));

            for (ScheduleEntry se : entries) {
                int dayIdx = DayToIndex(se.day);
                if (dayIdx < 0 || dayIdx > 4) continue;
                int startIdx = MinutesToSlotIndex(se.startMinutes);
                int endIdx = MinutesToSlotIndex(se.endMinutes);
                if (endIdx <= startIdx) continue;

                String typeTag = se.isLab ? "[Lab]" : "[Lec]";
                String tooltip = fullLabel + " " + typeTag + " (" + se.startStr + "–" + se.endStr + ")";
                AddBlockToGrid(dayIdx, startIdx, endIdx, color, code.isEmpty() ? title : code, tooltip);
            }
        }

        for (Map.Entry<Integer, Color> e : courseColorMap.entrySet()) {
            Course course = courseMap.get(e.getKey());
            if (course == null) continue;
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            chip.setOpaque(false);
            JLabel colorBox = new JLabel("  ");
            colorBox.setOpaque(true);
            colorBox.setBackground(e.getValue());
            colorBox.setPreferredSize(new Dimension(16, 16));
            JLabel text = new JLabel(Safe(course.GetCode()) + (Safe(course.GetTitle()).isEmpty() ? "" : " - " + Safe(course.GetTitle())));
            text.setForeground(ColorText);
            chip.add(colorBox);
            chip.add(text);
            legend.add(chip);
        }

        TimetableContainer.add(grid, BorderLayout.CENTER);
        TimetableContainer.add(legend, BorderLayout.SOUTH);
        TimetableContainer.revalidate();
        TimetableContainer.repaint();
        StatusLabel.setText("Double-check overlapping colors if you see clashes.");
    }

    private JPanel BuildBaseGrid() {
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(SlotsPerDay + 1, 6));
        grid.setOpaque(false);

        CellLabels = new JLabel[SlotsPerDay][5];

        grid.add(MakeHeaderCell(""));
        grid.add(MakeHeaderCell("Monday"));
        grid.add(MakeHeaderCell("Tuesday"));
        grid.add(MakeHeaderCell("Wednesday"));
        grid.add(MakeHeaderCell("Thursday"));
        grid.add(MakeHeaderCell("Friday"));

        for (int row = 0; row < SlotsPerDay; row++) {
            int totalMinutes = StartHour * 60 + row * SlotMinutes;
            grid.add(MakeTimeCell(FormatTime(totalMinutes)));
            for (int day = 0; day < 5; day++) {
                JLabel cell = new JLabel("", SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBackground(ColorPanel);
                cell.setForeground(ColorText);
                cell.setFont(cell.getFont().deriveFont(11f));
                cell.setBorder(BorderFactory.createMatteBorder(1, day == 0 ? 1 : 0, 0, 1, ColorGridLine));
                CellLabels[row][day] = cell;
                grid.add(cell);
            }
        }
        gridWrapper.add(grid, BorderLayout.CENTER);
        return gridWrapper;
    }

    private JLabel MakeHeaderCell(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(ColorBackground);
        l.setForeground(ColorText);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, ColorGridLine));
        return l;
    }

    private JLabel MakeTimeCell(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setOpaque(true);
        l.setBackground(ColorBackground);
        l.setForeground(ColorTextDim);
        l.setFont(l.getFont().deriveFont(11f));
        l.setBorder(new EmptyBorder(0, 0, 0, 6));
        return l;
    }

    private void AddBlockToGrid(int dayIdx, int startIdx, int endIdx, Color color, String label, String tooltip) {
        if (CellLabels == null) return;
        startIdx = Math.max(0, startIdx);
        endIdx = Math.min(SlotsPerDay, endIdx);
        for (int r = startIdx; r < endIdx; r++) {
            JLabel cell = CellLabels[r][dayIdx];
            cell.setBackground(color);
            cell.setOpaque(true);
            cell.setForeground(Color.BLACK);
            if (r == startIdx) cell.setText(label);
            String exist = cell.getToolTipText();
            cell.setToolTipText(exist == null ? tooltip : exist + " | " + tooltip);
        }
    }

    private static class ScheduleEntry {
        String day;
        int startMinutes;
        int endMinutes;
        String startStr;
        String endStr;
        boolean isLab;
    }

    private String NormalizeJson(String json) {
        if (json == null) return null;
        String s = json.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        return s;
    }

    private List<ScheduleEntry> ParseScheduleJson(String json, boolean isLab) {
        List<ScheduleEntry> out = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return out;

        String s = json.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
        if (s.isEmpty()) return out;

        List<String> objects = new ArrayList<>();
        int brace = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') brace++;
            if (c == '}') brace--;
            current.append(c);
            if (brace == 0 && c == '}') {
                objects.add(current.toString());
                current.setLength(0);
            }
        }

        for (String obj : objects) {
            String day = ExtractJsonString(obj, "day");
            String start = ExtractJsonString(obj, "start");
            String end = ExtractJsonString(obj, "end");
            if (day == null || start == null || end == null) continue;

            ScheduleEntry se = new ScheduleEntry();
            se.day = day;
            se.startStr = start;
            se.endStr = end;
            se.startMinutes = ParseTimeMinutes(start);
            se.endMinutes = ParseTimeMinutes(end);
            se.isLab = isLab;
            out.add(se);
        }
        return out;
    }

    private String ExtractJsonString(String obj, String key) {
        String pattern = "\"" + key + "\"";
        int idx = obj.indexOf(pattern);
        if (idx < 0) return null;
        idx = obj.indexOf(':', idx);
        if (idx < 0) return null;
        idx++;
        while (idx < obj.length() && Character.isWhitespace(obj.charAt(idx))) idx++;
        if (idx >= obj.length() || obj.charAt(idx) != '"') return null;
        idx++;
        int end = obj.indexOf('"', idx);
        if (end < 0) return null;
        return obj.substring(idx, end);
    }

    private int ParseTimeMinutes(String t) {
        try {
            String[] parts = t.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            return h * 60 + m;
        }
        catch (Exception e) {
            return StartHour * 60;
        }
    }

    private int DayToIndex(String day) {
        if (day == null) return -1;
        String d = day.trim().toLowerCase(Locale.ROOT);
        if (d.startsWith("mon")) return 0;
        if (d.startsWith("tue")) return 1;
        if (d.startsWith("wed")) return 2;
        if (d.startsWith("thu")) return 3;
        if (d.startsWith("fri")) return 4;
        return -1;
    }

    private int MinutesToSlotIndex(int minutes) {
        int base = StartHour * 60;
        return (minutes - base) / SlotMinutes;
    }

    private String FormatTime(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format("%02d:%02d", h, m);
    }

    private static String Safe(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private static class TimetableData {
        List<Offerings> offerings = new ArrayList<>();
        Map<Integer, Course> courseMap = new HashMap<>();
        String error;
    }
}