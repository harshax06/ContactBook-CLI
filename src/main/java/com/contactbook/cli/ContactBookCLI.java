package com.contactbook.cli;

import com.contactbook.config.DatabaseConfig;
import com.contactbook.dao.ContactDAO;
import com.contactbook.model.Contact;
import com.contactbook.export.CSVExporter;
import com.contactbook.service.ContactService;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ContactBookCLI {
    private final Scanner scanner = new Scanner(System.in);
    private final ContactService service = new ContactService();
    private final ContactDAO dao = service.getDao();

    public static void main(String[] args) {
        try {
            DatabaseConfig.initializeSchema();
            Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConfig::closePool));
            new ContactBookCLI().run();
        } finally {
            DatabaseConfig.closePool();
        }
    }

    public void run() {
        while (true) {
            printMenu();
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addContact();
                    case "2" -> searchContacts();
                    case "3" -> updateContact();
                    case "4" -> deleteContact();
                    case "5" -> service.displayPaginated();
                    case "6" -> dao.printResultSetMetaData();
                    case "7" -> exportCSV();
                    case "0" -> {
                        System.out.println("Closing connection pool... Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.err.println("DB Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("""
            
            ╔════════════════════════════════════╗
            ║        CONTACT BOOK (MySQL)        ║
            ╠════════════════════════════════════╣
            ║  1. Add Contact                    ║
            ║  2. Search (LIKE)                  ║
            ║  3. Update Contact                 ║
            ║  4. Delete Contact                 ║
            ║  5. List All (Paginated)           ║
            ║  6. Show Table Metadata            ║
            ║  7. Export to CSV (Batch)          ║
            ║  0. Exit                           ║
            ╚════════════════════════════════════╝""");
    }

    private void addContact() throws SQLException {
        System.out.print("First name: "); String fn = scanner.nextLine();
        System.out.print("Last name:  "); String ln = scanner.nextLine();
        System.out.print("Email:      "); String em = scanner.nextLine();
        System.out.print("Phone:      "); String ph = scanner.nextLine();
        System.out.print("Address:    "); String ad = scanner.nextLine();

        Contact c = new Contact(fn, ln, em, ph, ad);
        dao.add(c);
        System.out.println("✓ Created contact ID: " + c.getId());
    }

    private void searchContacts() throws SQLException {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine();
        List<Contact> results = dao.search(keyword);

        if (results.isEmpty()) {
            System.out.println("No matches.");
            return;
        }

        System.out.printf("%n%-5s %-15s %-15s %-28s %-15s%n",
                "ID", "First", "Last", "Email", "Phone");
        System.out.println("-".repeat(85));
        for (Contact c : results) {
            System.out.printf("%-5d %-15s %-15s %-28s %-15s%n",
                    c.getId(), c.getFirstName(), c.getLastName(),
                    c.getEmail(), c.getPhone());
        }
        System.out.println("Found: " + results.size() + " result(s)");
    }

    private void updateContact() throws SQLException {
        System.out.print("Contact ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        Optional<Contact> opt = dao.findById(id);

        if (opt.isEmpty()) {
            System.out.println("Contact not found.");
            return;
        }

        Contact c = opt.get();
        System.out.printf("First name [%s]: ", c.getFirstName());
        String fn = scanner.nextLine();
        System.out.printf("Last name  [%s]: ", c.getLastName());
        String ln = scanner.nextLine();
        System.out.printf("Email      [%s]: ", c.getEmail());
        String em = scanner.nextLine();
        System.out.printf("Phone      [%s]: ", c.getPhone());
        String ph = scanner.nextLine();
        System.out.printf("Address    [%s]: ", c.getAddress());
        String ad = scanner.nextLine();

        if (!fn.isBlank()) c.setFirstName(fn);
        if (!ln.isBlank()) c.setLastName(ln);
        if (!em.isBlank()) c.setEmail(em);
        if (!ph.isBlank()) c.setPhone(ph);
        if (!ad.isBlank()) c.setAddress(ad);

        if (dao.update(c)) System.out.println("✓ Updated.");
    }

    private void deleteContact() throws SQLException {
        System.out.print("Contact ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine());
        if (dao.delete(id)) System.out.println("✓ Deleted.");
        else System.out.println("Not found.");
    }

    private void exportCSV() throws Exception {
        System.out.print("Output filename: ");
        String filename = scanner.nextLine();
        System.out.print("Batch size: ");
        int batch = Integer.parseInt(scanner.nextLine());

        new CSVExporter().exportToCSV(Paths.get(filename), batch);
    }
}
