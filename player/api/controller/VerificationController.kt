package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.filter.filterValues
import dev.tmsoft.lib.query.paging.pagingParameters
import dev.tmsoft.lib.structure.Either
import dev.tmsoft.lib.upload.File
import dev.tmsoft.lib.upload.FileManagerFactory
import io.betforge.player.application.verification.Validation
import io.betforge.player.application.verification.VerificationInfoLoader
import io.betforge.player.application.verification.VerificationNotFound
import io.betforge.player.application.verification.queryobject.PlayerVerificationsQO
import io.betforge.player.application.verification.queryobject.Verification
import io.betforge.player.application.verification.queryobject.VerificationQO
import io.betforge.player.application.verification.queryobject.VerificationsCountQO
import io.betforge.player.application.verification.queryobject.VerificationsQO
import io.betforge.player.application.verification.type.Email
import io.betforge.player.application.verification.type.IDScan
import io.betforge.player.model.verification.Factory
import io.betforge.player.model.verification.RulesEngine
import io.betforge.player.model.verification.VerificationRepository
import java.util.UUID

class VerificationController @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val verification: io.betforge.player.application.verification.Verification,
    private val verificationInfo: VerificationInfoLoader,
    private val verificationFactory: Factory,
    private val validation: Validation,
    private val fileManager: FileManagerFactory,
    private val rulesEngine: RulesEngine,
    private val verifications: VerificationRepository,
    private val transaction: TransactionManager
) : Controller() {

    suspend fun list(): Response.Listing<Verification> {
        return Response.Listing(
            queryExecutor.execute(
                VerificationsQO(
                    call.request.pagingParameters(),
                    call.parameters.filterValues()
                )
            )
        )
    }

    fun all(): Response.Data<Set<String>> {
        return Response.Data(rulesEngine.types())
    }

    suspend fun playerVerifications(playerId: UUID): Response.Data<List<Verification>> {
        return Response.Data(
            queryExecutor.execute(
                PlayerVerificationsQO(playerId)
            )
        )
    }

    suspend fun idScanVerification(playerId: UUID, images: List<File>): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onIDScan(images)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        val verification = verificationFactory.getVerifier(IDScan)
        val fileManager = fileManager.current()
        val paths = images.map {
            fileManager.add(it, "verification")
        }
        transaction {
            val info = verificationInfo.load(playerId).copy(idScan = paths.map { fileManager.getWebUri(it) })
            verification.init(info)
        }
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun emailVerification(token: UUID): Response.Either<Response.Ok, Response.Error> {
        val verification = verificationFactory.getVerifier(Email)
        return transaction {
            val existVerification = verifications.findByToken(token)
                ?: return@transaction Response.Either(Either.Right(Response.Error(dev.tmsoft.lib.validation.Error("Not found player"))))
            val info = verificationInfo.load(existVerification.player).copy(emailCode = token)
            verification.verify(info)
            return@transaction Response.Either(Either.Left(Response.Ok))
        }
    }

    suspend fun show(verification: UUID): Response.Data<Verification> {
        return Response.Data(queryExecutor.execute(VerificationQO(verification)))
    }

    suspend fun total(): Response.Data<Long> {
        return Response.Data(queryExecutor.execute(VerificationsCountQO(call.parameters.filterValues())))
    }

    suspend fun approve(playerId: UUID, key: String, reason: String?): Response.Either<Response.Ok, Response.Error> {
        try {
            val playerVerification = queryExecutor.execute(PlayerVerificationsQO(playerId)).single { it.type == key }
            val error = validation.onApprove(playerVerification)
            if (error != null) {
                return Response.Either(Either.Right(Response.Error(error)))
            }
            verification.approve(playerId, key, reason)
        } catch (e: VerificationNotFound) {
            return Response.Either(Either.Right(Response.Error(dev.tmsoft.lib.validation.Error(e.localizedMessage))))
        }
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun decline(
        playerId: UUID,
        key: String,
        reason: String?
    ): Response.Either<Response.Ok, Response.Error> {
        try {
            val playerVerification = queryExecutor.execute(PlayerVerificationsQO(playerId)).single { it.type == key }
            val error = validation.onDecline(playerVerification)
            if (error != null) {
                return Response.Either(Either.Right(Response.Error(error)))
            }
            verification.decline(playerId, key, reason)
        } catch (e: VerificationNotFound) {
            return Response.Either(Either.Right(Response.Error(dev.tmsoft.lib.validation.Error(e.localizedMessage))))
        }
        return Response.Either(Either.Left(Response.Ok))
    }
}
