import sharp from 'sharp';
import fs from 'fs';
import path from 'path';

const publicDir = path.resolve(process.cwd(), 'public');
if (!fs.existsSync(publicDir)) {
  fs.mkdirSync(publicDir, { recursive: true });
}

// Crisp Arc Reactor SVG for T-HACK AI
const svgIcon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <defs>
    <radialGradient id="bgGrad" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#07121a" />
      <stop offset="85%" stop-color="#030609" />
      <stop offset="100%" stop-color="#010204" />
    </radialGradient>
    <radialGradient id="coreGlow" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#78f7ff" stop-opacity="1" />
      <stop offset="35%" stop-color="#00e5ff" stop-opacity="0.8" />
      <stop offset="70%" stop-color="#007c91" stop-opacity="0.3" />
      <stop offset="100%" stop-color="#00e5ff" stop-opacity="0" />
    </radialGradient>
    <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#78f7ff" />
      <stop offset="100%" stop-color="#00e5ff" />
    </linearGradient>
  </defs>

  <!-- Background -->
  <rect width="512" height="512" rx="100" fill="url(#bgGrad)" />

  <!-- Outer Tech Ring -->
  <circle cx="256" cy="256" r="210" fill="none" stroke="#007c91" stroke-width="3" stroke-dasharray="16 10" opacity="0.6" />
  <circle cx="256" cy="256" r="185" fill="none" stroke="#00e5ff" stroke-width="4" opacity="0.8" />

  <!-- Arc Core Glow -->
  <circle cx="256" cy="256" r="140" fill="url(#coreGlow)" />

  <!-- Segments of Arc Reactor -->
  <g stroke="#00e5ff" stroke-width="12" stroke-linecap="round" fill="none">
    <path d="M 256 105 A 151 151 0 0 1 386 180" />
    <path d="M 407 256 A 151 151 0 0 1 386 332" />
    <path d="M 256 407 A 151 151 0 0 1 126 332" />
    <path d="M 105 256 A 151 151 0 0 1 126 180" />
  </g>

  <!-- Inner Rings & Core -->
  <circle cx="256" cy="256" r="110" fill="none" stroke="#31f5a3" stroke-width="3" stroke-dasharray="6 8" opacity="0.8" />
  <circle cx="256" cy="256" r="75" fill="#07121a" stroke="#00e5ff" stroke-width="6" />
  <circle cx="256" cy="256" r="45" fill="#78f7ff" />

  <!-- T-HACK Central Emblem Symbol -->
  <path d="M 226 230 L 286 230 M 256 230 L 256 282" stroke="#030609" stroke-width="12" stroke-linecap="square" />
  <circle cx="256" cy="256" r="16" fill="none" stroke="#00e5ff" stroke-width="4" opacity="0.9" />
</svg>`;

// Write SVG
fs.writeFileSync(path.join(publicDir, 'favicon.svg'), svgIcon);
fs.writeFileSync(path.join(publicDir, 'icon.svg'), svgIcon);

async function generatePngs() {
  const svgBuffer = Buffer.from(svgIcon);

  // 192x192
  await sharp(svgBuffer)
    .resize(192, 192)
    .png()
    .toFile(path.join(publicDir, 'pwa-192x192.png'));

  // 512x512
  await sharp(svgBuffer)
    .resize(512, 512)
    .png()
    .toFile(path.join(publicDir, 'pwa-512x512.png'));

  // 512x512 Maskable with safe area margin
  await sharp(svgBuffer)
    .resize(410, 410)
    .extend({
      top: 51,
      bottom: 51,
      left: 51,
      right: 51,
      background: '#030609'
    })
    .png()
    .toFile(path.join(publicDir, 'pwa-maskable-512x512.png'));

  // 180x180 Apple touch icon
  await sharp(svgBuffer)
    .resize(180, 180)
    .png()
    .toFile(path.join(publicDir, 'apple-touch-icon.png'));

  console.log('PWA icons successfully generated in /public');
}

generatePngs().catch(console.error);
