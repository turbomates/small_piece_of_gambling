package io.betforge.player.application.group.command

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.player.application.filter.queryobject.FilterExistQO
import io.betforge.player.application.group.queryobject.GroupExistsQO
import io.betforge.player.application.group.queryobject.SingleGroupQO
import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.functions.isEmpty
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isNull
import java.util.UUID

class Validation @Inject constructor(
    private val queryExecutor: QueryExecutor
) {
    object GroupExists : Constraint {
        override val name: String
            get() = "Group does not exist"
    }

    object FilterExists : Constraint {
        override val name: String
            get() = "Player filter does not exist"
    }

    object DynamicGroup : Constraint {
        override val name: String
            get() = "This is dynamic group, you cannot change it manually"
    }

    suspend fun onCreateGroup(command: CreateGroup): List<Error> {
        return validate(command) {
            validate(CreateGroup::name).isNotNull().isNotEmpty()
            if (command.filterId != null) {
                validate(CreateGroup::playerIds).isEmpty()
            }

            if (command.playerIds != null) {
                validate(CreateGroup::filterId).isNull()
            }

            validate(CreateGroup::color).isNotNull().isNotEmpty()
            validate(CreateGroup::priority).isNotNull()
        }
    }

    suspend fun onEditGroup(command: EditGroup): List<Error> {
        return validate(command) {
            validate(EditGroup::id).isNotNull().isExistingGroup()
            validate(EditGroup::name).isNotNull().isNotEmpty()
            validate(EditGroup::color).isNotNull().isNotEmpty()
            validate(EditGroup::priority).isNotNull()
            if (command.filterId != null) {
                validate(EditGroup::filterId).isExistingFilter()
            }
        }
    }

    suspend fun onDeleteGroup(command: DeleteGroup): List<Error> {
        return validate(command) {
            validate(DeleteGroup::id).isNotNull().isExistingGroup()
        }
    }

    suspend fun onAddPlayersToGroup(command: AddPlayersToGroup): List<Error> {
        return validate(command) {
            validate(AddPlayersToGroup::groupId).isNotNull().isExistingGroup().isManualGroup()
            validate(AddPlayersToGroup::playerIds).isNotNull().isNotEmpty()
        }
    }

    suspend fun onRemovePlayersFromGroup(command: RemovePlayersFromGroup): List<Error> {
        return validate(command) {
            validate(RemovePlayersFromGroup::groupId).isNotNull().isExistingGroup().isManualGroup()
        }
    }

    private suspend fun <E> Validator<E>.Property<UUID?>.isExistingGroup(): Validator<E>.Property<UUID?> =
        this.coValidate(GroupExists) { value ->
            value == null || queryExecutor.execute(GroupExistsQO(value))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isExistingFilter(): Validator<E>.Property<UUID?> =
        this.coValidate(FilterExists) { value ->
            value != null && queryExecutor.execute(FilterExistQO(value))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isManualGroup(): Validator<E>.Property<UUID?> =
        this.coValidate(DynamicGroup) { value ->
            value == null || queryExecutor.execute(SingleGroupQO(value))?.filterId == null
        }
}
