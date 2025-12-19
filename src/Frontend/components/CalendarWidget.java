package Frontend.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarWidget extends JPanel {

    public static class Theme {
        public Color Background = new Color(0x121212);
        public Color Panel = new Color(0x1E1E1E);
        public Color TextPrimary = new Color(0xFFFFFF);
        public Color TextSecondary = new Color(0xB0B0B0);
        public Color Accent = new Color(0xB0F2B4);
        public Color CellBackground = new Color(0x2A2A2A);
        public Color CellToday = new Color(0x3C6E71);
        public Color OtherMonth = new Color(0x777777);
        public Color Divider = new Color(0x2A2A2A);
    }

    private Theme ThemeData = new Theme();
    private Locale LocaleData = Locale.getDefault();
    private YearMonth Current;

    private final JLabel MonthLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton PrevBtn = new JButton("◀");
    private final JButton NextBtn = new JButton("▶");

    private final JPanel Header = new JPanel(new BorderLayout());
    private final JPanel DaysHeader = new JPanel(new GridLayout(1, 7));
    private final JPanel GridPanel = new JPanel(new GridLayout(6, 7, 6, 6));

    private final List<DayCell> DayCells = new ArrayList<>(42);
    private final LocalDate Today = LocalDate.now();

    public CalendarWidget() {
        this(YearMonth.now(), null);
    }

    public CalendarWidget(YearMonth StartMonth, Theme ThemeData) {
        if (ThemeData != null) this.ThemeData = ThemeData;
        this.Current = StartMonth == null ? YearMonth.now() : StartMonth;
        InitUI();
        RebuildGrid();
    }

    public void SetYearMonth(YearMonth YM) {
        if (YM == null) return;
        this.Current = YM;
        RebuildGrid();
    }

    public YearMonth GetYearMonth() { return Current; }

    public void SetLocale(Locale Loc) {
        this.LocaleData = (Loc == null) ? Locale.getDefault() : Loc;
        RebuildGrid();
    }

    public void SetTheme(Theme T) {
        if (T == null) return;
        this.ThemeData = T;
        ApplyTheme();
        RebuildGrid();
    }

    private void InitUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setOpaque(true);

        JPanel Nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        StyleButton(PrevBtn);
        StyleButton(NextBtn);
        PrevBtn.addActionListener(this::PrevMonth);
        NextBtn.addActionListener(this::NextMonth);
        Nav.add(PrevBtn); Nav.add(NextBtn);

        MonthLabel.setFont(MonthLabel.getFont().deriveFont(Font.BOLD, 16f));
        MonthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        MonthLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        MonthLabel.setPreferredSize(new Dimension(0, 28));

        Header.add(Nav, BorderLayout.WEST);
        Header.add(MonthLabel, BorderLayout.CENTER);
        Header.setOpaque(true);
        Header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeData.Divider));
        add(Header, BorderLayout.NORTH);

        DaysHeader.setOpaque(true);
        DaysHeader.setBackground(ThemeData.Panel);
        for (DayOfWeek Dow : DayOfWeek.values()) {
            String Txt = Dow.getDisplayName(TextStyle.SHORT_STANDALONE, LocaleData).substring(0, 1).toUpperCase(LocaleData);
            JLabel Lbl = new JLabel(Txt, SwingConstants.CENTER);
            Lbl.setBorder(new EmptyBorder(4, 0, 6, 0));
            Lbl.setForeground(ThemeData.TextSecondary);
            DaysHeader.add(Lbl);
        }

        GridPanel.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel CenterWrap = new JPanel(new BorderLayout());
        CenterWrap.setOpaque(false);
        CenterWrap.add(DaysHeader, BorderLayout.NORTH);
        CenterWrap.add(GridPanel, BorderLayout.CENTER);
        add(CenterWrap, BorderLayout.CENTER);

        for (int i = 0; i < 42; i++) {
            DayCell Cell = new DayCell();
            DayCells.add(Cell);
            GridPanel.add(Cell);
        }

        ApplyTheme();
    }

    private void ApplyTheme() {
        setBackground(ThemeData.Panel);
        Header.setBackground(ThemeData.Panel);
        DaysHeader.setBackground(ThemeData.Panel);
        MonthLabel.setForeground(ThemeData.TextPrimary);
    }

    private void StyleButton(JButton B) {
        B.setFocusable(false);
        B.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        B.setContentAreaFilled(true);
        B.setOpaque(true);
    }

    private void PrevMonth(ActionEvent e) { SetYearMonth(Current.minusMonths(1)); }
    private void NextMonth(ActionEvent e) { SetYearMonth(Current.plusMonths(1)); }

    private void RebuildGrid() {
        String Mon = Current.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, LocaleData).toUpperCase(LocaleData);
        MonthLabel.setText(Mon + " " + Current.getYear());

        DayOfWeek FirstDayOfWeek = FirstDayOfWeek(LocaleData);
        LocalDate FirstOfMonth = Current.atDay(1);
        int Shift = ((FirstOfMonth.getDayOfWeek().getValue() - FirstDayOfWeek.getValue()) + 7) % 7;
        LocalDate CellDate = FirstOfMonth.minusDays(Shift);

        for (int i = 0; i < 42; i++) {
            DayCell Cell = DayCells.get(i);
            boolean InMonth = CellDate.getMonth().equals(Current.getMonth());
            Cell.SetDate(CellDate, InMonth, CellDate.equals(Today));
            CellDate = CellDate.plusDays(1);
        }

        revalidate();
        repaint();
    }

    private static DayOfWeek FirstDayOfWeek(Locale Loc) {
        String C = (Loc == null ? Locale.getDefault() : Loc).getCountry();
        if ("US".equalsIgnoreCase(C) || "IN".equalsIgnoreCase(C)) return DayOfWeek.SUNDAY;
        return DayOfWeek.MONDAY;
    }

    private class DayCell extends JButton {
        private LocalDate Date;
        private boolean InCurrentMonth;
        private boolean IsToday;

        DayCell() {
            super();
            setMargin(new Insets(6, 6, 6, 6));
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8, 6, 6, 6));
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.TOP);
            setLayout(new BorderLayout());
        }

        void SetDate(LocalDate D, boolean InMonth, boolean TodayFlag) {
            this.Date = D;
            this.InCurrentMonth = InMonth;
            this.IsToday = TodayFlag;
            setText(Integer.toString(D.getDayOfMonth()));
            RefreshAppearance();
        }

        void RefreshAppearance() {
            Color Fg = InCurrentMonth ? ThemeData.TextPrimary : ThemeData.OtherMonth;
            Color Bg = ThemeData.CellBackground;
            if (IsToday && InCurrentMonth) Bg = ThemeData.CellToday;
            setForeground(Fg);
            setBackground(Bg);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(44, 42);
        }
    }
}
