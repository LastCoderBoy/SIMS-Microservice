package com.sims.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Table(name = "BlacklistedTokens")
@NoArgsConstructor
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", unique = true, nullable = false, length = 500)
    private String token;

    @Column(name = "blacklistedAt")
    private Date blacklistedAt;

    public BlacklistedToken(String token, Date blacklistedAt) {
        this.token = token;
        this.blacklistedAt = blacklistedAt;
    }
}