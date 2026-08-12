#!/usr/bin/env python3
"""
Dump every code/config comment in the project.

The LLM evaluates the output against ASD-STE100. This script only
collects comments so a human (or the model) can judge them.

Covered comment styles:
  - C-style (// and /* */): .java .kt .kts .js .ts .gradle
  - Hash (#): .properties .toml .yml .yaml .py .ps1 .sh .cfg .ini
              .accesswidener .editorconfig
  - Python triple-quoted docstrings
  - HTML (<!-- -->): .md .html .xml

Usage:
  python scripts/dump_comments.py                 # print to stdout
  python scripts/dump_comments.py --out comments.txt
  python scripts/dump_comments.py --json comments.json
  python scripts/dump_comments.py --exclude build --exclude run
"""

import argparse
import json
import re
import sys
from pathlib import Path

# Build output, third-party reference clones, the STE skill's own
# verbatim corpus, and VCS data are not part of this sweep.
DEFAULT_EXCLUDE_DIRS = {
    ".git", ".gradle", "build", "node_modules",
    "references", "run", "bin", "out", "target",
    "asd-ste100",
}

EXT_STYLE = {
    "java": "cstyle", "kt": "cstyle", "kts": "cstyle",
    "js": "cstyle", "ts": "cstyle", "gradle": "cstyle",
    "properties": "hash", "toml": "hash", "yml": "hash", "yaml": "hash",
    "py": "python", "ps1": "hash", "sh": "hash",
    "cfg": "hash", "ini": "hash", "accesswidener": "hash",
    "editorconfig": "hash",
    "md": "html", "html": "html", "xml": "html",
}


class Comment:
    def __init__(self, path, line, text):
        self.path = path
        self.line = line
        self.text = text


# --- C-style extraction (skips string literals) ---------------------------

def extract_cstyle(content):
    out, i, n, line = [], 0, len(content), 1
    state = "code"  # code | line | block | string | char
    buf, buf_line = [], 0

    while i < n:
        ch = content[i]
        nxt = content[i + 1] if i + 1 < n else ""

        if state == "code":
            if ch == "/" and nxt == "/":
                state, buf, buf_line = "line", [], line
                i += 2
                continue
            if ch == "/" and nxt == "*":
                state, buf, buf_line = "block", [], line
                buf.append("/*")
                i += 2
                continue
            if ch == '"':
                state = "string"
            elif ch == "'":
                state = "char"
            if ch == "\n":
                line += 1
            i += 1
            continue

        if state == "line":
            if ch == "\n":
                _flush(out, buf, buf_line)
                buf, state = [], "code"
                line += 1
            else:
                buf.append(ch)
            i += 1
            continue

        if state == "block":
            if ch == "*" and nxt == "/":
                buf.append("*/")
                _flush(out, buf, buf_line)
                buf, state = [], "code"
                i += 2
                continue
            if ch == "\n":
                line += 1
            buf.append(ch)
            i += 1
            continue

        if state == "string":
            if ch == "\\":
                i += 2
                continue
            if ch == '"':
                state = "code"
            if ch == "\n":
                line += 1
            i += 1
            continue

        if state == "char":
            if ch == "\\":
                i += 2
                continue
            if ch == "'":
                state = "code"
            if ch == "\n":
                line += 1
            i += 1
            continue

    _flush(out, buf, buf_line)
    return out


def _flush(out, buf, buf_line):
    if not buf:
        return
    raw = "".join(buf)
    text = _clean_cstyle(raw)
    if text:
        out.append((buf_line, text))


def _clean_cstyle(raw):
    if raw.startswith("/*"):
        raw = raw[2:]
    if raw.endswith("*/"):
        raw = raw[:-2]
    parts = []
    for ln in raw.splitlines():
        s = ln.strip()
        if s.startswith("//"):
            s = s[2:]
        if s.startswith("/*"):
            s = s[2:]
        if s.endswith("*/"):
            s = s[:-2]
        if s.startswith("*"):
            s = s[1:]
        parts.append(s.strip())
    return " ".join(p for p in parts if p)


