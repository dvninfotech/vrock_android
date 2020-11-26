package com.vrockk.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class MyExecutors {
    companion object {
        suspend fun executeOnIO(runnable: Runnable) {
            withContext(Dispatchers.IO) {
                runnable.run()
            }
        }

        suspend fun executeOnPool(runnable: Runnable) {
            withContext(Dispatchers.Default) {
                runnable.run()
            }
        }

        suspend fun executeOnMain(runnable: Runnable) {
            withContext(Dispatchers.Main) {
                runnable.run()
            }
        }
    }
}