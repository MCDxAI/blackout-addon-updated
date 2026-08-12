#!/usr/bin/env python3
"""
Curate raw acquisitions in scraped-guides/ into the final references/ tree.

VERBATIM rule: only FORMAT CLEANUP is performed, never summarization or
paraphrase. The transformations here are all structural/typographic:

  - merge related official pages into themed reference files
  - normalize heading levels & strip decorative ** around heading text
  - remove the FAQ's redundant table-of-contents block (duplicates the
    real category headings below it)
  - drop decorative stock photos (empty-alt adobestock images)
  - retarget internal cross-page .html links to the curated reference file
    (keeps external https:// links verbatim; drops dead anchors)
  - white-paper only: split bullet paragraphs on the bullet glyph into real
    markdown lists; repair obvious PDF kerning artifacts ("th is"->"this");
    strip the repeated running header/address chrome (not content)

Output: references/*.md  +  references/README.md (progressive-disclosure index)

Run:  MSYS_NO_PATHCONV=1 python scripts/curate_references.py
"""
import re
from pathlib import Path

SKILL_ROOT = Path(__file__).resolve().parent.parent
SRC = SKILL_ROOT / "scraped-guides"
OUT = SKILL_ROOT / "references"
OUT.mkdir(parents=True, exist_ok=True)

# Official page -> curated reference file (for internal link retargeting)
PAGE_TO_REF = {
    "index.html": "ste-overview.md",
    "about_STE.html": "ste-overview.md",
    "STE_faq.html": "ste-faq.md",
    "STE_downloads.html": "ste-resources.md",
    "STE_training.html": "ste-resources.md",
    "STEsoftware.html": "ste-resources.md",
    "STEtrainers.html": "ste-resources.md",
    "ASD_STEMG.html": "ste-governance.md",
    "STEWorkingGroups.html": "ste-governance.md",
}


def read(name: str) -> str:
    return (SRC / name).read_text(encoding="utf-8")


def strip_first_h1(text: str) -> str:
    """Remove the leading '# Title' line (the page's own hero/file title)."""
    return re.sub(r"^#\s+.+\n", "", text, count=1).strip()


def flatten_body_h1(text: str) -> str:
    """Demote body H1 headings to H2 (we keep a single file-level H1 ourselves).
    Hero sections on the official site emit several H1s; collapsing them to H2
    gives a clean, single-title document without losing any content."""
    return re.sub(r"(?m)^#\s+", "## ", text)


def write(name: str, body: str) -> None:
    (OUT / name).write_text(body.strip() + "\n", encoding="utf-8")
    n = body.count("\n") + 1
    print(f"  -> references/{name}  ({n} lines)")


# ---------- shared cleanup helpers ----------

def strip_source_comments(text: str) -> str:
    return "\n".join(
        ln for ln in text.split("\n") if not ln.strip().startswith("<!--")
    ).strip()


def demote_and_clean_headings(text: str) -> str:
    """Strip decorative **...** wrappers from heading text."""
    def repl(m):
        hashes, inner = m.group(1), m.group(2)
        inner = inner.replace("**", "").strip()
        return f"{hashes} {inner}"
    return re.sub(r"^(#{1,6})\s+(.+)$", repl, text, flags=re.MULTILINE)


def remove_decorative_images(text: str) -> str:
    """Drop adobestock decorative photos and any empty-alt local images."""
    out = []
    for ln in text.split("\n"):
        if re.search(r"!\[\s*\]\(assets/images/adobestock", ln):
            continue
        if re.search(r"!\[\s*\]\(assets/images/", ln):
            continue
        out.append(ln)
    return "\n".join(out)


