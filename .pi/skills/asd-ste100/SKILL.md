---
name: asd-ste100
description: "Simplified Technical English (ASD-STE100, Issue 9 / Jan 2025) writing assistant that applies STE principles to make developer-facing prose clear, plain, and easy to understand. Use when drafting or revising commit messages, pull-request descriptions, release notes, README.md / CHANGELOG.md entries, API or user guides, and other end-user-facing technical documentation. Trigger on: requests to make text 'plain', 'simple', 'clearer', 'concise', or 'easier to understand'; mentions of 'Simplified Technical English' or 'STE'; writing or rewording commit messages, PR descriptions, release notes, or docs; applying a short-sentence / active-voice / one-topic-per-sentence style. Delivers the operative STE writing-rules checklist plus before/after examples — it does NOT include the proprietary ~900-word approved-word dictionary."
---

# Simplified Technical English (ASD-STE100)

This skill applies **ASD-STE100 Simplified Technical English (STE)** — a controlled-language standard (current: **Issue 9, January 2025**) built from *writing rules* plus a *controlled dictionary* — to everyday developer prose. It exists to make **commit messages, pull-request descriptions, release notes, README/CHANGELOG entries, and other end-user-facing technical documentation** clear, short, and unambiguous, especially for readers who are not native English speakers. You get the operative rule checklist and worked examples here; the principles, rationale, and deep dives live in `references/`. You do **not** need the full proprietary spec for everyday writing.

## Quick Decision Tree

| You are writing… | Apply pattern | Then read reference |
|-------------------|---------------|---------------------|
| A commit message | `Commit messages` below | [`wikipedia-simplified-technical-english.md`](references/wikipedia-simplified-technical-english.md) (Writing rules) |
| A pull-request description | `Pull-request descriptions` below | [`ste-overview.md`](references/ste-overview.md) + [`ste-faq.md`](references/ste-faq.md) |
| A CHANGELOG / release-notes line | `Changelog & release notes` below | [`wikipedia-simplified-technical-english.md`](references/wikipedia-simplified-technical-english.md) (Writing rules) |
| A README / user-guide sentence | `User-facing docs prose` below | [`ste-overview.md`](references/ste-overview.md) (principles) |
| General prose — "make it clearer" | **The STE Writing Checklist** below | [`ste-faq.md`](references/ste-faq.md) |
| "Why does STE ban -ing / passive?" | — (rationale) | [`ste-faq.md`](references/ste-faq.md) (Rules & restrictions) |

## The STE Writing Checklist

The operative rules distilled into a tight checklist you can apply to any sentence. This is the heart of the skill. (Full rule list in [`wikipedia-simplified-technical-english.md`](references/wikipedia-simplified-technical-english.md); rationale in [`ste-faq.md`](references/ste-faq.md).)

### Sentences — short, one topic

- **Short sentences:** ≤ **20 words** for procedures/instructions, ≤ **25 words** for descriptive text. If a sentence is longer, split it.
- **One topic per sentence.** One instruction per sentence. Do not pack two ideas into one sentence.
- **One topic per paragraph,** ≤ **6 sentences.**
- **Do not omit the subject, verb, or article (a/an/the)** to shorten text — that removes clarity, not length.

### Voice & verbs — active, simple

- **Active voice.** Imperative for procedures and steps: *Add the flag.* — not *The flag must be added.*
- **Passive only in descriptive text when the doer is unknown:** *The data was corrupted during transmission.*
- **Simple verb forms only:** infinitive, imperative, simple present, simple past, simple future. Past participle only as an adjective.
- **No auxiliary-verb complexity** (*must be installed*, *will have been*). Pick a simple tense.
- **No "-ing" verb forms** — they cause ambiguity. (*opening* (n), *during* (prep) as fixed nouns/prepositions are fine.)

### Words — one word, one meaning

- **Approved-words mindset:** pick the single simplest word, drop synonyms. Use *start* (not begin/commence/initiate), *do* (not achieve/carry out/accomplish), *use* (not utilize).
- **Keep multi-word nouns ≤ 3 words.** Longer? Split or rename.
- **Consistent terminology:** the same concept gets the same word every time.
- **Keep articles** (a/an/the) where the reader needs them.

### Structure — condition first, concrete

- **Put the condition before the result:** *If the network times out, the client retries.* — not the reverse.
- **Start critical/safety instructions with a clear command or condition.**
- **Use vertical (bulleted/numbered) lists** for sequential or complex steps.
- **Be concrete and specific,** not vague. Name the thing.

## Core Patterns by Task

### Commit messages

- **Subject line ≤ ~50 chars, imperative mood:** *Fix login error when a session expires* — not *Fixed* / *Fixing* / *A fix for*.
- **No passive, no -ing.** *Add retry logic to the API client.* — not *Retry logic was added* / *Adding retry logic*.
- **Body = short sentences, one change per line.** State what changed, then why.
- **One commit = one logical change.** One topic per commit mirrors one topic per sentence.

