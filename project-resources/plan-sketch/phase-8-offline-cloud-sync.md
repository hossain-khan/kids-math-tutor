# Phase 8: Offline-First & Cloud Sync

**Duration**: 2 weeks  
**Goal**: Work anywhere, sync everywhere  
**Status**: 🔴 Not Started

---

## Overview

This phase transforms the app into a true offline-first application with cloud backup and multi-device support. By the end:
1. All features work completely offline
2. Progress automatically syncs to Firebase Cloud when online
3. Users can restore their progress on any device
4. Conflict resolution handles edge cases gracefully
5. Sync status visible to users with clear indicators

**Key Principle**: Offline by design, online by benefit. Children should never lose progress, whether connected or not. The cloud enhances the experience but never blocks it.

---

## Features Breakdown

### 1. Complete Offline Support

#### Offline-First Architecture

```
User Action
    ↓
Local Database (Room)  ← Always the source of truth
    ↓
Sync Queue (pending operations)
    ↓
[Network Available?]
    ├─ Yes → Sync to Firebase
    └─ No → Queue for later
```

#### Sync Queue System

```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val entityType: SyncEntityType, // Session, Badge, Profile, etc.
    
    val entityId: String, // ID of the entity to sync
    
    val operation: SyncOperation, // CREATE, UPDATE, DELETE
    
    val payload: String, // JSON serialized entity
    
    val createdAt: Instant,
    
    val retryCount: Int = 0,
    
    val lastAttemptAt: Instant? = null,
    
    val status: SyncStatus = SyncStatus.PENDING
)

enum class SyncEntityType {
    PRACTICE_SESSION,
    GAME_SESSION,
    BADGE,
    USER_PROFILE,
    DAILY_STREAK
}

enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE
}

enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
```

#### Network State Monitoring

```kotlin
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    fun isCurrentlyOnline(): Boolean
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class NetworkMonitorImpl constructor(
    @ApplicationContext private val context: Context
) : NetworkMonitor {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
        as ConnectivityManager
    
    private val _isOnline = MutableStateFlow(checkOnlineStatus())
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    
    init {
        observeNetworkChanges()
    }
    
    private fun observeNetworkChanges() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
            }
            
            override fun onLost(network: Network) {
                _isOnline.value = checkOnlineStatus()
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }
    
    override fun isCurrentlyOnline(): Boolean {
        return checkOnlineStatus()
    }
    
    private fun checkOnlineStatus(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
```

---

### 2. Firebase Cloud Backup

#### Firebase Setup

**Firebase Services Used**:
- **Firebase Authentication**: Anonymous auth + Google Sign-In
- **Cloud Firestore**: Document storage for user data
- **Firebase Storage**: Not needed for Phase 8

#### Firestore Data Structure

```
users/{userId}
    ├─ profile
    │   ├─ name: String
    │   ├─ gradeLevel: String
    │   ├─ createdAt: Timestamp
    │   └─ lastSyncedAt: Timestamp
    │
    ├─ sessions/{sessionId}
    │   ├─ date: Timestamp
    │   ├─ problemsAttempted: Number
    │   ├─ correctAnswers: Number
    │   ├─ operation: String
    │   ├─ gradeLevel: String
    │   └─ duration: Number
    │
    ├─ gameSessions/{gameSessionId}
    │   ├─ gameId: String
    │   ├─ score: Number
    │   ├─ correctAnswers: Number
    │   ├─ totalAttempts: Number
    │   ├─ duration: Number
    │   ├─ gradeLevel: String
    │   └─ timestamp: Timestamp
    │
    ├─ badges/{badgeId}
    │   ├─ badgeType: String
    │   ├─ unlockedAt: Timestamp
    │   └─ category: String
    │
    └─ streaks
        ├─ currentStreak: Number
        ├─ longestStreak: Number
        ├─ lastPracticeDate: Timestamp
        └─ history: Array<Timestamp>
```

#### Firebase Authentication Service

