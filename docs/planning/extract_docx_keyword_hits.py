from __future__ import annotations

import re
import sys
from pathlib import Path

from docx import Document


def _normalize_ws(value: str) -> str:
    return re.sub(r"\s+", " ", (value or "").strip())


def main() -> int:
    if len(sys.argv) < 3:
        print("Usage: python extract_docx_keyword_hits.py <docx_path> <keyword1> [keyword2] ...", file=sys.stderr)
        return 2

    docx_path = Path(sys.argv[1]).expanduser().resolve()
    keywords = [k.strip() for k in sys.argv[2:] if k.strip()]
    if not keywords:
        print("No keywords provided", file=sys.stderr)
        return 2

    doc = Document(str(docx_path))
    paras = [_normalize_ws(p.text) for p in doc.paragraphs]

    lowered = [p.lower() for p in paras]
    keys_lower = [k.lower() for k in keywords]

    hits = []
    for idx, text in enumerate(lowered):
        if not text:
            continue
        matched = [keywords[i] for i, k in enumerate(keys_lower) if k in text]
        if matched:
            hits.append((idx, matched))

    print("FILE:", docx_path.name)
    print("KEYWORDS:", keywords)
    print("HITS:", len(hits))
    print()

    for idx, matched in hits[:200]:
        prev_text = paras[idx - 1] if idx - 1 >= 0 else ""
        cur_text = paras[idx]
        next_text = paras[idx + 1] if idx + 1 < len(paras) else ""
        print(f"[{idx}] matched={matched}")
        if prev_text:
            print("  -1:", prev_text[:200])
        print("   0:", cur_text[:260])
        if next_text:
            print("  +1:", next_text[:200])
        print()

    if len(hits) > 200:
        print("... truncated ...")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

