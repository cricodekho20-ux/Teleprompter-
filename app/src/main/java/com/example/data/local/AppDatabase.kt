package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.RecordedVideo
import com.example.data.model.Script
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Script::class, RecordedVideo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teleprompter_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialScripts(database.scriptDao())
                    }
                }
            }

            suspend fun populateInitialScripts(scriptDao: ScriptDao) {
                // Hindi Reel / News Sample
                val hindiText = """नमस्ते दोस्तों! आज हम बात करने वाले हैं 3 ऐसे सीक्रेट्स के बारे में जो आपकी प्रोडक्टिविटी को 10 गुना बढ़ा सकते हैं।

पहला नियम: अपने दिन की शुरुआत सबसे महत्वपूर्ण काम से करें। जब दिमाग ताजा होता है, तो बड़े फैसले आसानी से लिए जाते हैं।

दूसरा नियम: 50 मिनट काम और 10 मिनट का ब्रेक। इसे पोमोडोरो तकनीक कहते हैं, जिससे आपकी ऊर्जा दिन भर बनी रहती है।

और तीसरा सबसे खास नियम: मल्टीटास्किंग बंद करें। एक समय में सिर्फ एक काम पर 100% फोकस करें।

अगर आपको यह वीडियो पसंद आया तो इसे लाइक करें, शेयर करें और हमारे चैनल को फॉलो करना न भूलें। मिलते हैं अगले वीडियो में!"""
                
                val hindiScript = Script(
                    title = "Daily Productivity Tips (Hindi)",
                    content = hindiText,
                    wordCount = Script.calculateWordCount(hindiText),
                    estimatedSeconds = Script.calculateEstimatedSeconds(Script.calculateWordCount(hindiText)),
                    isSample = true
                )
                scriptDao.insertScript(hindiScript)

                // English Tech / Product Pitch Sample
                val englishText = """Hey everyone, welcome back to the channel!

Today I want to show you the ultimate mobile recording setup that saves me 5 hours every single week.

Whether you are filming YouTube shorts, Instagram reels, or business presentations, keeping your eyes locked onto the camera lens makes all the difference.

With a smart teleprompter overlay, you never have to memorize paragraphs or stare at notes off-screen.

Drop a comment below with your favorite video creation tip, hit that subscribe button, and let's jump right in!"""

                val englishScript = Script(
                    title = "60-Second Creator Hook (English)",
                    content = englishText,
                    wordCount = Script.calculateWordCount(englishText),
                    estimatedSeconds = Script.calculateEstimatedSeconds(Script.calculateWordCount(englishText)),
                    isSample = true
                )
                scriptDao.insertScript(englishScript)
            }
        }
    }
}
