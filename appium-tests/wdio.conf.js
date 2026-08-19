// wdio.conf.js — mirrors the Selenium e2e conftest.py structure for Appium
// Triggers: same as selenium-e2e.yml (push / PR / workflow_dispatch)

const path = require('path');
const fs   = require('fs');
const XLSX = require('xlsx');
// Make Node's assert available globally in all spec files (like pytest's assert)
global.assert = require('assert');

const APK_PATH = process.env.APK_PATH
  || path.resolve(__dirname, '../SkillSyncAndroid/app/build/outputs/apk/debug/app-debug.apk');

const REPORT_PATH = process.env.APPIUM_REPORT_PATH
  || path.resolve(__dirname, 'reports/appium-test-report.xlsx');

// Collect results across all specs (mirrors conftest.py REPORT_ROWS)
const REPORT_ROWS = [];

exports.config = {
  runner: 'local',
  port: 4723,

  specs: [
    './tests/login-tests.js',
    './tests/dashboard-tests.js',
    './tests/roadmap-tests.js',
    './tests/chat-tests.js',
    './tests/resume-upload-tests.js',
  ],

  maxInstances: 1,

  capabilities: [{
    platformName: 'Android',
    'appium:automationName': 'UiAutomator2',
    'appium:app': APK_PATH,
    'appium:appPackage': 'com.skillsync.app',
    'appium:appActivity': 'com.skillsync.app.SplashActivity',
    'appium:noReset': false,
    'appium:fullReset': false,
    'appium:autoGrantPermissions': true,
    'appium:newCommandTimeout': 60,
    'appium:androidInstallTimeout': 120000,
    'appium:uiautomator2ServerInstallTimeout': 60000,
  }],

  logLevel: 'warn',
  bail: 0,
  waitforTimeout: 10000,
  connectionRetryTimeout: 120000,
  connectionRetryCount: 3,

  // Appium is started externally in CI (appium & in the workflow script)
  // wdio connects to it at localhost:4723 — no service needed
  services: [],

  framework: 'mocha',
  reporters: [
    'spec',
    ['json', { outputDir: './reports', outputFileFormat: () => 'wdio-results.json' }],
  ],

  mochaOpts: {
    ui: 'bdd',
    timeout: 60000,
  },

  // ── Hooks — mirrors conftest.py pytest_runtest_logreport ──

  afterTest(test, context, { error, result, duration, passed, retries }) {
    const suiteName = (test.parent || '').replace(/\s+/g, ' ').trim();
    const status    = passed ? 'PASSED' : (error && error.message === 'pending' ? 'SKIPPED' : 'FAILED');
    REPORT_ROWS.push({
      suite:        suiteName,
      test_name:    test.fullTitle || `${suiteName} > ${test.title}`,
      status,
      duration:     `${Math.round(duration)}ms`,
      error:        error ? String(error.message || error) : '',
    });
  },

  // ── onComplete — mirrors conftest.py pytest_sessionfinish ──
  onComplete(exitCode, config, capabilities, results) {
    const dir = path.dirname(REPORT_PATH);
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });

    const wb = XLSX.utils.book_new();

    // ── Sheet 1: Summary ──
    const total   = REPORT_ROWS.length;
    const passed  = REPORT_ROWS.filter(r => r.status === 'PASSED').length;
    const failed  = REPORT_ROWS.filter(r => r.status === 'FAILED').length;
    const skipped = REPORT_ROWS.filter(r => r.status === 'SKIPPED').length;
    const pct     = total > 0 ? `${((passed / total) * 100).toFixed(1)}%` : '0%';

    const summaryData = [
      ['Generated At',  new Date().toISOString()],
      ['Total Tests',   total],
      ['Passed',        passed],
      ['Failed',        failed],
      ['Skipped',       skipped],
      ['Pass Rate',     pct],
      ['Exit Code',     exitCode],
    ];
    const summarySheet = XLSX.utils.aoa_to_sheet(summaryData);
    summarySheet['!cols'] = [{ wch: 18 }, { wch: 32 }];
    XLSX.utils.book_append_sheet(wb, summarySheet, 'Summary');

    // ── Sheet 2: Full test details ──
    const headers = ['Suite', 'Test Name', 'Status', 'Duration', 'Error'];
    const detailRows = REPORT_ROWS.map(r => [
      r.suite, r.test_name, r.status, r.duration, r.error,
    ]);
    const detailSheet = XLSX.utils.aoa_to_sheet([headers, ...detailRows]);
    detailSheet['!cols'] = [
      { wch: 30 }, { wch: 90 }, { wch: 12 }, { wch: 14 }, { wch: 80 },
    ];
    XLSX.utils.book_append_sheet(wb, detailSheet, 'Test Details');

    XLSX.writeFile(wb, REPORT_PATH);
    console.log(`\n📊 Appium Excel report written → ${REPORT_PATH}`);
    console.log(`   Total: ${total} | Passed: ${passed} | Failed: ${failed} | Skipped: ${skipped} | Pass Rate: ${pct}`);
  },
};