```kotlin
interface FirebaseAuthService {
    val currentUserId: Flow<String?>
    suspend fun signInAnonymously(): Result<String>
    suspend fun signInWithGoogle(credential: AuthCredential): Result<String>
    suspend fun linkWithGoogle(credential: AuthCredential): Result<String>
    suspend fun signOut()
    fun isSignedIn(): Boolean
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class FirebaseAuthServiceImpl constructor(
    private val auth: FirebaseAuth = Firebase.auth
) : FirebaseAuthService {
    
    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)
    override val currentUserId: Flow<String?> = _currentUserId.asStateFlow()
    
    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserId.value = firebaseAuth.currentUser?.uid
        }
    }
    
    override suspend fun signInAnonymously(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInAnonymously().await()
            val userId = result.user?.uid ?: return@withContext Result.failure(
                Exception("No user ID after anonymous sign in")
            )
            Result.success(userId)
        } catch (e: Exception) {
            Timber.e(e, "Anonymous sign in failed")
            Result.failure(e)
        }
    }
    
    override suspend fun signInWithGoogle(credential: AuthCredential): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result = auth.signInWithCredential(credential).await()
                val userId = result.user?.uid ?: return@withContext Result.failure(
                    Exception("No user ID after Google sign in")
                )
                Result.success(userId)
            } catch (e: Exception) {
                Timber.e(e, "Google sign in failed")
                Result.failure(e)
            }
        }
    }
    
    override suspend fun linkWithGoogle(credential: AuthCredential): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: return@withContext Result.failure(
                    Exception("No current user to link")
                )
                val result = currentUser.linkWithCredential(credential).await()
                val userId = result.user?.uid ?: return@withContext Result.failure(
                    Exception("No user ID after linking")
                )
                Result.success(userId)
            } catch (e: Exception) {
                Timber.e(e, "Account linking failed")
                Result.failure(e)
            }
        }
    }
    
    override suspend fun signOut() {
        auth.signOut()
    }
    
    override fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }
}
```

#### Cloud Sync Service

