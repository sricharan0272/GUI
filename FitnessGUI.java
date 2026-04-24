package efc.ui;

import efc.model.*;
import efc.service.BookingManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FitnessGUI {

    private BookingManager bm;
    private JTable table;
    private DefaultTableModel model;

    private String lastSelectedBookingRef = "";

    public FitnessGUI(BookingManager bm) {
        this.bm = bm;
    }

    public void launch() {

        JFrame frame = new JFrame("Elite Fitness Club");
        frame.setSize(1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // ===== TITLE =====
        JLabel title = new JLabel("Elite Fitness Club Booking System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(title, BorderLayout.NORTH);

        // ===== LEFT PANEL =====
        JPanel panel = new JPanel(new GridLayout(11, 1, 10, 10));

        JButton viewScheduleBtn = new JButton("View Schedule");
        JButton viewMembersBtn = new JButton("View Members");
        JButton bookingsBtn = new JButton("My Bookings");
        JButton bookBtn = new JButton("Book Class");
        JButton modifyBtn = new JButton("Modify Booking");
        JButton cancelBtn = new JButton("Cancel Booking");
        JButton attendBtn = new JButton("Attend & Rate");
        JButton reportBtn = new JButton("Attendance Report");
        JButton revenueBtn = new JButton("Revenue Report");
        JButton clearBtn = new JButton("Clear Table");
        JButton exitBtn = new JButton("Exit");

        panel.add(viewScheduleBtn);
        panel.add(viewMembersBtn);
        panel.add(bookingsBtn);
        panel.add(bookBtn);
        panel.add(modifyBtn);
        panel.add(cancelBtn);
        panel.add(attendBtn);
        panel.add(reportBtn);
        panel.add(revenueBtn);
        panel.add(clearBtn);
        panel.add(exitBtn);

        frame.add(panel, BorderLayout.WEST);

        // ===== TABLE =====
        model = new DefaultTableModel();
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== FILTER PANEL =====
        JPanel filterPanel = new JPanel();

        JComboBox<String> dayFilter = new JComboBox<>(new String[]{
                "All", "SATURDAY", "SUNDAY"
        });

        JComboBox<String> typeFilter = new JComboBox<>(new String[]{
                "All", "Pilates", "HIIT", "CrossFit", "Yoga Flow", "Strength Training"
        });

        JButton applyFilterBtn = new JButton("Apply Filter");
        JButton resetFilterBtn = new JButton("Reset");

        filterPanel.add(new JLabel("Day:"));
        filterPanel.add(dayFilter);
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeFilter);
        filterPanel.add(applyFilterBtn);
        filterPanel.add(resetFilterBtn);

        frame.add(filterPanel, BorderLayout.SOUTH);

        // ===== AUTO-FILL BOOKING REF =====
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && model.getColumnCount() > 0) {
                if (model.getColumnName(0).equals("Booking Ref")) {
                    lastSelectedBookingRef = table.getValueAt(row, 0).toString();
                }
            }
        });

        // ================= ACTIONS =================

        // 🔹 VIEW SCHEDULE
        viewScheduleBtn.addActionListener(e -> loadSchedule());

        // 🔹 FILTER APPLY
        applyFilterBtn.addActionListener(e -> {

            model.setColumnIdentifiers(new String[]{
                    "Session ID", "Type", "Day", "Time", "Week", "Fee", "Spots"
            });

            model.setRowCount(0);

            String selectedDay = dayFilter.getSelectedItem().toString();
            String selectedType = typeFilter.getSelectedItem().toString();

            for (ClassSession s : bm.getSchedule().getAllSessions()) {

                boolean matchDay = selectedDay.equals("All") ||
                        s.getDay().toString().equalsIgnoreCase(selectedDay);

                boolean matchType = selectedType.equals("All") ||
                        s.getFitnessType().equalsIgnoreCase(selectedType);

                if (matchDay && matchType) {
                    model.addRow(new Object[]{
                            s.getSessionId(),
                            s.getFitnessType(),
                            s.getDay(),
                            s.getTimeSlot(),
                            s.getWeekNumber(),
                            s.getFee(),
                            s.getEnrolledIds().size() + "/4"
                    });
                }
            }
        });

        // 🔹 RESET FILTER
        resetFilterBtn.addActionListener(e -> {
            dayFilter.setSelectedIndex(0);
            typeFilter.setSelectedIndex(0);
            loadSchedule();
        });

        // 🔹 VIEW MEMBERS
        viewMembersBtn.addActionListener(e -> {
            model.setColumnIdentifiers(new String[]{
                    "Member ID", "Name", "Phone", "Email"
            });
            model.setRowCount(0);

            for (Member m : bm.getAllMembers()) {
                model.addRow(new Object[]{
                        m.getMemberId(),
                        m.getFullName(),
                        m.getPhone(),
                        m.getEmail()
                });
            }
        });

        // 🔹 BOOKINGS
        bookingsBtn.addActionListener(e -> {
            model.setColumnIdentifiers(new String[]{
                    "Booking Ref", "Member", "Session", "Status"
            });
            model.setRowCount(0);

            for (Booking b : bm.getAllBookings()) {
                model.addRow(new Object[]{
                        b.getBookingRef(),
                        b.getMemberId(),
                        b.getSessionId(),
                        b.getStatus()
                });
            }
        });

        // 🔹 BOOK
        bookBtn.addActionListener(e -> {

            JComboBox<String> memberBox = new JComboBox<>();
            JComboBox<String> sessionBox = new JComboBox<>();

            for (Member m : bm.getAllMembers()) memberBox.addItem(m.getMemberId());
            for (ClassSession s : bm.getSchedule().getAllSessions()) sessionBox.addItem(s.getSessionId());

            JPanel form = new JPanel(new GridLayout(2, 2));
            form.add(new JLabel("Member:"));
            form.add(memberBox);
            form.add(new JLabel("Session:"));
            form.add(sessionBox);

            if (JOptionPane.showConfirmDialog(frame, form, "Book Class",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                try {
                    Booking b = bm.placeBooking(
                            (String) memberBox.getSelectedItem(),
                            (String) sessionBox.getSelectedItem()
                    );

                    JOptionPane.showMessageDialog(frame,
                            "Booking Successful!\nRef: " + b.getBookingRef());

                    loadSchedule();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });

        // 🔹 MODIFY
        modifyBtn.addActionListener(e -> {

            JTextField refField = new JTextField(lastSelectedBookingRef);

            JComboBox<String> sessionBox = new JComboBox<>();
            for (ClassSession s : bm.getSchedule().getAllSessions()) sessionBox.addItem(s.getSessionId());

            JPanel form = new JPanel(new GridLayout(2, 2));
            form.add(new JLabel("Booking Ref:"));
            form.add(refField);
            form.add(new JLabel("New Session:"));
            form.add(sessionBox);

            if (JOptionPane.showConfirmDialog(frame, form, "Modify Booking",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                try {
                    bm.modifyBooking(refField.getText(),
                            (String) sessionBox.getSelectedItem());

                    JOptionPane.showMessageDialog(frame, "Booking Modified!");
                    loadSchedule();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });

        // 🔹 CANCEL
        cancelBtn.addActionListener(e -> {
            String ref = lastSelectedBookingRef;
            if (ref.isEmpty())
                ref = JOptionPane.showInputDialog("Enter Booking Ref:");

            try {
                bm.cancelBooking(ref);
                JOptionPane.showMessageDialog(frame, "Booking Cancelled!");
                loadSchedule();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        // 🔹 ATTEND & RATE
        attendBtn.addActionListener(e -> {

            JTextField refField = new JTextField(lastSelectedBookingRef);
            JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{1,2,3,4,5});
            JTextField commentField = new JTextField();

            JPanel form = new JPanel(new GridLayout(3,2));
            form.add(new JLabel("Booking Ref:"));
            form.add(refField);
            form.add(new JLabel("Rating:"));
            form.add(ratingBox);
            form.add(new JLabel("Comment:"));
            form.add(commentField);

            if (JOptionPane.showConfirmDialog(frame, form,
                    "Attend & Rate", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                try {
                    bm.recordAttendance(
                            refField.getText(),
                            (Integer) ratingBox.getSelectedItem(),
                            commentField.getText()
                    );

                    JOptionPane.showMessageDialog(frame, "Feedback Recorded!");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });

        // 🔹 ATTENDANCE REPORT
        reportBtn.addActionListener(e -> {
            model.setColumnIdentifiers(new String[]{
                    "Session", "Type", "Day", "Slot", "Week", "Attendees", "Avg Rating"
            });
            model.setRowCount(0);

            for (ClassSession s : bm.getSchedule().getAllSessions()) {
                if (s.getAttendanceCount() > 0) {
                    model.addRow(new Object[]{
                            s.getSessionId(),
                            s.getFitnessType(),
                            s.getDay(),
                            s.getTimeSlot(),
                            s.getWeekNumber(),
                            s.getAttendanceCount(),
                            String.format("%.2f", s.getAverageRating())
                    });
                }
            }
        });

        // 🔹 REVENUE REPORT
        revenueBtn.addActionListener(e -> {

            model.setColumnIdentifiers(new String[]{
                    "Fitness Type", "Revenue (£)"
            });

            model.setRowCount(0);

            Map<String, Double> map = new HashMap<>();

            for (ClassSession s : bm.getSchedule().getAllSessions()) {
                if (s.getAttendanceCount() > 0) {
                    map.merge(s.getFitnessType(),
                            s.getFee() * s.getAttendanceCount(),
                            Double::sum);
                }
            }

            double max = 0;
            String top = "";

            for (var e2 : map.entrySet()) {
                if (e2.getValue() > max) {
                    max = e2.getValue();
                    top = e2.getKey();
                }
            }

            for (var e2 : map.entrySet()) {
                model.addRow(new Object[]{
                        e2.getKey().equals(top) ? e2.getKey() + " ⭐ TOP" : e2.getKey(),
                        String.format("%.2f", e2.getValue())
                });
            }
        });

        clearBtn.addActionListener(e -> model.setRowCount(0));
        exitBtn.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private void loadSchedule() {
        model.setColumnIdentifiers(new String[]{
                "Session ID", "Type", "Day", "Time", "Week", "Fee", "Spots"
        });

        model.setRowCount(0);

        for (ClassSession s : bm.getSchedule().getAllSessions()) {
            model.addRow(new Object[]{
                    s.getSessionId(),
                    s.getFitnessType(),
                    s.getDay(),
                    s.getTimeSlot(),
                    s.getWeekNumber(),
                    s.getFee(),
                    s.getEnrolledIds().size() + "/4"
            });
        }
    }
}