package com.fraktalio.example.fmodelspringdemo.application

import com.fraktalio.example.fmodelspringdemo.domain.*
import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.application.StateStoredOrchestratingAggregate
import com.fraktalio.fmodel.domain.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.Serializable

internal typealias AggregateStateRepository = StateRepository<Command?, AggregateState>
internal typealias Aggregate = StateStoredOrchestratingAggregate<Command?, AggregateState, Event?>

/**
 * One, big aggregate that is `combining` all deciders: [orderDecider], [restaurantDecider].
 * Every command will be handled by one of the deciders.
 * The decider that is not interested in specific command type will simply ignore it (do nothing).
 *
 * @param orderDecider orderDecider is used internally to handle commands and produce new events.
 * @param restaurantDecider restaurantDecider is used internally to handle commands and produce new events.
 * @param orderSaga orderSaga is used internally to react on [RestaurantEvent]s and produce commands of type [OrderCommand]
 * @param restaurantSaga restaurantSaga is used internally to react on [OrderEvent]s and produce commands of type [RestaurantCommand]
 * @param stateRepository is used to store the newly produced state of the Aggregate
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun aggregate(
    orderDecider: OrderDecider,
    restaurantDecider: RestaurantDecider,
    orderSaga: OrderSaga,
    restaurantSaga: RestaurantSaga,
    stateRepository: AggregateStateRepository

): Aggregate = StateStoredOrchestratingAggregate(
    // Combining two deciders into one, and mapping the `Pair` into convenient domain specific data class `AggregateState`
    decider = orderDecider
        .combine(restaurantDecider)
        .dimapOnState(
            fl = { aggregateState: AggregateState -> Pair(aggregateState.order, aggregateState.restaurant) },
            fr = { pair: Pair<Order?, Restaurant?> -> AggregateState(pair.first, pair.second) }
        ),
    // How and where do you want to store new state.
    stateRepository = stateRepository,
    // Combining individual choreography Sagas into one orchestrating Saga.
    saga = orderSaga.combine(restaurantSaga)
)

@Serializable
data class AggregateState(val order: Order?, val restaurant: Restaurant?)