```kotlin
interface CloudSyncService {
    suspend fun syncToCloud(): Result<Unit>
    suspend fun syncFromCloud(): Result<Unit>
    suspend fun forceSyncNow(): Result<Unit>
    fun getSyncStatus(): Flow<SyncStatusInfo>
}

data class SyncStatusInfo(
    val isSyncing: Boolean,
    val lastSyncTime: Instant?,
    val pendingItems: Int,
    val failedItems: Int,
    val lastError: String?
)

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class CloudSyncServiceImpl constructor(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val authService: FirebaseAuthService,
    private val syncQueueDao: SyncQueueDao,
    private val sessionRepository: SessionRepository,
    private val gameRepository: GameRepository,
    private val badgeRepository: BadgeRepository,
    private val userProfileRepository: UserProfileRepository,
    private val networkMonitor: NetworkMonitor
) : CloudSyncService {
    
    private val _syncStatus = MutableStateFlow(SyncStatusInfo(
        isSyncing = false,
        lastSyncTime = null,
        pendingItems = 0,
        failedItems = 0,
        lastError = null
    ))
    
    override fun getSyncStatus(): Flow<SyncStatusInfo> = _syncStatus.asStateFlow()
    
    override suspend fun syncToCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = authService.currentUserId.first() ?: return@withContext Result.failure(
                Exception("Not authenticated")
            )
            
            if (!networkMonitor.isCurrentlyOnline()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            
            // Get pending sync items
            val pendingItems = syncQueueDao.getPendingSyncItems()
            
            pendingItems.forEach { item ->
                try {
                    when (item.entityType) {
                        SyncEntityType.PRACTICE_SESSION -> syncPracticeSession(userId, item)
                        SyncEntityType.GAME_SESSION -> syncGameSession(userId, item)
                        SyncEntityType.BADGE -> syncBadge(userId, item)
                        SyncEntityType.USER_PROFILE -> syncUserProfile(userId, item)
                        SyncEntityType.DAILY_STREAK -> syncDailyStreak(userId, item)
                    }
                    
                    // Mark as completed
                    syncQueueDao.updateStatus(item.id, SyncStatus.COMPLETED)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync item ${item.id}")
                    syncQueueDao.updateStatus(
                        item.id,
                        SyncStatus.FAILED,
                        retryCount = item.retryCount + 1
                    )
                }
            }
            
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncTime = Clock.System.now(),
                pendingItems = syncQueueDao.getPendingCount(),
                failedItems = syncQueueDao.getFailedCount()
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sync to cloud failed")
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastError = e.message
            )
            Result.failure(e)
        }
    }
    
    override suspend fun syncFromCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = authService.currentUserId.first() ?: return@withContext Result.failure(
                Exception("Not authenticated")
            )
            
            if (!networkMonitor.isCurrentlyOnline()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            
            // Fetch user data from Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            if (!userDoc.exists()) {
                // No cloud data, this is the first device
                _syncStatus.value = _syncStatus.value.copy(isSyncing = false)
                return@withContext Result.success(Unit)
            }
            
            // Sync profile
            syncProfileFromCloud(userId)
            
            // Sync sessions
            syncSessionsFromCloud(userId)
            
            // Sync game sessions
            syncGameSessionsFromCloud(userId)
            
            // Sync badges
            syncBadgesFromCloud(userId)
            
            // Sync streaks
            syncStreaksFromCloud(userId)
            
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncTime = Clock.System.now()
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sync from cloud failed")
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastError = e.message
            )
            Result.failure(e)
        }
    }
    
    override suspend fun forceSyncNow(): Result<Unit> {
        return syncToCloud()
    }
    
    private suspend fun syncPracticeSession(userId: String, item: SyncQueueEntity) {
        val session = Json.decodeFromString<PracticeSession>(item.payload)
        
        firestore.collection("users")
            .document(userId)
            .collection("sessions")
            .document(session.id.toString())
            .set(session.toFirestoreMap())
            .await()
    }
    
    private suspend fun syncGameSession(userId: String, item: SyncQueueEntity) {
        val session = Json.decodeFromString<GameSession>(item.payload)
        
        firestore.collection("users")
            .document(userId)
            .collection("gameSessions")
            .document(session.id.toString())
            .set(session.toFirestoreMap())
            .await()
    }
    
    private suspend fun syncBadge(userId: String, item: SyncQueueEntity) {
        val badge = Json.decodeFromString<Badge>(item.payload)
        
        firestore.collection("users")
            .document(userId)
            .collection("badges")
            .document(badge.badgeType.id)
            .set(badge.toFirestoreMap())
            .await()
    }
    
    private suspend fun syncUserProfile(userId: String, item: SyncQueueEntity) {
        val profile = Json.decodeFromString<UserProfile>(item.payload)
        
        firestore.collection("users")
            .document(userId)
            .collection("profile")
            .document("main")
            .set(profile.toFirestoreMap())
            .await()
    }
    
    private suspend fun syncDailyStreak(userId: String, item: SyncQueueEntity) {
        val streak = Json.decodeFromString<DailyStreak>(item.payload)
        
        firestore.collection("users")
            .document(userId)
            .collection("streaks")
            .document("main")
            .set(streak.toFirestoreMap())
            .await()
    }
    
    private suspend fun syncProfileFromCloud(userId: String) {
        val profileDoc = firestore.collection("users")
            .document(userId)
            .collection("profile")
            .document("main")
            .get()
            .await()
        
        if (profileDoc.exists()) {
            val profile = profileDoc.toUserProfile()
            userProfileRepository.saveProfile(profile)
        }
    }
    
    private suspend fun syncSessionsFromCloud(userId: String) {
        val sessions = firestore.collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .await()
        
        sessions.documents.forEach { doc ->
            val session = doc.toPracticeSession()
            // Check if exists locally, use conflict resolution
            sessionRepository.saveSessionFromCloud(session)
        }
    }
    
    private suspend fun syncGameSessionsFromCloud(userId: String) {
        val gameSessions = firestore.collection("users")
            .document(userId)
            .collection("gameSessions")
            .get()
            .await()
        
        gameSessions.documents.forEach { doc ->
            val session = doc.toGameSession()
            gameRepository.saveSessionFromCloud(session)
        }
    }
    
    private suspend fun syncBadgesFromCloud(userId: String) {
        val badges = firestore.collection("users")
            .document(userId)
            .collection("badges")
            .get()
            .await()
        
        badges.documents.forEach { doc ->
            val badge = doc.toBadge()
            badgeRepository.saveBadgeFromCloud(badge)
        }
    }
    
    private suspend fun syncStreaksFromCloud(userId: String) {
        val streakDoc = firestore.collection("users")
            .document(userId)
            .collection("streaks")
            .document("main")
            .get()
            .await()
        
        if (streakDoc.exists()) {
            val streak = streakDoc.toDailyStreak()
            // Use conflict resolution for streaks
            badgeRepository.syncStreakFromCloud(streak)
        }
    }
}
```

---

### 3. Conflict Resolution

#### Conflict Resolution Strategy

**Last-Write-Wins (LWW)**: Use timestamps to determine which version is newer

