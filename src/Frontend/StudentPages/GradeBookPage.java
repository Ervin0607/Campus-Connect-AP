package Frontend.StudentPages;

import Backend.DataBaseHandler.CourseHandler;
import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.RegistrationHandler;
import Backend.DataBaseHandler.StudentGradeRecordHandler;
import Backend.domain.Course;
import Backend.domain.Instructor;
import Backend.domain.Offerings;
import Backend.domain.Registration;
import Backend.domain.StudentGradeRecord;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GradeBookPage extends JPanel {
    private static final Color COLOR_BACKGROUND = new Color(0x121212);
    private static final Color COLOR_PANEL = new Color(0x1E1E1E);
    private static final Color COLOR_ACCENT = new Color(0xB0F2B4);
    private static final Color COLOR_TEXT_PRIMARY = new Color(0xFFFFFF);
    private static final Color COLOR_TEXT_SECONDARY = new Color(0xB0B0B0);
    private static final Color COLOR_GRID = new Color(0x303030);

    private static final Map<String, Double> GRADE_POINTS = Map.ofEntries(Map.entry("A+", 10.0), Map.entry("A", 10.0), Map.entry("A-", 9.0), Map.entry("B+", 9.0), Map.entry("B", 8.0), Map.entry("B-", 7.0), Map.entry("C+", 7.0), Map.entry("C", 6.0), Map.entry("C-", 5.0), Map.entry("D+", 5.0), Map.entry("D", 4.0), Map.entry("D-", 3.0), Map.entry("F", 0.0), Map.entry("I", 0.0));

    private final int studentRollNumber;

    private JComboBox<String> semesterBox;
    private JComboBox<Integer> yearBox;
    private JTable table;
    private GradeTableModel tableModel;
    private JLabel sgpaLabel;
    private GpaChartPanel chartPanel;
    private CourseBarChartPanel barChartPanel;

    private List<Registration> allRegistrations = new ArrayList<>();

    public GradeBookPage(int studentRollNumber) {
        super(new BorderLayout(10, 10));
        this.studentRollNumber = studentRollNumber;
        setOpaque(false);
        setBorder(new EmptyBorder(12, 16, 16, 16));

        add(CreateHeaderWithChart(), BorderLayout.NORTH);
        add(CreateTableSection(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        ReloadGradeBook();
        InitYearSemesterChoices();
    }

    public void ReloadGradeBook() {
        LoadRegistrations();
        GradeSource.preload(studentRollNumber);
        if (chartPanel != null) {
            chartPanel.reload(allRegistrations);
        }
        RefreshTable();
    }

    private JComponent CreateHeaderWithChart() {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Grade Book");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(COLOR_TEXT_PRIMARY);
        left.add(title);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        semesterBox = new JComboBox<>(new String[]{"SPRING", "SUMMER", "FALL", "WINTER"});
        StyleCombo(semesterBox);
        right.add(new JLabel(StyledLabel("Semester:")));
        right.add(semesterBox);

        yearBox = new JComboBox<>();
        StyleCombo(yearBox);
        right.add(new JLabel(StyledLabel("Year:")));
        right.add(yearBox);

        JButton reloadBtn = new JButton(new FlatSVGIcon("Frontend/assets/refresh.svg", 18, 18));
        StylizeButton(reloadBtn);
        reloadBtn.addActionListener(e -> ReloadGradeBook());
        right.add(reloadBtn);

        JButton printBtn = new JButton("Print");
        StylizeButton(printBtn);
        printBtn.addActionListener(e -> {
            try {
                String head = "Grade Book - " + semesterBox.getSelectedItem() + " " + yearBox.getSelectedItem();
                table.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat(head), null);
            }
            catch (java.awt.print.PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
            }
        });
        right.add(printBtn);

        JButton downloadBtn = new JButton("Download Transcript");
        StylizeButton(downloadBtn);
        downloadBtn.addActionListener(e -> TranscriptExporters.ExportCSV(this, studentRollNumber));
        right.add(downloadBtn);

        semesterBox.addActionListener(e -> RefreshTable());
        yearBox.addActionListener(e -> RefreshTable());

        header.add(right, BorderLayout.EAST);
        container.add(header);
        container.add(Box.createVerticalStrut(8));

        JPanel chartsContainer = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsContainer.setOpaque(false);
        chartsContainer.setPreferredSize(new Dimension(1000, 180));

        barChartPanel = new CourseBarChartPanel();
        chartsContainer.add(barChartPanel);

        chartPanel = new GpaChartPanel();
        chartsContainer.add(chartPanel);

        container.add(chartsContainer);

        return container;
    }

    private JComponent CreateTableSection(){
        tableModel = new GradeTableModel();
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 6));
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setBackground(COLOR_BACKGROUND);
        table.setSelectionBackground(COLOR_PANEL);
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);

        JTableHeaderUI.install(table, COLOR_PANEL, COLOR_TEXT_SECONDARY);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);

        table.getColumnModel().getColumn(4).setCellRenderer(new GradeCellRenderer());

        table.getColumnModel().getColumn(6).setCellRenderer(new DetailButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new DetailButtonEditor(new JCheckBox()));

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(COLOR_BACKGROUND);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        wrapper.setBorder(new EmptyBorder(8,0,0,0));
        return wrapper;
    }

    private JComponent createFooter(){
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        sgpaLabel = new JLabel("");
        sgpaLabel.setForeground(COLOR_TEXT_PRIMARY);
        sgpaLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(sgpaLabel);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void LoadRegistrations(){
        try {
            allRegistrations = RegistrationHandler.FindByStudentRollNumber(studentRollNumber);
        } catch (SQLException e){
            allRegistrations = new ArrayList<>();
        }
    }

    private void InitYearSemesterChoices(){
        List<Integer> years = new ArrayList<>();
        for (Registration r : allRegistrations){
            try {
                Offerings o = OfferingHandler.FindOffering(r.GetOfferingID());
                if (o != null) years.add(o.GetYear());
            } catch (SQLException ignored) {}
        }
        years = years.stream().distinct().sorted().collect(Collectors.toList());
        if (years.isEmpty()) years.add(java.time.LocalDate.now().getYear());
        yearBox.setModel(new DefaultComboBoxModel<>(years.toArray(new Integer[0])));
        yearBox.setSelectedItem(DetectCurrentYear());
        semesterBox.setSelectedItem(DetectCurrentSemester());
    }

    private void RefreshTable(){
        String sem = Objects.toString(semesterBox.getSelectedItem(), "FALL");
        Integer yr = (Integer) yearBox.getSelectedItem();
        if (yr == null) return;

        List<Row> rows = new ArrayList<>();

        for (Registration r : allRegistrations){
            try {
                Offerings o = OfferingHandler.FindOffering(r.GetOfferingID());
                if (o == null || !sem.equalsIgnoreCase(o.GetSemester()) || o.GetYear() != yr) continue;
                if ("DROPPED".equalsIgnoreCase(r.GetStatus()) || "PRE-ENROLLED".equalsIgnoreCase(r.GetStatus())) continue;

                Course c = CourseHandler.FindCourseByID(o.GetCourseID());
                Instructor ins = InstructorHandler.FindInstructorByInstructorID(o.GetInstructorID());

                String code = (c != null) ? String.valueOf(c.GetCode()) : "";
                String title = (c != null) ? c.GetTitle() : "";
                int credits = (c != null) ? c.GetCredits() : 0;
                String instructorName = (ins != null) ? ins.GetName() : "";

                String status = r.GetStatus();
                String displayGrade = "—";

                if ("COMPLETED".equalsIgnoreCase(status)) {
                    displayGrade = GradeSource.GetGradeFor(r.GetOfferingID());
                    if (displayGrade == null) displayGrade = "—";
                } else if ("ENROLLED".equalsIgnoreCase(status)) {
                    displayGrade = "Ongoing";
                }

                rows.add(new Row(r.GetOfferingID(), code, title, credits, instructorName, displayGrade, status));
            }
            catch (SQLException ex){

            }
        }

        rows.sort(Comparator.comparing(a -> a.code));
        tableModel.SetRows(rows);
        UpdateSgpa(rows);
        barChartPanel.updateData(rows);
    }

    private void UpdateSgpa(List<Row> rows){
        double points = 0.0;
        int credits = 0;
        for (Row r : rows){
            if (r.grade == null || r.grade.equals("Ongoing") || r.grade.equals("—")) continue;
            Double gp = GRADE_POINTS.get(r.grade.toUpperCase());
            if (gp != null) { points += gp * r.credits; credits += r.credits; }
        }
        sgpaLabel.setText(credits == 0 ? "SGPA: —" : String.format("SGPA: %.2f  (%.0f / %d)", points / credits, points, credits));
    }

    private static class Row {
        final int offeringId;
        final String code, title, instructor, grade, status;
        final int credits;
        Row(int offeringId, String c, String t, int cr, String i, String g, String s){
            this.offeringId = offeringId;
            code=c; title=t; credits=cr; instructor=i; grade=g; status=s;
        }
    }

    private static class GradeTableModel extends AbstractTableModel {
        private final String[] cols = {"Code", "Title", "Credits", "Instructor", "Grade", "Status", "Breakdown"};
        private List<Row> rows = new ArrayList<>();
        public void SetRows(List<Row> r){ rows = r; fireTableDataChanged(); }
        @Override public int getRowCount(){ return rows.size(); }
        @Override public int getColumnCount(){ return cols.length; }
        @Override public String getColumnName(int c){ return cols[c]; }
        @Override public boolean isCellEditable(int row, int col) { return col == 6; }
        @Override public Class<?> getColumnClass(int col){
            if (col==2) return Integer.class;
            return Object.class;
        }

        @Override public Object getValueAt(int r, int c){
            Row row = rows.get(r);
            return switch (c){
                case 0 -> row.code; case 1 -> row.title; case 2 -> row.credits;
                case 3 -> row.instructor; case 4 -> row.grade; case 5 -> row.status;
                case 6 -> row;
                default -> "";
            };
        }
    }

    private static class GradeCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hF, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, isS, hF, r, c);
            comp.setForeground(Color.WHITE);
            if (v != null){
                String g = v.toString();
                if (g.equalsIgnoreCase("Ongoing")) { comp.setForeground(COLOR_TEXT_SECONDARY); comp.setFont(new Font("SansSerif", Font.ITALIC, 12)); }
                else if (g.matches("F|I")) { comp.setForeground(new Color(0xFF6B6B)); comp.setFont(new Font("SansSerif", Font.BOLD, 12)); }
                else if (!g.equals("—")) { comp.setForeground(COLOR_ACCENT); comp.setFont(new Font("SansSerif", Font.BOLD, 12)); }
            } return comp;
        }
    }

    private static class DetailButtonRenderer extends JButton implements TableCellRenderer {
        public DetailButtonRenderer() {
            setOpaque(true);
            setBackground(COLOR_PANEL);
            setForeground(COLOR_ACCENT);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setBorder(BorderFactory.createLineBorder(COLOR_ACCENT));
            setText("View");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Row) {
                Row r = (Row) value;

                setText("View");

                if ("COMPLETED".equalsIgnoreCase(r.status)) {
                    setEnabled(true);
                    setForeground(COLOR_ACCENT);
                    setBorder(BorderFactory.createLineBorder(COLOR_ACCENT));
                }
                else {
                    setEnabled(false);
                    setForeground(COLOR_TEXT_SECONDARY);
                    setBorder(BorderFactory.createLineBorder(COLOR_TEXT_SECONDARY));
                }
                setBackground(COLOR_PANEL);
            }
            return this;
        }
    }

    private class DetailButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int offeringId;
        private boolean clicked;

        public DetailButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));

            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof Row) {
                Row r = (Row) value;
                offeringId = r.offeringId;

                button.setText("View");

                if ("COMPLETED".equalsIgnoreCase(r.status)) {
                    button.setEnabled(true);
                    button.setBackground(COLOR_ACCENT);
                    button.setForeground(Color.BLACK);
                    clicked = true;
                } else {
                    button.setEnabled(false);
                    button.setBackground(COLOR_PANEL);
                    button.setForeground(COLOR_TEXT_SECONDARY);
                    clicked = false;
                }
            }
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked) {
                ShowComponentGrades(offeringId);
            }
            clicked = false;
            return null;
        }
    }

    private void ShowComponentGrades(int offeringId) {
        try {
            StudentGradeRecord record = StudentGradeRecordHandler.GetStudentGradeRecord(offeringId, studentRollNumber);
            Offerings offering = OfferingHandler.FindOffering(offeringId);
            Course course = CourseHandler.FindCourseByID(offering.GetCourseID());

            if (record == null || offering == null) {
                JOptionPane.showMessageDialog(this, "No detailed grades available yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            new ComponentGradesDialog((Frame) SwingUtilities.getWindowAncestor(this), course.GetTitle(), record, offering).setVisible(true);

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class ComponentGradesDialog extends JDialog {
        public ComponentGradesDialog(Frame owner, String courseName, StudentGradeRecord record, Offerings offering) {
            super(owner, "Grade Breakdown - " + courseName, true);
            setSize(400, 400);
            setLocationRelativeTo(owner);
            getContentPane().setBackground(COLOR_BACKGROUND);
            setLayout(new BorderLayout(10, 10));

            JLabel title = new JLabel(courseName, SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 18));
            title.setForeground(COLOR_TEXT_PRIMARY);
            title.setBorder(new EmptyBorder(15, 0, 10, 0));
            add(title, BorderLayout.NORTH);

            String[] cols = {"Component", "Score", "Max"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            HashMap<String, Integer> maxMarks = offering.GetGradingComponents();
            HashMap<String, Integer> myMarks = record.getGrade();
            int totalObtained = 0;
            int totalMax = 0;

            if (maxMarks != null) {
                for (String comp : maxMarks.keySet()) {
                    int max = maxMarks.get(comp);
                    int obtained = 0;
                    if (myMarks != null && myMarks.containsKey(comp) && myMarks.get(comp) != null) {
                        obtained = myMarks.get(comp);
                    }
                    model.addRow(new Object[]{comp, obtained, max});
                    totalObtained += obtained;
                    totalMax += max;
                }
                model.addRow(new Object[]{"TOTAL", totalObtained, totalMax});
            }

            JTable t = new JTable(model);
            t.setBackground(COLOR_PANEL);
            t.setForeground(COLOR_TEXT_PRIMARY);
            t.setRowHeight(25);
            t.setFont(new Font("SansSerif", Font.PLAIN, 14));
            t.getTableHeader().setBackground(COLOR_BACKGROUND);
            t.getTableHeader().setForeground(COLOR_TEXT_SECONDARY);

            JScrollPane sp = new JScrollPane(t);
            sp.getViewport().setBackground(COLOR_PANEL);
            sp.setBorder(BorderFactory.createEmptyBorder());

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(0, 20, 20, 20));
            wrap.add(sp);

            add(wrap, BorderLayout.CENTER);

            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> dispose());
            JPanel btnPanel = new JPanel();
            btnPanel.setOpaque(false);
            btnPanel.add(closeBtn);
            add(btnPanel, BorderLayout.SOUTH);
        }
    }

    private class GpaChartPanel extends JPanel {
        private final List<TermPoint> points = new ArrayList<>();
        private Double cgpa = null;
        GpaChartPanel(){ setOpaque(false); setLayout(new BorderLayout()); setBorder(new EmptyBorder(8,0,8,0)); setPreferredSize(new Dimension(600,160)); }
        void reload(List<Registration> regs){ points.clear(); cgpa = computePoints(regs, points); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int left = 48, right = w - 16, top = 40, bottom = h - 28;

            g2.setColor(COLOR_PANEL); g2.fillRoundRect(0,0,w,h,14,14); g2.setColor(COLOR_TEXT_PRIMARY); g2.setFont(new Font("SansSerif",Font.BOLD,14)); g2.drawString("SGPA Trend",12,20); String cg="CGPA: "+(cgpa==null?"—":String.format("%.2f",cgpa)); g2.setFont(new Font("SansSerif",Font.PLAIN,12)); g2.drawString(cg,w-120,20); if(points.isEmpty()){g2.dispose();return;} g2.setColor(COLOR_GRID); for(int i=1;i<=4;i++){ int y=40+(h-40-28)*i/5; g2.drawLine(48,y,w-16,y); } double dx=(points.size()==1)?0:(double)(w-16-48)/(points.size()-1); g2.setColor(COLOR_ACCENT); g2.setStroke(new BasicStroke(2f)); for(int i=0;i<points.size()-1;i++){ int y1=yFor(points.get(i).gpa, top, bottom, 0.0, 10.0); int y2=yFor(points.get(i+1).gpa, top, bottom, 0.0, 10.0); g2.drawLine((int)(48+dx*i),y1,(int)(48+dx*(i+1)),y2); } for(int i=0;i<points.size();i++){ int x=(int)(48+dx*i); int y=yFor(points.get(i).gpa, top, bottom, 0.0, 10.0); g2.fill(new Ellipse2D.Double(x-4,y-4,8,8)); g2.setColor(COLOR_TEXT_SECONDARY); g2.setFont(new Font("SansSerif",Font.PLAIN,10)); g2.drawString(points.get(i).label, x-10, bottom+15); g2.setColor(COLOR_ACCENT); } g2.dispose(); }
        private int yFor(double val, int top, int bottom, double min, double max) { double t=(val-min)/(max-min); return (int)(bottom-t*(bottom-top)); }
        private Double computePoints(List<Registration> regs, List<TermPoint> out){ Map<Term, List<Registration>> byTerm = new HashMap<>(); for(Registration r:regs){try{Offerings o=OfferingHandler.FindOffering(r.GetOfferingID());if(o!=null)byTerm.computeIfAbsent(new Term(o.GetYear(),o.GetSemester()),k->new ArrayList<>()).add(r);}catch(Exception e){}} double tp=0; int tc=0; for(Map.Entry<Term,List<Registration>>e:byTerm.entrySet()){double p=0;int c=0;for(Registration r:e.getValue()){if(!"COMPLETED".equalsIgnoreCase(r.GetStatus()))continue;try{Offerings o=OfferingHandler.FindOffering(r.GetOfferingID());Course course=CourseHandler.FindCourseByID(o.GetCourseID());String g=GradeSource.GetGradeFor(r.GetOfferingID());if(g!=null){Double gp=GRADE_POINTS.get(g.toUpperCase());if(gp!=null){p+=gp*course.GetCredits();c+=course.GetCredits();}}}catch(Exception ex){}}if(c>0){out.add(new TermPoint(e.getKey(),p/c));tp+=p;tc+=c;}} out.sort(Comparator.comparing((TermPoint t)->t.term.year).thenComparing(t-> Order(t.term.semester))); return tc==0?null:tp/tc; }
    }
    private class CourseBarChartPanel extends JPanel {
        private List<Row> currentData=new ArrayList<>(); CourseBarChartPanel(){setOpaque(false);setBorder(new EmptyBorder(8,0,8,0));} void updateData(List<Row> rows){this.currentData=new ArrayList<>(rows);repaint();} @Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=getWidth(),h=getHeight();g2.setColor(COLOR_PANEL);g2.fillRoundRect(0,0,w,h,14,14);g2.setColor(COLOR_TEXT_PRIMARY);g2.setFont(new Font("SansSerif",Font.BOLD,14));g2.drawString("Course Performance",12,20);if(currentData.isEmpty()){g2.dispose();return;}int pl=40,pb=30,gw=w-pl-20,gh=h-40-pb,sy=40;g2.setColor(COLOR_GRID);g2.setStroke(new BasicStroke(1f));g2.drawLine(pl,sy+gh,w-20,sy+gh);int c=currentData.size();int bw=Math.min(40,(gw/c)-10);int gap=(gw-(c*bw))/(c+1);for(int i=0;i<c;i++){Row r=currentData.get(i);Double gp=(r.grade==null||r.grade.equals("Ongoing"))?0.0:GRADE_POINTS.getOrDefault(r.grade.toUpperCase(),0.0);int bh=(int)((gp/10.0)*gh);int x=pl+gap+(i*(bw+gap));int y=sy+(gh-bh);g2.setColor(COLOR_ACCENT);g2.fillRoundRect(x,y,bw,bh,4,4);g2.setColor(COLOR_TEXT_PRIMARY);g2.setFont(new Font("SansSerif",Font.PLAIN,10));g2.drawString(r.code.length()>6?r.code.substring(0,6):r.code,x,sy+gh+14);}g2.dispose();}
    }

    private String StyledLabel(String t) {
        return "<html><span style='color:#B0B0B0'>" + t + "</span></html>";
    }
    private void StyleCombo(JComboBox<?> box){
        box.setBackground(COLOR_PANEL); box.setForeground(COLOR_TEXT_PRIMARY);
        box.setFont(new Font("SansSerif", Font.PLAIN, 14));

    }
    private void StylizeButton(AbstractButton b){
        b.setBackground(COLOR_PANEL);
        b.setForeground(COLOR_TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    private int Order(String sem){
        return switch (sem.toUpperCase()) {
            case "SPRING" -> 1;
            case "SUMMER" -> 2;
            case "FALL" -> 3;
            case "WINTER" -> 4;
            default -> 5;
        };
    }

    private static String DetectCurrentSemester(){
        int m = java.time.LocalDate.now().getMonthValue();
        if (m <= 5) return "SPRING";
        if (m <= 7) return "SUMMER";
        return "FALL";
    }
    private static int DetectCurrentYear(){
        return java.time.LocalDate.now().getYear();

    }
    private record Term(int year, String semester){}
    private static class TermPoint {
        final Term term;
        final double gpa;
        final String label;
        TermPoint(Term t, double g){
            this.term=t;
            this.gpa=g;
            this.label=t.semester+" "+t.year;
        }
    }

    private static class JTableHeaderUI {
        static void install(JTable t, Color bg, Color fg){ t.getTableHeader().setBackground(bg);
            t.getTableHeader().setForeground(fg);
            t.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 13));
        }
    }

    private static class TranscriptExporters {
        static void ExportCSV(Component parent, int studentRollNumber) {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("Transcript_" + studentRollNumber + ".csv"));
            if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try (var out = new java.io.PrintWriter(chooser.getSelectedFile())) {
                    out.println("Year,Semester,Code,Title,Credits,Instructor,Grade,Status");
                    List<Registration> regs = RegistrationHandler.FindByStudentRollNumber(studentRollNumber);
                    for (Registration r : regs) {
                        if (!"COMPLETED".equalsIgnoreCase(r.GetStatus()) && !"ENROLLED".equalsIgnoreCase(r.GetStatus())) continue;
                        try {
                            Offerings o = OfferingHandler.FindOffering(r.GetOfferingID()); if (o==null) continue;
                            Course c = CourseHandler.FindCourseByID(o.GetCourseID());
                            String code = (c!=null)? String.valueOf(c.GetCode()) : "";
                            String title = (c!=null)? c.GetTitle() : "";
                            int credits = (c!=null)? c.GetCredits() : 0;
                            String grade = GradeSource.GetGradeFor(r.GetOfferingID());
                            out.printf("%d,%s,%s,%s,%d,N/A,%s,%s%n", o.GetYear(), o.GetSemester(), code, title, credits, grade==null?"":grade, r.GetStatus());
                        }
                        catch (SQLException ignored){}
                    }
                    JOptionPane.showMessageDialog(parent, "Exported!");
                }
                catch (Exception e) {}
            }
        }
    }

    static class GradeSource {
        private static final Map<Integer, String> CACHE = new HashMap<>();
        static void preload(int roll) {
            CACHE.clear();
            try {
                List<StudentGradeRecord> list = StudentGradeRecordHandler.FindByRollNumber(roll);
                for (StudentGradeRecord rec : list) {
                    int offeringID = rec.getOfferingID();
                    HashMap<String, Integer> perCompMarks = rec.getGrade();
                    if (perCompMarks == null || perCompMarks.isEmpty()) {
                        continue;
                    }
                    int totalMarks = 0;
                    for (Integer score : perCompMarks.values()) {
                        if (score != null) {
                            totalMarks += score;
                        }
                    }
                    Offerings o = OfferingHandler.FindOffering(rec.getOfferingID());
                    if (o != null) {
                        String letterGrade = calculateGrade(totalMarks, o.GetGradingSlabs());
                        CACHE.put(rec.getOfferingID(), letterGrade);
                    }
                }
            } catch (SQLException ignored) {}
        }
        static String GetGradeFor(int id) {
            return CACHE.get(id);
        }
        static private String calculateGrade(double score, HashMap<String, Integer> slabs) {
            String bestGrade = "F";
            int bestThreshold = -1;
            if (slabs != null) {
                for (Map.Entry<String, Integer> entry : slabs.entrySet()) {
                    Integer threshold = entry.getValue();
                    if (threshold == null) continue;
                    if (score >= threshold && threshold > bestThreshold) {
                        bestThreshold = threshold;
                        bestGrade = entry.getKey();
                    }
                }
            }
            return bestGrade;
        }
    }
}