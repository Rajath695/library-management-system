import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class data {
    private String username;
    private String password;
    private String role;

    // Constructor
    public data(String username,
            String password,
            String role

    ) {
        this.username = username;
        this.password = password;
        this.role = role;

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

class book {
    private String title;
    private String author;

    public book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}

class manager {
    private int currentUserId = -1;

    private Scanner scanner = new Scanner(System.in);

    public manager() {

    }

    public boolean rolevalidate(String role) {

        if (role.equals("user") || role.equals("admin")) {
            return true;
        } else {
            System.out.println("Invalid role. Please enter 'user' or 'admin'.");
            return false;
        }

    }

    // Register a new user
    public void registeruser() {

        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ps.executeUpdate();
            System.out.println("Registration successful.");
        } catch (SQLException e) {
            // Unique constraint violation = username already taken
            if (e.getMessage().contains("duplicate key")) {
                System.out.println("Username already taken.");
            } else {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    public void usermenu() {
        System.out.println("\n User Menu");
        System.out.println("1. View Books");
        System.out.println("2. Borrow Book");
        System.out.println("3. Borrowed Books");
        System.out.println("4. Return Book");
        System.out.println("5. Logout");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline left-over

        while (choice != 5) {
            switch (choice) {
                case 1:
                    viewbooks();
                    break;
                case 2:
                    borrowbook();
                    break;
                case 3:
                    returnbook();
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1, 2, 3, 4, or 5.");
                    break;
            }
            System.out.println("\n User Menu");
            System.out.println("1. View Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over
        }
    }

    public void loginuser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");

        String password = scanner.nextLine();

        String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentUserId = rs.getInt("user_id");
                    System.out.println("Login successful.");
                    this.usermenu();
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        System.out.println("Login failed. Username or password incorrect.");

    }

    public void loginadmin() {
        System.out.print("Enter Admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql = "SELECT admin_id FROM admins WHERE username = ? AND password = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                currentUserId = -1; // admin isn't a borrower
                System.out.println("Login successful.");
                this.adminmenu();
                return;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

    }

    public void viewbooks() {

        String sql = "SELECT title, author FROM books WHERE available = TRUE ORDER BY title";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            boolean any = false;
            while (rs.next()) {
                if (!any) {
                    System.out.println("\nAvailable Books:");
                    any = true;
                }
                System.out.println("Title: " + rs.getString("title")
                        + ", Author: " + rs.getString("author"));
            }

            if (!any) {
                System.out.println("No books available.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void borrowbook() {
        System.out.print("Enter the title of the book to borrow: ");
        String title = scanner.nextLine();

        String findSql = "SELECT book_id FROM books WHERE LOWER(title) = LOWER(?) AND available = TRUE";
        String updateBookSql = "UPDATE books SET available = FALSE WHERE book_id = ?";
        String insertLoanSql = "INSERT INTO loans (user_id, book_id) VALUES (?, ?)";

        try (Connection conn = Database.getConnection()) {

            // Step 1: find the book
            int bookId = -1;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, title);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        bookId = rs.getInt("book_id");
                    }
                }
            }

            if (bookId == -1) {
                System.out.println("Book not found or already borrowed.");
                return;
            }

            // Step 2: mark it unavailable
            try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            // Step 3: record the loan
            try (PreparedStatement ps = conn.prepareStatement(insertLoanSql)) {
                ps.setInt(1, currentUserId);
                ps.setInt(2, bookId);
                ps.executeUpdate();
            }

            System.out.println("You have borrowed: " + title);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void returnbook() {
        System.out.print("Enter the title of the book to return: ");
        String title = scanner.nextLine();

        String findSql = "SELECT book_id FROM books WHERE LOWER(title) = LOWER(?) AND available = FALSE";
        String updateBookSql = "UPDATE books SET available = TRUE WHERE book_id = ?";
        String updateLoanSql = "UPDATE loans SET is_returned = TRUE, return_date = CURRENT_DATE " +
                "WHERE book_id = ? AND user_id = ? AND is_returned = FALSE";

        try (Connection conn = Database.getConnection()) {

            // Step 1: find the borrowed book
            int bookId = -1;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, title);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        bookId = rs.getInt("book_id");
                    }
                }
            }

            if (bookId == -1) {
                System.out.println("Book not found in borrowed list.");
                return;
            }

            // Step 2: mark the book as available again
            try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            // Step 3: close the loan record
            try (PreparedStatement ps = conn.prepareStatement(updateLoanSql)) {
                ps.setInt(1, bookId);
                ps.setInt(2, currentUserId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    System.out.println("Warning: no matching loan found for this user.");
                }
            }

            System.out.println("You have returned: " + title);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void adminmenu() {
        System.out.println("\n Admin Menu");
        System.out.println("1. Register Book");
        System.out.println("2. Logout");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline left-over

        while (choice != 2) {
            switch (choice) {
                case 1:
                    registerbooks();
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1 or 2.");
                    break;
            }
            System.out.println("\n Admin Menu");
            System.out.println("1. Register Book");
            System.out.println("2. Logout");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over
        }
    }

    public void start() {
        while (true) {
            System.out.println("\n Management System");
            System.out.println("1. User");
            System.out.println("2. Register User");
            System.out.println("3. Admin");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over

            switch (choice) {
                case 1:
                    loginuser();
                    break;
                case 2:
                    registeruser();
                    break;
                case 3:
                    loginadmin();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1, 2, 3, or 4.");
                    break;
            }
        }

    }

    public void registerbooks() {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book author: ");
        String author = scanner.nextLine();

        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.executeUpdate();
            System.out.println("Book registered successfully.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

}

public class Main {
    public static void main(String[] args) {
        manager Manager = new manager();
        Manager.start();

    }
}
