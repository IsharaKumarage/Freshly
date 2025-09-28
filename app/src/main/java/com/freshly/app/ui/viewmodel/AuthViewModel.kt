package com.freshly.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.User
import com.freshly.app.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            _authState.value = AuthState.AUTHENTICATED
            loadCurrentUser(userId)
        } else {
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
    
    fun signUp(email: String, password: String, user: User) {
        viewModelScope.launch {
            _loading.value = true
            repository.signUp(email, password, user).fold(
                onSuccess = { userId ->
                    _authState.value = AuthState.AUTHENTICATED
                    loadCurrentUser(userId)
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            )
            _loading.value = false
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.signIn(email, password).fold(
                onSuccess = { userId ->
                    _authState.value = AuthState.AUTHENTICATED
                    loadCurrentUser(userId)
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            )
            _loading.value = false
        }
    }
    
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.sendPasswordReset(email).fold(
                onSuccess = { 
                    _error.value = null
                    // You might want to show a success message here
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.UNAUTHENTICATED
        _currentUser.value = null
    }
    
    private fun loadCurrentUser(userId: String) {
        viewModelScope.launch {
            repository.getUserData(userId).fold(
                onSuccess = { user ->
                    _currentUser.value = user
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: return@launch
            _loading.value = true
            repository.updateUserData(userId, user).fold(
                onSuccess = { 
                    _currentUser.value = user
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

enum class AuthState {
    AUTHENTICATED,
    UNAUTHENTICATED
}
