## GitHub Organization Repository Access Report

### Overview

This is a Spring Boot backend service that integrates with the GitHub API to generate a report showing which users have access to which repositories within a GitHub organization.

The service authenticates using GitHub OAuth, retrieves repositories for a given organization, identifies collaborators for each repository, and aggregates the data to produce a structured access report in JSON format.

---

### Features

GitHub OAuth authentication

Fetch repositories for a given organization

Retrieve collaborators for each repository

Generate aggregated user → repository mapping report

Efficient processing for large organizations

Global error handling for API failures



---

### Technology Stack

Java 17

Spring Boot

Spring Security OAuth2 Client

GitHub REST API

Maven



---

### GitHub Repository

`https://github.com/kapilBaser/github-access-report`


---

### Authentication Configuration

The application uses **GitHub OAuth**.

1. Create a new OAuth app in GitHub:
**GitHub → Settings → Developer Settings → OAuth Apps → New OAuth App**

Use the following configuration:

**Homepage URL:**
 `http://localhost:5055`

**Authorization callback URL:**
`http://localhost:5055/login/oauth2/code/github`



2. Configure application.properties with your GitHub credentials:


```
spring.security.oauth2.client.registration.github.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.github.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.github.scope=read:user,repo,read:org

```

Required GitHub permissions:

`read:user,repo`
`read:org`


> These allow the application to read organization and repository data.




---

### Running the Project

1. Clone the repository


```
git clone https://github.com/kapilBaser/github-access-report.git`
cd github-access-report
```

2. Configure GitHub OAuth credentials in application.properties


3. Build the project



`mvn clean install`

4. Run the application



`mvn spring-boot:run`

Server will start at: `http://localhost:5055`


---

### Login Flow

All endpoints are secured. To access the report:

**`GET /api/github/access-report/{org}`**

If the user is not logged in, they will be automatically redirected to GitHub login.

After authentication, the OAuth token is obtained to call GitHub APIs.


> To see all available organizations that the authenticated user has access to:



**`GET /api/github/organizations`**


---

### API Endpoints

1. Generate Organization Access Report



Endpoint:


`GET /api/github/access-report/{org}`

**Example:**


`GET http://localhost:5055/api/github/access-report/xyz-org`

Response Example:

```
{
  "alice": ["repo1", "repo2"],
  "bob": ["repo1"],
  "charlie": ["repo3"]
}
```
```
Pagination (optional):
>For organization repositories: page (default 1), perPage (default 10)
>For repository collaborators: page (default 1), perPage (default 100)
>Use query parameters to customize results, e.g., /api/github/access-report/xyz-org?page=2&perPage=20
```

2. List All Organizations



Endpoint:


`GET /api/github/organizations`

**Example:**


`GET http://localhost:5055/api/github/organizations`

Response Example:

```
[
  "Test-By-Kap",
  "Example-Org",
  "MyGitHubOrg"
]
```

---

### GitHub APIs Used

Get organization repositories: `GET /orgs/{org}/repos`

Get repository collaborators: `GET /repos/{org}/{repo}/collaborators`


> These APIs provide the data required to generate the access report.




---

### Implementation Design

**Controller:** Handles REST API requests

**Service:** Contains business logic for generating access report

**GitHub Client:** Handles communication with GitHub APIs

**Exception Handling:** Global error handling using **@RestControllerAdvice**



---

### Scalability Design

Designed for organizations with:

100+ repositories

1000+ users


Key optimizations:

**Parallel API Calls:** Repository collaborator requests are executed asynchronously

**Concurrent Data Structures:** Thread-safe ConcurrentHashMap aggregates user → repository mapping

**Pagination:** GitHub API results fetched with per_page=n to reduce calls

---

### Error Handling

Global error handling with @RestControllerAdvice for:

Unauthorized access (401)

Forbidden access (403)

Resource not found (404)

GitHub API errors

Internal server errors


> If authentication expires, users are redirected to GitHub login.

---

### Note
**It is recommended to test the APIs using a web browser because GitHub OAuth requires a redirect-based login flow.
Tools like Postman may not handle OAuth redirects properly unless a separate redirect URL configuration is provided.**

---

### Assumptions

Authenticated user has access to the organization

GitHub API rate limits are not exceeded

User has permission to view repository collaborators