```kotlin
interface ConflictResolver {
    fun <T : Timestamped> resolve(local: T?, remote: T?): T?
}

interface Timestamped {
    val timestamp: Instant
}

@Inject
class LastWriteWinsResolver : ConflictResolver {
    override fun <T : Timestamped> resolve(local: T?, remote: T?): T? {
        return when {
            local == null -> remote
            remote == null -> local
            remote.timestamp > local.timestamp -> remote
            else -> local
        }
    }
}
```

#### Conflict Handling Examples

```kotlin
// For practice sessions - keep both if different days
suspend fun mergeSessions(
    localSessions: List<PracticeSession>,
    remoteSessions: List<PracticeSession>
): List<PracticeSession> {
    val mergedMap = mutableMapOf<String, PracticeSession>()
    
    // Add all local sessions
    localSessions.forEach { session ->
        mergedMap[session.id.toString()] = session
    }
    
    // Merge remote sessions
    remoteSessions.forEach { remote ->
        val local = mergedMap[remote.id.toString()]
        
        if (local == null) {
            // Remote is new, add it
            mergedMap[remote.id.toString()] = remote
        } else {
            // Conflict: use last-write-wins
            val resolved = conflictResolver.resolve(local, remote)
            if (resolved != null) {
                mergedMap[remote.id.toString()] = resolved
            }
        }
    }
    
    return mergedMap.values.toList()
}

// For badges - union (if unlocked in either, it's unlocked)
suspend fun mergeBadges(
    localBadges: List<Badge>,
    remoteBadges: List<Badge>
): List<Badge> {
    val mergedMap = mutableMapOf<String, Badge>()
    
    localBadges.forEach { badge ->
        mergedMap[badge.badgeType.id] = badge
    }
    
    remoteBadges.forEach { badge ->
        val existing = mergedMap[badge.badgeType.id]
        if (existing == null || badge.unlockedAt < existing.unlockedAt) {
            // Keep earliest unlock time
            mergedMap[badge.badgeType.id] = badge
        }
    }
    
    return mergedMap.values.toList()
}

// For streaks - use the higher value
suspend fun mergeStreaks(
    local: DailyStreak?,
    remote: DailyStreak?
): DailyStreak? {
    return when {
        local == null -> remote
        remote == null -> local
        else -> DailyStreak(
            currentStreak = maxOf(local.currentStreak, remote.currentStreak),
            longestStreak = maxOf(local.longestStreak, remote.longestStreak),
            lastPracticeDate = maxOf(local.lastPracticeDate, remote.lastPracticeDate)
        )
    }
}
```

---

### 4. Background Sync with WorkManager

#### Sync Worker

```kotlin
@WorkerKey(SyncWorker::class)
@AssistedInject
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloudSyncService: CloudSyncService,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // Check if online
        if (!networkMonitor.isCurrentlyOnline()) {
            Timber.d("Not online, skipping sync")
            return Result.retry()
        }
        
        // Attempt sync
        return when (val result = cloudSyncService.syncToCloud()) {
            is kotlin.Result.Success -> {
                Timber.d("Sync successful")
                Result.success()
            }
            is kotlin.Result.Failure -> {
                Timber.e(result.exceptionOrNull(), "Sync failed")
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }
    
    companion object {
        const val WORK_NAME = "cloud_sync_work"
        
        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
```

#### Schedule Sync in Application

```kotlin
@Inject
lateinit var workManager: WorkManager

override fun onCreate() {
    super.onCreate()
    
    // Schedule periodic sync
    SyncWorker.schedule(workManager)
}
```

---

### 5. Sync UI Indicators

#### Sync Status Bar

```
┌─────────────────────────────────────┐
│  ☁️ Syncing... (12 items)           │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  ✓ Synced 2 minutes ago             │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  📴 Offline - 5 items pending       │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  ⚠️ Sync failed - Retry now?        │
└─────────────────────────────────────┘
```

#### Sync Status Component

