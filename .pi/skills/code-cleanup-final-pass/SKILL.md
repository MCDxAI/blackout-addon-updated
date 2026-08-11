---
name: code-cleanup-final-pass
description: Deep code review and cleanup workflow for performing a final quality pass before committing code or opening pull requests. Use when the user wants to review and clean up a codebase to ensure production-ready quality, eliminate technical debt, remove debug code, simplify overcomplicated logic, or prepare code for public repository contribution. Triggers include "final pass", "cleanup before commit", "review before PR", "clean up this code", "prepare for production", or any request to audit and improve code quality across a project.
---

# Code Cleanup Final Pass

## Overview

This skill provides a comprehensive workflow for performing deep code review and cleanup as a final quality pass before pushing code to a repository or opening a pull request. It follows a systematic process to discover project-specific best practices, interview the user about their goals, analyze the codebase for quality issues, generate detailed reports, and apply approved fixes.

## Workflow

### Phase 1: Discovery & Context Gathering

**Step 1: Locate Best Practices Documentation**

Recursively search the project for AI agent documentation files that may contain project-specific best practices, coding standards, or guidelines:

```bash
find . -type f \( -name "CLAUDE.md" -o -name "AGENTS.md" -o -name "GEMINI.md" \) 2>/dev/null
```

Read any discovered files to understand:
- Project-specific coding standards
- Preferred patterns and anti-patterns
- Language-specific conventions
- Testing requirements
- Documentation expectations
- Commit message formats
- Any other quality guidelines

**Step 2: Identify Project Languages and Frameworks**

Analyze the project structure to determine:
- Primary programming languages used
- Frameworks and libraries in use
- Build tools and configuration files
- Testing frameworks
- Linting/formatting configurations present

Look for configuration files like:
- `package.json`, `tsconfig.json`, `eslint.config.js` (JavaScript/TypeScript)
- `pyproject.toml`, `setup.py`, `requirements.txt` (Python)
- `Cargo.toml` (Rust)
- `go.mod` (Go)
- `Gemfile` (Ruby)
- `.editorconfig`, `.prettierrc`, etc.

### Phase 2: User Interview

**Step 3: Conduct Interview About Cleanup Goals**

Use the AskUserQuestionTool if available in Claude Code. If the tool is not available, manually ask questions (maximum 5 at a time) and wait for responses.

Essential questions to ask:

1. **Scope**: What files or directories should be reviewed? (entire project, specific directories, specific files)
2. **Goals**: What is the primary goal of this cleanup? (preparing for commit, preparing for PR, general code quality improvement, preparing for public release)
3. **Risk tolerance**: Are there any files or sections that are particularly sensitive or should not be modified?
4. **Priorities**: Which types of issues are most important to address?
   - Overcomplicated/duplicated code
   - Debug logging and console statements
   - Commented-out code
   - Unused imports/variables/functions
   - TODO/FIXME markers
   - Code formatting and style consistency
   - Performance issues
   - Security concerns
5. **Testing**: Are there tests that should be run after changes to verify nothing broke?
6. **Additional context**: Is there anything else I should know about this codebase or your preferences?

Adapt questions based on discovered documentation and project structure.

### Phase 3: Deep Code Analysis

**Step 4: Systematic Code Review**

Perform a thorough analysis of the codebase looking for:

**Code Quality Issues:**
- Overcomplicated logic that can be simplified
- Duplicated code that should be refactored
- Nested conditionals that can be flattened
- Long functions that should be broken down
- Complex expressions that need clarification
- Dead code or unreachable branches

**Debug and Development Artifacts:**
- Console logs, print statements, debug output
- Commented-out code blocks
- Temporary hacks or workarounds marked with comments
- Development-only imports or features
- Hardcoded values that should be configurable
- Test data or mock implementations in production code

**Code Cleanliness:**
- Unused imports, variables, functions, classes
- Inconsistent naming conventions
- Missing or incorrect type hints/annotations
- Incomplete error handling
- TODO, FIXME, HACK, XXX markers
- Inconsistent formatting (if no formatter detected)

