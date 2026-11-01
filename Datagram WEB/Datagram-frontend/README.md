# Datagram frontend

Migrated from Angular 9.1 to Angular/CLI 22.2.4, TypeScript 6.0 and RxJS 7.8.
Use Node.js 24 (the tested version is pinned in `.nvmrc`).

```sh
nvm use
npm ci
npm start
npm run build
npm test -- --watch=false
```

The development server runs at http://localhost:4200. The default build is
production with AOT and optimization; output is `dist/primeiro-projeto/browser`.
For a development build, run `npm run build -- --configuration development`.
Tests use the Angular CLI Vitest builder and jsdom, without a browser download.
The legacy Protractor and TSLint targets have been retired; their old configuration
files are historical and are not used by these commands.

NgModule components explicitly retain eager change detection and Zone.js so that
existing HTTP subscriptions continue to update templates under Angular 22.
The existing non-strict TypeScript policy is explicit because TypeScript 6 enables
strict mode by default. Template checking remains enabled.

The component CSS budget is 125 kB warning / 150 kB error to accommodate the
existing manager theme (about 121 kB optimized); the initial bundle budget is
unchanged. Bootstrap 4 markup and its jQuery/Popper integration are retained.

The backend already targets Java 21 in `../Datagram-backend/pom.xml`.
With JDK 21 and Maven installed, validate it using `mvn clean verify` in that folder.
