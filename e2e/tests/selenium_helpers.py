from selenium.common.exceptions import NoAlertPresentException
from selenium.common.exceptions import ElementClickInterceptedException
from selenium.webdriver.common.by import By

from conftest import wait_for


def open_path(driver, base_url, path):
    driver.get(f"{base_url}{path}")
    wait_for(lambda: driver.execute_script("return document.readyState") in {"interactive", "complete"})
    wait_for(lambda: driver.find_element(By.TAG_NAME, "body").text.strip())


def visible_text(driver):
    return wait_for(lambda: driver.find_element(By.TAG_NAME, "body").text.strip())


def click_text(driver, text):
    candidates = driver.find_elements(By.XPATH, f"//*[normalize-space()={xpath_literal(text)}]")
    if not candidates:
        candidates = driver.find_elements(By.XPATH, f"//*[contains(normalize-space(), {xpath_literal(text)})]")
    assert candidates, f"Could not find clickable text: {text}"
    element = candidates[0]
    driver.execute_script("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element)
    try:
        element.click()
    except ElementClickInterceptedException:
        driver.execute_script("arguments[0].click();", element)


def xpath_literal(value):
    if "'" not in value:
        return f"'{value}'"
    if '"' not in value:
        return f'"{value}"'
    return "concat(" + ", \"'\", ".join(f"'{part}'" for part in value.split("'")) + ")"


def assert_no_alert(driver):
    try:
        alert = driver.switch_to.alert
    except NoAlertPresentException:
        return
    text = alert.text
    alert.dismiss()
    raise AssertionError(f"Unexpected browser alert: {text}")


def assert_no_horizontal_overflow(driver):
    overflow = driver.execute_script(
        "return document.documentElement.scrollWidth - document.documentElement.clientWidth"
    )
    assert overflow <= 2


def assert_controls_are_usable(driver):
    controls = driver.find_elements(By.CSS_SELECTOR, "button, input, a[href]")
    visible_controls = [control for control in controls if control.is_displayed()]
    assert visible_controls
    for control in visible_controls[:25]:
        rect = control.rect
        tag_name = control.tag_name.lower()
        if tag_name in {"button", "input"}:
            assert rect["width"] >= 24
            assert rect["height"] >= 20
        else:
            assert rect["width"] >= 8
            assert rect["height"] >= 8
