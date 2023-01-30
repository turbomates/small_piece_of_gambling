package io.betforge.player.application.automation.command

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.bonus.application.queryobject.BonusRuleExistsQO
import io.betforge.player.application.group.queryobject.GroupExistsQO
import io.betforge.player.model.automation.Automation
import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.functions.isNotNull
import java.util.UUID

class Validation @Inject constructor(private val queryExecutor: QueryExecutor) {
    object GroupExists : Constraint {
        override val name: String
            get() = "Group does not exist"
    }

    object IssueExists : Constraint {
        override val name: String
            get() = "Issue does not exist"
    }

    suspend fun validate(command: CreateAutomation): List<Error> {
        return validate(command) {
            validate(CreateAutomation::groupId).isExistingGroup()
            validate(CreateAutomation::issueId).isExistingIssue(command.issueType)
            if (command.trigger == Automation.Trigger.PERIODIC) validate(CreateAutomation::period).isNotNull()
        }
    }

    private suspend fun <E> Validator<E>.Property<UUID?>.isExistingGroup(): Validator<E>.Property<UUID?> =
        this.coValidate(GroupExists) { value ->
            value == null || queryExecutor.execute(GroupExistsQO(value))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isExistingIssue(issueType: Automation.IssueType): Validator<E>.Property<UUID?> =
        this.coValidate(IssueExists) { value ->
            value != null && when (issueType) {
                Automation.IssueType.BONUS -> queryExecutor.execute(BonusRuleExistsQO(value))
                Automation.IssueType.SEND_EMAIL -> true
            }
        }
}
