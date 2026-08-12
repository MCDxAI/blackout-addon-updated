#!/usr/bin/env python3
"""
Fetch the Wikipedia "Simplified Technical English" article and convert it to
faithful Markdown, with a CC-BY-SA attribution header.

Wikipedia text is licensed CC-BY-SA 3.0; reuse requires attribution. We keep
the article text VERBATIM (no summarization) and add an attribution line at
the top citing the source URL + retrieval date.

Uses the MediaWiki REST API for clean HTML, then markdownify. Falls back to
scraping the rendered article with cloudscraper if the API route fails.

Output: scraped-guides/wikipedia-simplified-technical-english.md

Run:  MSYS_NO_PATHCONV=1 python scripts/fetch_wikipedia.py
"""
import datetime
import re
import sys
from pathlib import Path

import cloudscraper
from bs4 import BeautifulSoup
from markdownify import markdownify as md

SKILL_ROOT = Path(__file__).resolve().parent.parent
OUT = SKILL_ROOT / "scraped-guides" / "wikipedia-simplified-technical-english.md"

TITLE = "Simplified Technical English"
REST_URL = (
    "https://en.wikipedia.org/w/rest.php/v1/page/html/"
    "Simplified%20Technical%20English"
)
RENDER_URL = "https://en.wikipedia.org/wiki/Simplified_Technical_English"


def cleanup(text: str) -> str:
    text = re.sub(r"\n{3,}", "\n\n", text)
    text = re.sub(r"[ \t]+\n", "\n", text)
    # Demote Wikipedia's leading H1 (page title) — we provide our own header
    return text.strip() + "\n"


def via_rest(scraper) -> str | None:
    try:
        r = scraper.get(REST_URL, timeout=40, headers={"Accept": "text/html"})
        if r.status_code != 200 or not r.content:
            return None
        html = r.content.decode("utf-8", errors="replace")
    except Exception as e:
        print(f"[wiki] REST route failed: {e}")
        return None
    soup = BeautifulSoup(html, "lxml")
    # Strip edit spans, reference linkbacks, and nav junk
    for sel in ["span.mw-editsection", ".mw-editsection", "sup.reference",
                ".reference", "style", "script", "table.ambox", ".navbox",
                ".mw-references-wrap", ".reflist"]:
        for el in soup.select(sel):
            el.decompose()
    body = soup.find("body") or soup
    text = md(str(body), heading_style="ATX", bullets="-")
    return text


def via_rendered(scraper) -> str:
    r = scraper.get(RENDER_URL, timeout=40)
    r.raise_for_status()
    html = r.content.decode("utf-8", errors="replace")
    soup = BeautifulSoup(html, "lxml")
    content = soup.select_one("#mw-content-text .mw-parser-output") or \
        soup.select_one("#mw-content-text") or soup
    for sel in ["span.mw-editsection", "sup.reference", ".reference",
                "table.ambox", ".navbox", ".mw-references-wrap", ".reflist",
                ".hatnote", ".thumb", "style", "script", "#toc", ".toc"]:
        for el in content.select(sel):
            el.decompose()
    text = md(str(content), heading_style="ATX", bullets="-")
    return text


def main() -> int:
    scraper = cloudscraper.create_scraper()
    today = datetime.date.today().isoformat()

    text = via_rest(scraper)
    route = "MediaWiki REST HTML API"
    if not text or len(text) < 400:
        print("[wiki] REST route empty/short, falling back to rendered article")
        text = via_rendered(scraper)
        route = "rendered article HTML"
    text = cleanup(text)

    header = (
        "<!-- Source: https://en.wikipedia.org/wiki/Simplified_Technical_English -->\n"
        f"<!-- Retrieved: {today} via {route}. -->\n\n"
        "> **Attribution:** This article is adapted from \"Simplified Technical\n"
        "> English\" on Wikipedia, and is licensed under the Creative Commons\n"
        "> Attribution-ShareAlike 3.0 License (CC-BY-SA 3.0). Source:\n"
        "> <https://en.wikipedia.org/wiki/Simplified_Technical_English>.\n"
        "> Retrieved " + today + ".\n\n"
        "---\n\n"
        f"# {TITLE} (Wikipedia)\n\n"
    )
    OUT.write_text(header + text, encoding="utf-8")
    print(f"[wiki] wrote {OUT.relative_to(SKILL_ROOT)}  ({text.count(chr(10)) + 1} lines)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
