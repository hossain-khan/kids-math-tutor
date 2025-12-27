# Admin Portal Setup Guide

## Overview

The admin portal at `/manage-worksheets` allows you to:
- View all shared community worksheets
- Delete inappropriate or duplicate worksheets
- See worksheet statistics (views, downloads, ratings)

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

### 1. Set the Admin Password

Edit `webapp/wrangler.json` and update the admin password:

**For Development:**
```json
{
  "env": {
    "development": {
      "vars": {
        "ADMIN_PASSWORD": "your_secure_password_here"
      }
    }
  }
}
```

**For Production:**
```json
{
  "env": {
    "production": {
      "vars": {
        "ADMIN_PASSWORD": "your_very_secure_password_here"
      }
    }
  }
}
```

**Important:**
- Use a strong, unique password
- Never commit the actual password to version control
- In production, use a secrets manager instead (see below)

### 2. Using Environment Variables in Cloudflare

For production deployments, use Cloudflare's environment variables instead of hardcoding:

```bash
# Set the admin password as a secret on Cloudflare
wrangler secret put ADMIN_PASSWORD

# When prompted, enter your secure password
```

Then update `wrangler.json` to reference it:

```json
{
  "env": {
    "production": {
      "vars": {}
      // ADMIN_PASSWORD will come from secrets
    }
  }
}
```

Deploy with:
```bash
wrangler deploy --env production
```

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
  "message": "Worksheet deleted successfully"
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
- For production, use `wrangler secret list` to verify it's set
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
   - Use `wrangler secret put` for production

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

