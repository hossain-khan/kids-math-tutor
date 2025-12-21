# Firebase Console Dashboard Setup Guide

**Purpose**: Configure Firebase Analytics dashboards, funnels, and audiences for Kids Math Pup Tutor  
**Version**: 1.0  
**Last Updated**: December 21, 2025

---

## Overview

This guide walks through setting up Firebase Console for optimal analytics tracking and insights.

**What We'll Create**:
1. Custom Dashboard with 7+ cards
2. 2 Conversion Funnels
3. 5 User Audiences
4. Event-based Notifications

---

## Prerequisites

- Firebase project for Kids Math Pup Tutor
- Admin or Editor access to the project
- At least 24-48 hours of production data for meaningful insights

---

## 1. Custom Dashboard Setup

### Create Dashboard

1. Open [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Analytics → Custom Dashboards**
4. Click **Create Dashboard**
5. Name it: **"Kids Math Tutor - User Engagement"**

### Add Cards

Click **Add Card** for each of the following:

#### Card 1: Daily Active Users (DAU)

- **Type**: Score card
- **Metric**: Active users
- **Time period**: Last 7 days
- **Filters**: None
- **Position**: Top-left

#### Card 2: Screen Views by Screen Name

- **Type**: Bar chart
- **Metric**: Event count
- **Event name**: screen_view
- **Breakdown dimension**: screen_name
- **Time period**: Last 7 days
- **Sort**: Descending by count
- **Position**: Top-center

Expected screens to see:
- Home
- Math Practice
- Practice Results
- Operation Selector
- Badges
- Stats
- Settings
- Game Selection
- Math Race
- Onboarding
- Grade Selection
- Name Entry
- Audio & Haptic Settings

#### Card 3: Practice Sessions Completed

- **Type**: Line chart
- **Metric**: Event count
- **Event name**: practice_session_completed
- **Time period**: Last 30 days
- **Position**: Top-right

#### Card 4: Badge Unlocks

- **Type**: Score card
- **Metric**: Event count
- **Event name**: badge_unlocked
- **Time period**: Last 30 days
- **Position**: Middle-left

#### Card 5: Game Sessions

- **Type**: Bar chart
- **Metric**: Event count
- **Event name**: game_completed
- **Breakdown dimension**: game_id
- **Time period**: Last 30 days
- **Position**: Middle-center

#### Card 6: Average Session Duration

- **Type**: Score card
- **Metric**: Average engagement time
- **Time period**: Last 7 days
- **Position**: Middle-right

#### Card 7: User Retention

- **Type**: Cohort analysis
- **Cohort by**: First open date
- **Return criterion**: Any event
- **Time period**: Last 30 days
- **Position**: Bottom row

### Save Dashboard

Click **Save** to finalize your dashboard.

---

## 2. Conversion Funnels

### Funnel 1: Onboarding Completion

**Purpose**: Track how many users complete the onboarding process.

1. Navigate to **Analytics → Funnels**
2. Click **Create Funnel**
3. Name: **"Onboarding Completion"**
4. Add steps:
   - Step 1: `onboarding_started` (event)
   - Step 2: `grade_selected` (event)
   - Step 3: `name_entered` (event)
   - Step 4: `onboarding_completed` (event)
5. Time window: 10 minutes (default)
6. Click **Save**

**Expected Insights**:
- Drop-off rate at each step
- Percentage of users who skip name entry
- Overall onboarding completion rate

### Funnel 2: Practice Flow

**Purpose**: Track users from selecting an operation to completing a session.

1. Navigate to **Analytics → Funnels**
2. Click **Create Funnel**
3. Name: **"Practice Flow"**
4. Add steps:
   - Step 1: `screen_view` with `screen_name: "Operation Selector"`
   - Step 2: `operation_selected` (event)
   - Step 3: `practice_session_started` (event)
   - Step 4: `practice_session_completed` (event)
5. Time window: 30 minutes
6. Click **Save**

**Expected Insights**:
- Drop-off rate between starting and completing sessions
- Most popular operations (via `operation_type` parameter)
- Average time to complete a session

---

## 3. User Audiences

### Create Audiences

Navigate to **Analytics → Audiences** and create the following:

#### Audience 1: Active Learners

- **Name**: Active Learners
- **Description**: Users with 10+ practice sessions
- **Condition**:
  - Event: `practice_session_completed`
  - Count: Greater than or equal to 10
  - Time window: Last 30 days

**Use Case**: Target with advanced features or new game announcements.

#### Audience 2: Badge Collectors

- **Name**: Badge Collectors
- **Description**: Users with 5+ badges unlocked
- **Condition**:
  - User property: `total_badges_unlocked`
  - Value: Greater than or equal to 5

**Use Case**: Promote new badge achievements or special challenges.

#### Audience 3: Game Players

- **Name**: Game Players
- **Description**: Users who've completed at least one game
- **Condition**:
  - Event: `game_completed`
  - Count: Greater than or equal to 1
  - Time window: Last 90 days

**Use Case**: Target with new game releases or game-related features.

#### Audience 4: Kindergarten Users

- **Name**: Kindergarten Users
- **Description**: Users in Kindergarten grade level
- **Condition**:
  - User property: `grade_level`
  - Value: Equals "KINDERGARTEN"

**Use Case**: Tailor content and features for K-level students.

#### Audience 5: Streak Champions

- **Name**: Streak Champions
- **Description**: Users with 7+ day practice streak
- **Condition**:
  - User property: `current_streak`
  - Value: Greater than or equal to 7

**Use Case**: Celebrate and encourage consistent learners.

---

## 4. Event-Based Notifications

### Create Alerts

Navigate to **Analytics → Notifications** (or Alerts depending on UI version).

#### Alert 1: Error Spike

- **Name**: Error Spike Alert
- **Condition**:
  - Event: `error_occurred`
  - Threshold: 20% increase over previous period
  - Time window: 1 hour
- **Notification method**: Email to dev team
- **Purpose**: Quickly detect and respond to app crashes or errors

#### Alert 2: Engagement Drop

- **Name**: Daily Active Users Drop
- **Condition**:
  - Metric: Active users
  - Threshold: 30% decrease compared to previous day
  - Time window: 1 day
- **Notification method**: Email to product team
- **Purpose**: Identify retention issues early

---

## 5. Event-Level Insights

### Key Events to Monitor

Navigate to **Analytics → Events** to see all tracked events.

#### Most Important Events

1. **practice_session_completed**
   - Monitor: Total count, daily trend
   - Parameters to explore: `operation_type`, `accuracy`

2. **badge_unlocked**
   - Monitor: Which badges are most/least unlocked
   - Parameter: `badge_id`

3. **game_completed**
   - Monitor: Game popularity
   - Parameters: `game_id`, `game_score`

4. **error_occurred**
   - Monitor: Error frequency and types
   - Parameters: `error_context`, `is_fatal`

### Mark Events as Conversions

Mark these events as conversions for better reporting:

1. `onboarding_completed`
2. `practice_session_completed`
3. `badge_unlocked`
4. `game_completed`

**To mark as conversion**:
1. Go to **Analytics → Events**
2. Find the event
3. Toggle "Mark as conversion" switch

---

## 6. User Properties Exploration

Navigate to **Analytics → User Properties** to see all tracked properties.

### Key Properties

- `grade_level` - Segment users by education level
- `total_problems_solved` - Identify power users
- `current_streak` - Find engaged daily users
- `total_badges_unlocked` - Measure achievement completion
- `has_completed_onboarding` - Separate new vs returning users

### Create Custom Reports

Use user properties to create custom reports:

1. Go to **Analytics → Explorations**
2. Click **Create New Exploration**
3. Add dimensions: User properties (e.g., grade_level)
4. Add metrics: Event counts, session duration
5. Create segments based on user behavior

---

## 7. Integration with BigQuery (Optional)

For advanced analytics, export data to BigQuery:

1. Navigate to **Project Settings → Integrations**
2. Find **BigQuery**
3. Click **Link**
4. Choose:
   - Export all events daily
   - Include user properties
   - Include device information
5. Click **Link Project**

**Benefits**:
- Run complex SQL queries
- Join with external data sources
- Build custom ML models
- Create advanced visualizations

---

## 8. Maintenance Schedule

### Daily
- Check DebugView for real-time event verification
- Review error_occurred events

### Weekly
- Review User Engagement dashboard
- Check funnel drop-off rates
- Investigate any alert notifications

### Monthly
- Review user property distributions
- Analyze audience sizes and trends
- Update audiences based on new insights
- Review and refine conversion funnels

---

## Expected Metrics (First Month)

After the first month of production, expect to see:

- **DAU**: 50-500 (depends on distribution)
- **Screen Views**: 1000+ daily
- **Practice Sessions**: 100-1000 daily
- **Badge Unlocks**: 10-100 daily
- **Game Sessions**: 20-200 daily
- **Onboarding Completion**: 60-80%
- **Practice Session Completion**: 70-90%

---

## Troubleshooting

### Events Not Appearing in Reports

**Issue**: Events show in DebugView but not in Analytics reports.

**Solution**:
- Wait 24-48 hours for data processing
- Check that the app is in production mode (not debug)
- Verify google-services.json is properly configured

### User Properties Not Showing

**Issue**: User properties don't appear in the User Properties tab.

**Solution**:
- Wait 24 hours for properties to appear
- Verify properties are set via `setUserProperty()` in code
- Check that property names match UserProperty constants

### Dashboards Show No Data

**Issue**: Dashboard cards are empty.

**Solution**:
- Wait 24-48 hours after first app launch
- Change time period to "Last 30 days"
- Verify events are being logged (check Events tab)

---

## Resources

- [Firebase Analytics Documentation](https://firebase.google.com/docs/analytics)
- [Custom Dashboards Guide](https://support.google.com/firebase/answer/9237506)
- [Funnel Analysis](https://support.google.com/firebase/answer/9327635)
- [Audiences](https://support.google.com/firebase/answer/6317509)
- [BigQuery Export](https://firebase.google.com/docs/analytics/bigquery-export)

---

## Next Steps

After setting up the console:

1. ✅ Verify all dashboards are created
2. ✅ Test funnels with sample data
3. ✅ Configure email notifications for alerts
4. ✅ Share dashboard access with team members
5. ✅ Schedule weekly review meeting
6. ✅ Document insights and action items
