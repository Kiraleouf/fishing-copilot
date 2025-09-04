package com.example.demo.fisherman

import jakarta.persistence.*

@Entity
@Table(name = "fisherman")
class Fisherman(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(unique = true, nullable = false)
    var login: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Column(name = "secret_question", nullable = false)
    var secretQuestion: String = "",

    @Column(name = "secret_answer", nullable = false)
    var secretAnswer: String = ""
)
