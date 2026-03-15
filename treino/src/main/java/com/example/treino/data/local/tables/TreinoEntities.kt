package com.example.treino.data.local.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pastas_treinos")
data class PastaEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val idUser: Int,
    val visibilidade: String, // 'publica' ou 'privada'
    val isDeletable: Boolean = true
)

@Entity(tableName = "sessoes")
data class SessaoEntity(
    @PrimaryKey val id: Int,
    val nome: String,
    val idPasta: Int,
    val idProximaSessao: Int
)

@Entity(tableName = "sessao_exercicios")
data class ExercicioEntity(
    @PrimaryKey val id: Int,
    val exercicioApiId: String,
    val notas: String,
    val idSessao: Int,
    val ordem: Int,
    val imagem: String?,
    val bodypart: String?
)

@Entity(tableName = "exercicio_sets")
data class SetEntity(
    @PrimaryKey val id: Int,
    val tipoSet: String,
    val peso: Double,
    val repeticoesMin: Int,
    val repeticoesMax: Int,
    val pesoUltimaVez: Double,
    val repeticoesUltimaVez: Int,
    val idExercicio: Int,
    val ordem: Int
)
