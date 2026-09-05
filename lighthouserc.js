const baseUrl = (process.env.LHCI_BASE_URL || 'http://localhost:4200').replace(/\/$/, '');
const preset = process.env.LHCI_PRESET || 'mobile';

module.exports = {
  ci: {
    collect: {
      url: [`${baseUrl}/login`, `${baseUrl}/registro`],
      numberOfRuns: 3,
      settings: preset === 'desktop' ? { preset: 'desktop' } : {},
    },
    assert: {
      assertions: {
        'categories:performance': ['warn', { minScore: 0.80 }],
        'categories:accessibility': ['error', { minScore: 0.90 }],
        'categories:best-practices': ['error', { minScore: 0.90 }],
        'categories:seo': ['warn', { minScore: 0.80 }],
      },
    },
    upload: {
      target: 'filesystem',
      outputDir: process.env.LHCI_OUTPUT_DIR || '.lighthouseci/reports',
    },
  },
};
