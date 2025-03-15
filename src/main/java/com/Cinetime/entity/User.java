package com.Cinetime.entity;

import com.Cinetime.enums.Gender;
import com.Cinetime.enums.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "USER")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 20)
    private String name;

    @NotBlank
    @Size(min = 3, max = 20)
    private String surname;

    @NotNull
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phoneNumber;

    @Past
    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;


    private Boolean builtIn = false;

    @NotNull
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true) //default
    private String resetPasswordCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

}
