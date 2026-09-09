package com.manette.config

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.manette.data.ControllerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ProfileManager(private val context: Context) {
    
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val profilesDir = File(context.filesDir, "profiles")
    
    companion object {
        private const val TAG = "ProfileManager"
        private const val DEFAULT_PROFILE_NAME = "default_profile.json"
    }
    
    init {
        if (!profilesDir.exists()) {
            profilesDir.mkdirs()
        }
    }
    
    suspend fun saveProfile(profile: ControllerProfile, filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(profilesDir, filename)
            val writer = FileWriter(file)
            gson.toJson(profile, writer)
            writer.close()
            Log.d(TAG, "Profile saved to $filename")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile: ${e.message}")
            false
        }
    }
    
    suspend fun loadProfile(filename: String): ControllerProfile? = withContext(Dispatchers.IO) {
        try {
            val file = File(profilesDir, filename)
            if (!file.exists()) {
                Log.w(TAG, "Profile file not found: $filename")
                return@withContext null
            }
            
            val reader = FileReader(file)
            val profile = gson.fromJson(reader, ControllerProfile::class.java)
            reader.close()
            Log.d(TAG, "Profile loaded from $filename")
            profile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load profile: ${e.message}")
            null
        }
    }
    
    suspend fun deleteProfile(filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(profilesDir, filename)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Profile deleted: $filename")
                true
            } else {
                Log.w(TAG, "Profile file not found: $filename")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete profile: ${e.message}")
            false
        }
    }
    
    suspend fun listProfiles(): List<String> = withContext(Dispatchers.IO) {
        try {
            profilesDir.listFiles()?.map { it.name }?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list profiles: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun exportProfile(profile: ControllerProfile, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(destinationPath)
            val writer = FileWriter(file)
            gson.toJson(profile, writer)
            writer.close()
            Log.d(TAG, "Profile exported to $destinationPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export profile: ${e.message}")
            false
        }
    }
    
    suspend fun importProfile(sourcePath: String): ControllerProfile? = withContext(Dispatchers.IO) {
        try {
            val file = File(sourcePath)
            if (!file.exists()) {
                Log.w(TAG, "Profile file not found: $sourcePath")
                return@withContext null
            }
            
            val reader = FileReader(file)
            val profile = gson.fromJson(reader, ControllerProfile::class.java)
            reader.close()
            Log.d(TAG, "Profile imported from $sourcePath")
            profile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import profile: ${e.message}")
            null
        }
    }
    
    suspend fun saveDefaultProfile(profile: ControllerProfile): Boolean {
        return saveProfile(profile, DEFAULT_PROFILE_NAME)
    }
    
    suspend fun loadDefaultProfile(): ControllerProfile? {
        return loadProfile(DEFAULT_PROFILE_NAME)
    }
    
    suspend fun createDefaultProfileIfNotExists(): ControllerProfile {
        val existing = loadDefaultProfile()
        if (existing != null) {
            return existing
        }
        
        val defaultProfile = ControllerProfile(
            name = "Default",
            version = 1,
            buttonMappings = getDefaultButtonMappings(),
            joystickSettings = getDefaultJoystickSettings(),
            sensitivitySettings = getDefaultSensitivitySettings(),
            deadzoneSettings = getDefaultDeadzoneSettings(),
            layoutConfig = getDefaultLayoutConfig()
        )
        
        saveDefaultProfile(defaultProfile)
        return defaultProfile
    }
    
    private fun getDefaultButtonMappings(): Map<String, String> {
        return mapOf(
            "a" to "button_a",
            "b" to "button_b",
            "x" to "button_x",
            "y" to "button_y",
            "left_bumper" to "button_l1",
            "right_bumper" to "button_r1",
            "back" to "button_select",
            "start" to "button_start",
            "left_thumb" to "button_l3",
            "right_thumb" to "button_r3",
            "dpad_up" to "dpad_up",
            "dpad_down" to "dpad_down",
            "dpad_left" to "dpad_left",
            "dpad_right" to "dpad_right"
        )
    }
    
    private fun getDefaultJoystickSettings(): Map<String, JoystickConfig> {
        return mapOf(
            "left" to JoystickConfig(
                enabled = true,
                sensitivity = 1.0f,
                deadzone = 0.1f,
                invertX = false,
                invertY = false
            ),
            "right" to JoystickConfig(
                enabled = true,
                sensitivity = 1.0f,
                deadzone = 0.1f,
                invertX = false,
                invertY = false
            )
        )
    }
    
    private fun getDefaultSensitivitySettings(): SensitivityConfig {
        return SensitivityConfig(
            overall = 1.0f,
            joystick = 1.0f,
            trigger = 1.0f,
            gyro = 1.0f
        )
    }
    
    private fun getDefaultDeadzoneSettings(): DeadzoneConfig {
        return DeadzoneConfig(
            leftStick = 0.1f,
            rightStick = 0.1f,
            leftTrigger = 0.05f,
            rightTrigger = 0.05f
        )
    }
    
    private fun getDefaultLayoutConfig(): LayoutConfig {
        return LayoutConfig(
            buttonPositions = mapOf(
                "a" to ButtonPosition(x = 800f, y = 600f, size = 50f),
                "b" to ButtonPosition(x = 900f, y = 500f, size = 50f),
                "x" to ButtonPosition(x = 700f, y = 500f, size = 50f),
                "y" to ButtonPosition(x = 800f, y = 400f, size = 50f),
                "left_stick" to ButtonPosition(x = 200f, y = 600f, size = 100f),
                "right_stick" to ButtonPosition(x = 800f, y = 600f, size = 100f)
            ),
            backgroundPath = "",
            skin = "default"
        )
    }
}
