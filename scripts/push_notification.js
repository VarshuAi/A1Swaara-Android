#!/usr/bin/env node

/**
 * A1 Swaara Cloud Push Notification Publisher
 * Broadcasts push notifications to all A1 Swaara users via Cloud Firestore.
 *
 * Usage:
 *   node scripts/push_notification.js "Title" "Message body" [optionalActionUrl]
 */

const https = require('https');

const args = process.argv.slice(2);
const title = args[0] || 'A1 Swaara Update';
const message = args[1] || 'A new update and features are available in A1 Swaara!';
const actionUrl = args[2] || 'https://github.com/VarshuAi/A1Swaara_apk';

const id = 'broadcast_' + Date.now();
const timestamp = Date.now().toString();

const postData = JSON.stringify({
  fields: {
    id: { stringValue: id },
    title: { stringValue: title },
    message: { stringValue: message },
    actionUrl: { stringValue: actionUrl },
    enabled: { booleanValue: true },
    timestamp: { integerValue: timestamp }
  }
});

const apiKey = process.env.SWAARA_CLOUD_API_KEY || '';
const projectId = process.env.SWAARA_CLOUD_PROJECT_ID || 'swaara-cloud';

const options = {
  hostname: 'firestore.googleapis.com',
  path: `/v1/projects/${projectId}/databases/(default)/documents/notifications/broadcast?key=${apiKey}`,
  method: 'PATCH',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(postData)
  }
};

console.log('Broadcasting push notification to A1 Swaara users...');
console.log('ID:       ' + id);
console.log('Title:    ' + title);
console.log('Message:  ' + message);
console.log('Action:   ' + actionUrl);

const req = https.request(options, (res) => {
  let body = '';
  res.on('data', chunk => body += chunk);
  res.on('end', () => {
    if (res.statusCode >= 200 && res.statusCode < 300) {
      console.log('\nSUCCESS: Push notification broadcasted to all users!');
    } else {
      console.error('\nERROR: Failed to broadcast notification (' + res.statusCode + ')');
      console.error(body);
    }
  });
});

req.on('error', (err) => {
  console.error('\nNetwork error:', err.message);
});

req.write(postData);
req.end();
