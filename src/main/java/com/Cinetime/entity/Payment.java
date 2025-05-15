package com.Cinetime.entity;

import com.Cinetime.converter.PaymentStatusConverter;
import com.Cinetime.converter.TicketStatusConverter;
import com.Cinetime.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "anonymous_user_id")
    private AnonymousUser anonymousUser;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Ticket> tickets = new HashSet<>();

    @NotNull
    @Convert(converter = PaymentStatusConverter.class)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;


    @PrePersist
    @PreUpdate
    private void validateUserOrAnonymous() {
        if (user == null && anonymousUser == null) {
            throw new IllegalStateException("Ticket must have either a User or AnonymousUser");
        }
        if (user != null && anonymousUser != null) {
            throw new IllegalStateException("Ticket cannot have both User and AnonymousUser");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
