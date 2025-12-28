## Admin Portal Quick Start

### Access the Admin Portal
```
https://math-worksheet.gohk.xyz/manage-worksheets
```

### Set Your Admin Password

**Development:**
```json
"env": {
  "development": {
    "vars": {
      "ADMIN_PASSWORD": "dev_password"
    }
  }
}
```

**Production (Cloudflare Secrets - REQUIRED):**
```bash
wrangler secret put ADMIN_PASSWORD --env production
# Enter your secure password when prompted

# Verify it was set:
wrangler secret list --env production
```

⚠️ **Important:** Production passwords MUST use Cloudflare Secrets, not `wrangler.json`

### What You Can Do

✅ **View all shared worksheets** with stats (views, downloads, ratings)  
✅ **See worksheet details** (title, problems, grades, creation date)  
✅ **Delete inappropriate worksheets** with confirmation  
✅ **View worksheet links** to check before deletion  

### Security

- 🔐 Password-protected access only  
- 🕐 Automatic logout after 24 hours  
- 🔒 Secure token-based authentication  
- ✅ No passwords in URLs  

### Common Tasks

**Viewing a worksheet before deleting:**
- Click the "View →" link to open in new tab
- Check if it's appropriate or duplicate
- Return to admin panel to delete if needed

**Deleting a worksheet:**
- Click the "Delete" button
- Review the worksheet (optional)
- Click "Confirm" in the dialog
- Worksheet is permanently removed

**Session expired:**
- Page will ask for password again
- 24-hour sessions for security
- Click "Logout" to manually clear session

