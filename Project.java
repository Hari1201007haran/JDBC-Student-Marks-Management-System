import java.sql.*;
import java.util.Scanner;

public class StudentMarksSystem {
    
    private static final String URL = "jdbc:mysql://localhost:3306/student_marks_db";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    
    private static Connection connection = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
         
            createTables();
            
 
            boolean running = true;
            while (running) {
                System.out.println("\nStudent Marks Management System");
                System.out.println("1. Student Operations");
                System.out.println("2. Subject Operations");
                System.out.println("3. Marks Operations");
                System.out.println("4. Generate Reports");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (choice) {
                    case 1:
                        studentOperations();
                        break;
                    case 2:
                        subjectOperations();
                        break;
                    case 3:
                        marksOperations();
                        break;
                    case 4:
                        generateReports();
                        break;
                    case 5:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void createTables() throws SQLException {
        // Create students table
        String createStudentsTable = "CREATE TABLE IF NOT EXISTS students (" +
                "student_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE, " +
                "phone VARCHAR(15), " +
                "department VARCHAR(50))";
        
        // Create subjects table
        String createSubjectsTable = "CREATE TABLE IF NOT EXISTS subjects (" +
                "subject_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "subject_name VARCHAR(100) NOT NULL, " +
                "subject_code VARCHAR(20) UNIQUE, " +
                "credit_hours INT)";
        
        // Create marks table
        String createMarksTable = "CREATE TABLE IF NOT EXISTS marks (" +
                "mark_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_id INT, " +
                "subject_id INT, " +
                "marks_obtained DECIMAL(5,2), " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                "FOREIGN KEY (subject_id) REFERENCES subjects(subject_id))";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createStudentsTable);
            stmt.execute(createSubjectsTable);
            stmt.execute(createMarksTable);
        }
    }
    
