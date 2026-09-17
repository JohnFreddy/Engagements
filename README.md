# Template update review

Answers two questions for a firm's engagement files without ever loading one (~1 minute each):
which engagements are behind their product template, and what changes if the update is applied.

```
api/       client/server contract, the single source of truth
server/    Java 25, plain classes, no framework  (Part 2)
client/    Angular 21, signals, fixture-backed   (Part 3)
data/      the supplied sample data
```

## Server

Requires JDK 25 and Maven. On macOS:

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"   # if needed
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 25.0.2-tem     # check `sdk list java | grep tem` for the current 25.x
sdk install maven
java -version && mvn -v
```

Add `source "$HOME/.sdkman/bin/sdkman-init.sh"` to `~/.zshrc` if the installer did not.

```bash
cd server && mvn test
```

There is no HTTP layer, persistence or runnable service, by design — the exercise asks for domain
models and core logic.

## Client

Requires Node 24+.

```bash
npm install -g @angular/cli@21
cd client
ng new client --standalone --style=css --ssr=false --skip-git   # scaffold once, then copy src/ over
npm install
ng serve      # http://localhost:4200
ng test
```

No CSS, by design. No HTTP either: `FixtureTemplateUpdateGateway` serves `data/` shapes from
memory, and swapping it for an `HttpClient` implementation is one provider in `app.config.ts`.

## Deploying the client

Vercel has no Java runtime, so only the Angular app deploys there. That is consistent with the
exercise: the client is fixture-backed and needs no backend.

```bash
# Vercel project settings
Build command:      ng build
Output directory:   dist/client/browser
Install command:    npm install
```

The Java module is verified by `mvn test` in CI rather than deployed.

## Where the diff becomes readable text

On the server, at summary-generation time, cached under `(templateId, fromVersion, toVersion)`.
Three reasons, in order of weight:

1. Section display names live in the template content, not the diff. The diff says
   `/sections/inquiries/...`; only the template says that section is called "Inquiries". The
   client cannot resolve that without a second fetch it has no business making.
2. The summary is identical for every firm on the same version pair. In the sample data, twelve
   engagements reduce to six cached summaries. Computing per client throws that away.
3. The text has to be reproducible. `generatorVersion` is stored with every summary so a summary
   a practitioner relied on can be explained months later.

## Accumulated updates

An engagement two releases behind gets one pending update spanning both, not two decisions.

The net change set is what the user sees. REVIEW-CA moves the analytics tolerance 0.15 to 0.12 in
v6 to v7 and 0.12 to 0.10 in v7 to v8. Concatenating the two diffs reports the threshold changing
twice and shows 0.12, a value an engagement on v6 has never held. `DiffCoalescer` folds the chain
to the five net changes that the supplied collapsed v6 to v8 diff also reports; that equivalence
is asserted in `DiffCoalescerTest`.
