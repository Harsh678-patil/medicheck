<img width="1093" height="693" alt="image" src="https://github.com/user-attachments/assets/b97f1f2c-0ba7-4a70-affa-cb26477e8d9a" /># 💊 MediCheck — AI Medical Management System

> A production-ready Pharmacy & Clinic Management desktop application built with Java Swing, Maven, and MySQL (via XAMPP).

---

## 📋 Table of Contents

- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Project Structure](#-project-structure)
- [Step 1 — Set Up the Database (XAMPP)](#-step-1--set-up-the-database-xampp)
- [Step 2 — Configure the Application](#-step-2--configure-the-application)
- [Step 3 — Run the Application](#-step-3--run-the-application)
- [Default Login Credentials](#-default-login-credentials)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Features

- 🔐 Role-based login (Admin, Pharmacist, Doctor)
- 💊 Medicine & Inventory Management
- 🧾 Prescription & Billing / Sales
- 👨‍⚕️ Patient & Doctor Management
- 📊 Analytics with Charts (JFreeChart)
- 🤖 Optional AI Medical Assistant (OpenAI GPT)
- 📦 Barcode/QR Code support
- 📄 CSV & Excel Export
- 🔍 Audit Logs

---

## 🛠 Prerequisites

Make sure you have the following installed **before** running the project:

| Tool | Version | Download |
|------|---------|----------|
| **Java JDK** | 11 or higher | https://www.oracle.com/java/technologies/downloads/ |
| **XAMPP** | Any recent | https://www.apachefriends.org/download.html |
| **Maven** *(optional)* | 3.8+ | https://maven.apache.org/download.cgi |

> 💡 **Maven is optional** — the project includes a portable Maven in the `.maven/` folder. The `launch.bat` script uses it automatically.

---

## 📁 Project Structure

```
medicheck/
├── src/
│   └── main/java/com/medicheck/    ← Java source code
│       ├── Main.java               ← Application entry point
│       ├── config/                 ← DB & App configuration
│       ├── dao/                    ← Database access layer
│       ├── model/                  ← Data models/entities
│       ├── service/                ← Business logic
│       └── ui/                     ← Swing UI views
├── sql/
│   ├── schema.sql                  ← Database table definitions
│   ├── sample_data.sql             ← Sample/demo data
│   └── setup_all.sql              ← FULL setup (schema + data combined)
├── app.properties                  ← App settings (DB url, credentials, etc.)
├── pom.xml                         ← Maven dependencies
├── launch.bat                      ← 🚀 ONE-CLICK launcher (uses portable Maven)
└── run_medicheck.bat               ← Launcher (requires system Maven in PATH)
```

---

## 🗄 Step 1 — Set Up the Database (XAMPP)

This project uses **MySQL** running through **XAMPP**. Follow these steps carefully:

### 1.1 — Start XAMPP

1. Open **XAMPP Control Panel** (search for it in Start Menu, or run `C:\xampp\xampp-control.exe`)
2. Click **Start** next to **Apache**
3. Click **Start** next to **MySQL**
4. Both should show a **green** status ✅

### 1.2 — Open phpMyAdmin

1. Open your browser and go to: **http://localhost/phpmyadmin**
2. You should see the phpMyAdmin dashboard

### 1.3 — Import the Database

1. In phpMyAdmin, click **"Import"** in the top navigation bar
2. Click **"Choose File"** and navigate to:
   ```
   C:\Users\ACER\OneDrive\Desktop\medicheck\sql\setup_all.sql
   ```
3. Click **"Go"** at the bottom of the page
4. Wait for the import to complete — you should see a **green success message** ✅
5. In the left panel, you should now see a database called **`medicheck`** with all its tables

> ✅ This single file (`setup_all.sql`) creates the database, all tables, and loads sample data.

---

## ⚙ Step 2 — Configure the Application

Open the file `app.properties` in the project root and verify these settings match your XAMPP setup:

```properties
db.url=jdbc:mysql://localhost:3306/medicheck?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
db.username=root
db.password=
```

> 🔑 By default, XAMPP MySQL uses `root` with **no password** — this is already configured correctly.  
> If you've set a MySQL password in XAMPP, put it after `db.password=`.

---

## 🚀 Step 3 — Run the Application

You have **two options** to run MediCheck:

---

### ✅ Option A — One-Click Launcher (Recommended)

Simply double-click **`launch.bat`** in the project folder.

```
medicheck\launch.bat
```

This script will:
- ✅ Automatically use the bundled portable Maven (`.maven/`)
- ✅ Compile the Java source code
- ✅ Start the application

> ⚠️ On first run, Maven may download dependencies from the internet. This can take **2–5 minutes**.  
> Subsequent runs will be much faster.

---

### ✅ Option B — Run from VS Code

1. Ensure the **Extension Pack for Java** is installed in VS Code
2. Open the project folder in VS Code:
   ```
   File → Open Folder → C:\Users\ACER\OneDrive\Desktop\medicheck
   ```
3. Open `src/main/java/com/medicheck/Main.java`
4. Click the **▶ Run** button that appears above the `main` method
   — OR —  
   Press `F5` to run with debug

---

### ✅ Option C — Run from Terminal (System Maven)

If you have Maven installed and in your system PATH:

```powershell
cd C:\Users\ACER\OneDrive\Desktop\medicheck
mvn clean compile exec:java -Dexec.mainClass="com.medicheck.Main"
```

---

### ✅ Option D — Build a JAR and Run It

To package the app as a standalone executable JAR:

```powershell
cd C:\Users\ACER\OneDrive\Desktop\medicheck
# Using portable Maven:
.maven\bin\mvn.cmd clean package

# Run the built JAR:
java -jar target\medicheck-jar-with-dependencies.jar
```

---

## 🔑 Default Login Credentials

After importing `setup_all.sql`, use these credentials to log in:

| Role | Username | Password |
|------|----------|----------|
| **Admin** | `admin` | `admin123` |
| **Pharmacist** | `pharmacist` | `pharma123` |
| **Doctor** | `doctor` | `doctor123` |

> 🔐 You can change passwords after logging in from the User Management section.

---

## 🤖 Optional — Enable AI Assistant (OpenAI)

To enable the AI Medical Assistant feature:

1. Get an API key from https://platform.openai.com/api-keys
2. Open `app.properties` and update:
   ```properties
   openai.enabled=true
   openai.api.key=sk-your-key-here
   openai.model=gpt-3.5-turbo
   ```
3. Restart the application

---

## 🔧 Troubleshooting

### ❌ "Cannot connect to MySQL database!"

- Make sure **XAMPP is running** and **MySQL is started** (green in XAMPP panel)
- Verify the database `medicheck` exists in phpMyAdmin
- Check `app.properties`: `db.username=root` and `db.password=` (empty)

### ❌ "Port 3306 already in use" in XAMPP

- Another MySQL instance may be running. Open Task Manager and end `mysqld.exe`
- Or change the MySQL port in XAMPP config and update `app.properties` accordingly

### ❌ Java not found / `javac` not recognized

- Install JDK 11+ from https://www.oracle.com/java/technologies/downloads/
- Add Java to your system PATH:  
  `System Properties → Environment Variables → Path → Add C:\Program Files\Java\jdk-XX\bin`

### ❌ Maven build fails

- Delete the `target/` folder and try again
- Check internet connection (first-time dependency download)
- Use `launch.bat` which includes a portable Maven — no system install needed

### ❌ Application opens but shows blank / crashes

- Check `logs/` folder in the project root for error details
- Make sure `setup_all.sql` was **fully imported** without errors

---

## 📦 Key Dependencies

| Library | Purpose |
|---------|---------|
| `mysql-connector-java 8.0.33` | MySQL JDBC driver |
| `flatlaf 3.4.1` | Modern dark Swing theme |
| `jfreechart 1.5.4` | Analytics charts |
| `zxing 3.5.3` | Barcode/QR support |
| `webcam-capture 0.3.12` | Live barcode scanning |
| `poi-ooxml 5.2.5` | Excel export |
| `commons-csv 1.10.0` | CSV export |
| `jbcrypt 0.4` | Password hashing |
| `logback-classic 1.4.14` | Logging |

---

*MediCheck v1.0.0 — Built with ❤️ using Java 11, Maven, and MySQL*
Screenshots Of MediCheck :



#screenshots