### Pull-request descriptions

- **Lead with the change and its reason** in the first sentence (active voice).
- **Use a short bulleted list** for the changes — one change per bullet, ≤ 20 words each.
- **Separate "what changed" from "why" and "how to test."** One topic per section.
- **Active voice for what you did;** reserve passive for outcomes where the doer is irrelevant (*The flaky test was removed*).

### Changelog & release notes

- **One change per line, imperative or simple past, active voice:** *- Users can now export data to CSV.*
- **Drop marketing words** (*exciting*, *powerful*, *seamlessly*). State the capability.
- **Group by type** (Added / Changed / Fixed / Removed) so readers scan fast.

### User-facing docs prose (README / guides)

- **Short, active sentences.** One concept per sentence.
- **Direct address:** *Use this library to build dashboards.* — not *Developers are enabled to rapidly build…*.
- **Define each term once, then reuse the same word.** No synonym rotation.
- **Numbered lists for install/setup steps** (imperative: *Install the package. Add the config. Start the server.*).

## Worked Examples

These are illustrative syntheses (not copied from the references) showing the checklist applied to software-dev prose.

**Commit message**
- Before: *Refactored the authentication module and the login errors that users were experiencing when their session had expired have been resolved by adding a token refresh on each request*
- After:
  ```
  Fix login error when a session expires

  The auth module did not refresh expired tokens. Add a token refresh
  on each request. Users can now log in without errors.
  ```
  *(imperative subject, active voice, no -ing, one change per sentence, condition stated plainly)*

**CHANGELOG entry**
- Before: *A new feature has been added that is allowing users to be exporting their data to CSV format, which was a highly requested capability.*
- After: *- Added: users can export data to CSV.*
  *(active, one change per line, marketing words removed, ~9 words)*

**README sentence**
- Before: *Utilizing this library, developers are enabled to rapidly be building responsive dashboards that are featuring drag-and-drop widget functionality.*
- After: *Use this library to build responsive dashboards. Each dashboard supports drag-and-drop widgets.*
  *(active, direct address, no -ing, "utilize"→"use", two short sentences)*

## Reference Index

Load these on demand (progressive disclosure). Each is **verbatim** official or open-source material.

- [`ste-overview.md`](references/ste-overview.md) — What STE is, why it exists, its two parts (writing rules + controlled dictionary), history, STEMG. **Best entry point for the principles.**
- [`ste-faq.md`](references/ste-faq.md) — Official FAQ, verbatim. Richest deep dive: rule rationale (why no -ing / no passive), word-selection logic, misconceptions, and AI guidance. Read when you need the "why".
- [`wikipedia-simplified-technical-english.md`](references/wikipedia-simplified-technical-english.md) — The operative **Writing rules** list (≤20/25 words, active voice, one topic, etc.) plus a dictionary sample and tools. Read for the concrete rule list you apply.
- [`ste-whitepaper-ai.md`](references/ste-whitepaper-ai.md) — Official STEMG white paper on STE + AI: when to trust AI output vs. the spec, risks, safeguards. Read when judging LLM-generated STE text.
- [`ste-governance.md`](references/ste-governance.md) — How STE is maintained (STEMG + national support teams). Background only.
- [`ste-resources.md`](references/ste-resources.md) — Where to get the free official spec, certified training, and (non-endorsed) checking tools. Next steps for humans.
- [`README.md`](references/README.md) — Progressive-disclosure index of the references themselves + the licensing/scope note.

## Scope & Common Gotchas

- **No full word dictionary here.** The proprietary ~900-word approved/unapproved dictionary and the fully-enumerated ~65 rules are **not** reproduced (copyright). This skill gives *principles + the operative rules summary + examples* — enough to sharply improve everyday prose. Do **not** try to enforce a strict approved-word list you do not possess; apply the *mindset* (simplest word, one meaning), not the dictionary.
- **STE principles transfer; the strict dictionary does not.** For commit messages and PRs, apply sentence/voice/structure rules — a literal approved-word lock is not the goal.
- **Passive is not always wrong.** In *descriptive* text, passive is correct when the doer is unknown (*The build was cancelled.*). STE bans passive in *procedures*, not everywhere.
- **STE ≠ "baby English."** It is precise and structured for clarity, not a dumbed-down vocabulary. Keep technical terms (technical nouns/verbs) — STE explicitly permits subject-specific terms.
- **Don't strip articles or subjects** to shorten. Clarity first; length is a result, not the method.
- **Put the condition before the result** so the reader knows the trigger before the action.

<!--
Built with the skill prototyping pipeline (/prototype-skill).
References are VERBATIM source docs — never summarized. See references/ for full knowledge.
-->