def retarget_links(text: str) -> str:
    """Point internal .html links to curated reference files; keep externals."""
    def repl(m):
        label, href = m.group(1), m.group(2)
        # external or non-html -> leave untouched
        if href.startswith("http") or href.startswith("mailto:") or "@" in href and href.startswith("#"):
            return m.group(0)
        # split off any #anchor and query
        base = href.split("#")[0].split("?")[0]
        if base in PAGE_TO_REF:
            return f"[{label}]({PAGE_TO_REF[base]})"
        # unknown internal page -> keep text only (no dead link in packaged skill)
        return label
    return re.sub(r"\[([^\]]+)\]\(([^)]+)\)", repl, text)


def collapse_blanks(text: str) -> str:
    text = re.sub(r"[ \t]+\n", "\n", text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def common_cleanup(text: str) -> str:
    text = strip_source_comments(text)
    text = demote_and_clean_headings(text)
    text = remove_decorative_images(text)
    text = retarget_links(text)
    text = collapse_blanks(text)
    return text


# ---------- per-reference builders ----------

HEADER_OVERVIEW = """<!-- Curated from asd-ste100.org (index.html + about_STE.html). VERBATIM official content; format cleanup only. -->
<!-- Source: https://www.asd-ste100.org/index.html , https://www.asd-ste100.org/about_STE.html -->

# STE Overview — What ASD-STE100 Simplified Technical English Is

"""


def build_overview() -> str:
    home = flatten_body_h1(strip_first_h1(read("home.md")))
    about = flatten_body_h1(strip_first_h1(read("about_STE.md")))
    body = (
        "## At a glance (home page)\n\n" + home + "\n\n"
        "---\n\n"
        + about
    )
    return common_cleanup(HEADER_OVERVIEW + body)


def build_faq() -> str:
    faq = read("STE_faq.md")
    faq = strip_source_comments(faq)
    # Cut everything before the first category heading: this removes the
    # file-title H1, the page-hero H1 ("# Frequently Asked Questions"), the
    # duplicate intro paragraph, AND the redundant ##### TOC link block in
    # one pass. Real content starts at the first #### category heading.
    m = re.search(r"(?m)^####\s+", faq)
    if m:
        faq = faq[m.start():]
    # Demote category headings #### -> ## ; questions ### stay ###
    faq = re.sub(r"^####\s+", "## ", faq, flags=re.MULTILINE)
    # Demote any stray H1 sections (e.g. the closing "Do you have further
    # questions?" call-to-action) so the file has a single H1.
    faq = flatten_body_h1(faq)
    faq = demote_and_clean_headings(faq)
    faq = remove_decorative_images(faq)
    faq = retarget_links(faq)
    faq = collapse_blanks(faq)
    header = (
        "<!-- Curated VERBATIM from https://www.asd-ste100.org/STE_faq.html -->\n"
        "<!-- The richest single reference on STE principles, rules, and use. -->\n\n"
        "# STE Frequently Asked Questions (Official)\n\n"
        "Prepared by the STEMG to help you understand the key concepts of "
        "ASD-STE100 Simplified Technical English (STE).\n\n"
    )
    return header + faq


def build_resources() -> str:
    parts = [
        ("Downloads", read("STE_downloads.md")),
        ("Training", read("STE_training.md")),
        ("Software / checking tools", read("STEsoftware.md")),
        ("Accredited trainers", read("STEtrainers.md")),
    ]
    chunks = []
    for title, body in parts:
        body = flatten_body_h1(strip_first_h1(body))
        chunks.append(f"## {title}\n\n{body}")
    body = "\n\n---\n\n".join(chunks)
    header = (
        "<!-- Curated VERBATIM from asd-ste100.org: STE_downloads.html, STE_training.html, STEsoftware.html, STEtrainers.html -->\n\n"
        "# STE Resources — Downloads, Training, Tools, Trainers\n\n"
        "Where to get the official standard, learn STE, and find supporting "
        "tooling. (ASD and the STEMG do not endorse any commercial tool.)\n\n"
    )
    return common_cleanup(header + body)


def build_governance() -> str:
    stemg = flatten_body_h1(strip_first_h1(read("ASD_STEMG.md")))
    wg = flatten_body_h1(strip_first_h1(read("STEWorkingGroups.md")))
    body = (
        "## The ASD Simplified Technical English Maintenance Group (STEMG)\n\n"
        + stemg + "\n\n---\n\n"
        "## STE Working Groups & Support Teams\n\n" + wg
    )
    header = (
        "<!-- Curated VERBATIM from asd-ste100.org: ASD_STEMG.html + STEWorkingGroups.html -->\n\n"
        "# STE Governance — How the Standard Is Maintained\n\n"
    )
    return common_cleanup(header + body)


# White-paper specific transforms --------------------------------------------

# Repeated running header on every PDF page (org name + address + page no.)
RUNNING_HEADER_RE = re.compile(
    r"Aerospace, Security and Defence Industries Association of Europe \|"
    r"[^\n]*?Page \d+[^\n]*",
    flags=re.DOTALL,
)

# Obvious PDF kerning artifacts (space inserted by the PDF's text layout).
KERNING_FIXES = [
    (r"\bth is\b", "this"),
    (r"\bS TEMG\b", "STEMG"),
    (r"\baccel erate\b", "accelerate"),
    (r"\bASD\s+-\s*STE100\b", "ASD-STE100"),
    (r"\bSTE\s+-\s*compliant\b", "STE-compliant"),
    (r"\bAI\s+-\s*assisted\b", "AI-assisted"),
    (r"\brisk\s+-\s*based\b", "risk-based"),
    (r"\bsafety\s+-\s*critical\b", "safety-critical"),
    (r"\bnon\s+-\s*endorsement\b", "non-endorsement"),
    (r"\bnon\s+-\s*standard\b", "non-standard"),
    (r"\bAI\s+-\s*", "AI-"),  # remaining "AI -X"
]

# Standalone title block repeated at top of each page ("Simplified Technical
# English\n\nMaintenance Group") - chrome, not content.
PAGE_TITLE_CHROME = re.compile(
    r"Simplified Technical English\s*\n\s*Maintenance Group\s*\n+",
)


def split_bullets(text: str) -> str:
    """Turn paragraphs that pack bullet items with the bullet glyph into lists."""
    out = []
    for block in re.split(r"(\n{2,})", text):
        if "•" in block and "\n" not in block.strip():
            # single-paragraph bullet run -> markdown list
            chunks = block.split("•")
            prefix = chunks[0]              # text before the first bullet (the lead-in)
            items = [c.strip() for c in chunks[1:] if c.strip()]
            if prefix.strip():
                out.append(prefix.strip() + "\n")
            out.append("\n".join(f"- {it}" for it in items))
        else:
            out.append(block)
    return "".join(out)


def build_whitepaper() -> str:
    raw = read("whitepaper-ai.md")
    raw = strip_source_comments(raw)
    # remove our own added title + attribution blockquote (re-added below)
    raw = raw.split("# ASD-STE100 and Artificial Intelligence", 1)[1]
    # drop the H1 line itself
    raw = re.sub(r"^#\s+.+\n", "", raw, count=1)
    # strip attribution blockquote (lines starting with '> ')
    raw = "\n".join(ln for ln in raw.split("\n") if not ln.lstrip().startswith(">"))
    # strip "---" page separators + the per-page HTML comments already removed
    raw = raw.replace("\n---\n", "\n\n")
    # remove running headers
    raw = RUNNING_HEADER_RE.sub("", raw)
    # remove repeated page-title chrome
    raw = PAGE_TITLE_CHROME.sub("", raw)
    # repair kerning artifacts
    for pat, rep in KERNING_FIXES:
        raw = re.sub(pat, rep, raw)
    # repair PDF end-of-sentence spacing artifacts: "word ." -> "word."
    raw = re.sub(r"(?<=[a-z]) \.(?=\s|$)", ".", raw)
    # split packed bullet paragraphs into lists
    raw = split_bullets(raw)
    raw = collapse_blanks(raw)
    header = (
        "<!-- Source PDF: https://www.asd-ste100.org/assets/files/WhitePaper-ASD-STE100_and_AI.pdf -->\n"
        "<!-- Official white paper. VERBATIM text extraction; PDF layout artifacts repaired, bullets normalized. -->\n\n"
        "# ASD-STE100 and Artificial Intelligence — Official White Paper\n\n"
        "> Published by the ASD Simplified Technical English Maintenance Group "
        "(STEMG) and the STEMG Artificial Intelligence Task Team (AITT), June 2026. "
        "Reproduced verbatim from the public PDF.\n\n"
    )
    return header + raw


def build_wikipedia() -> str:
    raw = read("wikipedia-simplified-technical-english.md")
    # Keep attribution blockquote intact; only run light cleanup.
    # Remove the "See also" portal icon image clutter (icon thumbnails).
    raw = re.sub(r"\[!\[icon\]\([^)]*\)\]\([^)]*\)", "", raw)
    raw = collapse_blanks(raw)
    return raw


README_BODY = """<!-- Progressive-disclosure index for the asd-ste100 skill references. -->

# STE Reference Index

These references are **verbatim** official / open-source documentation about
ASD-STE100 Simplified Technical English. They are the knowledge base the skill
draws on; load the one that matches the task.

> **Licensing note:** The full ASD-STE100 *Specification* (all ~65 writing
> rules enumerated in full + the proprietary approved/unapproved word
> dictionary) is a paid, copyrighted document and is **not** reproduced here.
> These references use only freely-published official material + the public
> Wikipedia summary. That is sufficient for writing clear commit messages,
> PR descriptions, and end-user docs in a STE-influenced style.

## Start here

- [`ste-overview.md`](ste-overview.md) — What STE is, why it exists, its two
  parts (writing rules + controlled dictionary), history, and the STEMG.
  **Best entry point.**
- [`ste-faq.md`](ste-faq.md) — The official FAQ, verbatim. The richest single
  reference: principles, rules, common misconceptions, and practical guidance.

## Deeper references

- [`ste-whitepaper-ai.md`](ste-whitepaper-ai.md) — Official STEMG white paper on
  STE and Artificial Intelligence (content creation, translation, risks,
  safeguards, user guidance).
- [`wikipedia-simplified-technical-english.md`](wikipedia-simplified-technical-english.md)
  — Wikipedia article (CC-BY-SA 3.0): history, structure of the standard, an
  illustrative dictionary sample, tools, and adoption issues.

## Supporting context

- [`ste-governance.md`](ste-governance.md) — How STE is maintained: the STEMG
  and the national / multi-country support teams.
- [`ste-resources.md`](ste-resources.md) — Where to download the official
  standard, get certified training, and find (non-endorsed) checking tools.

## How to use these references

- For **commit messages / PR descriptions / docs prose**: read `ste-overview.md`
  (principles) + the "Writing rules" section of the Wikipedia article
  (concrete rule list), then apply the principles. You do **not** need the
  full paid spec for everyday technical writing.
- The controlled dictionary's ~900 approved words are proprietary; instead,
  internalize the *principles* (one word = one meaning, short sentences,
  active voice, no "-ing" forms, one topic per sentence) the references teach.
"""


def main() -> None:
    print("Curating references/ ...")
    write("ste-overview.md", build_overview())
    write("ste-faq.md", build_faq())
    write("ste-resources.md", build_resources())
    write("ste-governance.md", build_governance())
    write("ste-whitepaper-ai.md", build_whitepaper())
    write("wikipedia-simplified-technical-english.md", build_wikipedia())
    (OUT / "README.md").write_text(README_BODY.strip() + "\n", encoding="utf-8")
    print("  -> references/README.md  (index)")
    print("\nDone.")


if __name__ == "__main__":
    main()