```kotlin
@Composable
fun SyncStatusBar(
    syncStatus: SyncStatusInfo,
    isOnline: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = syncStatus.isSyncing || syncStatus.pendingItems > 0 || syncStatus.lastError != null,
        modifier = modifier
    ) {
        Surface(
            color = when {
                syncStatus.isSyncing -> MaterialTheme.colorScheme.primaryContainer
                syncStatus.lastError != null -> MaterialTheme.colorScheme.errorContainer
                !isOnline -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        syncStatus.isSyncing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Syncing... (${syncStatus.pendingItems} items)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        syncStatus.lastError != null -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sync failed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        !isOnline -> {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline - ${syncStatus.pendingItems} items pending",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            syncStatus.lastSyncTime?.let { lastSync ->
                                val timeAgo = Clock.System.now().minus(lastSync)
                                Text(
                                    text = "Synced ${timeAgo.toReadableString()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                if (syncStatus.lastError != null) {
                    TextButton(onClick = onRetryClick) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
```

---

### 6. Sign In with Google

#### Google Sign-In Screen

```
┌─────────────────────────────────────┐
│  [←]    Account Settings     [?]    │
├─────────────────────────────────────┤
│                                     │
│   Sync & Backup                     │
│                                     │
│   Currently signed in as:           │
│   Anonymous User                    │
│                                     │
│   ────────────────────────────      │
│                                     │
│   🔐 Sign in with Google            │
│                                     │
│   Benefits:                         │
│   • Sync across multiple devices    │
│   • Never lose your progress        │
│   • Automatic cloud backup          │
│                                     │
│   [Sign in with Google]             │
│                                     │
│   ────────────────────────────      │
│                                     │
│   Last synced: 5 minutes ago        │
│   Synced items: 145                 │
│                                     │
│   [Force Sync Now]                  │
│                                     │
└─────────────────────────────────────┘
```

#### AccountSettingsScreen (Circuit)

```kotlin
@Parcelize
data class AccountSettingsScreen() : Screen {
    data class State(
        val isSignedIn: Boolean,
        val isAnonymous: Boolean,
        val userEmail: String?,
        val syncStatus: SyncStatusInfo,
        val eventSink: (Event) -> Unit
    ) : CircuitUiState
    
    sealed interface Event : CircuitUiEvent {
        object SignInWithGoogle : Event
        object SignOut : Event
        object ForceSyncNow : Event
    }
}

@CircuitInject(AccountSettingsScreen::class, AppScope::class)
@Composable
fun AccountSettingsPresenter(
    @Assisted screen: AccountSettingsScreen,
    @Assisted navigator: Navigator,
    authService: FirebaseAuthService,
    cloudSyncService: CloudSyncService
): AccountSettingsScreen.State {
    
    val userId by authService.currentUserId.collectAsState(initial = null)
    val syncStatus by cloudSyncService.getSyncStatus().collectAsState(
        initial = SyncStatusInfo(false, null, 0, 0, null)
    )
    
    val coroutineScope = rememberCoroutineScope()
    
    return AccountSettingsScreen.State(
        isSignedIn = userId != null,
        isAnonymous = Firebase.auth.currentUser?.isAnonymous == true,
        userEmail = Firebase.auth.currentUser?.email,
        syncStatus = syncStatus
    ) { event ->
        when (event) {
            is AccountSettingsScreen.Event.SignInWithGoogle -> {
                coroutineScope.launch {
                    // Trigger Google Sign-In flow
                    // This requires Google Sign-In library integration
                }
            }
            is AccountSettingsScreen.Event.SignOut -> {
                coroutineScope.launch {
                    authService.signOut()
                }
            }
            is AccountSettingsScreen.Event.ForceSyncNow -> {
                coroutineScope.launch {
                    cloudSyncService.forceSyncNow()
                }
            }
        }
    }
}
```

---

### 7. First-Time Sync Flow

#### Initial Sync Dialog

```
┌─────────────────────────────────────┐
│                                     │
│   Welcome Back! 👋                  │
│                                     │
│   We found your progress in         │
│   the cloud.                        │
│                                     │
│   • 50 practice sessions            │
│   • 8 badges unlocked               │
│   • 7-day streak                    │
│                                     │
│   Restore from cloud?               │
│                                     │
│   [Start Fresh]  [Restore]          │
│                                     │
└─────────────────────────────────────┘
```

#### First-Time Sync Logic

```kotlin
suspend fun handleFirstLaunch() {
    // Sign in anonymously
    val userId = authService.signInAnonymously().getOrNull()
        ?: return
    
    // Check if cloud data exists
    val cloudDataExists = checkCloudDataExists(userId)
    
    if (cloudDataExists) {
        // Show restore dialog
        showRestoreDialog()
    } else {
        // This is truly the first device
        // Sync local data to cloud
        cloudSyncService.syncToCloud()
    }
}

private suspend fun checkCloudDataExists(userId: String): Boolean {
    val userDoc = firestore.collection("users")
        .document(userId)
        .get()
        .await()
    
    return userDoc.exists()
}
```

