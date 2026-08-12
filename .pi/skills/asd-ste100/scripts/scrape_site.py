#!/usr/bin/env python3
"""
Bespoke scraper + HTML->Markdown converter for the ASD-STE100 official site.

The site is built with Mobirise: server-rendered static HTML composed of
Bootstrap <section> blocks. Each page has the same chrome:

    section.menu          -> top navigation bar   (STRIP)
    section.headerNN      -> hero banner          (keep text content)
    section.articleNN     -> prose content        (KEEP)
    section.featuresNNNN  -> card/grid content    (KEEP)
    section.listNN        -> FAQ accordion Q&A    (KEEP, special handling)
    section.footerN       -> footer               (STRIP)

The FAQ page uses Bootstrap accordions. Each .card holds:
    h6.panel-title-edit   -> the question
    div.panel-body        -> the answer (one or more <p>)

We convert each card into clean markdown (**Q:** / answer) so the content is
VERBATIM but readable. No summarization occurs anywhere.

Output: scraped-guides/raw_html/*.html  (raw fidelity)
        scraped-guides/*.md             (faithful markdown conversion)

Run:  MSYS_NO_PATHCONV=1 python scripts/scrape_site.py
"""
import os
import re
import sys
from pathlib import Path

import cloudscraper
from bs4 import BeautifulSoup
from markdownify import markdownify as md

BASE = "https://www.asd-ste100.org/"
SKILL_ROOT = Path(__file__).resolve().parent.parent
OUT_ROOT = SKILL_ROOT / "scraped-guides"
RAW_HTML = OUT_ROOT / "raw_html"
OUT_ROOT.mkdir(parents=True, exist_ok=True)
RAW_HTML.mkdir(parents=True, exist_ok=True)

# (page path, output stem, source title for attribution)
PAGES = [
    ("index.html",          "home",              "ASD-STE100 home"),
    ("about_STE.html",      "about_STE",         "About STE"),
    ("STE_faq.html",        "STE_faq",           "STE FAQ"),
    ("STE_downloads.html",  "STE_downloads",     "STE downloads"),
    ("STE_training.html",   "STE_training",      "STE training"),
    ("STEsoftware.html",    "STEsoftware",       "STE software"),
    ("STEtrainers.html",    "STEtrainers",       "STE trainers"),
    ("ASD_STEMG.html",      "ASD_STEMG",         "ASD STEMG"),
    ("STEWorkingGroups.html", "STEWorkingGroups", "STE working groups"),
]

# Headings to demote so the curated output uses a sane hierarchy.
HRULE = re.compile(r"\n{3,}")


def strip_chrome(soup: BeautifulSoup) -> None:
    """Remove navigation, footer, scripts, styles and decorative overlays."""
    for sel in [
        "section.menu",                       # top nav
        "section[class*=footer]",             # any footer section
        "nav",                                # any stray nav
        "script", "style", "noscript", "link",
        "div.cookie-banner", "div#cookie-banner",
        ".mbr-overlay",                       # decorative color overlay
        "span.sign",                          # accordion +/- icon glyphs
    ]:
        for el in soup.select(sel):
            el.decompose()


def collapse_br_to_paragraphs(soup: BeautifulSoup) -> None:
    """Mobirise separates paragraphs with <br/><br/>. Turn those into real <p>."""
    for br in soup.find_all("br"):
        nxt = br.find_next_sibling()
        if nxt and nxt.name == "br":
            # replace the double <br> with a paragraph boundary marker
            br.insert_after(soup.new_tag("pbr"))
            nxt.decompose()
            br.decompose()


def transform_accordions(soup: BeautifulSoup) -> None:
    """Turn each FAQ accordion card into <h3>Q</h3><div>A</div> for clean MD."""
    for card in soup.select("div.card"):
        head = card.select_one("h6.panel-title-edit") or card.select_one(".panel-title")
        body = card.select_one("div.panel-body")
        if not head or not body:
            continue
        q = head.get_text(" ", strip=True)
        # Build replacement structure: <h3>Q: ...</h3> then answer body
        new = soup.new_tag("div")
        h = soup.new_tag("h3")
        h.string = f"Q: {q}"
        new.append(h)
        # move body children
        for child in list(body.contents):
            new.append(child.extract() if not isinstance(child, str) else child)
        card.replace_with(new)


def convert_sections_to_md(soup: BeautifulSoup, page_stem: str) -> str:
    """Convert remaining content sections to markdown, section by section."""
    chunks = []
    for sec in soup.find_all("section"):
        # section title is often an h4.mbr-section-title or h1
        title_el = sec.select_one("h4.mbr-section-title") or sec.select_one("h1") \
            or sec.select_one("h2")
        # Render the section body. Use ATX headings, no wrapping.
        section_md = md(
            str(sec),
            heading_style="ATX",
            bullets="-",
            strip=["a"] if page_stem == "STEsoftware" else [],  # keep most links
            code_language="",
        )
        section_md = _cleanup(section_md)
        if section_md.strip():
            chunks.append(section_md)
    body = "\n\n".join(chunks)
    return _cleanup(body)


def _cleanup(text: str) -> str:
    """Collapse excessive blank lines and tidy common artifacts."""
    # Drop empty header anchors like "# \n"
    text = re.sub(r"^#+\s*$", "", text, flags=re.MULTILINE)
    # Our <pbr> markers -> blank line (paragraph break)
    text = text.replace("<pbr/>", "\n\n").replace("<pbr>", "\n\n")
    # Collapse 3+ newlines to 2
    text = HRULE.sub("\n\n", text)
    # Trim trailing spaces on lines
    text = re.sub(r"[ \t]+\n", "\n", text)
    return text.strip() + "\n"


def main() -> int:
    scraper = cloudscraper.create_scraper(
        browser={"browser": "chrome", "platform": "windows", "mobile": False}
    )
    scraper.headers.update({"Accept-Language": "en-US,en;q=0.9"})

    failures = []
    for path, stem, title in PAGES:
        url = BASE + path
        print(f"[fetch] {url}")
        try:
            r = scraper.get(url, timeout=40)
            r.raise_for_status()
        except Exception as e:
            print(f"  ! FAILED: {e}")
            failures.append(url)
            continue

        # Force UTF-8 decode: ASD-STE100 serves text/html WITHOUT a charset,
        # so requests/ cloudscraper default to ISO-8859-1 and mangle smart
        # quotes. The pages are UTF-8.
        html = r.content.decode("utf-8", errors="replace")
        (RAW_HTML / f"{stem}.html").write_text(html, encoding="utf-8")

        soup = BeautifulSoup(html, "lxml")
        strip_chrome(soup)
        transform_accordions(soup)
        collapse_br_to_paragraphs(soup)
        md_text = convert_sections_to_md(soup, stem)

        header = (
            f"<!-- Source: {url} -->\n"
            f"<!-- Acquired by scripts/scrape_site.py with cloudscraper. "
            f"VERBATIM content; HTML->Markdown format cleanup only. -->\n\n"
            f"# {title}\n\n"
        )
        out = OUT_ROOT / f"{stem}.md"
        out.write_text(header + md_text, encoding="utf-8")
        lines = md_text.count("\n") + 1
        print(f"  -> {out.relative_to(SKILL_ROOT)}  ({lines} lines)")

    if failures:
        print("\nFAILED URLs:")
        for u in failures:
            print("  -", u)
        return 1
    print("\nAll pages scraped successfully.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