**Best Practices Violations:**
- Violations of discovered project standards
- Language-specific anti-patterns
- Framework misuse or deprecated API usage
- Security vulnerabilities (hardcoded secrets, SQL injection, XSS)
- Performance issues (N+1 queries, unnecessary loops)

**Documentation Gaps:**
- Missing docstrings/comments for complex logic
- Outdated comments that don't match code
- Missing README sections
- Undocumented configuration requirements

**Step 5: Categorize and Prioritize Issues**

Organize findings by:
- **Severity**: Critical (security, bugs) → High (major quality issues) → Medium (style, minor issues) → Low (suggestions)
- **Category**: Security, Performance, Maintainability, Style, Documentation
- **File/Location**: Group issues by file for easier navigation
- **Effort**: Quick fixes vs. complex refactoring

### Phase 4: Reporting

**Step 6: Generate Comprehensive Reports**

Create two detailed reports:

**Issues Report (`code-cleanup-issues-report.md`):**
- Executive summary of findings
- Issues organized by severity and category
- Each issue includes:
  - File path and line numbers
  - Description of the problem
  - Why it matters
  - Severity level
- Statistics (total issues, breakdown by type/severity)

**Resolution Plan (`code-cleanup-resolution-plan.md`):**
- Proposed fixes organized by file
- Each fix includes:
  - Issue being addressed
  - Proposed solution approach
  - Code changes (before/after snippets for significant changes)
  - Potential risks or side effects
  - Testing recommendations
- Estimated effort/complexity for each fix

**Step 7: Present Summary and Seek Approval**

Present a concise summary in the chat:

```
Code Cleanup Analysis Complete
==============================

Found X issues across Y files:
- Critical: N issues
- High: N issues  
- Medium: N issues
- Low: N issues

Top categories:
1. [Category]: N issues
2. [Category]: N issues
3. [Category]: N issues

Full issues report: code-cleanup-issues-report.md
Full resolution plan: code-cleanup-resolution-plan.md

Proposed approach:
[Brief summary of the most important fixes and overall strategy]

May I proceed with implementing these fixes?
```

Wait for user approval before proceeding.

### Phase 5: Implementation

**Step 8: Execute Approved Fixes**

Once approved:
1. Work through fixes systematically, file by file
2. Make changes carefully, testing as you go
3. If tests are present, run them after each significant change
4. Document any deviations from the plan or unexpected issues
5. Track completed fixes

**Step 9: Final Verification**

After all changes:
1. Run any configured linters/formatters
2. Run test suite if present
3. Verify all targeted issues were addressed
4. Check for any new issues introduced
5. Generate a completion summary

**Step 10: Completion Report**

Provide a final summary:

```
Code Cleanup Complete
====================

Changes made:
- Files modified: N
- Issues resolved: N
- Lines changed: ~N

What was done:
[Brief summary of major changes]

Testing:
[Test results or note if no tests present]

Recommendations:
[Any follow-up suggestions or remaining issues]
```

## Best Practices

### When Using This Skill

- **Before major commits**: Use before pushing significant changes to ensure quality
- **Before pull requests**: Clean up branches before requesting reviews
- **Regular maintenance**: Periodically review code to prevent technical debt
- **After rapid prototyping**: Clean up code that was written quickly
- **Before public release**: Ensure code meets professional standards

### Communication Guidelines

- Be thorough but not overwhelming in reports
- Explain *why* issues matter, not just *what* they are
- Prioritize user's stated goals over theoretical perfection
- Acknowledge when issues are subjective vs. objective
- Respect project conventions even if they differ from general best practices
- Ask before making significant architectural changes

### Safety Considerations

- Never modify files marked as sensitive or generated
- Be cautious with files lacking tests
- Preserve functionality over style
- Keep backups in mind (git makes this easy, but remind user)
- Flag potentially breaking changes in the report
- When in doubt, ask rather than assume

## Limitations

This skill:
- Does not replace human code review
- Cannot detect all logical errors or bugs
- May not catch domain-specific anti-patterns without context
- Cannot run or interpret complex test suites automatically
- Cannot make architectural decisions requiring business context

Always combine automated cleanup with thoughtful human oversight.
