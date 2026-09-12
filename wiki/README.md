# Ashcore WIKI

English documentation for Ashcore 1.2.0, built from the shared WIKI template.
It retains the template's dark and light themes, responsive navigation, local search,
code copying, section links, and interactive figures. It runs entirely from local browser assets.

## Preview

From the repository root, run:

```shell
node wiki/preview.mjs
```

Open [the local preview](http://127.0.0.1:4173). You can also open `wiki/index.html`
directly. All article URLs use hash routes, such as `#/raycasting`, so refreshing
a page works under the GitHub Pages repository path.

Set the `PORT` environment variable if 4173 is in use. Stop the server with Ctrl+C.

## Edit and verify

Use Node.js 20 or newer. Run these commands in `wiki`:

```shell
npm ci
npm run dev
```

The development command rebuilds CSS when sources change. Refresh the browser
after saving. Edit `src/*.css`, not the generated `assets/styles.css`.

| File | Content |
| --- | --- |
| `content/site.js` | Product version, navigation, and external links |
| `content/pages.js` | Shared HTML helpers and article collection |
| `content/getting-started.js` | Overview, installation, and first example |
| `content/math-geometry.js` | Math, transforms, geometry, rays, and collisions |
| `content/random-utilities.js` | Sampling, noise, statistics, and utilities |
| `content/reference.js` | Public API directory, migration, and troubleshooting |
| `authoring/` | Source records and validation notes for maintainers |

Each article has a stable page ID, description, kind, and sections with stable IDs.
Use `#/page-id?section=section-id` for links to a specific section. Add new content
scripts to `index.html` before `assets/app.js`; validation reads that same script list.
Keep code examples complete and use the real package and method names.

Before submitting changes:

```shell
npm run build
npm run check:examples
```

The example check requires JDK 21+ on PATH or in `JAVA_HOME`. It compiles the actual
standalone Java blocks against `src/main/java`, then runs them with assertions enabled.
Temporary files are written beneath the ignored `.verification` directory. It does
not require Maven, a Minecraft server, or downloaded production dependencies.

`npm run build` compiles Tailwind, checks content, navigation, local assets, internal
links, and the version against `pom.xml`, then creates `wiki/_site`. The generated CSS
is tracked so the local preview works without installing Node dependencies.

## GitHub Pages

The workflow [Ashcore WIKI](../.github/workflows/pages.yml) validates pull requests.
On `master`, it also deploys the browser files to the `github-pages` environment.
It can be run manually; deployment remains restricted to `master`.

To enable publication after reviewing the site:

1. Set **Settings → Pages → Build and deployment → Source** to **GitHub Actions**.
2. Merge the reviewed WIKI changes into `master` and push that branch.
3. Check the **Ashcore WIKI** run in Actions. Open the deployment URL reported by its deploy job.
4. Verify navigation, search, code copying, and a refreshed section URL on the published site.

The expected URL for this repository is [miciasty.github.io/Ashcore](https://miciasty.github.io/Ashcore/).
Creating local files does not enable or publish that address.
See [GitHub's custom workflow instructions](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

Only `index.html`, `.nojekyll`, `assets`, `content`, `LICENSE`, and `NOTICE` enter the
Pages artifact. Source styles, Node dependencies, scripts, and authoring notes stay
outside the publication directory. No CDN, font service, analytics, or backend is required.

## Content basis

The source and POM match the local `v1.2.0` tag. The WIKI describes a Java library;
the template's fictional plugin commands, permissions, configuration, and release
history have been replaced. The language and visual decisions are recorded in
[the authoring notes](authoring/SOURCES.md).
