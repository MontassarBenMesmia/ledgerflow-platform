import playwright from '../frontend/node_modules/playwright/index.js';
const { chromium } = playwright;

const browser = await chromium.launch({ channel: 'msedge', headless: true });
const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 });
await page.goto('http://localhost', { waitUntil: 'networkidle' });
if (await page.locator('#username').count()) {
  await page.locator('#username').fill('operator');
  await page.locator('#password').fill('operator123');
  await page.locator('#kc-login').click();
}
await page.getByRole('heading', { name: 'Ledger overview' }).waitFor();
await page.waitForTimeout(1500);
await page.screenshot({ path: 'docs/screenshots/dashboard.png', fullPage: true });
await browser.close();
