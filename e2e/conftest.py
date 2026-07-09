import os
import time

import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options


def pytest_addoption(parser):
    parser.addoption(
        "--base-url",
        action="store",
        default=os.getenv("E2E_BASE_URL", "http://127.0.0.1:5173"),
        help="Base URL for the running frontend app.",
    )


@pytest.fixture(scope="session")
def base_url(pytestconfig):
    return pytestconfig.getoption("--base-url").rstrip("/")


@pytest.fixture(scope="session")
def driver():
    options = Options()
    options.add_argument("--headless=new")
    options.add_argument("--window-size=1440,1000")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-extensions")
    options.add_argument("--page-load-strategy=eager")
    browser = webdriver.Chrome(options=options)
    browser.set_page_load_timeout(30)
    browser.implicitly_wait(1)
    yield browser
    browser.quit()


@pytest.fixture(autouse=True)
def clean_browser_state(driver, base_url):
    if not driver.current_url.startswith(base_url):
        driver.get(base_url)
    driver.delete_all_cookies()
    driver.execute_script("window.localStorage.clear(); window.sessionStorage.clear();")
    yield


def wait_for(condition, timeout=5, interval=0.05):
    deadline = time.time() + timeout
    last_error = None
    while time.time() < deadline:
        try:
            result = condition()
            if result:
                return result
        except Exception as exc:
            last_error = exc
        time.sleep(interval)
    if last_error:
        raise last_error
    raise AssertionError("Timed out waiting for condition")
