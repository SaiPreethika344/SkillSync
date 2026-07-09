import pytest

from case_catalog import PUBLIC_ROUTES, VIEWPORTS, repeat_to_count
from selenium_helpers import assert_controls_are_usable, assert_no_horizontal_overflow, open_path, visible_text


UI_UX_CASES = [
    (route, expected, width, height)
    for route, expected in PUBLIC_ROUTES
    for width, height in VIEWPORTS
]


@pytest.mark.uiux
@pytest.mark.parametrize("route,expected,width,height,case_id", repeat_to_count(UI_UX_CASES, 120))
def test_ui_ux_cases(driver, base_url, route, expected, width, height, case_id):
    driver.set_window_size(width, height)
    open_path(driver, base_url, route)
    assert expected in visible_text(driver)
    assert_no_horizontal_overflow(driver)
    assert_controls_are_usable(driver)
