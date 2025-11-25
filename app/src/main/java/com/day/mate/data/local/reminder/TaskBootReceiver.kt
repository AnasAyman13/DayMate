package com.day.mate.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.day.mate.data.local.AppDatabase
import com.day.mate.data.repository.TodoRepository
import com.day.mate.ui.theme.screens.todo.TodoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val appContext = context.applicationContext

            // 🟦 1) نجيب الـ Database
            val db = AppDatabase.getInstance(appContext)

            // 🟦 2) نعمل Repository
            val repo = TodoRepository(
                db.todoDao(),
                db.categoryDao()
            )

            // 🟦 3) نعمل ViewModel يدويًا (مسموح هنا)
            val viewModel = TodoViewModel(repo)

            // 🟦 4) نرجّع نسجّل كل الريمايندرات
            CoroutineScope(Dispatchers.IO).launch {

                // مهم: لازم نجمع الـ TODOS من الـ Repository مباشرة
                repo.getAllTodos().collect { todosList ->
                    todosList.forEach { todo ->
                        if (todo.remindMe) {
                            viewModel.scheduleReminder(appContext, todo)
                        }
                    }
                }
            }
        }
    }
}
