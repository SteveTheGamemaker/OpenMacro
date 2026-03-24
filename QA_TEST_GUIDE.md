# OpenMacro QA Test Guide

This guide covers everything built in Milestones 2-5. Work through it section by section. For each test, note PASS/FAIL and any observations. If something crashes, grab the Logcat stack trace (`level:ERROR` filter).

Keep the Logcat open the whole time with `package:com.openmacro` filter so you catch any background errors.

---

## 0. Prerequisite Setup

Before testing, do a clean install:
1. Uninstall OpenMacro from your phone (clears old DB)
2. Build and install fresh from Android Studio
3. Open the app — you should see 4 bottom tabs: **Macros | Logs | Variables | Settings**

**Quick sanity check:**
- [ ] App opens without crashing
- [ ] All 4 tabs are tappable and show their screens
- [ ] Macros tab shows empty state
- [ ] Logs tab shows empty state
- [ ] Variables tab shows empty state

---

## 1. Macro CRUD (Create / Read / Update / Delete)

### 1a. Create a macro
1. Tap the FAB (+ button) on the Macros tab
2. Enter name: "Test Macro 1"
3. Tap Save (checkmark)
4. You should return to the macro list and see "Test Macro 1"

- [ ] Macro appears in list after save
- [ ] Macro name displays correctly

### 1b. Edit a macro
1. Tap "Test Macro 1" to open the editor
2. Change the name to "Renamed Macro"
3. Save

- [ ] Editor loads with correct name
- [ ] Name change persists after save

### 1c. Delete a macro
1. On the macro list, swipe "Renamed Macro" from right to left
2. It should be removed

- [ ] Swipe-to-delete works
- [ ] Macro disappears from list

---

## 2. Triggers — Milestone 2

For each trigger test, create a new macro with that trigger + a **Display Notification** action (title: "Test", body: "Trigger fired"). Enable the macro, then perform the triggering action. Check that a notification appears. After testing, check the Logs tab for a SUCCESS entry.

### 2a. Screen On/Off
- **Setup:** Trigger = Screen On/Off, config: "Trigger on Screen On" = ON. Action = Display Notification.
- **Test:** Enable macro. Lock phone (screen off). Unlock phone (screen on).
- [ ] Notification appears when screen turns on
- [ ] Log entry shows SUCCESS

