package com.example.treino.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pasta(
    val id: Int,
    val nome: String,
    val idUser: Int,
    val visibilidade: String, // 'publica' ou 'privada'
    val isDeletable: Boolean = true,
    val frequency: Int? = null,
    val experience: Int? = null
) : Parcelable

@Parcelize
data class Sessao(
    val id: Int,
    val nome: String,
    val idPasta: Int,
    val idProximaSessao: Int = 0
) : Parcelable

@Parcelize
data class Exercicio(
    val id: Int,
    val exercicioApiId: String,
    val nome: String,
    val notas: String,
    val idSessao: Int,
    val ordem: Int,
    val imagem: String? = null,
    val bodypart: String? = null
) : Parcelable

@Parcelize
data class ExercicioSet(
    val id: Int,
    val tipoSet: String,
    val peso: Double,
    val repeticoesMin: Int,
    val repeticoesMax: Int,
    val pesoUltimaVez: Double,
    val repeticoesUltimaVez: Int,
    val idExercicio: Int,
    val ordem: Int,
    val isChecked: Boolean = false
) : Parcelable
