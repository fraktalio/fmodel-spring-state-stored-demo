package com.fraktalio.example.fmodelspringdemo.adapter.web.rest

import com.fraktalio.example.fmodelspringdemo.application.Aggregate
import com.fraktalio.example.fmodelspringdemo.application.AggregateState
import com.fraktalio.example.fmodelspringdemo.domain.Command
import com.fraktalio.example.fmodelspringdemo.domain.CreateRestaurantCommand
import com.fraktalio.example.fmodelspringdemo.domain.PlaceOrderCommand
import com.fraktalio.fmodel.application.handle
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class AggregateRestCommandController(private val aggregate: Aggregate) {

    private suspend fun handle(command: Command): AggregateState =
        aggregate.handle(command)

    @PostMapping("restaurants")
    suspend fun createRestaurant(@RequestBody command: CreateRestaurantCommand): AggregateState =
        handle(command)

    @PostMapping("restaurants/{restaurantId}/orders")
    suspend fun placeOrder(@RequestBody command: PlaceOrderCommand, @PathVariable restaurantId: UUID): AggregateState {
        require(command.identifier.value == restaurantId) {
            "Restaurant identifier must be the same as the one in the request"
        }
        return handle(command)
    }
}