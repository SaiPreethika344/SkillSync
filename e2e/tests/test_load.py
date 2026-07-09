import pytest

from case_catalog import PUBLIC_ROUTES, repeat_to_count
from selenium_helpers import open_path, visible_text


LOAD_CASES = [
    (route, expected, budget_ms)
    for route, expected in PUBLIC_ROUTES
    for budget_ms in (6000, 8000, 10000)
]


@pytest.mark.load
@pytest.mark.parametrize("route,expected,budget_ms,case_id", repeat_to_count(LOAD_CASES, 110))
def test_browser_load_cases(driver, base_url, route, expected, budget_ms, case_id):
    open_path(driver, base_url, route)
    assert expected in visible_text(driver)
    duration = driver.execute_script(
        """
        const entry = performance.getEntriesByType('navigation')[0];
        return entry ? entry.duration : performance.timing.loadEventEnd - performance.timing.navigationStart;
        """
    )
    assert duration < budget_ms
