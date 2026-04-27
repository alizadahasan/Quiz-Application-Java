# Online Quiz System

A Java-based desktop application for creating, managing, and taking quizzes. The system supports both admin and user roles, features a JavaFX interface, and uses SQLite for data storage.

## 🚀 Features

* **User Authentication** (Admin & User roles)
* **Quiz Creation** with customizable questions & time limits
* **Quiz Taking** with real-time countdown timer
* **Score & Result Tracking**
* **JavaFX UI** for a clean and interactive experience
* **SQLite Database** for persistent data storage

---

## ▶️ Running the Application

### **Prerequisites**

* **JDK 22+**
* **Apache Maven 3.8+**
* **JavaFX-supported IDE** (e.g., IntelliJ IDEA) or command line

### **Setup & Run**

1. Download and unzip the project.
2. Ensure the project contains `pom.xml` and the `src/main/java` directory.
3. Open a terminal in the project root and run:

```bash
mvn clean compile javafx:run
```

4. The app will initialize `quiz_system.db` and open the login screen.

### **Default Login**

* **Admin:** `admin` / `admin123`
* **User:** Any user account stored in the database

---

## ✅ Testing

Run the test suite with:

```bash
mvn test
```

The application creates the SQLite schema on startup. The same schema is available in `init.sql` for manual setup or inspection.

---

## 🛠 Technologies & Dependencies

* **Java 22**
* **JavaFX 24.0.1**
* **SQLite (sqlite-jdbc)**
* **Maven**
* **JUnit 5**

Key dependencies (from `pom.xml`):

* `org.openjfx:javafx-controls:24.0.1`
* `org.openjfx:javafx-fxml:24.0.1`
* `org.xerial:sqlite-jdbc:3.49.1.0`
* `org.junit.jupiter:junit-jupiter:5.11.3`

---

## 📂 Project Structure

```
src/main/java/com/quizsystem/
 ├── dao/       # Database access layer
 ├── model/     # Data models (User, Quiz, Question, Result)
 ├── service/   # Business logic
 ├── ui/        # JavaFX controllers and main app
 └── util/      # Helper utilities (e.g., DB connection)

src/main/resources/
 └── FXML layouts & CSS styles

pom.xml         # Maven configuration
```

---

## 📘 Documentation

Generate Javadoc using:

```bash
mvn javadoc:javadoc
```

Output is located at:

```
target/site/apidocs
```

---
