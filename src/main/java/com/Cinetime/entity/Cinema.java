package com.Cinetime.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district; //

    @Column(nullable = false)
    private String address;

    @Column
    private String phone;

    @Column
    private String email;

    @OneToMany(mappedBy = "cinema")
    private List<Hall> halls;//

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

}
