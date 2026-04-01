# 📈 Portfolio Tracker

A desktop application for managing and tracking personal investment portfolios in stocks and cryptocurrencies, built with Java and JavaFX.

## 📋 Overview

Portfolio Tracker is a JavaFX desktop application that allows users to manage their investments, track real-time prices, and monitor their portfolio performance. The application supports multiple users and persists all data locally using JSON file storage.

## ✨ Features

- 🔐 **User Authentication** — Register and login with secure password hashing (SHA-256)
- 📊 **Dashboard** — Overview of total portfolio value, profit/loss, and invested amount
- 💰 **Transactions** — Add buy/sell transactions with live market prices
- 📁 **Holdings** — View current asset positions with real-time P/L calculations
- 👁️ **Watchlist** — Track assets of interest with live price updates
- 💾 **Data Persistence** — All data stored locally in JSON files
- 🌐 **Live Market Data** — Real-time prices via CoinGecko API with caching

## 🏗️ Architecture

The application follows a **5-layer architecture**:
```
UI Layer (JavaFX Controllers)
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (Data Access)
        ↓
Storage Layer (File I/O)
        ↓
JSON Files (data/)
```

### Packages

| Package | Description |
|---------|-------------|
| `app` | Application entry point |
| `controller` | JavaFX controllers for each screen |
| `service` | Business logic (Auth, Portfolio, Transactions, etc.) |
| `repository` | Data access layer |
| `storage` | JSON file I/O operations |
| `model` | Domain entities (User, Transaction, Holding, etc.) |
| `util` | Utility classes (Password hashing, Validation, etc.) |
| `exception` | Custom exception classes |
| `integration` | External API clients (CoinGecko) |
| `config` | Application configuration |

## 🛠️ Technologies

| Technology | Purpose |
|------------|---------|
| Java 21 | Core programming language |
| JavaFX 21 | Desktop UI framework |
| Gson 2.10.1 | JSON serialization/deserialization |
| CoinGecko API | Live cryptocurrency market data |
| Maven | Build and dependency management |

## 📦 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Internet connection (for live market data)

## 🚀 Getting Started

**1. Clone the repository:**
```bash
git clone https://github.com/KostasMezani/Portfolio-Tracker.git
cd Portfolio-Tracker
```

**2. Build the project:**
```bash
mvn clean install
```

**3. Run the application:**
```bash
mvn javafx:run
```

## 📁 Project Structure
```
Portfolio-Tracker/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/portfoliotracker/
│       │       ├── app/
│       │       ├── config/
│       │       ├── controller/
│       │       ├── exception/
│       │       ├── integration/
│       │       ├── model/
│       │       ├── repository/
│       │       ├── service/
│       │       ├── storage/
│       │       └── util/
│       └── resources/
│           └── style.css
├── data/                    ← Created automatically on first Register/Transaction
│   ├── users.json
│   ├── transactions.json
│   ├── watchlist.json
│   └── alerts.json
└── pom.xml
```

## 🖥️ Screens

| Screen | Description |
|--------|-------------|
| Login | User authentication |
| Register | New user registration |
| Dashboard | Portfolio overview with summary cards |
| Transactions | Transaction history with search and filter |
| Add Transaction | Create buy/sell transactions |
| Holdings | Current asset positions |
| Watchlist | Track assets of interest |

## 🔒 Security

- Passwords are hashed using **SHA-256** before storage
- No plain-text passwords are ever stored
- Each user's data is isolated by `userId`

## 📊 Key Calculations

- **Total Portfolio Value** = Sum of (quantity × current price) for all holdings
- **Total Invested** = Sum of BUY amounts - Sum of SELL amounts  
- **Profit/Loss** = (Current Price - Avg Buy Price) × Quantity
- **Average Buy Price** = Total BUY Amount / Total BUY Quantity

## 👨‍💻 Author

**Kostas Mezanis**  
Computer Science Student — Advanced Programming Assignment (CN5004)  
Metropolitan College — 2025/26

## 📄 License

This project was developed as an academic assignment for the CN5004 Advanced Programming course.
