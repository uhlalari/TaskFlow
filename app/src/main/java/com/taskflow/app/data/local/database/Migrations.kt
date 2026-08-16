package com.taskflow.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            UPDATE categories
            SET sortOrder = (
                SELECT COUNT(*) FROM categories AS other
                WHERE other.name < categories.name
            )
            """.trimIndent()
        )
    }
}

val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
