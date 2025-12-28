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
npx wrangler secret put ADMIN_PASSWORD --env production
# Enter your secure password when prompted

# Verify it was set:
npx wrangler secret list --env production
```

⚠️ **Important:** Production passwords MUST use Cloudflare Secrets, not `wrangler.json`

### What You Can Do

✅ **View all shared worksheets** with stats (views, downloads, ratings)  
✅ **See worksheet details** (title, problems, grades, creation date)  
✅ **Delete inappropriate worksheets** with confirmation  
✅ **Check all content for safety** using AI-based content filtering  
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

**Running bulk safety checks:**
- Click "Check All Content" button to scan all worksheets
- See real-time progress bar as worksheets are checked
- View results with color-coded badges:
  - ✅ Green: Content is safe
  - ⚠️ Yellow: Potential safety issues detected
- Click on flagged worksheets to expand details and see why they were flagged
- Check runs with rate limiting (5 worksheets per batch) to stay within AI quota
- Results are calculated on-demand (no data stored)

**Session expired:**
- Page will ask for password again
- 24-hour sessions for security
- Click "Logout" to manually clear session

