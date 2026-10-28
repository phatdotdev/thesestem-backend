# 🎓 Thesis Management System Backend (thesestem-backend)

A robust, enterprise-ready RESTful backend API service built for managing university **Thesis & Capstone Projects** (`Thesis Management System`). This system streamlines the end-to-end workflow of academic thesis registration, advisor assignment, progress tracking, document submission, and evaluation.

---

## 📌 Table of Contents
- [Overview](#-overview)
- [Key Features](#-key-features)
- [Role-Based Workflows](#-role-based-workflows)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Installation & Execution](#installation--execution)
- [API Reference Summary](#-api-reference-summary)
- [Author](#-author)

---

## 📖 Overview

Managing academic theses involves complex coordination between students, faculty advisors, department heads, and reviewers. **thesestem-backend** provides a centralized backend infrastructure to handle topic proposals, approvals, progress reporting, file uploads (drafts/final theses), and evaluation scoring with strict role-based authorization.

---

## ✨ Key Features

- 🔐 **Authentication & Security:** Secure user login/register with JWT, password encryption (bcrypt), and refresh token rotation.
- 👥 **Role-Based Access Control (RBAC):** Distinct permissions for `Students`, `Advisors/Lecturers`, `Department Chairs`, and `System Admins`.
- 📌 **Thesis Topic Management:** Proposal submission, review workflow (Approve/Reject/Request Revision), and status updates.
- 📈 **Progress & Milestone Tracking:** Track thesis phases (Outline approval, Mid-term review, Final defense submission).
- 📁 **Document Submission:** Upload and manage thesis documentation, research proposals, and presentation slides.
- 📊 **Evaluation & Defense Scheduling:** Manage defense committees, score records, and reviewer feedback.

---

## 👤 Role-Based Workflows

```text
[ Student ]     ---> Submits Thesis Proposal / Uploads Progress Reports
[ Advisor ]     ---> Reviews Proposal, Mentors, Approves Submissions & Gives Scores
[ Dept Chair ]  ---> Assigns Advisors, Approves Final Defense Committees
[ Admin ]       ---> Manages Users, Academic Years, Semesters, and System Audits
