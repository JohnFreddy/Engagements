# Submission Notes

## Assumptions Made

- Version numbers are just identifiers, not counters. The sample data starts at v3, v6 and v10, so
  I take the order from the template catalog instead of doing math on the numbers.
- Diff paths point to map keys, not array positions. The AUDIT-CA v5 fragment shows `questions` as
  an object with keys `"3"` and `"7"`, so adding at "12" is not an array insert.
- Section names like "Inquiries" only exist in the template content, not in the diff. To show them
  to the user, you need to read the template. This is one reason the summary is built on the server.
- No apply or decline history. Each engagement's saved version is its starting point.
- Applying the content is out of scope. A decision returns `202`, which means we saved the answer,
  not that the file is updated yet.
- The AUDIT-CA v5 fragment does not include the `items` list that the v4→v5 diff adds. I treated
  the fragment as a short example, not as a conflict.

## AI Usage

I used Claude for most of this, both for design discussion and for writing code. I ran everything
myself, and I can explain every part of it.

### Where AI helped

The most useful thing was comparing the sample diffs carefully. If you join the REVIEW-CA v6→v7 and
v7→v8 diffs, you get 6 changes, and the analytics tolerance appears twice: 0.15 → 0.12, then
0.12 → 0.10. But the collapsed v6→v8 diff in the fixtures has only 5 changes, and it shows
0.15 → 0.10. The 0.12 never existed in the customer's file. Showing it would tell a practitioner
the threshold moved twice, which is wrong. This became the main idea of the design and the reason
`DiffCoalescer` exists.

Before writing any Java, I tested the merge rules against the real fixture files to confirm they
produce exactly the collapsed v6→v8 diff. The Java test checks the same thing.

It also helped with the API contract first, then both sides against it, and with the repetitive
parts: records, the Angular store, the fixture data.

### Where I corrected, rewrote, or ignored AI output

Most problems were about tools and versions, not about the design:

- It suggested SDKMAN to install Java. It does not work on macOS because Apple still ships Bash 3.2.
  I used Homebrew instead.
- It gave me a Java version number that does not exist (`25.0.2-tem`).
- When copying the contract file into the client, it added a comment in the wrong place and broke
  the comment block. That gave me 8 TypeScript errors.
- It guessed wrong about an Angular CLI flag. The real problem was that my global CLI was still
  version 15, hidden by an old `ng` binary in `/usr/local/bin`.
- The CI file used Karma, but Angular 21 now uses Vitest.
- The Vercel output folder was wrong, so the first deploy showed a 404.

I also decided not to add Tailwind or any styling, even though it would look nicer. The instructions
say styling is not evaluated and unstyled markup is expected.

In short: it was good at thinking about the data I gave it, and not reliable about versions,
commands and paths. Those answers always sound confident, so you have to run them.

### How I would guide other engineers using AI on this system

- Give it the real data, not only the description. Everything useful here came from the fixtures.
- Ask it to test an idea against the data before writing the final code.
- Never trust a version number, command or file path without running it.
- Fix the API contract first. Then both sides stay consistent.

### Where AI should not be trusted in this domain

It should not decide what changed. In my design, the rules engine classifies the changes, and the
model is only allowed to rewrite that list in nicer words. The output is checked against the list
before we save it.

The reason is simple. If the model writes a nice sentence saying an update makes a requirement
weaker, when it actually makes it stricter, that is a serious problem in an audit product. A boring
sentence is safer than a wrong one.

Summaries are also cached and may be used months later, so every summary is saved together with the
version of the generator that produced it. If someone asks later why the system said something, we
can answer.

## Approximate Time Spent

- Design and implementation: about 2 hours
- Setting up the environment (Java, Angular CLI, deploy): about 30 min

## What I Would Do Next

1. A job that checks the projection against reality. It would load a few real engagement files at
   night and compare versions. Right now, if we miss an event, nothing would notice. This is the
   biggest gap.
2. Tests for repeated or out-of-order events. I assume they work, but I did not prove it.
3. The text generation layer for nicer summaries, with validation and a feature flag.
4. A test that the same diff always produces the same summary. Caching is only safe if this is true.
5. Show the per-version breakdown in the UI. The data is already in the contract and in the store,
   the panel just does not display it yet.

If I had less time, I would have cut things in the order the instructions suggest: fewer tests
first, then Decline, then the detail of the summary. In the end all three are included, and the
design, the API contract, the Java and the Angular code are consistent with each other.
