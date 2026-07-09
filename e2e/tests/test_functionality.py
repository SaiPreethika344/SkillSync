import pytest
from selenium.webdriver.common.by import By

from case_catalog import FIELD_SKILL_CASES, PUBLIC_ROUTES, TEXT_EXPECTATIONS, repeat_to_count
from selenium_helpers import click_text, open_path, visible_text


FUNCTIONALITY_CASES = (
    [("route", route, expected) for route, expected in PUBLIC_ROUTES]
    + [("text", route, expected) for route, expected in TEXT_EXPECTATIONS]
    + [("skill", field, skill) for field, skill in FIELD_SKILL_CASES]
    + [
        ("nav", "/", "Start Career Analysis"),
        ("nav", "/", "Log in"),
        ("nav", "/login", "Sign up free"),
        ("nav", "/signup", "Log in"),
        ("nav", "/results", "Log in for results"),
        ("nav", "/results", "Sign up free"),
        ("guard", "/dashboard", "Welcome back"),
    ]
)


@pytest.mark.functionality
@pytest.mark.parametrize("case_type,arg,expected,case_id", repeat_to_count(FUNCTIONALITY_CASES, 120))
def test_functionality_cases(driver, base_url, case_type, arg, expected, case_id):
    if case_type in {"route", "text"}:
        open_path(driver, base_url, arg)
        assert expected in visible_text(driver)
        return

    if case_type == "skill":
        open_path(driver, base_url, "/analysis")
        click_text(driver, arg)
        click_text(driver, expected)
        assert "1 selected" in visible_text(driver)
        assert expected in visible_text(driver)
        return

    if case_type == "nav":
        open_path(driver, base_url, arg)
        click_text(driver, expected)
        assert driver.current_url.startswith(base_url)
        assert driver.find_elements(By.TAG_NAME, "body")
        return

    if case_type == "guard":
        open_path(driver, base_url, arg)
        assert expected in visible_text(driver)
