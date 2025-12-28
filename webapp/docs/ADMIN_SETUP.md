# Admin Portal Setup Guide

## Overview

The admin portal at `/manage-worksheets` allows you to:
- View all shared community worksheets
- Delete inappropriate or duplicate worksheets
- See worksheet statistics (views, downloads, ratings)
- Run bulk AI safety checks on all worksheets
- Identify and flag content with safety concerns

The portal is protected with password authentication that works seamlessly with Cloudflare Workers.

## How It Works

### Authentication Architecture

1. **Password-based authentication** - You enter a password on the login page
2. **Token generation** - A token is created server-side and stored in browser localStorage
3. **Token validation** - All admin API requests validate the token before proceeding
4. **Token expiry** - Tokens automatically expire after 24 hours for security

### Security Features

- ✅ Password protected access
- ✅ Server-side token validation
- ✅ Automatic session expiration (24 hours)
- ✅ No authentication tokens in URLs
- ✅ Protected API endpoints with Bearer token validation

## Setup Instructions

### 1. Development Setup

Edit `webapp/wrangler.json` and set the admin password for development:

```json
{
  "env": {
    "development": {
      "vars": {
        "ADMIN_PASSWORD": "your_development_password"
      }
    }
  }
}
```

Then run:
```bash
cd webapp
npx wrangler dev --env development
# Visit http://localhost:5173/manage-worksheets
```

**Note:** Keep development passwords simple for testing. Do not commit actual passwords to version control.

### 2. Production Setup

For production, **you MUST use Cloudflare Secrets** to securely store the password:

```bash
# Set the admin password as a secret on Cloudflare
npx wrangler secret put ADMIN_PASSWORD --env production

# When prompted, enter your secure production password
```

You can verify the secret was set:
```bash
npx wrangler secret list --env production
```

**Important Security Notes:**
- 🔐 **Never store the production password in `wrangler.json`**
- ✅ Use Cloudflare Secrets for all sensitive credentials
- 📝 The `production` environment in `wrangler.json` has NO `vars` for `ADMIN_PASSWORD`
- 🔄 The application automatically uses secrets when available, with fallback to vars for development

Deploy with:
```bash
npx wrangler deploy --env production
```

**⚠️ Critical Configuration**: When deploying to production, your `wrangler.json` production environment MUST include the worker name to prevent creating a separate unrouted worker:

```json
{
  "env": {
    "production": {
      "name": "pup-tutor-worksheet-generator",  // ← REQUIRED
      "vars": {},
      "workers_dev": false
    }
  }
}
```

Without this `name` field, Cloudflare creates a separate `pup-tutor-worksheet-generator-production` worker without routes, causing the site to be unreachable. See the main [README.md](./README.md#environment-configuration) for details.

### 3. Access the Admin Portal

1. Navigate to: `https://math-worksheet.gohk.xyz/manage-worksheets`
2. Enter your admin password
3. You'll get a 24-hour session token automatically
4. Browse and manage worksheets

### 4. Logging Out

Click the "Logout" button to:
- Clear your session token
- Return to the login screen
- Force re-authentication on next visit

## API Endpoints

The following endpoints are protected and require admin authentication:

### POST `/api/v1/admin/auth`
**Purpose:** Get an authentication token

**Request:**
```json
{
  "password": "your_admin_password"
}
```

**Response (success):**
```json
{
  "token": "base64_encoded_token",
  "expiry": 1234567890000,
  "message": "Authentication successful"
}
```

**Response (failure):**
```json
{
  "error": "Invalid password"
}
```

### GET `/api/v1/admin/worksheets`
**Purpose:** List all shared worksheets

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "worksheets": [...],
  "total": 42
}
```

### DELETE `/api/v1/admin/worksheets/:id`
**Purpose:** Delete a worksheet by ID

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "

### POST `/api/v1/admin/check-safety`
**Purpose:** Bulk AI safety check for all worksheets

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body (optional):**
```json
{
  "worksheetIds": ["id1", "id2", "id3"]
}
```

**Response:**
```json
{
  "results": [
    {
      "worksheetId": "abc123",
      "title": "Addition Basics",
      "safe": true,
      "categories": [],
      "explanation": null,
      "method": "AI-based safety check",
      "confidence": 0.95,
      "lastChecked": 1703779200000
    },
    {
      "worksheetId": "def456",
      "title": "Problematic Content",
      "safe": false,
      "categories": ["profanity"],
      "explanation": "Contains inappropriate language",
      "method": "AI-based safety check",
      "confidence": 0.92,
   Recent Enhancements

Recently implemented:
- ✅ **Bulk AI Safety Checks** - Scan all worksheets for inappropriate content using Llama Guard 3
  - Real-time progress tracking
  - Color-coded safety badges (safe/flagged)
  - Expandable details showing flagged categories
  - See [AI_SAFETY.md](./AI_SAFETY.md) for technical details

## Future Enhancements

Possible improvements:
- Two-factor authentication (2FA)
- Role-based access control (RBAC)
- Audit logging for delete actions
- JWT tokens instead of simple Base64
- Email notifications on worksheet deletions
- Automatic scheduled safety checks
- Safety check result history and trends
}
```

**Features:**
- Batch processing with rate limiting (5 worksheets per batch, 100ms delays)
- Uses Llama Guard 3 AI for content safety classification
- Graceful fallback to pattern-based filtering if AI unavailable
- Returns confidence scores for each check
- Works within Cloudflare Workers AI free tier (10,000 neurons/day)message": "Worksheet deleted successfully"
}
```

## Frontend Token Management

Tokens are stored and managed automatically in `src/lib/adminAuth.ts`:

```typescript
// Get current token (returns null if expired)
const token = getAdminAuthToken();

// Check if admin is authenticated
if (isAdminAuthenticated()) {
  // User can access admin features
}

// Clear token on logout
clearAdminAuthToken();
```

## Troubleshooting

### "Unauthorized" Error
- Token may have expired (24 hour limit)
- Password in `wrangler.json` doesn't match what you entered
- Session was cleared (check browser console)

**Solution:** Click "Logout" and log back in

### "Connection Error"
- Cloudflare Worker may be down
- Check: `https://math-worksheet.gohk.xyz/api/health`

### Can't Remember Password
- Check `wrangler.json` in development
- For production, use `npx wrangler secret list` to verify it's set
- Contact your infrastructure team if needed

## Future Enhancements

Possible improvements:
- Two-factor authentication (2FA)
- Role-based access control (RBAC)
- Audit logging for delete actions
- JWT tokens instead of simple Base64
- Email notifications on worksheet deletions
- Bulk operations (delete multiple, etc.)
- Worksheet statistics dashboard

## Security Best Practices

1. **Use Strong Passwords**
   - At least 16 characters
   - Mix of uppercase, lowercase, numbers, special characters
   - No dictionary words or personal info

2. **Rotate Passwords Regularly**
   - Change admin password every 3-6 months
   - Rotate after staff changes

3. **Use Secrets in Production**
   - Don't hardcode passwords in version control
   - Use `npx wrangler secret put` for production

4. **Monitor Access**
   - Keep logs of who accessed the portal
   - Review deleted worksheets periodically

5. **Session Management**
   - Tokens expire after 24 hours
   - Always log out when done
   - Clear browser data before shared devices

## Testing

To test the admin portal locally:

1. Update `wrangler.json` development password
2. Run: `pnpm dev`
3. Visit: `http://localhost:5173/worksheets/manage`
4. Enter the password from `wrangler.json`
5. Should see list of shared worksheets

