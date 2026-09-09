"""
Keyboard/Mouse Handler - Hybrid keyboard and mouse input mode
"""

import asyncio
import logging
from typing import Optional
from dataclasses import dataclass

try:
    import pyautogui
except ImportError:
    pyautogui = None
    logging.warning("pyautogui not installed. Keyboard/mouse mode will not work.")

logger = logging.getLogger(__name__)


@dataclass
class KeyMapping:
    """Mapping from gamepad button to keyboard key."""
    button: str
    key: str


@dataclass
class MouseMapping:
    """Mapping from joystick to mouse movement."""
    joystick: str
    sensitivity: float


class KeyboardMouseHandler:
    """Handles keyboard and mouse input for games without gamepad support."""
    
    def __init__(self, config: dict):
        self.config = config
        self.enabled = config.get('keyboard_mouse', {}).get('enabled', True)
        self.sensitivity = config.get('keyboard_mouse', {}).get('sensitivity', 1.0)
        self._initialized = False
        
        # Default key mappings (Xbox 360 layout)
        self.key_mappings = {
            'a': 'z',  # Often used for confirm
            'b': 'x',  # Often used for cancel
            'x': 'c',
            'y': 'v',
            'start': 'enter',
            'back': 'escape',
            'left_bumper': 'q',
            'right_bumper': 'e',
            'dpad_up': 'up',
            'dpad_down': 'down',
            'dpad_left': 'left',
            'dpad_right': 'right',
        }
        
        # Mouse mappings
        self.mouse_mappings = {
            'left_stick': {'x': True, 'y': True},  # Both axes
            'right_stick': {'x': False, 'y': False},  # Disabled by default
        }
        
        if pyautogui:
            pyautogui.FAILSAFE = False  # Disable failsafe for game use
    
    async def initialize(self):
        """Initialize the keyboard/mouse handler."""
        if pyautogui is None:
            logger.error("pyautogui library not available. Cannot initialize keyboard/mouse handler.")
            return
        
        self._initialized = True
        logger.info("Keyboard/Mouse handler initialized")
    
    async def cleanup(self):
        """Clean up keyboard/mouse resources."""
        if pyautogui and self._initialized:
            try:
                # Release all mapped keys
                for key in self.key_mappings.values():
                    try:
                        pyautogui.keyUp(key)
                    except Exception:
                        pass
                logger.info("Keyboard/Mouse handler cleaned up")
            except Exception as e:
                logger.error(f"Error during cleanup: {e}")
        
        self._initialized = False
    
    async def handle_button_press(self, button: str, pressed: bool):
        """Handle a button press/release."""
        if not self.enabled or not self._initialized:
            return
        
        if button not in self.key_mappings:
            return
        
        key = self.key_mappings[button]
        
        try:
            if pressed:
                pyautogui.keyDown(key)
            else:
                pyautogui.keyUp(key)
        except Exception as e:
            logger.error(f"Error handling button {button}: {e}")
    
    async def handle_joystick_movement(self, joystick: str, x: float, y: float):
        """Handle joystick movement as mouse movement."""
        if not self.enabled or not self._initialized:
            return
        
        if joystick not in self.mouse_mappings:
            return
        
        mapping = self.mouse_mappings[joystick]
        
        try:
            # Calculate mouse movement based on joystick position and sensitivity
            move_x = 0
            move_y = 0
            
            if mapping['x']:
                move_x = int(x * 10 * self.sensitivity)
            if mapping['y']:
                move_y = int(y * 10 * self.sensitivity)
            
            if move_x != 0 or move_y != 0:
                pyautogui.moveRel(move_x, move_y)
                
        except Exception as e:
            logger.error(f"Error handling joystick {joystick}: {e}")
    
    async def handle_trigger(self, trigger: str, value: float):
        """Handle trigger input (could be mapped to mouse scroll or keys)."""
        if not self.enabled or not self._initialized:
            return
        
        # Map left trigger to scroll down, right trigger to scroll up
        try:
            if trigger == 'left_trigger' and value > 0.5:
                pyautogui.scroll(-1)
            elif trigger == 'right_trigger' and value > 0.5:
                pyautogui.scroll(1)
        except Exception as e:
            logger.error(f"Error handling trigger {trigger}: {e}")
    
    def set_key_mapping(self, button: str, key: str):
        """Set a custom key mapping."""
        self.key_mappings[button] = key
        logger.info(f"Key mapping updated: {button} -> {key}")
    
    def set_mouse_mapping(self, joystick: str, x_enabled: bool, y_enabled: bool):
        """Set mouse mapping for a joystick."""
        self.mouse_mappings[joystick] = {'x': x_enabled, 'y': y_enabled}
        logger.info(f"Mouse mapping updated: {joystick} -> x={x_enabled}, y={y_enabled}")
    
    def set_sensitivity(self, sensitivity: float):
        """Set mouse sensitivity."""
        self.sensitivity = max(0.1, min(5.0, sensitivity))
        logger.info(f"Sensitivity set to {self.sensitivity}")
