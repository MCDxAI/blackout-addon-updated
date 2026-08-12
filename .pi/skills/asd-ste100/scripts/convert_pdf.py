#!/usr/bin/env python3
"""
Convert the official ASD-STE100 white paper PDF to faithful Markdown.

Source PDF:
  https://www.asd-ste100.org/assets/files/WhitePaper-ASD-STE100_and_AI.pdf

Uses pypdf to extract text page by page. PDF text extraction is imperfect,
so we apply light, lossless cleanup only:
  - de-hyphenate words broken across line breaks ("machine-\\nlearning")
  - join lines within a paragraph, separate paragraphs on blank-line gaps
  - promote ALL-CAPS / Title-Case short lines that look like headings
  - preserve all substantive content verbatim (no summarization)

Output: scraped-guides/whitepaper-ai.md

Run:  MSYS_NO_PATHCONV=1 python scripts/convert_pdf.py
"""
import re
import sys
from pathlib import Path

import cloudscraper
from pypdf import PdfReader

URL = "https://www.asd-ste100.org/assets/files/WhitePaper-ASD-STE100_and_AI.pdf"
SKILL_ROOT = Path(__file__).resolve().parent.parent
OUT = SKILL_ROOT / "scraped-guides" / "whitepaper-ai.md"
PDF_CACHE = SKILL_ROOT / "scraped-guides" / "raw_html" / "WhitePaper-ASD-STE100_and_AI.pdf"


def fetch_pdf() -> bytes:
    if PDF_CACHE.exists() and PDF_CACHE.stat().st_size > 1000:
        print(f"[pdf] using cached {PDF_CACHE.name}")
        return PDF_CACHE.read_bytes()
    print(f"[pdf] downloading {URL}")
    scraper = cloudscraper.create_scraper()
    r = scraper.get(URL, timeout=60)
    r.raise_for_status()
    PDF_CACHE.parent.mkdir(parents=True, exist_ok=True)
    PDF_CACHE.write_bytes(r.content)
    return r.content


# Heuristic: a heading line is short, has no trailing period, and is either
# ALL CAPS (acronyms/section caps) or Title Case.
HEADING_RE = re.compile(r"^[A-Z0-9][A-Za-z0-9,:/& -]{2,70}$")


def clean_page(text: str) -> str:
    """Clean one page's extracted text without losing content."""
    # Normalise whitespace
    lines = [ln.rstrip() for ln in text.split("\n")]
    out = []
    buf = []

    def flush():
        if buf:
            joined = dehyphenate(" ".join(s.strip() for s in buf))
            out.append(joined)
            buf.clear()

    for ln in lines:
        if not ln.strip():
            flush()  # paragraph boundary
            continue
        # Detect likely standalone headings (short, no period, capitalised)
        stripped = ln.strip()
        looks_heading = (
            len(stripped) <= 70
            and not stripped.endswith(".")
            and not stripped.endswith(",")
            and (stripped.isupper() or HEADING_RE.match(stripped))
            and len(stripped.split()) <= 12
        )
        if looks_heading and not buf:
            out.append(stripped)
            continue
        buf.append(ln)
    flush()
    return "\n\n".join(out)


def dehyphenate(s: str) -> str:
    # "machine-\nlearning" -> "machinelearning"? No: join hyphenated splits
    # back to "machine-learning" only when the hyphen is at end of a token
    # followed by lowercase. pypdf already removes newlines, so handle "word- "
    s = re.sub(r"([A-Za-z])-\s+([a-z])", r"\1-\2", s)
    return s


def promote_headings(md: str) -> str:
    """Best-effort: mark obvious document/section headings with ##."""
    lines = md.split("\n")
    result = []
    for ln in lines:
        s = ln.strip()
        if not s:
            result.append("")
            continue
        # Skip if already a markdown heading
        if s.startswith("#"):
            result.append(ln)
            continue
        # ALL-CAPS heading (>=3 letters, mostly caps)
        letters = [c for c in s if c.isalpha()]
        if (
            len(s) <= 70
            and len(letters) >= 3
            and sum(1 for c in letters if c.isupper()) / max(1, len(letters)) > 0.8
            and not s.endswith((".", ",", ";", ":"))
        ):
            result.append(f"## {s}")
            continue
        result.append(ln)
    return "\n".join(result)


def main() -> int:
    data = fetch_pdf()
    reader = PdfReader(__import__("io").BytesIO(data))
    print(f"[pdf] {len(reader.pages)} pages")

    pages_md = []
    for i, page in enumerate(reader.pages, 1):
        try:
            txt = page.extract_text() or ""
        except Exception as e:
            print(f"  page {i}: extract failed ({e})")
            txt = ""
        cleaned = clean_page(txt)
        pages_md.append(f"<!-- PDF page {i} -->\n\n{cleaned}")

    body = "\n\n---\n\n".join(pages_md)
    body = promote_headings(body)
    # Collapse 3+ blank lines
    body = re.sub(r"\n{3,}", "\n\n", body).strip()

    header = (
        "<!-- Source PDF: https://www.asd-ste100.org/assets/files/WhitePaper-ASD-STE100_and_AI.pdf -->\n"
        "<!-- Converted by scripts/convert_pdf.py with pypdf. VERBATIM text extraction; format cleanup only. -->\n\n"
        "# ASD-STE100 and Artificial Intelligence (Official White Paper)\n\n"
        "> Official white paper published by the ASD Simplified Technical English\n"
        "> Maintenance Group (STEMG). Reproduced here verbatim from the public PDF.\n\n"
    )
    OUT.write_text(header + body + "\n", encoding="utf-8")
    print(f"[pdf] wrote {OUT.relative_to(SKILL_ROOT)}  ({body.count(chr(10)) + 1} lines)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