# --- Hash extraction -------------------------------------------------------

def extract_hash(content):
    out = []
    for idx, raw in enumerate(content.splitlines(), start=1):
        in_s = in_d = False
        col = 0
        while col < len(raw):
            c = raw[col]
            if c == "\\" and col + 1 < len(raw):
                col += 2
                continue
            if c == '"' and not in_s:
                in_d = not in_d
            elif c == "'" and not in_d:
                in_s = not in_s
            elif c == "#" and not in_d and not in_s:
                text = raw[col + 1:].strip()
                if text:
                    out.append((idx, text))
                break
            col += 1
    return out


def extract_python(content):
    out = extract_hash(content)
    for m in re.finditer(r'"""(.*?)"""', content, re.DOTALL):
        start = content.count("\n", 0, m.start()) + 1
        text = " ".join(p.strip() for p in m.group(1).splitlines() if p.strip())
        if text:
            out.append((start, text))
    for m in re.finditer(r"'''(.*?)'''", content, re.DOTALL):
        start = content.count("\n", 0, m.start()) + 1
        text = " ".join(p.strip() for p in m.group(1).splitlines() if p.strip())
        if text:
            out.append((start, text))
    return out


HTML_RE = re.compile(r"<!--(.*?)-->", re.DOTALL)


def extract_html(content):
    out = []
    for m in HTML_RE.finditer(content):
        start = content.count("\n", 0, m.start()) + 1
        text = " ".join(p.strip() for p in m.group(1).splitlines() if p.strip())
        if text:
            out.append((start, text))
    return out


def extract(path):
    ext = path.suffix.lstrip(".").lower()
    style = EXT_STYLE.get(ext)
    if not style:
        return []
    try:
        content = path.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return []
    if style == "cstyle":
        rows = extract_cstyle(content)
    elif style == "hash":
        rows = extract_hash(content)
    elif style == "python":
        rows = extract_python(content)
    elif style == "html":
        rows = extract_html(content)
    else:
        return []
    return [Comment(str(path), ln, tx) for ln, tx in rows]


def collect_files(root, excludes):
    root = Path(root)
    files = []
    for p in root.rglob("*"):
        if not p.is_file():
            continue
        if any(part in excludes for part in p.parts):
            continue
        if p.suffix.lstrip(".").lower() in EXT_STYLE:
            files.append(p)
    return sorted(files)


def main(argv=None):
    ap = argparse.ArgumentParser(description="Dump project comments")
    ap.add_argument("--root", default=".")
    ap.add_argument("--out", help="write text output here")
    ap.add_argument("--json", dest="json_path", help="write JSON here")
    ap.add_argument("--exclude", action="append", default=[])
    args = ap.parse_args(argv)

    excludes = set(DEFAULT_EXCLUDE_DIRS) | set(args.exclude)
    files = collect_files(args.root, excludes)

    by_file = {}
    for path in files:
        comments = extract(path)
        if comments:
            by_file[str(path)] = comments

    lines = [f"# {len(files)} files scanned, "
             f"{sum(len(c) for c in by_file.values())} comments", ""]
    for f in sorted(by_file):
        lines.append(f"=== {f} ===")
        for c in by_file[f]:
            text = c.text.replace("\n", " ")
            lines.append(f"  L{c.line} | {text}")
        lines.append("")
    report = "\n".join(lines)

    if args.out:
        Path(args.out).write_text(report, encoding="utf-8")
    else:
        print(report)

    if args.json_path:
        data = [{"file": c.path, "line": c.line, "text": c.text}
                for f in by_file for c in by_file[f]]
        Path(args.json_path).write_text(json.dumps(data, indent=2),
                                        encoding="utf-8")
    return 0


if __name__ == "__main__":
    sys.exit(main())
