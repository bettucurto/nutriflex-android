package com.example.treino.domain.mappers

import com.example.treino.data.local.tables.ExercicioEntity
import com.example.treino.data.local.tables.PastaEntity
import com.example.treino.data.local.tables.SessaoEntity
import com.example.treino.data.local.tables.SetEntity
import com.example.treino.data.remote.ExercicioDto
import com.example.treino.data.remote.ExercicioSetDto
import com.example.treino.data.remote.PastaDto
import com.example.treino.data.remote.SessaoDto
import com.example.treino.domain.models.Exercicio
import com.example.treino.domain.models.ExercicioSet
import com.example.treino.domain.models.Pasta
import com.example.treino.domain.models.Sessao

// --- PASTA ---
fun PastaDto.toEntity() = PastaEntity(
    id = id,
    nome = nome,
    idUser = idUser,
    visibilidade = visibilidade,
    isDeletable = isDeletable == 1
)

fun PastaEntity.toDomain() = Pasta(
    id = id,
    nome = nome,
    idUser = idUser,
    visibilidade = visibilidade,
    isDeletable = isDeletable
)

// --- SESSAO ---
fun SessaoDto.toEntity() = SessaoEntity(
    id = id,
    nome = nome,
    idPasta = idPasta,
    idProximaSessao = idProximaSessao
)

fun SessaoEntity.toDomain() = Sessao(
    id = id,
    nome = nome,
    idPasta = idPasta,
    idProximaSessao = idProximaSessao
)

// --- EXERCICIO ---
fun ExercicioDto.toEntity() = ExercicioEntity(
    id = id,
    exercicioApiId = exercicioApiId,
    notas = notas,
    idSessao = idSessao,
    ordem = ordem,
    imagem = imagem,
    bodypart = bodypart
)

fun ExercicioEntity.toDomain() = Exercicio(
    id = id,
    exercicioApiId = exercicioApiId,
    notas = notas,
    idSessao = idSessao,
    ordem = ordem,
    imagem = imagem,
    bodypart = bodypart
)

// --- SET ---
fun ExercicioSetDto.toEntity() = SetEntity(
    id = id,
    tipoSet = tipoSet,
    peso = peso,
    repeticoesMin = repeticoesMin,
    repeticoesMax = repeticoesMax,
    pesoUltimaVez = pesoUltimaVez,
    repeticoesUltimaVez = repeticoesUltimaVez,
    idExercicio = idExercicio,
    ordem = ordem
)

fun SetEntity.toDomain() = ExercicioSet(
    id = id,
    tipoSet = tipoSet,
    peso = peso,
    repeticoesMin = repeticoesMin,
    repeticoesMax = repeticoesMax,
    pesoUltimaVez = pesoUltimaVez,
    repeticoesUltimaVez = repeticoesUltimaVez,
    idExercicio = idExercicio,
    ordem = ordem
)
