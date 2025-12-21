# Badge Design & Export Guide

This guide explains how to create and integrate new badge designs for the Kids Math Pup Tutor app.

## Design Requirements

### Visual Style
- **Theme**: Match the Math Pup mascot design (friendly, educational, child-appropriate)
- **Style**: Cartoon illustration with clear lines and vibrant colors
- **Size**: Design at high resolution (512x512px minimum) for scaling
- **Colors**: Use bright, appealing colors suitable for K-2 children
- **Consistency**: Maintain visual consistency with existing badges

### Badge Elements
- **Central Icon**: Clear visual representation of the achievement
- **Background**: Circular or shield shape with decorative elements
- **Mascot**: Optional Math Pup character integration
- **Badge Category**: Visual cues that align with badge type (stars, ribbons, medals)

## File Specifications

### Source Files (.pxd)
- **Format**: Pixelmator Pro document (.pxd)
- **Resolution**: 512x512px or higher
- **Layers**: Organized layers for easy editing
- **Location**: `project-resources/badges-exported/`
- **Naming**: `badge_[name_in_snake_case].pxd`

### Exported Images (.webp)
- **Format**: WebP (best compression for web/mobile)
- **Size**: 512x512px (xxxhdpi density)
- **Quality**: 85-90% (balance between size and quality)
- **Location**: `app/src/main/res/drawable-xxxhdpi/`
- **Naming**: `badge_[name_in_snake_case].webp`

## Creating New Badges

### 1. Design Phase

#### Using AI Tools (Recommended)
Use the following prompt template with Math Pup mascot reference image:

```
Create a badge icon for a kids math learning app featuring a friendly puppy mascot.

Badge Details:
- Name: [Badge Name]
- Achievement: [What this badge represents]
- Category: [Getting Started/Volume/Operation Mastery/Speed & Accuracy/Streak/Games]
- Style: Cartoon illustration matching the friendly puppy mascot theme

Design Requirements:
- Circular or shield-shaped badge
- Vibrant, child-friendly colors
- Clear central icon representing the achievement
- Simple, recognizable design for small sizes
- Optional mascot integration
- Size: 512x512px
- Format: High-resolution PNG or WebP

Reference the attached Math Pup mascot for consistent styling.
```

#### Manual Design (Pixelmator Pro)
1. Open existing badge .pxd file as template
2. Duplicate and modify for new badge concept
3. Maintain consistent canvas size (512x512px)
4. Use organized layer structure
5. Save as `badge_[name].pxd`

### 2. Export Process

#### From Pixelmator Pro
1. Open the .pxd source file
2. Go to **File > Export**
3. Settings:
   - Format: **WebP**
   - Size: **512x512px**
   - Quality: **85-90%**
4. Save to: `app/src/main/res/drawable-xxxhdpi/`
5. Filename: `badge_[name_in_snake_case].webp`

#### Batch Export (if needed)
```bash
# Use ImageMagick or similar tool for batch conversion
# Example: Convert PNG to WebP
for file in *.png; do
    cwebp -q 85 "$file" -o "${file%.png}.webp"
done
```

### 3. Code Integration

#### Step 1: Add Enum Value
Add new badge type to `BadgeIcon.kt` enum:

```kotlin
enum class BadgeIcon {
    // Existing badges...
    NEW_BADGE_NAME,  // Add new entry
}
```

#### Step 2: Update Mapper
Add mapping in `BadgeIconMapper.kt`:

```kotlin
fun toDrawableRes(icon: BadgeIcon): Int {
    return when (icon) {
        // Existing mappings...
        BadgeIcon.NEW_BADGE_NAME -> R.drawable.badge_new_badge_name
    }
}
```

#### Step 3: Define Badge
Add badge definition in `BadgeDefinitions.kt`:

