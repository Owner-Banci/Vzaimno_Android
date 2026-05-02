package com.vzaimno.app.feature.chats

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ReportGmailerrorred
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.components.VzaimnoBottomSheetColumn
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.feature.shell.navigation.HideShellBottomBarEffect

@Composable
fun ChatThreadRoute(
    onBack: () -> Unit,
    isSupportEntry: Boolean,
    viewModel: ChatThreadViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(isSupportEntry) {
        viewModel.configure(isSupportEntry)
        viewModel.onAppear()
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onDisappear()
        }
    }

    ChatThreadScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onComposerTextChanged = viewModel::updateComposerText,
        onImagePicked = viewModel::onImagePicked,
        onRemovePendingImage = viewModel::removePendingImage,
        onSend = viewModel::sendMessage,
        onOpenReport = viewModel::showReportSheet,
        onDismissReport = viewModel::dismissReportSheet,
        onReportReasonSelected = viewModel::selectReportReason,
        onReportCommentChanged = viewModel::updateReportComment,
        onSubmitReport = viewModel::submitReport,
        onOpenReview = viewModel::showReviewDialog,
        onDismissReview = viewModel::dismissReviewDialog,
        onReviewStarsChanged = viewModel::updateReviewStars,
        onReviewCommentChanged = viewModel::updateReviewComment,
        onSubmitReview = viewModel::submitReview,
        onOpenDispute = viewModel::showOpenDisputeSheet,
        onDismissOpenDispute = viewModel::dismissOpenDisputeSheet,
        onOpenDisputeTitleChanged = viewModel::updateOpenDisputeTitle,
        onOpenDisputeDescriptionChanged = viewModel::updateOpenDisputeDescription,
        onOpenDisputeCompensationChanged = viewModel::updateOpenDisputeCompensation,
        onOpenDisputeResolutionChanged = viewModel::updateOpenDisputeResolution,
        onSubmitOpenDispute = viewModel::submitOpenDispute,
        onRespondAsCounterparty = viewModel::showCounterpartyDisputeSheet,
        onDismissCounterpartyDispute = viewModel::dismissCounterpartyDisputeSheet,
        onCounterpartyAcceptModeChanged = viewModel::updateCounterpartyAcceptMode,
        onCounterpartyResponseChanged = viewModel::updateCounterpartyResponse,
        onCounterpartyRefundPercentChanged = viewModel::updateCounterpartyRefundPercent,
        onCounterpartyResolutionChanged = viewModel::updateCounterpartyResolution,
        onAcceptCounterpartyTerms = viewModel::acceptCounterpartyTerms,
        onSubmitCounterpartyResponse = viewModel::submitCounterpartyResponse,
        onShowDisputeOptionDetail = viewModel::showDisputeOptionDetail,
        onDismissDisputeOptionDetail = viewModel::dismissDisputeOptionDetail,
        onConfirmDisputeOption = viewModel::confirmDisputeOption,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun ChatThreadScreen(
    state: ChatThreadUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onComposerTextChanged: (String) -> Unit,
    onImagePicked: (Uri?) -> Unit,
    onRemovePendingImage: () -> Unit,
    onSend: () -> Unit,
    onOpenReport: () -> Unit,
    onDismissReport: () -> Unit,
    onReportReasonSelected: (String) -> Unit,
    onReportCommentChanged: (String) -> Unit,
    onSubmitReport: () -> Unit,
    onOpenReview: () -> Unit,
    onDismissReview: () -> Unit,
    onReviewStarsChanged: (Int) -> Unit,
    onReviewCommentChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    onOpenDispute: () -> Unit,
    onDismissOpenDispute: () -> Unit,
    onOpenDisputeTitleChanged: (String) -> Unit,
    onOpenDisputeDescriptionChanged: (String) -> Unit,
    onOpenDisputeCompensationChanged: (String) -> Unit,
    onOpenDisputeResolutionChanged: (DisputeResolutionKind) -> Unit,
    onSubmitOpenDispute: () -> Unit,
    onRespondAsCounterparty: () -> Unit,
    onDismissCounterpartyDispute: () -> Unit,
    onCounterpartyAcceptModeChanged: (Boolean) -> Unit,
    onCounterpartyResponseChanged: (String) -> Unit,
    onCounterpartyRefundPercentChanged: (String) -> Unit,
    onCounterpartyResolutionChanged: (DisputeResolutionKind) -> Unit,
    onAcceptCounterpartyTerms: () -> Unit,
    onSubmitCounterpartyResponse: () -> Unit,
    onShowDisputeOptionDetail: (String) -> Unit,
    onDismissDisputeOptionDetail: () -> Unit,
    onConfirmDisputeOption: () -> Unit,
) {
    HideShellBottomBarEffect(reason = "chat_thread")

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )
    val messages = state.messagesState.messages
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.messagesState.isRefreshing,
        onRefresh = onRefresh,
    )
    val isNearBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= (layoutInfo.totalItemsCount - 3).coerceAtLeast(0)
        }
    }
    var previousMessageCount by remember { mutableIntStateOf(messages.size) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onImagePicked,
    )

    LaunchedEffect(messages.size) {
        if (messages.isEmpty()) return@LaunchedEffect
        val lastMessage = messages.last()
        if (messages.size != previousMessageCount && (isNearBottom || lastMessage.isCurrentUser)) {
            listState.animateScrollToItem(messages.lastIndex)
        }
        previousMessageCount = messages.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.messagesState.header.title.isNotBlank()) {
                            state.messagesState.header.title
                        } else {
                            stringResource(R.string.chats_thread_title)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.chats_back),
                        )
                    }
                },
                actions = {
                    if (!state.messagesState.isInitialLoading) {
                        if (state.canShowOpenDisputeAction) {
                            IconButton(onClick = onOpenDispute) {
                                Icon(
                                    imageVector = Icons.Outlined.ReportGmailerrorred,
                                    contentDescription = "Открыть спор",
                                )
                            }
                        }
                        IconButton(
                            onClick = onOpenReport,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = stringResource(R.string.chats_report_action),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                    )
                    .pullRefresh(pullRefreshState),
            ) {
                when {
                    state.supportThreadState.isSupportEntry && state.supportThreadState.isResolving -> {
                        ConversationStatusCard(
                            title = stringResource(R.string.chats_support_resolving_title),
                            message = stringResource(R.string.chats_support_resolving_message),
                            showProgress = true,
                        )
                    }

                    state.messagesState.isInitialLoading -> {
                        ConversationStatusCard(
                            title = stringResource(R.string.chats_thread_loading_title),
                            message = stringResource(R.string.chats_thread_loading_message),
                            showProgress = true,
                        )
                    }

                    state.messagesState.errorMessage != null && messages.isEmpty() -> {
                        ConversationStatusCard(
                            title = stringResource(R.string.chats_thread_error_title),
                            message = state.messagesState.errorMessage,
                            showProgress = false,
                            primaryAction = {
                                Button(onClick = onRetry) {
                                    Text(text = stringResource(R.string.chats_retry))
                                }
                            },
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = MaterialTheme.spacing.large,
                                top = MaterialTheme.spacing.small,
                                end = MaterialTheme.spacing.large,
                                bottom = 24.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        ) {
                            item {
                                ConversationHeaderCard(
                                    header = state.messagesState.header,
                                    transportMessage = state.transportState.statusMessage,
                                    reviewMessage = state.reviewState.errorMessage,
                                )
                            }

                            if (state.messagesState.kind != ChatConversationKind.Support &&
                                (state.disputeState.hasActiveDispute ||
                                    state.disputeState.shouldShowThinkingState ||
                                    state.canShowOpenDisputeAction)
                            ) {
                                item {
                                    DisputePanel(
                                        state = state.disputeState,
                                        onOpenDispute = onOpenDispute,
                                        onRespondAsCounterparty = onRespondAsCounterparty,
                                        onOptionSelected = onShowDisputeOptionDetail,
                                        canOpenDispute = state.canShowOpenDisputeAction &&
                                            !state.disputeState.hasActiveDispute,
                                    )
                                }
                            }

                            if (messages.isEmpty()) {
                                item {
                                    ConversationStatusCard(
                                        title = stringResource(R.string.chats_thread_empty_title),
                                        message = stringResource(R.string.chats_thread_empty_message),
                                        showProgress = false,
                                    )
                                }
                            } else {
                                itemsIndexed(
                                    items = messages,
                                    key = { _, message -> message.id },
                                ) { index, message ->
                                    val previousEpochSeconds = messages.getOrNull(index - 1)?.createdAtEpochSeconds
                                    if (shouldShowDateDivider(previousEpochSeconds, message.createdAtEpochSeconds)) {
                                        DateDivider(
                                            title = formatDateDivider(message.createdAtEpochSeconds),
                                        )
                                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                                    }

                                    val showTap = shouldShowCounterpartyTapTargetUi(
                                        dispute = state.disputeState.activeDispute,
                                        message = message,
                                        canRespondAsCounterparty = state.disputeState.canRespondAsCounterparty,
                                    )
                                    MessageBubble(
                                        message = message,
                                        onSystemMessageTap = if (showTap) onRespondAsCounterparty else null,
                                        onImageClick = { imageUrl -> selectedImageUrl = imageUrl },
                                    )
                                }
                            }

                            if (state.reviewState.hasVisibleCard) {
                                item {
                                    ReviewEligibilityCard(
                                        state = state.reviewState,
                                        onOpenReview = onOpenReview,
                                    )
                                }
                            }

                            if (!state.reportState.successMessage.isNullOrBlank()) {
                                item {
                                    InlineInfoCard(
                                        message = state.reportState.successMessage,
                                    )
                                }
                            }

                            if (!state.reviewState.successMessage.isNullOrBlank()) {
                                item {
                                    InlineInfoCard(
                                        message = state.reviewState.successMessage,
                                    )
                                }
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = state.messagesState.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }

            ComposerBar(
                value = state.composerText,
                pendingImage = state.pendingImage,
                uploadStatusMessage = state.imageUploadStatusMessage,
                onValueChange = onComposerTextChanged,
                onPickImage = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onRemoveImage = onRemovePendingImage,
                onSend = onSend,
                enabled = !state.messagesState.isInitialLoading && !state.supportThreadState.isResolving,
                canAttachImage = state.messagesState.kind != ChatConversationKind.Support,
                isSending = state.isSending,
            )
        }
    }

    selectedImageUrl?.let { imageUrl ->
        PhotoViewerDialog(
            imageUrl = imageUrl,
            onDismiss = { selectedImageUrl = null },
        )
    }

    if (state.reportState.isSheetVisible) {
        ReportBottomSheet(
            state = state.reportState,
            onDismiss = onDismissReport,
            onReasonSelected = onReportReasonSelected,
            onCommentChanged = onReportCommentChanged,
            onSubmit = onSubmitReport,
        )
    }

    if (state.reviewState.isDialogVisible) {
        ReviewBottomSheet(
            state = state.reviewState,
            onDismiss = onDismissReview,
            onStarsChanged = onReviewStarsChanged,
            onCommentChanged = onReviewCommentChanged,
            onSubmit = onSubmitReview,
        )
    }

    if (state.disputeState.openForm.isVisible) {
        OpenDisputeBottomSheet(
            state = state.disputeState,
            onDismiss = onDismissOpenDispute,
            onTitleChanged = onOpenDisputeTitleChanged,
            onDescriptionChanged = onOpenDisputeDescriptionChanged,
            onCompensationChanged = onOpenDisputeCompensationChanged,
            onResolutionSelected = onOpenDisputeResolutionChanged,
            onSubmit = onSubmitOpenDispute,
        )
    }

    if (state.disputeState.counterpartyForm.isVisible) {
        CounterpartyDisputeBottomSheet(
            state = state.disputeState,
            onDismiss = onDismissCounterpartyDispute,
            onAcceptModeSelected = onCounterpartyAcceptModeChanged,
            onResponseChanged = onCounterpartyResponseChanged,
            onRefundPercentChanged = onCounterpartyRefundPercentChanged,
            onResolutionSelected = onCounterpartyResolutionChanged,
            onAccept = onAcceptCounterpartyTerms,
            onSubmitCounter = onSubmitCounterpartyResponse,
        )
    }

    if (state.disputeState.optionDetail.isVisible) {
        DisputeOptionDetailBottomSheet(
            state = state.disputeState,
            onDismiss = onDismissDisputeOptionDetail,
            onConfirm = onConfirmDisputeOption,
        )
    }
}

@Composable
private fun ConversationHeaderCard(
    header: ChatThreadHeaderUi,
    transportMessage: String?,
    reviewMessage: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThreadAvatar(
                    title = header.title,
                    avatarUrl = header.avatarUrl,
                    fallback = header.avatarFallback,
                    isSupport = header.isSupport,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = header.title.ifBlank { stringResource(R.string.chats_thread_title) },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    header.announcementTitle?.takeIf(String::isNotBlank)?.let { announcementTitle ->
                        Text(
                            text = announcementTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            transportMessage?.let { message ->
                InlineInfoCard(message = message)
            }
            reviewMessage?.let { message ->
                InlineInfoCard(message = message)
            }
        }
    }
}

@Composable
private fun ConversationStatusCard(
    title: String,
    message: String,
    showProgress: Boolean,
    primaryAction: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.xLarge),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                if (showProgress) {
                    CircularProgressIndicator()
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                primaryAction?.invoke()
            }
        }
    }
}

