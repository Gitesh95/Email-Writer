package com.email_writer.email_writer.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "email_replies")
public class EmailReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // For full email content, LONGTEXT is safer
    @Column(columnDefinition = "LONGTEXT")
    private String originalEmail;

    private String tone;

    // Also LONGTEXT since replies can be long too
    @Column(columnDefinition = "LONGTEXT")
    private String generatedReply;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Long getId() { return id; }
    public String getOriginalEmail() { return originalEmail; }
    public void setOriginalEmail(String originalEmail) { this.originalEmail = originalEmail; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
    public String getGeneratedReply() { return generatedReply; }
    public void setGeneratedReply(String generatedReply) { this.generatedReply = generatedReply; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