---

## Technical Implementation

### Dependencies

#### Update gradle/libs.versions.toml

```toml
[versions]
firebase-bom = "34.7.0"
google-services = "4.4.2"
play-services-auth = "21.2.0"

[libraries]
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-auth = { module = "com.google.firebase:firebase-auth" }
firebase-firestore = { module = "com.google.firebase:firebase-firestore" }
play-services-auth = { module = "com.google.android.gms:play-services-auth", version.ref = "play-services-auth" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "google-services" }
```

#### Update app/build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    
    // Google Sign-In
    implementation(libs.play.services.auth)
    
    // WorkManager (already added)
    implementation(libs.androidx.work.runtime)
}
```

### Permissions

#### Update AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## Testing Strategy

### Unit Tests

#### Conflict Resolution Tests

```kotlin
@Test
fun `last write wins - remote is newer`() {
    val local = PracticeSession(
        timestamp = Instant.parse("2025-01-01T10:00:00Z"),
        // ... other fields
    )
    
    val remote = PracticeSession(
        timestamp = Instant.parse("2025-01-01T11:00:00Z"),
        // ... other fields
    )
    
    val result = conflictResolver.resolve(local, remote)
    
    assertEquals(remote, result)
}

@Test
fun `badge merge keeps earliest unlock time`() {
    val local = Badge(
        badgeType = BadgeType.PERFECT_10,
        unlockedAt = Instant.parse("2025-01-01T10:00:00Z")
    )
    
    val remote = Badge(
        badgeType = BadgeType.PERFECT_10,
        unlockedAt = Instant.parse("2025-01-01T09:00:00Z")
    )
    
    val result = mergeBadges(listOf(local), listOf(remote))
    
    assertEquals(1, result.size)
    assertEquals(remote.unlockedAt, result.first().unlockedAt)
}

@Test
fun `streak merge uses higher value`() {
    val local = DailyStreak(
        currentStreak = 5,
        longestStreak = 10,
        lastPracticeDate = LocalDate(2025, 1, 1)
    )
    
    val remote = DailyStreak(
        currentStreak = 7,
        longestStreak = 8,
        lastPracticeDate = LocalDate(2025, 1, 2)
    )
    
    val result = mergeStreaks(local, remote)
    
    assertEquals(7, result?.currentStreak)
    assertEquals(10, result?.longestStreak)
}
```

#### Network Monitor Tests

```kotlin
@Test
fun `network monitor detects online state`() = runTest {
    val networkMonitor = NetworkMonitorImpl(context)
    
    val isOnline = networkMonitor.isOnline.first()
    
    assertTrue(isOnline) // Assuming device is online
}
```

#### Sync Queue Tests

```kotlin
@Test
fun `sync queue adds pending items`() = runTest {
    val session = PracticeSession(/* ... */)
    
    syncQueueDao.addToQueue(
        entityType = SyncEntityType.PRACTICE_SESSION,
        entityId = session.id.toString(),
        payload = Json.encodeToString(session)
    )
    
    val pending = syncQueueDao.getPendingSyncItems()
    assertEquals(1, pending.size)
}

