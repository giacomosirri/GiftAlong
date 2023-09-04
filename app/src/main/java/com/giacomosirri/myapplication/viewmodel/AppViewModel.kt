package com.giacomosirri.myapplication.viewmodel

import androidx.lifecycle.*
import com.giacomosirri.myapplication.R
import com.giacomosirri.myapplication.data.entity.Event
import com.giacomosirri.myapplication.data.entity.Item
import com.giacomosirri.myapplication.data.entity.Relationship
import com.giacomosirri.myapplication.repository.EventRepository
import com.giacomosirri.myapplication.repository.ItemRepository
import com.giacomosirri.myapplication.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AppViewModel(
    private val eventRepository: EventRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    // User functions
    fun registerUser(
        username: String,
        password: String,
        name: String,
        surname: String,
        image: Int? = null,
        birthday: Date
    ) {
        viewModelScope.launch {
            userRepository.insertUser(username, password, name, surname, image?: R.drawable.placeholder, birthday)
        }
    }

    suspend fun loginUser(username: String, password: String): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            userRepository.getUser(username, password)
        }
    }

    fun unregisterUser(username: String) {
        viewModelScope.launch {
            userRepository.deleteUser(username)
        }
    }

    // Event functions
    fun addEvent(
        name: String,
        date: Date,
        location: String?,
        organizer: String,
        dressCode: String?,
        friendsAllowed: Boolean,
        familyAllowed: Boolean,
        partnersAllowed: Boolean,
        colleaguesAllowed: Boolean
    ) {
        viewModelScope.launch {
            eventRepository.insertEvent(
                name, date, location, organizer, dressCode,
                friendsAllowed, familyAllowed, partnersAllowed, colleaguesAllowed
            )
        }
    }

    fun removeEvent(eventId: Int) = viewModelScope.launch {
        eventRepository.deleteEvent(eventId)
    }

    fun updateEvent(
        id: Int,
        name: String,
        date: Date,
        location: Nothing?,
        organizer: String,
        dressCode: String?,
        friendsAllowed: Boolean,
        partnersAllowed: Boolean,
        familyAllowed: Boolean,
        colleaguesAllowed: Boolean
    ) {

    }

    private fun isInvitedToEvent(type : Relationship.RelationshipType, event : Event) : Boolean {
        return when(type) {
            Relationship.RelationshipType.FRIEND -> event.friendsAllowed
            Relationship.RelationshipType.FAMILY -> event.familyAllowed
            Relationship.RelationshipType.PARTNER -> event.partnersAllowed
            Relationship.RelationshipType.COLLEAGUE -> event.colleaguesAllowed
        }
    }

    fun getEventsOfUser(username : String): Flow<Set<Event>> {
        val potentialEvents = eventRepository.getPotentialEventsOfUser(username)
        return potentialEvents.map { map ->
            map.filter { (event, type) ->
                isInvitedToEvent(type, event)
            }.keys
        }
    }

    // Item functions

    fun getItemsOfUser(username: String): Flow<List<Item>> = itemRepository.getItemsOfUser(username)

    fun addItem(
        name: String,
        description: String? = null,
        url: String? = null,
        image: Int? = null,
        priceL: Double? = null,
        priceU: Double? = null,
        listedBy: String
    ) {
        viewModelScope.launch {
            itemRepository.insertItem(name, description, url, image?: R.drawable.placeholder, priceL, priceU, listedBy)
        }
    }

    fun deleteItem(id : Int) = viewModelScope.launch {
        itemRepository.deleteItem(id)
    }

    fun updateItem(
        id: Int,
        bought: Boolean? = null,
        name: String? = null,
        description: String? = null,
        url: String? = null,
        image: Int? = null,
        priceL: Double? = null,
        priceU: Double? = null,
        reservedBy: String? = null
    ) {
        viewModelScope.launch {
            itemRepository.updateItem(id, bought, name, description, url, image, priceL, priceU, reservedBy)
        }
    }
}

class AppViewModelFactory(
    private val eventRepository: EventRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(eventRepository, itemRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}