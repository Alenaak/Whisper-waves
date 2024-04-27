package com.example.whisper_waves.ui.users

import androidx.lifecycle.*
import com.example.whisper_waves.data.db.entity.User
import com.example.whisper_waves.data.db.repository.DatabaseRepository
import com.example.whisper_waves.ui.DefaultViewModel
import com.example.whisper_waves.data.Event
import com.example.whisper_waves.data.Result


class UsersViewModelFactory(private val myUserID: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UsersViewModel(myUserID) as T
    }
}

class UsersViewModel(private val myUserID: String) : DefaultViewModel() {
    private val repository: DatabaseRepository = DatabaseRepository()

    private val _selectedUser = MutableLiveData<Event<User>>()
    var selectedUser: LiveData<Event<User>> = _selectedUser
    private val updatedUsersList = MutableLiveData<MutableList<User>>()
    val usersList = MediatorLiveData<List<User>>()

    init {
        usersList.addSource(updatedUsersList) { mutableList ->
            usersList.value = updatedUsersList.value?.filter { it.info.id != myUserID }
        }
        loadUsers()
    }

    private fun loadUsers() {
        repository.loadUsers { result: Result<MutableList<User>> ->
            onResult(updatedUsersList, result)
        }
    }

    fun selectUser(user: User) {
        _selectedUser.value = Event(user)
    }
}