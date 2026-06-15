# 📘 MicroLend – Backend Documentation (Short)

## 📌 Overview
MicroLend is a **Microfinance Loan Management System backend** built using Spring Boot.

It manages the **complete loan lifecycle**:
- Borrower registration  
- KYC verification  
- Loan application & approval  
- Loan disbursement
- EMI collection
- Delinquency tracking
- Reporting & notifications

---

## 🧰 Tech Stack
- **Backend:** Spring Boot  
- **Language:** Java  
- **Database:** MySQL / H2  
- **Security:** Spring Security + JWT  
- **Build Tool:** Maven  
- **API Docs:** Swagger (OpenAPI)

---

## 🏗️ Architecture

```
Client → Controller → Service → Repository → Database
```

Extras:
- JWT Authentication  
- Role-based access  
- Scheduler for delinquency  
- Global exception handling

---

## 🔐 User Roles
- ADMIN  
- BRANCH MANAGER  
- CREDIT OFFICER  
- FIELD OFFICER  
- COLLECTIONS OFFICER  
- BORROWER

---

## 🧩 Core Modules

### 1. Auth & User
- Login & JWT token generation  
- User roles & permissions  

### 2. Borrower
- Add & manage borrowers  
- Auto-create login account  

### 3. KYC
- Upload documents  
- Verify / Reject  

### 4. Group & Centre
- Borrower groups (JLG)  
- Village centres  
- Weekly meetings  

### 5. Loan Product
- Define loan schemes  
- Interest, tenure, limits  

### 6. Loan Application
Flow:
```
DRAFT → SUBMITTED → UNDER REVIEW → APPROVED / REJECTED
```

### 7. Credit Assessment
- Credit score  
- Eligibility check  

### 8. Sanction Letter
- Loan approval details  
- Must be accepted before disbursement  

### 9. Loan Account
- Created after disbursement  
- Generates EMI schedule  

### 10. Repayment & Collection
- EMI payments (Full / Partial)  
- Loan closure handling  

### 11. Delinquency
- Tracks overdue loans  
- Uses DPD (Days Past Due)  
- Auto-created by scheduler  

### 12. Reports
- Loan performance  
- NPA & outstanding  

### 13. Notifications
- Alerts & reminders  
- Read / unread tracking  

### 14. Audit Logs
- Tracks system & user actions  

---

## 🔁 Loan Flow

```
Borrower → KYC → Loan Application
→ Credit Check → Approval → Sanction
→ Disbursement → EMI → Collection
→ Delinquency (if missed)
```

---

## ⚙️ Scheduler
- Runs daily  
- Detects overdue EMIs  
- Updates loan status  
- Creates delinquency cases

---

## 📊 Database Design
- ~17 entities / tables  
- Uses JPA (Hibernate)  

Main tables:
- users  
- borrowers  
- loan_applications  
- loan_accounts  
- repayment_schedule  
- collections  
- delinquency_cases  

---

## 🔢 EMI Calculation

**Flat Interest**
```
EMI = (Principal + Interest) / Tenure
```

**Reducing Interest**
```
EMI = P × r × (1+r)^n / ((1+r)^n − 1)
```

---

## 🚀 Run Project

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

---

## 🌐 Access

- Base URL → `http://localhost:8082`
- Swagger UI → `http://localhost:8082/swagger-ui.html`

---

## 🔑 Default Login

```json
{
  "email": "admin@microlend.com",
  "password": "admin123"
}
```

---

## ✅ Key Highlights

- Secure (JWT + RBAC)  
- Real business workflow  
- Automated delinquency system  
- Structured layered architecture  
- Financial logic (EMI, DPD)  

---

## 🎯 Quick Revision

```
User → Borrower → Loan → EMI → Payment → Default → Report
```
