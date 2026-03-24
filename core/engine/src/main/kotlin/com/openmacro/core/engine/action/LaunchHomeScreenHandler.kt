package com.openmacro.core.engine.action

import android.content.Intent
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import javax.inject.Inject

class LaunchHomeScreenHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "launch_home_screen"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.androidContext.startActivity(intent)
    }
}
