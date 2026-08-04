# Banking Platform Domain Scope

## Purpose

The system manages internal money transfers between accounts owned by customers of the same bank.

## Core Capabilities

- Register customers.
- Create bank accounts.
- Query account balances.
- Deposit test funds.
- Transfer money between accounts.
- Query transfer status.
- Query account movements.

## Main Domain Concepts

- Customer
- Account
- Transfer
- Account Movement
- Money
- Currency

## Initial Business Rules

- Transfer amounts must be positive.
- Source and destination accounts must be different.
- Both accounts must be active.
- Both accounts must use the same currency.
- The source account must have sufficient balance.
- A transfer must be atomic.
- A transfer request must be idempotent.
- Account movements cannot be deleted.
- Monetary values must use BigDecimal.

## Initial Scope

The first version supports internal transfers in PEN and USD.

## Out of Scope

- Cards
- Loans
- Interest calculation
- Foreign exchange
- Interbank transfers
- Payment processing
- Microservices
- Event streaming