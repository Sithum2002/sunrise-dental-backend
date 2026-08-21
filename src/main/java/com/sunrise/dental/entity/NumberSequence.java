package com.sunrise.dental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// Optimistic-locked counter used to generate sequential business numbers
// (patient reg no, appointment no, bill no) without double-allocation.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "number_sequences")
public class NumberSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_key", nullable = false, unique = true, length = 50)
    private String sequenceKey;

    @Column(nullable = false)
    private long currentValue;

    @Column(name = "prefix", nullable = false, length = 10)
    private String prefix;

    @Version
    @Column(name = "version")
    private long version;
}
