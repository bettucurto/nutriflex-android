package com.example.treino.domain.models

data class Pasta(
    val id: Int,
    val nome: String,
    val idUser: Int,
    val visibilidade: String, // 'publica' ou 'privada'
    val isDeletable: Boolean = true
)

data class Sessao(
    val id: Int,
    val nome: String,
    val idPasta: Int,
    val idProximaSessao: Int = 0
)

data class Exercicio(
    val id: Int,
    val exercicioApiId: String,
    val notas: String,
    val idSessao: Int,
    val ordem: Int,
    val imagem: String? = null,
    val bodypart: String? = null
)

data class ExercicioSet(
    val id: Int,
    val tipoSet: String,
    val peso: Double,
    val repeticoesMin: Int,
    val repeticoesMax: Int,
    val pesoUltimaVez: Double,
    val repeticoesUltimaVez: Int,
    val idExercicio: Int,
    val ordem: Int
)
