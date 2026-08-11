import argparse
import os
import subprocess
import sys
from pathlib import Path

# Ensure stdout uses utf-8
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    except AttributeError:
        import io
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

DEFAULT_TARGET = "./ai_reference"


def clone_repo(repo_url: str, target_dir: Path) -> bool:
    """
    Clone a repository into the target directory.
    """
    # Extract repo name from URL
    repo_name = repo_url.rstrip('/').split('/')[-1]
    if repo_name.endswith('.git'):
        repo_name = repo_name[:-4]
    
    clone_path = target_dir / repo_name
    
    # Skip if already exists
    if clone_path.exists():
        print(f"[SKIP] {repo_name} (already exists)", file=sys.stderr)
        return True
    
    print(f"[CLONE] Cloning {repo_name}...", file=sys.stderr)
    
    try:
        # Pass env with GIT_TERMINAL_PROMPT=0 to avoid hanging on auth requests
        env = os.environ.copy()
        env['GIT_TERMINAL_PROMPT'] = '0'
        
        result = subprocess.run(
            ['git', 'clone', '--depth=1', repo_url, str(clone_path)],
            capture_output=True,
            text=True,
            check=True,
            env=env
        )
        print(f"[SUCCESS] Cloned {repo_name}", file=sys.stderr)
        return True
    except subprocess.CalledProcessError as e:
        print(f"[ERROR] Failed to clone {repo_name}: {e.stderr}", file=sys.stderr)
        return False


def cleanup_references(target_dir: Path) -> None:
    """Remove all cloned repositories."""
    if not target_dir.exists():
        print(f"No reference directory found at {target_dir}", file=sys.stderr)
        return
    
    import shutil
    print(f"[CLEANUP] Cleaning up {target_dir}...", file=sys.stderr)
    shutil.rmtree(target_dir, ignore_errors=True)
    print(f"[SUCCESS] Cleaned up reference directory", file=sys.stderr)


def main():
    parser = argparse.ArgumentParser(
        description="Clone Meteor addon repositories for analysis"
    )
    parser.add_argument(
        "repo_urls",
        nargs='*',
        help="One or more GitHub repository URLs to clone"
    )
    parser.add_argument(
        "--target",
        default=DEFAULT_TARGET,
        help=f"Target directory for cloned repos (default: {DEFAULT_TARGET})"
    )
    parser.add_argument(
        "--cleanup",
        action="store_true",
        help="Remove all cloned repositories"
    )
    
    args = parser.parse_args()
    target_dir = Path(args.target)
    
    if args.cleanup:
        cleanup_references(target_dir)
        return
    
    if args.repo_urls:
        target_dir.mkdir(parents=True, exist_ok=True)
        successful = 0
        failed = 0
        
        for repo_url in args.repo_urls:
            if clone_repo(repo_url, target_dir):
                successful += 1
            else:
                failed += 1
        
        print(f"\n[SUMMARY] {successful} successful, {failed} failed", file=sys.stderr)
    else:
        parser.print_help()
        sys.exit(1)


if __name__ == "__main__":
    main()