### 2b. Screen Off variant
- **Setup:** Same macro but change config to "Trigger on Screen Off" = ON, "Screen On" = OFF.
- **Test:** Lock phone.
- [ ] Notification appears when screen turns off (you'll see it when you unlock)
- [ ] Log entry shows SUCCESS

### 2c. Battery Level
- **Setup:** Trigger = Battery Level, threshold = your current battery % + 1, "When below" = OFF (i.e., trigger when ABOVE threshold).
- Actually, this one is hard to test since you can't easily change battery level. **Skip for now** — just verify:
- [ ] Editor UI loads: slider + switch visible
- [ ] Config saves and reloads correctly when you re-open the editor

### 2d. Power Connected
- **Setup:** Trigger = Power Connected, "Trigger on connect" = ON. Action = Display Notification.
- **Test:** Enable macro. Plug in your charger.
- [ ] Notification appears when charger connected
- **Setup variant:** Change to "Trigger on disconnect" = ON, connect = OFF.
- **Test:** Unplug charger.
- [ ] Notification appears when charger disconnected

### 2e. Day/Time
- **Setup:** Trigger = Day/Time, set time to 1-2 minutes from now, select today's day of week. Action = Display Notification.
- **Test:** Enable macro and wait.
- [ ] Notification appears around the scheduled time (may be a minute or so off due to Doze)
- **Note:** This uses `setRepeating()` which isn't exact. If it doesn't fire within 5 minutes, note it as a known Doze issue, not a bug.

### 2f. App Launch
- **Prerequisites:** This needs Usage Stats permission.
  1. Go to phone Settings > Apps > Special app access > Usage access
  2. Find OpenMacro and enable it
- **Setup:** Trigger = App Launch, pick an app (e.g., Calculator), "Trigger on launch" = ON. Action = Display Notification.
- **Test:** Enable macro. Open Calculator.
- [ ] Notification appears when Calculator opens
- [ ] App picker sheet works (search, icons display)

---

## 3. Actions — Milestone 2

For action tests, use **Screen On** as the trigger (it's the easiest to fire — just lock/unlock). Create separate macros for each action test, or chain multiple actions in one macro.

### 3a. Display Notification
Already tested above. Just verify:
- [ ] Custom title and body appear in the notification
- [ ] Notification is clearable (auto-cancel)

### 3b. Launch Application
- **Setup:** Trigger = Screen On. Action = Launch Application, pick Calculator (or any app).
- **Test:** Lock and unlock phone.
- [ ] Calculator launches automatically

### 3c. Set Volume
- **Setup:** Action = Set Volume, Stream = Music, Level = 100%.
- **Test:** Turn your music volume down first. Trigger the macro.
- [ ] Music volume jumps to max
- **Variant:** Set to 0%.
- [ ] Music volume goes to silent

### 3d. Vibrate
- **Setup:** Action = Vibrate, duration = 2000ms.
- **Test:** Trigger the macro.
- [ ] Phone vibrates for about 2 seconds

### 3e. Wait
- **Setup:** Three actions in sequence: Display Notification (body: "First"), Wait (3000ms), Display Notification (body: "Second").
- **Test:** Trigger the macro.
- [ ] First notification appears
- [ ] Second notification appears ~3 seconds later

---

## 4. Constraints — Milestone 4

For constraint tests, use **Screen On** trigger + **Display Notification** action, then add a constraint. The macro should only fire when the constraint is met.

### 4a. Battery Level Constraint
- **Setup:** Constraint = Battery Level, min = 0, max = 100.
- **Test:** Trigger — should fire (your battery is between 0 and 100).
- [ ] Fires normally

- **Setup variant:** Set max = 1 (so constraint fails unless battery is 0-1%).
- **Test:** Trigger — should NOT fire.
- [ ] Does NOT fire
- [ ] Log shows CONSTRAINT_NOT_MET status

### 4b. Time of Day Constraint
- **Setup:** Constraint = Time of Day, start = now - 1hr, end = now + 1hr.
- **Test:** Should fire (you're within the time window).
- [ ] Fires

- **Setup variant:** Set start/end to some time in the middle of the night.
- [ ] Does NOT fire

### 4c. Day of Week Constraint
- **Setup:** Constraint = Day of Week, select today.
- [ ] Fires

- **Variant:** Deselect today, select only a different day.
- [ ] Does NOT fire

### 4d. WiFi Connected Constraint
- **Setup:** Constraint = WiFi Connected, SSID = blank (any WiFi).
- **Test with WiFi on:** Should fire.
- [ ] Fires when on WiFi

- **Test with WiFi off:** Turn WiFi off, trigger.
- [ ] Does NOT fire

### 4e. Screen State Constraint
- **Setup:** Constraint = Screen State, "Screen is on" = ON.
- You're unlocking the phone to trigger (screen on), so constraint should pass.
- [ ] Fires

### 4f. Power Connected Constraint
- **Setup:** Constraint = Power Connected, "Power is connected" = ON.
- **Test while plugged in:** Should fire.
- **Test while unplugged:** Should not fire.
- [ ] Fires only when plugged in

### 4g. Variable Value Constraint
- **Setup:**
  1. Go to Variables tab, create a variable: name = "test_var", type = String, value = "hello"
  2. Macro: constraint = Variable Value, variable name = "test_var", operator = "==", value = "hello"
- [ ] Fires

- **Variant:** Change constraint value to "world".
- [ ] Does NOT fire

### 4h. Multiple Constraints with Logic Operators
- **Setup:** Two constraints:
  1. Battery Level (min 0, max 100) — will pass
  2. Day of Week (deselect today) — will fail
  3. Logic operator between them: **AND**
- [ ] Does NOT fire (both must pass for AND)

- **Change operator to OR:**
- [ ] DOES fire (only one needs to pass for OR)

---

## 5. Variables — Milestone 4

### 5a. Variable Manager Screen
1. Go to Variables tab
2. Create a new variable: name = "counter", type = String, value = "0"
- [ ] Variable appears in list
- [ ] Shows name, type, value

3. Edit the variable: change value to "42"
- [ ] Value updates

4. Delete the variable
- [ ] Variable disappears

### 5b. Set Variable Action
- **Setup:** Trigger = Screen On. Actions:
  1. Set Variable: name = "my_var", value = "triggered!"
  2. Display Notification: body = "{v_my_var}"
- **Test:** Trigger the macro.
- [ ] Notification body shows "triggered!"
- [ ] Variable appears in Variables tab with value "triggered!"

### 5c. Local Variables
- **Setup:** Actions:
  1. Set Variable: name = "lv_temp", value = "local only"
  2. Display Notification: body = "{lv_temp}"
- [ ] Notification shows "local only"
- [ ] "lv_temp" does NOT appear in Variables tab (it's local to the execution)

### 5d. Delete Variable Action
- **Setup:** First create a global variable "to_delete" = "exists" in Variables tab. Then macro actions:
  1. Delete Variable: name = "to_delete"
- [ ] After triggering, variable is gone from Variables tab

---

## 6. Magic Text — Milestone 4 & 5

Test magic text tokens by using them in a Display Notification body.

### 6a. Built-in Tokens
- **Setup:** Display Notification, body = "Battery: {battery_level}%, Time: {time}, Date: {date}, Device: {device_name}"
- [ ] Notification shows actual values (not the raw tokens)
- [ ] Battery level is a number
- [ ] Time format is HH:mm
- [ ] Date format is yyyy-MM-dd

### 6b. Macro/Trigger Tokens
- **Setup:** body = "Macro: {macro_name}, Trigger: {trigger_type}"
- [ ] Shows the macro name and "screen_on_off"

### 6c. Unknown Tokens
- **Setup:** body = "Unknown: {does_not_exist}"
- [ ] Shows literal "{does_not_exist}" (not resolved, not crashed)

---

## 7. Triggers — Milestone 5

Same pattern as Section 2: trigger + Display Notification, enable, perform the action.

### 7a. WiFi State Change
- **Setup:** Trigger = WiFi State Change, "on WiFi disabled" = ON. Action = Display Notification.
- **Test:** Enable macro. Turn WiFi OFF in quick settings.
- [ ] Notification appears

- **Variant:** "on WiFi enabled" = ON, disabled = OFF. Turn WiFi back ON.
- [ ] Notification appears

### 7b. WiFi SSID Transition
- **Prerequisites:** Location permission must be granted (the app should ask, or grant manually in app settings). Location services must be ON.
- **Setup:** Trigger = WiFi SSID Transition, SSID = blank (any), "on connect" = ON.
- **Test:** Turn WiFi off, then back on (so it reconnects).
- [ ] Notification appears when WiFi reconnects

- **Variant with specific SSID:** Enter your home WiFi name. Disconnect and reconnect.
- [ ] Fires only for matching SSID

### 7c. Bluetooth Event
- **Prerequisites:** Bluetooth permission must be granted.
- **Setup:** Trigger = Bluetooth Event, "Bluetooth enabled" = ON.
- **Test:** Turn Bluetooth OFF then ON.
- [ ] Notification appears

- **Variant — device connect:** If you have a BT device (headphones, speaker):
  - "Device connected" = ON. Connect the device.
  - [ ] Notification appears

### 7d. Data Connectivity Change
- **Setup:** Trigger = Data Connectivity Change, "on disconnected" = ON.
- **Test:** Turn on Airplane Mode (this kills data).
- [ ] Notification appears
- Turn Airplane Mode off. With "on connected" = ON:
- [ ] Notification appears when data reconnects

### 7e. Airplane Mode Changed
- **Setup:** Trigger = Airplane Mode Changed, "on enabled" = ON.
- **Test:** Turn on Airplane Mode.
- [ ] Notification appears

### 7f. SMS Received
- **Prerequisites:** SMS permission must be granted.
- **Setup:** Trigger = SMS Received, sender filter = blank (any).
- **Test:** Send yourself an SMS from another phone (or use an online SMS service).
- [ ] Notification appears
- [ ] Magic text test: body = "From: {sms_sender}, Msg: {sms_message}" — verify values

### 7g. Call Incoming
- **Prerequisites:** Phone state permission must be granted.
- **Setup:** Trigger = Incoming Call, number filter = blank.
- **Test:** Call your phone from another phone.
- [ ] Notification appears while ringing
- [ ] Magic text: body = "Caller: {call_number}" — shows the calling number

### 7h. Call Ended
- **Setup:** Trigger = Call Ended, number filter = blank.
- **Test:** Call your phone, answer it, then hang up.
- [ ] Notification appears after call ends

### 7i. Call Missed
- **Setup:** Trigger = Missed Call, number filter = blank.
- **Test:** Call your phone and let it ring until it stops (don't answer).
- [ ] Notification appears after the missed call

### 7j. Regular Interval
- **Setup:** Trigger = Regular Interval, interval slider to minimum (~5 seconds).
- **Test:** Enable macro and wait.
- [ ] Notifications appear repeatedly at roughly the interval
- [ ] Disable macro — notifications stop

---

## 8. Actions — Milestone 5

Use **Screen On** trigger for all of these unless noted.

### 8a. WiFi Configure
- **Setup:** Action = WiFi Configure, "Enable WiFi" = OFF (disable).
- **Test:** Trigger.
- **On Android 10+:** A settings panel should open asking to toggle WiFi.
- **On Android 9:** WiFi should turn off directly.
- [ ] Settings panel opens OR WiFi toggles

### 8b. Bluetooth Configure
- **Setup:** Action = Bluetooth Configure, "Enable Bluetooth" = OFF.
- **Test:** Trigger.
- [ ] Bluetooth settings opens OR Bluetooth toggles

### 8c. Airplane Mode
- **Setup:** Action = Airplane Mode, "Enable" = ON.
- **Test:** Trigger.
- [ ] Airplane mode settings opens (unless WRITE_SECURE_SETTINGS is granted via ADB)

### 8d. Send SMS
- **Prerequisites:** SMS permission granted.
- **Setup:** Action = Send SMS, phone number = your own number (or a test number), message = "Test from OpenMacro".
- **Test:** Trigger.
- [ ] SMS is sent (check your messages)
- **CAUTION:** This actually sends a real SMS. Use a number you control.

### 8e. Make Call
- **Prerequisites:** Phone permission granted.
- **Setup:** Action = Make Call, phone number = a number you control.
- **Test:** Trigger.
- [ ] Phone dialer opens and starts calling
- **CAUTION:** This actually makes a real call. Be ready to hang up.

### 8f. Launch Home Screen
- **Setup:** Action = Launch Home Screen.
- **Test:** Open any app first, then trigger the macro.
- [ ] You're taken to the home screen

### 8g. Open Website
- **Setup:** Action = Open Website, URL = "example.com", "Open in browser" = ON.
- **Test:** Trigger.
- [ ] Browser opens to example.com
- [ ] Verify "https://" is auto-prepended (you didn't type it)

### 8h. HTTP Request
- **Setup:** Action = HTTP Request, URL = "https://httpbin.org/get", method = GET. Then add a second action: Display Notification, body = "Response: {http_response_body}"
- **Test:** Trigger.
- [ ] Notification shows JSON response from httpbin
- [ ] No crash

- **POST variant:** URL = "https://httpbin.org/post", method = POST, body = "hello=world".
- [ ] Works without crash

- **Save to variable:** Set "Save response to variable" = "api_result". Check Variables tab after.
- [ ] Variable "api_result" exists with the response body

### 8i. Speak Text
- **Setup:** Action = Speak Text, text = "Hello from OpenMacro", speed = 1.0, pitch = 1.0.
- **Test:** Trigger. Make sure your media volume is up.
- [ ] Phone speaks the text aloud

- **Variant:** Change speed to 2.0, pitch to 0.5.
- [ ] Speaks faster and lower pitch

### 8j. Fill Clipboard
- **Setup:** Action = Fill Clipboard, text = "Copied by OpenMacro".
- **Test:** Trigger. Then open any text field and long-press > Paste.
- [ ] Pasted text is "Copied by OpenMacro"

- **Magic text variant:** text = "Battery is {battery_level}%"
- [ ] Pasted text shows actual battery percentage

---

## 9. Constraints — Milestone 5

Use Screen On trigger + Display Notification. Add the constraint and verify it blocks/allows correctly.

### 9a. Bluetooth Connected
- **Setup:** Constraint = Bluetooth Connected, device address = blank (any).
- **With a BT device connected:** Should fire.
- **With no BT devices connected:** Should NOT fire.
- [ ] Correct behavior

### 9b. WiFi Enabled
- **Setup:** Constraint = WiFi Enabled, "WiFi is enabled" = ON.
- **WiFi on:** Should fire.
- **WiFi off:** Should NOT fire.
- [ ] Correct behavior

### 9c. Airplane Mode Constraint
- **Setup:** Constraint = Airplane Mode, "Airplane mode is on" = ON.
- **Airplane mode on:** Should fire.
- **Airplane mode off:** Should NOT fire.
- [ ] Correct behavior

### 9d. Call State
- **Setup:** Constraint = Call State, state = "idle".
- **Not in a call:** Should fire.
- **During a call (state = offhook):** Should NOT fire.
- [ ] Correct behavior

---

## 10. Logs

After running several tests above, the Logs tab should have entries.

- [ ] Log entries show with macro name, trigger type, timestamp
- [ ] SUCCESS entries for macros that fired
- [ ] CONSTRAINT_NOT_MET entries for blocked macros
- [ ] FAILURE entries if anything errored (check what went wrong)
- [ ] Logs are in reverse chronological order (newest first)

---

## 11. Edge Cases & Stress Tests

### 11a. Macro with no trigger
- Create a macro with only an action (no trigger). Save and enable.
- [ ] App doesn't crash
- [ ] Macro never fires (expected — no trigger to activate it)

### 11b. Macro with no action
- Create a macro with only a trigger (no action). Enable and fire the trigger.
- [ ] App doesn't crash
- [ ] Log entry appears (trigger fired, just nothing to execute)

### 11c. Disable/re-enable macro
- Enable a macro, disable it, re-enable it.
- [ ] No crash
- [ ] Macro fires correctly after re-enable

### 11d. Multiple macros with same trigger
- Create 2 macros both with Screen On trigger, different notification messages.
- [ ] Both notifications appear on screen unlock

### 11e. Long action chain
- Create a macro with 5+ actions (notification, wait, vibrate, notification, set variable, notification).
- [ ] All actions execute in order
- [ ] Log shows SUCCESS

### 11f. App kill and restart
- Enable a macro. Force-kill OpenMacro from app settings.
- [ ] Service restarts (should see the foreground notification reappear)
- [ ] Macro still fires

### 11g. Device reboot
- Enable a macro. Reboot phone.
- [ ] After reboot, service starts automatically
- [ ] Macro still fires

---

## Recording Results

For each failed test, note:
1. **What you did** (steps)
2. **What you expected**
3. **What actually happened**
4. **Logcat output** (if crash — filter `level:ERROR`)

When done, share results and we'll fix whatever's broken before continuing with Milestone 6.

---

## QA Session Notes

### Post-M5 Bug Fixes & Improvements (2026-03-24)

#### Runtime Permission System
- App was never requesting runtime permissions — triggers/actions silently failed
- Added `PermissionHelper.kt` mapping each trigger/action/constraint type to required Android permissions
- `MacrosScreen` now checks and prompts for missing permissions when a macro is toggled ON
- Affected permissions: SMS, phone state, location, Bluetooth, notifications

#### DayTime Trigger Fix
- Switched from `setRepeating()` to `setExactAndAllowWhileIdle()` — alarms now survive Doze mode
- Alarms reschedule themselves after each fire (exact alarms are one-shot)
- Added Logcat logging when an alarm fires but the day-of-week doesn't match
- Fixed default days-of-week: was `[1,2,3,4,5]` (Sun–Thu), now correctly `[2,3,4,5,6]` (Mon–Fri)

#### Monitor Unregister Bug Fix
- All BroadcastReceiver/NetworkCallback monitors were nulling references in `stop()` without actually unregistering
- Fixed 8 monitors: ScreenOnOff, PowerConnected, WifiStateChange, WifiSsidTransition, BluetoothEvent, DataConnectivityChange, AirplaneModeChanged, SmsReceived
- Each now stores `appContext` and calls `unregisterReceiver()`/`unregisterNetworkCallback()` in `stop()`

#### Phone Number Matching Fix
- SMS and call triggers now strip formatting (dashes, spaces, parens) before comparing numbers
- `+1 (555) 867-5309` and `15558675309` now match correctly

#### App Icon
- Replaced placeholder purple triangle with the OpenMacro gear+lightning-M logo
- Adaptive icon: dark indigo background, purple gear + orange M foreground
- Notification small icon updated to lightning-M silhouette (status bar + lock screen)

#### Contact Picker
- Added `ContactPickerField` component — text field with a contacts button
- Applied to 6 editors: SMS Received, Incoming Call, Call Ended, Missed Call, Send SMS, Make Call
- Opens system contact picker, fills in the selected contact's phone number

#### Roadmap Update
- Expanded Milestone 6 description with Magic Text picker UI feature
- Token browser with trigger-specific tokens first, then user variables, then built-in tokens
