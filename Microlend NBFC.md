# MicroLend — Modules & Workflows

Table of Contents
- [Introduction](#introduction)
- [Actors](#actors)
- [Architecture Overview](#architecture-overview)
- [Module Summaries](#module-summaries)
  - [1. Identity & Access Management (IAM)](#1-identity--access-management-iam)
  - [2. Borrower Onboarding & KYC](#2-borrower-onboarding--kyc)
  - [3. Group & Centre Management](#3-group--centre-management)
  - [4. Loan Origination & Approval](#4-loan-origination--approval)
  - [5. Loan Disbursement & Account Management](#5-loan-disbursement--account-management)
  - [6. Repayment Collection & Delinquency Management](#6-repayment-collection--delinquency-management)
  - [7. Portfolio Analytics & Reporting](#7-portfolio-analytics--reporting)
  - [8. Notifications & Alerts](#8-notifications--alerts)
- [Cross-Module Data Flows (End-to-End)](#cross-module-data-flows-end-to-end)
- [Deployment & Integration Notes](#deployment--integration-notes)
- [Appendix: Suggested REST endpoints](#appendix-suggested-rest-endpoints)

---

## Introduction

MicroLend is a microfinance and NBFC loan management backend. This document maps each module to workflows and indicates how modules interact in common scenarios (borrower onboarding, loan origination, disbursement, collections and delinquency handling).

## Actors

- Borrower
- Field Officer
- Credit Officer
- Branch Manager
- Collections Officer
- NBFC Admin

## Architecture Overview

- Frontend: React
- Backend: REST API (Spring Boot in this repo)
- Database: Relational (MySQL / PostgreSQL / SQL Server)
- API docs: `springdoc-openapi` (Swagger UI)

---

## Module Summaries

### 1. Identity & Access Management (IAM)

Purpose
- Provide authentication, authorization (RBAC), branch-scoped sessions and audit trails.

Key entities
- `User` (UserID, Name, Role, Email, Phone, BranchID, Status)
- `AuditLog` (AuditID, UserID, Action, Module, Timestamp)

Core workflow
1. User registration (Admin registers staff / borrowers can self-register).
2. Authentication (login) issues JWTs or session tokens.
3. Authorization gate uses role claims to allow access to endpoints.
4. Every security-sensitive action writes an `AuditLog` entry with module and action.

Interactions
- All modules rely on IAM for authorizing operations and for audit logging.

Security notes
- Use short-lived access tokens + refresh tokens; store JWT signing secret in secure config or secrets manager.
- Log authentication/authorization failures in `AuditLog`.

Suggested endpoints (examples)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/{id}`
- `PUT /api/users/{id}/roles`

### 2. Borrower Onboarding & KYC

Purpose
- Capture borrower profiles, KYC documents and internal credit assessments.

Key entities
- `Borrower` and `BorrowerKYC`, `CreditAssessment`.

Core workflow
1. Borrower or Field Officer creates a `Borrower` profile (basic demographic and contact data).
2. Upload KYC documents linked to a `BorrowerKYC` record; status starts as `Pending`.
3. KYC verification (manual or automated): verifier updates `BorrowerKYC.Status` to `Verified` or `Rejected`.
4. Credit Officer or automated rules run internal credit assessment and create `CreditAssessment` with `InternalCreditScore` and recommendation.
5. If `CreditAssessment.Recommendation` is `Eligible` or `Conditional`, borrower can apply for a loan.

Interactions
- Sends verified borrower data to Loan Origination.
- IAM controls who may upload/verify documents; Audit logs all KYC decisions.

Suggested endpoints
- `POST /api/borrowers`
- `GET /api/borrowers/{id}`
- `POST /api/borrowers/{id}/kyc`
- `PUT /api/borrowers/{id}/kyc/{kycId}/verify`

Notes
- Store only hashed identifiers where possible and protect PII with encryption at rest.

### 3. Group & Centre Management

Purpose
- Manage group-based lending constructs and centre meeting schedules.

Key entities
- `BorrowerGroup`, `Centre`, `CentreMeeting`.

Core workflow
1. Field Officer creates a `BorrowerGroup` and assigns borrower members.
2. Group is linked to a `Centre` and a `FieldOfficerID` is assigned.
3. Centre meeting schedules are created (weekly/monthly). `CentreMeeting` entries created for each scheduled date.
4. At each meeting, attendance is recorded and a `CollectionAmount` summary is stored.

Interactions
- Loan applications for group lending attach `GroupID`.
- Collections recorded at the centre map to loan schedules in `RepaymentSchedule` (module 6).

Suggested endpoints
- `POST /api/centres`
- `POST /api/centres/{id}/meetings`
- `POST /api/groups`
- `POST /api/groups/{id}/members`

Notes
- Support joint-liability flag (`JointLiabilityEnabled`) and member-level liability shared proportions.

### 4. Loan Origination & Approval

Purpose
- Accept loan applications, enforce credit policy rules, run approval workflows and produce sanction letters.

Key entities
- `LoanApplication`, `LoanProduct`, `SanctionLetter`.

Core workflow
1. Borrower (or FO on behalf) submits `LoanApplication` with `RequestedAmount`, `LoanProductID`.
2. System validates borrower eligibility (check `Borrower.Status`, `CreditAssessment`, blacklists) and product constraints (min/max amount, tenure).
3. Application moves to `UnderReview` and assigned to a `CreditOfficer`.
4. CreditOfficer performs manual review (additional checks, field visit notes) and either `Approve` or `Reject`.
5. On approval, generate a `SanctionLetter` with sanctioned terms. Borrower must accept (digital acceptance) to proceed to disbursement.

Interactions
- Relies on Borrower module for verified borrower data.
- On approval, triggers creation of `LoanAccount` in module 5 and sends notifications via module 8.

Suggested endpoints
- `POST /api/loan-applications`
- `GET /api/loan-applications/{id}`
- `PUT /api/loan-applications/{id}/approve`
- `POST /api/loan-applications/{id}/sanction/accept`

Notes
- Implement multi-level approvals if `RequestedAmount` crosses thresholds.

### 5. Loan Disbursement & Account Management

Purpose
- Create loan accounts after sanction, manage disbursement tranches, generate repayment schedules and maintain outstanding balances.

Key entities
- `LoanAccount`, `RepaymentSchedule`.

Core workflow
1. On `SanctionLetter.AcceptedByBorrower`, create a `LoanAccount` record and compute `TotalInterest` and `TotalRepayable`.
2. Generate `RepaymentSchedule` for each installment with `DueDate`, `PrincipalDue`, `InterestDue`.
3. Disburse funds (manual ledger entry or external bank API later). Update `DisbursedAmount` and `DisbursementDate`.
4. Track `OutstandingPrincipal` per account and update after each collection.

Interactions
- Sends disbursement confirmation notifications (module 8).
- Collections (module 6) post payments against `RepaymentSchedule` entries.

Suggested endpoints
- `POST /api/loan-accounts` (internal / triggered)
- `GET /api/loan-accounts/{id}`
- `GET /api/loan-accounts/{id}/schedule`

Notes
- Treat disbursements as ledger entries with idempotency keys to avoid duplicate postings.

### 6. Repayment Collection & Delinquency Management

Purpose
- Record collections, update schedules, track overdue accounts, and manage delinquency workflows.

Key entities
- `CollectionRecord`, `DelinquencyCase`.

Core workflow
1. Collections officer or Field Officer records a `CollectionRecord` (links to `LoanAccountID` and `ScheduleID`). Payment mode recorded (cash/bank/centre).
2. On payment, update `RepaymentSchedule.Status` to `Paid` (or `Partial`) and decrement `LoanAccount.OutstandingPrincipal`.
3. A background job identifies overdue schedules (DPD > 0) and creates or updates `DelinquencyCase` with `PARBucket`.
4. Collections Officer executes recovery actions (field visits, restructuring). Update `DelinquencyCase.Action` and `Status` as progress is made.
5. If resolved, mark `DelinquencyCase.Status=Resolved` and update account state; in severe cases, mark as `WrittenOff`.

Interactions
- Triggers notifications for borrower and field officer on missed payments (module 8).
- Delinquency metrics feed into Portfolio Analytics (module 7).

Suggested endpoints
- `POST /api/collections`
- `GET /api/loan-accounts/{id}/collections`
- `GET /api/delinquency/cases`
- `PUT /api/delinquency/cases/{id}/action`

Notes
- Ensure strong audit logging for collection amounts and adjustments. Provide idempotency keys for collection submission.

### 7. Portfolio Analytics & Reporting

Purpose
- Provide dashboards and reports (PAR, disbursements, collection efficiency, field officer productivity).

Key entities
- `PortfolioReport` (ReportID, Scope, Metrics, GeneratedDate)

Core workflow
1. Periodic aggregator jobs compute metrics from transactional tables (LoanAccount, RepaymentSchedule, CollectionRecord) and generate `PortfolioReport` artifacts.
2. Reports can be generated on demand via API or scheduled (daily, weekly, monthly).
3. Dashboards visualize PAR30/PAR90, NPAs, disbursal volumes, and collection rates.

Interactions
- Consumes data from modules 4,5,6 to compute metrics.
- Sends alerts when KPIs cross thresholds to module 8.

Suggested endpoints
- `GET /api/reports/summary?scope=branch&id=123&period=2026-06`
- `POST /api/reports/generate`

Notes
- Use pre-aggregated tables or materialized views to keep dashboard response times low.

### 8. Notifications & Alerts

Purpose
- Deliver in-app notifications (and later, SMS/Email) for repayment reminders, overdue alerts, meeting schedules and disbursement confirmations.

Key entities
- `Notification` (NotificationID, UserID, Message, Category, Status, CreatedDate)

Core workflow
1. Events in other modules (e.g., `RepaymentSchedule` due/overdue, `SanctionLetter.Issued`, `CentreMeeting` scheduled) emit notification events.
2. Notification service stores `Notification` records and dispatches messages to connected clients or in-app feeds.
3. Users mark notifications `Read`/`Dismissed`. Escalation rules trigger additional notifications (e.g., to branch manager) when thresholds met.

Interactions
- Subscribed by frontend clients and used by all modules for user-facing messages.

Suggested endpoints
- `GET /api/notifications?userId=123`
- `POST /api/notifications/mark-read`

Notes
- For production, replace in-app-only with SMS/Email channels via pluggable providers. Keep messages idempotent and avoid PII in notifications.

---

## Cross-Module Data Flows (End-to-End)

Example: Borrower applies for a loan (high-level sequence)

1. Borrower registers → IAM creates `User` and issues credentials.
2. Borrower profile & KYC submitted → `Borrower` + `BorrowerKYC` created (module 2).
3. KYC verified → `Borrower` status set to Active.
4. Borrower submits `LoanApplication` → (module 4) validation against `CreditAssessment` and `LoanProduct`.
5. Application approved → generate `SanctionLetter` → Borrower accepts.
6. Create `LoanAccount` and `RepaymentSchedule` (module 5) → Disbursement recorded.
7. Collections recorded against schedules (module 6) → Update `OutstandingPrincipal`.
8. Overdues are detected → `DelinquencyCase` created → Collections Officer actions proceed.
9. Reporting jobs aggregate metrics (module 7) → Dashboard updates and alerts triggered (module 8).

ASCII flow (simplified):

Borrower → IAM → Borrower/KYC → LoanApplication → Approval → LoanAccount → Disbursement → RepaymentSchedule → Collections → Delinquency → Reports

---

## Deployment & Integration Notes

- Local dev: `mvn spring-boot:run` + local RDBMS or H2 for in-memory.
- Staging/Prod: Run behind API gateway; place JWT secret, DB credentials and other secrets in a vault (Azure Key Vault / AWS Secrets Manager).
- Consider adding `docker-compose.yml` for local MySQL + app to simplify developer onboarding.

---

## Appendix: Suggested REST endpoints

- Authentication: `POST /api/auth/login`, `POST /api/auth/register`
- Borrowers: `POST /api/borrowers`, `GET /api/borrowers/{id}`
- KYC: `POST /api/borrowers/{id}/kyc`, `PUT /api/borrowers/{id}/kyc/{kycId}/verify`
- Groups & Centres: `POST /api/centres`, `POST /api/centres/{id}/meetings`, `POST /api/groups`
- Loan origination: `POST /api/loan-applications`, `PUT /api/loan-applications/{id}/approve`
- Loan accounts: `GET /api/loan-accounts/{id}`, `GET /api/loan-accounts/{id}/schedule`
- Collections: `POST /api/collections`, `GET /api/loan-accounts/{id}/collections`
- Delinquency: `GET /api/delinquency/cases`, `PUT /api/delinquency/cases/{id}/action`
- Reports: `GET /api/reports/summary`, `POST /api/reports/generate`
- Notifications: `GET /api/notifications`, `POST /api/notifications/mark-read`

---------------------------------------------------------------