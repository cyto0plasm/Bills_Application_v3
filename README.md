# 🧾 Bills — Client & Invoice Management System

A desktop application built with **Java Swing** and **SQLite**, designed to replace spreadsheets for managing clients, invoices, and financial tracking — all in one clean tabular interface.

---

## 🛠 Tech Stack

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Java_Swing-007396?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)

---

## 🔑 Core Features

- **Client Management** — Add, update, and manage full client profiles
- **Invoice Management** — Create and track invoices per client with full detail view
- **Invoice Details** — Break down each invoice into line items and calculations
- **Payment Tracking** — Record payments and update them at any time
- **Financial Overview** — Monitor money in/out with real-time calculations
- **Tabular Interface** — Clean table-based UI — no more Excel sheets

---

## 💡 Why Bills?

| Before | After |
|---|---|
| Scattered Excel sheets | Centralized database |
| Manual calculations | Automatic deep calculations |
| No payment history | Full payment lifecycle tracking |
| Hard to query clients | Instant tabular view and search|

---

## 🗃 Data Model

```
Clients
  └── Invoices
        ├── Invoice Details (line items)
        └── Payments (updatable)
```

---

## 📁 Project Structure

```
Bills/
├── models/
│   ├── Client
│   ├── Invoice
│   ├── InvoiceDetail
│   └── Payment
├── views/
│   ├── ClientsPanel
│   ├── InvoicesPanel
│   └── PaymentsPanel
├── controllers/
└── db/
    └── DatabaseManager
```

---

