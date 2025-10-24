## description: Run tests for both JDBC and JPA profiles and analyze results

Run the test suite for the Task Management System with both profiles:

1. First, run all tests with the JDBC profile:
    - Execute: `./mvnw clean test -Dspring.profiles.active=jdbc`
    - Capture and analyze the output
2. Then, run all tests with the JPA profile:
    - Execute: `./mvnw clean test -Dspring.profiles.active=jpa`
    - Capture and analyze the output
3. For each profile:
    - Report the number of tests run, passed, and failed
    - If any tests fail, provide:
        - The test class and method name
        - The error message and stack trace summary
        - Root cause analysis of what went wrong
        - Specific recommendations for fixing the issue
4. Compare results between profiles and note any differences
5. Provide a summary of overall test health and any action items