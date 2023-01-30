package io.betforge.player.application.verification

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.upload.File
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.extensions.validation.pictureFormat
import io.betforge.infrastructure.extensions.validation.pictureSize
import io.betforge.player.application.verification.queryobject.Verification
import io.betforge.player.model.verification.Status
import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.functions.isNotNull

class Validation {
    suspend fun onApprove(verification: Verification): Error? {
        return validate(verification) {
            this.validate(Verification::status).approveVerificationStatus()
        }.singleOrNull()
    }

    suspend fun onDecline(verification: Verification): Error? {
        return validate(verification) {
            this.validate(Verification::status).declineVerificationStatus()
        }.singleOrNull()
    }

    suspend fun onIDScan(images: List<File>): List<Error> {
        return images.map { image ->
            validate(image) {
                validate(File::content).isNotNull().pictureSize(max = 5000)
                validate(File::extension).pictureFormat()
            }
        }.reduce { acc, map -> acc + map }
    }

    object ApproveVerificationStatus : VerificationStatusConstraint
    object DeclineVerificationStatus : VerificationStatusConstraint
    interface VerificationStatusConstraint : Constraint

    private fun <E> Validator<E>.Property<Status?>.approveVerificationStatus(): Validator<E>.Property<Status?> =
        this.validate(ApproveVerificationStatus) { status ->
            status == Status.PENDING || status == Status.WAITING
        }

    private fun <E> Validator<E>.Property<Status?>.declineVerificationStatus(): Validator<E>.Property<Status?> =
        this.validate(DeclineVerificationStatus) { status ->
            status == Status.PENDING
        }
}
