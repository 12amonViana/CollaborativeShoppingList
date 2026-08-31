package com.collaborativeshoppinglist.core.error

import com.collaborativeshoppinglist.core.logging.AppLogger
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctionsException

sealed interface AppError {
    val message: String

    data class Authentication(override val message: String) : AppError
    data class Permission(override val message: String) : AppError
    data class Validation(override val message: String) : AppError
    data class Network(override val message: String) : AppError
    data class Conflict(override val message: String) : AppError
    data class Unexpected(override val message: String) : AppError
}

object AppErrorMapper {
    fun from(throwable: Throwable): AppError {
        AppLogger.error("Falha em operação de dados", throwable)
        return when (throwable) {
        is IllegalArgumentException -> AppError.Validation(
            throwable.message ?: "Dados inválidos.",
        )
        is FirebaseNetworkException -> AppError.Network(
            "Sem conexão. Verifique a internet e tente novamente.",
        )
        is FirebaseAuthException -> AppError.Authentication(
            "Não foi possível autenticar. Verifique os dados informados.",
        )
        is FirebaseFirestoreException -> mapFirestore(throwable)
        is FirebaseFunctionsException -> mapFunction(throwable)
            else -> AppError.Unexpected("Ocorreu um erro inesperado. Tente novamente.")
        }
    }

    private fun mapFirestore(error: FirebaseFirestoreException): AppError =
        when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                AppError.Permission("Você não tem permissão para realizar esta ação.")
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                AppError.Network("A alteração não foi confirmada. Tente novamente.")
            FirebaseFirestoreException.Code.ABORTED,
            FirebaseFirestoreException.Code.ALREADY_EXISTS,
            -> AppError.Conflict("Os dados foram alterados. Atualize e tente novamente.")
            else -> AppError.Unexpected("Não foi possível acessar a lista.")
        }

    private fun mapFunction(error: FirebaseFunctionsException): AppError =
        when (error.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                AppError.Authentication("Entre novamente para continuar.")
            FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                AppError.Permission("Você não tem permissão para realizar esta ação.")
            FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                AppError.Validation(error.message ?: "Dados inválidos.")
            FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                AppError.Conflict("O convite expirou. Solicite um novo convite.")
            FirebaseFunctionsException.Code.ABORTED,
            FirebaseFunctionsException.Code.ALREADY_EXISTS,
            -> AppError.Conflict(error.message ?: "Esta ação não está mais disponível.")
            FirebaseFunctionsException.Code.FAILED_PRECONDITION ->
                if (error.message?.contains("LIST_CLOSED") == true) {
                    AppError.Conflict("A lista já foi encerrada.")
                } else {
                    AppError.Conflict("O convite ou a operação não está mais disponível.")
                }
            FirebaseFunctionsException.Code.UNAVAILABLE ->
                AppError.Network("Serviço indisponível. Tente novamente.")
            else -> AppError.Unexpected("Não foi possível concluir a ação.")
        }
}
