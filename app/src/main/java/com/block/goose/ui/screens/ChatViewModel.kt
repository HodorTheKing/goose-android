package com.block.goose.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.block.goose.data.api.GooseApiService
import com.block.goose.data.db.entity.MessageStatus
import com.block.goose.data.model.*
import com.block.goose.data.repository.BookmarkRepository
import com.block.goose.data.repository.MessageRepository
import com.block.goose.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingSession: Boolean = false,
    val isActivatingSession: Boolean = false,
    val currentSessionId: String? = null,
    val sessionName: String? = null,
    val isSessionActivated: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val showingRetryButton: Boolean = false,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val apiService: GooseApiService,
    private val messageRepository: MessageRepository,
    private val sessionRepository: SessionRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {
    private val TAG = "ChatViewModel"
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var streamJob: Job? = null
    private var currentSessionId: String? = null
    
    init {
        observeNetworkState()
    }
    
    private fun observeNetworkState() {
        // TODO: Add network state monitoring
    }
    
    fun startNewSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSession = true, error = null) }
            
            when (val result = apiService.startAgent()) {
                is com.block.goose.data.api.ApiResult.Success -> {
                    val sessionId = result.data.id
                    currentSessionId = sessionId
                    
                    // Save session to local database
                    val session = ChatSession(
                        id = sessionId,
                        description = "New Session",
                        messageCount = 0,
                        createdAt = java.time.Instant.now().toString(),
                        updatedAt = java.time.Instant.now().toString()
                    )
                    sessionRepository.insertSession(session)
                    
                    // Load conversation if exists
                    val messages = result.data.conversation ?: emptyList()
                    messages.forEach { messageRepository.insertMessage(it, sessionId) }
                    
                    _uiState.update { 
                        it.copy(
                            currentSessionId = sessionId,
                            messages = messages,
                            isLoadingSession = false,
                            isSessionActivated = false,
                            sessionName = "New Session"
                        )
                    }
                }
                is com.block.goose.data.api.ApiResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingSession = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSession = true, error = null) }
            currentSessionId = sessionId
            
            // First load from local database
            launch {
                messageRepository.getMessagesForSession(sessionId).collect { messages ->
                    _uiState.update { 
                        it.copy(
                            messages = messages,
                            isLoadingSession = false
                        )
                    }
                }
            }
            
            // Then fetch from server to update
            when (val result = apiService.resumeAgent(sessionId, loadModelAndExtensions = false)) {
                is com.block.goose.data.api.ApiResult.Success -> {
                    result.data.conversation?.let { messages ->
                        messageRepository.insertMessages(messages, sessionId)
                    }
                    
                    _uiState.update { 
                        it.copy(
                            currentSessionId = sessionId,
                            isLoadingSession = false,
                            isSessionActivated = false
                        )
                    }
                }
                is com.block.goose.data.api.ApiResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingSession = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun sendMessage(text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty() || _uiState.value.isLoading) return
        
        val sessionId = _uiState.value.currentSessionId
        
        if (sessionId == null) {
            startNewSessionAndSend(trimmedText)
        } else {
            sendMessageToSession(trimmedText, sessionId)
        }
    }
    
    private fun startNewSessionAndSend(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSession = true) }
            
            when (val result = apiService.startAgent()) {
                is com.block.goose.data.api.ApiResult.Success -> {
                    val sessionId = result.data.id
                    currentSessionId = sessionId
                    
                    val session = ChatSession(
                        id = sessionId,
                        description = "New Session",
                        messageCount = 0,
                        createdAt = java.time.Instant.now().toString(),
                        updatedAt = java.time.Instant.now().toString()
                    )
                    sessionRepository.insertSession(session)
                    
                    _uiState.update { 
                        it.copy(
                            currentSessionId = sessionId,
                            isLoadingSession = false,
                            isSessionActivated = false,
                            sessionName = "New Session"
                        )
                    }
                    
                    sendMessageToSession(text, sessionId)
                }
                is com.block.goose.data.api.ApiResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoadingSession = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    private fun sendMessageToSession(text: String, sessionId: String) {
        val userMessage = Message.user(text)
        
        viewModelScope.launch {
            // Save user message locally
            messageRepository.insertMessage(userMessage, sessionId, MessageStatus.SENDING)
        }
        
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true,
                error = null
            )
        }
        
        streamJob = viewModelScope.launch {
            try {
                // Activate session if needed
                if (!_uiState.value.isSessionActivated) {
                    _uiState.update { it.copy(isActivatingSession = true) }
                    
                    apiService.resumeAgent(sessionId, loadModelAndExtensions = true)
                    apiService.updateFromSession(sessionId)
                    
                    _uiState.update { 
                        it.copy(
                            isSessionActivated = true,
                            isActivatingSession = false
                        )
                    }
                }
                
                // Update user message status to sent
                messageRepository.updateMessageStatus(userMessage.id, MessageStatus.SENT)
                
                // Stream the chat
                val allMessages = _uiState.value.messages
                apiService.streamChat(allMessages, sessionId)
                    .catch { e ->
                        messageRepository.markMessageFailed(userMessage.id, e.message)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Stream failed",
                                showingRetryButton = true
                            )
                        }
                    }
                    .collect { event ->
                        handleSSEEvent(event, sessionId)
                    }
            } catch (e: Exception) {
                messageRepository.markMessageFailed(userMessage.id, e.message)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isActivatingSession = false,
                        error = e.message ?: "Failed to send message",
                        showingRetryButton = true
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun handleSSEEvent(event: SSEEvent, sessionId: String) {
        when (event) {
            is SSEEvent.MessageEvent -> {
                viewModelScope.launch {
                    messageRepository.insertMessage(event.message, sessionId, MessageStatus.SENT)
                }
            }
            is SSEEvent.ErrorEvent -> {
                _uiState.update { 
                    it.copy(error = event.error)
                }
            }
            is SSEEvent.FinishEvent -> {
                _uiState.update { it.copy(isLoading = false) }
            }
            is SSEEvent.UpdateConversationEvent -> {
                viewModelScope.launch {
                    messageRepository.insertMessages(event.conversation, sessionId)
                }
                _uiState.update { 
                    it.copy(messages = event.conversation)
                }
            }
            else -> {}
        }
    }
    
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            messageRepository.softDeleteMessage(messageId)
        }
    }
    
    fun retryFailedMessage() {
        _uiState.update { it.copy(showingRetryButton = false) }
        // TODO: Implement retry logic
    }
    
    fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        _uiState.update { it.copy(isLoading = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null, showingRetryButton = false) }
    }
    
    fun refreshMessages() {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                _uiState.update { it.copy(isRefreshing = true) }
                
                when (val result = apiService.resumeAgent(sessionId, loadModelAndExtensions = false)) {
                    is com.block.goose.data.api.ApiResult.Success -> {
                        result.data.conversation?.let { messages ->
                            messageRepository.insertMessages(messages, sessionId)
                        }
                    }
                    else -> {}
                }
                
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
    
    fun renameSession(newName: String) {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                sessionRepository.updateSessionDescription(sessionId, newName)
                _uiState.update { it.copy(sessionName = newName) }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        streamJob?.cancel()
    }
}
