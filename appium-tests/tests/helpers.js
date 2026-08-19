// helpers.js — mirrors selenium_helpers.py
// Shared utilities for all Appium test files

const APP_PKG = 'com.skillsync.app';

/**
 * Build a fully-qualified Android resource-id selector.
 * Mirrors Selenium's By.ID usage.
 */
function id(resourceId) {
  return `id=${APP_PKG}:id/${resourceId}`;
}

/**
 * Wait for an element to be displayed, then return it.
 * Mirrors Selenium's wait_for() helper.
 */
async function waitForElement(resourceId, timeoutMs = 8000) {
  const el = await $(id(resourceId));
  await el.waitForDisplayed({ timeout: timeoutMs });
  return el;
}

/**
 * Wait for element, tap it.
 */
async function tap(resourceId, timeoutMs = 8000) {
  const el = await waitForElement(resourceId, timeoutMs);
  await el.click();
  return el;
}

/**
 * Clear a field and type text into it.
 */
async function typeInto(resourceId, text, timeoutMs = 8000) {
  const el = await waitForElement(resourceId, timeoutMs);
  await el.clearValue();
  await el.setValue(text);
  return el;
}

/**
 * Return the text of an element (safe, returns '' if missing).
 */
async function getText(resourceId, timeoutMs = 5000) {
  try {
    const el = await $(id(resourceId));
    await el.waitForDisplayed({ timeout: timeoutMs });
    return (await el.getText()) || '';
  } catch {
    return '';
  }
}

/**
 * Check if element is displayed without throwing.
 */
async function isDisplayed(resourceId, timeoutMs = 4000) {
  try {
    const el = await $(id(resourceId));
    await el.waitForDisplayed({ timeout: timeoutMs });
    return true;
  } catch {
    return false;
  }
}

/**
 * Check if element is NOT displayed.
 */
async function isNotDisplayed(resourceId, timeoutMs = 3000) {
  return !(await isDisplayed(resourceId, timeoutMs));
}

/**
 * Wait for an activity to be current.
 */
async function waitForActivity(activitySuffix, timeoutMs = 10000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    try {
      const current = await driver.getCurrentActivity();
      if (current && current.includes(activitySuffix)) return true;
    } catch { /* retry */ }
    await driver.pause(300);
  }
  throw new Error(`Timed out waiting for activity: ${activitySuffix}`);
}

/**
 * Reset the app to a clean state (equivalent to clean_browser_state).
 */
async function resetApp() {
  try {
    await driver.terminateApp(APP_PKG);
    await driver.pause(500);
    await driver.activateApp(APP_PKG);
    await driver.pause(1000);
  } catch {
    // If app not installed yet, ignore
  }
}

/**
 * Scroll down on the screen.
 */
async function scrollDown() {
  const { width, height } = await driver.getWindowSize();
  await driver.action('pointer')
    .move({ x: Math.floor(width / 2), y: Math.floor(height * 0.7) })
    .down()
    .move({ x: Math.floor(width / 2), y: Math.floor(height * 0.3) })
    .up()
    .perform();
}

/**
 * Press the Android back button.
 */
async function pressBack() {
  await driver.pressKeyCode(4);  // KEYCODE_BACK
}

/**
 * Find element by text using UiSelector.
 */
async function findByText(text, timeoutMs = 6000) {
  const sel = `android=new UiSelector().text("${text}")`;
  const el = await $(sel);
  await el.waitForDisplayed({ timeout: timeoutMs });
  return el;
}

/**
 * Check if any visible element contains the given text.
 */
async function pageContainsText(text, timeoutMs = 5000) {
  try {
    const sel = `android=new UiSelector().textContains("${text}")`;
    const el = await $(sel);
    await el.waitForDisplayed({ timeout: timeoutMs });
    return true;
  } catch {
    return false;
  }
}

/**
 * Perform login with given credentials.
 */
async function doLogin(email, password) {
  await waitForActivity('LoginActivity');
  await typeInto('emailInput', email);
  await typeInto('passwordInput', password);
  await tap('loginButton');
}

/**
 * Navigate to login screen from any state.
 */
async function goToLogin() {
  await resetApp();
  await waitForActivity('SplashActivity', 5000).catch(() => {});
  await waitForActivity('LoginActivity', 8000).catch(() => waitForActivity('LandingActivity', 5000));
}

module.exports = {
  APP_PKG,
  id,
  waitForElement,
  tap,
  typeInto,
  getText,
  isDisplayed,
  isNotDisplayed,
  waitForActivity,
  resetApp,
  scrollDown,
  pressBack,
  findByText,
  pageContainsText,
  doLogin,
  goToLogin,
};
