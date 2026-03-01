package com.block.goose.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.block.goose.data.model.Message
import com.block.goose.data.model.MessageContent
import com.block.goose.data.model.MessageRole
import com.block.goose.util.TimeUtil.formatTimestamp
import java.time.Instant

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onBookmark: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    showStatus: Boolean = false,
    status: String? = null
) {
    val isUser = message.role == MessageRole.USER
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    var showMenu by remember { mutableStateOf(false) }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }
    
    val bubbleColor = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 50.dp, max = 280.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { offset ->
                                menuPosition = offset
                                showMenu = true
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(bubbleColor)
                        .padding(12.dp)
                ) {
                    // Message content
                    message.content.forEach { content ->
                        when (content) {
                            is MessageContent.Text -> {
                                Text(
                                    text = content.text,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            is MessageContent.Thinking -> {
                                ThinkingContent(content.thinking)
                            }
                            is MessageContent.ToolRequest -> {
                                ToolRequestContent(content.toolCall)
                            }
                            is MessageContent.ToolResponse -> {
                                ToolResponseContent(content.toolResult)
                            }
                        }
                    }
                    
                    // Timestamp and status
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimestamp(message.created),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        
                        if (showStatus && status != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusIndicator(status)
                        }
                    }
                }
                
                // Context Menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(menuPosition.x.dp, menuPosition.y.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                        onClick = {
                            onCopy?.invoke()
                            showMenu = false
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text(if (isBookmarked) "Remove Bookmark" else "Bookmark") },
                        leadingIcon = { 
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, 
                                null
                            ) 
                        },
                        onClick = {
                            onBookmark?.invoke()
                            showMenu = false
                        }
                    )
                    
                    if (isUser && status == "failed") {
                        DropdownMenuItem(
                            text = { Text("Retry") },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) },
                            onClick = {
                                onRetry?.invoke()
                                showMenu = false
                            }
                        )
                    }
                    
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = {
                            onDelete?.invoke()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingContent(thinking: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .animateContentSize()
        ) {
            Text(
                text = "💭 Thinking...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (expanded) {
                Text(
                    text = thinking,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (expanded) "Show less" else "Show more")
            }
        }
    }
}

@Composable
private fun ToolRequestContent(toolCall: com.block.goose.data.model.ToolCall) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "🔧 Using: ${toolCall.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ToolResponseContent(toolResult: com.block.goose.data.model.ToolResult) {
    val isSuccess = toolResult.status == "success"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = if (isSuccess) "✓ Done" else "✗ Failed",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSuccess) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun StatusIndicator(status: String) {
    val (icon, color) = when (status.lowercase()) {
        "pending" -> "⏳" to MaterialTheme.colorScheme.outline
        "sending" -> "📤" to MaterialTheme.colorScheme.primary
        "sent" -> "✓" to MaterialTheme.colorScheme.primary
        "failed" -> "✗" to MaterialTheme.colorScheme.error
        "delivered" -> "✓✓" to MaterialTheme.colorScheme.primary
        else -> "" to Color.Transparent
    }
    
    Text(
        text = icon,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

@Composable
fun RetryFailedButton(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onRetry,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Retry Failed Message")
    }
}
