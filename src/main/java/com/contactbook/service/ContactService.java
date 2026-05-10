package com.contactbook.service;


import com.contactbook.dao.ContactDAO;
import com.contactbook.model.Contact;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ContactService {
    private final ContactDAO dao = new ContactDAO();
    private static final int PAGE_SIZE = 5;
    private final Scanner scanner = new Scanner(System.in);

    public void displayPaginated() throws SQLException {
        int total = dao.countAll();
        if (total == 0) {
            System.out.println("No contacts found.");
            return;
        }

        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        int currentPage = 1;

        while (true) {
            int offset = (currentPage - 1) * PAGE_SIZE;
            List<Contact> page = dao.findAllPaginated(PAGE_SIZE, offset);

            System.out.printf("%n┌──────── Page %d of %d (Total: %d) ────────┐%n",
                    currentPage, totalPages, total);
            System.out.printf("│ %-4s │ %-12s │ %-12s │ %-22s │ %-14s │%n",
                    "ID", "First", "Last", "Email", "Phone");
            System.out.println("├──────┼──────────────┼──────────────┼────────────────────────┼────────────────┤");

            for (Contact c : page) {
                System.out.printf("│ %-4d │ %-12s │ %-12s │ %-22s │ %-14s │%n",
                        c.getId(),
                        truncate(c.getFirstName(), 12),
                        truncate(c.getLastName(), 12),
                        truncate(c.getEmail(), 22),
                        truncate(c.getPhone(), 14));
            }
            System.out.println("└──────┴──────────────┴──────────────┴────────────────────────┴────────────────┘");

            if (totalPages <= 1) break;

            System.out.print("[n]ext | [p]revious | [q]uit: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "n" -> { if (currentPage < totalPages) currentPage++; }
                case "p" -> { if (currentPage > 1) currentPage--; }
                case "q" -> { return; }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    public ContactDAO getDao() { return dao; }
}