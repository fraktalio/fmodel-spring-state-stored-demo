package com.fraktalio.example.fmodelspringdemo

import com.fraktalio.example.fmodelspringdemo.adapter.persistence.*
import com.fraktalio.example.fmodelspringdemo.application.Aggregate
import com.fraktalio.example.fmodelspringdemo.application.AggregateStateRepository
import com.fraktalio.example.fmodelspringdemo.application.aggregate
import com.fraktalio.example.fmodelspringdemo.domain.*
import io.r2dbc.spi.ConnectionFactory
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.messaging.converter.KotlinSerializationJsonMessageConverter
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.web.util.pattern.PathPatternRouteMatcher


@Configuration
class FmodelSpringDemoConfiguration {
    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer =
        ConnectionFactoryInitializer().apply {
            setConnectionFactory(connectionFactory)
            setDatabasePopulator(
                CompositeDatabasePopulator().apply {
                    addPopulators(
                        ResourceDatabasePopulator(ClassPathResource("./sql/schema.sql"))
                    )
                }
            )
        }

    @Bean
    internal fun restaurantDeciderBean() = restaurantDecider()

    @Bean
    internal fun orderDeciderBean() = orderDecider()

    @Bean
    internal fun restaurantSagaBean() = restaurantSaga()

    @Bean
    internal fun orderSagaBean() = orderSaga()

    @Bean
    internal fun aggregateStateRepositoryBean(
        restaurantRepository: RestaurantCoroutineRepository,
        restaurantOrderRepository: OrderCoroutineRepository,
        restaurantOrderItemRepository: OrderItemCoroutineRepository,
        menuItemCoroutineRepository: MenuItemCoroutineRepository,
        operator: TransactionalOperator
    ): AggregateStateRepository = AggregateStateRepositoryImpl(
        restaurantRepository,
        restaurantOrderRepository,
        restaurantOrderItemRepository,
        menuItemCoroutineRepository,
        operator
    )

    @Bean
    internal fun aggregateBean(
        restaurantDecider: RestaurantDecider,
        orderDecider: OrderDecider,
        restaurantSaga: RestaurantSaga,
        orderSaga: OrderSaga,
        stateRepository: AggregateStateRepository
    ): Aggregate = aggregate(orderDecider, restaurantDecider, orderSaga, restaurantSaga, stateRepository)


    @Bean
    fun messageConverter(): KotlinSerializationJsonMessageConverter {
        return KotlinSerializationJsonMessageConverter(Json)
    }

    @Bean
    fun rsocketStrategies(): RSocketStrategies {

        return RSocketStrategies.builder()
            .encoders { it.add(KotlinSerializationJsonEncoder()) }
            .decoders { it.add(KotlinSerializationJsonDecoder()) }
            .routeMatcher(PathPatternRouteMatcher())
            .build()
    }
}