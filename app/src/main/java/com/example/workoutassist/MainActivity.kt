package com.example.workoutassist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.workoutassist.ui.WorkoutAssistApp
import com.example.workoutassist.ui.theme.WorkoutAssistTheme

class MainActivity : ComponentActivity() {
    private var openBackupSettingsRequested by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openBackupSettingsRequested = consumeOpenBackupSettingsExtra(intent)
        setContent {
            WorkoutAssistTheme {
                WorkoutAssistApp(
                    openBackupSettingsSignal = openBackupSettingsRequested,
                    onOpenBackupSettingsHandled = { openBackupSettingsRequested = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (consumeOpenBackupSettingsExtra(intent)) {
            openBackupSettingsRequested = true
        }
    }

    private fun consumeOpenBackupSettingsExtra(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA_OPEN_BACKUP_SETTINGS, false) == true

    companion object {
        private const val EXTRA_OPEN_BACKUP_SETTINGS = "open_backup_settings"

        /** Deep-links into Settings > Backup & Restore, used by the weekly backup reminder. */
        fun newBackupSettingsIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_OPEN_BACKUP_SETTINGS, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
