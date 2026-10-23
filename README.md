# SpendWise — Personal Expense Tracker

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Render-46E3B7?style=for-the-badge&logo=render&logoColor=white)](https://spendwise-75bx.onrender.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Render-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

🔗 **Live Application URL:** [https://spendwise-75bx.onrender.com](https://spendwise-75bx.onrender.com)

SpendWise is a clean, modern, fully functional multi-user personal expense tracker built with **Java 17**, **Spring Boot 3**, **Spring Security**, and responsive vanilla **HTML5 / CSS3 / JavaScript**.

Designed with a calm, professional aesthetic, living dynamic background effects, multi-currency support, robust user isolation, and rich visual analytics.

---

## 🌟 Key Features

### 🔐 1. Spring Security & Multi-User Isolation
- **Secure Authentication**: Session-based authentication with BCrypt password hashing.
- **Complete User Data Isolation**: Every registered user gets their own private expense database, summaries, and charts. One user cannot view or mutate another user's expenses.
- **Unified Sign In / Create Account Card**:
  - Sleek card with buttery-smooth height morphing and directional sliding transitions between *Sign In* and *Create Account*.
  - Interactive single-eye password toggle with clean instant reveal/hide.
  - Automatically redirects authenticated users straight to their dashboard.

### 🎨 2. Ambient Living Background & Modern Theme System
- **Living Ambient Dynamic Effect**: Subtle, floating ambient glow orbs with interactive cursor spotlight across both dark and light modes.
- **Oceanic Midnight Dark Mode**: Rich oceanic navy palette (`#0A0E1A`, `#111C38`, `#162447`) with high-contrast glowing electric lime accents (`#A3E635`).
- **Soothing Slate Light Mode**: Soft slate background (`#F1F5F9`, `#E2E8F0`) eliminating harsh pure white glare while maintaining high contrast.
- **Instant Theme Toggle**: Single-click sun/moon toggle with automatic system theme detection and persistent `localStorage` preference.

### 📊 3. Analytics & Elevated Dashboard
- **Modern Metric Cards**: Elevated cards with custom SVG icon badges for:
  - **Today's Total** (with yesterday comparison)
  - **This Month's Spending** (with previous month trend)
  - **Average per transaction** & total recorded count
  - **Top Spending Category** & monthly consumption
- **Visual Analytics with Real Data**:
  - **Category Breakdown (Donut Chart)**: Percentage and absolute consumption per category with color-coded badges.
  - **Daily Spending (14-Day Trend Bar Chart)**: Optimal 2-week view of day-to-day spending patterns and weekend spikes.

### 🌍 4. Multi-Currency Support
- Live currency selector supporting **INR (`₹`)**, **USD (`$`)**, **EUR (`€`)**, **GBP (`£`)**, **JPY (`¥`)**, **CAD (`CA$`)**, **AUD (`A$`)**, **AED (`AED`)**, **SGD (`SG$`)**, and **CHF (`CHF`)**.
- Localized number formatting (including Indian numbering commas for INR e.g. `₹1,25,000.00`).
- Persistent user preference across sessions.

### ⚡ 5. Complete Expense Management (CRUD & Filters)
- **Add & Edit**: Intuitive modal with category, amount, date, payment method (UPI, Cash, Debit, Credit, Net Banking), and notes.
- **Safe Delete**: Confirmation modal with instant metric recalculation.
- **Instant Search & Presets**: Filter by text, category, or quick date ranges (*Today*, *This Week*, *This Month*, *Last Month*, *Custom*).
- **Sorting**: Date, Amount, Title ascending/descending.
- **Export to CSV**: Download filtered expense transactions directly for Excel or Google Sheets.

### 🏷️ 6. Official Brand Favicon Suite
- Full favicon suite featuring the SpendWise lime badge with dark dollar symbol:
  - Vector SVG (`favicon.svg`) for razor-sharp rendering on all modern screens.
  - Multi-resolution Windows ICO (`favicon.ico`).
  - Standard PNGs (`favicon-32x32.png`, `favicon-16x16.png`).
  - Apple Touch Icon (`apple-touch-icon.png`) for mobile shortcuts.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.5 (Spring Web, Spring Data JPA, Spring Security, Bean Validation) |
| **Security** | Spring Security with BCrypt Password Encoding & Session Management |
| **Database** | Embedded file-based H2 (persists in `./data/spendwisedb`), ready for PostgreSQL/MySQL in production |
| **Frontend** | Semantic HTML5, CSS3 Variables & Animations, Vanilla JavaScript (ES6+), Chart.js |
| **Build & Deploy** | Maven Wrapper (`mvnw`), Multi-stage Dockerfile |

---

## 🚀 Quick Start (Running Locally)

### Prerequisites
- **Java 17+** installed (`java -version`)

You do **not** need Maven pre-installed; the included Maven wrapper (`./mvnw` or `mvnw.cmd`) handles everything automatically.

### 1. Clone & Navigate
```bash
git clone https://github.com/Anilsoni27/SpendWise-Tracker.git
cd SpendWise-Tracker
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
- Create an account or sign in to access your private dashboard.

---

## 📡 REST API Reference

### Authentication Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user (`fullName`, `email`, `password`) |
| `POST` | `/api/auth/login` | Authenticate user and initiate session |
| `GET` | `/api/auth/me` | Fetch currently logged-in user profile |
| `POST` | `/api/auth/logout` | Terminate session and log out |

### Expense Endpoints (User-Scoped)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/expenses` | List user's expenses (supports `search`, `category`, `startDate`, `endDate`, `sortBy`, `sortDir`) |
| `GET` | `/api/expenses/{id}` | Get single expense details |
| `POST` | `/api/expenses` | Create a new expense |
| `PUT` | `/api/expenses/{id}` | Update existing expense |
| `DELETE` | `/api/expenses/{id}` | Delete expense |
| `GET` | `/api/expenses/summary` | Today, month, and all-time spending totals |
| `GET` | `/api/expenses/categories` | List of all expense categories and display colors |
| `GET` | `/api/expenses/charts/categories` | Category spending breakdown for doughnut chart |
| `GET` | `/api/expenses/charts/daily?days=14` | Daily spending sequence for 14-day bar chart |
| `GET` | `/api/expenses/export` | Download filtered expenses as CSV file |
| `POST` | `/api/expenses/reset-demo` | Reset current user's expenses to sample data |

---

## 🧪 Running Automated Tests

```bash
# Windows
mvnw.cmd test

# Linux / macOS
./mvnw test
```

---

## ☁️ Cloud & Production Deployment

### Option 1: Docker
Build and run the container locally or on any cloud server:
```bash
docker build -t spendwise:1.0 .
docker run -p 8080:8080 spendwise:1.0
```

### Option 2: Deploy to Render (Recommended)

1. Push this repository to GitHub.
2. In your [Render Dashboard](https://dashboard.render.com/), click **New +** > **Web Service**.
3. Connect your GitHub repository `SpendWise-Tracker`.
4. **Runtime**: Select **Docker** (Recommended — Render will automatically build using the existing multi-stage `Dockerfile`, which activates `SPRING_PROFILES_ACTIVE=prod`).
5. **Environment Variables**: Add the following required PostgreSQL connection variables under the **Environment** tab:
   - `SPRING_DATASOURCE_URL` — e.g. `jdbc:postgresql://<host>:5432/<dbname>`
     > **Note for Neon / Supabase**: Append `?sslmode=require` to the JDBC URL if SSL is enforced:  
     > `jdbc:postgresql://<host>:5432/<dbname>?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME` — Your PostgreSQL database username
   - `SPRING_DATASOURCE_PASSWORD` — Your PostgreSQL database password
6. Click **Deploy Web Service**.
   > **Note on Free Instances**: Render free instances automatically spin down (sleep) after periods of inactivity. The first request after a period of inactivity may take 30–50 seconds while the container boots up.

---

## 📄 License
Open-source and beginner-friendly under the MIT License.
