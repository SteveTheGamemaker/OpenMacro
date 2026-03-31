package com.openmacro.core.engine.action

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.SetWallpaperConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SetWallpaperHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "set_wallpaper"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<SetWallpaperConfig>(config.configJson)
        val wallpaperManager = WallpaperManager.getInstance(context.androidContext)
        val bitmap = BitmapFactory.decodeFile(parsed.imagePath) ?: return

        when (parsed.target) {
            "lock" -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            else -> wallpaperManager.setBitmap(bitmap)
        }

        bitmap.recycle()
    }
}
