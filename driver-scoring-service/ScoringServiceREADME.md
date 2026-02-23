🏎️ Driver Scoring Service API Endpoints
1. Get Driver Score
   Returns the scoring details for a specific driver.

   URL: GET /{driverId}/score
   Path Parameters:
   driverId (String): Must match format DRV-XXXX (e.g., DRV-1234).
   Query Parameters:
   Supports standard Spring Data Pagination (e.g., ?page=0&size=10).
   Success Response: 200 OK with DriverScoreDetails.
   Error Response: 404 Not Found if the driver doesn't exist, or 400 Bad Request for invalid ID formats.

2. Get Violation History
   Retrieves violations for a driver within a specific timeframe.

   URL: GET /{driverId}/violations
   Query Parameters:
   days (int): Number of days to look back. Default is 30. Max 1000.
   Supports standard Spring Data Pagination.
   Success Response: 200 OK with ViolationHistoryResponse.
   Error Response: 404 Not Found if the driver doesn't exist.

3. Get Leaderboard
   Fetches the top-performing drivers.

   URL: GET /leaderboard
   Query Parameters:
   limit (int): Number of drivers to return. Default 10. Max 50.
   Success Response: 200 OK with LeaderboardResponse.

4. Get Bottom Drivers
   Fetches the lowest-performing drivers.

   URL: GET /bottom
   Query Parameters:
   limit (int): Number of drivers to return. Default 10. Max 50.
   Success Response: 200 OK with BottomDriversResponse.