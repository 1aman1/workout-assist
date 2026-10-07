package com.example.workoutassist.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.workoutassist.data.WorkoutDatabase
import com.example.workoutassist.data.WorkoutRepository
import com.example.workoutassist.ui.KEY_AUTO_BACKUP_DIR_URI
import com.example.workoutassist.ui.KEY_SCHEDULE_TITLE
import com.example.workoutassist.ui.PREFS_NAME
import com.example.workoutassist.ui.exportBackupToUri

internal const val AUTO_BACKUP_FILE_NAME = "workout-assist-auto-backup.json"

/** Fires weekly (see [AutoBackupScheduler]) to silently export a backup into the user-chosen folder, overwriting the previous one. */
internal class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val treeUriString = prefs.getString(KEY_AUTO_BACKUP_DIR_URI, null) ?: return Result.success()
        val scheduleTitle = prefs.getString(KEY_SCHEDULE_TITLE, "") ?: ""

        return try {
            val treeUri = Uri.parse(treeUriString)
            val repository = WorkoutRepository(WorkoutDatabase.getInstance(applicationContext).workoutDao())
            val fileUri = resolveOrCreateBackupFileUri(applicationContext, treeUri)
            exportBackupToUri(
                context = applicationContext,
                repository = repository,
                scheduleTitle = scheduleTitle,
                outputUri = fileUri
            )
            Result.success()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    private fun resolveOrCreateBackupFileUri(context: Context, treeUri: Uri): Uri {
        val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val resolver = context.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId)
        resolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == AUTO_BACKUP_FILE_NAME) {
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(idIndex))
                }
            }
        }
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)
        return DocumentsContract.createDocument(resolver, parentUri, "application/json", AUTO_BACKUP_FILE_NAME)
            ?: error("Unable to create backup file in the selected folder")
    }
}