@Composable
private fun DateDivider(
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessageUi,
    onSystemMessageTap: (() -> Unit)? = null,
    onImageClick: (String) -> Unit = {},
) {
    val alignment = when {
        message.isSystem -> Alignment.CenterHorizontally
        message.isCurrentUser -> Alignment.End
        else -> Alignment.Start
    }
    val bubbleColor = when {
        message.isSystem -> MaterialTheme.colorScheme.secondaryContainer
        message.isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        message.isSystem -> MaterialTheme.colorScheme.onSecondaryContainer
        message.isCurrentUser -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            modifier = if (onSystemMessageTap != null) {
                Modifier.clickable(onClick = onSystemMessageTap)
            } else {
                Modifier
            },
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = if (message.isCurrentUser) 24.dp else 10.dp,
                bottomEnd = if (message.isCurrentUser) 10.dp else 24.dp,
            ),
            color = bubbleColor,
            tonalElevation = if (message.isSystem) 0.dp else 1.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(if (message.isSystem) 1f else 0.88f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                message.mediaUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onImageClick(imageUrl) },
                        contentScale = ContentScale.Crop,
                    )
                }
                if (message.text.isNotBlank() && !(message.mediaUrl != null && message.text == "Фото")) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                    )
                }
                Text(
                    text = message.timeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.72f),
                )
                if (onSystemMessageTap != null) {
                    Text(
                        text = "Нажмите, чтобы ответить",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    var scale by remember(imageUrl) { mutableStateOf(1f) }
    var offset by remember(imageUrl) { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f)),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 72.dp, horizontal = 12.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    )
                    .pointerInput(imageUrl) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nextScale = (scale * zoom).coerceIn(1f, 5f)
                            scale = nextScale
                            offset = if (nextScale == 1f) {
                                Offset.Zero
                            } else {
                                offset + pan
                            }
                        }
                    },
                contentScale = ContentScale.Fit,
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                scale = (scale / 1.25f).coerceAtLeast(1f)
                                if (scale == 1f) offset = Offset.Zero
                            }
                            .padding(top = 8.dp),
                        text = "-",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { scale = (scale * 1.25f).coerceAtMost(5f) }
                            .padding(top = 8.dp),
                        text = "+",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Закрыть фото",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewEligibilityCard(
    state: ChatReviewUiState,
    onOpenReview: () -> Unit,
) {
    val eligibility = state.eligibility ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Text(
                text = if (eligibility.alreadySubmitted) {
                    stringResource(R.string.chats_review_sent_title)
                } else {
                    stringResource(R.string.chats_review_ready_title)
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = eligibility.message?.takeIf(String::isNotBlank)
                    ?: if (eligibility.alreadySubmitted) {
                        stringResource(R.string.chats_review_sent_message)
                    } else {
                        stringResource(R.string.chats_review_ready_message)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (eligibility.canSubmit) {
                Button(onClick = onOpenReview) {
                    Text(text = stringResource(R.string.chats_review_action))
                }
            }
        }
    }
}

@Composable
private fun ComposerBar(
    value: String,
    pendingImage: PendingChatImageUi?,
    uploadStatusMessage: String?,
    onValueChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    canAttachImage: Boolean,
    isSending: Boolean,
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = MaterialTheme.spacing.large)
                .padding(top = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            pendingImage?.let { image ->
                PendingImagePreview(
                    image = image,
                    onRemove = onRemoveImage,
                )
            }
            uploadStatusMessage?.takeIf(String::isNotBlank)?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (canAttachImage) {
                    IconButton(
                        modifier = Modifier.size(56.dp),
                        enabled = enabled && !isSending,
                        onClick = onPickImage,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Прикрепить фото",
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp),
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled && !isSending,
                    placeholder = {
                        Text(text = stringResource(R.string.chats_composer_placeholder))
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSend = {
                            if (enabled && !isSending && (value.trim().isNotBlank() || pendingImage != null)) {
                                onSend()
                            }
                        },
                    ),
                )

                val canSend = enabled && !isSending && (value.trim().isNotBlank() || pendingImage != null)
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = canSend,
                            onClick = onSend,
                        ),
                    shape = CircleShape,
                    color = if (canSend) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = stringResource(R.string.chats_send),
                                tint = if (canSend) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingImagePreview(
    image: PendingChatImageUi,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.small),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = Uri.parse(image.uriString),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = image.fileName ?: "Фото",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Убрать фото",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportBottomSheet(
    state: ChatReportUiState,
    onDismiss: () -> Unit,
    onReasonSelected: (String) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        VzaimnoBottomSheetColumn {
            Text(
                text = stringResource(R.string.chats_report_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            state.targetSummary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.isLoadingOptions) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = stringResource(R.string.chats_report_loading_reasons),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    state.options.forEach { option ->
                        val isSelected = state.selectedReasonCode == option.code
                        val isOtherOption = option.code.equals("other", ignoreCase = true) ||
                            option.title.equals("Другое", ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onReasonSelected(option.code) },
                            shape = RoundedCornerShape(22.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            tonalElevation = 1.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(MaterialTheme.spacing.large),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (isSelected && isOtherOption) {
                                    OutlinedTextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        value = state.comment,
                                        onValueChange = onCommentChanged,
                                        label = {
                                            Text(text = stringResource(R.string.chats_report_comment_label))
                                        },
                                        placeholder = {
                                            Text(text = stringResource(R.string.chats_report_comment_placeholder))
                                        },
                                        minLines = 3,
                                        maxLines = 4,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            state.errorMessage?.let { message ->
                InlineInfoCard(message = message)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                onClick = onSubmit,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = stringResource(R.string.chats_report_submit))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewBottomSheet(
    state: ChatReviewUiState,
    onDismiss: () -> Unit,
    onStarsChanged: (Int) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        VzaimnoBottomSheetColumn {
            Text(
                text = stringResource(R.string.chats_review_dialog_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.chats_review_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                (1..5).forEach { stars ->
                    Icon(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onStarsChanged(stars) },
                        imageVector = Icons.Rounded.Star,
                        contentDescription = stringResource(R.string.chats_review_stars_description, stars),
                        tint = if (stars <= state.selectedStars) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        },
                    )
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.comment,
                onValueChange = onCommentChanged,
                label = {
                    Text(text = stringResource(R.string.chats_review_comment_label))
                },
                placeholder = {
                    Text(text = stringResource(R.string.chats_review_comment_placeholder))
                },
                minLines = 3,
                maxLines = 4,
            )

            state.errorMessage?.let { message ->
                InlineInfoCard(message = message)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting,
                onClick = onSubmit,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = stringResource(R.string.chats_review_submit))
                }
            }
        }
    }
}

@Composable
private fun InlineInfoCard(
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f),
    ) {
        Text(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun ThreadAvatar(
    title: String,
    avatarUrl: String?,
    fallback: String,
    isSupport: Boolean,
) {
    val backgroundBrush = if (isSupport) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.primary,
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
            ),
        )
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = fallback,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
        }
    }
}
