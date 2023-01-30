package io.betforge.player.application.verification.type

import com.google.inject.Inject
import io.betforge.player.model.verification.Status
import io.betforge.player.model.verification.Verification
import io.betforge.player.model.verification.VerificationData
import io.betforge.player.model.verification.VerificationInfo
import io.betforge.player.model.verification.Verifications
import io.betforge.player.model.verification.Verifier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameStop @Inject constructor(
    private val access: GameStopAccess,
    private val verifications: Verifications,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) :
    Verifier {
    override val key = Companion

    private val client: HttpClient = HttpClient(CIO)

    override fun decline(info: VerificationInfo, reason: String?): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        verification.decline(reason)
        return verification
    }

    override fun approve(info: VerificationInfo, reason: String?): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        verification.approve(reason)
        return verification
    }

    override suspend fun init(info: VerificationInfo): Verification {
        return verify(info)
    }

    @OptIn(InternalAPI::class)
    override suspend fun verify(info: VerificationInfo): Verification = withContext(dispatcher) {
        val verification = verifications.findByPlayerAndType(info.playerId, key)
            ?: Verification.create(info.playerId, key)

        verification.appendData(
            GameStopData(
                info.bio.firstName,
                info.bio.lastName,
                info.bio.birthday,
                info.email,
                info.address.postcode
            )
        )
        val response = client.post(access.url) {
            header("X-API-Key", access.apiKey)
            body = FormDataContent(
                Parameters.build {
                    append("firstname", info.bio.firstName)
                    append("lastName", info.bio.lastName)
                    append("dateOfBirth", info.bio.birthday)
                    append("email", info.email)
                    append("postcode", info.address.postcode)
                }
            )
        }.body<HttpResponse>()
        val exclusion: String? = response.headers["X-Exclusion"]
        exclusion?.let { if (it.lowercase() == "n") verification.decline(it) else verification.approve(it) }
            ?: verification.decline(exclusion)
        verification
    }

    companion object : Verifier.Key<GameStop>("game stop")
}

data class GameStopAccess(val apiKey: String, val operatorId: String, val url: String)
data class GameStopData(
    val firstName: String,
    val lastName: String,
    val birthday: String,
    val email: String,
    val postcode: String,
    override val status: Status = Status.PENDING,
    override val reason: String? = null
) : VerificationData
