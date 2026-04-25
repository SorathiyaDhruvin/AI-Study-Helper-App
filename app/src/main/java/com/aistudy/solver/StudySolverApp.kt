package com.aistudy.solver

import android.app.Application
import com.aistudy.solver.utils.CloudinaryManager

class StudySolverApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Cloudinary at app startup
        CloudinaryManager.init(this)
    }
}
