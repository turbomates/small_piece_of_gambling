package io.betforge.player.application.verification.subscriber

import com.google.inject.Inject
import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.event.EventSubscriber
import dev.tmsoft.lib.event.EventsSubscriber
import io.betforge.player.application.verification.Verification
import io.betforge.player.model.event.PlayerRegistered
import io.betforge.player.model.event.VerificationApproved
import io.betforge.player.model.event.VerificationDeclined

class VerificationStatusApplier @Inject constructor(
    private val verification: Verification
) : EventsSubscriber {
    override fun subscribers(): List<EventsSubscriber.EventSubscriberItem<out Event>> {
        return listOf(
            VerificationDeclined to object : EventSubscriber<VerificationDeclined> {
                override suspend fun invoke(event: VerificationDeclined) {
                    verification.applyPlayerVerifications(event.playerId)
                }
            },
            VerificationApproved to object : EventSubscriber<VerificationApproved> {
                override suspend fun invoke(event: VerificationApproved) {
                    verification.applyPlayerVerifications(event.playerId)
                }
            },
            PlayerRegistered to object : EventSubscriber<PlayerRegistered> {
                override suspend fun invoke(event: PlayerRegistered) {
                    verification.start(event.id)
                }
            }
        )
    }
}
