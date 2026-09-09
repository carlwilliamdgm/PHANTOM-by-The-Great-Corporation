"""
Gamepad Emulator - Handles gamepad emulation using vgamepad
"""

import asyncio
import logging
from typing import Optional
from dataclasses import dataclass

try:
    import vgamepad as vg
except ImportError:
    vg = None
    logging.warning("vgamepad not installed. Gamepad emulation will not work.")

logger = logging.getLogger(__name__)


@dataclass
class GamepadState:
    """Represents the state of a gamepad."""
    # Buttons (Xbox 360 layout)
    a: bool = False
    b: bool = False
    x: bool = False
    y: bool = False
    left_bumper: bool = False
    right_bumper: bool = False
    left_trigger: float = 0.0  # 0.0 to 1.0
    right_trigger: float = 0.0  # 0.0 to 1.0
    back: bool = False
    start: bool = False
    left_thumb: bool = False
    right_thumb: bool = False
    
    # D-pad
    dpad_up: bool = False
    dpad_down: bool = False
    dpad_left: bool = False
    dpad_right: bool = False
    
    # Analog sticks
    left_stick_x: float = 0.0  # -1.0 to 1.0
    left_stick_y: float = 0.0  # -1.0 to 1.0
    right_stick_x: float = 0.0  # -1.0 to 1.0
    right_stick_y: float = 0.0  # -1.0 to 1.0


class GamepadEmulator:
    """Emulates a virtual gamepad using vgamepad."""
    
    def __init__(self, config: dict):
        self.config = config
        self.gamepad: Optional[vg.VX360Gamepad] = None
        self.current_state = GamepadState()
        self.deadzone_left = config.get('gamepad', {}).get('deadzone_left', 0.1)
        self.deadzone_right = config.get('gamepad', {}).get('deadzone_right', 0.1)
        self.vibration_enabled = config.get('gamepad', {}).get('vibration_enabled', True)
        self._initialized = False
        
    async def initialize(self):
        """Initialize the gamepad emulator."""
        if vg is None:
            logger.error("vgamepad library not available. Cannot initialize gamepad.")
            return
        
        try:
            emulation_type = self.config.get('gamepad', {}).get('emulation_type', 'xbox360').lower()
            if emulation_type in ['dualshock4', 'ds4', 'ps4']:
                self.gamepad = vg.VDS4Gamepad()
                logger.info("DualShock 4 gamepad emulator initialized successfully")
            else:
                self.gamepad = vg.VX360Gamepad()
                logger.info("Xbox 360 gamepad emulator initialized successfully")
            self._initialized = True
        except Exception as e:
            logger.error(f"Failed to initialize gamepad emulator: {e}")
            self._initialized = False
    
    async def cleanup(self):
        """Clean up gamepad resources."""
        if self.gamepad and self._initialized:
            try:
                # Reset all buttons and sticks
                self._reset_gamepad()
                self.gamepad.update()
                logger.info("Gamepad emulator cleaned up")
            except Exception as e:
                logger.error(f"Error during cleanup: {e}")
        
        self._initialized = False
    
    def _reset_gamepad(self):
        """Reset all gamepad inputs to default state."""
        if not self.gamepad:
            return
            
        # Reset buttons
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_A)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_B)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_X)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_Y)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_SHOULDER)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_SHOULDER)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_BACK)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_START)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_THUMB)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_THUMB)
        
        # Reset D-pad
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT)
        self.gamepad.reset_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT)
        
        # Reset triggers
        self.gamepad.left_trigger(0)
        self.gamepad.right_trigger(0)
        
        # Reset sticks
        self.gamepad.left_joystick(0, 0)
        self.gamepad.right_joystick(0, 0)
    
    def _apply_deadzone(self, value: float, deadzone: float) -> float:
        """Apply deadzone to analog input."""
        if abs(value) < deadzone:
            return 0.0
        # Scale the remaining range
        scale = 1.0 / (1.0 - deadzone)
        return (value - (deadzone if value > 0 else -deadzone)) * scale
    
    async def update_state(self, state: GamepadState):
        """Update the gamepad state from input data."""
        if not self.gamepad or not self._initialized:
            logger.warning("Gamepad not initialized, cannot update state")
            return
        
        try:
            # Update buttons
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_A, state.a)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_B, state.b)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_X, state.x)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_Y, state.y)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_SHOULDER, state.left_bumper)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_SHOULDER, state.right_bumper)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_BACK, state.back)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_START, state.start)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_LEFT_THUMB, state.left_thumb)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_RIGHT_THUMB, state.right_thumb)
            
            # Update D-pad
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_UP, state.dpad_up)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_DOWN, state.dpad_down)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_LEFT, state.dpad_left)
            self._update_button(vg.XUSB_BUTTON.XUSB_GAMEPAD_DPAD_RIGHT, state.dpad_right)
            
            # Update triggers (0-255)
            self.gamepad.left_trigger(int(state.left_trigger * 255))
            self.gamepad.right_trigger(int(state.right_trigger * 255))
            
            # Update analog sticks with deadzone
            left_x = self._apply_deadzone(state.left_stick_x, self.deadzone_left)
            left_y = self._apply_deadzone(state.left_stick_y, self.deadzone_left)
            right_x = self._apply_deadzone(state.right_stick_x, self.deadzone_right)
            right_y = self._apply_deadzone(state.right_stick_y, self.deadzone_right)
            
            self.gamepad.left_joystick(int(left_x * 32767), int(left_y * 32767))
            self.gamepad.right_joystick(int(right_x * 32767), int(right_y * 32767))
            
            # Apply changes
            self.gamepad.update()
            self.current_state = state
            
        except Exception as e:
            logger.error(f"Error updating gamepad state: {e}")
    
    def _update_button(self, button, pressed: bool):
        """Update a single button state."""
        if pressed:
            self.gamepad.press_button(button)
        else:
            self.gamepad.release_button(button)
    
    async def set_vibration(self, left_motor: float, right_motor: float):
        """Set vibration intensity (0.0 to 1.0)."""
        if not self.gamepad or not self._initialized or not self.vibration_enabled:
            return
        
        try:
            # vgamepad doesn't directly support vibration, but we can log it
            # for future implementation with ViGEmBus directly
            logger.debug(f"Vibration: left={left_motor}, right={right_motor}")
        except Exception as e:
            logger.error(f"Error setting vibration: {e}")
    
    def get_current_state(self) -> GamepadState:
        """Get the current gamepad state."""
        return self.current_state
