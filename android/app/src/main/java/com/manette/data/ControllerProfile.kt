package com.manette.data

import com.google.gson.annotations.SerializedName
import com.manette.config.*

data class ControllerProfile(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("version")
    val version: Int = 1,
    
    @SerializedName("button_mappings")
    val buttonMappings: Map<String, String> = emptyMap(),
    
    @SerializedName("joystick_settings")
    val joystickSettings: Map<String, JoystickConfig> = emptyMap(),
    
    @SerializedName("sensitivity_settings")
    val sensitivitySettings: SensitivityConfig = SensitivityConfig(),
    
    @SerializedName("deadzone_settings")
    val deadzoneSettings: DeadzoneConfig = DeadzoneConfig(),
    
    @SerializedName("layout_config")
    val layoutConfig: LayoutConfig = LayoutConfig(),
    
    @SerializedName("gyro_enabled")
    val gyroEnabled: Boolean = false,
    
    @SerializedName("haptic_enabled")
    val hapticEnabled: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun copyWithUpdates(
        name: String? = null,
        buttonMappings: Map<String, String>? = null,
        joystickSettings: Map<String, JoystickConfig>? = null,
        sensitivitySettings: SensitivityConfig? = null,
        deadzoneSettings: DeadzoneConfig? = null,
        layoutConfig: LayoutConfig? = null,
        gyroEnabled: Boolean? = null,
        hapticEnabled: Boolean? = null
    ): ControllerProfile {
        return copy(
            name = name ?: this.name,
            buttonMappings = buttonMappings ?: this.buttonMappings,
            joystickSettings = joystickSettings ?: this.joystickSettings,
            sensitivitySettings = sensitivitySettings ?: this.sensitivitySettings,
            deadzoneSettings = deadzoneSettings ?: this.deadzoneSettings,
            layoutConfig = layoutConfig ?: this.layoutConfig,
            gyroEnabled = gyroEnabled ?: this.gyroEnabled,
            hapticEnabled = hapticEnabled ?: this.hapticEnabled,
            updatedAt = System.currentTimeMillis()
        )
    }
}
