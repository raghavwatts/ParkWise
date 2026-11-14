package com.parkwise.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a payment transaction linked to a parking session.
 */
public class Payment {

    public enum PaymentMethod { CASH, CARD, UPI, WAIVED }
    public enum PaymentStatus { PENDING, COMPLETED, FAILED }

    private int           id;
    private int           sessionId;
    private BigDecimal    amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;

    public Payment() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public int           getId()            { return id; }
    public int           getSessionId()     { return sessionId; }
    public BigDecimal    getAmount()        { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getPaidAt()        { return paidAt; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(int id)                          { this.id = id; }
    public void setSessionId(int sessionId)            { this.sessionId = sessionId; }
    public void setAmount(BigDecimal amount)           { this.amount = amount; }
    public void setPaymentMethod(PaymentMethod method) { this.paymentMethod = method; }
    public void setPaymentStatus(PaymentStatus status) { this.paymentStatus = status; }
    public void setPaidAt(LocalDateTime paidAt)        { this.paidAt = paidAt; }
}
