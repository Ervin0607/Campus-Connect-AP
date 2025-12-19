# Campus Connect ERP System

A comprehensive, role-based Enterprise Resource Planning (ERP) system designed for academic institutions. Campus Connect streamlines course registration, grading, announcements, and administrative management through a modern, secure Java Swing interface.

---

## 🚀 Key Features

### 🔐 Role-Based Access Control (RBAC)
* **Secure Authentication:** Passwords hashed using BCrypt; strict role separation.
* **Three Distinct Portals:**
    * **Student:** View catalog, register/drop courses, view grades, download transcripts.
    * **Instructor:** Manage assigned sections, post announcements, grading with dynamic components (JSON), import/export grade sheets.
    * **Admin:** User management, course catalog maintenance, system settings (Maintenance Mode).

### 📚 Academic Management
* **Dynamic Course Scheduling:** Courses are instantiated as "Offerings" per semester with flexible JSON-based scheduling.
* **Custom Grading Logic:** Instructors can define variable grading components (e.g., Quiz 20%, Final 80%) per course section.
* **Smart Registration:** Real-time capacity tracking prevents over-enrollment; logic handles pre-requisites and term limits.

### 🔔 Advanced Functionality
* **Persistent Notifications:** Real-time alerts for grades and announcements stored efficiently in the database.
* **CSV Integration:** Bulk import/export functionality for grading to support offline workflows.
* **Maintenance Mode:** Global system lock capability for administrators to perform safe updates.
* **Modern UI:** Built with **FlatLaf** for a professional, high-contrast dark theme.

---

## 🛠️ Tech Stack

* **Language:** Java (JDK 21)
* **Frontend:** Java Swing, FlatLaf (Dark Theme), MigLayout
* **Backend Logic:** Java-based Controllers/Handlers
* **Database:** MySQL (Relational + JSON Columns)
* **Serialization:** Jackson Databind (JSON parsing)
* **Security:** BCrypt (Password Hashing)

---

## ⚙️ Installation & Setup

### Prerequisites
* **Java JDK 21** or higher
* **MySQL Server 8.0** or higher
* **IDE:** IntelliJ IDEA (Recommended), Eclipse, or VS Code

### 1. Database Initialization
1.  Open MySQL Workbench or your preferred SQL client.
2.  Create a new schema named `erp_db`.
3.  Import the initialization script provided in `/database/schema.sql`:
    ```bash
    mysql -u root -p erp_db < database/schema.sql
    ```

### 2. Configure Connection
Navigate to `src/Backend/util/DB.java` and update the database credentials:

```java
// src/Backend/util/DB.java
private static final String URL = "jdbc:mysql://localhost:3306/erp_db";
private static final String USER = "root";       // Your MySQL Username
private static final String PASSWORD = "password"; // Your MySQL Password
