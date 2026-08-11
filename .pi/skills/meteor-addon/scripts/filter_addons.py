#!/usr/bin/env python3
"""
Filter Meteor Client addons based on quality and version compatibility.

Usage:
    # Find verified addons for latest Minecraft version
    python filter_addons.py --verified
    
    # Find addons for specific Minecraft version
    python filter_addons.py --mc-version 1.21.1 --verified
    
    # Search for specific features
    python filter_addons.py --feature-type modules --feature-name ExampleModule
    
    # Include archived addons (normally excluded)
    python filter_addons.py --include-archived
"""

import argparse
import json
import sys
import re
from urllib.request import urlopen
from typing import List, Dict, Any

# Ensure stdout uses utf-8 and handles characters gracefully
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    except AttributeError:
        # Fallback for older python versions if necessary
        import io
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

ADDONS_DB_URL = "https://raw.githubusercontent.com/cqb13/meteor-addon-scanner/refs/heads/addons/addons.json"


def clean_text(text: str) -> str:
    """Keep only printable ASCII characters and normalize whitespace."""
    if not text:
        return ""
    # Keep only printable ASCII (space to tilde)
    text = re.sub(r'[^\x20-\x7E]', ' ', text)
    # Normalize whitespace
    text = re.sub(r'\s+', ' ', text).strip()
    return text


def fetch_addons() -> List[Dict[str, Any]]:
    """Fetch the addons database from GitHub."""
    try:
        with urlopen(ADDONS_DB_URL) as response:
            return json.loads(response.read().decode('utf-8'))
    except Exception as e:
        print(f"Error fetching addons database: {e}", file=sys.stderr)
        sys.exit(1)


def filter_addons(
    addons: List[Dict[str, Any]],
    mc_version: str = None,
    verified_only: bool = True,
    include_archived: bool = False,
    feature_type: str = None,
    feature_name: str = None,
    min_stars: int = 0,
    sort_by: str = "stars",
    search_query: str = None
) -> List[Dict[str, Any]]:
    """
    Filter addons based on various criteria.
    """
    filtered = []
    
    for addon in addons:
        # Skip archived unless explicitly included
        if not include_archived and addon.get("repo", {}).get("archived", False):
            continue
        
        # Verify status
        if verified_only and not addon.get("verified", False):
            continue
        
        # Minecraft version match
        if mc_version:
            addon_mc_version = addon.get("mc_version", "")
            supported_versions = addon.get("custom", {}).get("supported_versions") or []
            
            if mc_version not in [addon_mc_version] + supported_versions:
                continue
        
        # Feature filtering
        if feature_type and feature_name:
            features = addon.get("features", {}).get(feature_type) or []
            if feature_name not in features:
                continue
        
        # Star count
        stars = addon.get("repo", {}).get("stars", 0)
        if stars < min_stars:
            continue

        # Description search
        if search_query:
            desc = addon.get("custom", {}).get("description", "")
            if not desc or search_query.lower() not in desc.lower():
                continue
        
        filtered.append(addon)
    
    # Sort results
    if sort_by == "stars":
        filtered.sort(key=lambda x: x.get("repo", {}).get("stars", 0), reverse=True)
    elif sort_by == "downloads":
        filtered.sort(key=lambda x: x.get("repo", {}).get("downloads", 0), reverse=True)
    elif sort_by == "last_update":
        filtered.sort(key=lambda x: x.get("repo", {}).get("last_update", ""), reverse=True)
    
    return filtered


def format_addon_summary(addon: Dict[str, Any]) -> str:
    """Format an addon into a readable summary."""
    name = clean_text(addon.get("name", "Unknown"))
    desc = clean_text(addon.get("description", "No description"))
    mc_version = addon.get("mc_version", "Unknown")
    verified = "[VERIFIED]" if addon.get("verified", False) else "[UNVERIFIED]"
    
    repo = addon.get("repo", {})
    stars = repo.get("stars", 0)
    owner = repo.get("owner", "Unknown")
    repo_name = repo.get("name", "Unknown")
    last_update = repo.get("last_update", "Unknown").split("T")[0]  # Just date
    github_url = addon.get("links", {}).get("github", "")
    
    authors = addon.get("authors", [])
    if isinstance(authors, list):
        authors_str = ", ".join(clean_text(a) for a in authors)
    else:
        authors_str = "Unknown"

    # Supported versions from custom data
    custom = addon.get("custom", {})
    supported_versions = custom.get("supported_versions", [])
    if supported_versions:
        version_info = f"{mc_version} (also: {', '.join(supported_versions)})"
    else:
        version_info = mc_version
    
    features = addon.get("features", {})
    module_count = len(features.get("modules", []) or [])
    command_count = len(features.get("commands", []) or [])
    hud_count = len(features.get("hud_elements", []) or [])
    screen_count = len(features.get("custom_screens", []) or [])
    
    return f"{name} {verified}\n  MC: {version_info} | Stars: {stars} | Updated: {last_update}\n  Authors: {authors_str}\n  Repo: {owner}/{repo_name}\n  URL: {github_url}\n  Desc: {desc}\n  Features: {module_count} mods, {command_count} cmds, {hud_count} hud, {screen_count} screens\n"


def main():
    parser = argparse.ArgumentParser(
        description="Filter Meteor Client addons by quality and version"
    )
    parser.add_argument(
        "--mc-version",
        help="Filter by Minecraft version (e.g., 1.21.1)"
    )
    parser.add_argument(
        "--verified",
        action="store_true",
        help="Only show verified addons (default: True unless --no-verified)"
    )
    parser.add_argument(
        "--no-verified",
        action="store_true",
        help="Include non-verified addons"
    )
    parser.add_argument(
        "--include-archived",
        action="store_true",
        help="Include archived repositories"
    )
    parser.add_argument(
        "--feature-type",
        choices=["modules", "commands", "hud_elements", "custom_screens"],
        help="Filter by feature type"
    )
    parser.add_argument(
        "--feature-name",
        help="Filter by specific feature name (requires --feature-type)"
    )
    parser.add_argument(
        "--min-stars",
        type=int,
        default=0,
        help="Minimum GitHub stars"
    )
    parser.add_argument(
        "--sort-by",
        choices=["stars", "downloads", "last_update"],
        default="stars",
        help="Sort results by field"
    )

    parser.add_argument(
        "--search",
        help="Search query for custom description"
    )

    parser.add_argument(
        "--limit",
        type=int,
        help="Limit number of results"
    )
    
    args = parser.parse_args()
    
    # Fetch addons
    addons = fetch_addons()
    
    # Filter
    verified_only = args.verified or not args.no_verified
    filtered = filter_addons(
        addons,
        mc_version=args.mc_version,
        verified_only=verified_only,
        include_archived=args.include_archived,
        feature_type=args.feature_type,
        feature_name=args.feature_name,
        min_stars=args.min_stars,
        sort_by=args.sort_by,
        search_query=args.search
    )
    
    if args.limit:
        filtered = filtered[:args.limit]
    
    # Output
    print(f"Found {len(filtered)} addons for {args.mc_version or 'all versions'}\n")
    for addon in filtered:
        print(format_addon_summary(addon))


if __name__ == "__main__":
    main()
