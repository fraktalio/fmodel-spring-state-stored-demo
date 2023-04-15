package com.fraktalio.example.fmodelspringdemo.adapter.web.rsocket

import com.fraktalio.example.fmodelspringdemo.adapter.persistence.RestaurantCoroutineRepository
import com.fraktalio.example.fmodelspringdemo.adapter.persistence.RestaurantEntity
import com.fraktalio.example.fmodelspringdemo.application.Aggregate
import com.fraktalio.example.fmodelspringdemo.application.AggregateState
import com.fraktalio.example.fmodelspringdemo.domain.*
import com.fraktalio.fmodel.application.handle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import java.util.*

@Controller
internal class AggregateRsocketController(
    private val aggregate: Aggregate,
    private val repository: RestaurantCoroutineRepository
) {
    @OptIn(FlowPreview::class)
    @MessageMapping("commands")
    fun handleCommand(@Payload commands: Flow<Command>): Flow<AggregateState> =
        aggregate.handle(commands)

    @MessageMapping("queries.restaurants")
    fun findAllRestaurants(): Flow<RestaurantEntity> = repository.findAll()

    @MessageMapping("queries.restaurants.{id}")
    suspend fun findRestaurant(@DestinationVariable id: String): RestaurantEntity? = repository.findById(id)

}