@Test
fun `completed items removed from queue`() = runTest {
    // Add item
    val itemId = syncQueueDao.addToQueue(/* ... */)
    
    // Mark completed
    syncQueueDao.updateStatus(itemId, SyncStatus.COMPLETED)
    
    // Should not appear in pending
    val pending = syncQueueDao.getPendingSyncItems()
    assertEquals(0, pending.size)
}
```

### Integration Tests

```kotlin
@Test
fun `full sync cycle - local to cloud to new device`() = runTest {
    // 1. Create local data
    val session = createPracticeSession()
    sessionRepository.saveSession(session)
    
    // 2. Sync to cloud
    cloudSyncService.syncToCloud()
    
    // 3. Clear local database
    clearDatabase()
    
    // 4. Sync from cloud
    cloudSyncService.syncFromCloud()
    
    // 5. Verify data restored
    val restored = sessionRepository.getSession(session.id).first()
    assertEquals(session, restored)
}
```

### Manual Testing Checklist

**Offline Support**
- [ ] App works completely offline
- [ ] All features accessible without internet
- [ ] Progress saves locally
- [ ] Sync queue accumulates pending items
- [ ] Network indicator shows offline state

**Cloud Sync**
- [ ] Anonymous sign-in on first launch
- [ ] Data syncs to Firestore
- [ ] Sync happens automatically when online
- [ ] Manual "Sync Now" works
- [ ] Sync status shows current state

**Multi-Device**
- [ ] Sign in on second device
- [ ] Progress restores from cloud
- [ ] Badges transfer correctly
- [ ] Streaks transfer correctly
- [ ] Sessions transfer correctly

**Conflict Resolution**
- [ ] Latest data wins for sessions
- [ ] Badges union (all badges kept)
- [ ] Streaks use highest value
- [ ] No data loss during conflicts

**Google Sign-In**
- [ ] Can link anonymous account with Google
- [ ] Email shows in account settings
- [ ] Sign out works
- [ ] Can sign in again

**Background Sync**
- [ ] WorkManager schedules sync
- [ ] Sync runs hourly when online
- [ ] Failed syncs retry
- [ ] Battery optimization doesn't break sync

---

## Migration Plan from Phase 7

### Step 1: Network Monitoring (Days 1-2)
1. Implement NetworkMonitor interface
2. Add connectivity change listener
3. Expose Flow<Boolean> for online state
4. Test network detection
5. Add UI indicator for offline mode

### Step 2: Sync Queue System (Days 3-4)
1. Create SyncQueueEntity and Dao
2. Implement queue management
3. Add methods to queue CRUD operations
4. Test queue persistence
5. Add retry logic for failed items

### Step 3: Firebase Setup (Days 5-6)
1. Add Firebase dependencies
2. Configure Firebase project
3. Set up Firebase Authentication
4. Set up Cloud Firestore
5. Implement FirebaseAuthService
6. Test anonymous authentication

### Step 4: Cloud Sync Service (Days 7-9)
1. Design Firestore data structure
2. Implement CloudSyncService
3. Add upload logic (local → cloud)
4. Add download logic (cloud → local)
5. Implement conflict resolution
6. Test sync operations

### Step 5: Background Sync (Days 10-11)
1. Create SyncWorker
2. Schedule periodic sync with WorkManager
3. Add network constraints
4. Test background sync
5. Handle sync failures

### Step 6: UI & Polish (Days 12-14)
1. Add sync status bar to home screen
2. Create account settings screen
3. Add Google Sign-In UI
4. Implement first-time restore dialog
5. Add manual "Sync Now" button
6. End-to-end testing
7. CHANGELOG.md update

---

## Success Metrics

- ✅ App fully functional offline
- ✅ Data syncs reliably to Firebase
- ✅ Multi-device sync works correctly
- ✅ Conflict resolution prevents data loss
- ✅ Sync happens automatically in background
- ✅ Users can restore progress on new device
- ✅ Google Sign-In works smoothly
- ✅ Sync status always visible and accurate

---

## Definition of Done

- ✅ Complete offline support for all features
- ✅ Sync queue implemented with retry logic
- ✅ Firebase Authentication (anonymous + Google)
- ✅ Cloud Firestore sync working
- ✅ Conflict resolution implemented
- ✅ WorkManager background sync scheduled
- ✅ Network state monitoring
- ✅ Sync status UI indicators
- ✅ Account settings screen
- ✅ Google Sign-In integration
- ✅ First-time restore dialog
- ✅ Multi-device testing completed
- ✅ All tests passing
- ✅ CHANGELOG.md updated
- ✅ No data loss in any scenario

---

## Privacy & Security Considerations

### Data Privacy
- Anonymous authentication by default
- Google Sign-In is optional
- Users control when data syncs
- Clear privacy policy about cloud storage

### Security
- All data transmitted over HTTPS
- Firebase security rules configured
- User data isolated per userId
- No sensitive information stored in cloud

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## Performance Considerations

### Sync Optimization
- Batch uploads for efficiency
- Incremental sync (only changed items)
- Compress large payloads
- Limit retry attempts to prevent battery drain

### Firestore Costs
- Minimize document reads
- Use caching where possible
- Batch operations
- Monitor usage in Firebase Console

### Battery Impact
- Sync only when charging (optional setting)
- Exponential backoff for retries
- Cancel sync on low battery

---

*Document created: December 16, 2025*  
*Phase status: 🔴 Not Started*  
*Target completion: Week 17 (after Phase 7)*
