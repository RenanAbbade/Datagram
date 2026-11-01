import { defineConfig, devices } from '@playwright/test';
export default defineConfig({
  testDir: 'e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 45000,
  use: { baseURL: 'http://127.0.0.1:4200', trace: 'retain-on-failure' },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      command:
        'java -jar ../Datagram-backend/target/datagram-web-0.0.1-SNAPSHOT.jar --server.port=8080',
      env: {
        SPRING_PROFILES_ACTIVE: '',
        DATABASE_URL: 'jdbc:h2:mem:datagram-e2e',
        DATABASE_USER: 'sa',
        DATABASE_PASSWORD: '',
        JWT_SECRET: '',
        MIGRATE_LEGACY_PASSWORDS: 'false',
        CORS_ORIGIN: 'http://127.0.0.1:4200',
      },
      port: 8080,
      reuseExistingServer: false,
      timeout: 60000,
    },
    {
      command: 'npm start -- --host 127.0.0.1',
      url: 'http://127.0.0.1:4200',
      reuseExistingServer: false,
      timeout: 60000,
    },
  ],
});
