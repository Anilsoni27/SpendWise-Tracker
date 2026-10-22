# SpendWise — Personal Expense Tracker

SpendWise is a clean, lightweight, fully functional personal expense tracker built with **Java 17**, **Spring Boot 3**, and modern vanilla **HTML5 / CSS3 / JavaScript**.

Designed with a calm, natural, and professional interface — no flashy AI-style neon gradients or floating card clutter. It feels like a real, thoughtfully crafted financial tool.

---

## Key Features

- **Dashboard Spending Summaries**:
  - Live calculations for **Today's Total** (with yesterday comparison)
  - **This Month's Spending** (with previous month comparison)
  - **Average per transaction** & total count of recorded expenses
  - **Top Spending Category** and current month consumption
- **Professional Light & Dark Mode (60-30-10 Palette Rule)**:
  - **Light Mode**: 60% Canvas (`#F8FAFC`/`#FFFFFF`), 30% Structure & Text (`#0F172A`), 10% Interactive Accent (`#A3E635` Electric Lime Green).
  - **Dark Mode**: 60% Canvas (`#0B0F19`), 30% Containers (`#161B22`), 10% Glowing Accent (`#A3E635`).
  - Seamless one-click header toggle with automatic system theme detection and persistent `localStorage` preference.
  - Theme-aware Chart.js graphs (Electric Lime bars in Dark Mode, dark slate in Light Mode).
- **Multi-Currency Support & User Preference**:
  - Live currency switcher supporting **INR (`₹`)**, **USD (`$`)**, **EUR (`€`)**, **GBP (`£`)**, **JPY (`¥`)**, **CAD (`CA$`)**, **AUD (`A$`)**, **AED (`AED`)**, **SGD (`SG$`)**, and **CHF (`CHF`)**.
  - Automatically formats numbers with localized grouping (e.g. Indian numbering commas for INR `₹1,25,000.00`).
  - Saved in browser `localStorage`, retaining user preference across visits.
  - Dynamically updates summary cards, charts, transaction table, input modal, and delete dialogs instantly.
- **Visual Analytics with Real Database Data**:
  - **Category-wise Spending (Pie/Donut Chart)** with accessible custom legend, amounts, and percentage breakdown
  - **Daily Spending (14-Day Bar Chart)** showing day-to-day spending patterns directly from database queries
- **Complete Expense Management (CRUD)**:
  - **Add Expense**: Title, amount, category, date, payment method, and optional notes
  - **Edit Expense**: Pre-filled modal to update any transaction
  - **Delete Expense**: Safe confirmation dialog with immediate recalculation
- **Fast Search & Filtering**:
  - Live instant text search (matches title, notes, or payment method)
  - Category dropdown filter
  - Quick date presets: *Today*, *This Week*, *This Month*, *Last Month*, or *Custom Date Range*
  - Sorting: *Date (Newest/Oldest)*, *Amount (High to Low / Low to High)*, *Title (A-Z)*
  - Live filtered total sum and record count
- **Data Export**:
  - One-click export to **CSV** format for Excel or Google Sheets
- **Demo Mode**:
  - Pre-seeds realistic sample expenses on first startup, with a one-click "Reset Sample Data" button for testing

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.5 (Spring Web, Spring Data JPA, Bean Validation) |
| **Database** | Embedded file-based H2 (persists in `./data/spendwisedb`), with ready support for PostgreSQL/MySQL |
| **Frontend** | HTML5, Clean CSS3 (Responsive Grid/Flexbox), Vanilla JavaScript (ES6+), Chart.js |
| **Build & Deploy** | Maven Wrapper (`mvnw`), Multi-stage Dockerfile |

---

## Quick Start (Running Locally)

### Prerequisites
- **Java 17+** installed (`java -version`)

You do **not** need Maven pre-installed; the included Maven wrapper (`./mvnw` or `mvnw.cmd`) handles everything automatically.

### 1. Clone & Navigate
```bash
cd Project1
```

### 2. Run the Application

On **Windows**:
```cmd
mvnw.cmd spring-boot:run
```

On **macOS / Linux**:
```bash
chmod +x ./mvnw
./mvnw spring-boot:run
```

### 3. Open in Browser
Open [http://localhost:8080](http://localhost:8080) in your web browser.

SpendWise will start, automatically seed realistic initial expenses, and display the dashboard.

---

## Embedded Database & H2 Console

SpendWise saves data directly to a local file at `./data/spendwisedb.mv.db`, meaning **your expenses persist across app restarts**.

To inspect the database tables directly:
1. Navigate to: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
2. Enter the JDBC details:
   - **JDBC URL**: `jdbc:h2:file:./data/spendwisedb;DB_CLOSE_DELAY=-1`
   - **User Name**: `sa`
   - **Password**: *(leave blank)*
3. Click **Connect** to query the `expenses` table.

---

## Running Automated Tests

To run the full suite of unit and integration tests:

```bash
# Windows
mvnw.cmd test

# Linux / macOS
./mvnw test
```

---

## REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/expenses` | List expenses (supports `search`, `category`, `startDate`, `endDate`, `sortBy`, `sortDir`) |
| `GET` | `/api/expenses/{id}` | Get single expense details |
| `POST` | `/api/expenses` | Create a new expense |
| `PUT` | `/api/expenses/{id}` | Update existing expense |
| `DELETE` | `/api/expenses/{id}` | Delete expense |
| `GET` | `/api/expenses/summary` | Today, month, and all-time spending totals |
| `GET` | `/api/expenses/categories` | List of all expense categories and display colors |
| `GET` | `/api/expenses/charts/categories` | Category spending breakdown for pie chart |
| `GET` | `/api/expenses/charts/daily?days=14` | Daily spending sequence for bar chart |
| `GET` | `/api/expenses/export` | Download filtered expenses as CSV file |
| `POST` | `/api/expenses/reset-demo` | Reset database to sample expenses |

---

## Preparing for Online Cloud Deployment

SpendWise is configured for cloud deployment. The server automatically respects the `$PORT` environment variable provided by hosting platforms.

### Option 1: Docker
Build and run the container locally or on any cloud server:
```bash
docker build -t spendwise:1.0 .
docker run -p 8080:8080 spendwise:1.0
```

### Option 2: Deploy to Render / Railway / Fly.io
1. Push this repository to GitHub.
2. Link your repository in **Render** or **Railway**.
3. Choose **Dockerfile** or Java Maven environment.
4. Set the build command to `./mvnw clean package -DskipTests` and start command to `java -jar target/spendwise-1.0.0.jar`.
5. *(Optional)* For production PostgreSQL, set the standard environment variable:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<dbname>
   SPRING_DATASOURCE_USERNAME=<user>
   SPRING_DATASOURCE_PASSWORD=<password>
   SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
   ```

### Option 3: Standalone Executable JAR
Build the standalone JAR:
```bash
mvnw.cmd clean package
```
Run the generated JAR on any machine with Java 17+:
```bash
java -jar target/spendwise-1.0.0.jar
```

---

## License
Open-source and beginner-friendly under the MIT License.
