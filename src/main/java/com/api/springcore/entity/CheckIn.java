package com.api.springcore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id")
    private Attendee attendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_token_id")
    private QrToken qrToken;


    @Column(nullable = false, length = 20)
    private String method;

    @CreationTimestamp
    @Column(name = "checked_in_at", updatable = false)
    private LocalDateTime checkedInAt;
}