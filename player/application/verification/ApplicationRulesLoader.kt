package io.betforge.player.application.verification

import com.google.inject.Inject
import io.betforge.player.model.verification.Rule
import io.betforge.player.model.verification.RulesLoader
import javax.annotation.Nullable
import io.betforge.configuration.model.apps.Player as PlayerConfig

class ApplicationRulesLoader @Inject constructor(@Nullable private val player: PlayerConfig?) : RulesLoader {
    override fun rules(): List<Rule> {
        return player?.let { playerConfig ->
            playerConfig.verificationRules.map {
                Rule(it.key, it.value)
            }
        }.orEmpty()
    }
}
