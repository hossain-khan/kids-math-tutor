## Admin Portal Quick Start

### Access the Admin Portal
```
https://math-worksheet.gohk.xyz/worksheets/manage
```

### Set Your Admin Password

**Development (in `wrangler.json`):**
```json
"env": {
  "development": {
    "vars": {
      "ADMIN_PASSWORD": "your_password"
    }
  }
}
```

**Production (using Cloudflare secrets):**
```bash
wrangler secret put ADMIN_PASSWORD
# Enter your password when prompted
```

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

