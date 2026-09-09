# Manette - Troubleshooting Guide

Common issues and solutions for the Virtual Gamepad System.

## Table of Contents

1. [Server Issues](#server-issues)
2. [Android Client Issues](#android-client-issues)
3. [Connection Issues](#connection-issues)
4. [Performance Issues](#performance-issues)
5. [Platform-Specific Issues](#platform-specific-issues)

---

## Server Issues

### Server won't start

**Symptoms:**
- Error message when running `python main.py`
- Command exits immediately
- No output in console

**Solutions:**

1. **Check Python version**
   ```bash
   python --version
   ```
   Must be 3.10 or higher. If not, install from python.org.

2. **Install missing dependencies**
   ```bash
   pip install -r requirements.txt
   ```

3. **Check for port conflicts**
   - Close other applications using ports 8888-8890
   - Change ports in `server/config/server_config.json`

4. **Run as Administrator**
   - Right-click Command Prompt
   - Run as Administrator
   - Navigate to server directory
   - Run `python main.py`

5. **Check antivirus/firewall**
   - Add Python to antivirus exceptions
   - Allow Python through Windows Firewall
   - Temporarily disable to test

### ViGEm Bus driver issues

**Symptoms:**
- Gamepad not recognized by games
- Error message about ViGEm
- "Gamepad emulator initialized successfully" not shown

**Solutions:**

1. **Reinstall ViGEm Bus**
   - Download latest version from [GitHub](https://github.com/ViGEm/ViGEmBus/releases)
   - Uninstall existing version
   - Run installer as Administrator
   - Restart computer

2. **Check Device Manager**
   - Press Win+X, select Device Manager
   - Look for "ViGEm Bus Device"
   - If missing, reinstall driver
   - If yellow exclamation mark, reinstall driver

3. **Verify installation**
   ```bash
   # In Python, test vgamepad
   python -c "import vgamepad; print('vgamepad OK')"
   ```

### Server crashes on connection

**Symptoms:**
- Server starts but crashes when client connects
- Error traceback in console
- Server stops responding

**Solutions:**

1. **Check server logs**
   - Look for specific error messages
   - Note the line number and error type

2. **Test with single client**
   - Disconnect all other clients
   - Try connecting with one device only

3. **Update dependencies**
   ```bash
   pip install --upgrade -r requirements.txt
   ```

4. **Check config file**
   - Verify JSON is valid
   - Check for syntax errors
   - Use JSON validator if needed

---

## Android Client Issues

### App won't install

**Symptoms:**
- Installation fails
- "Parse error" message
- "App not installed" message

**Solutions:**

1. **Enable installation from unknown sources**
   - Settings → Security → Unknown sources
   - Enable for your browser/file manager

2. **Check Android version**
   - Requires Android 7.0 (Nougat) or higher
   - Check in Settings → About Phone

3. **Clear storage**
   - Settings → Apps → Manette → Clear storage
   - Try installing again

4. **Download APK again**
   - Previous download may be corrupted
   - Re-download from source

### Permissions denied

**Symptoms:**
- App crashes on startup
- Permission request dialogs
- Features not working

**Solutions:**

1. **Grant all permissions manually**
   - Settings → Apps → Manette → Permissions
   - Grant: Internet, Bluetooth, Nearby Devices, Vibration, Body Sensors

2. **For Android 12+**
   - "Nearby devices" permission is required
   - Grant in Settings → Privacy → Permission manager → Nearby devices

3. **Restart app after granting permissions**
   - Force close the app
   - Reopen to apply permissions

### App crashes on startup

**Symptoms:**
- App closes immediately after opening
- "Unfortunately, Manette has stopped" message

**Solutions:**

1. **Clear app data**
   - Settings → Apps → Manette → Storage → Clear data
   - Restart app

2. **Reinstall app**
   - Uninstall completely
   - Reinstall APK

3. **Check device compatibility**
   - Requires Android 7.0+
   - Some older devices may not be supported

4. **Check logs**
   - Enable USB debugging
   - Run: `adb logcat`
   - Look for crash messages

---

## Connection Issues

### Cannot connect to server

**Symptoms:**
- "Connection failed" message
- Timeout error
- Server not reachable

**Solutions:**

1. **Verify server is running**
   - Check server console for errors
   - Ensure server hasn't crashed
   - Restart server if needed

2. **Check IP address**
   - Verify correct PC IP address
   - Use `ipconfig` on Windows
   - Ensure both devices on same network

3. **Check firewall**
   - Windows Firewall may block connection
   - Add exception for Python
   - Temporarily disable to test

4. **Try different connection type**
   - UDP → WebSocket
   - WebSocket → Bluetooth
   - Try USB if available

### Connection drops frequently

**Symptoms:**
- Connected but disconnects randomly
- Have to reconnect often
- Intermittent connection

**Solutions:**

1. **Check network stability**
   - Test Wi-Fi signal strength
   - Move closer to router
   - Use 5GHz if available

2. **Increase timeout in config**
   - Edit `server/config/server_config.json`
   - Increase `timeout` value
   - Restart server

3. **Use USB connection**
   - Most stable connection
   - Zero latency
   - Requires ADB setup

4. **Check power settings**
   - Prevent phone from sleeping
   - Keep screen on during use
   - Disable battery optimization

### Bluetooth connection fails

**Symptoms:**
- Cannot pair with PC
- Bluetooth not working
- Connection refused

**Solutions:**

1. **Enable Bluetooth on both devices**
   - Ensure Bluetooth is enabled on PC
   - Ensure Bluetooth is enabled on Android
   - Restart Bluetooth if needed

2. **Pair devices first**
   - Go to Bluetooth settings
   - Scan for devices
   - Pair before using app

3. **Check Bluetooth permissions**
   - Grant Bluetooth permissions in app
   - For Android 12+, grant "Nearby devices"
   - Restart app after granting

4. **Restart Bluetooth**
   - Turn Bluetooth off and on
   - Restart both devices
   - Try again

### USB connection not working

**Symptoms:**
- USB connection fails
- ADB not recognized
- Port forwarding fails

**Solutions:**

1. **Enable USB debugging**
   - Settings → Developer Options → USB debugging
   - Accept debugging prompt on phone
   - Try different USB cable

2. **Install ADB**
   - Download Android Platform Tools
   - Add to system PATH
   - Restart Command Prompt

3. **Verify ADB connection**
   ```bash
   adb devices
   ```
   Should show your device.

4. **Set up port forwarding manually**
   ```bash
   adb forward tcp:8890 tcp:8890
   ```

5. **Run ADB as Administrator**
   - Right-click Command Prompt
   - Run as Administrator
   - Try ADB commands again

---

## Performance Issues

### High latency

**Symptoms:**
- Noticeable delay between input and response
- Latency indicator shows high values
- Unresponsive controls

**Solutions:**

1. **Use UDP instead of WebSocket**
   - UDP has lower latency
   - Change in app settings
   - Reconnect after changing

2. **Use USB connection**
   - Zero latency
   - Best for competitive gaming
   - Requires ADB setup

3. **Improve network quality**
   - Use 5GHz Wi-Fi
   - Move closer to router
   - Reduce network congestion

4. **Close background apps**
   - Free up CPU on Android
   - Close unnecessary apps
   - Restart device if needed

### Input lag

**Symptoms:**
- Delayed response to button presses
- Joystick movement feels sluggish
- Unresponsive controls

**Solutions:**

1. **Adjust sensitivity**
   - Increase joystick sensitivity
   - Reduce deadzone
   - Test different values

2. **Reduce polling interval**
   - Edit server config if needed
   - Lower timeout values
   - Restart server

3. **Use wired connection**
   - USB is fastest
   - Avoid wireless if possible
   - Use quality USB cable

4. **Check device performance**
   - Close other apps
   - Restart Android device
   - Free up memory

### Haptic feedback not working

**Symptoms:**
- No vibration on phone
- Haptic feedback enabled but not working
- Server sends vibration but no response

**Solutions:**

1. **Enable vibration in app**
   - Settings → Features → Haptic Feedback
   - Ensure toggle is on

2. **Check device vibration motor**
   - Test vibration in other apps
   - Some devices don't have vibration
   - Check if vibration is disabled in system

3. **Enable vibration in server**
   - Edit `server/config/server_config.json`
   - Set `vibration_enabled: true`
   - Restart server

4. **Check vibration permissions**
   - Settings → Apps → Manette → Permissions
   - Grant Vibration permission

---

## Platform-Specific Issues

### Android 12+ Bluetooth restrictions

**Symptoms:**
- Bluetooth not working on Android 12+
- "Nearby devices" permission error
- Cannot scan for devices

**Solutions:**

1. **Grant "Nearby devices" permission**
   - Settings → Privacy → Permission manager → Nearby devices
   - Allow Manette

2. **Use alternative connection**
   - Try Wi-Fi (UDP/WebSocket)
   - Use USB if possible
   - Use "The Great" mode instead of "Plug & Play"

3. **Check manufacturer restrictions**
   - Some manufacturers restrict Bluetooth HID
   - Check device documentation
   - Contact manufacturer if needed

### Windows 11 compatibility

**Symptoms:**
- ViGEm driver not installing
- Server not working on Windows 11
- Compatibility warnings

**Solutions:**

1. **Use latest ViGEm version**
   - Download from GitHub releases
   - Ensure Windows 11 compatible version
   - Run installer as Administrator

2. **Disable driver signature enforcement**
   - Restart in Advanced Startup
   - Disable driver signature enforcement
   - Install ViGEm driver
   - Restart normally

3. **Use compatibility mode**
   - Right-click installer
   - Properties → Compatibility
   - Run in Windows 10 mode

### iOS compatibility

**Note:** Manette is currently Android-only. iOS is not supported due to:
- Bluetooth HID restrictions on iOS
- App Store restrictions
- Different sensor APIs

**Alternatives:**
- Use Android device
- Use dedicated iOS gamepad apps
- Use physical controller

---

## Getting Help

If you're still experiencing issues:

1. **Check logs**
   - Server: Check console output
   - Android: Use `adb logcat`

2. **Provide information**
   - Android version
   - Windows version
   - Python version
   - Error messages
   - Steps to reproduce

3. **Search known issues**
   - Check GitHub issues
   - Search forums
   - Read documentation

4. **Report bugs**
   - Create GitHub issue
   - Include detailed information
   - Attach logs if possible
