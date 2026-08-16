# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keepclassmembers class * {
    public <init>(...);
}

# WorkManager
-keep class androidx.work.impl.** { *; }

# Gson (usado pelo Retrofit para desserializar a resposta da Open-Meteo). Gson
# resolve campos por reflection a partir do nome — sem manter os campos dos DTOs,
# o R8 pode renomear/remover algum e a resposta chega com tudo nulo, silenciosamente
# (nenhum erro de build acusa isso).
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.gson.**
-keep class com.taskflow.app.data.remote.dto.** { <fields>; }

# Retrofit + OkHttp: já trazem regras de consumidor embutidas no próprio AAR (é
# raro precisar de algo manual aqui), mas deixamos explícitas as recomendações
# oficiais do Square como segunda camada de proteção — não custa nada e evita
# depender só de "confiar que o merge das regras do AAR funcionou".
-keepattributes Exceptions
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep,allowobfuscation interface com.taskflow.app.data.remote.OpenMeteoForecastApi
-keep,allowobfuscation interface com.taskflow.app.data.remote.OpenMeteoGeocodingApi
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowshrinking,allowobfuscation class kotlin.coroutines.Continuation