    private static void studentOperations() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nStudent Operations");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. View Student by ID");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    viewAllStudents();
                    break;
                case 3:
                    viewStudentById();
                    break;
                case 4:
                    updateStudent();
                    break;
                case 5:
                    deleteStudent();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void addStudent() throws SQLException {
        System.out.println("\nAdd New Student");
        System.out.print("Enter student name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Enter department: ");
        String department = scanner.nextLine();
        
        String sql = "INSERT INTO students (name, email, phone, department) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, department);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Student added successfully!");
            }
        }
    }
    
    private static void viewAllStudents() throws SQLException {
        System.out.println("\nAll Students");
        String sql = "SELECT * FROM students";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.printf("%-10s %-20s %-30s %-15s %-20s%n", 
                    "ID", "Name", "Email", "Phone", "Department");
            System.out.println("-------------------------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-10d %-20s %-30s %-15s %-20s%n",
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("department"));
            }
        }
    }
    
    private static void viewStudentById() throws SQLException {
        System.out.print("\nEnter student ID: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\nStudent Details");
                System.out.println("ID: " + rs.getInt("student_id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Department: " + rs.getString("department"));
            } else {
                System.out.println("Student not found with ID: " + id);
            }
        }
    }
    
    private static void updateStudent() throws SQLException {
        System.out.print("\nEnter student ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // Check if student exists
        String checkSql = "SELECT * FROM students WHERE student_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("Student not found with ID: " + id);
                return;
            }
        }
        
        System.out.print("Enter new name (leave blank to keep current): ");
        String name = scanner.nextLine();
        
        System.out.print("Enter new email (leave blank to keep current): ");
        String email = scanner.nextLine();
        
        System.out.print("Enter new phone (leave blank to keep current): ");
        String phone = scanner.nextLine();
        
        System.out.print("Enter new department (leave blank to keep current): ");
        String department = scanner.nextLine();
        
        String sql = "UPDATE students SET name = COALESCE(NULLIF(?, ''), name), " +
                     "email = COALESCE(NULLIF(?, ''), email), " +
                     "phone = COALESCE(NULLIF(?, ''), phone), " +
                     "department = COALESCE(NULLIF(?, ''), department) " +
                     "WHERE student_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name.isEmpty() ? null : name);
            pstmt.setString(2, email.isEmpty() ? null : email);
            pstmt.setString(3, phone.isEmpty() ? null : phone);
            pstmt.setString(4, department.isEmpty() ? null : department);
            pstmt.setInt(5, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Student updated successfully!");
            }
        }
    }
    
    private static void deleteStudent() throws SQLException {
        System.out.print("\nEnter student ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // First delete marks records for this student to maintain referential integrity
        String deleteMarksSql = "DELETE FROM marks WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteMarksSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Student deleted successfully!");
            } else {
                System.out.println("Student not found with ID: " + id);
            }
        }
    }
    
    private static void subjectOperations() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nSubject Operations");
            System.out.println("1. Add Subject");
            System.out.println("2. View All Subjects");
            System.out.println("3. View Subject by ID");
            System.out.println("4. Update Subject");
            System.out.println("5. Delete Subject");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    addSubject();
                    break;
                case 2:
                    viewAllSubjects();
                    break;
                case 3:
                    viewSubjectById();
                    break;
                case 4:
                    updateSubject();
                    break;
                case 5:
                    deleteSubject();
                    break;
                case 6:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void addSubject() throws SQLException {
        System.out.println("\nAdd New Subject");
        System.out.print("Enter subject name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter subject code: ");
        String code = scanner.nextLine();
        
        System.out.print("Enter credit hours: ");
        int credits = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        String sql = "INSERT INTO subjects (subject_name, subject_code, credit_hours) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, code);
            pstmt.setInt(3, credits);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Subject added successfully!");
            }
        }
    }
    
    private static void viewAllSubjects() throws SQLException {
        System.out.println("\nAll Subjects");
        String sql = "SELECT * FROM subjects";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.printf("%-10s %-30s %-15s %-10s%n", 
                    "ID", "Name", "Code", "Credits");
            System.out.println("--------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-10d %-30s %-15s %-10d%n",
                        rs.getInt("subject_id"),
                        rs.getString("subject_name"),
                        rs.getString("subject_code"),
                        rs.getInt("credit_hours"));
            }
        }
    }
    
    private static void viewSubjectById() throws SQLException {
        System.out.print("\nEnter subject ID: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        String sql = "SELECT * FROM subjects WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\nSubject Details");
                System.out.println("ID: " + rs.getInt("subject_id"));
                System.out.println("Name: " + rs.getString("subject_name"));
                System.out.println("Code: " + rs.getString("subject_code"));
                System.out.println("Credit Hours: " + rs.getInt("credit_hours"));
            } else {
                System.out.println("Subject not found with ID: " + id);
            }
        }
    }
    
    private static void updateSubject() throws SQLException {
        System.out.print("\nEnter subject ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // Check if subject exists
        String checkSql = "SELECT * FROM subjects WHERE subject_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("Subject not found with ID: " + id);
                return;
            }
        }
        
        System.out.print("Enter new name (leave blank to keep current): ");
        String name = scanner.nextLine();
        
        System.out.print("Enter new code (leave blank to keep current): ");
        String code = scanner.nextLine();
        
        System.out.print("Enter new credit hours (enter 0 to keep current): ");
        int credits = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        String sql = "UPDATE subjects SET subject_name = COALESCE(NULLIF(?, ''), subject_name), " +
                     "subject_code = COALESCE(NULLIF(?, ''), subject_code), " +
                     "credit_hours = ? " +
                     "WHERE subject_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name.isEmpty() ? null : name);
            pstmt.setString(2, code.isEmpty() ? null : code);
            pstmt.setInt(3, credits == 0 ? getCurrentCredits(id) : credits);
            pstmt.setInt(4, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Subject updated successfully!");
            }
        }
    }
    
    private static int getCurrentCredits(int subjectId) throws SQLException {
        String sql = "SELECT credit_hours FROM subjects WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("credit_hours");
            }
        }
        return 0;
    }
    
    private static void deleteSubject() throws SQLException {
        System.out.print("\nEnter subject ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // First delete marks records for this subject to maintain referential integrity
        String deleteMarksSql = "DELETE FROM marks WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteMarksSql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
        
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Subject deleted successfully!");
            } else {
                System.out.println("Subject not found with ID: " + id);
            }
        }
    }
    
    private static void MarksOperations() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nMarks Operations");
            System.out.println("1. Add/Update Marks");
            System.out.println("2. View Marks by Student");
            System.out.println("3. View Marks by Subject");
            System.out.println("4. Delete Marks");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    addOrUpdateMarks();
                    break;
                case 2:
                    viewMarksByStudent();
                    break;
                case 3:
                    viewMarksBySubject();
                    break;
                case 4:
                    deleteMarks();
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void addOrUpdateMarks() throws SQLException {
        System.out.println("\nAdd/Update Marks");
        System.out.print("Enter student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        System.out.print("Enter subject ID: ");
        int subjectId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        System.out.print("Enter marks obtained: ");
        double marks = scanner.nextDouble();
        scanner.nextLine(); // consume newline
        
        // Check if marks record already exists
        String checkSql = "SELECT * FROM marks WHERE student_id = ? AND subject_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, subjectId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Update existing record
                String updateSql = "UPDATE marks SET marks_obtained = ? WHERE student_id = ? AND subject_id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, marks);
                    updateStmt.setInt(2, studentId);
                    updateStmt.setInt(3, subjectId);
                    
                    int affectedRows = updateStmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Marks updated successfully!");
                    }
                }
            } else {
                // Insert new record
                String insertSql = "INSERT INTO marks (student_id, subject_id, marks_obtained) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, studentId);
                    insertStmt.setInt(2, subjectId);
                    insertStmt.setDouble(3, marks);
                    
                    int affectedRows = insertStmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Marks added successfully!");
                    }
                }
            }
        }
    }
    
    private static void viewMarksByStudent() throws SQLException {
        System.out.print("\nEnter student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        String sql = "SELECT s.subject_name, m.marks_obtained " +
                     "FROM marks m " +
                     "JOIN subjects s ON m.subject_id = s.subject_id " +
                     "WHERE m.student_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\nMarks for Student ID: " + studentId);
            System.out.printf("%-30s %-10s%n", "Subject", "Marks");
            System.out.println("----------------------------------------");
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-30s %-10.2f%n",
                        rs.getString("subject_name"),
                        rs.getDouble("marks_obtained"));
            }
            
            if (!found) {
                System.out.println("No marks found for this student.");
            }
        }
    }
    
    private static void viewMarksBySubject() throws SQLException {
        System.out.print("\nEnter subject ID: ");
        int subjectId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        String sql = "SELECT st.name, m.marks_obtained " +
                     "FROM marks m " +
                     "JOIN students st ON m.student_id = st.student_id " +
                     "WHERE m.subject_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\nMarks for Subject ID: " + subjectId);
            System.out.printf("%-20s %-10s%n", "Student", "Marks");
            System.out.println("--------------------------------");
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-20s %-10.2f%n",
                        rs.getString("name"),
                        rs.getDouble("marks_obtained"));
            }
            
            if (!found) {
                System.out.println("No marks found for this subject.");
            }
        }
    }
    
    private static void deleteMarks() throws SQLException {
        System.out.print("\nEnter student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        System.out.print("Enter subject ID: ");
        int subjectId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        String sql = "DELETE FROM marks WHERE student_id = ? AND subject_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Marks record deleted successfully!");
            } else {
                System.out.println("No marks record found for this student and subject.");
            }
        }
    }
    
    private static void generateReports() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nGenerate Reports");
            System.out.println("1. Student Report Card");
            System.out.println("2. Subject-wise Performance");
            System.out.println("3. Class Performance Summary");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    generateStudentReportCard();
                    break;
                case 2:
                    generateSubjectPerformanceReport();
                    break;
                case 3:
                    generateClassPerformanceSummary();
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void generateStudentReportCard() throws SQLException {
        System.out.print("\nEnter student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // Get student details
        String studentSql = "SELECT * FROM students WHERE student_id = ?";
        try (PreparedStatement studentStmt = connection.prepareStatement(studentSql)) {
            studentStmt.setInt(1, studentId);
            ResultSet studentRs = studentStmt.executeQuery();
            
            if (!studentRs.next()) {
                System.out.println("Student not found with ID: " + studentId);
                return;
            }
            
            System.out.println("\nREPORT CARD");
            System.out.println("Student ID: " + studentRs.getInt("student_id"));
            System.out.println("Name: " + studentRs.getString("name"));
            System.out.println("Department: " + studentRs.getString("department"));
            System.out.println("\nSubject-wise Marks:");
            System.out.println("----------------------------------------");
            System.out.printf("%-30s %-10s %-10s%n", "Subject", "Marks", "Grade");
            System.out.println("----------------------------------------");
        }
        
        // Get marks details
        String marksSql = "SELECT s.subject_name, m.marks_obtained, s.credit_hours " +
                          "FROM marks m " +
                          "JOIN subjects s ON m.subject_id = s.subject_id " +
                          "WHERE m.student_id = ?";
        
        double totalMarks = 0;
        int totalCredits = 0;
        
        try (PreparedStatement marksStmt = connection.prepareStatement(marksSql)) {
            marksStmt.setInt(1, studentId);
            ResultSet marksRs = marksStmt.executeQuery();
            
            boolean found = false;
            while (marksRs.next()) {
                found = true;
                double marks = marksRs.getDouble("marks_obtained");
                int credits = marksRs.getInt("credit_hours");
                String grade = calculateGrade(marks);
                
                System.out.printf("%-30s %-10.2f %-10s%n",
                        marksRs.getString("subject_name"),
                        marks,
                        grade);
                
                totalMarks += marks * credits;
                totalCredits += credits;
            }
            
            if (!found) {
                System.out.println("No marks records found for this student.");
                return;
            }
        }
        
        // Calculate GPA
        if (totalCredits > 0) {
            double gpa = totalMarks / (totalCredits * 100) * 4.0; // Scale to 4.0
            System.out.println("----------------------------------------");
            System.out.printf("GPA: %.2f/4.0%n", gpa);
        }
    }
    
    private static String calculateGrade(double marks) {
        if (marks >= 90) return "A+";
        if (marks >= 85) return "A";
        if (marks >= 80) return "A-";
        if (marks >= 75) return "B+";
        if (marks >= 70) return "B";
        if (marks >= 65) return "B-";
        if (marks >= 60) return "C+";
        if (marks >= 55) return "C";
        if (marks >= 50) return "C-";
        if (marks >= 45) return "D+";
        if (marks >= 40) return "D";
        return "F";
    }
    
    private static void generateSubjectPerformanceReport() throws SQLException {
        System.out.print("\nEnter subject ID: ");
        int subjectId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // Get subject details
        String subjectSql = "SELECT * FROM subjects WHERE subject_id = ?";
        try (PreparedStatement subjectStmt = connection.prepareStatement(subjectSql)) {
            subjectStmt.setInt(1, subjectId);
            ResultSet subjectRs = subjectStmt.executeQuery();
            
            if (!subjectRs.next()) {
                System.out.println("Subject not found with ID: " + subjectId);
                return;
            }
            
            System.out.println("\nSUBJECT PERFORMANCE REPORT");
            System.out.println("Subject: " + subjectRs.getString("subject_name"));
            System.out.println("Code: " + subjectRs.getString("subject_code"));
            System.out.println("Credit Hours: " + subjectRs.getInt("credit_hours"));
            System.out.println("\nStudent Performance:");
            System.out.println("----------------------------------------");
            System.out.printf("%-20s %-10s %-10s%n", "Student", "Marks", "Grade");
            System.out.println("----------------------------------------");
        }
        
        // Get performance details
        String performanceSql = "SELECT st.name, m.marks_obtained " +
                               "FROM marks m " +
                               "JOIN students st ON m.student_id = st.student_id " +
                               "WHERE m.subject_id = ? " +
                               "ORDER BY m.marks_obtained DESC";
        
        int count = 0;
        double totalMarks = 0;
        double highest = 0;
        double lowest = 100;
        
        try (PreparedStatement performanceStmt = connection.prepareStatement(performanceSql)) {
            performanceStmt.setInt(1, subjectId);
            ResultSet performanceRs = performanceStmt.executeQuery();
            
            boolean found = false;
            while (performanceRs.next()) {
                found = true;
                count++;
                double marks = performanceRs.getDouble("marks_obtained");
                totalMarks += marks;
                
                if (marks > highest) highest = marks;
                if (marks < lowest) lowest = marks;
                
                System.out.printf("%-20s %-10.2f %-10s%n",
                        performanceRs.getString("name"),
                        marks,
                        calculateGrade(marks));
            }
            
            if (!found) {
                System.out.println("No marks records found for this subject.");
                return;
            }
        }
        
        // Calculate statistics
        double average = totalMarks / count;
        System.out.println("----------------------------------------");
        System.out.printf("Highest Marks: %.2f%n", highest);
        System.out.printf("Lowest Marks: %.2f%n", lowest);
        System.out.printf("Average Marks: %.2f%n", average);
    }
    
    private static void generateClassPerformanceSummary() throws SQLException {
        System.out.println("\nCLASS PERFORMANCE SUMMARY");
        
        // Get total students
        String studentCountSql = "SELECT COUNT(*) AS total FROM students";
        int totalStudents = 0;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(studentCountSql)) {
            if (rs.next()) {
                totalStudents = rs.getInt("total");
            }
        }
        
        // Get total subjects
        String subjectCountSql = "SELECT COUNT(*) AS total FROM subjects";
        int totalSubjects = 0;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(subjectCountSql)) {
            if (rs.next()) {
                totalSubjects = rs.getInt("total");
            }
        }
        
        // Get marks statistics
        String statsSql = "SELECT " +
                         "COUNT(*) AS records, " +
                         "AVG(marks_obtained) AS average, " +
                         "MAX(marks_obtained) AS highest, " +
                         "MIN(marks_obtained) AS lowest " +
                         "FROM marks";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(statsSql)) {
            if (rs.next()) {
                System.out.println("Total Students: " + totalStudents);
                System.out.println("Total Subjects: " + totalSubjects);
                System.out.println("Marks Records: " + rs.getInt("records"));
                System.out.printf("Overall Average: %.2f%n", rs.getDouble("average"));
                System.out.printf("Highest Marks: %.2f%n", rs.getDouble("highest"));
                System.out.printf("Lowest Marks: %.2f%n", rs.getDouble("lowest"));
            }
        }
        
        // Get grade distribution
        System.out.println("\nGrade Distribution:");
        System.out.println("-------------------");
        
        String gradeSql = "SELECT " +
                         "SUM(CASE WHEN marks_obtained >= 90 THEN 1 ELSE 0 END) AS a_plus, " +
                         "SUM(CASE WHEN marks_obtained >= 85 AND marks_obtained < 90 THEN 1 ELSE 0 END) AS a, " +
                         "SUM(CASE WHEN marks_obtained >= 80 AND marks_obtained < 85 THEN 1 ELSE 0 END) AS a_minus, " +
                         "SUM(CASE WHEN marks_obtained >= 75 AND marks_obtained < 80 THEN 1 ELSE 0 END) AS b_plus, " +
                         "SUM(CASE WHEN marks_obtained >= 70 AND marks_obtained < 75 THEN 1 ELSE 0 END) AS b, " +
                         "SUM(CASE WHEN marks_obtained >= 65 AND marks_obtained < 70 THEN 1 ELSE 0 END) AS b_minus, " +
                         "SUM(CASE WHEN marks_obtained >= 60 AND marks_obtained < 65 THEN 1 ELSE 0 END) AS c_plus, " +
                         "SUM(CASE WHEN marks_obtained >= 55 AND marks_obtained < 60 THEN 1 ELSE 0 END) AS c, " +
                         "SUM(CASE WHEN marks_obtained >= 50 AND marks_obtained < 55 THEN 1 ELSE 0 END) AS c_minus, " +
                         "SUM(CASE WHEN marks_obtained >= 45 AND marks_obtained < 50 THEN 1 ELSE 0 END) AS d_plus, " +
                         "SUM(CASE WHEN marks_obtained >= 40 AND marks_obtained < 45 THEN 1 ELSE 0 END) AS d, " +
                         "SUM(CASE WHEN marks_obtained < 40 THEN 1 ELSE 0 END) AS f " +
                         "FROM marks";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(gradeSql)) {
            if (rs.next()) {
                System.out.printf("A+: %d%n", rs.getInt("a_plus"));
                System.out.printf("A: %d%n", rs.getInt("a"));
                System.out.printf("A-: %d%n", rs.getInt("a_minus"));
                System.out.printf("B+: %d%n", rs.getInt("b_plus"));
                System.out.printf("B: %d%n", rs.getInt("b"));
                System.out.printf("B-: %d%n", rs.getInt("b_minus"));
                System.out.printf("C+: %d%n", rs.getInt("c_plus"));
                System.out.printf("C: %d%n", rs.getInt("c"));
                System.out.printf("C-: %d%n", rs.getInt("c_minus"));
                System.out.printf("D+: %d%n", rs.getInt("d_plus"));
                System.out.printf("D: %d%n", rs.getInt("d"));
                System.out.printf("F: %d%n", rs.getInt("f"));
            }
        }
    }
}
