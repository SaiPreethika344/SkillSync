import sys
import time
from urllib.request import urlopen


def main():
    url = sys.argv[1]
    timeout = int(sys.argv[2]) if len(sys.argv) > 2 else 60
    deadline = time.time() + timeout

    while time.time() < deadline:
        try:
            with urlopen(url, timeout=2) as response:
                if response.status < 500:
                    return 0
        except Exception:
            time.sleep(1)

    print(f"Timed out waiting for {url}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
