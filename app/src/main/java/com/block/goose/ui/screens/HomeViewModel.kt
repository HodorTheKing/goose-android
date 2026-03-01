package com.block.goose.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.block.goose.data.api.GooseApiService
import com.block.goose.data.model.ChatSession
import com.block.goose.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val sessions: List<ChatSession> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isTrialMode: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showArchived: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: GooseApiService,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val TAG = "HomeViewModel"
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadSessions()
        observeLocalSessions()
    }
    
    private fun observeLocalSessions() {
        viewModelScope.launch {
            sessionRepository.getAllActiveSessions()
                .collect { sessions ->
                    _uiState.update { it.copy(sessions = sessions) }
                }
        }
    }
    
    private fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Fetch from server
            when (val result = apiService.fetchSessions()) {
                is com.block.goose.data.api.ApiResult.Success -> {
                    val sessions = result.data
                    // Save to local database
                    sessions.forEach { session ->
                        sessionRepository.insertSession(session)
                    }
                    
                    _uiState.update {
                        it.copy(
                            sessions = sessions,
                            isLoading = false
                        )
                    }
                }
                is com.block.goose.data.api.ApiResult.Error -> {
                    // Don't show error, just use local cache
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
    
    fun refreshSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadSessions()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
    
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
    
    fun archiveSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.archiveSession(sessionId)
        }
    }
    
    fun pinSession(sessionId: String, isPinned: Boolean) {
        viewModelScope.launch {
            sessionRepository.togglePinSession(sessionId, isPinned)
        }
    }
    
    fun renameSession(sessionId: String, newName: String) {
        viewModelScope.launch {
            sessionRepository.updateSessionDescription(sessionId, newName)
        }
    }
    
    fun searchSessions(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isEmpty()) {
            loadSessions()
            return
        }
        
        viewModelScope.launch {
            val results = sessionRepository.searchSessions(query)
            _uiState.update { it.copy(sessions = results) }
        }
    }
    
    fun clearAllArchivedSessions() {
        viewModelScope.launch {
            sessionRepository.deleteAllArchivedSessions()
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