```kotlin
Badge(
    id = "new_badge_id",
    name = "Badge Name",
    description = "Achievement description",
    category = BadgeCategory.APPROPRIATE_CATEGORY,
    icon = BadgeIcon.NEW_BADGE_NAME,
    unlockCriteria = "Unlock condition",
    isUnlocked = false,
    earnedDate = null
)
```

#### Step 4: Run Code Quality Checks
```bash
# Format code
./gradlew formatKotlin

# Build to verify
./gradlew assembleDebug
```

#### Step 5: Update Changelog
Add entry to `CHANGELOG.md` under `[Unreleased]` section:

```markdown
### Added
- New badge: [Badge Name] for [achievement description]
```

## Badge Categories

Current categories and their themes:

| Category | Theme | Visual Style |
|----------|-------|--------------|
| **Getting Started** | First achievements | Stars, simple shapes |
| **Volume** | Problem quantity milestones | Numbers, stacks |
| **Operation Mastery** | Math operation skills | +, -, × symbols |
| **Speed & Accuracy** | Performance quality | Lightning, targets |
| **Streak** | Consistency | Flames, calendars |
| **Games** | Mini-game achievements | Game controllers, trophies |

## Naming Conventions

### File Names (snake_case)
- `badge_first_steps.pxd` / `badge_first_steps.webp`
- `badge_math_champion.pxd` / `badge_math_champion.webp`
- `badge_perfect_race.pxd` / `badge_perfect_race.webp`

### Enum Names (SCREAMING_SNAKE_CASE)
- `FIRST_STEPS`
- `MATH_CHAMPION`
- `PERFECT_RACE`

### Display Names (Title Case)
- "First Steps"
- "Math Champion"
- "Perfect Race"

## Quality Checklist

Before finalizing a new badge:

- [ ] Source .pxd file saved in `project-resources/badges-exported/`
- [ ] WebP exported at 512x512px, 85-90% quality
- [ ] File placed in `app/src/main/res/drawable-xxxhdpi/`
- [ ] Follows snake_case naming convention
- [ ] Matches Math Pup mascot visual style
- [ ] Clearly readable at small sizes (40-80dp)
- [ ] BadgeIcon enum updated
- [ ] BadgeIconMapper updated
- [ ] BadgeDefinitions updated
- [ ] Code formatted with kotlinter
- [ ] App builds successfully
- [ ] CHANGELOG.md updated
- [ ] Tested in all UI contexts (grid, dialog, home screen)

## Existing Badges Reference

### Getting Started (3)
- First Steps, Perfect Start, Perfect 10

### Volume (4)
- Math Rookie, Math Explorer, Math Champion, Math Legend

### Operation Mastery (3)
- Addition Expert, Subtraction Star, Mix Master

### Speed & Accuracy (3)
- Quick Thinker, Sharp Shooter, Perfectionist

### Streak (2)
- Streak Starter, Dedication Award

### Games (4)
- Game Master, Speed Demon, Racing Champion, Perfect Race

**Total: 19 badges**

## Resources

- **AI Image Generation**: ChatGPT, Midjourney, DALL-E 3, Stable Diffusion
- **Design Software**: Pixelmator Pro (Mac), Adobe Photoshop, Figma
- **Image Optimization**: cwebp (WebP encoder), ImageMagick
- **Material 3 Guidelines**: https://m3.material.io/
- **Project Structure**: See `/COPILOT_INSTRUCTIONS.md`

## Tips

1. **Consistency is Key**: Always reference existing badges for style consistency
2. **Test at Multiple Sizes**: Badges appear at 40dp, 56dp, 80dp in different contexts
3. **Consider Color Themes**: Design works well with both light and dark mode
4. **Child-Friendly**: Avoid complex details, use bold shapes and bright colors
5. **Performance**: WebP format provides 25-35% better compression than PNG
6. **Version Control**: Keep all .pxd source files for future edits

## Questions?

For technical questions about badge integration, see the project's Circuit and Metro DI documentation in `/COPILOT_INSTRUCTIONS.md`.
