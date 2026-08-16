package com.taskflow.app.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val TEST_DB_NAME = "migration-test"

/**
 * Valida que `MIGRATION_1_2` preserva dados existentes ao adicionar `sortOrder` em
 * `categories` — exatamente o tipo de teste que o comentário em [ALL_MIGRATIONS]
 * pedia desde o início do projeto: sem isto, uma migration com SQL incorreto só
 * seria descoberta em produção, no aparelho de um usuário real.
 *
 * Roda como teste instrumentado (não Robolectric): `MigrationTestHelper` lê o schema
 * exportado (`room.schemaLocation`) como asset do APK de teste — sob Robolectric, o
 * `test_config.properties` gerado pelo AGP aponta para os assets da variante `debug`
 * normal, não para os do source set `androidTest` (ver `sourceSets` no build.gradle.kts).
 */
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun `migracao 1 para 2 preserva categorias existentes e calcula sortOrder alfabetico`() {
        helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL("INSERT INTO categories (id, name, colorHex, icon) VALUES (1, 'Zebra', '#000000', 'icon')")
            execSQL("INSERT INTO categories (id, name, colorHex, icon) VALUES (2, 'Alfa', '#000000', 'icon')")
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        migratedDb.query("SELECT name, sortOrder FROM categories ORDER BY sortOrder ASC").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Alfa", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))

            assertTrue(cursor.moveToNext())
            assertEquals("Zebra", cursor.getString(0))
            assertEquals(1, cursor.getInt(1))
        }
    }
}
