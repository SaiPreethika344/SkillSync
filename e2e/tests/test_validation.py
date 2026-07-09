import pytest
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys

from case_catalog import AUTH_INPUTS, FIELD_SKILL_CASES, repeat_to_count
from selenium_helpers import click_text, open_path, visible_text


VALIDATION_CASES = (
    [("input", page, selector, value) for _, page, selector, value in AUTH_INPUTS]
    + [("empty-analysis", "/analysis", "Analyze my profile", "Select your field above")]
    + [("analysis-selection", field, skill, "selected") for field, skill in FIELD_SKILL_CASES]
    + [
        ("forgot-step", "/forgot-password", "not-an-email", "Send OTP"),
        ("login-empty", "/login", "Log in", "Welcome back"),
        ("signup-empty", "/signup", "Create account", "Create your account"),
        ("resume-mode", "/analysis", "Upload resume", "Upload your resume"),
    ]
)


@pytest.mark.validation
@pytest.mark.parametrize("case_type,arg,selector,value,case_id", repeat_to_count(VALIDATION_CASES, 110))
def test_validation_cases(driver, base_url, case_type, arg, selector, value, case_id):
    if case_type == "input":
        open_path(driver, base_url, arg)
        element = driver.find_element(By.CSS_SELECTOR, selector)
        element.clear()
        element.send_keys(value)
        assert element.get_attribute("value") == value
        return

    if case_type == "empty-analysis":
        open_path(driver, base_url, arg)
        button = driver.find_element(By.XPATH, f"//button[contains(., '{selector}')]")
        assert not button.is_enabled()
        assert value in visible_text(driver)
        return

    if case_type == "analysis-selection":
        open_path(driver, base_url, "/analysis")
        click_text(driver, arg)
        click_text(driver, selector)
        assert value in visible_text(driver)
        return

    if case_type == "forgot-step":
        open_path(driver, base_url, arg)
        driver.find_element(By.CSS_SELECTOR, "input[type='email']").send_keys(selector)
        click_text(driver, value)
        assert "Reset your password" in visible_text(driver) or "Unable to send OTP" in visible_text(driver)
        return

    if case_type in {"login-empty", "signup-empty"}:
        open_path(driver, base_url, arg)
        click_text(driver, selector)
        assert value in visible_text(driver)
        return

    if case_type == "resume-mode":
        open_path(driver, base_url, arg)
        click_text(driver, selector)
        assert value in visible_text(driver)
        file_input = driver.find_element(By.CSS_SELECTOR, "input[type='file']")
        assert file_input.get_attribute("accept") == ".pdf"
        driver.find_element(By.TAG_NAME, "body").send_keys(Keys.ESCAPE)
